(ns proto-explorer.unified-api-v2
  "Unified API v2 - Clean 2-step process: fuzzy search → info by descriptor name"
  (:require [proto-explorer.descriptor-search :as desc-search]
            [proto-explorer.descriptor-integration :as desc-int]
            [proto-explorer.java-class-info :as java-info]
            [proto-explorer.pronto-integration :as pronto-int]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

;; =============================================================================
;; Step 1: Fuzzy Search
;; =============================================================================

(defn fuzzy-search
  "Search for protobuf messages using fuzzy matching.
   Returns both descriptor names and Java class names."
  ([query] (fuzzy-search query 10))
  ([query limit]
   (desc-search/fuzzy-search-descriptors query limit)))

;; =============================================================================
;; Step 2: Get Complete Info
;; =============================================================================

(defn get-info-by-descriptor
  "Get complete information for a message by its descriptor name (e.g., 'cmd.Root')"
  [descriptor-name]
  (let [;; Get the descriptor info
        msg-info (desc-search/get-descriptor-by-name descriptor-name)
        
        ;; If not found, return error
        _ (when-not msg-info
            (throw (ex-info "Descriptor not found" {:descriptor descriptor-name})))
        
        ;; Get Java class name
        java-class-name (desc-search/get-java-class-for-descriptor msg-info)
        simple-name (:message-name msg-info)
        
        ;; 1. Java Class Info
        java-info (java-info/find-message-class simple-name)
        
        ;; 2. Pronto EDN
        pronto-result (if (:error java-info)
                        {:error "Class not found"}
                        (pronto-int/get-pronto-info simple-name))
        
        ;; 3. Descriptor Info with constraints
        desc-result (desc-int/get-message-descriptor-info descriptor-name)
        
        ;; 4. Extract field summary
        field-summary (when-not (:error java-info)
                       (mapv (fn [accessor]
                              {:proto-name (:field-name accessor)
                               :java-getter (:name (:getter accessor))
                               :java-type (-> accessor :getter :return-type :simple-name)})
                            (:field-accessors java-info)))]
    
    {:descriptor-name descriptor-name
     :java-class java-class-name
     :message-name simple-name
     :package (:package msg-info)
     :proto-file (:proto-file msg-info)
     :descriptor-file (:descriptor-file msg-info)
     
     :java-info (when-not (:error java-info)
                 {:package (get-in java-info [:class :package])
                  :methods-count (count (:methods java-info))
                  :fields-count (count (:fields java-info))
                  :field-accessors field-summary})
     
     :pronto-edn (when (:success pronto-result)
                  (:edn-structure pronto-result))
     
     :descriptor (when (:success desc-result)
                  (:descriptor-info desc-result))
     
     :errors (cond-> []
              (:error java-info) (conj {:type :java :message (:message java-info)})
              (:error pronto-result) (conj {:type :pronto :message (:error pronto-result)})
              (:error desc-result) (conj {:type :descriptor :message (:error desc-result)}))})
  )

(defn get-info-by-java-class
  "Get complete information for a message by its Java class name (e.g., 'cmd.JonSharedCmd$Root')"
  [java-class-name]
  ;; Map Java class to descriptor name
  (let [descriptor-info (desc-int/java-class-to-descriptor-info java-class-name)]
    (if descriptor-info
      (get-info-by-descriptor (str (:package descriptor-info) "." (:message descriptor-info)))
      {:error (str "Could not map Java class to descriptor: " java-class-name)})))

;; =============================================================================
;; Formatted Output
;; =============================================================================

(defn format-search-results
  "Format search results for display"
  [results]
  (str "Search results for: \"" (:query results) "\"\n\n"
       (str/join "\n" 
                (map-indexed 
                 (fn [idx match]
                   (format "%2d. %-30s %s (score: %s)\n    Descriptor: %-40s Proto: %s"
                          (inc idx)
                          (:message-name match)
                          (:java-class match)
                          (:score match)
                          (:descriptor-name match)
                          (:proto-file match)))
                 (:matches results)))
       "\n\nUse the descriptor name (e.g., " 
       (-> results :matches first :descriptor-name) 
       ") or Java class (e.g., "
       (-> results :matches first :java-class)
       ") with the 'info' command for details."))

(defn format-info
  "Format complete info for display"
  [info]
  (with-out-str
    (println "================================================================================")
    (println "PROTOBUF MESSAGE:" (:message-name info))
    (println "Descriptor:" (:descriptor-name info))
    (println "Java Class:" (:java-class info))
    (println "Package:" (:package info))
    (println "Proto File:" (:proto-file info))
    (println "Descriptor File:" (:descriptor-file info))
    (println "================================================================================")
    
    (when-let [java-info (:java-info info)]
      (println "\n=== JAVA CLASS INFO ===")
      (println "Package:" (:package java-info))
      (println "Methods:" (:methods-count java-info) "| Fields:" (:fields-count java-info))
      (when (seq (:field-accessors java-info))
        (println "\nField Accessors (top 5):")
        (doseq [field (take 5 (:field-accessors java-info))]
          (println (format "  %-25s -> %-30s : %s" 
                          (:proto-name field)
                          (:java-getter field)
                          (:java-type field))))))
    
    (when (:pronto-edn info)
      (println "\n=== PRONTO EDN STRUCTURE ===")
      (pprint/pprint (:pronto-edn info)))
    
    (when-let [desc (:descriptor info)]
      (println "\n=== DESCRIPTOR INFO ===")
      (println "Fields:" (count (:fields desc)))
      (when (seq (:fields desc))
        (println "\nField Details:")
        (doseq [field (take 10 (:fields desc))]
          (println (format "  [%2d] %-25s : %-15s %s%s"
                          (:number field)
                          (:name field)
                          (name (:type field))
                          (if (:type-name field) 
                            (str "(" (:type-name field) ")")
                            "")
                          (if (:constraints field)
                            " [has constraints]"
                            ""))))))
    
    (when (seq (:errors info))
      (println "\n=== ERRORS ===")
      (doseq [error (:errors info)]
        (println (str "- " (name (:type error)) ": " (:message error)))))))