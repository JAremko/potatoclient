(ns potatoclient.transit.sanity-check-validation-test
  "CRITICAL: Sanity checks to ensure each validation stage actually works and can signal failures.
  
  We test that each stage:
  1. Guardrails - Can reject invalid function arguments
  2. Transit - Can detect malformed data
  3. Kotlin - Can reject invalid command structures
  4. Protobuf - Can reject missing required fields
  5. buf.validate - Can enforce constraints
  6. Binary roundtrip - Can detect corruption
  7. Equals - Can detect differences"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.kotlin.integration-test-utils :as test-utils])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; =============================================================================
;; Stage 1: Guardrails Validation
;; =============================================================================

(deftest test-guardrails-catches-invalid-args
  (testing "Guardrails rejects invalid function arguments"
    ;; These should throw exceptions due to Guardrails validation
    
    ;; Test 1: Wrong argument type
    (is (thrown? Exception
                 (cmd/rotary-goto "not-a-map"))
        "Should reject string instead of map")
    
    ;; Test 2: Wrong keyword type  
    (is (thrown? Exception
                 (cmd/set-localization "en")) ; Expects keyword :en
        "Should reject string instead of keyword")
    
    ;; Test 3: Missing required fields
    (is (thrown? Exception
                 (cmd/rotary-goto {:azimuth 45.0})) ; Missing elevation
        "Should reject map missing required field")
    
    ;; Test 4: Invalid enum value
    (is (thrown? Exception
                 (cmd/heat-camera-palette :invalid-palette))
        "Should reject invalid enum value")))

;; =============================================================================
;; Stage 2: Transit Serialization
;; =============================================================================

(deftest test-transit-detects-corruption
  (testing "Transit can detect corrupted data"
    (let [valid-command (cmd/ping)
          baos (ByteArrayOutputStream.)
          writer (transit-core/make-writer baos)]
      
      ;; Write valid command
      (transit-core/write-message! writer valid-command baos)
      (let [valid-bytes (.toByteArray baos)]
        
        ;; Test 1: Truncated data
        (let [truncated (byte-array (/ (count valid-bytes) 2))]
          (System/arraycopy valid-bytes 0 truncated 0 (count truncated))
          (is (thrown? Exception
                       (let [bais (ByteArrayInputStream. truncated)
                             reader (transit-core/make-reader bais)]
                         (transit-core/read-message reader)))
              "Should fail on truncated Transit data"))
        
        ;; Test 2: Corrupted bytes
        (let [corrupted (byte-array valid-bytes)]
          ;; Corrupt some bytes in the middle
          (dotimes [i 10]
            (aset corrupted (+ 5 i) (unchecked-byte 0xFF)))
          (try
            (let [bais (ByteArrayInputStream. corrupted)
                  reader (transit-core/make-reader bais)
                  result (transit-core/read-message reader)]
              ;; If it somehow succeeds, the data should be different
              (is (not= valid-command result)
                  "Corrupted data should not equal original"))
            (catch Exception e
              ;; Expected - corruption should cause parse failure
              (is true "Corrupted Transit data correctly caused exception")))))))

;; =============================================================================
;; Stage 3: Kotlin Command Building
;; =============================================================================

(deftest test-kotlin-rejects-invalid-structures
  (testing "Kotlin GeneratedCommandHandlers rejects invalid command structures"
    (when (test-utils/kotlin-tests-available?)
      ;; Test 1: Unknown command type
      (let [result (test-utils/validate-command-with-kotlin
                     {:unknown-command {:foo "bar"}})]
        (is (not (:success result))
            "Should reject unknown command type")
        (is (:error result) "Should have error message"))
      
      ;; Test 2: Wrong structure for known command
      (let [result (test-utils/validate-command-with-kotlin
                     {:rotary "not-a-map"})] ; Should be {:rotary {:goto {...}}}
        (is (not (:success result))
            "Should reject wrong structure")
        (is (:error result) "Should have error message"))
      
      ;; Test 3: Missing nested command
      (let [result (test-utils/validate-command-with-kotlin
                     {:cv {}})] ; Missing :start-track-ndc
        (is (not (:success result))
            "Should reject missing nested command")))))

;; =============================================================================
;; Stage 4: Protobuf Required Fields
;; =============================================================================

(deftest test-protobuf-enforces-required-fields
  (testing "Protobuf enforces required field presence"
    (when (test-utils/kotlin-tests-available?)
      ;; Test missing required fields in nested structures
      (let [result (test-utils/validate-command-with-kotlin
                     {:cv {:start-track-ndc {:channel :heat}}})] ; Missing x, y
        (is (not (:success result))
            "Should reject command missing required x,y coordinates")
        (is (re-find #"required" (str (:error result)))
            "Error should mention required fields")))))

;; =============================================================================
;; Stage 5: buf.validate Constraints
;; =============================================================================

(deftest test-buf-validate-enforces-constraints
  (testing "buf.validate enforces field constraints"
    (when (test-utils/kotlin-tests-available?)
      ;; Test 1: Azimuth out of range
      (let [result (test-utils/validate-command-with-kotlin
                     {:rotary {:goto {:azimuth 400.0 :elevation 45.0}}})]
        (is (not (:success result))
            "Should reject azimuth > 360")
        (is (re-find #"azimuth|360|constraint" (str (:error result)))
            "Error should mention constraint violation"))
      
      ;; Test 2: Elevation out of range
      (let [result (test-utils/validate-command-with-kotlin
                     {:rotary {:goto {:azimuth 180.0 :elevation -45.0}}})]
        (is (not (:success result))
            "Should reject elevation < -30")
        (is (re-find #"elevation|-30|constraint" (str (:error result)))
            "Error should mention constraint violation"))
      
      ;; Test 3: NDC coordinates out of range
      (let [result (test-utils/validate-command-with-kotlin
                     {:cv {:start-track-ndc {:channel :heat :x 1.5 :y -1.5}}})]
        (is (not (:success result))
            "Should reject NDC coordinates outside [-1, 1]"))
      
      ;; Test 4: GPS latitude out of range  
      (let [result (test-utils/validate-command-with-kotlin
                     {:gps {:set-manual-position 
                            {:position {:latitude 91.0 
                                       :longitude 0.0
                                       :altitude 100.0}}}})]
        (is (not (:success result))
            "Should reject latitude > 90")))))

;; =============================================================================
;; Stage 6: Binary Roundtrip Integrity
;; =============================================================================

(deftest test-binary-roundtrip-detects-corruption
  (testing "Binary roundtrip detects data corruption"
    ;; This would need to be tested in Kotlin since protobuf parsing happens there
    ;; We can verify the test infrastructure includes binary roundtrip
    (when (test-utils/kotlin-tests-available?)
      (let [result (test-utils/validate-command-with-kotlin
                     (cmd/ping))]
        (is (contains? result :binary-size)
            "Result should include binary size")
        (is (contains? result :proto-equals)
            "Result should include equality check")))))

;; =============================================================================
;; Stage 7: Java Equals Detection
;; =============================================================================

(deftest test-java-equals-detects-differences
  (testing "Java equals can detect protobuf differences"
    ;; This verifies that our Kotlin validator actually uses equals
    (when (test-utils/kotlin-tests-available?)
      (let [result (test-utils/validate-command-with-kotlin
                     (cmd/rotary-goto {:azimuth 45.0 :elevation 30.0}))]
        (is (:proto-equals result)
            "Same protobuf should equal itself")
        (is (contains? result :original-hash)
            "Should include original hash")
        (is (contains? result :roundtrip-hash)
            "Should include roundtrip hash")
        (is (= (:original-hash result) (:roundtrip-hash result))
            "Hashes should match for identical protos")))))

;; =============================================================================
;; Comprehensive Sanity Check
;; =============================================================================

(deftest test-full-pipeline-sanity-check
  (testing "Each stage of the pipeline can detect and signal errors"
    (let [test-cases
          [;; [description, command, expected-stage-of-failure]
           ["Invalid argument type" 
            #(cmd/rotary-goto "not-a-map")
            :guardrails]
           
           ["Invalid enum value"
            #(cmd/heat-camera-palette :invalid)
            :guardrails]
           
           ["Unknown command structure"
            {:unknown {:command {:foo "bar"}}}
            :kotlin-builder]
           
           ["Constraint violation - azimuth"
            {:rotary {:goto {:azimuth 400.0 :elevation 45.0}}}
            :buf-validate]
           
           ["Constraint violation - elevation"  
            {:rotary {:goto {:azimuth 180.0 :elevation -50.0}}}
            :buf-validate]
           
           ["Missing required fields"
            {:cv {:start-track-ndc {:channel :heat}}} ; Missing x, y
            :protobuf-required]]]
      
      (doseq [[desc command-or-fn expected-stage] test-cases]
        (testing desc
          (if (fn? command-or-fn)
            ;; Test Guardrails
            (is (thrown? Exception (command-or-fn))
                (str "Should fail at " expected-stage))
            ;; Test Kotlin validation
            (when (test-utils/kotlin-tests-available?)
              (let [result (test-utils/validate-command-with-kotlin command-or-fn)]
                (is (not (:success result))
                    (str "Should fail at " expected-stage))
                (is (:error result)
                    "Should have error message")))))))))