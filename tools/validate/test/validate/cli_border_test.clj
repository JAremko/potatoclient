(ns validate.cli-border-test
  "CLI-level border/edge case tests for the validate tool.
   Tests handling of damaged, empty, invalid, and truncated payloads."
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.java.io :as io]
   [validate.validator :as v]
   [pronto.core :as p]
   [validate.test-harness :as h])
  (:import
   [java.nio.file Files Paths]))

;; ============================================================================
;; Test Data Helpers
;; ============================================================================

(defn create-temp-file
  "Create a temporary file with given content."
  [prefix suffix content]
  (let [temp-file (Files/createTempFile prefix suffix (into-array java.nio.file.attribute.FileAttribute []))
        path (.toString temp-file)]
    (when content
      (if (bytes? content)
        (Files/write temp-file content (into-array java.nio.file.OpenOption []))
        (spit path content)))
    path))

(defn delete-file
  "Delete a file if it exists."
  [path]
  (try
    (Files/deleteIfExists (Paths/get path (into-array String [])))
    (catch Exception _)))

;; ============================================================================
;; Empty/Nil Data Tests
;; ============================================================================

(deftest test-empty-binary-data
  (testing "Empty binary data handling"
    (let [result (v/validate-binary (byte-array 0))]
      (is (false? (:valid? result)))
      (is (= "Binary data is empty" (:message result)))
      (is (= 0 (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

(deftest test-nil-binary-data
  (testing "Nil binary data handling"
    (let [result (v/validate-binary nil)]
      (is (false? (:valid? result)))
      (is (= "Binary data is nil" (:message result)))
      (is (= 0 (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

(deftest test-empty-file
  (testing "Empty file handling"
    (let [path (create-temp-file "empty" ".bin" (byte-array 0))]
      (try
        (let [result (v/validate-file path)]
          (is (false? (:valid? result)))
          (is (= "Binary data is empty" (:message result)))
          (is (= 0 (:message-size result))))
        (finally
          (delete-file path))))))

;; ============================================================================
;; Corrupted/Invalid Data Tests
;; ============================================================================

(deftest test-random-binary-data
  (testing "Random binary data (not protobuf)"
    (let [random-data (byte-array (repeatedly 100 #(byte (- (rand-int 256) 128))))
          result (v/validate-binary random-data)]
      (is (false? (:valid? result)))
      (is (= 100 (:message-size result)))
      ;; Should fail to parse as valid protobuf
      (is (or (= "Could not detect or parse message type" (:message result))
              (= "Failed to parse binary as protobuf message" (:message result))
              (re-find #"Both validations failed" (:message result)))))))

(deftest test-text-as-binary
  (testing "Plain text data instead of protobuf"
    (let [text-data (.getBytes "This is not a protobuf message!")
          result (v/validate-binary text-data)]
      (is (false? (:valid? result)))
      (is (pos? (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

(deftest test-json-as-binary
  (testing "JSON data instead of protobuf"
    (let [json-data (.getBytes "{\"hello\": \"world\", \"number\": 42}")
          result (v/validate-binary json-data)]
      (is (false? (:valid? result)))
      (is (pos? (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

;; ============================================================================
;; Truncated Message Tests
;; ============================================================================

(deftest test-truncated-state-message
  (testing "Truncated state message (partial binary)"
    (let [;; Create a valid state message
          valid-state (h/valid-state)
          full-binary (p/proto-map->bytes valid-state)
          ;; Truncate to 50% of original size
          truncated (byte-array (take (/ (count full-binary) 2) full-binary))
          result (v/validate-binary truncated)]
      (is (false? (:valid? result)))
      (is (= (count truncated) (:message-size result)))
      ;; Should fail to parse or validate
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

(deftest test-truncated-cmd-message
  (testing "Truncated command message (partial binary)"
    (let [;; Create a valid command message
          valid-cmd (h/valid-ping-cmd)
          full-binary (p/proto-map->bytes valid-cmd)
          ;; Truncate to just first 10 bytes
          truncated (byte-array (take 10 full-binary))
          result (v/validate-binary truncated)]
      (is (false? (:valid? result)))
      (is (= 10 (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

;; ============================================================================
;; Single Byte Tests
;; ============================================================================

(deftest test-single-byte-data
  (testing "Single byte data"
    (doseq [byte-val [0 1 127 -128 -1 42]]
      (let [result (v/validate-binary (byte-array [byte-val]))]
        (is (false? (:valid? result)))
        (is (= 1 (:message-size result)))
        (is (false? (get-in result [:buf-validate :valid?])))
        (is (false? (get-in result [:malli :valid?])))))))

;; ============================================================================
;; Wrong Message Type Tests
;; ============================================================================

(deftest test-state-parsed-as-cmd
  (testing "Valid state message incorrectly parsed as command"
    (let [valid-state (h/valid-state)
          state-binary (p/proto-map->bytes valid-state)
          result (v/validate-binary state-binary :type :cmd)]
      (is (false? (:valid? result)))
      (is (= :cmd (:message-type result)))
      ;; buf.validate should fail
      (is (false? (get-in result [:buf-validate :valid?])))
      ;; Should have violations about missing cmd fields
      (is (seq (get-in result [:buf-validate :violations]))))))

(deftest test-cmd-parsed-as-state
  (testing "Valid command message incorrectly parsed as state"
    (let [valid-cmd (h/valid-ping-cmd)
          cmd-binary (p/proto-map->bytes valid-cmd)
          result (v/validate-binary cmd-binary :type :state)]
      (is (false? (:valid? result)))
      (is (= :state (:message-type result)))
      ;; Should fail validation
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

;; ============================================================================
;; Incomplete Message Tests
;; ============================================================================

(deftest test-state-missing-required-fields
  (testing "State message with missing required sub-messages"
    ;; Create minimal proto that might parse but won't validate
    (let [minimal-bytes (byte-array [8 1]) ; Just protocol_version = 1
          result (v/validate-binary minimal-bytes :type :state)]
      (is (false? (:valid? result)))
      (is (= :state (:message-type result)))
      ;; Should have violations for missing required fields
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

(deftest test-cmd-without-payload
  (testing "Command message without any payload (violates oneof)"
    ;; Create bytes with protocol_version and client_type but no payload
    (let [no-payload-bytes (byte-array [8 1 16 1]) ; protocol_version=1, client_type=1
          result (v/validate-binary no-payload-bytes :type :cmd)]
      (is (false? (:valid? result)))
      (is (= :cmd (:message-type result)))
      ;; Should have violation about missing oneof payload
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (seq (get-in result [:buf-validate :violations]))))))

;; ============================================================================
;; Large/Stress Test Cases
;; ============================================================================

(deftest test-very-large-random-data
  (testing "Very large random binary (1MB)"
    (let [large-data (byte-array (repeatedly (* 1024 1024) #(byte (- (rand-int 256) 128))))
          result (v/validate-binary large-data)]
      (is (false? (:valid? result)))
      (is (= (* 1024 1024) (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

(deftest test-repeated-pattern-data
  (testing "Repeated pattern data (not valid protobuf)"
    (let [pattern (byte-array [1 2 3 4 5])
          repeated (byte-array (flatten (repeat 100 pattern)))
          result (v/validate-binary repeated)]
      (is (false? (:valid? result)))
      (is (= 500 (:message-size result)))
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (false? (get-in result [:malli :valid?]))))))

;; ============================================================================
;; File System Edge Cases
;; ============================================================================

(deftest test-nonexistent-file
  (testing "Nonexistent file handling"
    (is (thrown? Exception
                 (v/validate-file "/nonexistent/path/to/file.bin")))))

(deftest test-directory-as-file
  (testing "Directory instead of file"
    (let [temp-dir (.toString (Files/createTempDirectory "test-dir" (into-array java.nio.file.attribute.FileAttribute [])))]
      (try
        (is (thrown? Exception
                     (v/validate-file temp-dir)))
        (finally
          (Files/deleteIfExists (Paths/get temp-dir (into-array String []))))))))

;; ============================================================================
;; Mixed Valid/Invalid Tests
;; ============================================================================

(deftest test-valid-proto-invalid-constraints
  (testing "Valid protobuf structure but violates buf.validate constraints"
    ;; This would need a specially crafted message that parses but has invalid values
    ;; For example, GPS coordinates out of range
    (let [state (h/valid-state)
          ;; Modify to have invalid GPS coordinates
          modified-state (p/p-> state
                               (assoc-in [:gps :latitude] 200.0) ; Invalid: > 90
                               (assoc-in [:gps :longitude] -400.0)) ; Invalid: > 180
          binary (p/proto-map->bytes modified-state)
          result (v/validate-binary binary)]
      (is (false? (:valid? result)))
      (is (= :state (:message-type result)))
      ;; buf.validate should catch the constraint violations
      (is (false? (get-in result [:buf-validate :valid?])))
      (is (seq (get-in result [:buf-validate :violations]))))))

;; ============================================================================
;; Auto-detection Edge Cases
;; ============================================================================

(deftest test-auto-detect-ambiguous-data
  (testing "Auto-detection with ambiguous binary data"
    ;; Some binary patterns might parse as both types
    (let [ambiguous (byte-array [8 1 16 1 24 1]) ; Could be either type
          result (v/validate-binary ambiguous :type :auto)]
      (is (false? (:valid? result)))
      ;; Should pick one type or the other
      (is (#{:state :cmd nil} (:message-type result))))))

;; ============================================================================
;; Concurrent Validation Test
;; ============================================================================

(deftest test-concurrent-validations
  (testing "Multiple concurrent validations don't interfere"
    (let [state-data (p/proto-map->bytes (h/valid-state))
          cmd-data (p/proto-map->bytes (h/valid-ping-cmd))
          empty-data (byte-array 0)
          random-data (byte-array [1 2 3 4 5])
          
          ;; Run validations in parallel
          futures [(future (v/validate-binary state-data))
                   (future (v/validate-binary cmd-data))
                   (future (v/validate-binary empty-data))
                   (future (v/validate-binary random-data))]
          
          results (map deref futures)]
      
      ;; First should be valid state
      (is (true? (:valid? (nth results 0))))
      (is (= :state (:message-type (nth results 0))))
      
      ;; Second should be valid cmd
      (is (true? (:valid? (nth results 1))))
      (is (= :cmd (:message-type (nth results 1))))
      
      ;; Third should be invalid (empty)
      (is (false? (:valid? (nth results 2))))
      
      ;; Fourth should be invalid (random)
      (is (false? (:valid? (nth results 3)))))))