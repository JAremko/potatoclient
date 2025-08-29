(ns potatoclient.cmd.validation-sanity-test
  "Sanity checks and negative tests to ensure our validation actually catches errors.
   Tests both Malli schema validation and protobuf (buf validate) validation.
   
   This test suite verifies that:
   1. Malli instrumentation catches invalid function inputs at runtime
   2. Malli schemas reject malformed command structures
   3. Protobuf serialization enforces type constraints
   4. Value ranges are properly validated (lat/lon, angles, etc.)
   5. Oneof field constraints are enforced
   
   Note: Functions must be instrumented with mi/instrument! for runtime validation
   to catch invalid inputs. Without instrumentation, Malli schemas are metadata-only."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.instrument :as mi]
   [potatoclient.cmd.validation :as validation]
   [potatoclient.cmd.compass :as compass]
   [potatoclient.cmd.rotary :as rotary]
   [potatoclient.cmd.gps :as gps]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]
   [potatoclient.proto.serialize :as serialize]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; Collect and instrument all cmd functions for runtime validation
(mi/collect! {:ns ['potatoclient.cmd.compass
                   'potatoclient.cmd.gps
                   'potatoclient.cmd.rotary]})
(mi/instrument!)

;; ============================================================================
;; Malli Schema Validation - Negative Tests
;; ============================================================================

(deftest malli-schema-catches-invalid-inputs
  (testing "Malli catches wrong argument types"
    ;; compass/set-magnetic-declination expects a double
    (is (thrown? Exception
                 (compass/set-magnetic-declination "not-a-number"))
        "Should reject string when double expected")
    
    (is (thrown? Exception
                 (compass/set-magnetic-declination nil))
        "Should reject nil when double expected")
    
    (is (thrown? Exception
                 (compass/set-magnetic-declination {:some "map"}))
        "Should reject map when double expected"))
  
  (testing "Malli catches wrong number of arguments"
    ;; rotary/rotate-azimuth-to expects exactly 1 argument
    (is (thrown? Exception
                 (rotary/rotate-azimuth-to))
        "Should reject no arguments when one expected")
    
    (is (thrown? Exception
                 (rotary/rotate-azimuth-to 45.0 90.0))
        "Should reject too many arguments"))
  
  (testing "Malli catches invalid boolean values"
    ;; compass/set-use-rotary-position expects a boolean
    (is (thrown? Exception
                 (compass/set-use-rotary-position "true"))
        "Should reject string when boolean expected")
    
    (is (thrown? Exception
                 (compass/set-use-rotary-position 1))
        "Should reject number when boolean expected")))

(deftest malli-output-validation-catches-malformed-commands
  (testing "Malli catches commands with missing or invalid base fields"
    ;; The :oneof schema provides defaults for base fields, so partial commands may validate
    ;; Check that completely empty command is invalid
    (let [empty-cmd {}]
      (is (not (m/validate :cmd/root empty-cmd))
          "Should reject empty command with no fields at all"))
    
    (let [invalid-cmd {:protocol_version 1
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       ;; Missing the actual command payload
                       }]
      (is (not (m/validate :cmd/root invalid-cmd))
          "Should reject command with no payload")))
  
  (testing "Malli catches commands with invalid field values"
    (let [invalid-cmd {:protocol_version -1 ; Should be positive
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       :compass {:start {}}}]
      (is (not (m/validate :cmd/root invalid-cmd))
          "Should reject negative protocol_version"))
    
    (let [invalid-cmd {:protocol_version 1
                       :client_type :INVALID_CLIENT_TYPE ; Invalid enum
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       :compass {:start {}}}]
      (is (not (m/validate :cmd/root invalid-cmd))
          "Should reject invalid client_type enum"))))

;; ============================================================================
;; Protobuf Validation - Negative Tests
;; ============================================================================

(deftest protobuf-validation-catches-invalid-structures
  (testing "Protobuf validation catches type mismatches"
    ;; Create a command with wrong field types
    (let [invalid-cmd {:protocol_version "1" ; String instead of int
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       :compass {:start {}}}]
      (is (thrown? Exception
                   (serialize/serialize-cmd-payload invalid-cmd))
          "Protobuf should reject string for int field")))
  
  (testing "Protobuf validation catches invalid enum values"
    (let [invalid-cmd {:protocol_version 1
                       :client_type :TOTALLY_INVALID_ENUM_VALUE
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       :compass {:start {}}}]
      (is (thrown? Exception
                   (serialize/serialize-cmd-payload invalid-cmd))
          "Protobuf should reject invalid enum value")))
  
  (testing "Protobuf validation catches missing required fields in nested messages"
    ;; GPS set-manual-position requires lat, lon, alt
    (let [invalid-cmd {:protocol_version 1
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       :gps {:set_manual_position {:latitude 45.0
                                                  ;; Missing longitude and altitude
                                                  }}}]
      (is (thrown? Exception
                   (serialize/serialize-cmd-payload invalid-cmd))
          "Protobuf should reject GPS command missing required fields"))))

(deftest protobuf-roundtrip-validation-catches-data-corruption
  (testing "Roundtrip validation detects data changes"
    (let [valid-cmd (compass/set-magnetic-declination 45.5)]
      ;; First verify it's valid
      (is (:valid? (validation/validate-roundtrip-with-report valid-cmd))
          "Valid command should pass roundtrip")
      
      ;; Now corrupt the data after creation
      (let [corrupted (assoc-in valid-cmd [:compass :set_magnetic_declination :value] "corrupted")]
        (is (thrown? Exception
                     (validation/validate-roundtrip-with-report corrupted))
            "Should detect corrupted field value"))))
  
  (testing "Roundtrip validation catches extra fields"
    (let [valid-cmd (compass/start)
          ;; Add an extra field that shouldn't exist
          invalid-cmd (assoc-in valid-cmd [:compass :start :extra_field] "shouldn't be here")]
      (is (thrown? Exception
                   (validation/validate-roundtrip-with-report invalid-cmd))
          "Should reject command with extra fields"))))

;; ============================================================================
;; Range and Constraint Validation
;; ============================================================================

(deftest value-range-validation
  (testing "Latitude/Longitude range validation"
    ;; Valid ranges: latitude -90 to 90, longitude -180 to 180
    (let [valid-gps (gps/set-manual-position 45.0 -122.0 100.0)]
      (is (:valid? (validation/validate-roundtrip-with-report valid-gps))
          "Valid GPS coordinates should pass"))
    
    ;; Test out of range values
    (is (thrown? Exception
                 (gps/set-manual-position 91.0 -122.0 100.0))
        "Should reject latitude > 90")
    
    (is (thrown? Exception
                 (gps/set-manual-position -91.0 -122.0 100.0))
        "Should reject latitude < -90")
    
    (is (thrown? Exception
                 (gps/set-manual-position 45.0 181.0 100.0))
        "Should reject longitude > 180")
    
    (is (thrown? Exception
                 (gps/set-manual-position 45.0 -181.0 100.0))
        "Should reject longitude < -180"))
  
  (testing "Angle range validation"
    ;; Azimuth should be 0-360, elevation typically -90 to 90
    ;; rotate-azimuth-to requires (target speed direction)
    (let [valid-rotary (rotary/rotate-azimuth-to 180.0 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)]
      (is (:valid? (validation/validate-roundtrip-with-report valid-rotary))
          "Valid azimuth should pass"))
    
    (is (thrown? Exception
                 (rotary/rotate-azimuth-to -1.0 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE))
        "Should reject negative azimuth")
    
    (is (thrown? Exception
                 (rotary/rotate-azimuth-to 361.0 0.5 :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE))
        "Should reject azimuth > 360")))

;; ============================================================================
;; Oneof Field Validation
;; ============================================================================

(deftest oneof-field-validation
  (testing "Only one command field should be present"
    (let [valid-cmd {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :session_id 0
                     :important false
                     :from_cv_subsystem false
                     :compass {:start {}}}]
      (is (m/validate :cmd/root valid-cmd)
          "Single command field should be valid"))
    
    ;; Try to create command with multiple payload fields
    (let [invalid-cmd {:protocol_version 1
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :session_id 0
                       :important false
                       :from_cv_subsystem false
                       :compass {:start {}}
                       :gps {:start {}}}] ; Two command fields!
      (is (not (m/validate :cmd/root invalid-cmd))
          "Multiple command fields should be invalid"))))

;; ============================================================================
;; Sanity Check - Verify Our Tests Actually Work
;; ============================================================================

(deftest sanity-check-validation-is-working
  (testing "Confirm valid commands pass all validation"
    (let [commands [(compass/start)
                   (compass/stop)
                   (gps/start)
                   (rotary/halt)
                   (compass/set-magnetic-declination 45.0)
                   (gps/set-manual-position 37.7749 -122.4194 10.0)]]
      (doseq [cmd commands]
        (is (m/validate :cmd/root cmd)
            "Valid command should pass Malli validation")
        (is (:valid? (validation/validate-roundtrip-with-report cmd))
            "Valid command should pass protobuf roundtrip"))))
  
  (testing "Confirm our negative tests actually catch errors"
    ;; This meta-test ensures our test framework is working
    (is (= 1 1) "Basic assertion should work")
    (is (thrown? AssertionError
                 (assert false))
        "Should be able to catch exceptions")
    
    ;; Verify Malli validation is actually running
    (is (m/validate :int 42) "Malli should validate integers")
    (is (not (m/validate :int "not-an-int")) "Malli should reject non-integers")
    
    ;; Verify protobuf serialization is actually running
    (let [valid-cmd (compass/start)
          binary (serialize/serialize-cmd-payload valid-cmd)]
      (is (bytes? binary) "Should produce byte array")
      (is (> (count binary) 0) "Should have non-empty binary"))))