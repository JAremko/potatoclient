(ns potatoclient.cmd.test
  "Test namespace for demonstrating the command system"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn =>]]
            [potatoclient.cmd.compass :as compass]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.cmd.cv :as cv]
            [potatoclient.cmd.day-camera :as day-camera]
            [potatoclient.cmd.glass-heater :as glass-heater]
            [potatoclient.cmd.gps :as gps]
            [potatoclient.cmd.heat-camera :as heat-camera]
            [potatoclient.cmd.lrf :as lrf]
            [potatoclient.cmd.lrf-alignment :as lrf-alignment]
            [potatoclient.cmd.osd :as osd]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.system :as system])
  (:import [ser
            JonSharedDataTypes$JonGuiDataRotaryDirection]))

(>defn test-basic-commands
  "Test basic command sending"
  []
  [=> nil?]
  (println "\n=== Testing Basic Commands ===")

  ;; Send ping
  (println "Sending ping...")
  (cmd-core/send-cmd-ping)

  ;; Small delay between commands
  (Thread/sleep 100)

  ;; Send frozen
  (println "Sending frozen...")
  (cmd-core/send-cmd-frozen)

  ;; Send noop
  (Thread/sleep 100)
  (println "Sending noop...")
  (cmd-core/send-cmd-noop)
  nil)

(>defn test-rotary-commands
  "Test rotary platform commands"
  []
  [=> nil?]
  (println "\n=== Testing Rotary Commands ===")

  ;; Basic rotary commands
  (println "Starting rotary...")
  (rotary/rotary-start)

  (Thread/sleep 100)
  (println "Setting platform azimuth to 45.0...")
  (rotary/rotary-set-platform-azimuth 45.0)

  (Thread/sleep 100)
  (println "Setting platform elevation to 30.0...")
  (rotary/rotary-set-platform-elevation 30.0)

  ;; Rotation commands
  (Thread/sleep 100)
  (println "Rotating azimuth clockwise at speed 10...")
  (rotary/rotary-azimuth-rotate 10.0
                                JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE)

  (Thread/sleep 100)
  (println "Halting azimuth...")
  (rotary/rotary-halt-azimuth)

  ;; Combined axis commands
  (Thread/sleep 100)
  (println "Rotating both axes to target positions...")
  (rotary/rotate-both-to
    90.0 5.0 JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
    45.0 3.0)

  ;; Mode and GPS commands
  (Thread/sleep 100)
  (println "Setting rotary mode to speed...")
  (rotary/set-rotary-mode (rotary/string->rotary-mode "speed"))

  (Thread/sleep 100)
  (println "Setting GPS origin...")
  (rotary/set-origin-gps 10.123 20.456 100.0)

  ;; Scan commands
  (Thread/sleep 100)
  (println "Starting scan...")
  (rotary/scan-start)

  (Thread/sleep 100)
  (println "Stopping scan...")
  (rotary/scan-stop)

  (Thread/sleep 100)
  (println "Stopping rotary...")
  (rotary/rotary-stop)
  nil)

(>defn test-day-camera-commands
  "Test day camera commands"
  []
  [=> nil?]
  (println "\n=== Testing Day Camera Commands ===")

  ;; Start/stop commands
  (println "Starting camera...")
  (day-camera/start)

  (Thread/sleep 100)
  (println "Taking photo...")
  (day-camera/take-photo)

  ;; Zoom commands
  (Thread/sleep 100)
  (println "Zooming in...")
  (day-camera/zoom-in)

  (Thread/sleep 100)
  (println "Setting zoom to table value 5...")
  (day-camera/zoom-set-table-value 5)

  (Thread/sleep 100)
  (println "Setting zoom to 0.5...")
  (day-camera/zoom-set-value 0.5)

  ;; Focus commands
  (Thread/sleep 100)
  (println "Setting auto focus...")
  (day-camera/focus-auto)

  (Thread/sleep 100)
  (println "Setting manual focus...")
  (day-camera/focus-manual)

  (Thread/sleep 100)
  (println "Setting focus value to 0.8...")
  (day-camera/focus-set-value 0.8)

  ;; Camera settings
  (Thread/sleep 100)
  (println "Setting IR filter on...")
  (day-camera/set-infrared-filter true)

  (Thread/sleep 100)
  (println "Setting auto iris...")
  (day-camera/set-auto-iris true)

  ;; FX modes
  (Thread/sleep 100)
  (println "Setting FX mode to A...")
  (when-let [mode (day-camera/string->fx-mode "a")]
    (day-camera/set-fx-mode mode))

  (Thread/sleep 100)
  (println "Setting CLAHE level to 0.7...")
  (day-camera/set-clahe-level 0.7)

  (Thread/sleep 100)
  (println "Stopping camera...")
  (day-camera/stop)

  (Thread/sleep 100)
  (println "Setting shutter speed to 1/60...")
  ;; (day-camera/set-shutter-speed 60.0) ; Not implemented yet

  (Thread/sleep 100)
  (println "Setting gain to 12.5...")
  ;; (day-camera/set-gain 12.5) ; Not implemented yet

  ;; Defog settings
  (Thread/sleep 100)
  (println "Setting defog level to 3...")
  ;; (day-camera/set-defog-level 3) ; Not implemented yet

  (Thread/sleep 100)
  (println "Setting defog status to auto...")
  ;; (day-camera/set-defog-status (day-camera/string->defog-status "auto")) ; Not implemented yet
  nil)

(>defn test-heat-camera-commands
  "Test heat camera command generation"
  []
  [=> nil?]
  (println "\n=== Testing Heat Camera Commands ===")

  ;; Start/stop commands
  (println "Starting heat camera...")
  (heat-camera/start)

  (Thread/sleep 100)
  (println "Taking thermal photo...")
  (heat-camera/take-photo)

  (Thread/sleep 100)
  (println "Calibrating (shutter)...")
  (heat-camera/calibrate)

  ;; Zoom commands
  (Thread/sleep 100)
  (println "Zooming in...")
  (heat-camera/zoom-in)

  (Thread/sleep 100)
  (println "Setting zoom table value 3...")
  (heat-camera/zoom-set-table-value 3)

  (Thread/sleep 100)
  (println "Stopping zoom...")
  (heat-camera/zoom-stop)

  ;; Focus commands
  (Thread/sleep 100)
  (println "Enabling auto focus...")
  (heat-camera/focus-auto)

  (Thread/sleep 100)
  (println "Focus step plus...")
  (heat-camera/focus-step-plus)

  ;; AGC and Filter
  (Thread/sleep 100)
  (println "Setting AGC mode to mode_2...")
  (when-let [mode (heat-camera/string->agc-mode "mode_2")]
    (heat-camera/set-agc-mode mode))

  (Thread/sleep 100)
  (println "Setting filter to hot_black...")
  (when-let [filter (heat-camera/string->filter "hot_black")]
    (heat-camera/set-filter filter))

  ;; DDE
  (Thread/sleep 100)
  (println "Enabling DDE...")
  (heat-camera/enable-dde)

  (Thread/sleep 100)
  (println "Setting DDE level to 50...")
  (heat-camera/set-dde-level 50)

  ;; FX and CLAHE
  (Thread/sleep 100)
  (println "Setting FX mode to B...")
  (when-let [mode (heat-camera/string->fx-mode "b")]
    (heat-camera/set-fx-mode mode))

  (Thread/sleep 100)
  (println "Setting CLAHE level to 0.6...")
  (heat-camera/set-clahe-level 0.6)

  (Thread/sleep 100)
  (println "Stopping heat camera...")
  (heat-camera/stop)

  (println "\nHeat camera tests complete!")
  nil)

(>defn test-read-only-mode
  "Test read-only mode restrictions"
  []
  [=> nil?]
  (println "\n=== Testing Read-Only Mode ===")

  ;; Enable read-only mode
  (println "Enabling read-only mode...")
  (cmd-core/set-read-only-mode! true)

  ;; Try to send regular commands (should be blocked)
  (Thread/sleep 100)
  (println "Attempting to start rotary (should be blocked)...")
  (rotary/rotary-start)

  (Thread/sleep 100)
  (println "Attempting to zoom in (should be blocked)...")
  (day-camera/zoom-in)

  ;; Ping and frozen should still work
  (Thread/sleep 100)
  (println "Sending ping (should work in read-only)...")
  (cmd-core/send-cmd-ping)

  (Thread/sleep 100)
  (println "Sending frozen (should work in read-only)...")
  (cmd-core/send-cmd-frozen)

  ;; Disable read-only mode
  (Thread/sleep 100)
  (println "Disabling read-only mode...")
  (cmd-core/set-read-only-mode! false)

  ;; Now commands should work again
  (Thread/sleep 100)
  (println "Starting rotary (should work now)...")
  (rotary/rotary-start)
  nil)

(>defn test-system-commands
  "Test system commands"
  []
  [=> nil?]
  (println "\n=== Testing System Commands ===")

  ;; Subsystem control
  (println "Starting all subsystems...")
  (system/start-all)

  (Thread/sleep 100)
  (println "Stopping all subsystems...")
  (system/stop-all)

  ;; Recording
  (Thread/sleep 100)
  (println "Starting recording...")
  (system/start-rec)

  (Thread/sleep 100)
  (println "Marking recording as important...")
  (system/mark-rec-important)

  (Thread/sleep 100)
  (println "Stopping recording...")
  (system/stop-rec)

  ;; Localization
  (Thread/sleep 100)
  (println "Setting localization to English...")
  (when-let [loc (system/string->localization "english")]
    (system/set-localization loc))

  ;; Mode settings
  (Thread/sleep 100)
  (println "Enabling geodesic mode...")
  (system/enable-geodesic-mode)

  (Thread/sleep 100)
  (println "Disabling geodesic mode...")
  (system/disable-geodesic-mode)

  (println "\nSystem command tests complete!")
  nil)

(>defn test-osd-commands
  "Test OSD commands"
  []
  [=> nil?]
  (println "\n=== Testing OSD Commands ===")

  ;; Screen displays
  (println "Showing default screen...")
  (osd/show-default-screen)

  (Thread/sleep 100)
  (println "Showing LRF measure screen...")
  (osd/show-lrf-measure-screen)

  (Thread/sleep 100)
  (println "Showing LRF result screen...")
  (osd/show-lrf-result-screen)

  ;; OSD overlays
  (Thread/sleep 100)
  (println "Enabling day camera OSD...")
  (osd/enable-day-osd)

  (Thread/sleep 100)
  (println "Enabling heat camera OSD...")
  (osd/enable-heat-osd)

  (Thread/sleep 100)
  (println "Disabling day camera OSD...")
  (osd/disable-day-osd)

  (Thread/sleep 100)
  (println "Disabling heat camera OSD...")
  (osd/disable-heat-osd)

  (println "\nOSD command tests complete!")
  nil)

(>defn test-gps-commands
  "Test GPS commands"
  []
  [=> nil?]
  (println "\n=== Testing GPS Commands ===")

  ;; Basic control
  (println "Starting GPS...")
  (gps/start)

  (Thread/sleep 100)
  (println "Setting manual position (48.8566° N, 2.3522° E, 35m)...")
  (gps/set-manual-position 48.8566 2.3522 35.0)

  (Thread/sleep 100)
  (println "Enabling use of manual position...")
  (gps/set-use-manual-position true)

  (Thread/sleep 100)
  (println "Getting GPS meteo data...")
  (gps/get-meteo)

  (Thread/sleep 100)
  (println "Disabling use of manual position...")
  (gps/set-use-manual-position false)

  (Thread/sleep 100)
  (println "Stopping GPS...")
  (gps/stop)

  (println "\nGPS command tests complete!")
  nil)

(>defn test-lrf-commands
  "Test LRF commands"
  []
  [=> nil?]
  (println "\n=== Testing LRF Commands ===")

  ;; Basic control
  (println "Starting LRF...")
  (lrf/start)

  (Thread/sleep 100)
  (println "Triggering measurement...")
  (lrf/measure)

  (Thread/sleep 100)
  (println "Starting new session...")
  (lrf/new-session)

  ;; Modes
  (Thread/sleep 100)
  (println "Enabling scan mode...")
  (lrf/scan-on)

  (Thread/sleep 100)
  (println "Enabling fog mode...")
  (lrf/enable-fog-mode)

  (Thread/sleep 100)
  (println "Setting target designator to mode A...")
  (lrf/target-designator-mode-a)

  (Thread/sleep 100)
  (println "Turning off target designator...")
  (lrf/target-designator-off)

  (Thread/sleep 100)
  (println "Getting LRF meteo data...")
  (lrf/get-meteo)

  (Thread/sleep 100)
  (println "Stopping LRF...")
  (lrf/stop)

  (println "\nLRF command tests complete!")
  nil)

(>defn test-lrf-alignment-commands
  "Test LRF alignment commands"
  []
  [=> nil?]
  (println "\n=== Testing LRF Alignment Commands ===")

  ;; Day camera offsets
  (println "Setting day camera offset (0.5, -0.3)...")
  (lrf-alignment/set-offset-day 0.5 -0.3)

  (Thread/sleep 100)
  (println "Shifting day camera offset by (0.1, 0.05)...")
  (lrf-alignment/shift-offset-day 0.1 0.05)

  ;; Heat camera offsets
  (Thread/sleep 100)
  (println "Setting heat camera offset (0.2, -0.1)...")
  (lrf-alignment/set-offset-heat 0.2 -0.1)

  (Thread/sleep 100)
  (println "Saving offsets...")
  (lrf-alignment/save-offsets)

  (Thread/sleep 100)
  (println "Resetting offsets to defaults...")
  (lrf-alignment/reset-offsets)

  (println "\nLRF alignment command tests complete!")
  nil)

(>defn test-compass-commands
  "Test compass commands"
  []
  [=> nil?]
  (println "\n=== Testing Compass Commands ===")

  ;; Basic control
  (println "Starting compass...")
  (compass/start)

  (Thread/sleep 100)
  (println "Setting declination to 5.5 degrees...")
  (compass/set-declination 5.5)

  (Thread/sleep 100)
  (println "Setting offset angles (azimuth: 1.2, elevation: -0.8)...")
  (compass/set-offset-angle-azimuth 1.2)
  (compass/set-offset-angle-elevation -0.8)

  ;; Calibration
  (Thread/sleep 100)
  (println "Starting short calibration...")
  (compass/calibrate-short)

  (Thread/sleep 100)
  (println "Moving to next calibration step...")
  (compass/calibrate-next)

  (Thread/sleep 100)
  (println "Canceling calibration...")
  (compass/calibrate-cancel)

  (Thread/sleep 100)
  (println "Getting compass meteo data...")
  (compass/get-meteo)

  (Thread/sleep 100)
  (println "Stopping compass...")
  (compass/stop)

  (println "\nCompass command tests complete!")
  nil)

(>defn test-cv-commands
  "Test computer vision commands"
  []
  [=> nil?]
  (println "\n=== Testing Computer Vision Commands ===")

  ;; Tracking
  (println "Starting tracking at (0.5, 0.5)...")
  (cv/start-tracking 0.5 0.5 (System/currentTimeMillis))

  (Thread/sleep 100)
  (println "Setting auto-focus for day camera...")
  (when-let [channel (cv/string->channel "day")]
    (cv/set-auto-focus channel true))

  (Thread/sleep 100)
  (println "Enabling vampire mode...")
  (cv/enable-vampire-mode)

  (Thread/sleep 100)
  (println "Enabling stabilization mode...")
  (cv/enable-stabilization-mode)

  ;; Video recording
  (Thread/sleep 100)
  (println "Starting video dump...")
  (cv/start-video-dump)

  (Thread/sleep 100)
  (println "Stopping video dump...")
  (cv/stop-video-dump)

  (Thread/sleep 100)
  (println "Stopping tracking...")
  (cv/stop-tracking)

  (println "\nComputer vision command tests complete!")
  nil)

(>defn test-glass-heater-commands
  "Test glass heater commands"
  []
  [=> nil?]
  (println "\n=== Testing Glass Heater Commands ===")

  ;; Basic control
  (println "Starting glass heater subsystem...")
  (glass-heater/start)

  (Thread/sleep 100)
  (println "Turning heater on...")
  (glass-heater/on)

  (Thread/sleep 100)
  (println "Getting heater status...")
  (glass-heater/get-meteo)

  (Thread/sleep 100)
  (println "Turning heater off...")
  (glass-heater/off)

  (Thread/sleep 100)
  (println "Stopping glass heater subsystem...")
  (glass-heater/stop)

  (println "\nGlass heater command tests complete!")
  nil)

(>defn run-all-tests
  "Run all command tests"
  []
  [=> nil?]
  (println "Starting PotatoClient Command System Test")
  (println "=========================================")

  ;; Initialize the command system
  (cmd-core/init!)

  ;; Give the reader thread time to start
  (Thread/sleep 500)

  ;; Run tests
  (test-basic-commands)
  (Thread/sleep 500)

  (test-rotary-commands)
  (Thread/sleep 500)

  (test-day-camera-commands)
  (Thread/sleep 500)

  (test-heat-camera-commands)
  (Thread/sleep 500)

  (test-system-commands)
  (Thread/sleep 500)

  (test-osd-commands)
  (Thread/sleep 500)

  (test-gps-commands)
  (Thread/sleep 500)

  (test-lrf-commands)
  (Thread/sleep 500)

  (test-lrf-alignment-commands)
  (Thread/sleep 500)

  (test-compass-commands)
  (Thread/sleep 500)

  (test-cv-commands)
  (Thread/sleep 500)

  (test-glass-heater-commands)
  (Thread/sleep 500)

  (test-read-only-mode)

  (println "\n=========================================")
  (println "Command System Test Complete!")
  nil)

;; To run the test from REPL:
;; (potatoclient.cmd.test/run-all-tests)