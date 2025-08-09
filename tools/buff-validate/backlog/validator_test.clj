(ns buff-validate.validator-test
  "Unit tests for the validator namespace with in-memory validation."
  (:require
   [clojure.test :refer [deftest testing is]]
   [buff-validate.validator :as validator]
   [buff-validate.test-harness :as harness])
  (:import
   [java.io ByteArrayInputStream]))

(deftest test-create-validator
  (testing "Creating a buf.validate validator"
    (let [v (validator/create-validator)]
      (is (not (nil? v)) "Validator should be created")
      (is (instance? build.buf.protovalidate.Validator v) "Should be a Validator instance"))))

(deftest test-parse-messages
  (testing "Parsing state messages"
    ;; We need the actual proto classes to be compiled first
    ;; This test will work after proto generation
    (is (thrown-with-msg? Exception #"Failed to parse"
                          (validator/parse-state-message (byte-array [0x00 0x01 0x02])))
        "Invalid binary should throw parse error"))
  
  (testing "Parsing command messages"
    (is (thrown-with-msg? Exception #"Failed to parse"
                          (validator/parse-cmd-message (byte-array [0x00 0x01 0x02])))
        "Invalid binary should throw parse error")))

(deftest test-auto-detect-message-type
  (testing "Auto-detection with invalid data"
    (let [invalid-data (harness/create-invalid-binary)]
      (is (nil? (validator/auto-detect-message-type invalid-data))
          "Invalid data should return nil"))))

(deftest test-validate-binary-with-invalid-data
  (testing "Validation with completely invalid binary"
    (let [invalid-data (harness/create-invalid-binary)]
      (is (thrown-with-msg? Exception #"Could not detect message type"
                           (validator/validate-binary invalid-data))
          "Should throw when cannot detect message type"))))

(deftest test-validate-stream
  (testing "Validation from stream"
    (let [test-data (byte-array [0x08 0x01 0x10 0x02])
          stream (ByteArrayInputStream. test-data)]
      (is (thrown? Exception
                  (validator/validate-stream stream :type :state))
          "Should handle stream validation"))))

(deftest test-format-validation-result
  (testing "Formatting successful validation"
    (let [result {:valid? true
                  :message-type :state
                  :message-size 100
                  :message "Validation successful"
                  :violations []}
          formatted (validator/format-validation-result result)]
      (is (string? formatted) "Should return a string")
      (is (.contains formatted "Valid: true") "Should indicate success")
      (is (.contains formatted "Message Type: :state") "Should show message type")))
  
  (testing "Formatting failed validation with violations"
    (let [result {:valid? false
                  :message-type :cmd
                  :message-size 50
                  :message "Validation failed"
                  :violations [{:field "system.reboot.delayMs"
                               :constraint "int32.gte"
                               :message "value must be greater than or equal to 0"}]}
          formatted (validator/format-validation-result result)]
      (is (string? formatted) "Should return a string")
      (is (.contains formatted "Valid: false") "Should indicate failure")
      (is (.contains formatted "Violations:") "Should show violations section")
      (is (.contains formatted "system.reboot.delayMs") "Should show field path")
      (is (.contains formatted "int32.gte") "Should show constraint"))))

(deftest test-corrupted-binary-handling
  (testing "Handling corrupted binary data"
    (let [;; Create a valid-looking but corrupted protobuf header
          corrupted (byte-array [0x08 0xFF 0xFF 0xFF 0xFF 0xFF])]
      (is (thrown? Exception
                  (validator/validate-binary corrupted :type :state))
          "Should throw on corrupted data"))))

(deftest test-truncated-binary-handling
  (testing "Handling truncated binary data"
    (let [;; Start of a valid protobuf but truncated
          truncated (byte-array [0x08 0x01 0x10])]
      (is (thrown? Exception
                  (validator/validate-binary truncated :type :cmd))
          "Should throw on truncated data"))))

(deftest test-empty-binary-handling
  (testing "Handling empty binary data"
    (let [empty-data (byte-array 0)]
      (is (thrown? Exception
                  (validator/validate-binary empty-data))
          "Should throw on empty data"))))

(deftest test-validator-reuse
  (testing "Reusing validator instance"
    (let [v (validator/create-validator)
          data1 (byte-array [0x08 0x01])
          data2 (byte-array [0x10 0x02])]
      ;; Both should fail but validator should be reusable
      (is (thrown? Exception
                  (validator/validate-binary data1 :validator v :type :state)))
      (is (thrown? Exception
                  (validator/validate-binary data2 :validator v :type :cmd)))
      (is (not (nil? v)) "Validator should still be valid"))))