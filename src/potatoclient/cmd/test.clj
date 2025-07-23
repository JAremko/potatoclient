(ns potatoclient.cmd.test
  "Test namespace for demonstrating the command system"
  (:require [potatoclient.cmd.core :as cmd-core]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.cmd.day-camera :as day-camera]
            [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]])
  (:import [data
            JonSharedDataTypes$JonGuiDataRotaryDirection
            JonSharedDataTypes$JonGuiDataRotaryMode]))

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

  ;; Power commands
  (println "Powering on camera...")
  (day-camera/power-on)

  ;; Zoom commands
  (Thread/sleep 100)
  (println "Zooming in...")
  (day-camera/zoom-in)

  (Thread/sleep 100)
  (println "Setting zoom to table value 5...")
  ;; (day-camera/zoom-direct-table-value 5) ; Not implemented yet

  (Thread/sleep 100)
  (println "Setting zoom to direct value 2.5...")
  ;; (day-camera/zoom-direct-value 2.5) ; Not implemented yet

  ;; Focus commands
  (Thread/sleep 100)
  (println "Setting auto focus...")
  ;; (day-camera/focus-auto) ; Not implemented yet

  (Thread/sleep 100)
  (println "Setting manual focus...")
  ;; (day-camera/focus-manual) ; Not implemented yet

  (Thread/sleep 100)
  (println "Setting focus value to 1.8...")
  ;; (day-camera/focus-direct-value 1.8) ; Not implemented yet

  ;; Palette and settings
  (Thread/sleep 100)
  (println "Changing palette to BW...")
  ;; (day-camera/change-palette (day-camera/string->palette "bw")) ; Not implemented yet

  (Thread/sleep 100)
  (println "Enabling stabilization...")
  ;; (day-camera/set-stabilization true) ; Not implemented yet

  ;; Camera parameters
  (Thread/sleep 100)
  (println "Setting AGC mode to auto...")
  ;; (day-camera/set-agc-mode (day-camera/string->agc-mode "auto")) ; Not implemented yet

  (Thread/sleep 100)
  (println "Setting exposure mode to manual...")
  ;; (day-camera/set-exposure-mode (day-camera/string->exposure-mode "manual")) ; Not implemented yet

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

  (test-read-only-mode)

  (println "\n=========================================")
  (println "Command System Test Complete!")
  nil)

;; To run the test from REPL:
;; (potatoclient.cmd.test/run-all-tests)