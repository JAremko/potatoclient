(ns proto-explorer.generated-specs
  "Load and use generated Malli specs from JSON descriptors"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [malli.core :as m]
            [malli.registry :as mr]
            [clojure.edn :as edn]
            [clj-fuzzy.metrics :as fuzzy]
            [potatoclient.specs.malli-oneof :as oneof]))

(defonce spec-registry (atom {}))

;; Create a Malli registry that includes our custom :oneof schema and all loaded specs
(defn create-registry
  "Create a registry with default schemas, oneof, and loaded specs."
  []
  (let [base-registry (merge (m/default-schemas)
                            {:oneof oneof/-oneof-schema})
        ;; Add both full and short keys for all specs
        all-specs (reduce (fn [acc [k v]]
                           (let [acc-with-full (assoc acc k v)]
                             ;; If it's a namespaced keyword like :potatoclient.specs.cmd/root
                             ;; also add the short version :cmd/root
                             (if (and (namespace k)
                                     (str/starts-with? (namespace k) "potatoclient.specs."))
                               (let [short-ns (str/replace (namespace k) #"^potatoclient\.specs\." "")
                                     short-key (keyword short-ns (name k))]
                                 (assoc acc-with-full short-key v))
                               acc-with-full)))
                         base-registry
                         @spec-registry)]
    all-specs))

(defn proto-registry
  "Get the current proto registry."
  []
  (create-registry))

(defn kebab->snake
  "Convert kebab-case to snake_case for proto field names."
  [s]
  (str/replace s #"-" "_"))

(defn load-spec-file
  "Load a generated spec file and extract specs."
  [file]
  (try
    (let [content (slurp file)
          ;; Extract namespace from file
          ns-match (re-find #"^\(ns\s+([\w.-]+)" content)
          namespace (when ns-match (second ns-match))
          ;; Extract def forms
          defs (re-seq #"\(def\s+(\S+)\s+\"[^\"]+\"\s+(.+?)\)\n\n" content)]
      (when (and namespace defs)
        (reduce (fn [acc [_ spec-name spec-form]]
                  (let [spec-key (keyword namespace spec-name)]
                    (try
                      ;; Read the spec form
                      (assoc acc spec-key (edn/read-string spec-form))
                      (catch Exception e
                        (println "Error parsing spec" spec-key ":" (.getMessage e))
                        acc))))
                {}
                defs)))
    (catch Exception e
      (println "Error loading spec file" (.getName file) ":" (.getMessage e))
      {})))

(defn load-all-specs!
  "Load all generated spec files from the shared location."
  [spec-dir]
  (let [spec-path (io/file spec-dir)
        spec-files (when (.exists spec-path)
                    (filter #(str/ends-with? (.getName %) "-specs.clj")
                           (.listFiles spec-path)))]
    (if (seq spec-files)
      (do
        (println "Loading" (count spec-files) "spec files from" spec-dir)
        (let [all-specs (reduce merge {} (map load-spec-file spec-files))]
          (reset! spec-registry all-specs)
          (println "Loaded" (count all-specs) "specs")
          all-specs))
      (do
        (println "No spec files found in" spec-dir)
        {}))))

(defn get-spec
  "Get a spec by qualified keyword.
   Handles both short form (:cmd/ping) and full form (:potatoclient.specs.cmd/ping).
   Also handles case variations by trying lowercase version."
  [spec-key]
  (or (get @spec-registry spec-key)
      ;; Try to expand short form to full form
      (when (and (keyword? spec-key) (namespace spec-key))
        (let [ns-part (namespace spec-key)
              name-part (name spec-key)
              full-key (keyword (str "potatoclient.specs." ns-part) name-part)]
          (or (get @spec-registry full-key)
              ;; Try with lowercase name
              (get @spec-registry (keyword (str "potatoclient.specs." ns-part) 
                                          (str/lower-case name-part))))))
      ;; Try with just lowercase name
      (when (keyword? spec-key)
        (let [ns-part (namespace spec-key)
              name-part (str/lower-case (name spec-key))]
          (if ns-part
            (or (get @spec-registry (keyword ns-part name-part))
                (get @spec-registry (keyword (str "potatoclient.specs." ns-part) name-part)))
            (get @spec-registry (keyword name-part)))))))

(defn get-spec-exact
  "Get a spec by exact name match (case-sensitive).
   Accepts either a keyword or string representation."
  [spec-name]
  (cond
    (keyword? spec-name) (get @spec-registry spec-name)
    (string? spec-name) (if (str/starts-with? spec-name ":")
                         (get @spec-registry (edn/read-string spec-name))
                         (get @spec-registry (keyword spec-name)))
    :else nil))

(defn calculate-match-score
  "Calculate a composite match score using multiple fuzzy algorithms.
   Returns a score between 0.0 and 1.0, where 1.0 is a perfect match."
  [pattern target]
  (let [pattern-lower (str/lower-case pattern)
        target-lower (str/lower-case target)
        ;; Extract just the name part for better matching
        target-name (last (str/split target #"/"))
        target-name-lower (str/lower-case target-name)
        ;; Use multiple algorithms and combine scores
        jaro-score (fuzzy/jaro pattern-lower target-lower)
        jaro-name-score (fuzzy/jaro pattern-lower target-name-lower)
        ;; Levenshtein distance normalized to 0-1 (inverted so lower distance = higher score)
        lev-distance (fuzzy/levenshtein pattern-lower target-lower)
        lev-name-distance (fuzzy/levenshtein pattern-lower target-name-lower)
        max-len (max (count pattern-lower) (count target-lower))
        max-name-len (max (count pattern-lower) (count target-name-lower))
        lev-score (if (zero? max-len) 1.0 (- 1.0 (/ lev-distance max-len)))
        lev-name-score (if (zero? max-name-len) 1.0 (- 1.0 (/ lev-name-distance max-name-len)))
        ;; Substring match bonus
        substring-score (cond
                         (str/includes? target-lower pattern-lower) 1.0
                         (str/includes? target-name-lower pattern-lower) 0.9
                         :else 0.0)
        ;; Prefix match bonus
        prefix-score (cond
                      (str/starts-with? target-lower pattern-lower) 1.0
                      (str/starts-with? target-name-lower pattern-lower) 0.95
                      :else 0.0)]
    ;; Weighted combination of scores
    (+ (* 0.2 jaro-score)
       (* 0.2 jaro-name-score)
       (* 0.15 lev-score)
       (* 0.15 lev-name-score)
       (* 0.2 substring-score)
       (* 0.1 prefix-score))))

(defn find-specs
  "Find specs matching a pattern using fuzzy search.
   Returns up to 10 best results sorted by match quality, best matches first."
  [pattern]
  (if (str/blank? pattern)
    []
    (let [results-with-scores
          (for [[spec-key spec-value] @spec-registry
                :let [score (calculate-match-score pattern (str spec-key))]
                :when (> score 0.3)] ; Minimum threshold to filter out poor matches
            {:spec-key spec-key
             :spec-value spec-value
             :score score})]
      ;; Sort by score descending, take top 10, and return as [key value] pairs
      (->> results-with-scores
           (sort-by :score >)
           (take 10)
           (map (fn [{:keys [spec-key spec-value]}]
                  [spec-key spec-value]))))))

(defn find-best-spec
  "Find the single best matching spec for a pattern.
   Returns [spec-key spec-value] or nil if no match found."
  [pattern]
  (first (find-specs pattern)))

(defn find-specs-with-scores
  "Find specs matching a pattern, returning results with match scores.
   Useful for debugging or showing match quality to users."
  [pattern]
  (if (str/blank? pattern)
    []
    (let [results-with-scores
          (for [[spec-key spec-value] @spec-registry
                :let [score (calculate-match-score pattern (str spec-key))]
                :when (> score 0.3)]
            {:spec-key spec-key
             :score score})]
      (->> results-with-scores
           (sort-by :score >)
           (take 10)))))

(defn spec->field-info
  "Extract field information from a Malli spec."
  [spec]
  (when (and (vector? spec) (= :map (first spec)))
    (let [fields (rest spec)]
      (map (fn [[field-key field-spec]]
             {:name (name field-key)
              :proto-name (kebab->snake (name field-key))
              :type (cond
                      (keyword? field-spec) field-spec
                      (vector? field-spec) 
                      (let [spec-type (if (= :maybe (first field-spec))
                                       (second field-spec)
                                       (first field-spec))]
                        spec-type)
                      :else :unknown)
              :optional? (and (vector? field-spec) 
                            (= :maybe (first field-spec)))})
           fields))))

(defn spec->message-info
  "Extract message information from a spec."
  [[spec-key spec-value]]
  (let [ns-name (namespace spec-key)
        msg-name (name spec-key)]
    {:name msg-name
     :namespace ns-name
     :full-name (str spec-key)
     :fields (spec->field-info spec-value)
     :spec spec-value}))

(defn find-message
  "Find a message spec by name."
  [message-name]
  (let [matches (find-specs message-name)]
    (when-let [[k v] (first matches)]
      (spec->message-info [k v]))))

(defn list-messages
  "List all message specs."
  []
  (map (fn [[k v]]
         {:name (name k)
          :namespace (namespace k)
          :full-name (str k)})
       @spec-registry))

(defn validate-data
  "Validate data against a spec."
  [spec-key data]
  (if-let [spec (get-spec spec-key)]
    (m/validate spec data {:registry (proto-registry)})
    (throw (ex-info "Spec not found" {:spec spec-key}))))

(defn explain-errors
  "Explain validation errors."
  [spec-key data]
  (if-let [spec (get-spec spec-key)]
    (m/explain spec data {:registry (proto-registry)})
    (throw (ex-info "Spec not found" {:spec spec-key}))))