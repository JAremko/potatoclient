(ns potatoclient.proto.deserialize-test
  "Tests for proto deserialization utility.
   Tests both fast (*) and validating versions of deserialize functions."
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [malli.core :as m]
   [malli.generator :as mg]
   [pronto.core :as pronto]
   [pronto.utils]
   [potatoclient.malli.registry :as registry]
   [potatoclient.proto.deserialize :as deserialize]
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.state.root]
   [potatoclient.test-harness :as harness])
  (:import
   [com.google.protobuf ByteString]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Test Helpers
;; ============================================================================

;; Define Pronto mappers at compile time (proto classes must be available)
;; Run 'make compile' or 'clojure -T:build compile-all' first
(pronto/defmapper cmd-mapper [cmd.JonSharedCmd$Root])
(pronto/defmapper state-mapper [ser.JonSharedData$JonGUIState])

(defn edn->cmd-bytes
  "Convert EDN map to CMD protobuf bytes for testing."
  [edn-data]
  (try
    (let [proto-map (pronto/clj-map->proto-map cmd-mapper
                                               cmd.JonSharedCmd$Root
                                               edn-data)
          proto (pronto.utils/proto-map->proto proto-map)]
      (.toByteArray proto))
    (catch Exception e
      (throw (ex-info "Failed to convert EDN to CMD proto bytes"
                      {:error (.getMessage e)
                       :data edn-data})))))

(defn edn->state-bytes
  "Convert EDN map to State protobuf bytes for testing."
  [edn-data]
  (try
    (let [proto-map (pronto/clj-map->proto-map state-mapper
                                               ser.JonSharedData$JonGUIState
                                               edn-data)
          proto (pronto.utils/proto-map->proto proto-map)]
      (.toByteArray proto))
    (catch Exception e
      (throw (ex-info "Failed to convert EDN to State proto bytes"
                      {:error (.getMessage e)
                       :data edn-data})))))

(defn generate-valid-cmd
  "Generate a valid CMD message using Malli specs."
  []
  (mg/generate (m/schema :cmd/root)))

(defn generate-valid-state
  "Generate a valid State message using Malli specs."
  []
  (mg/generate (m/schema :state/root)))

;; ============================================================================
;; CMD Deserialization Tests
;; ============================================================================

(deftest test-deserialize-cmd-payload*
  (testing "Fast CMD deserialization without validation"
    
    (testing "Valid CMD message deserializes correctly"
      (let [original-edn (generate-valid-cmd)
            binary-data (edn->cmd-bytes original-edn)
            deserialized (deserialize/deserialize-cmd-payload* binary-data)]
        (is (map? deserialized) "Should return a map")
        (is (= (:protocol_version original-edn) 
               (:protocol_version deserialized))
            "Protocol version should match")
        (is (= (:session_id original-edn)
               (:session_id deserialized))
            "Session ID should match")))
    
    (testing "Invalid binary data throws exception"
      (is (thrown? clojure.lang.ExceptionInfo
                   (deserialize/deserialize-cmd-payload* 
                    (byte-array [1 2 3 4 5])))
          "Should throw on invalid binary data"))
    
    (testing "Empty binary data returns empty map"
      (let [result (deserialize/deserialize-cmd-payload* (byte-array []))]
        (is (map? result) "Should return a map")
        (is (empty? result) "Should return empty map for empty proto")))))

(deftest test-deserialize-cmd-payload
  (testing "CMD deserialization with full validation"
    
    (testing "Valid CMD message passes all validations"
      (let [original-edn (generate-valid-cmd)
            binary-data (edn->cmd-bytes original-edn)
            deserialized (deserialize/deserialize-cmd-payload binary-data)]
        (is (map? deserialized) "Should return a map")
        (is (m/validate :cmd/root deserialized)
            "Deserialized data should be valid according to Malli spec")))
    
    (testing "CMD with invalid protocol_version fails validation"
      (let [invalid-edn {:protocol_version 0  ; Invalid: must be > 0
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}
            binary-data (edn->cmd-bytes invalid-edn)]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"buf.validate validation failed"
                              (deserialize/deserialize-cmd-payload binary-data))
            "Should throw buf.validate error for invalid protocol_version")))
    
    (testing "CMD with no oneof field fails buf.validate"
      (let [invalid-edn {:protocol_version 1
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK}
            ;; Note: buf.validate correctly requires the oneof field
            binary-data (edn->cmd-bytes invalid-edn)]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"buf.validate validation failed"
                              (deserialize/deserialize-cmd-payload binary-data))
            "Should throw buf.validate error for missing oneof field")))
    
    (testing "Malformed binary data throws deserialization error"
      (let [result (try
                    (deserialize/deserialize-cmd-payload 
                     (byte-array [255 255 255 255]))
                    nil
                    (catch clojure.lang.ExceptionInfo e
                      (ex-data e)))]
        (is (= :deserialization-error (:type result))
            "Should throw deserialization error for malformed data")))))

;; ============================================================================
;; State Deserialization Tests
;; ============================================================================

(deftest test-deserialize-state-payload*
  (testing "Fast State deserialization without validation"
    
    (testing "Valid State message deserializes correctly"
      (let [original-edn (generate-valid-state)
            binary-data (edn->state-bytes original-edn)
            deserialized (deserialize/deserialize-state-payload* binary-data)]
        (is (map? deserialized) "Should return a map")
        (is (= (:protocol_version original-edn) 
               (:protocol_version deserialized))
            "Protocol version should match")
        ;; Check all required fields are present
        (is (contains? deserialized :system) "Should have system")
        (is (contains? deserialized :gps) "Should have gps")
        (is (contains? deserialized :compass) "Should have compass")))
    
    (testing "Invalid binary data throws exception"
      (is (thrown? clojure.lang.ExceptionInfo
                   (deserialize/deserialize-state-payload* 
                    (byte-array [1 2 3 4 5])))
          "Should throw on invalid binary data"))))

(deftest test-deserialize-state-payload
  (testing "State deserialization with full validation"
    
    (testing "Valid State message passes all validations"
      (let [original-edn (generate-valid-state)
            binary-data (edn->state-bytes original-edn)
            deserialized (deserialize/deserialize-state-payload binary-data)]
        (is (map? deserialized) "Should return a map")
        (is (m/validate :state/root deserialized)
            "Deserialized data should be valid according to Malli spec")))
    
    (testing "State with invalid GPS coordinates fails validation"
      (let [base-state (generate-valid-state)
            invalid-edn (assoc base-state
                              :gps (assoc (:gps base-state)
                                         :latitude 91.0  ; Invalid: > 90
                                         :longitude -181.0))  ; Invalid: < -180
            binary-data (edn->state-bytes invalid-edn)]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"buf.validate validation failed"
                              (deserialize/deserialize-state-payload binary-data))
            "Should throw buf.validate error for invalid GPS coordinates")))
    
    (testing "Malformed binary data throws deserialization error"
      (let [result (try
                    (deserialize/deserialize-state-payload 
                     (byte-array [255 255 255 255]))
                    nil
                    (catch clojure.lang.ExceptionInfo e
                      (ex-data e)))]
        (is (= :deserialization-error (:type result))
            "Should throw deserialization error for malformed data")))))

;; ============================================================================
;; Round-trip Tests
;; ============================================================================

(deftest test-cmd-round-trip
  (testing "CMD round-trip: EDN -> Proto -> EDN"
    (dotimes [_ 100]  ; Test with 100 random samples
      (let [original-edn (generate-valid-cmd)
            binary-data (edn->cmd-bytes original-edn)
            deserialized (deserialize/deserialize-cmd-payload binary-data)]
        ;; Check key fields are preserved
        (is (= (:protocol_version original-edn)
               (:protocol_version deserialized))
            "Protocol version should be preserved")
        (is (= (:session_id original-edn)
               (:session_id deserialized))
            "Session ID should be preserved")
        (is (= (:client_type original-edn)
               (:client_type deserialized))
            "Client type should be preserved")))))

(deftest test-state-round-trip
  (testing "State round-trip: EDN -> Proto -> EDN"
    (dotimes [_ 100]  ; Test with 100 random samples
      (let [original-edn (generate-valid-state)
            binary-data (edn->state-bytes original-edn)
            deserialized (deserialize/deserialize-state-payload binary-data)]
        ;; Check key fields are preserved
        (is (= (:protocol_version original-edn)
               (:protocol_version deserialized))
            "Protocol version should be preserved")
        ;; Check nested structures
        (when (:gps original-edn)
          (is (= (get-in original-edn [:gps :latitude])
                 (get-in deserialized [:gps :latitude]))
              "GPS latitude should be preserved"))))))

;; ============================================================================
;; Performance Comparison Tests
;; ============================================================================

(deftest test-performance-comparison
  (testing "Performance comparison between validating and non-validating versions"
    (let [cmd-samples (repeatedly 1000 generate-valid-cmd)
          cmd-bytes (mapv edn->cmd-bytes cmd-samples)
          
          ;; Time fast version
          start-fast (System/currentTimeMillis)
          _ (doseq [bytes cmd-bytes]
              (deserialize/deserialize-cmd-payload* bytes))
          fast-time (- (System/currentTimeMillis) start-fast)
          
          ;; Time validating version
          start-validating (System/currentTimeMillis)
          _ (doseq [bytes cmd-bytes]
              (deserialize/deserialize-cmd-payload bytes))
          validating-time (- (System/currentTimeMillis) start-validating)]
      
      (println (format "\nPerformance comparison (1000 CMD messages):"))
      (println (format "  Fast version (*): %d ms" fast-time))
      (println (format "  Validating version: %d ms" validating-time))
      (println (format "  Validation overhead: %.1fx slower" 
                      (/ (double validating-time) fast-time)))
      
      ;; Fast version should be significantly faster
      (is (< fast-time validating-time)
          "Fast version should be faster than validating version"))))

;; ============================================================================
;; Error Information Tests
;; ============================================================================

(deftest test-error-information
  (testing "Errors contain useful debugging information"
    
    (testing "buf.validate errors contain violation details"
      (let [invalid-edn {:protocol_version 0  ; Invalid
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}
            binary-data (edn->cmd-bytes invalid-edn)]
        (try
          (deserialize/deserialize-cmd-payload binary-data)
          (is false "Should have thrown exception")
          (catch clojure.lang.ExceptionInfo e
            (let [data (ex-data e)]
              (is (= :buf-validate-error (:type data)))
              (is (vector? (:violations data)))
              (is (some #(re-find #"protocol_version" (:field %))
                       (:violations data))
                  "Should include field name in violation"))))))
    
    (testing "Malli errors contain human-readable information"
      ;; Create a message that passes buf.validate but fails Malli
      (let [invalid-edn {:protocol_version 1
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         ;; No oneof field - will fail Malli validation
                         }
            binary-data (edn->cmd-bytes invalid-edn)]
        (try
          (deserialize/deserialize-cmd-payload binary-data)
          (is false "Should have thrown exception")
          (catch clojure.lang.ExceptionInfo e
            (let [data (ex-data e)]
              (is (= :malli-validation-error (:type data)))
              (is (= :cmd/root (:spec data)))
              (is (some? (:errors data))
                  "Should include error details"))))))))