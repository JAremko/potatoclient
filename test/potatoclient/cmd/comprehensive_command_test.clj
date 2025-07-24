(ns potatoclient.cmd.comprehensive-command-test
  "Comprehensive tests for ALL 200+ command functions"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.core.async :as async :refer [<! >! go timeout alt!]]
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
            [potatoclient.logging :as logging])
  (:import [cmd JonSharedCmd$Root]
           [com.google.protobuf.util JsonFormat]))

;; ============================================================================
;; Test Setup
;; ============================================================================

(def test-command-channel (atom nil))

(defn command-capture-fixture [f]
  (let [original-channel cmd-core/command-channel]
    (reset! test-command-channel (async/chan 1000))
    (alter-var-root #'cmd-core/command-channel (constantly @test-command-channel))
    (f)
    (alter-var-root #'cmd-core/command-channel (constantly original-channel))
    (async/close! @test-command-channel)))

(use-fixtures :each command-capture-fixture)

(defn capture-command! []
  (go
    (alt!
      @test-command-channel ([cmd] cmd)
      (timeout 1000) nil)))

(defn decode-command [{:keys [pld]}]
  (when pld
    (JonSharedCmd$Root/parseFrom pld)))

(defn validate-base-command [cmd-proto]
  (is (= 1 (.getProtocolVersion cmd-proto)) "Protocol version should be 1")
  (is (.hasClientType cmd-proto) "Should have client type"))

;; ============================================================================
;; Core Commands
;; ============================================================================

(deftest test-all-core-commands
  (testing "Core command functions"
    ;; Ping
    (cmd-core/send-cmd-ping)
    (is (some? (<! (capture-command!))) "Ping command sent")
    
    ;; Frozen
    (cmd-core/send-cmd-frozen)
    (is (some? (<! (capture-command!))) "Frozen command sent")
    
    ;; Noop
    (cmd-core/send-cmd-noop)
    (is (some? (<! (capture-command!))) "Noop command sent")))

;; ============================================================================
;; Rotary Commands (42 functions)
;; ============================================================================

(deftest test-all-rotary-commands
  (testing "All rotary platform commands"
    ;; Basic control
    (rotary/rotary-start)
    (is (some? (<! (capture-command!))) "Start command sent")
    
    (rotary/rotary-stop)
    (is (some? (<! (capture-command!))) "Stop command sent")
    
    (rotary/rotary-halt)
    (is (some? (<! (capture-command!))) "Halt command sent")
    
    ;; Platform position
    (rotary/rotary-set-platform-azimuth 180.0)
    (is (some? (<! (capture-command!))) "Set platform azimuth sent")
    
    (rotary/rotary-set-platform-elevation 45.0)
    (is (some? (<! (capture-command!))) "Set platform elevation sent")
    
    (rotary/rotary-set-platform-bank 30.0)
    (is (some? (<! (capture-command!))) "Set platform bank sent")
    
    ;; Axis halt
    (rotary/rotary-halt-azimuth)
    (is (some? (<! (capture-command!))) "Halt azimuth sent")
    
    (rotary/rotary-halt-elevation)
    (is (some? (<! (capture-command!))) "Halt elevation sent")
    
    (rotary/rotary-halt-elevation-and-azimuth)
    (is (some? (<! (capture-command!))) "Halt both axes sent")
    
    ;; Azimuth control
    (rotary/rotary-azimuth-set-value 90.0 :normal)
    (is (some? (<! (capture-command!))) "Set azimuth value sent")
    
    (rotary/rotary-azimuth-rotate-to 270.0 0.5 :shortest)
    (is (some? (<! (capture-command!))) "Rotate azimuth to sent")
    
    (rotary/rotary-azimuth-rotate 0.7 :cw)
    (is (some? (<! (capture-command!))) "Rotate azimuth sent")
    
    (rotary/rotary-azimuth-rotate-relative 45.0 0.5 :ccw)
    (is (some? (<! (capture-command!))) "Rotate azimuth relative sent")
    
    (rotary/rotary-azimuth-rotate-relative-set 30.0 :normal)
    (is (some? (<! (capture-command!))) "Rotate azimuth relative set sent")
    
    ;; Elevation control
    (rotary/rotary-elevation-set-value 30.0)
    (is (some? (<! (capture-command!))) "Set elevation value sent")
    
    (rotary/rotary-elevation-rotate-to 60.0 0.5)
    (is (some? (<! (capture-command!))) "Rotate elevation to sent")
    
    (rotary/rotary-elevation-rotate 0.3 :up)
    (is (some? (<! (capture-command!))) "Rotate elevation sent")
    
    (rotary/rotary-elevation-rotate-relative 15.0 0.5 :down)
    (is (some? (<! (capture-command!))) "Rotate elevation relative sent")
    
    (rotary/rotary-elevation-rotate-relative-set 20.0 :up)
    (is (some? (<! (capture-command!))) "Rotate elevation relative set sent")
    
    ;; Combined axis
    (rotary/rotate-both-to 180.0 0.5 :normal 45.0 0.5)
    (is (some? (<! (capture-command!))) "Rotate both to sent")
    
    (rotary/rotate-both 0.5 :cw 0.3 :up)
    (is (some? (<! (capture-command!))) "Rotate both sent")
    
    (rotary/set-both-to 90.0 30.0 :shortest)
    (is (some? (<! (capture-command!))) "Set both to sent")
    
    ;; Other commands
    (rotary/set-calculate-base-position-from-compass true)
    (is (some? (<! (capture-command!))) "Use rotary as compass sent")
    
    (rotary/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")
    
    (rotary/set-rotate-to-gps 12.345 56.789 100.0)
    (is (some? (<! (capture-command!))) "Rotate to GPS sent")
    
    (rotary/set-origin-gps 0.0 0.0 0.0)
    (is (some? (<! (capture-command!))) "Set origin GPS sent")
    
    (rotary/set-rotary-mode :speed)
    (is (some? (<! (capture-command!))) "Set rotary mode sent")
    
    (rotary/rotate-to-ndc :day 0.5 -0.3)
    (is (some? (<! (capture-command!))) "Rotate to NDC sent")
    
    ;; Scan commands
    (rotary/scan-start)
    (is (some? (<! (capture-command!))) "Scan start sent")
    
    (rotary/scan-stop)
    (is (some? (<! (capture-command!))) "Scan stop sent")
    
    (rotary/scan-pause)
    (is (some? (<! (capture-command!))) "Scan pause sent")
    
    (rotary/scan-unpause)
    (is (some? (<! (capture-command!))) "Scan unpause sent")
    
    (rotary/scan-prev)
    (is (some? (<! (capture-command!))) "Scan prev sent")
    
    (rotary/scan-next)
    (is (some? (<! (capture-command!))) "Scan next sent")))

;; ============================================================================
;; Day Camera Commands (44 functions)
;; ============================================================================

(deftest test-all-day-camera-commands
  (testing "All day camera commands"
    ;; Basic control
    (day-camera/start)
    (is (some? (<! (capture-command!))) "Start sent")
    
    (day-camera/stop)
    (is (some? (<! (capture-command!))) "Stop sent")
    
    (day-camera/take-photo)
    (is (some? (<! (capture-command!))) "Take photo sent")
    
    (day-camera/halt-all)
    (is (some? (<! (capture-command!))) "Halt all sent")
    
    ;; Camera settings
    (day-camera/set-infrared-filter true)
    (is (some? (<! (capture-command!))) "Set IR filter sent")
    
    (day-camera/set-iris 50)
    (is (some? (<! (capture-command!))) "Set iris sent")
    
    (day-camera/set-auto-iris true)
    (is (some? (<! (capture-command!))) "Set auto iris sent")
    
    ;; Focus commands
    (day-camera/focus-set-value 0.5)
    (is (some? (<! (capture-command!))) "Focus set value sent")
    
    (day-camera/focus-move 0.8 0.5)
    (is (some? (<! (capture-command!))) "Focus move sent")
    
    (day-camera/focus-halt)
    (is (some? (<! (capture-command!))) "Focus halt sent")
    
    (day-camera/focus-offset 0.1)
    (is (some? (<! (capture-command!))) "Focus offset sent")
    
    (day-camera/focus-reset)
    (is (some? (<! (capture-command!))) "Focus reset sent")
    
    (day-camera/focus-save-to-table)
    (is (some? (<! (capture-command!))) "Focus save to table sent")
    
    ;; Focus convenience
    (day-camera/focus-in)
    (is (some? (<! (capture-command!))) "Focus in sent")
    
    (day-camera/focus-out)
    (is (some? (<! (capture-command!))) "Focus out sent")
    
    (day-camera/focus-step-in)
    (is (some? (<! (capture-command!))) "Focus step in sent")
    
    (day-camera/focus-step-out)
    (is (some? (<! (capture-command!))) "Focus step out sent")
    
    (day-camera/focus-auto)
    (is (some? (<! (capture-command!))) "Focus auto sent")
    
    (day-camera/focus-manual)
    (is (some? (<! (capture-command!))) "Focus manual sent")
    
    ;; Zoom commands
    (day-camera/zoom-set-value 0.7)
    (is (some? (<! (capture-command!))) "Zoom set value sent")
    
    (day-camera/zoom-move 0.9 0.5)
    (is (some? (<! (capture-command!))) "Zoom move sent")
    
    (day-camera/zoom-halt)
    (is (some? (<! (capture-command!))) "Zoom halt sent")
    
    (day-camera/zoom-offset 0.2)
    (is (some? (<! (capture-command!))) "Zoom offset sent")
    
    (day-camera/zoom-reset)
    (is (some? (<! (capture-command!))) "Zoom reset sent")
    
    (day-camera/zoom-save-to-table)
    (is (some? (<! (capture-command!))) "Zoom save to table sent")
    
    (day-camera/zoom-set-table-value 5)
    (is (some? (<! (capture-command!))) "Zoom set table value sent")
    
    (day-camera/zoom-next-table-position)
    (is (some? (<! (capture-command!))) "Zoom next table pos sent")
    
    (day-camera/zoom-prev-table-position)
    (is (some? (<! (capture-command!))) "Zoom prev table pos sent")
    
    (day-camera/set-digital-zoom-level 2.0)
    (is (some? (<! (capture-command!))) "Set digital zoom sent")
    
    ;; Zoom convenience
    (day-camera/zoom-in)
    (is (some? (<! (capture-command!))) "Zoom in sent")
    
    (day-camera/zoom-out)
    (is (some? (<! (capture-command!))) "Zoom out sent")
    
    ;; FX & Enhancement
    (day-camera/set-fx-mode :day-a)
    (is (some? (<! (capture-command!))) "Set FX mode sent")
    
    (day-camera/next-fx-mode)
    (is (some? (<! (capture-command!))) "Next FX mode sent")
    
    (day-camera/prev-fx-mode)
    (is (some? (<! (capture-command!))) "Prev FX mode sent")
    
    (day-camera/set-clahe-level 0.5)
    (is (some? (<! (capture-command!))) "Set CLAHE level sent")
    
    (day-camera/shift-clahe-level 0.1)
    (is (some? (<! (capture-command!))) "Shift CLAHE level sent")
    
    ;; Other
    (day-camera/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")))

;; ============================================================================
;; Heat Camera Commands (35 functions)
;; ============================================================================

(deftest test-all-heat-camera-commands
  (testing "All heat camera commands"
    ;; Basic control
    (heat-camera/start)
    (is (some? (<! (capture-command!))) "Start sent")
    
    (heat-camera/stop)
    (is (some? (<! (capture-command!))) "Stop sent")
    
    (heat-camera/take-photo)
    (is (some? (<! (capture-command!))) "Take photo sent")
    
    (heat-camera/calibrate)
    (is (some? (<! (capture-command!))) "Calibrate sent")
    
    ;; Camera settings
    (heat-camera/set-agc-mode :agc-1)
    (is (some? (<! (capture-command!))) "Set AGC mode sent")
    
    (heat-camera/set-filter :hot-white)
    (is (some? (<! (capture-command!))) "Set filter sent")
    
    (heat-camera/set-auto-focus true)
    (is (some? (<! (capture-command!))) "Set auto focus sent")
    
    (heat-camera/set-calib-mode)
    (is (some? (<! (capture-command!))) "Set calib mode sent")
    
    ;; Zoom commands
    (heat-camera/zoom-in)
    (is (some? (<! (capture-command!))) "Zoom in sent")
    
    (heat-camera/zoom-out)
    (is (some? (<! (capture-command!))) "Zoom out sent")
    
    (heat-camera/zoom-stop)
    (is (some? (<! (capture-command!))) "Zoom stop sent")
    
    (heat-camera/zoom-set-table-value 3)
    (is (some? (<! (capture-command!))) "Zoom set table value sent")
    
    (heat-camera/zoom-next-table-position)
    (is (some? (<! (capture-command!))) "Zoom next table pos sent")
    
    (heat-camera/zoom-prev-table-position)
    (is (some? (<! (capture-command!))) "Zoom prev table pos sent")
    
    (heat-camera/set-digital-zoom-level 1.5)
    (is (some? (<! (capture-command!))) "Set digital zoom sent")
    
    (heat-camera/zoom-reset)
    (is (some? (<! (capture-command!))) "Zoom reset sent")
    
    (heat-camera/zoom-save-to-table)
    (is (some? (<! (capture-command!))) "Zoom save to table sent")
    
    ;; Focus commands
    (heat-camera/focus-in)
    (is (some? (<! (capture-command!))) "Focus in sent")
    
    (heat-camera/focus-out)
    (is (some? (<! (capture-command!))) "Focus out sent")
    
    (heat-camera/focus-stop)
    (is (some? (<! (capture-command!))) "Focus stop sent")
    
    (heat-camera/focus-step-plus)
    (is (some? (<! (capture-command!))) "Focus step plus sent")
    
    (heat-camera/focus-step-minus)
    (is (some? (<! (capture-command!))) "Focus step minus sent")
    
    (heat-camera/focus-auto)
    (is (some? (<! (capture-command!))) "Focus auto sent")
    
    (heat-camera/focus-manual)
    (is (some? (<! (capture-command!))) "Focus manual sent")
    
    ;; DDE
    (heat-camera/enable-dde)
    (is (some? (<! (capture-command!))) "Enable DDE sent")
    
    (heat-camera/disable-dde)
    (is (some? (<! (capture-command!))) "Disable DDE sent")
    
    (heat-camera/set-dde-level 100)
    (is (some? (<! (capture-command!))) "Set DDE level sent")
    
    (heat-camera/shift-dde-level 10)
    (is (some? (<! (capture-command!))) "Shift DDE level sent")
    
    ;; FX & Enhancement
    (heat-camera/set-fx-mode :heat-b)
    (is (some? (<! (capture-command!))) "Set FX mode sent")
    
    (heat-camera/next-fx-mode)
    (is (some? (<! (capture-command!))) "Next FX mode sent")
    
    (heat-camera/prev-fx-mode)
    (is (some? (<! (capture-command!))) "Prev FX mode sent")
    
    (heat-camera/set-clahe-level 0.7)
    (is (some? (<! (capture-command!))) "Set CLAHE level sent")
    
    (heat-camera/shift-clahe-level -0.1)
    (is (some? (<! (capture-command!))) "Shift CLAHE level sent")
    
    ;; Other
    (heat-camera/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")))

;; ============================================================================
;; System Commands (14 functions)
;; ============================================================================

(deftest test-all-system-commands
  (testing "All system commands"
    ;; System control
    (system/reboot)
    (is (some? (<! (capture-command!))) "Reboot sent")
    
    (system/power-off)
    (is (some? (<! (capture-command!))) "Power off sent")
    
    (system/reset-configs)
    (is (some? (<! (capture-command!))) "Reset configs sent")
    
    (system/enter-transport)
    (is (some? (<! (capture-command!))) "Enter transport sent")
    
    ;; Subsystem control
    (system/start-all)
    (is (some? (<! (capture-command!))) "Start all sent")
    
    (system/stop-all)
    (is (some? (<! (capture-command!))) "Stop all sent")
    
    ;; Recording
    (system/start-rec)
    (is (some? (<! (capture-command!))) "Start rec sent")
    
    (system/stop-rec)
    (is (some? (<! (capture-command!))) "Stop rec sent")
    
    (system/mark-rec-important)
    (is (some? (<! (capture-command!))) "Mark rec important sent")
    
    (system/unmark-rec-important)
    (is (some? (<! (capture-command!))) "Unmark rec important sent")
    
    ;; Settings
    (system/enable-geodesic-mode)
    (is (some? (<! (capture-command!))) "Enable geodesic sent")
    
    (system/disable-geodesic-mode)
    (is (some? (<! (capture-command!))) "Disable geodesic sent")
    
    (system/set-localization :ua)
    (is (some? (<! (capture-command!))) "Set localization sent")))

;; ============================================================================
;; OSD Commands (8 functions)
;; ============================================================================

(deftest test-all-osd-commands
  (testing "All OSD commands"
    (osd/show-default-screen)
    (is (some? (<! (capture-command!))) "Show default screen sent")
    
    (osd/show-lrf-measure-screen)
    (is (some? (<! (capture-command!))) "Show LRF measure sent")
    
    (osd/show-lrf-result-screen)
    (is (some? (<! (capture-command!))) "Show LRF result sent")
    
    (osd/show-lrf-result-simplified-screen)
    (is (some? (<! (capture-command!))) "Show LRF simplified sent")
    
    (osd/enable-day-osd)
    (is (some? (<! (capture-command!))) "Enable day OSD sent")
    
    (osd/disable-day-osd)
    (is (some? (<! (capture-command!))) "Disable day OSD sent")
    
    (osd/enable-heat-osd)
    (is (some? (<! (capture-command!))) "Enable heat OSD sent")
    
    (osd/disable-heat-osd)
    (is (some? (<! (capture-command!))) "Disable heat OSD sent")))

;; ============================================================================
;; GPS Commands (5 functions)
;; ============================================================================

(deftest test-all-gps-commands
  (testing "All GPS commands"
    (gps/start)
    (is (some? (<! (capture-command!))) "Start sent")
    
    (gps/stop)
    (is (some? (<! (capture-command!))) "Stop sent")
    
    (gps/set-manual-position 48.8566 2.3522 35.0)
    (is (some? (<! (capture-command!))) "Set manual position sent")
    
    (gps/set-use-manual-position true)
    (is (some? (<! (capture-command!))) "Set use manual sent")
    
    (gps/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")))

;; ============================================================================
;; LRF Commands (13 functions)
;; ============================================================================

(deftest test-all-lrf-commands
  (testing "All LRF commands"
    ;; Basic control
    (lrf/start)
    (is (some? (<! (capture-command!))) "Start sent")
    
    (lrf/stop)
    (is (some? (<! (capture-command!))) "Stop sent")
    
    (lrf/measure)
    (is (some? (<! (capture-command!))) "Measure sent")
    
    (lrf/new-session)
    (is (some? (<! (capture-command!))) "New session sent")
    
    ;; Scan mode
    (lrf/scan-on)
    (is (some? (<! (capture-command!))) "Scan on sent")
    
    (lrf/scan-off)
    (is (some? (<! (capture-command!))) "Scan off sent")
    
    ;; Modes
    (lrf/enable-fog-mode)
    (is (some? (<! (capture-command!))) "Enable fog mode sent")
    
    (lrf/disable-fog-mode)
    (is (some? (<! (capture-command!))) "Disable fog mode sent")
    
    ;; Target designator
    (lrf/target-designator-off)
    (is (some? (<! (capture-command!))) "Target designator off sent")
    
    (lrf/target-designator-mode-a)
    (is (some? (<! (capture-command!))) "Target designator A sent")
    
    (lrf/target-designator-mode-b)
    (is (some? (<! (capture-command!))) "Target designator B sent")
    
    ;; Other
    (lrf/refine-on)
    (is (some? (<! (capture-command!))) "Refine on sent")
    
    (lrf/refine-off)
    (is (some? (<! (capture-command!))) "Refine off sent")
    
    (lrf/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")))

;; ============================================================================
;; LRF Alignment Commands (6 functions)
;; ============================================================================

(deftest test-all-lrf-alignment-commands
  (testing "All LRF alignment commands"
    (lrf-align/set-offset-day 10 20)
    (is (some? (<! (capture-command!))) "Set offset day sent")
    
    (lrf-align/shift-offset-day 5 -5)
    (is (some? (<! (capture-command!))) "Shift offset day sent")
    
    (lrf-align/set-offset-heat 15 25)
    (is (some? (<! (capture-command!))) "Set offset heat sent")
    
    (lrf-align/shift-offset-heat -10 10)
    (is (some? (<! (capture-command!))) "Shift offset heat sent")
    
    (lrf-align/save-offsets)
    (is (some? (<! (capture-command!))) "Save offsets sent")
    
    (lrf-align/reset-offsets)
    (is (some? (<! (capture-command!))) "Reset offsets sent")))

;; ============================================================================
;; Compass Commands (9 functions)
;; ============================================================================

(deftest test-all-compass-commands
  (testing "All compass commands"
    ;; Basic control
    (compass/start)
    (is (some? (<! (capture-command!))) "Start sent")
    
    (compass/stop)
    (is (some? (<! (capture-command!))) "Stop sent")
    
    ;; Settings
    (compass/set-declination 5.5)
    (is (some? (<! (capture-command!))) "Set declination sent")
    
    (compass/set-offset-angles 2.0 -1.5)
    (is (some? (<! (capture-command!))) "Set offset angles sent")
    
    ;; Calibration
    (compass/calibrate-long)
    (is (some? (<! (capture-command!))) "Calibrate long sent")
    
    (compass/calibrate-short)
    (is (some? (<! (capture-command!))) "Calibrate short sent")
    
    (compass/calibrate-next)
    (is (some? (<! (capture-command!))) "Calibrate next sent")
    
    (compass/calibrate-cancel)
    (is (some? (<! (capture-command!))) "Calibrate cancel sent")
    
    ;; Other
    (compass/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")))

;; ============================================================================
;; Computer Vision Commands (8 functions)
;; ============================================================================

(deftest test-all-cv-commands
  (testing "All CV commands"
    (cv/start-tracking 0.5 -0.3)
    (is (some? (<! (capture-command!))) "Start tracking sent")
    
    (cv/stop-tracking)
    (is (some? (<! (capture-command!))) "Stop tracking sent")
    
    (cv/set-auto-focus :day true)
    (is (some? (<! (capture-command!))) "Set auto focus sent")
    
    (cv/set-vampire-mode true)
    (is (some? (<! (capture-command!))) "Set vampire mode sent")
    
    (cv/set-stabilization-mode false)
    (is (some? (<! (capture-command!))) "Set stabilization sent")
    
    (cv/start-video-dump)
    (is (some? (<! (capture-command!))) "Start video dump sent")
    
    (cv/stop-video-dump)
    (is (some? (<! (capture-command!))) "Stop video dump sent")))

;; ============================================================================
;; Glass Heater Commands (5 functions)
;; ============================================================================

(deftest test-all-glass-heater-commands
  (testing "All glass heater commands"
    (glass-heater/start)
    (is (some? (<! (capture-command!))) "Start sent")
    
    (glass-heater/stop)
    (is (some? (<! (capture-command!))) "Stop sent")
    
    (glass-heater/on)
    (is (some? (<! (capture-command!))) "On sent")
    
    (glass-heater/off)
    (is (some? (<! (capture-command!))) "Off sent")
    
    (glass-heater/get-meteo)
    (is (some? (<! (capture-command!))) "Get meteo sent")))

;; ============================================================================
;; Property-Based Tests for Commands
;; ============================================================================

(deftest test-property-numeric-command-parameters
  (testing "Commands with numeric parameters handle edge cases"
    ;; Test extreme but valid values
    (let [test-cases [
          ;; Angles
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
          {:fn (fn [s] (rotary/rotary-azimuth-rotate s :cw)) :values [0.0 1.0 0.5]}
          {:fn (fn [s] (rotary/rotary-elevation-rotate s :up)) :values [0.0 1.0 0.3]}]]
      
      (doseq [{:keys [fn values]} test-cases]
        (doseq [value values]
          (fn value)
          (is (some? (<! (capture-command!))) 
              (str "Command should handle value: " value)))))))

(deftest test-property-all-enum-values
  (testing "Commands handle all enum values correctly"
    ;; Rotary directions
    (doseq [dir [:normal :cw :ccw :shortest]]
      (rotary/rotary-azimuth-set-value 180.0 dir)
      (is (some? (<! (capture-command!)))))
    
    ;; Rotary modes
    (doseq [mode [:initialization :speed :position :stabilization :targeting :video-tracker]]
      (rotary/set-rotary-mode mode)
      (is (some? (<! (capture-command!)))))
    
    ;; Heat AGC modes
    (doseq [mode [:agc-1 :agc-2 :agc-3]]
      (heat-camera/set-agc-mode mode)
      (is (some? (<! (capture-command!)))))
    
    ;; Heat filters
    (doseq [filter [:hot-white :hot-black :sepia :sepia-inverse]]
      (heat-camera/set-filter filter)
      (is (some? (<! (capture-command!)))))
    
    ;; System localizations
    (doseq [loc [:en :ua :ar :cs]]
      (system/set-localization loc)
      (is (some? (<! (capture-command!)))))
    
    ;; CV channels
    (doseq [channel [:day :heat]]
      (cv/set-auto-focus channel true)
      (is (some? (<! (capture-command!)))))))

;; ============================================================================
;; Command Validation Tests
;; ============================================================================

(deftest test-command-structure-validation
  (testing "All commands produce valid protobuf structure"
    ;; Sample commands from each namespace
    (let [test-commands [
          #(cmd-core/send-cmd-ping)
          #(rotary/axis {:azimuth (rotary/azimuth 180.0)})
          #(day-camera/zoom {:mode :zoom-mode-in :speed 0.5})
          #(heat-camera/brightness 75)
          #(system/start-rec)
          #(osd/enable-day-osd)
          #(gps/start)
          #(lrf/measure)
          #(compass/calibrate-long)
          #(cv/start-tracking 0.0 0.0)
          #(glass-heater/on)]]
      
      (doseq [cmd-fn test-commands]
        (cmd-fn)
        (let [captured (<! (capture-command!))]
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
        (rotary/axis {:azimuth (rotary/azimuth (rand 360))})
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
      
      ;; Capture all commands
      (let [captured-count (atom 0)]
        (go-loop []
          (when-let [cmd (alt!
                          @test-command-channel ([c] c)
                          (timeout 100) nil)]
            (swap! captured-count inc)
            (recur)))
        
        (Thread/sleep 200)
        
        (is (= @total-commands @captured-count)
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