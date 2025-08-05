(ns check-spec-accuracy
  (:require [proto-explorer.json-to-edn :as json-edn]
            [proto-explorer.spec-generator :as spec-gen]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :as pp]))

;; Function to check a single proto file
(defn check-proto-file
  [json-file proto-file]
  (println "\n========================================")
  (println "Checking:" (.getName json-file))
  (println "Proto file:" (.getName proto-file))
  (println "========================================")
  
  (let [descriptor-set (json-edn/load-json-descriptor (.getPath json-file))
        specs (spec-gen/generate-specs descriptor-set)
        proto-content (when (.exists proto-file) (slurp proto-file))]
    
    ;; Show generated packages and message counts
    (doseq [[pkg pkg-specs] specs]
      (println (str "\nPackage: " pkg))
      (println (str "  Messages: " (count pkg-specs)))
      
      ;; For each message in specs, try to find it in proto
      (doseq [[msg-key msg-spec] pkg-specs]
        (let [msg-name (name msg-key)
              ;; Convert kebab-case back to original for searching
              proto-msg-name (str/replace msg-name #"-" "")
              pattern (re-pattern (str "(?i)message\\s+" proto-msg-name "\\s*\\{"))
              found-in-proto? (when proto-content
                               (re-find pattern proto-content))]
          
          (when (and (= :map (first msg-spec)) 
                    (not= msg-name "Root")) ; Skip showing all fields for Root
            (println (str "\n  Message: " msg-name))
            (println (str "    Found in proto: " (if found-in-proto? "YES" "NO")))
            (println (str "    Fields: " (count (rest msg-spec))))
            
            ;; Show first few fields
            (let [fields (take 3 (rest msg-spec))]
              (doseq [[field-key field-spec] fields]
                (println (str "      - " (name field-key) ": " 
                            (if (vector? field-spec)
                              (pr-str (take 2 field-spec))
                              field-spec))))))))))
  
  (println "\n----------------------------------------"))

;; Main checking function
(defn check-all-specs []
  (println "Checking generated specs against proto files...")
  
  (let [json-dir (io/file "output/json-descriptors")
        proto-dir (io/file "proto")
        json-files (filter #(and (str/ends-with? (.getName %) ".json")
                               (not= "descriptor-set.json" (.getName %)))
                          (.listFiles json-dir))]
    
    ;; Process a sample of files for manual inspection
    (doseq [json-file (take 5 json-files)]
      (let [base-name (str/replace (.getName json-file) #"\.json$" "")
            proto-file (io/file proto-dir (str base-name ".proto"))]
        (check-proto-file json-file proto-file))))
  
  (println "\n\nDone checking specs!"))

;; Run the check
(check-all-specs)