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
  e.g. 'jon_shared_cmd_compass.proto' -> 'cmd.compass'"
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
  Returns {:nodes #{...} :edges {node #{deps...}}}"
  [backend-output]
  (let [all-files (concat (get-in backend-output [:command :files])
                          (get-in backend-output [:state :files]))
        nodes (set (map :name all-files))
        edges (reduce (fn [acc file]
                        (let [file-name (:name file)
                              imports (extract-imports-from-file file)]
                          (assoc acc file-name (set imports))))
                      {}
                      all-files)]
    {:nodes nodes
     :edges edges}))

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
  "Find all dependencies for a given proto file."
  [graph proto-file]
  (loop [to-visit [proto-file]
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

(defn proto-to-clj-requires
  "Convert proto dependencies to Clojure require specs.
  Returns a vector of require specs like [potatoclient.proto.ser.types :as types]"
  [proto-file deps ns-prefix]
  (let [current-ns (proto-file->namespace proto-file)
        deps-namespaces (map proto-file->namespace deps)
        ;; Filter out self-dependencies (same namespace)
        external-deps (remove #(= % current-ns) deps-namespaces)]
    (vec (for [dep-ns external-deps
               :let [full-ns (str ns-prefix "." dep-ns)
                     ;; Generate a meaningful alias from the namespace
                     alias (if (= dep-ns "ser")
                            "types"  ;; Special case for common types
                            (last (str/split dep-ns #"\.")))]]
           [(symbol full-ns) :as (symbol alias)]))))

(defn analyze-type-references
  "Analyze a message to find external type references.
  Returns set of referenced namespaces."
  [message type-lookup]
  ;; TODO: Implement actual analysis of type references
  ;; For now, return empty set
  #{})

;; =============================================================================
;; Main API
;; =============================================================================

(defn analyze-dependencies
  "Analyze dependencies and return enriched backend output.
  Adds :clj-requires key to each file with require specs."
  [backend-output ns-prefix]
  (let [graph (build-dependency-graph backend-output)
        ;; Build enhanced file data with dependencies
        enhance-file (fn [file]
                       (let [proto-deps (:dependencies file [])
                             requires (proto-to-clj-requires (:name file) proto-deps ns-prefix)]
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