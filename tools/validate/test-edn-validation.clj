(ns test-edn-validation
  (:require [validate.validator :as v]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]))

(defn test-edn-file []
  (println "Testing EDN file validation...")
  
  ;; Read the EDN file
  (let [edn-data (edn/read-string (slurp "test/resources/valid-state.edn"))
        result (v/validate-edn edn-data :type :state)]
    
    (println "\nEDN data keys (first 10):" (take 10 (keys edn-data)))
    
    (println "\nValidation result:")
    (println "  Overall valid?:" (:valid? result))
    (println "  Message:" (:message result))
    (println "  buf.validate valid?:" (get-in result [:buf-validate :valid?]))
    (println "  Malli valid?:" (get-in result [:malli :valid?]))
    
    (when-not (get-in result [:malli :valid?])
      (println "\nMalli violations:")
      (let [violations (get-in result [:malli :violations])]
        (if (seq violations)
          (doseq [v (take 10 violations)]
            (println "  -" (:field v) ":" (:message v)))
          (println "  No specific violations recorded"))))
    
    (when-not (get-in result [:buf-validate :valid?])
      (println "\nbuf.validate violations:")
      (doseq [v (take 5 (get-in result [:buf-validate :violations]))]
        (println "  -" (:field v) ":" (:constraint v) "-" (:message v))))))

(test-edn-file)