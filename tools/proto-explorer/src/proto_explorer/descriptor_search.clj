(ns proto-explorer.descriptor-search
  "Search through JSON descriptors to find protobuf messages"
  (:require [proto-explorer.json-to-edn :as json-edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-fuzzy.metrics :as fuzzy]))

;; Path is relative to where we run from (usually project root via Makefile)
(def descriptor-base-path "output/json-descriptors/")

(defn load-all-descriptors
  "Load all JSON descriptor files and extract message definitions"
  []
  (let [descriptor-dir (io/file descriptor-base-path)]
    (when (.exists descriptor-dir)
      (let [json-files (filter #(and (str/ends-with? (.getName %) ".json")
                                     ;; Skip the combined descriptor-set.json
                                     (not= (.getName %) "descriptor-set.json"))
                               (.listFiles descriptor-dir))]
        (mapcat (fn [file]
                 (try
                   (let [descriptor (json-edn/load-json-descriptor (.getPath file))
                         file-name (.getName file)]
                     ;; Extract all messages from all proto files in this descriptor
                     (mapcat (fn [proto-file]
                              (when-let [package (:package proto-file)]
                                (map (fn [msg]
                                      {:message-name (:name msg)
                                       :full-name (str package "." (:name msg))
                                       :package package
                                       :descriptor-file file-name
                                       :proto-file (:name proto-file)
                                       :fields (count (:field msg))})
                                    (or (:messageType proto-file) []))))
                            (:file descriptor)))
                   (catch Exception e
                     (println "Error loading" (.getName file) ":" (.getMessage e))
                     [])))
               json-files)))))

(defn get-java-class-for-descriptor
  "Map a descriptor message to its Java class name"
  [{:keys [package full-name descriptor-file]}]
  (cond
    ;; cmd.Root from jon_shared_cmd.json -> cmd.JonSharedCmd$Root
    (and (= package "cmd") 
         (= descriptor-file "jon_shared_cmd.json"))
    (str "cmd.JonSharedCmd$" (last (str/split full-name #"\.")))
    
    ;; ser.JonGUIState from jon_shared_data.json -> ser.JonSharedData$JonGUIState
    (and (= package "ser")
         (= descriptor-file "jon_shared_data.json"))
    (str "ser.JonSharedData$" (last (str/split full-name #"\.")))
    
    ;; cmd.Compass.Root from jon_shared_cmd_compass.json -> cmd.Compass$Root
    (re-matches #"cmd\.(\w+)\.(.+)" full-name)
    (let [[_ sub-pkg msg] (re-matches #"cmd\.(\w+)\.(.+)" full-name)]
      (str "cmd." sub-pkg "$" msg))
    
    ;; ser.SubPackage.Message
    (re-matches #"ser\.(\w+)\.(.+)" full-name)
    (let [[_ sub-pkg msg] (re-matches #"ser\.(\w+)\.(.+)" full-name)]
      (str "ser." sub-pkg "$" msg))
    
    ;; Default: use the full name as-is
    :else
    full-name))

(defn fuzzy-search-descriptors
  "Search through all descriptor messages and return top matches"
  ([query] (fuzzy-search-descriptors query 10))
  ([query limit]
   (let [all-messages (load-all-descriptors)
         ;; Calculate scores for each message
         scored (map (fn [msg-info]
                      (let [simple-name (:message-name msg-info)
                            ;; Use multiple scoring methods
                            jaro-score (fuzzy/jaro-winkler query simple-name)
                            ;; Bonus for exact substring match
                            substring-bonus (if (str/includes? 
                                               (str/lower-case simple-name)
                                               (str/lower-case query))
                                             0.3 0)
                            ;; Bonus for matching start
                            start-bonus (if (str/starts-with? 
                                          (str/lower-case simple-name)
                                          (str/lower-case query))
                                         0.2 0)
                            total-score (min 1.0 (+ jaro-score substring-bonus start-bonus))]
                        (assoc msg-info
                               :score total-score
                               :java-class (get-java-class-for-descriptor msg-info))))
                    all-messages)
         ;; Sort by score and take top N
         top-matches (->> scored
                         (sort-by :score >)
                         (take limit))]
     {:query query
      :matches (mapv (fn [match]
                      {:descriptor-name (:full-name match)
                       :java-class (:java-class match)
                       :message-name (:message-name match)
                       :package (:package match)
                       :proto-file (:proto-file match)
                       :descriptor-file (:descriptor-file match)
                       :field-count (:fields match)
                       :score (format "%.2f" (:score match))})
                    top-matches)})))

(defn get-descriptor-by-name
  "Get a specific descriptor by its full name (e.g., 'cmd.Root')"
  [full-name]
  (let [all-messages (load-all-descriptors)]
    (first (filter #(= (:full-name %) full-name) all-messages))))