(ns generator.deps
  "Dependency resolution for protobuf files.
  Builds dependency graphs and performs topological sorting to ensure
  files are processed in the correct order."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [generator.specs :as specs]
            [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
            [com.stuartsierra.dependency :as dep]
            [com.rpl.specter :as sp]
            [clojure.core.match :refer [match]]
            [malli.core :as m]))

;; =============================================================================
;; Dependency Graph Building
;; =============================================================================

(>defn extract-file-dependencies
  "Extract dependency information from a file descriptor.
  Returns {:name file-name :package package :depends-on [dep1 dep2 ...]}"
  [file-desc]
  [[:map 
    [:type [:= :file]]
    [:name string?]
    [:package string?]
    [:dependencies {:optional true} [:vector string?]]
    [:dependency {:optional true} [:vector string?]]]
   => 
   [:map
    [:name string?]
    [:package string?]
    [:depends-on [:vector string?]]]]
  {:name (:name file-desc)
   :package (:package file-desc)
   :depends-on (vec (remove #{"buf/validate/validate.proto"
                              "google/protobuf/descriptor.proto"
                              "google/protobuf/duration.proto"
                              "google/protobuf/timestamp.proto"}
                            ;; Handle both :dependency (from JSON) and :dependencies (from EDN)
                            (or (:dependencies file-desc)
                                (:dependency file-desc)
                                [])))})

(>defn build-dependency-graph
  "Build a dependency graph from descriptor set.
  Returns both a Stuart Sierra dependency graph and our file metadata map."
  [descriptor-set]
  [[:map 
    [:type [:= :descriptor-set]] 
    [:files [:vector [:map [:name string?] [:package string?]]]]] 
   => 
   [:map 
    [:graph any?] ;; Stuart Sierra's graph type
    [:file-info [:map-of string? [:map
                                   [:name string?]
                                   [:package string?]
                                   [:depends-on [:vector string?]]]]]]]
  (let [files (get-in descriptor-set [:files])
        file-info (into {}
                        (map (fn [file]
                               [(:name file) (extract-file-dependencies file)])
                             files))
        ;; Build Stuart Sierra dependency graph
        graph (reduce (fn [g [filename node]]
                        (if (empty? (:depends-on node))
                          ;; For files with no dependencies, add a dummy dep then remove it
                          ;; This ensures the node exists in the graph
                          (-> g
                              (dep/depend filename ::dummy)
                              (dep/remove-edge filename ::dummy)
                              (dep/remove-all ::dummy))
                          ;; For files with dependencies, add them normally
                          (reduce (fn [g2 dep]
                                    (dep/depend g2 filename dep))
                                  g
                                  (:depends-on node))))
                      (dep/graph)
                      file-info)]
    {:graph graph
     :file-info file-info}))

;; =============================================================================
;; Topological Sort
;; =============================================================================

(>defn topological-sort
  "Perform topological sort on dependency graph using Stuart Sierra's library.
  Returns files in order they should be processed."
  [graph]
  [any? => [:vector string?]]
  (try
    (vec (dep/topo-sort graph))
    (catch Exception e
      (if (re-find #"circular|cycle" (.getMessage e))
        (throw (ex-info "Circular dependency detected in proto files"
                        {:error (.getMessage e)
                         :graph graph}))
        (throw e)))))

;; =============================================================================
;; Symbol Registry Building
;; =============================================================================

(>defn collect-file-symbols
  "Collect all symbols (enums and messages) defined in a file."
  [file]
  [[:map 
    [:package string?]
    [:enums {:optional true} [:vector map?]]
    [:messages {:optional true} [:vector map?]]]
   => 
   [:vector [:map
             [:fqn string?]
             [:type [:enum :enum :message]]
             [:definition map?]]]]
  (let [package (:package file)
        ;; Helper to build fully qualified name
        make-fqn (fn [parent-names item]
                   (str "." package 
                        (when (seq parent-names)
                          (str "." (str/join "." parent-names)))
                        "." (:proto-name item)))
        ;; Recursively collect from nested types
        collect-nested (fn collect-nested [parent-names items]
                         (mapcat (fn [item]
                                   (case (:type item)
                                     :enum [{:fqn (make-fqn parent-names item)
                                             :type :enum
                                             :definition item}]
                                     :message (concat [{:fqn (make-fqn parent-names item)
                                                        :type :message
                                                        :definition item}]
                                                      (collect-nested (conj parent-names (:proto-name item))
                                                                      (:nested-types item [])))))
                                 items))]
    (vec
     (concat
      ;; Top-level enums
      (map (fn [enum]
             {:fqn (make-fqn [] enum)
              :type :enum
              :definition enum})
           (:enums file []))
      ;; Top-level messages and their nested types
      (mapcat (fn [msg]
                (concat [{:fqn (make-fqn [] msg)
                          :type :message
                          :definition msg}]
                        (collect-nested [(:proto-name msg)]
                                        (:nested-types msg []))))
              (:messages file []))))))

(>defn build-symbol-registry
  "Build a global registry of all symbols in dependency order."
  [descriptor-set sorted-files]
  [[:map [:files [:vector map?]]] 
   [:vector string?] 
   => 
   [:map-of string? [:map
                     [:fqn string?]
                     [:type [:enum :enum :message]]
                     [:definition map?]]]]
  (let [file-map (into {} 
                       (map (fn [f] [(:name f) f])
                            (:files descriptor-set)))]
    (reduce (fn [registry filename]
              (let [file (get file-map filename)
                    symbols (collect-file-symbols file)]
                (reduce (fn [reg sym]
                          (assoc reg (:fqn sym) sym))
                        registry
                        symbols)))
            {}
            sorted-files)))

;; =============================================================================
;; IR Enrichment with Specter and core.match
;; =============================================================================

;; Define Specter paths for common transformations
(def ^:private ALL-FIELDS-PATH
  "Path to all fields in all messages"
  [:messages sp/ALL :fields sp/ALL])

(>defn- enrich-type-reference
  "Enrich a type reference with resolved information from the symbol registry."
  [type-ref symbol-registry current-package]
  [string? 
   [:map-of string? any?] 
   string? 
   => 
   [:map
    [:type-ref string?]
    [:resolved {:optional true} any?]
    [:cross-namespace {:optional true} boolean?]
    [:target-package {:optional true} string?]]]
  (let [resolved (get symbol-registry type-ref)
        target-package (when resolved
                         (-> resolved :definition :package))]
    (cond-> {:type-ref type-ref}
      resolved (assoc :resolved resolved)
      (and target-package (not= target-package current-package))
      (assoc :cross-namespace true
             :target-package target-package))))

(>defn- enrich-field-type
  "Enrich the type portion of a field based on its structure."
  [field-type symbol-registry current-package]
  [map? [:map-of string? any?] string? => map?]
  (match field-type
    ;; Scalar type - return as-is
    {:scalar _} field-type
    
    ;; Message type with type-ref
    {:message msg-type} 
    (if-let [type-ref (:type-ref msg-type)]
      {:message (merge msg-type
                       (enrich-type-reference type-ref symbol-registry current-package))}
      field-type)
    
    ;; Enum type with type-ref
    {:enum enum-type}
    (if-let [type-ref (:type-ref enum-type)]
      {:enum (merge enum-type
                    (enrich-type-reference type-ref symbol-registry current-package))}
      field-type)
    
    ;; Any other structure - return as-is
    :else field-type))

(>defn enrich-file
  "Enrich a file with resolved type references using Specter."
  ^{:no-doc true}  ; Mark as no-doc but keep public for testing
  [file symbol-registry]
  [[:map 
    [:package string?]
    [:messages {:optional true} [:vector map?]]]
   [:map-of string? any?]
   => 
   map?]
  (let [current-package (:package file)]
    ;; Use Specter to transform all field types in one pass
    (sp/transform 
     ALL-FIELDS-PATH
     (fn [field]
       (update field :type 
               #(enrich-field-type % symbol-registry current-package)))
     file)))

;; =============================================================================
;; Public API
;; =============================================================================

(>defn enrich-descriptor-set
  "Enrich a descriptor set with dependency information and resolved references.
  This is the main entry point for the dependency resolution system."
  [descriptor-set]
  [[:map 
    [:type [:= :descriptor-set]]
    [:files [:vector map?]]]
   => 
   [:map 
    [:type [:= :combined]]
    [:files [:vector map?]]
    [:dependency-graph map?]
    [:sorted-files [:vector string?]]
    [:symbol-registry [:map-of string? any?]]]]
  (let [;; Build dependency graph
        {:keys [graph file-info]} (build-dependency-graph descriptor-set)
        ;; Sort files by dependencies
        sorted-files (topological-sort graph)
        ;; Build symbol registry
        symbol-registry (build-symbol-registry descriptor-set sorted-files)
        ;; Enrich the descriptor set
        result (-> descriptor-set
                   (assoc :dependency-graph {:nodes (set (keys file-info))
                                             :edges (into {}
                                                          (map (fn [[k v]]
                                                                 [k (set (:depends-on v))])
                                                               file-info))
                                             :file->package (into {}
                                                                  (map (fn [[k v]]
                                                                         [k (:package v)])
                                                                       file-info))}
                          :sorted-files sorted-files
                          :symbol-registry symbol-registry
                          :type :combined)  ; Change type for enriched version
                   (update :files (fn [files]
                                    (mapv #(enrich-file % symbol-registry)
                                          files))))]
    ;; Validate output
    (specs/validate! specs/EnrichedDescriptorSet result "enrich-descriptor-set output")
    result))