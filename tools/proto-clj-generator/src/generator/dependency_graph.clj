(ns generator.dependency-graph
  "Build and analyze dependency graphs for proto files.
  Ensures correct namespace requires and compilation order."
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [com.rpl.specter :as sp]))

;; =============================================================================
;; Dependency Extraction
;; =============================================================================

(defn proto-file->namespace
  "Convert proto filename to Clojure namespace suffix.
  e.g. 'jon_shared_cmd_compass.proto' -> 'cmd.compass'
  NOTE: This is only used for fallback when package info is not available."
  [proto-file]
  (-> proto-file
      (str/replace #"\.proto$" "")
      (str/replace #"jon_shared_" "")
      ;; Handle special cases first
      ;; ALL data_* files map to "ser" namespace (consolidated)
      (str/replace #"^data_.*$" "ser")
      (str/replace #"^cmd_" "cmd.")
      ;; Convert remaining underscores to dots
      (str/replace #"_" "."))) ; then back to dots

(defn package->namespace
  "Convert protobuf package to Clojure namespace suffix.
  e.g. 'cmd.DayCamera' -> 'cmd.daycamera'"
  [proto-package]
  (-> proto-package
      (str/lower-case)
      (str/replace "_" "-")))

(defn extract-imports-from-file
  "Extract import dependencies from a file EDN structure."
  [file-edn]
  ;; Use actual dependencies from backend
  (:dependencies file-edn []))

;; =============================================================================
;; Graph Building
;; =============================================================================

(defn build-dependency-graph
  "Build a dependency graph from backend output.
  Returns {:nodes #{...} :edges {node #{deps...}}} where nodes are package names."
  [backend-output]
  (let [all-files (concat (get-in backend-output [:command :files])
                          (get-in backend-output [:state :files]))
        ;; Build a map from filename to package for dependency resolution
        file->package (reduce (fn [acc file]
                               (assoc acc (:name file) (:package file)))
                             {}
                             all-files)
        ;; Nodes are packages, not filenames
        nodes (set (map :package all-files))
        ;; Build edges based on package dependencies
        edges (reduce (fn [acc file]
                        (let [package (:package file)
                              import-files (extract-imports-from-file file)
                              ;; Convert import filenames to packages
                              import-packages (set (keep #(get file->package %) import-files))]
                          (assoc acc package import-packages)))
                      {}
                      all-files)]
    {:nodes nodes
     :edges edges
     :file->package file->package}))

;; =============================================================================
;; Graph Analysis
;; =============================================================================

(defn topological-sort
  "Perform topological sort on dependency graph.
  Returns ordered sequence of nodes or throws if cycle detected."
  [{:keys [nodes edges]}]
  (loop [sorted []
         remaining nodes
         in-degree (reduce (fn [acc [node deps]]
                             (reduce (fn [acc' dep]
                                       (update acc' dep (fnil inc 0)))
                                     acc
                                     deps))
                           (zipmap nodes (repeat 0))
                           edges)]
    (if (empty? remaining)
      sorted
      (let [ready (filter #(zero? (in-degree %)) remaining)]
        (if (empty? ready)
          (throw (ex-info "Circular dependency detected!" 
                          {:remaining remaining
                           :in-degree in-degree}))
          (let [node (first ready)
                deps (edges node #{})]
            (recur (conj sorted node)
                   (disj remaining node)
                   (reduce (fn [acc dep]
                             (update acc dep dec))
                           in-degree
                           deps))))))))

(defn find-dependencies
  "Find all dependencies for a given package."
  [graph package]
  (loop [to-visit [package]
         visited #{}
         deps #{}]
    (if (empty? to-visit)
      deps
      (let [current (first to-visit)
            remaining (rest to-visit)]
        (if (visited current)
          (recur remaining visited deps)
          (let [direct-deps (get-in graph [:edges current] #{})
                new-deps (set/difference direct-deps visited)]
            (recur (concat remaining new-deps)
                   (conj visited current)
                   (set/union deps new-deps))))))))

;; =============================================================================
;; Namespace Mapping
;; =============================================================================

(defn package-to-clj-requires
  "Convert package dependencies to Clojure require specs.
  Returns a vector of require specs like [potatoclient.proto.ser.types :as types]"
  [current-package dep-packages ns-prefix]
  (let [current-ns (package->namespace current-package)
        deps-namespaces (map package->namespace dep-packages)
        ;; Filter out self-dependencies (same namespace)
        external-deps (remove #(= % current-ns) deps-namespaces)
        ;; Track used aliases to avoid duplicates
        used-aliases (atom {})]
    (vec (for [dep-ns external-deps
               :let [full-ns (str ns-prefix "." dep-ns)
                     ;; Generate a meaningful alias from the namespace
                     base-alias (cond
                                 (= dep-ns "ser") "types"  ;; Special case for common types
                                 ;; For nested namespaces like cmd.daycamera, use last part
                                 :else (last (str/split dep-ns #"\.")))
                     ;; Handle duplicate aliases by appending a number
                     alias (loop [candidate base-alias
                                  n 2]
                             (if (contains? @used-aliases candidate)
                               (recur (str base-alias n) (inc n))
                               (do (swap! used-aliases assoc candidate true)
                                   candidate)))]]
           [(symbol full-ns) :as (symbol alias)]))))

(defn analyze-type-references
  "Analyze a message to find external type references.
  Returns set of referenced packages."
  [message type-lookup]
  (let [extract-type-refs (fn extract-refs [field]
                           (when-let [type-ref (or (get-in field [:type :message :type-ref])
                                                  (get-in field [:type :enum :type-ref]))]
                             ;; Remove leading dot
                             (let [clean-ref (if (str/starts-with? type-ref ".")
                                              (subs type-ref 1)
                                              type-ref)]
                               (when-let [type-def (get type-lookup clean-ref)]
                                 (:package type-def)))))
        ;; Extract from regular fields
        field-refs (keep extract-type-refs (:fields message))
        ;; Extract from oneof fields
        oneof-refs (for [oneof (:oneofs message)
                         field (:fields oneof)]
                    (extract-type-refs field))
        ;; Extract from nested types (if any)
        nested-refs (when (:nested-types message)
                     (mapcat #(analyze-type-references % type-lookup)
                            (:nested-types message)))]
    ;; Return set of all referenced packages, excluding current package
    (set (remove #(or (nil? %) (= % (:package message)))
                (concat field-refs oneof-refs nested-refs)))))

;; =============================================================================
;; Main API
;; =============================================================================

(defn analyze-dependencies
  "Analyze dependencies and return enriched backend output.
  Adds :clj-requires key to each file with require specs."
  [backend-output ns-prefix]
  (let [graph (build-dependency-graph backend-output)
        file->package (:file->package graph)
        ;; Build enhanced file data with dependencies
        enhance-file (fn [file]
                       (let [current-package (:package file)
                             proto-deps (:dependencies file [])
                             ;; Convert file dependencies to package dependencies
                             dep-packages (set (keep #(get file->package %) proto-deps))
                             requires (package-to-clj-requires current-package dep-packages ns-prefix)]
                         (assoc file :clj-requires requires)))]
    ;; Return backend output with enhanced files
    (-> backend-output
        (update-in [:command :files] #(mapv enhance-file %))
        (update-in [:state :files] #(mapv enhance-file %))
        (assoc :dependency-graph graph))))

(defn compilation-order
  "Get compilation order for all proto files."
  [backend-output]
  (let [graph (build-dependency-graph backend-output)]
    (topological-sort graph)))