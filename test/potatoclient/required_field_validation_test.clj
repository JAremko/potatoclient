(ns potatoclient.required-field-validation-test
  "Tests for required field validation in commands and state.
   Ensures that protobuf's required constraints are properly enforced."
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [potatoclient.state.schemas :as state-schemas]
            [potatoclient.proto :as proto]
            [potatoclient.cmd.core :as cmd-core])
  (:import [cmd JonSharedCmd$Root JonSharedCmd$Root$PayloadCase]
           [cmd.Compass JonSharedCmdCompass$Root JonSharedCmdCompass$Root$CmdCase]
           [cmd.RotaryPlatform JonSharedCmdRotary$Root JonSharedCmdRotary$Root$CmdCase]
           [ser JonSharedDataTypes$JonGuiDataClientType]))

;; ============================================================================
;; Command Oneof Required Tests
;; ============================================================================

(deftest command-payload-required-tests
  (testing "Root command must have a payload (oneof required)"
    ;; Valid command with payload
    (let [root-msg (cmd-core/create-root-message)
          ping-msg (cmd.JonSharedCmd$Ping/newBuilder)]
      (.setPing root-msg ping-msg)
      (is (some? (.build root-msg)) "Command with payload should build"))

    ;; Test that protobuf doesn't enforce required oneof at build time
    (let [root-msg (cmd-core/create-root-message)
          built-msg (.build root-msg)]
      (is (some? built-msg)
          "Protobuf allows building without required oneof")
      (is (= (.getPayloadCase built-msg)
             JonSharedCmd$Root$PayloadCase/PAYLOAD_NOT_SET)
          "Payload case should be NOT_SET"))

    ;; This shows why runtime validation is critical!
    (testing "Runtime validation would catch missing payload"
      ;; In a real system, we'd have validation like:
      (let [root-msg (cmd-core/create-root-message)
            built-msg (.build root-msg)]
        (is (= false (not= (.getPayloadCase built-msg)
                           JonSharedCmd$Root$PayloadCase/PAYLOAD_NOT_SET))
            "Runtime check can detect missing required oneof")))))

(deftest subsystem-command-oneof-tests
  (testing "Compass command - protobuf allows empty oneofs"
    ;; Valid compass command
    (let [compass-root (JonSharedCmdCompass$Root/newBuilder)]
      (.setStart compass-root (cmd.Compass.JonSharedCmdCompass$Start/newBuilder))
      (is (some? (.build compass-root)) "Compass command with payload should build"))

    ;; Protobuf allows building without required oneof
    (let [compass-root (JonSharedCmdCompass$Root/newBuilder)
          built-msg (.build compass-root)]
      (is (some? built-msg)
          "Protobuf allows building without required oneof")
      (is (= (.getCmdCase built-msg)
             JonSharedCmdCompass$Root$CmdCase/CMD_NOT_SET)
          "Command case should be NOT_SET")))

  (testing "Rotary command - protobuf allows empty oneofs"
    ;; Valid rotary command
    (let [rotary-root (JonSharedCmdRotary$Root/newBuilder)]
      (.setStop rotary-root (cmd.RotaryPlatform.JonSharedCmdRotary$Stop/newBuilder))
      (is (some? (.build rotary-root)) "Rotary command with payload should build"))

    ;; Protobuf allows building without required oneof
    (let [rotary-root (JonSharedCmdRotary$Root/newBuilder)
          built-msg (.build rotary-root)]
      (is (some? built-msg)
          "Protobuf allows building without required oneof")
      (is (= (.getCmdCase built-msg)
             JonSharedCmdRotary$Root$CmdCase/CMD_NOT_SET)
          "Command case should be NOT_SET"))))

;; ============================================================================
;; State Required Field Tests
;; ============================================================================

(deftest state-subsystem-required-fields-tests
  (testing "System subsystem requires all fields"
    (let [valid-system {:cpu-temperature 20.0
                        :gpu-temperature 30.0
                        :gpu-load 50.0
                        :cpu-load 40.0
                        :power-consumption 100.0
                        :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                        :cur-video-rec-dir-year 2024
                        :cur-video-rec-dir-month 7
                        :cur-video-rec-dir-day 24
                        :cur-video-rec-dir-hour 12
                        :cur-video-rec-dir-minute 30
                        :cur-video-rec-dir-second 0
                        :rec-enabled false
                        :important-rec-enabled false
                        :low-disk-space false
                        :no-disk-space false
                        :disk-space 80
                        :tracking false
                        :vampire-mode false
                        :stabilization-mode false
                        :geodesic-mode false
                        :cv-dumping false}]

      ;; Valid system should pass
      (is (m/validate state-schemas/system-schema valid-system))

      ;; Missing any required field should fail
      (doseq [field (keys valid-system)]
        (is (not (m/validate state-schemas/system-schema
                             (dissoc valid-system field)))
            (str field " should be required in system schema")))))

  (testing "GPS subsystem requires all fields"
    (let [valid-gps {:longitude 0.0
                     :latitude 0.0
                     :altitude 0.0
                     :manual-longitude 0.0
                     :manual-latitude 0.0
                     :manual-altitude 0.0
                     :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_NONE"
                     :use-manual false}]

      ;; Valid GPS should pass
      (is (m/validate state-schemas/gps-schema valid-gps))

      ;; Missing any required field should fail
      (doseq [field (keys valid-gps)]
        (is (not (m/validate state-schemas/gps-schema
                             (dissoc valid-gps field)))
            (str field " should be required in GPS schema")))))

  (testing "Compass subsystem requires all fields"
    (let [valid-compass {:azimuth 0.0
                         :elevation 0.0
                         :bank 0.0
                         :offset-azimuth 0.0
                         :offset-elevation 0.0
                         :magnetic-declination 0.0
                         :calibrating false}]

      ;; Valid compass should pass
      (is (m/validate state-schemas/compass-schema valid-compass))

      ;; Missing any required field should fail
      (doseq [field (keys valid-compass)]
        (is (not (m/validate state-schemas/compass-schema
                             (dissoc valid-compass field)))
            (str field " should be required in compass schema"))))))

(deftest optional-field-tests
  (testing "LRF target field is correctly optional"
    (let [valid-lrf-without-target {:is-scanning false
                                    :is-measuring false
                                    :measure-id 0
                                    :pointer-mode "JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
                                    :fog-mode-enabled false
                                    :is-refining false}

          valid-lrf-with-target (assoc valid-lrf-without-target
                                       :target {:timestamp 123456
                                                :target-longitude 0.0
                                                :target-latitude 0.0
                                                :target-altitude 0.0
                                                :observer-longitude 0.0
                                                :observer-latitude 0.0
                                                :observer-altitude 0.0
                                                :observer-azimuth 0.0
                                                :observer-elevation 0.0
                                                :observer-bank 0.0
                                                :distance-2d 0.0
                                                :distance-3b 0.0
                                                :observer-fix-type "JON_GUI_DATA_GPS_FIX_TYPE_NONE"
                                                :session-id 0
                                                :target-id 0
                                                :target-color {:red 255 :green 0 :blue 0}
                                                :type 1
                                                :uuid-part1 0
                                                :uuid-part2 0
                                                :uuid-part3 0
                                                :uuid-part4 0})]

      ;; Both with and without target should be valid
      (is (m/validate state-schemas/lrf-schema valid-lrf-without-target)
          "LRF without target should be valid")
      (is (m/validate state-schemas/lrf-schema valid-lrf-with-target)
          "LRF with target should be valid"))))

;; ============================================================================
;; Nested Required Field Tests
;; ============================================================================

(deftest nested-required-field-tests
  (testing "Scan node requires all fields"
    (let [valid-scan-node {:index 0
                           :day-zoom-table-value 0
                           :heat-zoom-table-value 0
                           :azimuth 0.0
                           :elevation 0.0
                           :linger 0.0
                           :speed 0.5}]

      ;; Valid scan node should pass
      (is (m/validate state-schemas/scan-node-schema valid-scan-node))

      ;; Missing any required field should fail
      (doseq [field (keys valid-scan-node)]
        (is (not (m/validate state-schemas/scan-node-schema
                             (dissoc valid-scan-node field)))
            (str field " should be required in scan node schema")))))

  (testing "RGB color requires all components"
    (let [valid-color {:red 255 :green 128 :blue 0}]

      ;; Valid color should pass
      (is (m/validate state-schemas/rgb-color-schema valid-color))

      ;; Missing any component should fail
      (doseq [component [:red :green :blue]]
        (is (not (m/validate state-schemas/rgb-color-schema
                             (dissoc valid-color component)))
            (str component " should be required in RGB color schema"))))))