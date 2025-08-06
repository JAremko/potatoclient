(ns generator.dependency-graph
  "Build and analyze dependency graphs for proto files.
  Ensures correct namespace requires and compilation order."
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [com.rpl.specter :as sp]
            [generator.naming :as naming]
            [generator.frontend-namespaced :refer [file->namespace-suffix]]
            [generator.proto-registry :as registry]))

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

;; Use centralized naming function
(defn package->namespace-suffix
  "Get just the namespace suffix part for a package.
  e.g. 'cmd.DayCamera' -> 'cmd.daycamera'"
  [proto-package]
  (let [full-ns (naming/proto-package->clj-namespace proto-package)
        prefix "potatoclient.proto."]
    (if (str/starts-with? full-ns prefix)
      (subs full-ns (count prefix))
      full-ns)))

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

(defn file-deps-to-clj-requires
  "Convert file-based dependencies to Clojure require specs.
  Returns a vector of require specs like [potatoclient.proto.ser.types :as types]"
  [current-file dep-files ns-prefix all-files]
  (let [current-ns (file->namespace-suffix (:name current-file) (:package current-file))
        ;; For each dependency file, get its namespace
        deps-namespaces (map (fn [dep-file]
                              (file->namespace-suffix (:name dep-file) (:package dep-file)))
                            dep-files)
        ;; Filter out self-dependencies (same namespace)
        external-deps (remove #(= % current-ns) deps-namespaces)
        ;; Track used aliases to avoid duplicates
        used-aliases (atom {})]
    (vec (for [dep-ns external-deps
               :let [full-ns (str ns-prefix "." dep-ns)
                     ;; Generate a meaningful alias from the namespace
                     ;; Generate alias using registry metadata
                     dep-file (first (filter #(= (file->namespace-suffix (:name %) (:package %)) dep-ns) dep-files))
                     base-alias (if dep-file
                                 ;; Generate alias from package
                                 (naming/proto-package->clojure-alias (:package dep-file))
                                 ;; Fallback to last part of namespace
                                 (last (str/split dep-ns #"\.")))
                     ;; Handle duplicate aliases by appending a number
                     alias (loop [candidate base-alias
                                  n 2]
                             (if (contains? @used-aliases candidate)
                               (recur (str base-alias n) (inc n))
                               (do (swap! used-aliases assoc candidate true)
                                   candidate)))]]
           [(symbol full-ns) :as (symbol alias)]))))

(defn collect-type-refs-from-field
  "Collect type references from a field."
  [field]
  (cond
    ;; Enum type reference
    (get-in field [:type :enum :type-ref])
    #{(get-in field [:type :enum :type-ref])}
    
    ;; Message type reference
    (get-in field [:type :message :type-ref])
    #{(get-in field [:type :message :type-ref])}
    
    :else #{}))

(defn collect-type-refs-from-message
  "Recursively collect all type references from a message."
  [message]
  (let [;; Regular fields
        field-refs (mapcat collect-type-refs-from-field (:fields message []))
        ;; Oneof fields
        oneof-refs (mapcat (fn [oneof]
                            (mapcat collect-type-refs-from-field (:fields oneof [])))
                          (:oneofs message []))]
    (set (concat field-refs oneof-refs))))

(defn analyze-file-dependencies
  "Analyze a file to find which other files it depends on.
  Returns a set of files that this file depends on."
  [file type-lookup all-files]
  (let [;; Collect all type references from all messages in this file
        all-type-refs (mapcat collect-type-refs-from-message (:messages file []))
        ;; Look up which file each type is defined in
        dep-files (keep (fn [type-ref]
                          ;; Try both with and without leading dot, and as string
                          (let [clean-ref (str/replace type-ref #"^\." "")
                                type-info (or (get type-lookup clean-ref)  ; Try as string first
                                             (get type-lookup (keyword clean-ref)))]
                            (when type-info
                              ;; Find the file that defines this type
                              (let [filename (:filename type-info)]
                                (first (filter #(= (:name %) filename) all-files))))))
                        all-type-refs)]
    (set dep-files)))

;; =============================================================================
;; Main API
;; =============================================================================

(defn analyze-dependencies
  "Analyze dependencies and return enriched backend output.
  Adds :clj-requires key to each file with require specs."
  [backend-output ns-prefix]
  (let [;; Get all files and type lookup
        all-files (concat (get-in backend-output [:command :files])
                         (get-in backend-output [:state :files]))
        type-lookup (:type-lookup backend-output)
        
        ;; Build enhanced file data with dependencies
        enhance-file (fn [file]
                       (let [;; Analyze actual type usage to find dependencies
                             dep-files (analyze-file-dependencies file type-lookup all-files)
                             ;; Convert to require specs
                             requires (file-deps-to-clj-requires file dep-files ns-prefix all-files)]
                         (assoc file :clj-requires requires)))
        
        ;; Build the original graph for compatibility
        graph (build-dependency-graph backend-output)]
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