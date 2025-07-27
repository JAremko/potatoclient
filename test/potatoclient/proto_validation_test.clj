(ns potatoclient.proto-validation-test
  "Test protobuf serialization without validation"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.proto :as proto]
            [potatoclient.runtime :as runtime]))

(deftest test-basic-serialization
  (testing "Basic command serialization"
    (testing "Valid commands should serialize successfully"
      (let [cmd (proto/cmd-ping 12345 :local)]
        (is (bytes? (proto/serialize-cmd cmd)))))
    
    (testing "Invalid enum values should throw during parsing"
      (let [invalid-cmd {:protocol-version 1
                         :session-id 12345
                         :important false
                         :from-cv-subsystem false
                         :client-type "INVALID_CLIENT_TYPE"
                         :ping {}}]
        ;; This should throw during enum parsing
        (is (thrown? Exception
                     (proto/serialize-cmd invalid-cmd)))))))

(deftest test-state-deserialization
  (testing "State deserialization error handling"
    (testing "Invalid state bytes should throw parse error"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Failed to deserialize state"
                            (proto/deserialize-state (byte-array [1 2 3])))))))

;; Note: We're using standard protobuf bindings without validation.
;; Validation constraints are preserved in the proto files as documentation
;; but are not enforced at runtime. Applications can implement their own
;; validation logic based on the buf.validate annotations if needed.