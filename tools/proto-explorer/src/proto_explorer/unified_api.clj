(ns proto-explorer.unified-api
  "Unified API for protobuf exploration with fuzzy search and comprehensive info retrieval"
  (:require [proto-explorer.java-class-info :as java-info]
            [proto-explorer.pronto-integration :as pronto-int]
            [proto-explorer.descriptor-integration :as desc-int]
            [clj-fuzzy.metrics :as fuzzy]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint])
  (:import [java.io File]))

;; =============================================================================
;; Class Discovery and Search
;; =============================================================================

(defn get-all-protobuf-classes
  "Get all available protobuf message classes from the classpath"
  []
  (let [class-loader (.getContextClassLoader (Thread/currentThread))
        ;; Look for classes in known protobuf packages
        cmd-classes (try
                     (let [pkg-path "cmd"
                           urls (enumeration-seq (.getResources class-loader pkg-path))]
                       (mapcat (fn [url]
                                (when (= "file" (.getProtocol url))
                                  (let [dir (io/file (.toURI url))]
                                    (when (.exists dir)
                                      (->> (.listFiles dir)
                                           (filter #(str/ends-with? (.getName %) ".class"))
                                           (map #(str/replace (.getName %) ".class" ""))
                                           (filter #(re-find #"\$" %))  ; Only nested classes (messages)
                                           (map #(str "cmd." %)))))))
                              urls))
                     (catch Exception _ []))
        ser-classes (try
                     (let [pkg-path "ser"
                           urls (enumeration-seq (.getResources class-loader pkg-path))]
                       (mapcat (fn [url]
                                (when (= "file" (.getProtocol url))
                                  (let [dir (io/file (.toURI url))]
                                    (when (.exists dir)
                                      (->> (.listFiles dir)
                                           (filter #(str/ends-with? (.getName %) ".class"))
                                           (map #(str/replace (.getName %) ".class" ""))
                                           (filter #(re-find #"\$" %))  ; Only nested classes (messages)
                                           (map #(str "ser." %)))))))
                              urls))
                     (catch Exception _ []))]
    (distinct (concat cmd-classes ser-classes))))

(defn extract-simple-name
  "Extract the simple class name from a full class name"
  [full-name]
  (if (str/includes? full-name "$")
    (last (str/split full-name #"\$"))
    (last (str/split full-name #"\."))))

(defn fuzzy-search-classes
  "Perform fuzzy search on protobuf classes and return top N matches"
  ([query] (fuzzy-search-classes query 10))
  ([query limit]
   (let [all-classes (get-all-protobuf-classes)
         ;; Calculate scores for each class
         scored (map (fn [class-name]
                      (let [simple-name (extract-simple-name class-name)
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
                        {:class-name class-name
                         :simple-name simple-name
                         :score total-score}))
                    all-classes)
         ;; Sort by score and take top N
         top-matches (->> scored
                         (sort-by :score >)
                         (take limit))]
     {:query query
      :matches (mapv (fn [{:keys [class-name simple-name score]}]
                      {:full-name class-name
                       :simple-name simple-name
                       :score (format "%.2f" score)})
                    top-matches)})))

;; =============================================================================
;; Proto File Path Resolution
;; =============================================================================

(defn get-proto-file-path
  "Get the absolute path to the proto file from descriptor info"
  [descriptor-info]
  (when-let [file-info (first (:file descriptor-info))]
    (when-let [proto-name (:name file-info)]
      (let [base-path (io/file (System/getProperty "user.dir"))
            ;; Proto files are typically in these locations
            possible-paths [(io/file base-path "proto" proto-name)
                          (io/file base-path "tools/proto-explorer/proto" proto-name)
                          (io/file base-path "examples/protogen/proto" proto-name)]
            existing-path (first (filter #(.exists %) possible-paths))]
        (when existing-path
          (.getAbsolutePath existing-path))))))

;; =============================================================================
;; Comprehensive Info Retrieval
;; =============================================================================

(defn get-complete-class-info
  "Get all available information for a protobuf class by its full name"
  [full-class-name]
  (let [;; Parse the class name
        simple-name (extract-simple-name full-class-name)
        
        ;; 1. Java Class Info
        java-info (java-info/find-message-class simple-name)
        
        ;; 2. Pronto EDN
        pronto-result (if (:error java-info)
                        {:error "Class not found"}
                        (pronto-int/get-pronto-info simple-name))
        
        ;; 3. Descriptor Info with constraints
        desc-result (desc-int/get-message-descriptor-info simple-name)
        
        ;; 4. Proto file path
        proto-file (when (and (:success desc-result)
                             (not (:error desc-result)))
                    (or (get-proto-file-path (desc-int/load-descriptor-for-message simple-name))
                        "Proto file not found"))
        
        ;; 5. Extract field summary
        field-summary (when-not (:error java-info)
                       (mapv (fn [accessor]
                              {:proto-name (:field-name accessor)
                               :java-getter (:name (:getter accessor))
                               :java-type (-> accessor :getter :return-type :simple-name)})
                            (:field-accessors java-info)))]
    
    {:class-name full-class-name
     :simple-name simple-name
     :proto-file proto-file
     
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
              (:error desc-result) (conj {:type :descriptor :message (:error desc-result)}))}))

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
                   (format "%2d. %-30s %s (score: %s)"
                          (inc idx)
                          (:simple-name match)
                          (:full-name match)
                          (:score match)))
                 (:matches results)))
       "\n\nUse the full name (e.g., " 
       (-> results :matches first :full-name) 
       ") with the 'info' command for details."))

(defn format-class-info
  "Format complete class info for display"
  [info]
  (with-out-str
    (println "================================================================================")
    (println "PROTOBUF MESSAGE:", (:simple-name info))
    (println "Full Name:", (:class-name info))
    (when (:proto-file info)
      (println "Proto File:", (:proto-file info)))
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

(comment
  ;; Test fuzzy search
  (fuzzy-search-classes "root")
  (fuzzy-search-classes "azimuth")
  (fuzzy-search-classes "gps")
  
  ;; Test complete info
  (get-complete-class-info "cmd.JonSharedCmd$Root")
  (get-complete-class-info "ser.JonSharedData$JonGUIState")
  )