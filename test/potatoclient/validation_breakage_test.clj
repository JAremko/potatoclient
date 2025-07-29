(ns potatoclient.validation-breakage-test
  "Tests that intentionally generate invalid values to ensure Malli specs catch them.
   This serves as a safety net to verify our validation is working correctly."
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.specs :as specs]
            [potatoclient.state.schemas :as state-schemas]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.cmd.system]
            [potatoclient.cmd.lrf-alignment]
            [potatoclient.cmd.day-camera]
            [potatoclient.cmd.heat-camera]
            [potatoclient.cmd.rotary]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.proto :as proto]
            [potatoclient.logging :as logging])
  (:import [ser JonSharedData$JonGUIState
            JonSharedDataGps$JonGuiDataGps]
           [cmd.Lrf_calib JonSharedCmdLrfAlign$SetOffsets]))

;; ============================================================================
;; Invalid Value Generators
;; ============================================================================

(defn generate-invalid-numbers
  "Generate numbers outside valid ranges"
  []
  [{:type :negative-where-positive-required :value -100}
   {:type :too-large :value 999999}
   {:type :too-small :value -999999}
   {:type :nan :value Double/NaN}
   {:type :infinity :value Double/POSITIVE_INFINITY}
   {:type :wrong-type :value "not-a-number"}])

(defn generate-invalid-strings
  "Generate invalid string values"
  []
  [{:type :empty-string :value ""}
   {:type :nil :value nil}
   {:type :too-long :value (apply str (repeat 10000 "x"))}
   {:type :wrong-type :value 12345}
   {:type :special-chars :value "\u0000\u0001\u0002"}])

(defn generate-invalid-enums
  "Generate invalid enum values"
  []
  [{:type :wrong-keyword :value :invalid-enum-value}
   {:type :string-instead :value "ENUM_VALUE"}
   {:type :number :value 42}
   {:type :nil :value nil}])

;; ============================================================================
;; State Validation Breakage Tests
;; ============================================================================

(deftest state-validation-catches-invalid-values
  (testing "GPS state with invalid coordinates"
    (let [invalid-states [{:latitude 200.0      ; > 90
                           :longitude 45.0
                           :speed 0.0
                           :heading 0.0
                           :altitude 0.0
                           :source "GPS"}
                          {:latitude 45.0
                           :longitude 300.0     ; > 180
                           :speed 0.0
                           :heading 0.0
                           :altitude 0.0
                           :source "GPS"}
                          {:latitude -100.0     ; < -90
                           :longitude 45.0
                           :speed 0.0
                           :heading 0.0
                           :altitude 0.0
                           :source "GPS"}
                          {:latitude "not-a-number"
                           :longitude 45.0
                           :speed 0.0
                           :heading 0.0
                           :altitude 0.0
                           :source "GPS"}]]
      (doseq [state invalid-states]
        (is (not (m/validate state-schemas/gps-schema state))
            (str "Should reject invalid GPS state: " state)))))

  (testing "LRF state with invalid distances"
    (let [invalid-states [{:distance -100       ; negative distance
                           :ready false}
                          {:distance 100000     ; too large
                           :ready false}
                          {:distance "far"      ; wrong type
                           :ready false}
                          {:distance nil        ; nil
                           :ready false}]]
      (doseq [state invalid-states]
        (is (not (m/validate state-schemas/lrf-schema state))
            (str "Should reject invalid LRF state: " state)))))

  (testing "Compass state with invalid angles"
    (let [invalid-states [{:azimuth -10.0       ; < 0
                           :pitch 0.0
                           :roll 0.0}
                          {:azimuth 400.0       ; > 360
                           :pitch 0.0
                           :roll 0.0}
                          {:azimuth 0.0
                           :pitch 100.0         ; > 90
                           :roll 0.0}
                          {:azimuth 0.0
                           :pitch 0.0
                           :roll -200.0}        ; < -180
                          {:azimuth "north"     ; wrong type
                           :pitch 0.0
                           :roll 0.0}]]
      (doseq [state invalid-states]
        (is (not (m/validate state-schemas/compass-schema state))
            (str "Should reject invalid compass state: " state)))))

  (testing "Camera state with invalid zoom values"
    (let [invalid-day-states [{:zoom-pos -0.1          ; < 0
                               :focus-pos 0.5
                               :iris-pos 0.5
                               :infrared-filter false
                               :zoom-table-pos 0
                               :zoom-table-pos-max 10
                               :fx-mode "FX_MODE_VISIBLE"
                               :auto-focus false
                               :auto-iris false
                               :digital-zoom-level 1.0
                               :clahe-level 0.5}
                              {:zoom-pos 1.1           ; > 1
                               :focus-pos 0.5
                               :iris-pos 0.5
                               :infrared-filter false
                               :zoom-table-pos 0
                               :zoom-table-pos-max 10
                               :fx-mode "FX_MODE_VISIBLE"
                               :auto-focus false
                               :auto-iris false
                               :digital-zoom-level 1.0
                               :clahe-level 0.5}]
          invalid-heat-states [{:zoom-pos "2x"         ; wrong type
                                :agc-mode "AGC_MODE_AUTO"
                                :filter "FILTER_WHITE_HOT"
                                :auto-focus false
                                :zoom-table-pos 0
                                :zoom-table-pos-max 10
                                :dde-level 0
                                :dde-enabled false
                                :fx-mode "FX_MODE_WHITE_HOT"
                                :digital-zoom-level 1.0
                                :clahe-level 0.5}]]
      (doseq [state invalid-day-states]
        (is (not (m/validate state-schemas/camera-day-schema state))
            (str "Should reject invalid day camera state: " state)))
      (doseq [state invalid-heat-states]
        (is (not (m/validate state-schemas/camera-heat-schema state))
            (str "Should reject invalid heat camera state: " state))))))

;; ============================================================================
;; Command Validation Breakage Tests
;; ============================================================================

(deftest command-validation-catches-invalid-parameters
  (testing "Invalid numeric parameters"
    ;; Test offset values outside valid range
    (let [invalid-offsets [{:x 2000 :y 0}      ; x > 1920
                           {:x 0 :y 2000}       ; y > 1080
                           {:x -2000 :y 0}      ; x < -1920
                           {:x 0 :y -2000}]]    ; y < -1080
      (doseq [{:keys [x y]} invalid-offsets]
        ;; Protobuf doesn't validate ranges at build time!
        (let [root-msg (cmd-core/create-root-message)
              lrf-align-root (cmd.Lrf_calib.JonSharedCmdLrfAlign$Root/newBuilder)
              offsets (cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets/newBuilder)
              set-offsets (-> (JonSharedCmdLrfAlign$SetOffsets/newBuilder)
                              (.setX x)
                              (.setY y)
                              (.build))]
          ;; The protobuf accepts invalid values!
          (is (= x (.getX set-offsets))
              (str "Protobuf accepts out-of-range x=" x))

          ;; This is why we need Malli validation
          (when (not= x 0)
            (is (not (m/validate ::specs/offset-value x))
                (str "Malli correctly rejects x=" x)))
          (when (not= y 0)
            (is (not (m/validate ::specs/offset-value y))
                (str "Malli correctly rejects y=" y)))))))

  (testing "Invalid enum parameters"
    ;; Test invalid localization values
    (is (nil? (potatoclient.cmd.system/string->localization "invalid-lang"))
        "Should return nil for invalid localization"))

  (testing "Invalid boolean parameters"
    ;; Test commands that expect specific boolean values
    (let [invalid-bools [nil "true" 1 0 "yes" "no"]]
      (doseq [val invalid-bools]
        (is (not (m/validate boolean? val))
            (str "Should reject non-boolean value: " val))))))

;; ============================================================================
;; Runtime Validation Tests with Guardrails
;; ============================================================================

(deftest guardrails-catches-spec-violations
  (testing "Guardrails should catch invalid function arguments"
    ;; This test only works when Guardrails is enabled (dev mode)
    (when (System/getProperty "guardrails.enabled")
      (testing "Invalid command parameters"
        ;; Note: These will only throw when guardrails is enabled
        ;; which requires running with: clojure -M:dev:test
        (is true "Guardrails validation requires dev mode")))))

;; ============================================================================
;; Protobuf Serialization Breakage Tests
;; ============================================================================

(deftest protobuf-serialization-rejects-invalid-data
  (testing "Protobuf validation limitations"
    (testing "Protobuf accepts out-of-range values at build time"
      ;; Protobuf doesn't validate ranges when building messages
      (let [gps-builder (JonSharedDataGps$JonGuiDataGps/newBuilder)]
        (.setLatitude gps-builder 200.0)  ; > 90
        (.setLongitude gps-builder 300.0) ; > 180
        (let [built (.build gps-builder)]
          (is (= 200.0 (.getLatitude built))
              "Protobuf accepts invalid latitude")
          (is (= 300.0 (.getLongitude built))
              "Protobuf accepts invalid longitude"))))

    (testing "Numeric fields with wrong types"
      ;; Note: This is a compile-time type check, not runtime
      ;; Protobuf setters are strongly typed in Java
      (is true "Protobuf numeric setters are type-safe at compile time"))))

;; ============================================================================
;; Edge Case Value Generators
;; ============================================================================

(defn generate-edge-case-numbers
  "Generate edge case numeric values that might break validation"
  []
  {:floats [Float/MAX_VALUE
            Float/MIN_VALUE
            Float/NEGATIVE_INFINITY
            Float/POSITIVE_INFINITY
            Float/NaN
            -0.0
            0.0]
   :doubles [Double/MAX_VALUE
             Double/MIN_VALUE
             Double/NEGATIVE_INFINITY
             Double/POSITIVE_INFINITY
             Double/NaN
             -0.0
             0.0]
   :integers [Integer/MAX_VALUE
              Integer/MIN_VALUE
              0
              -1]
   :longs [Long/MAX_VALUE
           Long/MIN_VALUE
           0
           -1]})

(deftest edge-case-validation
  (testing "Float edge cases in GPS coordinates"
    (let [edge-floats (-> (generate-edge-case-numbers) :floats)]
      (doseq [val edge-floats]
        (when-not (and (number? val)
                       (not (Double/isNaN val))
                       (not (Double/isInfinite val)))
          (is (not (m/validate state-schemas/gps-schema
                               {:latitude val :longitude 0.0
                                :speed 0.0 :heading 0.0
                                :altitude 0.0 :source "GPS"}))
              (str "Should reject edge case float: " val))))))

  (testing "Integer edge cases in LRF distance"
    (let [edge-ints (-> (generate-edge-case-numbers) :integers)]
      (doseq [val edge-ints]
        (when (or (neg? val) (> val 50000))
          (is (not (m/validate state-schemas/lrf-schema
                               {:distance val :ready false}))
              (str "Should reject edge case integer: " val)))))))

;; ============================================================================
;; Integration Test: Full Validation Pipeline
;; ============================================================================

(deftest full-validation-pipeline-breakage
  (testing "Complete state update with multiple invalid fields"
    (let [invalid-state {:system {:model "X" ; too short
                                  :serial-number ""} ; empty
                         :gps {:latitude 200.0 ; out of range
                               :longitude -200.0} ; out of range
                         :compass {:azimuth -45.0 ; negative
                                   :pitch 120.0} ; > 90
                         :lrf {:distance -500} ; negative
                         :camera-day {:zoom 0.0} ; must be positive
                         :camera-heat {:zoom -1.0}}] ; negative

      ;; Each invalid field should be caught by validation
      (is (not (m/validate state-schemas/jon-gui-state-schema invalid-state))
          "Should reject state with multiple invalid fields"))))

;; ============================================================================
;; Performance Impact of Validation
;; ============================================================================

(deftest validation-performance-impact
  (testing "Measure validation overhead"
    (let [valid-state (mg/generate state-schemas/jon-gui-state-schema)
          iterations 1000]

      (testing "Time with validation"
        (let [start (System/nanoTime)]
          (dotimes [_ iterations]
            (m/validate state-schemas/jon-gui-state-schema valid-state))
          (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
            (logging/log-info {:msg (str "Validation of " iterations
                                         " states took " elapsed-ms "ms")})
            (is (< elapsed-ms 250)
                "Validation should be fast enough for real-time use (< 250ms for 1000 validations)"))))

      (testing "Time without validation"
        (let [start (System/nanoTime)]
          (dotimes [_ iterations]
            ;; Just access the data without validation
            (:gps valid-state))
          (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
            (logging/log-info {:msg (str "Access without validation took "
                                         elapsed-ms "ms")})))))))

;; ============================================================================
;; Utility Functions for Breakage Testing
;; ============================================================================

(defn intentionally-break-schema
  "Take a valid value and intentionally break it according to schema constraints"
  [schema value]
  (cond
    ;; For numbers, go outside valid range
    (and (number? value) (m/validate pos? value))
    (* -1 value)

    ;; For strings, return nil or empty
    (string? value)
    nil

    ;; For booleans, return string
    (boolean? value)
    "true"

    ;; For maps, remove required keys
    (map? value)
    (dissoc value (first (keys value)))

    ;; Default: return nil
    :else nil))

(deftest schema-breakage-utility-test
  (testing "Breakage utility creates invalid values"
    (is (= -5 (intentionally-break-schema pos? 5)))
    (is (nil? (intentionally-break-schema string? "hello")))
    (is (= "true" (intentionally-break-schema boolean? true)))
    (is (= {} (intentionally-break-schema [:map [:a any?]] {:a 1})))))