;; The oneof implementation is in potatoclient.specs.malli-oneof
;; The test is in proto-explorer.oneof-test

(require '[clojure.test :as test])
(require '[proto-explorer.oneof-test])

(println "Running :oneof schema type tests...")
(println "=" 80)

(let [results (test/run-tests 'proto-explorer.oneof-test)]
  (println)
  (println "Test Summary:")
  (println "  Passed:" (:pass results))
  (println "  Failed:" (:fail results))
  (println "  Errors:" (:error results))
  (System/exit (if (zero? (+ (:fail results) (:error results))) 0 1)))