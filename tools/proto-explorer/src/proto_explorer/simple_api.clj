(ns proto-explorer.simple-api
  "Simplified API using descriptor-set.json as single source of truth"
  (:require [proto-explorer.descriptor-set-search :as search]
            [proto-explorer.java-class-info :as java-info]
            [proto-explorer.pronto-integration :as pronto]
            [proto-explorer.descriptor-integration :as desc-int]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

;; =============================================================================
;; Step 1: Search / List - Returns actionable results
;; =============================================================================

(defn search-messages
  "Search for protobuf messages by name or Java class.
   Returns results with ready-to-use query strings for step 2."
  ([query] (search-messages query 10))
  ([query limit]
   (let [results (search/fuzzy-search-messages query limit)
         matches (:matches results)]
     (assoc results
            :matches (mapv (fn [r]
                            (assoc r 
                                   :query-command (str "make proto-info QUERY='" (:java-class r) "'")
                                   :query-string (:java-class r)))
                          matches)))))

(defn search-by-java-class
  "Search specifically by Java class name.
   Returns results with ready-to-use query strings for step 2."
  ([query] (search-by-java-class query 10))
  ([query limit]
   (let [results (search/search-java-classes query limit)
         matches (:matches results)]
     (assoc results
            :matches (mapv (fn [r]
                            (assoc r
                                   :query-command (str "make proto-info QUERY='" (:java-class r) "'")
                                   :query-string (:java-class r)))
                          matches)))))

(defn search
  "Unified search that checks both message names and Java classes.
   Returns results with ready-to-use query strings for step 2."
  ([query] (search query 10))
  ([query limit]
   (let [results (search/fuzzy-search-messages query limit)
         matches (:matches results)]
     (assoc results
            :matches (mapv (fn [r]
                            (assoc r
                                   :query-command (str "make proto-info QUERY='" (:java-class r) "'")
                                   :query-string (:java-class r)))
                          matches)))))

(defn list-all-messages
  "List all available protobuf messages, optionally filtered by package"
  ([]
   (let [all-messages (search/load-all-messages)
         by-package (group-by :proto-package all-messages)]
     {:total (count all-messages)
      :packages (sort (keys by-package))
      :by-package (into (sorted-map)
                       (map (fn [[pkg msgs]]
                             [pkg (mapv (fn [m]
                                         {:message-name (:message-name m)
                                          :java-class (:java-class m)
                                          :field-count (:field-count m)})
                                       (sort-by :message-name msgs))])
                           by-package))}))
  ([package-filter]
   (let [all-messages (search/load-all-messages)
         filtered (filter #(str/starts-with? (:proto-package %) package-filter) all-messages)
         by-package (group-by :proto-package filtered)]
     {:total (count filtered)
      :filter package-filter
      :packages (sort (keys by-package))
      :by-package (into (sorted-map)
                       (map (fn [[pkg msgs]]
                             [pkg (mapv (fn [m]
                                         {:message-name (:message-name m)
                                          :java-class (:java-class m)
                                          :field-count (:field-count m)})
                                       (sort-by :message-name msgs))])
                           by-package))})))

;; =============================================================================
;; Step 2: Get Info
;; =============================================================================

(defn get-message-info
  "Get complete information for a message by its Java class name (e.g., 'cmd.JonSharedCmd$Root')"
  [java-class-name]
  (let [;; Get message data from descriptor
        msg-data (search/get-message-by-java-class java-class-name)
        
        _ (when-not msg-data
            (throw (ex-info "Message not found" {:java-class java-class-name})))
        
        ;; Extract simple name for display
        simple-name (last (str/split java-class-name #"\$"))
        
        ;; 1. Java Class Info - use full class name
        java-result (try 
                      (java-info/analyze-protobuf-class java-class-name)
                      (catch Exception e
                        {:error (.getMessage e)}))
        
        ;; 2. Pronto EDN - use full class name
        pronto-result (try
                       (when-not (:error java-result)
                         (pronto/get-pronto-info-by-class java-class-name))
                       (catch Exception e
                         {:error (.getMessage e)}))
        
        ;; 3. Pronto Schema - use full class name
        schema-result (try
                       (when-not (:error java-result)
                         (pronto/get-schema-by-class java-class-name))
                       (catch Exception e
                         {:error (.getMessage e)}))
        
        ;; 4. Field details from descriptor
        field-details (mapv (fn [field]
                             {:name (:name field)
                              :number (:number field)
                              :type (name (:type field))
                              :type-name (:type-name field)
                              :json-name (:json-name field)})
                           (:fields msg-data))]
    
    {:java-class java-class-name
     :message-name simple-name
     :proto-package (:proto-package msg-data)
     :proto-file (:proto-file msg-data)
     :field-count (:field-count msg-data)
     
     :java-info (when-not (:error java-result)
                 {:package (get-in java-result [:class :package])
                  :methods-count (count (:methods java-result))
                  :fields-count (count (:fields java-result))})
     
     :pronto-edn (when (:success pronto-result)
                  (:edn-structure pronto-result))
     
     :pronto-schema (when-not (:error schema-result)
                     (:schema schema-result))
     
     :fields field-details
     
     :errors (cond-> []
              (:error java-result) (conj {:type :java :message (:error java-result)})
              (:error pronto-result) (conj {:type :pronto :message (:error pronto-result)})
              (:error schema-result) (conj {:type :schema :message (:error schema-result)}))}))

;; =============================================================================
;; Formatting
;; =============================================================================

(defn format-list-results
  "Format list results for display with query strings"
  [results]
  (str "PROTOBUF MESSAGES - Total: " (:total results) "\n"
       (if (:filter results)
         (str "Filter: " (:filter results) "\n")
         "")
       "Packages: " (str/join ", " (:packages results)) "\n"
       "\n"
       (str/join "\n\n"
                (map (fn [[pkg messages]]
                      (str "=== " pkg " (" (count messages) " messages) ===\n"
                          (str/join "\n"
                                   (map (fn [msg]
                                         (format "  %-30s → %s\n  %-30s   (fields: %d)"
                                                (:message-name msg)
                                                (:java-class msg)
                                                ""
                                                (:field-count msg)))
                                       messages))))
                    (:by-package results)))
       "\n\nTo get details, use: make proto-info QUERY='<java-class-from-above>'"))

(defn format-search-results
  "Format search results for display with actionable query strings"
  [results]
  (str "Search results for: \"" (:query results) "\"\n\n"
       (str/join "\n\n" 
                (map-indexed 
                 (fn [idx match]
                   (format "%2d. %-25s %s (score: %s)\n    Package: %-20s\n    Proto: %s\n    → Query: %s"
                          (inc idx)
                          (:message-name match)
                          (:java-class match)
                          (:score match)
                          (:proto-package match)
                          (:proto-file match)
                          (or (:query-string match) (:java-class match))))
                 (:matches results)))
       "\n\n" 
       "To get details, use:\n"
       "  make proto-info QUERY='<query-string-from-above>'"))

(defn format-message-info
  "Format message info for display"
  [info]
  (with-out-str
    (println "================================================================================")
    (println "PROTOBUF MESSAGE:" (:message-name info))
    (println "Java Class:" (:java-class info))
    (println "Proto Package:" (:proto-package info))
    (println "Proto File:" (:proto-file info))
    (println "Total Fields:" (:field-count info))
    (println "================================================================================")
    
    (when-let [java-info (:java-info info)]
      (println "\n=== JAVA CLASS INFO ===")
      (println "Package:" (:package java-info))
      (println "Methods:" (:methods-count java-info) "| Fields:" (:fields-count java-info)))
    
    (when (:pronto-edn info)
      (println "\n=== PRONTO EDN STRUCTURE ===")
      (pprint/pprint (:pronto-edn info)))
    
    (when (:pronto-schema info)
      (println "\n=== PRONTO SCHEMA ===")
      (pprint/pprint (:pronto-schema info)))
    
    (when (seq (:fields info))
      (println "\n=== FIELD DETAILS ===")
      (println "Fields:" (count (:fields info)))
      (doseq [field (take 15 (:fields info))]
        (println (format "  [%2d] %-25s : %-15s %s"
                        (:number field)
                        (:name field)
                        (:type field)
                        (or (:type-name field) "")))))
    
    (when (seq (:errors info))
      (println "\n=== ERRORS ===")
      (doseq [error (:errors info)]
        (println (str "- " (name (:type error)) ": " (:message error)))))))