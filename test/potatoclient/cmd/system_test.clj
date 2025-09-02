(ns potatoclient.cmd.system-test
  "Roundtrip tests for system commands.
   Validates that commands are constructed correctly and survive serialization/deserialization."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [potatoclient.cmd.system :as sys]
    [potatoclient.cmd.core :as core]
    [potatoclient.cmd.validation :as validation]
    [potatoclient.malli.registry :as registry]
    [potatoclient.test-harness :as harness]
    [malli.core :as m]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Test Helpers
;; ============================================================================

(defn validate-cmd
  "Validate a command against the cmd/root spec."
  [cmd]
  (m/validate (m/schema :cmd/root) cmd))

;; ============================================================================
;; System Command Tests
;; ============================================================================

(deftest reboot-test
  (testing "reboot command construction and roundtrip"
    (let [cmd (sys/reboot)
          expected {:system {:reboot {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest power-off-test
  (testing "power-off command construction and roundtrip"
    (let [cmd (sys/power-off)
          expected {:system {:power_off {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest reset-configs-test
  (testing "reset-configs command construction and roundtrip"
    (let [cmd (sys/reset-configs)
          expected {:system {:reset_configs {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest start-all-test
  (testing "start-all command construction and roundtrip"
    (let [cmd (sys/start-all)
          expected {:system {:start_all {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest stop-all-test
  (testing "stop-all command construction and roundtrip"
    (let [cmd (sys/stop-all)
          expected {:system {:stop_all {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest mark-rec-important-test
  (testing "mark-rec-important command construction and roundtrip"
    (let [cmd (sys/mark-rec-important)
          expected {:system {:mark_rec_important {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest unmark-rec-important-test
  (testing "unmark-rec-important command construction and roundtrip"
    (let [cmd (sys/unmark-rec-important)
          expected {:system {:unmark_rec_important {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest set-localization-test
  (testing "set-localization command construction and roundtrip"
    (let [cmd (sys/set-localization :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN)
          expected {:system {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))

      (testing "with different localizations"
        (doseq [loc [:JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
                     :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
                     :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS]]
          (let [cmd (sys/set-localization loc)
                roundtrip-result (validation/validate-roundtrip-with-report cmd)]
            (is (match? {:system {:localization {:loc loc}}} cmd)
                (str "Should create correct command for " loc))
            (is (:valid? roundtrip-result)
                (str "Should survive roundtrip for " loc
                     (when-not (:valid? roundtrip-result)
                       (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))))

(deftest enter-transport-test
  (testing "enter-transport command construction and roundtrip"
    (let [cmd (sys/enter-transport)
          expected {:system {:enter_transport {}}}]
      (is (match? expected cmd) "Command structure should match expected")
      (is (validate-cmd cmd)
          "Command should be valid against spec")

      (testing "roundtrip serialization"
        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))

(deftest geodesic-mode-test
  (testing "geodesic mode enable/disable commands"
    (testing "enable geodesic mode"
      (let [cmd (sys/enable-geodesic-mode)
            expected {:system {:geodesic_mode_enable {}}}]
        (is (match? expected cmd) "Enable command structure should match expected")
        (is (validate-cmd cmd)
            "Enable command should be valid against spec")

        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Enable command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))

    (testing "disable geodesic mode"
      (let [cmd (sys/disable-geodesic-mode)
            expected {:system {:geodesic_mode_disable {}}}]
        (is (match? expected cmd) "Disable command structure should match expected")
        (is (validate-cmd cmd)
            "Disable command should be valid against spec")

        (let [roundtrip-result (validation/validate-roundtrip-with-report cmd)]
          (is (:valid? roundtrip-result)
              (str "Disable command should survive serialization/deserialization"
                   (when-not (:valid? roundtrip-result)
                     (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))))