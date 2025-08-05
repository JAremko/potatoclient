(require '[proto-explorer.nested-class-mapper :as mapper]
         '[cheshire.core :as json])

;; Test with one descriptor
(def desc (json/parse-string (slurp "output/json-descriptors/jon_shared_cmd_rotary.json") true))

(println "Extracting hierarchy...")
(def entries (mapper/extract-class-hierarchy desc))

(println "Found" (count entries) "entries")

(doseq [entry (take 5 entries)]
  (println "\nParent:" (:parent-class entry))
  (println "Children:" (:children entry)))