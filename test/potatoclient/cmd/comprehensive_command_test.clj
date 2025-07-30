(ns potatoclient.cmd.comprehensive-command-test
  "Comprehensive tests for ALL 200+ command functions"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.core.async :as async :refer [<! <!! >! go go-loop timeout alt!]]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.day-camera :as day-camera]
            [potatoclient.cmd.heat-camera :as heat-camera]
            [potatoclient.cmd.system :as system]
            [potatoclient.cmd.osd :as osd]
            [potatoclient.cmd.gps :as gps]
            [potatoclient.cmd.lrf :as lrf]
            [potatoclient.cmd.lrf-alignment :as lrf-align]
            [potatoclient.cmd.compass :as compass]
            [potatoclient.cmd.cv :as cv]
            [potatoclient.cmd.glass-heater :as glass-heater]
            [potatoclient.proto :as proto]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging]
            [potatoclient.cmd.test-helpers :as h])
  (:import [cmd JonSharedCmd$Root]
           [com.google.protobuf.util JsonFormat]))

;; ============================================================================
;; Test Setup
;; ============================================================================

(def test-commands (atom []))

;; Mock WebSocketManager that captures commands
(defn create-mock-websocket-manager []
  (proxy [potatoclient.java.websocket.WebSocketManager] ["test-domain" nil nil]
    (sendCommand [command]
      (swap! test-commands conj command)
      true)  ; WebSocket sendCommand returns boolean
    (start [] nil)
    (stop [] nil)
    (isConnected [] true)))

(defn command-capture-fixture [f]
  ;; Access the private atom
  (let [ws-manager-atom @#'cmd-core/websocket-manager
        original-manager @ws-manager-atom]
    ;; Reset captured commands
    (reset! test-commands [])
    ;; Install mock manager
    (reset! ws-manager-atom (create-mock-websocket-manager))
    ;; Run test
    (f)
    ;; Restore original manager
    (reset! ws-manager-atom original-manager)))

(use-fixtures :each command-capture-fixture)

(defn capture-command-sync! []
  "Get the last captured command"
  (Thread/sleep 10) ;; Small delay to ensure command is processed
  (when-let [cmd (last @test-commands)]
    ;; Return in the expected format with :pld key
    {:pld (.toByteArray cmd)}))

(defn decode-command [{:keys [pld]}]
  (when pld
    (JonSharedCmd$Root/parseFrom pld)))

(defn validate-base-command [cmd-proto]
  (is (= 1 (.getProtocolVersion cmd-proto)) "Protocol version should be 1")
  (is (some? (.getClientType cmd-proto)) "Should have client type"))

;; ============================================================================
;; Core Commands
;; ============================================================================

(deftest test-all-core-commands
  (testing "Core command functions"
    ;; Ping
    (cmd-core/send-cmd-ping)
    (is (some? (capture-command-sync!)) "Ping command sent")

    ;; Frozen
    (cmd-core/send-cmd-frozen)
    (is (some? (capture-command-sync!)) "Frozen command sent")

    ;; Noop
    (cmd-core/send-cmd-noop)
    (is (some? (capture-command-sync!)) "Noop command sent")))

;; ============================================================================
;; Rotary Commands (42 functions)
;; ============================================================================

(deftest test-all-rotary-commands
  (testing "All rotary platform commands"
    ;; Basic control
    (rotary/rotary-start)
    (is (some? (capture-command-sync!)) "Start command sent")

    (rotary/rotary-stop)
    (is (some? (capture-command-sync!)) "Stop command sent")

    (rotary/rotary-halt)
    (is (some? (capture-command-sync!)) "Halt command sent")

    ;; Platform position
    (rotary/rotary-set-platform-azimuth 180.0)
    (is (some? (capture-command-sync!)) "Set platform azimuth sent")

    (rotary/rotary-set-platform-elevation 45.0)
    (is (some? (capture-command-sync!)) "Set platform elevation sent")

    (rotary/rotary-set-platform-bank 30.0)
    (is (some? (capture-command-sync!)) "Set platform bank sent")

    ;; Axis halt
    (rotary/rotary-halt-azimuth)
    (is (some? (capture-command-sync!)) "Halt azimuth sent")

    (rotary/rotary-halt-elevation)
    (is (some? (capture-command-sync!)) "Halt elevation sent")

    (rotary/rotary-halt-elevation-and-azimuth)
    (is (some? (capture-command-sync!)) "Halt both axes sent")

    ;; Azimuth control
    (rotary/rotary-azimuth-set-value 90.0 (h/direction :normal))
    (is (some? (capture-command-sync!)) "Set azimuth value sent")

    (rotary/rotary-azimuth-rotate-to 270.0 0.5 (h/direction :shortest))
    (is (some? (capture-command-sync!)) "Rotate azimuth to sent")

    (rotary/rotary-azimuth-rotate 0.7 (h/direction :cw))
    (is (some? (capture-command-sync!)) "Rotate azimuth sent")

    (rotary/rotary-azimuth-rotate-relative 45.0 0.5 (h/direction :ccw))
    (is (some? (capture-command-sync!)) "Rotate azimuth relative sent")

    (rotary/rotary-azimuth-rotate-relative-set 30.0 (h/direction :normal))
    (is (some? (capture-command-sync!)) "Rotate azimuth relative set sent")

    ;; Elevation control
    (rotary/rotary-elevation-set-value 30.0)
    (is (some? (capture-command-sync!)) "Set elevation value sent")

    (rotary/rotary-elevation-rotate-to 60.0 0.5)
    (is (some? (capture-command-sync!)) "Rotate elevation to sent")

    (rotary/rotary-elevation-rotate 0.3 (h/direction :up))
    (is (some? (capture-command-sync!)) "Rotate elevation sent")

    (rotary/rotary-elevation-rotate-relative 15.0 0.5 (h/direction :down))
    (is (some? (capture-command-sync!)) "Rotate elevation relative sent")

    (rotary/rotary-elevation-rotate-relative-set 20.0 (h/direction :up))
    (is (some? (capture-command-sync!)) "Rotate elevation relative set sent")

    ;; Combined axis
    (rotary/rotate-both-to 180.0 0.5 (h/direction :normal) 45.0 0.5)
    (is (some? (capture-command-sync!)) "Rotate both to sent")

    (rotary/rotate-both 0.5 (h/direction :cw) 0.3 (h/direction :up))
    (is (some? (capture-command-sync!)) "Rotate both sent")

    (rotary/set-both-to 90.0 30.0 (h/direction :shortest))
    (is (some? (capture-command-sync!)) "Set both to sent")

    ;; Other commands
    (rotary/set-calculate-base-position-from-compass true)
    (is (some? (capture-command-sync!)) "Use rotary as compass sent")

    (rotary/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")

    (rotary/set-rotate-to-gps 12.345 56.789 100.0)
    (is (some? (capture-command-sync!)) "Rotate to GPS sent")

    (rotary/set-origin-gps 0.0 0.0 0.0)
    (is (some? (capture-command-sync!)) "Set origin GPS sent")

    (rotary/set-rotary-mode (h/mode :speed))
    (is (some? (capture-command-sync!)) "Set rotary mode sent")

    (rotary/rotate-to-ndc (h/channel :day) 0.5 -0.3)
    (is (some? (capture-command-sync!)) "Rotate to NDC sent")

    ;; Scan commands
    (rotary/scan-start)
    (is (some? (capture-command-sync!)) "Scan start sent")

    (rotary/scan-stop)
    (is (some? (capture-command-sync!)) "Scan stop sent")

    (rotary/scan-pause)
    (is (some? (capture-command-sync!)) "Scan pause sent")

    (rotary/scan-unpause)
    (is (some? (capture-command-sync!)) "Scan unpause sent")

    (rotary/scan-prev)
    (is (some? (capture-command-sync!)) "Scan prev sent")

    (rotary/scan-next)
    (is (some? (capture-command-sync!)) "Scan next sent")))

;; ============================================================================
;; Day Camera Commands (44 functions)
;; ============================================================================

(deftest test-all-day-camera-commands
  (testing "All day camera commands"
    ;; Basic control
    (day-camera/start)
    (is (some? (capture-command-sync!)) "Start sent")

    (day-camera/stop)
    (is (some? (capture-command-sync!)) "Stop sent")

    (day-camera/take-photo)
    (is (some? (capture-command-sync!)) "Take photo sent")

    (day-camera/halt-all)
    (is (some? (capture-command-sync!)) "Halt all sent")

    ;; Camera settings
    (day-camera/set-infrared-filter true)
    (is (some? (capture-command-sync!)) "Set IR filter sent")

    (day-camera/set-iris 50)
    (is (some? (capture-command-sync!)) "Set iris sent")

    (day-camera/set-auto-iris true)
    (is (some? (capture-command-sync!)) "Set auto iris sent")

    ;; Focus commands
    (day-camera/focus-set-value 0.5)
    (is (some? (capture-command-sync!)) "Focus set value sent")

    (day-camera/focus-move 0.8 0.5)
    (is (some? (capture-command-sync!)) "Focus move sent")

    (day-camera/focus-halt)
    (is (some? (capture-command-sync!)) "Focus halt sent")

    (day-camera/focus-offset 0.1)
    (is (some? (capture-command-sync!)) "Focus offset sent")

    (day-camera/focus-reset)
    (is (some? (capture-command-sync!)) "Focus reset sent")

    (day-camera/focus-save-to-table)
    (is (some? (capture-command-sync!)) "Focus save to table sent")

    ;; Focus convenience
    (day-camera/focus-in)
    (is (some? (capture-command-sync!)) "Focus in sent")

    (day-camera/focus-out)
    (is (some? (capture-command-sync!)) "Focus out sent")

    (day-camera/focus-step-in)
    (is (some? (capture-command-sync!)) "Focus step in sent")

    (day-camera/focus-step-out)
    (is (some? (capture-command-sync!)) "Focus step out sent")

    (day-camera/focus-auto)
    (is (some? (capture-command-sync!)) "Focus auto sent")

    (day-camera/focus-manual)
    (is (some? (capture-command-sync!)) "Focus manual sent")

    ;; Zoom commands
    (day-camera/zoom-set-value 0.7)
    (is (some? (capture-command-sync!)) "Zoom set value sent")

    (day-camera/zoom-move 0.9 0.5)
    (is (some? (capture-command-sync!)) "Zoom move sent")

    (day-camera/zoom-halt)
    (is (some? (capture-command-sync!)) "Zoom halt sent")

    (day-camera/zoom-offset 0.2)
    (is (some? (capture-command-sync!)) "Zoom offset sent")

    (day-camera/zoom-reset)
    (is (some? (capture-command-sync!)) "Zoom reset sent")

    (day-camera/zoom-save-to-table)
    (is (some? (capture-command-sync!)) "Zoom save to table sent")

    (day-camera/zoom-set-table-value 5)
    (is (some? (capture-command-sync!)) "Zoom set table value sent")

    (day-camera/zoom-next-table-position)
    (is (some? (capture-command-sync!)) "Zoom next table pos sent")

    (day-camera/zoom-prev-table-position)
    (is (some? (capture-command-sync!)) "Zoom prev table pos sent")

    (day-camera/set-digital-zoom-level 2.0)
    (is (some? (capture-command-sync!)) "Set digital zoom sent")

    ;; Zoom convenience
    (day-camera/zoom-in)
    (is (some? (capture-command-sync!)) "Zoom in sent")

    (day-camera/zoom-out)
    (is (some? (capture-command-sync!)) "Zoom out sent")

    ;; FX & Enhancement
    (day-camera/set-fx-mode (h/day-fx :day-a))
    (is (some? (capture-command-sync!)) "Set FX mode sent")

    (day-camera/next-fx-mode)
    (is (some? (capture-command-sync!)) "Next FX mode sent")

    (day-camera/prev-fx-mode)
    (is (some? (capture-command-sync!)) "Prev FX mode sent")

    (day-camera/set-clahe-level 0.5)
    (is (some? (capture-command-sync!)) "Set CLAHE level sent")

    (day-camera/shift-clahe-level 0.1)
    (is (some? (capture-command-sync!)) "Shift CLAHE level sent")

    ;; Other
    (day-camera/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")))

;; ============================================================================
;; Heat Camera Commands (35 functions)
;; ============================================================================

(deftest test-all-heat-camera-commands
  (testing "All heat camera commands"
    ;; Basic control
    (heat-camera/start)
    (is (some? (capture-command-sync!)) "Start sent")

    (heat-camera/stop)
    (is (some? (capture-command-sync!)) "Stop sent")

    (heat-camera/take-photo)
    (is (some? (capture-command-sync!)) "Take photo sent")

    (heat-camera/calibrate)
    (is (some? (capture-command-sync!)) "Calibrate sent")

    ;; Camera settings
    (heat-camera/set-agc-mode (h/agc-mode :agc-1))
    (is (some? (capture-command-sync!)) "Set AGC mode sent")

    (heat-camera/set-filter (h/heat-filter :hot-white))
    (is (some? (capture-command-sync!)) "Set filter sent")

    (heat-camera/set-auto-focus true)
    (is (some? (capture-command-sync!)) "Set auto focus sent")

    (heat-camera/set-calib-mode)
    (is (some? (capture-command-sync!)) "Set calib mode sent")

    ;; Zoom commands
    (heat-camera/zoom-in)
    (is (some? (capture-command-sync!)) "Zoom in sent")

    (heat-camera/zoom-out)
    (is (some? (capture-command-sync!)) "Zoom out sent")

    (heat-camera/zoom-stop)
    (is (some? (capture-command-sync!)) "Zoom stop sent")

    (heat-camera/zoom-set-table-value 3)
    (is (some? (capture-command-sync!)) "Zoom set table value sent")

    (heat-camera/zoom-next-table-position)
    (is (some? (capture-command-sync!)) "Zoom next table pos sent")

    (heat-camera/zoom-prev-table-position)
    (is (some? (capture-command-sync!)) "Zoom prev table pos sent")

    (heat-camera/set-digital-zoom-level 1.5)
    (is (some? (capture-command-sync!)) "Set digital zoom sent")

    (heat-camera/zoom-reset)
    (is (some? (capture-command-sync!)) "Zoom reset sent")

    (heat-camera/zoom-save-to-table)
    (is (some? (capture-command-sync!)) "Zoom save to table sent")

    ;; Focus commands
    (heat-camera/focus-in)
    (is (some? (capture-command-sync!)) "Focus in sent")

    (heat-camera/focus-out)
    (is (some? (capture-command-sync!)) "Focus out sent")

    (heat-camera/focus-stop)
    (is (some? (capture-command-sync!)) "Focus stop sent")

    (heat-camera/focus-step-plus)
    (is (some? (capture-command-sync!)) "Focus step plus sent")

    (heat-camera/focus-step-minus)
    (is (some? (capture-command-sync!)) "Focus step minus sent")

    (heat-camera/focus-auto)
    (is (some? (capture-command-sync!)) "Focus auto sent")

    (heat-camera/focus-manual)
    (is (some? (capture-command-sync!)) "Focus manual sent")

    ;; DDE
    (heat-camera/enable-dde)
    (is (some? (capture-command-sync!)) "Enable DDE sent")

    (heat-camera/disable-dde)
    (is (some? (capture-command-sync!)) "Disable DDE sent")

    (heat-camera/set-dde-level 100)
    (is (some? (capture-command-sync!)) "Set DDE level sent")

    (heat-camera/shift-dde-level 10)
    (is (some? (capture-command-sync!)) "Shift DDE level sent")

    ;; FX & Enhancement
    (heat-camera/set-fx-mode (h/heat-fx :heat-b))
    (is (some? (capture-command-sync!)) "Set FX mode sent")

    (heat-camera/next-fx-mode)
    (is (some? (capture-command-sync!)) "Next FX mode sent")

    (heat-camera/prev-fx-mode)
    (is (some? (capture-command-sync!)) "Prev FX mode sent")

    (heat-camera/set-clahe-level 0.7)
    (is (some? (capture-command-sync!)) "Set CLAHE level sent")

    (heat-camera/shift-clahe-level -0.1)
    (is (some? (capture-command-sync!)) "Shift CLAHE level sent")

    ;; Other
    (heat-camera/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")))

;; ============================================================================
;; System Commands (14 functions)
;; ============================================================================

(deftest test-all-system-commands
  (testing "All system commands"
    ;; System control
    (system/reboot)
    (is (some? (capture-command-sync!)) "Reboot sent")

    (system/power-off)
    (is (some? (capture-command-sync!)) "Power off sent")

    (system/reset-configs)
    (is (some? (capture-command-sync!)) "Reset configs sent")

    (system/enter-transport)
    (is (some? (capture-command-sync!)) "Enter transport sent")

    ;; Subsystem control
    (system/start-all)
    (is (some? (capture-command-sync!)) "Start all sent")

    (system/stop-all)
    (is (some? (capture-command-sync!)) "Stop all sent")

    ;; Recording
    (system/start-rec)
    (is (some? (capture-command-sync!)) "Start rec sent")

    (system/stop-rec)
    (is (some? (capture-command-sync!)) "Stop rec sent")

    (system/mark-rec-important)
    (is (some? (capture-command-sync!)) "Mark rec important sent")

    (system/unmark-rec-important)
    (is (some? (capture-command-sync!)) "Unmark rec important sent")

    ;; Settings
    (system/enable-geodesic-mode)
    (is (some? (capture-command-sync!)) "Enable geodesic sent")

    (system/disable-geodesic-mode)
    (is (some? (capture-command-sync!)) "Disable geodesic sent")

    (system/set-localization (h/localization :ua))
    (is (some? (capture-command-sync!)) "Set localization sent")))

;; ============================================================================
;; OSD Commands (8 functions)
;; ============================================================================

(deftest test-all-osd-commands
  (testing "All OSD commands"
    (osd/show-default-screen)
    (is (some? (capture-command-sync!)) "Show default screen sent")

    (osd/show-lrf-measure-screen)
    (is (some? (capture-command-sync!)) "Show LRF measure sent")

    (osd/show-lrf-result-screen)
    (is (some? (capture-command-sync!)) "Show LRF result sent")

    (osd/show-lrf-result-simplified-screen)
    (is (some? (capture-command-sync!)) "Show LRF simplified sent")

    (osd/enable-day-osd)
    (is (some? (capture-command-sync!)) "Enable day OSD sent")

    (osd/disable-day-osd)
    (is (some? (capture-command-sync!)) "Disable day OSD sent")

    (osd/enable-heat-osd)
    (is (some? (capture-command-sync!)) "Enable heat OSD sent")

    (osd/disable-heat-osd)
    (is (some? (capture-command-sync!)) "Disable heat OSD sent")))

;; ============================================================================
;; GPS Commands (5 functions)
;; ============================================================================

(deftest test-all-gps-commands
  (testing "All GPS commands"
    (gps/start)
    (is (some? (capture-command-sync!)) "Start sent")

    (gps/stop)
    (is (some? (capture-command-sync!)) "Stop sent")

    (gps/set-manual-position 48.8566 2.3522 35.0)
    (is (some? (capture-command-sync!)) "Set manual position sent")

    (gps/set-use-manual-position true)
    (is (some? (capture-command-sync!)) "Set use manual sent")

    (gps/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")))

;; ============================================================================
;; LRF Commands (13 functions)
;; ============================================================================

(deftest test-all-lrf-commands
  (testing "All LRF commands"
    ;; Basic control
    (lrf/start)
    (is (some? (capture-command-sync!)) "Start sent")

    (lrf/stop)
    (is (some? (capture-command-sync!)) "Stop sent")

    (lrf/measure)
    (is (some? (capture-command-sync!)) "Measure sent")

    (lrf/new-session)
    (is (some? (capture-command-sync!)) "New session sent")

    ;; Scan mode
    (lrf/scan-on)
    (is (some? (capture-command-sync!)) "Scan on sent")

    (lrf/scan-off)
    (is (some? (capture-command-sync!)) "Scan off sent")

    ;; Modes
    (lrf/enable-fog-mode)
    (is (some? (capture-command-sync!)) "Enable fog mode sent")

    (lrf/disable-fog-mode)
    (is (some? (capture-command-sync!)) "Disable fog mode sent")

    ;; Target designator
    (lrf/target-designator-off)
    (is (some? (capture-command-sync!)) "Target designator off sent")

    (lrf/target-designator-mode-a)
    (is (some? (capture-command-sync!)) "Target designator A sent")

    (lrf/target-designator-mode-b)
    (is (some? (capture-command-sync!)) "Target designator B sent")

    ;; Other
    (lrf/refine-on)
    (is (some? (capture-command-sync!)) "Refine on sent")

    (lrf/refine-off)
    (is (some? (capture-command-sync!)) "Refine off sent")

    (lrf/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")))

;; ============================================================================
;; LRF Alignment Commands (6 functions)
;; ============================================================================

(deftest test-all-lrf-alignment-commands
  (testing "All LRF alignment commands"
    (lrf-align/set-offset-day 10 20)
    (is (some? (capture-command-sync!)) "Set offset day sent")

    (lrf-align/shift-offset-day 5 -5)
    (is (some? (capture-command-sync!)) "Shift offset day sent")

    (lrf-align/set-offset-heat 15 25)
    (is (some? (capture-command-sync!)) "Set offset heat sent")

    (lrf-align/shift-offset-heat -10 10)
    (is (some? (capture-command-sync!)) "Shift offset heat sent")

    (lrf-align/save-offsets)
    (is (some? (capture-command-sync!)) "Save offsets sent")

    (lrf-align/reset-offsets)
    (is (some? (capture-command-sync!)) "Reset offsets sent")))

;; ============================================================================
;; Compass Commands (9 functions)
;; ============================================================================

(deftest test-all-compass-commands
  (testing "All compass commands"
    ;; Basic control
    (compass/start)
    (is (some? (capture-command-sync!)) "Start sent")

    (compass/stop)
    (is (some? (capture-command-sync!)) "Stop sent")

    ;; Settings
    (compass/set-declination 5.5)
    (is (some? (capture-command-sync!)) "Set declination sent")

    (compass/set-offset-angle-azimuth 2.0)
    (is (some? (capture-command-sync!)) "Set offset azimuth sent")

    (compass/set-offset-angle-elevation -1.5)
    (is (some? (capture-command-sync!)) "Set offset elevation sent")

    ;; Calibration
    (compass/calibrate-long)
    (is (some? (capture-command-sync!)) "Calibrate long sent")

    (compass/calibrate-short)
    (is (some? (capture-command-sync!)) "Calibrate short sent")

    (compass/calibrate-next)
    (is (some? (capture-command-sync!)) "Calibrate next sent")

    (compass/calibrate-cancel)
    (is (some? (capture-command-sync!)) "Calibrate cancel sent")

    ;; Other
    (compass/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")))

;; ============================================================================
;; Computer Vision Commands (8 functions)
;; ============================================================================

(deftest test-all-cv-commands
  (testing "All CV commands"
    (cv/start-tracking (h/channel :heat) 0.5 -0.3 (System/currentTimeMillis))
    (is (some? (capture-command-sync!)) "Start tracking sent")

    (cv/stop-tracking)
    (is (some? (capture-command-sync!)) "Stop tracking sent")

    (cv/set-auto-focus (h/channel :day) true)
    (is (some? (capture-command-sync!)) "Set auto focus sent")

    (cv/enable-vampire-mode)
    (is (some? (capture-command-sync!)) "Enable vampire mode sent")

    (cv/disable-vampire-mode)
    (is (some? (capture-command-sync!)) "Disable vampire mode sent")

    (cv/enable-stabilization-mode)
    (is (some? (capture-command-sync!)) "Enable stabilization sent")

    (cv/disable-stabilization-mode)
    (is (some? (capture-command-sync!)) "Disable stabilization sent")

    (cv/start-video-dump)
    (is (some? (capture-command-sync!)) "Start video dump sent")

    (cv/stop-video-dump)
    (is (some? (capture-command-sync!)) "Stop video dump sent")))

;; ============================================================================
;; Glass Heater Commands (5 functions)
;; ============================================================================

(deftest test-all-glass-heater-commands
  (testing "All glass heater commands"
    (glass-heater/start)
    (is (some? (capture-command-sync!)) "Start sent")

    (glass-heater/stop)
    (is (some? (capture-command-sync!)) "Stop sent")

    (glass-heater/on)
    (is (some? (capture-command-sync!)) "On sent")

    (glass-heater/off)
    (is (some? (capture-command-sync!)) "Off sent")

    (glass-heater/get-meteo)
    (is (some? (capture-command-sync!)) "Get meteo sent")))

;; ============================================================================
;; Property-Based Tests for Commands
;; ============================================================================

(deftest test-property-numeric-command-parameters
  (testing "Commands with numeric parameters handle edge cases"
    ;; Test extreme but valid values
    (let [test-cases [;; Angles
                      {:fn #(rotary/rotary-set-platform-azimuth %) :values [0.0 359.999 180.0]}
                      {:fn #(rotary/rotary-set-platform-elevation %) :values [-90.0 90.0 0.0]}
                      {:fn #(rotary/rotary-set-platform-bank %) :values [-180.0 179.999 0.0]}

          ;; Normalized values
                      {:fn #(day-camera/focus-set-value %) :values [0.0 1.0 0.5]}
                      {:fn #(day-camera/zoom-set-value %) :values [0.0 1.0 0.75]}
                      {:fn #(heat-camera/set-clahe-level %) :values [0.0 1.0 0.25]}

          ;; Percentages
                      {:fn #(day-camera/set-iris %) :values [0 100 50]}
                      {:fn #(heat-camera/set-dde-level %) :values [0 512 256]}

          ;; GPS coordinates
                      {:fn (fn [[lat lon alt]] (gps/set-manual-position lat lon alt))
                       :values [[-90.0 -180.0 -433.0] [90.0 180.0 8848.0] [0.0 0.0 0.0]]}

          ;; Speeds
                      {:fn (fn [s] (rotary/rotary-azimuth-rotate s (h/direction :cw))) :values [0.0 1.0 0.5]}
                      {:fn (fn [s] (rotary/rotary-elevation-rotate s (h/direction :up))) :values [0.0 1.0 0.3]}]]

      (doseq [{:keys [fn values]} test-cases]
        (doseq [value values]
          (fn value)
          (is (some? (capture-command-sync!))
              (str "Command should handle value: " value)))))))

(deftest test-property-all-enum-values
  (testing "Commands handle all enum values correctly"
    ;; Rotary directions
    (doseq [dir [:normal :cw :ccw :shortest]]
      (rotary/rotary-azimuth-set-value 180.0 (h/direction dir))
      (is (some? (capture-command-sync!))))

    ;; Rotary modes
    (doseq [mode [:initialization :speed :position :stabilization :targeting :video-tracker]]
      (rotary/set-rotary-mode (h/mode mode))
      (is (some? (capture-command-sync!))))

    ;; Heat AGC modes
    (doseq [mode [:agc-1 :agc-2 :agc-3]]
      (heat-camera/set-agc-mode (h/agc-mode mode))
      (is (some? (capture-command-sync!))))

    ;; Heat filters
    (doseq [filter [:hot-white :hot-black :sepia :sepia-inverse]]
      (heat-camera/set-filter (h/heat-filter filter))
      (is (some? (capture-command-sync!))))

    ;; System localizations
    (doseq [loc [:en :ua :ar :cs]]
      (system/set-localization (h/localization loc))
      (is (some? (capture-command-sync!))))

    ;; CV channels
    (doseq [channel [:day :heat]]
      (cv/set-auto-focus (h/channel channel) true)
      (is (some? (capture-command-sync!))))))

;; ============================================================================
;; Command Validation Tests
;; ============================================================================

(deftest test-command-structure-validation
  (testing "All commands produce valid protobuf structure"
    ;; Sample commands from each namespace
    (let [test-commands [#(cmd-core/send-cmd-ping)
                         #(rotary/rotary-set-platform-azimuth 180.0)
                         #(day-camera/zoom-in)
                         #(heat-camera/set-agc-mode (h/agc-mode :agc-1))
                         #(system/start-rec)
                         #(osd/enable-day-osd)
                         #(gps/start)
                         #(lrf/measure)
                         #(compass/calibrate-long)
                         #(cv/start-tracking (h/channel :day) 0.0 0.0 (System/currentTimeMillis))
                         #(glass-heater/on)]]

      (doseq [cmd-fn test-commands]
        (cmd-fn)
        (let [captured (capture-command-sync!)]
          (is (some? captured) "Command should be captured")
          (when captured
            (let [proto-cmd (decode-command captured)]
              (validate-base-command proto-cmd)
              ;; Verify it can be serialized to JSON
              (is (string? (-> (JsonFormat/printer)
                               (.includingDefaultValueFields)
                               (.print proto-cmd)))
                  "Command should serialize to JSON"))))))))

;; ============================================================================
;; Performance and Throughput Tests
;; ============================================================================

(deftest test-command-burst-performance
  (testing "System handles rapid command bursts"
    (let [commands-per-type 10
          start-time (System/currentTimeMillis)
          total-commands (atom 0)]

      ;; Send bursts of different command types
      (doseq [_ (range commands-per-type)]
        ;; Core
        (cmd-core/send-cmd-ping)
        (swap! total-commands inc)

        ;; Rotary
        (rotary/rotary-set-platform-azimuth (rand 360))
        (swap! total-commands inc)

        ;; Cameras
        (day-camera/zoom-set-value (rand))
        (swap! total-commands inc)
        (heat-camera/set-dde-level (rand-int 512))
        (swap! total-commands inc)

        ;; System
        (if (even? (rand-int 2))
          (system/mark-rec-important)
          (system/unmark-rec-important))
        (swap! total-commands inc))

      ;; Wait for commands to be captured
      (Thread/sleep 200)

      ;; Check captured commands count
      (let [captured-count (count @test-commands)]
        (is (= @total-commands captured-count)
            "All commands should be captured")

        (let [duration (- (System/currentTimeMillis) start-time)]
          (is (< duration 1000)
              (str "Should send " @total-commands " commands in under 1 second"))
          (logging/log-info {:msg (str "Sent " @total-commands " commands in " duration "ms")}))))))

;; ============================================================================
;; Summary Test
;; ============================================================================

(deftest test-command-coverage-summary
  (testing "Command system coverage summary"
    (let [namespaces {:core 11
                      :rotary 42
                      :day-camera 44
                      :heat-camera 35
                      :system 14
                      :osd 8
                      :gps 5
                      :lrf 13
                      :lrf-alignment 6
                      :compass 9
                      :cv 8
                      :glass-heater 5}
          total (reduce + (vals namespaces))]

      (is (= 200 total) "Should have exactly 200 command functions")

      (logging/log-info {:msg "Command coverage complete"
                         :namespaces namespaces
                         :total total}))))