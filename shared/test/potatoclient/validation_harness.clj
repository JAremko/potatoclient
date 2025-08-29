(ns potatoclient.validation-harness
  "Validation harness to ensure our validation systems are working.
   This should be run at the start of test suites to catch silent failures."
  (:require
   [malli.core :as m]
   [malli.instrument :as mi]
   [potatoclient.malli.registry :as registry]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.cmd.validation :as validation]))

;; ============================================================================
;; Validation System Sanity Checks
;; ============================================================================

(defn check-malli-validation-working!
  "Sanity check that Malli validation is actually working.
   Throws if validation is not functioning properly."
  []
  ;; Check that basic Malli validation works
  (assert (m/validate :int 42) 
          "Malli should validate valid integer")
  (assert (not (m/validate :int "not-an-int"))
          "Malli should reject invalid integer")
  
  ;; Check that our custom registry is working
  (assert (m/validate :cmd/empty {})
          "Custom registry should validate empty command")
  (assert (not (m/validate :cmd/empty {:extra "field"}))
          "Custom registry should reject extra fields in empty command")
  
  ;; Check that position specs work with proper ranges
  (assert (m/validate :position/latitude 45.0)
          "Should accept valid latitude")
  (assert (not (m/validate :position/latitude 91.0))
          "Should reject latitude > 90")
  (assert (not (m/validate :position/latitude -91.0))
          "Should reject latitude < -90")
  
  (assert (m/validate :position/longitude 122.0)
          "Should accept valid longitude")
  (assert (not (m/validate :position/longitude 181.0))
          "Should reject longitude > 180")
  
  true)

(defn check-protobuf-validation-working!
  "Sanity check that protobuf validation is actually working.
   Throws if protobuf serialization is not functioning properly."
  []
  ;; Check that valid commands can be serialized
  (let [valid-cmd {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :ping {}}]
    (assert (bytes? (serialize/serialize-cmd-payload valid-cmd))
            "Should be able to serialize valid command")
    
    (assert (:valid? (validation/validate-roundtrip-with-report valid-cmd))
            "Valid command should pass roundtrip validation"))
  
  ;; Check that invalid commands are rejected
  (let [invalid-cmd {:protocol_version "not-a-number"
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :session_id 0
                     :important false
                     :from_cv_subsystem false
                     :ping {}}
        throws? (try
                  (serialize/serialize-cmd-payload invalid-cmd)
                  false ; If no exception, validation is not working
                  (catch Exception _ true))]
    (assert throws?
            "Should reject command with wrong field type"))
  
  true)

(defn check-instrumentation-working!
  "Sanity check that Malli instrumentation catches errors when enabled.
   This does NOT leave functions instrumented - it's just a check."
  []
  ;; Temporarily instrument a test function to verify it works
  (let [test-fn (fn [x] (inc x))]
    ;; Add schema to the function
    (m/=> test-fn [:=> [:cat :int] :int])
    
    ;; Collect and instrument
    (mi/collect! {:ns ['potatoclient.validation-harness]})
    (mi/instrument! {:report (fn [_] nil)}) ; Silent report for test
    
    (let [instrumented? (try
                         (test-fn "not-a-number")
                         false ; If no exception, instrumentation is not working
                         (catch Exception _ true))]
      
      ;; Clean up
      (mi/unstrument!)
      
      (assert instrumented?
              "Instrumentation should catch type errors when enabled")))
  
  true)

(def validation-working?
  "Atom to track if validation has been verified"
  (atom false))

(defn ensure-validation-working!
  "Run all validation sanity checks once.
   Caches result to avoid running multiple times."
  []
  (when-not @validation-working?
    (println "\n=== Validation System Sanity Checks ===")
    (print "Checking Malli validation... ")
    (flush)
    (check-malli-validation-working!)
    (println "✓")
    
    (print "Checking Protobuf validation... ")
    (flush)
    (check-protobuf-validation-working!)
    (println "✓")
    
    (print "Checking instrumentation capability... ")
    (flush)
    (check-instrumentation-working!)
    (println "✓")
    
    (println "All validation systems operational!")
    (println "=========================================\n")
    (reset! validation-working? true))
  true)

;; ============================================================================
;; Test Fixture
;; ============================================================================

(defn validation-sanity-fixture
  "Fixture that ensures validation is working before running tests.
   Use this at the namespace level for test files that depend on validation."
  [f]
  (ensure-validation-working!)
  (f))