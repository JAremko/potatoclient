(require '[clojure.test :as t])

;; Load only unit test namespaces
(require '[proto-explorer.json-to-edn-test])
(require '[proto-explorer.spec-generator-test])

;; Run tests
(def results
  (t/run-tests
    'proto-explorer.json-to-edn-test
    'proto-explorer.spec-generator-test))

;; Exit with proper code
(System/exit (if (zero? (+ (:fail results) (:error results))) 0 1))