(ns generator.roundtrip-test-test
  "Test that generated code can do roundtrip conversions."
  (:require [clojure.test :refer :all]))

;; Removed - this test was for single namespace mode which we no longer use
;; We have actual roundtrip tests in simple-roundtrip-test.clj

;; Note: Full roundtrip tests would require:
;; 1. Adding generated code to classpath
;; 2. Compiling the protobuf Java classes
;; 3. Creating test data
;; 4. Running conversions
;;
;; Example test structure (requires compiled protos):
(comment
  (deftest command-roundtrip-test
    (testing "Ping command roundtrips"
      (let [original {:ping {}}
            proto (build-root original)
            roundtripped (parse-root proto)]
        (is (= original roundtripped)))))
  
  (deftest state-roundtrip-test
    (testing "Basic state roundtrips"
      (let [original {:protocol-version 1}
            proto (build-jon-gui-state original)
            roundtripped (parse-jon-gui-state proto)]
        (is (= (:protocol-version original)
               (:protocol-version roundtripped)))))))