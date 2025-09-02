(ns potatoclient.cmd.compass-test
  "Tests for compass command functions."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [potatoclient.cmd.compass :as compass]
    [potatoclient.cmd.validation :as validation]
    [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

;; ============================================================================
;; Control Commands Tests
;; ============================================================================

(deftest compass-control-commands-test
  (testing "Compass control commands"

    (testing "start command"
      (let [cmd (compass/start)]
        (is (match? {:compass {:start {}}
                     :protocol_version 1
                     :session_id 0}
                    cmd)
            "Should have correct structure")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "stop command"
      (let [cmd (compass/stop)]
        (is (match? {:compass {:stop {}}}
                    cmd)
            "Should have empty stop payload")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))))

;; ============================================================================
;; Data Request Commands Tests
;; ============================================================================

(deftest compass-data-request-test
  (testing "Compass data request commands"

    (testing "get-meteo command"
      (let [cmd (compass/get-meteo)]
        (is (match? {:compass {:get_meteo {}}}
                    cmd)
            "Should have empty get_meteo payload")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))))

;; ============================================================================
;; Configuration Commands Tests
;; ============================================================================

(deftest compass-configuration-commands-test
  (testing "Compass configuration commands"

    (testing "set-magnetic-declination command"
      (let [test-value 15.5
            cmd (compass/set-magnetic-declination test-value)]
        (is (= test-value (get-in cmd [:compass :set_magnetic_declination :value]))
            "Should set magnetic declination value")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "set-offset-angle-azimuth command"
      (let [test-value 32.0
            cmd (compass/set-offset-angle-azimuth test-value)]
        (is (= test-value (get-in cmd [:compass :set_offset_angle_azimuth :value]))
            "Should set azimuth offset value")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "set-offset-angle-elevation command"
      (let [test-value -50.0
            cmd (compass/set-offset-angle-elevation test-value)]
        (is (= test-value (get-in cmd [:compass :set_offset_angle_elevation :value]))
            "Should set elevation offset value")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "set-use-rotary-position command"
      (testing "with true flag"
        (let [cmd (compass/set-use-rotary-position true)]
          (is (= true (get-in cmd [:compass :set_use_rotary_position :flag]))
              "Should set flag to true")

          (testing "roundtrip validation"
            (let [result (validation/validate-roundtrip-with-report cmd)]
              (is (:valid? result)
                  (str "Should pass roundtrip validation"
                       (when-not (:valid? result)
                         (str "\n" (:pretty-diff result)))))))))

      (testing "with false flag"
        (let [cmd (compass/set-use-rotary-position false)]
          (is (= false (get-in cmd [:compass :set_use_rotary_position :flag]))
              "Should set flag to false")

          (testing "roundtrip validation"
            (let [result (validation/validate-roundtrip-with-report cmd)]
              (is (:valid? result)
                  (str "Should pass roundtrip validation"
                       (when-not (:valid? result)
                         (str "\n" (:pretty-diff result))))))))))))

;; ============================================================================
;; Calibration Commands Tests
;; ============================================================================

(deftest compass-calibration-commands-test
  (testing "Compass calibration commands"

    (testing "calibrate-long-start command"
      (let [cmd (compass/calibrate-long-start)]
        (is (match? {:compass {:start_calibrate_long {}}}
                    cmd)
            "Should have empty start_calibrate_long payload")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "calibrate-short-start command"
      (let [cmd (compass/calibrate-short-start)]
        (is (match? {:compass {:start_calibrate_short {}}}
                    cmd)
            "Should have empty start_calibrate_short payload")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "calibrate-next command"
      (let [cmd (compass/calibrate-next)]
        (is (match? {:compass {:calibrate_next {}}}
                    cmd)
            "Should have empty calibrate_next payload")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))

    (testing "calibrate-cancel command (with proto typo)"
      (let [cmd (compass/calibrate-cancel)]
        (is (match? {:compass {:calibrate_cencel {}}}
                    cmd)
            "Should have empty calibrate_cencel payload (with typo)")

        (testing "roundtrip validation"
          (let [result (validation/validate-roundtrip-with-report cmd)]
            (is (:valid? result)
                (str "Should pass roundtrip validation"
                     (when-not (:valid? result)
                       (str "\n" (:pretty-diff result)))))))))))

;; ============================================================================
;; Edge Cases and Validation Tests
;; ============================================================================

(deftest compass-edge-cases-test
  (testing "Edge cases for compass commands"

    (testing "Numeric values at boundaries"
      (testing "Zero values"
        (let [cmd (compass/set-magnetic-declination 0.0)]
          (is (= 0.0 (get-in cmd [:compass :set_magnetic_declination :value]))
              "Should accept zero")
          (is (:valid? (validation/validate-roundtrip-with-report cmd))
              "Zero should validate")))

      (testing "Negative values"
        (let [cmd (compass/set-offset-angle-elevation -18.0)]
          (is (= -18.0 (get-in cmd [:compass :set_offset_angle_elevation :value]))
              "Should accept negative values")
          (is (:valid? (validation/validate-roundtrip-with-report cmd))
              "Negative values should validate")))

      (testing "Large values"
        (let [cmd (compass/set-offset-angle-azimuth 64.0)]
          (is (= 64.0 (get-in cmd [:compass :set_offset_angle_azimuth :value]))
              "Should accept large values")
          (is (:valid? (validation/validate-roundtrip-with-report cmd))
              "Large values should validate"))))))