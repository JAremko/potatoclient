(ns potatoclient.transit.registry-sanity-test
  (:require [clojure.test :refer [deftest is testing]])
  (:import [potatoclient.transit ActionRegistry]))

(deftest test-all-modules-loaded
  (testing "All command modules are loaded"
    (let [all-commands (ActionRegistry/getAllActionNames)]
      ;; Check we have the expected modules
      (is (some #(= "ping" %) all-commands) "Basic commands loaded")
      (is (some #(= "gps-start" %) all-commands) "GPS commands loaded")
      (is (some #(= "rotary-start" %) all-commands) "Rotary commands loaded")
      (is (some #(= "system-start-all" %) all-commands) "System commands loaded")
      (is (some #(= "cv-stop-track" %) all-commands) "CV commands loaded")
      (is (some #(= "compass-start" %) all-commands) "Compass commands loaded")
      (is (some #(= "day-camera-start" %) all-commands) "Day camera commands loaded")
      (is (some #(= "heat-camera-start" %) all-commands) "Heat camera commands loaded")
      (is (some #(= "lrf-start" %) all-commands) "LRF commands loaded")
      (is (some #(= "osd-show" %) all-commands) "OSD commands loaded")
      (is (some #(= "lrf-align-start" %) all-commands) "LRF Align commands loaded")
      (is (some #(= "glass-heater-start" %) all-commands) "Glass Heater commands loaded")
      (is (some #(= "lira-start" %) all-commands) "LIRA commands loaded")
      
      ;; Check important commands
      (is (some #(= "rotary-rotate-to-ndc" %) all-commands) "Important rotary NDC command")
      (is (some #(= "cv-start-track-ndc" %) all-commands) "Important CV tracking command")
      
      ;; We should have all 180 commands
      (is (= 180 (count all-commands)) 
          (str "Expected 180 commands, got " (count all-commands))))))