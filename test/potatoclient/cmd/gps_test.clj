(ns potatoclient.cmd.gps-test
  "Tests for GPS command functions."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [potatoclient.cmd.gps :as gps]
    [potatoclient.cmd.validation :as validation]
    [malli.core :as m]
    [malli.instrument :as mi]
    [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

;; ============================================================================

;; ============================================================================
;; GPS Control Tests
;; ============================================================================

(deftest test-gps-control
  (testing "start creates valid command"
    (let [result (gps/start)]
      (is (m/validate :cmd/root result))
      (is (match? {:gps {:start {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))

  (testing "stop creates valid command"
    (let [result (gps/stop)]
      (is (m/validate :cmd/root result))
      (is (match? {:gps {:stop {}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Manual Position Control Tests
;; ============================================================================

(deftest test-manual-position-control
  (testing "set-manual-position creates valid command with typical coordinates"
    (let [result (gps/set-manual-position 40.7128 -74.0060 10.5)]
      (is (m/validate :cmd/root result))
      (is (match? {:gps {:set_manual_position {:latitude 40.7128
                                               :longitude -74.0060
                                               :altitude 10.5}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))

  (testing "set-manual-position works with extreme values"
    ;; North Pole
    (let [result (gps/set-manual-position 90.0 0.0 0.0)]
      (is (m/validate :cmd/root result))
      (is (match? {:gps {:set_manual_position {:latitude 90.0}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))

    ;; South Pole (using 179.9999 instead of 180.0 since longitude must be < 180)
    (let [result (gps/set-manual-position -90.0 179.9999 1000.0)]
      (is (m/validate :cmd/root result))
      (is (= -90.0 (get-in result [:gps :set_manual_position :latitude])))
      (is (= 179.9999 (get-in result [:gps :set_manual_position :longitude])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))

    ;; Dead Sea (lowest point on Earth)
    (let [result (gps/set-manual-position 31.5590 35.4732 -430.0)]
      (is (m/validate :cmd/root result))
      (is (= -430.0 (get-in result [:gps :set_manual_position :altitude])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))

    ;; Edge of space (Kármán line approximation)
    (let [result (gps/set-manual-position 0.0 0.0 100000.0)]
      (is (m/validate :cmd/root result))
      (is (= 100000.0 (get-in result [:gps :set_manual_position :altitude])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))

  (testing "set-use-manual-position creates valid command"
    (let [result (gps/set-use-manual-position true)]
      (is (m/validate :cmd/root result))
      (is (= true (get-in result [:gps :set_use_manual_position :flag])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))

    (let [result (gps/set-use-manual-position false)]
      (is (m/validate :cmd/root result))
      (is (= false (get-in result [:gps :set_use_manual_position :flag])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(deftest test-meteo
  (testing "get-meteo creates valid command"
    (let [result (gps/get-meteo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:gps :get_meteo])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result)
            (str "Should pass roundtrip validation"
                 (when-not (:valid? roundtrip-result)
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

