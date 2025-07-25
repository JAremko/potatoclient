(ns potatoclient.cmd.generator-test
  "Generator-based tests for command system using Malli schemas"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.core.async :as async :refer [<! >! go timeout alt!]]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.day-camera :as day-camera]
            [potatoclient.cmd.heat-camera :as heat-camera]
            [potatoclient.proto :as proto]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging])
  (:import [cmd JonSharedCmd$Root
            JonSharedCmd$Ping JonSharedCmd$Frozen JonSharedCmd$Noop]
           [cmd.RotaryPlatform JonSharedCmdRotary$Root
            JonSharedCmdRotary$Axis JonSharedCmdRotary$Azimuth
            JonSharedCmdRotary$Elevation]
           [cmd.DayCamera JonSharedCmdDayCamera$Root]
           [cmd.HeatCamera JonSharedCmdHeatCamera$Root]
           [com.google.protobuf.util JsonFormat]))

;; ============================================================================
;; Test Fixtures
;; ============================================================================

(def test-command-channel
  "Test channel for capturing commands"
  (atom nil))

(defn command-capture-fixture
  "Replace the command channel with a test channel"
  [f]
  ;; Save original channel
  (let [original-channel cmd-core/command-channel]
    ;; Create test channel
    (reset! test-command-channel (async/chan 100))
    ;; Replace the channel
    (alter-var-root #'cmd-core/command-channel (constantly @test-command-channel))
    ;; Run test
    (f)
    ;; Restore original channel
    (alter-var-root #'cmd-core/command-channel (constantly original-channel))
    ;; Close test channel
    (async/close! @test-command-channel)))

(use-fixtures :each command-capture-fixture)

;; ============================================================================
;; Test Utilities
;; ============================================================================

(defn capture-command!
  "Capture the next command from the test channel"
  []
  (go
    (alt!
      @test-command-channel ([cmd] cmd)
      (timeout 1000) nil)))

(defn decode-command
  "Decode a captured command to protobuf"
  [{:keys [pld should-buffer]}]
  (when pld
    (JonSharedCmd$Root/parseFrom pld)))

(defn command->json
  "Convert command to JSON for debugging"
  [proto-msg]
  (-> (JsonFormat/printer)
      (.includingDefaultValueFields)
      (.print proto-msg)))

(defn validate-base-command
  "Validate common command properties"
  [cmd-proto]
  (is (= 1 (.getProtocolVersion cmd-proto)) "Protocol version should be 1")
  (is (.hasClientType cmd-proto) "Should have client type")
  (is (= "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
         (str (.getClientType cmd-proto))) "Should be local network client"))

;; ============================================================================
;; Basic Command Tests
;; ============================================================================

(deftest test-ping-command
  (testing "Ping command generation and serialization"
    (cmd-core/send-cmd-ping)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasPing proto-cmd) "Should have ping")
          (is (false? (:should-buffer captured)) "Ping should not be buffered"))))))

(deftest test-frozen-command
  (testing "Frozen command generation"
    (cmd-core/send-cmd-frozen)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasFrozen proto-cmd) "Should have frozen")
          (is (:should-buffer captured) "Frozen should be buffered"))))))

(deftest test-noop-command
  (testing "No-op command generation"
    (cmd-core/send-cmd-noop)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasNoop proto-cmd) "Should have noop")
          (is (:should-buffer captured) "Noop should be buffered"))))))

;; ============================================================================
;; Rotary Platform Command Tests
;; ============================================================================

(deftest test-rotary-axis-command
  (testing "Rotary axis command with azimuth only"
    (rotary/axis {:azimuth (rotary/azimuth 45.5)})
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasRotary proto-cmd) "Should have rotary")
          (let [rotary-root (.getRotary proto-cmd)]
            (is (.hasAxis rotary-root) "Should have axis")
            (let [axis (.getAxis rotary-root)]
              (is (.hasAzimuth axis) "Should have azimuth")
              (is (< (Math/abs (- 45.5 (.getPosition (.getAzimuth axis)))) 0.001)
                  "Azimuth position should match")))))))

  (testing "Rotary axis command with elevation only"
    (rotary/axis {:elevation (rotary/elevation -30.0)})
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasRotary proto-cmd) "Should have rotary")
          (let [rotary-root (.getRotary proto-cmd)]
            (is (.hasAxis rotary-root) "Should have axis")
            (let [axis (.getAxis rotary-root)]
              (is (.hasElevation axis) "Should have elevation")
              (is (< (Math/abs (- -30.0 (.getPosition (.getElevation axis)))) 0.001)
                  "Elevation position should match")))))))

  (testing "Rotary axis command with both azimuth and elevation"
    (rotary/axis {:azimuth (rotary/azimuth 180.0)
                  :elevation (rotary/elevation 45.0)})
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasRotary proto-cmd) "Should have rotary")
          (let [axis (.getAxis (.getRotary proto-cmd))]
            (is (.hasAzimuth axis) "Should have azimuth")
            (is (.hasElevation axis) "Should have elevation")
            (is (< (Math/abs (- 180.0 (.getPosition (.getAzimuth axis)))) 0.001)
                "Azimuth should match")
            (is (< (Math/abs (- 45.0 (.getPosition (.getElevation axis)))) 0.001)
                "Elevation should match")))))))

;; ============================================================================
;; Rotary Movement Commands
;; ============================================================================

(deftest test-rotary-movement-commands
  (testing "Various rotary movement commands"
    ;; Test each movement command
    (doseq [[cmd-fn expected-field] [[rotary/move-home :home]
                                     [rotary/stop :stop]
                                     [rotary/halt :halt]
                                     [rotary/center :center]
                                     [rotary/zero :zero]
                                     [rotary/go-to-north :goToNorth]
                                     [rotary/scan :scan]
                                     [rotary/patrol :patrol]]]
      (testing (str "Command: " expected-field)
        (cmd-fn)
        (let [captured (<! (capture-command!))]
          (is (some? captured) "Should capture command")
          (when captured
            (let [proto-cmd (decode-command captured)
                  rotary-root (.getRotary proto-cmd)
                  field-name (str "has" (clojure.string/capitalize (name expected-field)))]
              ;; Use reflection to check for the field
              (is (-> rotary-root
                      (.getClass)
                      (.getMethod field-name (into-array Class []))
                      (.invoke rotary-root (into-array Object [])))
                  (str "Should have " expected-field)))))))))

;; ============================================================================
;; Day Camera Command Tests
;; ============================================================================

(deftest test-day-camera-basic-commands
  (testing "Day camera start command"
    (day-camera/start)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasDayCamera proto-cmd) "Should have day camera")
          (is (.hasStart (.getDayCamera proto-cmd)) "Should have start")))))

  (testing "Day camera stop command"
    (day-camera/stop)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasDayCamera proto-cmd) "Should have day camera")
          (is (.hasStop (.getDayCamera proto-cmd)) "Should have stop"))))))

(deftest test-day-camera-zoom-commands
  (testing "Day camera zoom command"
    (day-camera/zoom {:mode :zoom-mode-stop :speed 0.5})
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasDayCamera proto-cmd) "Should have day camera")
          (let [day-root (.getDayCamera proto-cmd)]
            (is (.hasZoom day-root) "Should have zoom")
            (let [zoom (.getZoom day-root)]
              (is (= "JON_GUI_DATA_ZOOM_MODE_STOP" (str (.getMode zoom)))
                  "Zoom mode should be stop")
              (is (< (Math/abs (- 0.5 (.getSpeed zoom))) 0.001)
                  "Zoom speed should match"))))))))

(deftest test-day-camera-focus-commands
  (testing "Day camera focus command"
    (day-camera/focus {:mode :focus-mode-far :speed 0.8})
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasDayCamera proto-cmd) "Should have day camera")
          (let [day-root (.getDayCamera proto-cmd)]
            (is (.hasFocus day-root) "Should have focus")
            (let [focus (.getFocus day-root)]
              (is (= "JON_GUI_DATA_FOCUS_MODE_FAR" (str (.getMode focus)))
                  "Focus mode should be far")
              (is (< (Math/abs (- 0.8 (.getSpeed focus))) 0.001)
                  "Focus speed should match"))))))))

;; ============================================================================
;; Heat Camera Command Tests
;; ============================================================================

(deftest test-heat-camera-commands
  (testing "Heat camera NUC command"
    (heat-camera/nuc)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasHeatCamera proto-cmd) "Should have heat camera")
          (is (.hasNuc (.getHeatCamera proto-cmd)) "Should have NUC")))))

  (testing "Heat camera brightness command"
    (heat-camera/brightness 75)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture command")
      (when captured
        (let [proto-cmd (decode-command captured)]
          (validate-base-command proto-cmd)
          (is (.hasHeatCamera proto-cmd) "Should have heat camera")
          (let [heat-root (.getHeatCamera proto-cmd)]
            (is (.hasBrightness heat-root) "Should have brightness")
            (is (= 75 (.getValue (.getBrightness heat-root)))
                "Brightness value should match")))))))

;; ============================================================================
;; Read-Only Mode Tests
;; ============================================================================

(deftest test-read-only-mode
  (testing "Commands blocked in read-only mode"
    (cmd-core/set-read-only-mode! true)

    ;; Try sending a non-allowed command
    (day-camera/start)
    (let [captured (<! (capture-command!))]
      (is (nil? captured) "Should not capture day camera command in read-only mode"))

    ;; Ping should still work
    (cmd-core/send-cmd-ping)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture ping in read-only mode"))

    ;; Frozen should still work
    (cmd-core/send-cmd-frozen)
    (let [captured (<! (capture-command!))]
      (is (some? captured) "Should capture frozen in read-only mode"))

    ;; Reset read-only mode
    (cmd-core/set-read-only-mode! false)))

;; ============================================================================
;; Command Serialization Roundtrip Tests
;; ============================================================================

(deftest test-command-roundtrip
  (testing "Command serialization and deserialization"
    (let [test-cases [{:fn #(cmd-core/send-cmd-ping) :name "ping"}
                      {:fn #(rotary/axis {:azimuth (rotary/azimuth 123.45)}) :name "rotary-axis"}
                      {:fn #(day-camera/zoom {:mode :zoom-mode-in :speed 0.7}) :name "day-zoom"}
                      {:fn #(heat-camera/brightness 50) :name "heat-brightness"}]]

      (doseq [{:keys [fn name]} test-cases]
        (testing (str "Roundtrip for " name)
          (fn)
          (let [captured (<! (capture-command!))]
            (is (some? captured) (str "Should capture " name))
            (when captured
              (let [proto-bytes (:pld captured)
                    ;; Decode
                    decoded (JonSharedCmd$Root/parseFrom proto-bytes)
                    ;; Re-encode
                    re-encoded (.toByteArray decoded)
                    ;; Decode again
                    final-decoded (JonSharedCmd$Root/parseFrom re-encoded)]

                ;; Verify they're equal
                (is (= decoded final-decoded)
                    (str name " should survive roundtrip"))

                ;; Verify JSON representation is the same
                (is (= (command->json decoded)
                       (command->json final-decoded))
                    (str name " JSON should be identical"))))))))))

;; ============================================================================
;; Generator-Based Property Tests
;; ============================================================================

(deftest test-generated-rotary-positions
  (testing "Property: All generated rotary positions are valid"
    (dotimes [_ 20]
      (let [azimuth-pos (- (rand 360) 180)  ; -180 to 180
            elevation-pos (- (rand 180) 90)]  ; -90 to 90
        (rotary/axis {:azimuth (rotary/azimuth azimuth-pos)
                      :elevation (rotary/elevation elevation-pos)})
        (let [captured (<! (capture-command!))]
          (is (some? captured) "Should capture generated command")
          (when captured
            (let [proto-cmd (decode-command captured)
                  axis (.getAxis (.getRotary proto-cmd))]
              (is (< (Math/abs (- azimuth-pos (.getPosition (.getAzimuth axis)))) 0.001)
                  (str "Azimuth " azimuth-pos " should be preserved"))
              (is (< (Math/abs (- elevation-pos (.getPosition (.getElevation axis)))) 0.001)
                  (str "Elevation " elevation-pos " should be preserved")))))))))

;; ============================================================================
;; Performance Tests
;; ============================================================================

(deftest test-command-throughput
  (testing "Command system can handle rapid commands"
    (let [num-commands 100
          start-time (System/currentTimeMillis)]

      ;; Send many commands rapidly
      (dotimes [i num-commands]
        (case (mod i 4)
          0 (cmd-core/send-cmd-ping)
          1 (rotary/axis {:azimuth (rotary/azimuth (rand 360))})
          2 (day-camera/zoom {:mode :zoom-mode-stop :speed 0.5})
          3 (heat-camera/brightness (rand-int 100))))

      ;; Capture all commands
      (let [captured-count (atom 0)]
        (go-loop []
          (when-let [cmd (alt!
                           @test-command-channel ([c] c)
                           (timeout 100) nil)]
            (swap! captured-count inc)
            (recur)))

        ;; Wait a bit
        (Thread/sleep 200)

        (let [end-time (System/currentTimeMillis)
              duration (- end-time start-time)]
          (is (= num-commands @captured-count)
              "All commands should be captured")
          (is (< duration 1000)
              (str "Should send " num-commands " commands in under 1 second")))))))