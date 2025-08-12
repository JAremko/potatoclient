#!/usr/bin/env clojure

(ns run-oneof-tests
  "Standalone test runner for oneof_edn tests"
  (:require
   [clojure.test :as t]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize the registry with oneof_edn schema
(println "\n=== Initializing Registry ===")
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))
(println "Registry initialized with oneof_edn schema")

;; Load and run the test namespace
(println "\n=== Loading Test Namespace ===")
(try
  (require 'validate.specs.oneof_edn_test)
  (println "Test namespace loaded successfully")
  
  (println "\n=== Running Tests ===")
  (let [test-ns 'validate.specs.oneof_edn_test
        results (t/run-tests test-ns)]
    (println "\n=== Test Results ===")
    (println (str "Tests run: " (:test results)))
    (println (str "Assertions: " (:assert results)))
    (println (str "Failures: " (:fail results)))
    (println (str "Errors: " (:error results)))
    
    ;; Exit with appropriate code
    (if (and (zero? (:fail results))
             (zero? (:error results)))
      (do
        (println "\n✅ All tests passed!")
        (System/exit 0))
      (do
        (println "\n❌ Some tests failed!")
        (System/exit 1))))
  
  (catch Exception e
    (println "\n❌ Error loading or running tests:")
    (println (.getMessage e))
    (.printStackTrace e)
    (System/exit 1)))