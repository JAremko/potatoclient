(ns potatoclient.transit.command-roundtrip-test
  "Comprehensive roundtrip tests for Transit command format.
  
  Tests the new nested command format that maps directly to protobuf structure.
  Validates Transit encoding/decoding and command structure."
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit]
            [potatoclient.transit.app-db :as app-db]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; =============================================================================
;; Test Fixtures
;; =============================================================================

(defn reset-app-db-fixture [f]
  (app-db/reset-to-initial-state!)
  (f)
  (app-db/reset-to-initial-state!))

(use-fixtures :each reset-app-db-fixture)

;; =============================================================================
;; Transit Encoding/Decoding Helpers
;; =============================================================================

(defn transit-roundtrip
  "Encode a command to Transit and decode it back."
  [command]
  (let [out (ByteArrayOutputStream.)
        writer (transit/make-writer out)]
    ;; Write the command
    (transit/write-message! writer command out)
    ;; Read it back
    (let [in (ByteArrayInputStream. (.toByteArray out))
          reader (transit/make-reader in)]
      (transit/read-message reader))))

(defn verify-command-structure
  "Verify a command has the expected nested structure."
  [command expected-path]
  (let [value (get-in command expected-path)]
    (is (some? value) 
        (str "Command missing expected path: " expected-path " in " command))
    value))

;; =============================================================================
;; Basic Commands Tests
;; =============================================================================

(deftest test-basic-commands
  (testing "Ping command"
    (let [cmd (cmd/ping)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (:ping cmd)))
      (verify-command-structure cmd [:ping])))
  
  (testing "Noop command"
    (let [cmd (cmd/noop)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (:noop cmd)))
      (verify-command-structure cmd [:noop])))
  
  (testing "Frozen command"
    (let [cmd (cmd/frozen)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (:frozen cmd)))
      (verify-command-structure cmd [:frozen]))))

;; =============================================================================
;; System Commands Tests
;; =============================================================================

(deftest test-system-commands
  (testing "Set localization"
    (let [cmd-en (cmd/set-localization "en")
          cmd-uk (cmd/set-localization "uk")
          roundtrip-en (transit-roundtrip cmd-en)
          roundtrip-uk (transit-roundtrip cmd-uk)]
      ;; Check that values are preserved correctly
      (is (= "en" (get-in cmd-en [:system :localization :loc])))
      (is (= "uk" (get-in cmd-uk [:system :localization :loc])))
      (is (= cmd-en roundtrip-en))
      (is (= cmd-uk roundtrip-uk))))
  
  (testing "Set recording"
    (let [cmd-start (cmd/set-recording true)
          cmd-stop (cmd/set-recording false)
          roundtrip-start (transit-roundtrip cmd-start)
          roundtrip-stop (transit-roundtrip cmd-stop)]
      (is (= cmd-start roundtrip-start))
      (is (= cmd-stop roundtrip-stop))
      ;; Check that different commands are generated
      (is (contains? (:system cmd-start) :start-rec))
      (is (contains? (:system cmd-stop) :stop-rec))
      (is (= {} (get-in cmd-start [:system :start-rec])))
      (is (= {} (get-in cmd-stop [:system :stop-rec]))))))

;; =============================================================================
;; GPS Commands Tests
;; =============================================================================

(deftest test-gps-commands
  (testing "Set GPS manual - enable manual mode"
    (let [cmd (cmd/set-gps-manual {:use-manual true})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= true (get-in cmd [:gps :set-use-manual-position :flag])))))
  
  (testing "Set GPS manual - disable manual mode"
    (let [cmd (cmd/set-gps-manual {:use-manual false})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= false (get-in cmd [:gps :set-use-manual-position :flag])))))
  
  (testing "Set GPS manual - with coordinates"
    (let [cmd (cmd/set-gps-manual {:use-manual true
                                    :latitude 51.5074
                                    :longitude -0.1278
                                    :altitude 100.0})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      ;; When use-manual is true, it should just set the flag
      (is (contains? (:gps cmd) :set-use-manual-position))
      (is (= true (get-in cmd [:gps :set-use-manual-position :flag]))))))

;; =============================================================================
;; Compass Commands Tests
;; =============================================================================

(deftest test-compass-commands
  (testing "Set compass unit"
    ;; Note: This is a placeholder command as noted in the implementation
    (let [cmd-degrees (cmd/set-compass-unit "degrees")
          cmd-mils (cmd/set-compass-unit "mils")]
      (is (contains? (:compass cmd-degrees) :calibrate))
      (is (contains? (:compass cmd-mils) :calibrate)))))

;; =============================================================================
;; LRF Commands Tests
;; =============================================================================

(deftest test-lrf-commands
  (testing "LRF single measurement"
    (let [cmd (cmd/lrf-single-measurement)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:lrf :measure])))))
  
  (testing "LRF continuous start"
    (let [cmd (cmd/lrf-continuous-start)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:lrf :scan-on])))))
  
  (testing "LRF continuous stop"
    (let [cmd (cmd/lrf-continuous-stop)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:lrf :scan-off]))))))

;; =============================================================================
;; Rotary Platform Commands Tests
;; =============================================================================

(deftest test-rotary-commands
  (testing "Rotary goto"
    (let [cmd (cmd/rotary-goto {:azimuth 180.0 :elevation 45.0})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= 180.0 (get-in cmd [:rotary :goto :azimuth])))
      (is (= 45.0 (get-in cmd [:rotary :goto :elevation])))))
  
  (testing "Rotary stop"
    (let [cmd (cmd/rotary-stop)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:rotary :stop])))))
  
  (testing "Rotary halt"
    (let [cmd (cmd/rotary-halt)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:rotary :halt])))))
  
  (testing "Rotary goto NDC"
    (let [cmd (cmd/rotary-goto-ndc :heat 0.5 -0.3)
          roundtrip (transit-roundtrip cmd)]
      ;; Check values are preserved correctly
      (is (= "heat" (get-in cmd [:rotary :goto-ndc :channel])))
      (is (= 0.5 (get-in cmd [:rotary :goto-ndc :x])))
      (is (= -0.3 (get-in cmd [:rotary :goto-ndc :y])))
      (is (= cmd roundtrip))))
  
  (testing "Rotary set velocity"
    (let [cmd (cmd/rotary-set-velocity 10.0 5.0 :clockwise :counter-clockwise)
          roundtrip (transit-roundtrip cmd)]
      ;; Check values are preserved correctly
      (is (= 10.0 (get-in cmd [:rotary :set-velocity :azimuth-speed])))
      (is (= 5.0 (get-in cmd [:rotary :set-velocity :elevation-speed])))
      (is (= "clockwise" (get-in cmd [:rotary :set-velocity :azimuth-direction])))
      (is (= "counter-clockwise" (get-in cmd [:rotary :set-velocity :elevation-direction])))
      (is (= cmd roundtrip)))))

;; =============================================================================
;; Computer Vision Commands Tests
;; =============================================================================

(deftest test-cv-commands
  (testing "CV start track NDC without timestamp"
    (let [cmd (cmd/cv-start-track-ndc :day 0.2 0.7 nil)
          roundtrip (transit-roundtrip cmd)]
      ;; Check values are preserved correctly
      (is (= "day" (get-in cmd [:cv :start-track-ndc :channel])))
      (is (= 0.2 (get-in cmd [:cv :start-track-ndc :x])))
      (is (= 0.7 (get-in cmd [:cv :start-track-ndc :y])))
      (is (nil? (get-in cmd [:cv :start-track-ndc :frame-time])))
      (is (= cmd roundtrip))))
  
  (testing "CV start track NDC with timestamp"
    (let [timestamp 1234567890
          cmd (cmd/cv-start-track-ndc :heat -0.5 0.8 timestamp)
          roundtrip (transit-roundtrip cmd)]
      ;; Check values are preserved correctly
      (is (= "heat" (get-in cmd [:cv :start-track-ndc :channel])))
      (is (= -0.5 (get-in cmd [:cv :start-track-ndc :x])))
      (is (= 0.8 (get-in cmd [:cv :start-track-ndc :y])))
      (is (= timestamp (get-in cmd [:cv :start-track-ndc :frame-time])))
      (is (= cmd roundtrip)))))

;; =============================================================================
;; Day Camera Commands Tests
;; =============================================================================

(deftest test-day-camera-commands
  (testing "Day camera zoom"
    (let [cmd (cmd/day-camera-zoom 25.5)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= 25.5 (get-in cmd [:day-camera :zoom :set-value :value])))))
  
  (testing "Day camera focus - auto"
    (let [cmd (cmd/day-camera-focus {:mode "auto"})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:day-camera :focus :reset-focus])))))
  
  (testing "Day camera focus - manual"
    (let [cmd (cmd/day-camera-focus {:mode "manual" :distance 50.0})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= 50.0 (get-in cmd [:day-camera :focus :set-value :value])))))
  
  (testing "Day camera focus - infinity"
    (let [cmd (cmd/day-camera-focus {:mode "infinity"})
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= 999999.0 (get-in cmd [:day-camera :focus :set-value :value])))))
  
  (testing "Day camera photo"
    (let [cmd (cmd/day-camera-photo)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:day-camera :photo]))))))

;; =============================================================================
;; Heat Camera Commands Tests
;; =============================================================================

(deftest test-heat-camera-commands
  (testing "Heat camera zoom"
    (let [cmd (cmd/heat-camera-zoom 4.0)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= 4.0 (get-in cmd [:heat-camera :zoom :set-value :value])))))
  
  (testing "Heat camera calibrate"
    (let [cmd (cmd/heat-camera-calibrate)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:heat-camera :nuc])))))
  
  (testing "Heat camera palette"
    (let [palettes ["white-hot" "black-hot" "rainbow" "ironbow" "lava" "arctic"]
          expected-indices [0 1 2 3 4 5]]
      (doseq [[palette index] (map vector palettes expected-indices)]
        (testing (str "Palette: " palette)
          (let [cmd (cmd/heat-camera-palette palette)
                roundtrip (transit-roundtrip cmd)]
            (is (= cmd roundtrip))
            (is (= index (get-in cmd [:heat-camera :set-color-palette :index]))))))))
  
  (testing "Heat camera photo"
    (let [cmd (cmd/heat-camera-photo)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:heat-camera :photo]))))))

;; =============================================================================
;; Glass Heater Commands Tests
;; =============================================================================

(deftest test-glass-heater-commands
  (testing "Glass heater on"
    (let [cmd (cmd/glass-heater-on)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:day-cam-glass-heater :turn-on])))))
  
  (testing "Glass heater off"
    (let [cmd (cmd/glass-heater-off)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:day-cam-glass-heater :turn-off]))))))

;; =============================================================================
;; OSD Commands Tests
;; =============================================================================

(deftest test-osd-commands
  (testing "OSD enable day"
    (let [cmd (cmd/osd-enable-day)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:osd :enable-day-osd])))))
  
  (testing "OSD disable day"
    (let [cmd (cmd/osd-disable-day)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:osd :disable-day-osd])))))
  
  (testing "OSD enable heat"
    (let [cmd (cmd/osd-enable-heat)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:osd :enable-heat-osd])))))
  
  (testing "OSD disable heat"
    (let [cmd (cmd/osd-disable-heat)
          roundtrip (transit-roundtrip cmd)]
      (is (= cmd roundtrip))
      (is (= {} (get-in cmd [:osd :disable-heat-osd]))))))

;; =============================================================================
;; Integration Tests
;; =============================================================================

(deftest test-command-envelope-integration
  (testing "Commands wrapped with Transit message envelope"
    (let [cmd (cmd/ping)
          message (transit/create-message :command cmd)
          roundtrip (transit-roundtrip message)]
      (is (= :command (:msg-type message)))
      (is (string? (:msg-id message)))
      (is (number? (:timestamp message)))
      (is (= cmd (:payload message)))
      (is (= message roundtrip)))))

(deftest test-all-commands-valid-structure
  (testing "All commands produce valid nested structures"
    (let [test-cases
          [;; Basic commands
           [(cmd/ping) [:ping]]
           [(cmd/noop) [:noop]]
           [(cmd/frozen) [:frozen]]
           
           ;; System commands
           [(cmd/set-localization "en") [:system :localization]]
           [(cmd/set-recording true) [:system :start-rec]]
           [(cmd/set-recording false) [:system :stop-rec]]
           
           ;; GPS commands
           [(cmd/set-gps-manual {:use-manual true}) [:gps :set-use-manual-position]]
           
           ;; Compass commands
           [(cmd/set-compass-unit "degrees") [:compass :calibrate]]
           
           ;; LRF commands
           [(cmd/lrf-single-measurement) [:lrf :measure]]
           [(cmd/lrf-continuous-start) [:lrf :scan-on]]
           [(cmd/lrf-continuous-stop) [:lrf :scan-off]]
           
           ;; Rotary commands
           [(cmd/rotary-goto {:azimuth 90.0 :elevation 30.0}) [:rotary :goto]]
           [(cmd/rotary-stop) [:rotary :stop]]
           [(cmd/rotary-halt) [:rotary :halt]]
           [(cmd/rotary-goto-ndc :heat 0.5 0.5) [:rotary :goto-ndc]]
           [(cmd/rotary-set-velocity 5.0 5.0 :clockwise :clockwise) [:rotary :set-velocity]]
           
           ;; CV commands
           [(cmd/cv-start-track-ndc :day 0.0 0.0 nil) [:cv :start-track-ndc]]
           
           ;; Day camera commands
           [(cmd/day-camera-zoom 10.0) [:day-camera :zoom]]
           [(cmd/day-camera-focus {:mode "auto"}) [:day-camera :focus]]
           [(cmd/day-camera-photo) [:day-camera :photo]]
           
           ;; Heat camera commands
           [(cmd/heat-camera-zoom 2.0) [:heat-camera :zoom]]
           [(cmd/heat-camera-calibrate) [:heat-camera :nuc]]
           [(cmd/heat-camera-palette "white-hot") [:heat-camera :set-color-palette]]
           [(cmd/heat-camera-photo) [:heat-camera :photo]]
           
           ;; Glass heater commands
           [(cmd/glass-heater-on) [:day-cam-glass-heater :turn-on]]
           [(cmd/glass-heater-off) [:day-cam-glass-heater :turn-off]]
           
           ;; OSD commands
           [(cmd/osd-enable-day) [:osd :enable-day-osd]]
           [(cmd/osd-disable-day) [:osd :disable-day-osd]]
           [(cmd/osd-enable-heat) [:osd :enable-heat-osd]]
           [(cmd/osd-disable-heat) [:osd :disable-heat-osd]]]]
      
      (doseq [[command expected-path] test-cases]
        (testing (str "Command path: " expected-path)
          (is (some? (get-in command expected-path))
              (str "Missing path " expected-path " in command " command)))))))

;; =============================================================================
;; Performance Test
;; =============================================================================

(deftest test-transit-encoding-performance
  (testing "Transit encoding/decoding performance"
    (let [commands [(cmd/ping)
                    (cmd/rotary-goto {:azimuth 180.0 :elevation 45.0})
                    (cmd/cv-start-track-ndc :heat 0.5 0.5 1234567890)
                    (cmd/heat-camera-palette "rainbow")]
          iterations 1000
          start (System/currentTimeMillis)]
      
      (dotimes [_ iterations]
        (doseq [cmd commands]
          (transit-roundtrip cmd)))
      
      (let [end (System/currentTimeMillis)
            duration (- end start)
            ops-per-sec (/ (* iterations (count commands)) (/ duration 1000.0))]
        (println (format "Transit roundtrip performance: %.0f ops/sec" ops-per-sec))
        ;; Ensure reasonable performance (at least 1k ops/sec in test environment)
        (is (> ops-per-sec 1000) "Transit encoding/decoding should achieve at least 1k ops/sec")))))

(comment
  ;; Run specific tests
  (test-basic-commands)
  (test-rotary-commands)
  (test-all-commands-valid-structure)
  
  ;; Check a specific command
  (cmd/rotary-goto {:azimuth 90.0 :elevation 30.0})
  ;; => {:rotary {:goto {:azimuth 90.0, :elevation 30.0}}}
  
  ;; Test Transit roundtrip
  (transit-roundtrip (cmd/ping))
  ;; => {:ping {}}
  )