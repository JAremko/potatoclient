(require '[proto-explorer.json-to-edn :as json-edn])
(require '[clojure.java.io :as io])
(require '[clojure.string :as str])

(println "Checking descriptors against proto files...")

(let [json-dir (io/file "output/json-descriptors")
      json-files (filter #(and (str/ends-with? (.getName %) ".json")
                             (not= "descriptor-set.json" (.getName %)))
                        (.listFiles json-dir))]
  
  ;; Process a sample of files
  (doseq [json-file (take 3 json-files)]
    (println "\n========================================")
    (println "Checking:" (.getName json-file))
    (println "========================================")
    
    (let [descriptor-set (json-edn/load-json-descriptor (.getPath json-file))]
      ;; Show descriptor structure
      (doseq [file-desc (:file descriptor-set)]
        (when (re-find #"jon_shared" (:name file-desc))
          (println "Package:" (:package file-desc))
          (println "Messages:" (count (:messageType file-desc)))
          
          ;; Show first few messages
          (doseq [msg (take 3 (:messageType file-desc))]
            (println "  - Message:" (:name msg) 
                    "Fields:" (count (:field msg)))))))))

(println "\nDone checking!")