(ns potatoclient.property-test
  "Property-based testing using Malli schemas.
   
   This namespace provides property-based tests that automatically
   generate test data from Malli schemas and verify properties."
  (:require
    [clojure.test :refer [deftest is testing]]
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.properties :as prop]
    [clojure.test.check.generators :as gen]
    [malli.core :as m]
    [malli.generator :as mg]
    [potatoclient.init :as init]
    [potatoclient.test-harness :as harness]
    [potatoclient.proto.serialize :as serialize]
    [potatoclient.proto.deserialize :as deserialize]
    [potatoclient.cmd.builder :as builder]
    [potatoclient.state :as state]
    [potatoclient.ipc.transit :as transit]
    [potatoclient.malli.oneof :as oneof]))

;; Ensure test harness and registry are initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

(init/ensure-registry!)

;; ============================================================================
;; Property: Command Serialization Roundtrip
;; ============================================================================

(defspec cmd-serialization-roundtrip-property
  100 ; Number of test cases to generate
  (prop/for-all [cmd (mg/generator :cmd/root)]
    (try
      ;; Serialize and deserialize
      (let [serialized (serialize/serialize-cmd-payload* cmd)
            deserialized (deserialize/deserialize-cmd-payload serialized)]
        ;; Check that essential fields are preserved
        ;; Note: Some fields may be added/modified during serialization
        (and (= (:protocol_version cmd) (:protocol_version deserialized))
             (= (:client_type cmd) (:client_type deserialized))
             (= (:session_id cmd) (:session_id deserialized))))
      (catch Exception e
        ;; Log the failing case for debugging
        (println "Failed cmd:" (pr-str cmd))
        (println "Error:" (.getMessage e))
        false))))

;; ============================================================================
;; Property: State Serialization Roundtrip
;; ============================================================================

(defspec state-serialization-roundtrip-property
  100
  (prop/for-all [state-data (mg/generator :state/root)]
    (try
      ;; Serialize and deserialize
      (let [serialized (serialize/serialize-state-payload* state-data)
            deserialized (deserialize/deserialize-state-payload serialized)]
        ;; Check that the structure is preserved
        (and (map? deserialized)
             ;; Check that major keys are present if they were in original
             (or (nil? (:system state-data))
                 (some? (:system deserialized)))
             (or (nil? (:compass state-data))
                 (some? (:compass deserialized)))))
      (catch Exception e
        (println "Failed state:" (pr-str state-data))
        (println "Error:" (.getMessage e))
        false))))

;; ============================================================================
;; Property: Transit Message Roundtrip
;; ============================================================================

(defspec transit-message-roundtrip-property
  100
  (prop/for-all [message (gen/one-of
                           [(gen/let [type-val (gen/elements [:event :command :response])
                                      id-val gen/nat
                                      data-val (gen/map gen/keyword gen/simple-type
                                                       {:min-elements 0 :max-elements 5})]
                              (gen/return {:type type-val
                                           :id id-val
                                           :data data-val}))
                            (gen/vector gen/simple-type 0 10)
                            (gen/map gen/keyword gen/simple-type
                                     {:min-elements 0 :max-elements 5})])]
    (try
      (let [serialized (transit/write-message message)
            deserialized (transit/read-message serialized)]
        (= message deserialized))
      (catch Exception e
        (println "Failed message:" (pr-str message))
        (println "Error:" (.getMessage e))
        false))))

;; ============================================================================
;; Property: Oneof Schema Validation
;; ============================================================================

(defspec oneof-schema-generation-property
  100
  (prop/for-all [oneof-value (mg/generator
                                [:oneof
                                 [:base-field {:base true} :int]
                                 [:option-a :string]
                                 [:option-b :boolean]
                                 [:option-c [:map [:x :int] [:y :int]]]])]
    ;; Generated values should always be valid
    (let [schema [:oneof
                  [:base-field {:base true} :int]
                  [:option-a :string]
                  [:option-b :boolean]
                  [:option-c [:map [:x :int] [:y :int]]]]]
      (m/validate schema oneof-value))))

;; ============================================================================
;; Property: Command Builder Functions
;; ============================================================================

(defspec command-builder-property
  50
  (prop/for-all [field-key (gen/elements [:ping :noop :cv :rotary])]
    (try
      (let [payload {field-key {}}
            cmd (builder/populate-cmd-fields payload)]
        (and (map? cmd)
             (= (:protocol_version cmd) 1)
             (= (:client_type cmd) :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
             (= (:session_id cmd) 0)
             (= (:important cmd) false)
             (= (:from_cv_subsystem cmd) false)
             ;; Payload should be included
             (contains? cmd field-key)
             (= (get cmd field-key) {})))
      (catch Exception _
        ;; Some values might be invalid for the command
        false))))

;; ============================================================================
;; Property: State Management
;; ============================================================================

(defspec state-atom-update-property
  50
  (prop/for-all [theme (gen/elements [:sol-dark :sol-light])
                 locale (gen/elements [:english :ukrainian])
                 fullscreen gen/boolean]
    (let [test-atom (atom state/initial-state)]
      ;; Update the atom
      (swap! test-atom assoc-in [:ui :theme] theme)
      (swap! test-atom assoc-in [:ui :locale] locale)
      (swap! test-atom assoc-in [:ui :fullscreen] fullscreen)
      
      ;; Verify the updates
      (let [final-state @test-atom]
        (and (= (get-in final-state [:ui :theme]) theme)
             (= (get-in final-state [:ui :locale]) locale)
             (= (get-in final-state [:ui :fullscreen]) fullscreen)
             ;; State should still be valid according to schema
             (m/validate state/app-state-spec final-state))))))

;; ============================================================================
;; Property: Schema Validation Consistency
;; ============================================================================

(deftest schema-validation-consistency-test
  (testing "Generated values should always validate against their schemas"
    ;; Test a sample of important schemas
    (doseq [[schema-key test-count] [[:cmd/root 20]
                                      [:state/root 20]
                                      [:cmd/gps 30]
                                      [:cmd/compass 30]
                                      [:state/system 30]]]
      (testing (str "Schema: " schema-key)
        (let [schema (m/schema schema-key)]
          (dotimes [_ test-count]
            (let [generated (mg/generate schema)]
              (is (m/validate schema generated)
                  (str "Generated value should validate for " schema-key)))))))))

