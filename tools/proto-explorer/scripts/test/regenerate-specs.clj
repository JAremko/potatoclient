#!/usr/bin/env clojure

(require '[proto-explorer.spec-generator :as gen])

(println "Regenerating all protobuf specs...")
(println "================================================")

;; Generate to the shared directory
(let [shared-dir "../../../shared"
      results (gen/generate-all-specs! shared-dir)]
  (println)
  (println "Regeneration complete!")
  (println "Results:" results))

(System/exit 0)