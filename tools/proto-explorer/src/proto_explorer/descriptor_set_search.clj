(ns proto-explorer.descriptor-set-search
  "Search using the unified descriptor-set.json file"
  (:require [proto-explorer.json-to-edn :as json-edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-fuzzy.metrics :as fuzzy]))

(defn find-descriptor-set-path
  "Find the descriptor-set.json file by checking multiple possible locations"
  []
  (let [possible-paths ["examples/protogen/output/json-descriptors/descriptor-set.json"
                        "../../examples/protogen/output/json-descriptors/descriptor-set.json"
                        "../examples/protogen/output/json-descriptors/descriptor-set.json"
                        "output/json-descriptors/descriptor-set.json"
                        "../../output/json-descriptors/descriptor-set.json"
                        "../output/json-descriptors/descriptor-set.json"]]
    (first (filter #(.exists (io/file %)) possible-paths))))

(def descriptor-set-path (or (find-descriptor-set-path)
                             "output/json-descriptors/descriptor-set.json"))

(defn proto-file-to-java-outer-class
  "Convert proto file name to Java outer class name.
   Examples:
   - jon_shared_cmd.proto -> JonSharedCmd
   - jon_shared_cmd_gps.proto -> JonSharedCmdGps
   - jon_shared_data_gps.proto -> JonSharedDataGps"
  [proto-file-name]
  (-> proto-file-name
      (str/replace ".proto" "")
      (str/split #"_")
      (->> (map str/capitalize)
           (str/join))))

(defn get-java-class-name
  "Get the Java class name for a message based on proto file and package.
   Handles various proto file naming conventions and package structures."
  [proto-file-name proto-package message-name]
  (let [java-outer-class (proto-file-to-java-outer-class proto-file-name)]
    (cond
      ;; Special case: jon_shared_cmd.proto with package 'cmd' -> cmd.JonSharedCmd$MessageName
      (and (= proto-file-name "jon_shared_cmd.proto")
           (= proto-package "cmd"))
      (str "cmd.JonSharedCmd$" message-name)
      
      ;; Special case: jon_shared_data.proto with package 'ser' -> ser.JonSharedData$MessageName  
      (and (= proto-file-name "jon_shared_data.proto")
           (= proto-package "ser"))
      (str "ser.JonSharedData$" message-name)
      
      ;; Files with nested packages (e.g., cmd.Gps) use the Java outer class pattern
      ;; jon_shared_cmd_gps.proto with package cmd.Gps -> cmd.Gps.JonSharedCmdGps$MessageName
      (str/includes? proto-package ".")
      (str proto-package "." java-outer-class "$" message-name)
      
      ;; Files in ser package without dots use just the Java outer class
      ;; jon_shared_data_gps.proto with package ser -> ser.JonSharedDataGps$MessageName
      (= proto-package "ser")
      (str "ser." java-outer-class "$" message-name)
      
      ;; Default case - shouldn't normally happen but provides fallback
      :else
      (str proto-package "$" message-name))))

(defn load-all-messages
  "Load all messages from the descriptor-set.json"
  []
  (let [descriptor-file (io/file descriptor-set-path)]
    (if (.exists descriptor-file)
      (let [descriptor (json-edn/load-json-descriptor (.getPath descriptor-file))]
        (mapcat (fn [proto-file]
                 (let [proto-name (:name proto-file)
                       package (:package proto-file)]
                   (when (and package 
                             (or (str/starts-with? package "cmd")
                                 (str/starts-with? package "ser")))
                     (map (fn [msg]
                           {:message-name (:name msg)
                            :proto-package package
                            :proto-file proto-name
                            :java-class (get-java-class-name proto-name package (:name msg))
                            :field-count (count (:field msg))
                            :fields (:field msg)})
                         (or (:message-type proto-file) [])))))
               (:file descriptor)))
      (println "Error: descriptor-set.json not found at" descriptor-set-path))))

(defn fuzzy-search-messages
  "Search through all messages and return top matches.
   Primary: case-insensitive substring match in both message name and Java class name
   Secondary: fuzzy string similarity for typos"
  ([query] (fuzzy-search-messages query 10))
  ([query limit]
   (let [all-messages (load-all-messages)
         query-lower (str/lower-case query)
         ;; Calculate scores for each message
         scored (map (fn [msg-info]
                      (let [message-name (:message-name msg-info)
                            java-class (:java-class msg-info)
                            name-lower (str/lower-case message-name)
                            class-lower (str/lower-case java-class)
                            ;; Check both message name and Java class name
                            contains-in-name? (str/includes? name-lower query-lower)
                            contains-in-class? (str/includes? class-lower query-lower)
                            contains-query? (or contains-in-name? contains-in-class?)
                            ;; Position of match (earlier is better)
                            match-position-name (if contains-in-name?
                                                  (str/index-of name-lower query-lower)
                                                  Integer/MAX_VALUE)
                            match-position-class (if contains-in-class?
                                                   (str/index-of class-lower query-lower)
                                                   Integer/MAX_VALUE)
                            match-position (min match-position-name match-position-class)
                            ;; Exact matches
                            exact-match-name? (= name-lower query-lower)
                            exact-match-class? (= class-lower query-lower)
                            exact-match? (or exact-match-name? exact-match-class?)
                            ;; Starts with query
                            starts-with-name? (str/starts-with? name-lower query-lower)
                            starts-with-class? (str/starts-with? class-lower query-lower)
                            starts-with? (or starts-with-name? starts-with-class?)
                            ;; Ends with query
                            ends-with-name? (str/ends-with? name-lower query-lower)
                            ends-with-class? (str/ends-with? class-lower query-lower)
                            ends-with? (or ends-with-name? ends-with-class?)
                            ;; Word boundary match (e.g., "GPS" in "GetGPSData")
                            word-boundary-match-name? (and contains-in-name?
                                                           (or (re-find (re-pattern (str "(?i)\\b" query "\\b")) message-name)
                                                               (re-find (re-pattern (str "(?i)[A-Z]" query)) message-name)))
                            word-boundary-match-class? (and contains-in-class?
                                                            (or (re-find (re-pattern (str "(?i)\\b" query "\\b")) java-class)
                                                                (re-find (re-pattern (str "(?i)[A-Z]" query)) java-class)))
                            word-boundary-match? (or word-boundary-match-name? word-boundary-match-class?)
                            ;; Fuzzy match for typos (fallback)
                            fuzzy-score-name (fuzzy/jaro-winkler query-lower name-lower)
                            fuzzy-score-class (fuzzy/jaro-winkler query-lower class-lower)
                            fuzzy-score (max fuzzy-score-name fuzzy-score-class)
                            ;; Calculate final score
                            score (cond
                                   exact-match?      1.0
                                   starts-with?      0.95
                                   ends-with?        0.90
                                   word-boundary-match? 0.85
                                   contains-query?   (- 0.80 (* 0.001 match-position)) ; Earlier matches score higher
                                   :else            (* 0.5 fuzzy-score))] ; Fuzzy match as fallback
                        (assoc msg-info 
                               :score score
                               :match-type (cond
                                           exact-match? "exact"
                                           starts-with? "prefix"
                                           ends-with? "suffix"
                                           contains-query? "substring"
                                           (> fuzzy-score 0.7) "fuzzy"
                                           :else "none"))))
                    all-messages)
         ;; Filter out very low scores and sort
         top-matches (->> scored
                         (filter #(> (:score %) 0.3))
                         (sort-by :score >)
                         (take limit))]
     {:query query
      :matches (mapv (fn [match]
                      {:message-name (:message-name match)
                       :java-class (:java-class match)
                       :proto-package (:proto-package match)
                       :proto-file (:proto-file match)
                       :field-count (:field-count match)
                       :score (format "%.2f" (:score match))})
                    top-matches)})))

(defn get-message-by-java-class
  "Get a specific message by its Java class name (e.g., 'cmd.JonSharedCmd$Root')"
  [java-class]
  (let [all-messages (load-all-messages)]
    (first (filter #(= (:java-class %) java-class) all-messages))))

(defn get-message-by-proto-name
  "Get a specific message by its proto package and name (e.g., 'cmd', 'Root')"
  [proto-package message-name]
  (let [all-messages (load-all-messages)]
    (first (filter #(and (= (:proto-package %) proto-package)
                        (= (:message-name %) message-name)) 
                  all-messages))))

(defn search-java-classes
  "Search specifically by Java class name, focusing only on the Java class field"
  ([query] (search-java-classes query 10))
  ([query limit]
   (let [all-messages (load-all-messages)
         query-lower (str/lower-case query)
         ;; Calculate scores for each message based on Java class only
         scored (map (fn [msg-info]
                      (let [java-class (:java-class msg-info)
                            class-lower (str/lower-case java-class)
                            ;; Check only Java class name
                            contains-query? (str/includes? class-lower query-lower)
                            ;; Position of match (earlier is better)
                            match-position (if contains-query?
                                             (str/index-of class-lower query-lower)
                                             Integer/MAX_VALUE)
                            ;; Exact match
                            exact-match? (= class-lower query-lower)
                            ;; Starts with query
                            starts-with? (str/starts-with? class-lower query-lower)
                            ;; Ends with query
                            ends-with? (str/ends-with? class-lower query-lower)
                            ;; Check if query matches after $ (inner class name)
                            inner-class-name (last (str/split java-class #"\$"))
                            inner-class-lower (str/lower-case inner-class-name)
                            inner-exact? (= inner-class-lower query-lower)
                            inner-starts? (str/starts-with? inner-class-lower query-lower)
                            inner-contains? (str/includes? inner-class-lower query-lower)
                            ;; Word boundary match
                            word-boundary-match? (and contains-query?
                                                     (or (re-find (re-pattern (str "(?i)\\b" query "\\b")) java-class)
                                                         (re-find (re-pattern (str "(?i)\\$" query)) java-class)))
                            ;; Fuzzy match for typos (fallback)
                            fuzzy-score (fuzzy/jaro-winkler query-lower class-lower)
                            ;; Calculate final score
                            score (cond
                                   exact-match?      1.0
                                   inner-exact?      0.98
                                   starts-with?      0.95
                                   inner-starts?     0.93
                                   ends-with?        0.90
                                   word-boundary-match? 0.85
                                   inner-contains?   0.82
                                   contains-query?   (- 0.80 (* 0.001 match-position))
                                   :else            (* 0.5 fuzzy-score))]
                        (assoc msg-info 
                               :score score
                               :match-type (cond
                                           exact-match? "exact"
                                           inner-exact? "inner-exact"
                                           starts-with? "prefix"
                                           inner-starts? "inner-prefix"
                                           ends-with? "suffix"
                                           contains-query? "substring"
                                           (> fuzzy-score 0.7) "fuzzy"
                                           :else "none"))))
                    all-messages)
         ;; Filter out very low scores and sort
         top-matches (->> scored
                         (filter #(> (:score %) 0.3))
                         (sort-by :score >)
                         (take limit))]
     {:query query
      :matches (mapv (fn [match]
                      {:message-name (:message-name match)
                       :java-class (:java-class match)
                       :proto-package (:proto-package match)
                       :proto-file (:proto-file match)
                       :field-count (:field-count match)
                       :score (format "%.2f" (:score match))})
                    top-matches)})))