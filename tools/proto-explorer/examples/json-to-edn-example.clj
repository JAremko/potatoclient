(ns json-to-edn-example
  "Example showing the reworked json->edn API"
  (:require [proto-explorer.json-to-edn :as json-edn]))

;; The json->edn function now ONLY accepts JSON strings
;; This makes the API cleaner and more predictable

(defn example-json-string []
  ;; Convert a JSON string to EDN
  (let [json-string "{\"file\":[{\"name\":\"test.proto\",\"package\":\"cmd\",\"message_type\":[{\"name\":\"Ping\",\"field\":[]}]}]}"
        result (json-edn/json->edn json-string)]
    (println "JSON string converted to EDN:")
    (println result)))

(defn example-load-from-file []
  ;; To load from a file, use load-json-descriptor
  (let [result (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd.json")]
    (println "\nLoaded from file:")
    (println (keys result))
    (println "Number of files:" (count (:file result)))))

(defn example-without-value-conversion []
  ;; You can disable proto constant conversion
  (let [json-string "{\"type\":\"TYPE_STRING\",\"label\":\"LABEL_OPTIONAL\"}"
        with-conversion (json-edn/json->edn json-string)
        without-conversion (json-edn/json->edn json-string {:convert-values? false})]
    (println "\nWith value conversion:")
    (println with-conversion)
    (println "\nWithout value conversion:")
    (println without-conversion)))

(defn example-without-metadata []
  ;; You can disable metadata enrichment for constraints
  (let [json-string "{\"file\":[{\"name\":\"test.proto\"}]}"
        with-metadata (json-edn/json->edn json-string)
        without-metadata (json-edn/json->edn json-string {:enrich-metadata? false})]
    (println "\nWith metadata enrichment:")
    (println "Has metadata?" (some? (meta (:file with-metadata))))
    (println "\nWithout metadata enrichment:")
    (println "Has metadata?" (some? (meta (:file without-metadata))))))

(defn -main []
  (example-json-string)
  (example-load-from-file)
  (example-without-value-conversion)
  (example-without-metadata))