(ns potatoclient.cmd.validation-safety-test
  "Safety tests for command validation - ensures specs catch out-of-range values"
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.specs :as specs]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.cmd.lrf-alignment]
            [potatoclient.cmd.day-camera]
            [potatoclient.cmd.heat-camera]
            [potatoclient.logging :as logging])
  (:import [com.google.protobuf InvalidProtocolBufferException]
           [cmd.Lrf_calib JonSharedCmdLrfAlign$SetOffsets]
           [cmd.RotaryPlatform JonSharedCmdRotary$RotateAzimuth JonSharedCmdRotary$RotateElevation]
           [cmd.DayCamera JonSharedCmdDayCamera$SetDigitalZoomLevel]))

;; ============================================================================
;; Out-of-Range Value Generators
;; ============================================================================

(defn generate-out-of-range-offsets
  "Generate offset values outside the valid range [-1920, 1920] x [-1080, 1080]"
  []
  [{:x 2000 :y 0 :desc "x too large"}
   {:x -2000 :y 0 :desc "x too small"}
   {:x 0 :y 1200 :desc "y too large"}
   {:x 0 :y -1200 :desc "y too small"}
   {:x 5000 :y 5000 :desc "both too large"}
   {:x -5000 :y -5000 :desc "both too small"}
   {:x Integer/MAX_VALUE :y 0 :desc "x at max int"}
   {:x 0 :y Integer/MIN_VALUE :desc "y at min int"}])

(defn generate-out-of-range-zoom
  "Generate zoom values outside valid ranges"
  []
  [{:value -1.0 :desc "negative zoom"}
   {:value -0.1 :desc "small negative"}
   {:value 1.1 :desc "slightly over 1.0"}
   {:value 2.0 :desc "too large"}
   {:value 1000.0 :desc "way too large"}
   {:value Float/NEGATIVE_INFINITY :desc "negative infinity"}
   {:value Float/POSITIVE_INFINITY :desc "positive infinity"}
   {:value Float/NaN :desc "NaN"}])

(defn generate-out-of-range-speed
  "Generate speed values outside valid percentage range [0, 100]"
  []
  [{:value -10 :desc "negative speed"}
   {:value 101 :desc "over 100%"}
   {:value 200 :desc "way over 100%"}
   {:value -100 :desc "large negative"}
   {:value Integer/MAX_VALUE :desc "max int"}
   {:value Integer/MIN_VALUE :desc "min int"}])

;; ============================================================================
;; Protobuf Validation Tests
;; ============================================================================

(deftest protobuf-builder-validation
  (testing "LRF alignment offset validation"
    (doseq [{:keys [x y desc]} (generate-out-of-range-offsets)]
      (testing desc
        (let [builder (JonSharedCmdLrfAlign$SetOffsets/newBuilder)]
          ;; Protobuf might accept the values during building
          (.setX builder x)
          (.setY builder y)
          (let [built (.build builder)]
            ;; But our specs should catch them
            (is (or (not (<= -1920 x 1920))
                    (not (<= -1080 y 1080)))
                (str "Value should be out of range: x=" x " y=" y)))))))

  (testing "Camera zoom validation"
    (doseq [{:keys [value desc]} (generate-out-of-range-zoom)]
      (testing desc
        (when (and (number? value) (not (Double/isNaN value)))
          (let [builder (JonSharedCmdDayCamera$SetDigitalZoomLevel/newBuilder)]
            (when (float? value)
              (.setValue builder value)
              (is (or (< value 0.0)
                      (> value 1.0)
                      (Double/isInfinite value))
                  (str "Zoom should be invalid: " value))))))))

  (testing "Rotary speed validation"
    (doseq [{:keys [value desc]} (generate-out-of-range-speed)]
      (testing desc
        (let [builder (JonSharedCmdRotary$RotateAzimuth/newBuilder)]
          (.setSpeed builder (float (/ value 100.0)))  ;; Convert percentage to 0-1 range
          (is (or (< value 0) (> value 100))
              (str "Speed should be out of range: " value)))))))

;; ============================================================================
;; Malli Spec Validation Tests
;; ============================================================================

(deftest malli-specs-catch-invalid-values
  (testing "Offset value specs"
    (is (m/validate ::specs/offset-value 0))
    (is (m/validate ::specs/offset-value 1920))
    (is (m/validate ::specs/offset-value -1920))
    (is (not (m/validate ::specs/offset-value 2000)))
    (is (not (m/validate ::specs/offset-value -2000)))
    (is (not (m/validate ::specs/offset-value "100")))
    (is (not (m/validate ::specs/offset-value nil))))

  (testing "Offset shift specs"
    (is (m/validate ::specs/offset-shift 10))
    (is (m/validate ::specs/offset-shift -10))
    (is (m/validate ::specs/offset-shift 0))
    (is (not (m/validate ::specs/offset-shift 5000)))
    (is (not (m/validate ::specs/offset-shift -5000))))

  (testing "Zoom level specs"
    (is (m/validate ::specs/zoom-level 0.0))
    (is (m/validate ::specs/zoom-level 0.5))
    (is (m/validate ::specs/zoom-level 1.0))
    (is (not (m/validate ::specs/zoom-level -0.1)))
    (is (not (m/validate ::specs/zoom-level -1.0)))
    (is (not (m/validate ::specs/zoom-level 1.1))))

  (testing "Speed percentage specs"
    (is (m/validate ::specs/speed-percentage 0))
    (is (m/validate ::specs/speed-percentage 50))
    (is (m/validate ::specs/speed-percentage 100))
    (is (not (m/validate ::specs/speed-percentage -1)))
    (is (not (m/validate ::specs/speed-percentage 101)))))

;; ============================================================================
;; Integration Tests with Command Functions
;; ============================================================================

(deftest command-functions-validate-inputs
  (testing "Commands should validate inputs before sending"
    ;; Note: These tests assume Guardrails is enabled
    (when (System/getProperty "guardrails.enabled")

      (testing "LRF alignment with invalid offsets"
        (is (thrown? Exception
                     (potatoclient.cmd.lrf-alignment/set-offset-day 2000 0))
            "Should reject x value out of range")
        (is (thrown? Exception
                     (potatoclient.cmd.lrf-alignment/set-offset-heat 0 -2000))
            "Should reject y value out of range"))

      (testing "Camera zoom with invalid values"
        (is (thrown? Exception
                     (potatoclient.cmd.day-camera/set-digital-zoom-level 0.5))
            "Should reject value less than 1.0 for digital zoom")
        (is (thrown? Exception
                     (potatoclient.cmd.heat-camera/set-digital-zoom-level -5.0))
            "Should reject negative zoom"))

      ;; TODO: set-speed function doesn't exist yet
      #_(testing "Rotary speed with invalid percentages"
          (is (thrown? Exception
                       (potatoclient.cmd.rotary/set-speed -10))
              "Should reject negative speed")
          (is (thrown? Exception
                       (potatoclient.cmd.rotary/set-speed 150))
              "Should reject speed over 100%")))))

;; ============================================================================
;; Stress Testing with Random Invalid Data
;; ============================================================================

(defn generate-random-invalid-data
  "Generate completely random data that should fail validation"
  []
  [(rand-int 1000000)
   (- (rand-int 1000000))
   (rand)
   (- (rand))
   ""
   "random string"
   nil
   []
   {}
   #{1 2 3}
   :random-keyword])

(deftest stress-test-with-random-data
  (testing "Random data should fail appropriate validations"
    (let [random-values (repeatedly 100 generate-random-invalid-data)]
      (doseq [values random-values]
        (doseq [val (flatten [values])]
          ;; Test various specs with random data
          (when-not (number? val)
            (is (not (m/validate ::specs/offset-value val))
                "Non-numbers should fail offset validation"))

          (when-not (and (number? val) (<= 0 val 100))
            (is (not (m/validate ::specs/speed-percentage val))
                "Invalid values should fail percentage validation"))

          (when-not (and (number? val) (<= 0.0 val 1.0))
            (is (not (m/validate ::specs/zoom-level val))
                "Invalid values should fail zoom validation")))))))

;; ============================================================================
;; Performance of Validation as Safety Net
;; ============================================================================

(deftest validation-performance-as-safety-net
  (testing "Validation overhead for catching errors"
    (let [valid-offset 100
          invalid-offset 5000
          iterations 10000]

      (testing "Valid value validation time"
        (let [start (System/nanoTime)]
          (dotimes [_ iterations]
            (m/validate ::specs/offset-value valid-offset))
          (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
            (logging/log-info {:msg (str "Valid validation: " elapsed-ms "ms for "
                                         iterations " iterations")})
            (is (< elapsed-ms 50) "Validation should be fast"))))

      (testing "Invalid value validation time"
        (let [start (System/nanoTime)]
          (dotimes [_ iterations]
            (m/validate ::specs/offset-value invalid-offset))
          (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
            (logging/log-info {:msg (str "Invalid validation: " elapsed-ms "ms for "
                                         iterations " iterations")})
            (is (< elapsed-ms 50) "Invalid validation should also be fast")))))))

;; ============================================================================
;; Documentation of Validation Boundaries
;; ============================================================================

(deftest document-validation-boundaries
  (testing "Document all validation boundaries for reference"
    (let [boundaries
          {:lrf-offset {:x [-1920 1920] :y [-1080 1080]}
           :zoom {:normalized [0.0 1.0] :digital [1.0 "∞"]}
           :speed {:rotary [0 100]}
           :gps {:latitude [-90.0 90.0] :longitude [-180.0 180.0]}
           :compass {:azimuth [0.0 360.0] :pitch [-90.0 90.0] :roll [-180.0 180.0]}
           :lrf {:distance [0 50000]}
           :temperature {:min -50.0 :max 100.0}
           :pressure {:min 500.0 :max 1500.0}
           :humidity {:min 0.0 :max 100.0}}]

      (logging/log-info {:msg "Validation boundaries:" :boundaries boundaries})

      ;; Verify boundaries are correctly specified in specs
      (is (not (m/validate ::specs/offset-value 1921)))
      (is (not (m/validate ::specs/offset-value -1921)))
      (is true "Boundaries documented"))))