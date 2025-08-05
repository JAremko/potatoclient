(ns run-integration-tests
  (:require [clojure.test :as t]
            [clojure.java.io :as io]))

(println "Running integration tests...")
(println "Checking if specs exist...")

(def specs-dir (io/file "../../shared/specs/protobuf"))
(def cmd-spec-file (io/file specs-dir "cmd_specs.clj"))
(def state-spec-file (io/file specs-dir "state_specs.clj"))

(println "Specs directory:" (.getAbsolutePath specs-dir))
(println "CMD specs exist?" (.exists cmd-spec-file))
(println "State specs exist?" (.exists state-spec-file))

(if (and (.exists cmd-spec-file) (.exists state-spec-file))
  (do
    (println "\nSpecs found! Running tests...")
    (require 'integration.generated-specs-test)
    (require 'integration.protobuf-roundtrip-test)
    
    ;; Run the tests
    (let [results (t/run-tests 'integration.generated-specs-test
                              'integration.protobuf-roundtrip-test)]
      (println "\nTest Results:")
      (println "Tests run:" (:test results))
      (println "Assertions:" (:assert results))
      (println "Failures:" (:fail results))
      (println "Errors:" (:error results))
      
      ;; Exit with appropriate code
      (System/exit (if (zero? (+ (:fail results) (:error results))) 0 1))))
  (do
    (println "\nSpecs not found. Please generate them first with:")
    (println "  ./generate-specs.sh")
    (System/exit 1)))