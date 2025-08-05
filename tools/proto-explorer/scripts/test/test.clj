(require '[clojure.test :as t])
(require '[clojure.java.io :as io])

;; Add test directory to classpath
(System/setProperty "clojure.compile.path" "target/classes")

;; Load test namespaces
(load-file "test/proto_explorer/json_to_edn_test.clj")
(load-file "test/proto_explorer/spec_generator_test.clj")

;; Run tests
(println "\n===== Running JSON to EDN tests =====")
(def json-results (t/run-tests 'proto-explorer.json-to-edn-test))

(println "\n===== Running Spec Generator tests =====")
(def spec-results (t/run-tests 'proto-explorer.spec-generator-test))

;; Summary
(let [total-tests (+ (:test json-results) (:test spec-results))
      total-pass (+ (:pass json-results) (:pass spec-results))
      total-fail (+ (:fail json-results) (:fail spec-results))
      total-error (+ (:error json-results) (:error spec-results))]
  (println "\n===== SUMMARY =====")
  (println "Total tests:" total-tests)
  (println "Passed:" total-pass)
  (println "Failed:" total-fail)
  (println "Errors:" total-error)
  (System/exit (if (zero? (+ total-fail total-error)) 0 1)))