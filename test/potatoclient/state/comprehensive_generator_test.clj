(ns potatoclient.state.comprehensive-generator-test
  "Comprehensive generator-based tests for ALL state subsystems"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.core.async :as async :refer [<! >! go go-loop timeout]]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.proto :as proto]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.state.device :as device]
            [potatoclient.state.schemas :as schemas]
            [potatoclient.logging :as logging])
  (:import [ser JonSharedData$JonGUIState
            JonSharedDataSystem$JonGuiDataSystem
            JonSharedDataGps$JonGuiDataGps
            JonSharedDataCompass$JonGuiDataCompass
            JonSharedDataLrf$JonGuiDataLrf
            JonSharedDataRotary$JonGuiDataRotary
            JonSharedDataRotary$ScanNode
            JonSharedDataCameraDay$JonGuiDataCameraDay
            JonSharedDataCameraHeat$JonGuiDataCameraHeat
            JonSharedDataTime$JonGuiDataTime
            JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
            JonSharedDataRecOsd$JonGuiDataRecOsd
            JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
            JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
            JonSharedDataTypes$JonGuiDataGpsFixType
            JonSharedDataTypes$JonGuiDataMeteo
            JonSharedDataTypes$JonGuiDataSystemLocalizations
            JonSharedDataTypes$JonGuiDataRotaryMode
            JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes
            JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes
            JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters
            JonSharedDataTypes$JonGuiDataFxModeDay
            JonSharedDataTypes$JonGuiDataFxModeHeat
            JonSharedDataTypes$JonGuiDataCompassCalibrateStatus
            JonSharedDataTypes$JonGuiDataTimeFormats
            JonSharedDataTypes$JonGuiDataRecOsdScreen]))

;; ============================================================================
;; Test Fixtures
;; ============================================================================

(defn reset-state-fixture
  "Reset all state before and after tests"
  [f]
  (device/reset-all-states!)
  (dispatch/enable-validation! false)
  (dispatch/enable-debug! false)
  (f)
  (device/reset-all-states!)
  (dispatch/dispose!))

(use-fixtures :each reset-state-fixture)

;; ============================================================================
;; Protobuf Builder Utilities
;; ============================================================================

(defn set-system-fields!
  "Set all fields on a System builder from EDN data"
  [builder data]
  (doto builder
    (.setCpuTemperature (:cpu-temperature data))
    (.setGpuTemperature (:gpu-temperature data))
    (.setCpuLoad (:cpu-load data))
    (.setGpuLoad (:gpu-load data))
    (.setPowerConsumption (:power-consumption data))
    (.setLoc (JonSharedDataTypes$JonGuiDataSystemLocalizations/valueOf (:loc data)))
    (.setCurVideoRecDirYear (:cur-video-rec-dir-year data))
    (.setCurVideoRecDirMonth (:cur-video-rec-dir-month data))
    (.setCurVideoRecDirDay (:cur-video-rec-dir-day data))
    (.setCurVideoRecDirHour (:cur-video-rec-dir-hour data))
    (.setCurVideoRecDirMinute (:cur-video-rec-dir-minute data))
    (.setCurVideoRecDirSecond (:cur-video-rec-dir-second data))
    (.setRecEnabled (:rec-enabled data))
    (.setImportantRecEnabled (:important-rec-enabled data))
    (.setLowDiskSpace (:low-disk-space data))
    (.setNoDiskSpace (:no-disk-space data))
    (.setDiskSpace (:disk-space data))
    (.setTracking (:tracking data))
    (.setVampireMode (:vampire-mode data))
    (.setStabilizationMode (:stabilization-mode data))
    (.setGeodesicMode (:geodesic-mode data))
    (.setCvDumping (:cv-dumping data))))

(defn set-gps-fields!
  "Set all fields on a GPS builder from EDN data"
  [builder data]
  (doto builder
    (.setLongitude (:longitude data))
    (.setLatitude (:latitude data))
    (.setAltitude (:altitude data))
    (.setManualLongitude (:manual-longitude data))
    (.setManualLatitude (:manual-latitude data))
    (.setManualAltitude (:manual-altitude data))
    (.setFixType (JonSharedDataTypes$JonGuiDataGpsFixType/valueOf (:fix-type data)))
    (.setUseManual (:use-manual data))))

(defn set-compass-fields!
  "Set all fields on a Compass builder from EDN data"
  [builder data]
  (doto builder
    (.setAzimuth (:azimuth data))
    (.setElevation (:elevation data))
    (.setBank (:bank data))
    (.setOffsetAzimuth (:offset-azimuth data))
    (.setOffsetElevation (:offset-elevation data))
    (.setMagneticDeclination (:magnetic-declination data))
    (.setCalibrating (:calibrating data))))

(defn set-lrf-fields!
  "Set all fields on an LRF builder from EDN data"
  [builder data]
  (doto builder
    (.setIsScanning (:is-scanning data))
    (.setIsMeasuring (:is-measuring data))
    (.setMeasureId (:measure-id data))
    (.setPointerMode (JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes/valueOf (:pointer-mode data)))
    (.setFogModeEnabled (:fog-mode-enabled data))
    (.setIsRefining (:is-refining data))
    ;; Target is optional and complex - skip for now
    ))

(defn set-time-fields!
  "Set all fields on a Time builder from EDN data"
  [builder data]
  (doto builder
    (.setTimestamp (:timestamp data))
    (.setManualTimestamp (:manual-timestamp data))
    (.setZoneId (:zone-id data))
    (.setUseManualTime (:use-manual-time data))))

(defn set-scan-node-fields!
  "Set fields on a ScanNode builder from EDN data"
  [builder data]
  (doto builder
    (.setIndex (:index data))
    (.setDayZoomTableValue (:day-zoom-table-value data))
    (.setHeatZoomTableValue (:heat-zoom-table-value data))
    (.setAzimuth (:azimuth data))
    (.setElevation (:elevation data))
    (.setLinger (:linger data))
    (.setSpeed (:speed data))))

(defn set-rotary-fields!
  "Set all fields on a Rotary builder from EDN data"
  [builder data]
  (doto builder
    (.setAzimuth (:azimuth data))
    (.setAzimuthSpeed (:azimuth-speed data))
    (.setElevation (:elevation data))
    (.setElevationSpeed (:elevation-speed data))
    (.setPlatformAzimuth (:platform-azimuth data))
    (.setPlatformElevation (:platform-elevation data))
    (.setPlatformBank (:platform-bank data))
    (.setIsMoving (:is-moving data))
    (.setMode (JonSharedDataTypes$JonGuiDataRotaryMode/valueOf (:mode data)))
    (.setIsScanning (:is-scanning data))
    (.setIsScanningPaused (:is-scanning-paused data))
    (.setUseRotaryAsCompass (:use-rotary-as-compass data))
    (.setScanTarget (:scan-target data))
    (.setScanTargetMax (:scan-target-max data))
    (.setSunAzimuth (:sun-azimuth data))
    (.setSunElevation (:sun-elevation data))
    ;; Set current-scan-node if present
    (cond-> (:current-scan-node data)
      (.setCurrentScanNode (set-scan-node-fields!
                             (JonSharedDataRotary$ScanNode/newBuilder)
                             (:current-scan-node data))))))

(defn set-camera-day-fields!
  "Set all fields on a Day Camera builder from EDN data"
  [builder data]
  (doto builder
    (.setFocusPos (:focus-pos data))
    (.setZoomPos (:zoom-pos data))
    (.setIrisPos (:iris-pos data))
    (.setInfraredFilter (:infrared-filter data))
    (.setZoomTablePos (:zoom-table-pos data))
    (.setZoomTablePosMax (:zoom-table-pos-max data))
    (.setFxMode (JonSharedDataTypes$JonGuiDataFxModeDay/valueOf (:fx-mode data)))
    (.setAutoFocus (:auto-focus data))
    (.setAutoIris (:auto-iris data))
    (.setDigitalZoomLevel (:digital-zoom-level data))
    (.setClaheLevel (:clahe-level data))))

(defn set-camera-heat-fields!
  "Set all fields on a Heat Camera builder from EDN data"
  [builder data]
  (doto builder
    (.setZoomPos (:zoom-pos data))
    (.setAgcMode (JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/valueOf (:agc-mode data)))
    (.setFilter (JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/valueOf (:filter data)))
    (.setAutoFocus (:auto-focus data))
    (.setZoomTablePos (:zoom-table-pos data))
    (.setZoomTablePosMax (:zoom-table-pos-max data))
    (.setDdeLevel (:dde-level data))
    (.setDdeEnabled (:dde-enabled data))
    (.setFxMode (JonSharedDataTypes$JonGuiDataFxModeHeat/valueOf (:fx-mode data)))
    (.setDigitalZoomLevel (:digital-zoom-level data))
    (.setClaheLevel (:clahe-level data))))

(defn set-compass-calibration-fields!
  "Set all fields on a Compass Calibration builder from EDN data"
  [builder data]
  (doto builder
    (.setStatus (JonSharedDataTypes$JonGuiDataCompassCalibrateStatus/valueOf (:status data)))
    (cond-> (:progress data)
      (.setProgress (:progress data)))))

(defn set-rec-osd-fields!
  "Set all fields on a Rec/OSD builder from EDN data"
  [builder data]
  (doto builder
    (.setHeatOsdEnabled (:heat-osd-enabled data))
    (.setDayOsdEnabled (:day-osd-enabled data))
    (.setScreen (JonSharedDataTypes$JonGuiDataRecOsdScreen/valueOf (:screen data)))
    (.setHeatCrosshairOffsetHorizontal (:heat-crosshair-offset-horizontal data))
    (.setHeatCrosshairOffsetVertical (:heat-crosshair-offset-vertical data))
    (.setDayCrosshairOffsetHorizontal (:day-crosshair-offset-horizontal data))
    (.setDayCrosshairOffsetVertical (:day-crosshair-offset-vertical data))))

(defn set-glass-heater-fields!
  "Set all fields on a Glass Heater builder from EDN data"
  [builder data]
  (doto builder
    (.setStatus (:status data))
    (cond-> (:temperature data)
      (.setTemperature (:temperature data)))))

(defn set-meteo-fields!
  "Set all fields on a Meteo builder from EDN data"
  [builder data]
  (doto builder
    (.setTemperature (:temperature data))
    (.setHumidity (:humidity data))
    (.setPressure (:pressure data))))

(defn edn->protobuf-state
  "Convert complete EDN state to protobuf JonGUIState"
  [state-map]
  (let [builder (JonSharedData$JonGUIState/newBuilder)]
    (.setProtocolVersion builder (:protocol-version state-map))

    ;; Set each subsystem if present
    (when-let [data (:system state-map)]
      (.setSystem builder (set-system-fields! (JonSharedDataSystem$JonGuiDataSystem/newBuilder) data)))

    (when-let [data (:gps state-map)]
      (.setGps builder (set-gps-fields! (JonSharedDataGps$JonGuiDataGps/newBuilder) data)))

    (when-let [data (:compass state-map)]
      (.setCompass builder (set-compass-fields! (JonSharedDataCompass$JonGuiDataCompass/newBuilder) data)))

    (when-let [data (:lrf state-map)]
      (.setLrf builder (set-lrf-fields! (JonSharedDataLrf$JonGuiDataLrf/newBuilder) data)))

    (when-let [data (:time state-map)]
      (.setTime builder (set-time-fields! (JonSharedDataTime$JonGuiDataTime/newBuilder) data)))

    (when-let [data (:rotary state-map)]
      (.setRotary builder (set-rotary-fields! (JonSharedDataRotary$JonGuiDataRotary/newBuilder) data)))

    (when-let [data (:camera-day state-map)]
      (.setCameraDay builder (set-camera-day-fields! (JonSharedDataCameraDay$JonGuiDataCameraDay/newBuilder) data)))

    (when-let [data (:camera-heat state-map)]
      (.setCameraHeat builder (set-camera-heat-fields! (JonSharedDataCameraHeat$JonGuiDataCameraHeat/newBuilder) data)))

    (when-let [data (:compass-calibration state-map)]
      (.setCompassCalibration builder (set-compass-calibration-fields! (JonSharedDataCompassCalibration$JonGuiDataCompassCalibration/newBuilder) data)))

    (when-let [data (:rec-osd state-map)]
      (.setRecOsd builder (set-rec-osd-fields! (JonSharedDataRecOsd$JonGuiDataRecOsd/newBuilder) data)))

    (when-let [data (:day-cam-glass-heater state-map)]
      (.setDayCamGlassHeater builder (set-glass-heater-fields! (JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater/newBuilder) data)))

    (when-let [data (:meteo-internal state-map)]
      (.setMeteoInternal builder (set-meteo-fields! (JonSharedDataTypes$JonGuiDataMeteo/newBuilder) data)))

    (.build builder)))

(defn create-test-websocket-message
  "Create a binary WebSocket message from EDN state"
  [state-map]
  (.toByteArray (edn->protobuf-state state-map)))

(defn mock-websocket-flow
  "Simulate complete WebSocket flow"
  [state-map]
  (let [binary-msg (create-test-websocket-message state-map)]
    (dispatch/handle-binary-state binary-msg)))

;; ============================================================================
;; Individual Subsystem Tests
;; ============================================================================

(deftest test-system-subsystem
  (testing "System subsystem with all fields"
    (let [system-data (mg/generate schemas/system-schema)
          state-map {:protocol-version 1 :system system-data}]

      (is (schemas/validate-subsystem :system system-data)
          "Generated system data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/system-state) "System state should be set")

      ;; Check specific fields with float tolerance
      (when @device/system-state
        (is (< (Math/abs (- (:cpu-temperature system-data)
                            (:cpu-temperature @device/system-state)))
               0.00001)
            "CPU temperature should match")
        (is (= (:loc system-data) (:loc @device/system-state))
            "Localization should match")
        (is (= (:rec-enabled system-data) (:rec-enabled @device/system-state))
            "Recording enabled should match")))))

(deftest test-gps-subsystem
  (testing "GPS subsystem with all fields"
    (let [gps-data (mg/generate schemas/gps-schema)
          state-map {:protocol-version 1 :gps gps-data}]

      (is (schemas/validate-subsystem :gps gps-data)
          "Generated GPS data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/gps-state) "GPS state should be set")

      (when @device/gps-state
        (is (< (Math/abs (- (:latitude gps-data) (:latitude @device/gps-state)))
               0.00001)
            "Latitude should match")
        (is (= (:fix-type gps-data) (:fix-type @device/gps-state))
            "Fix type should match")
        (is (= (:use-manual gps-data) (:use-manual @device/gps-state))
            "Use manual should match")))))

(deftest test-compass-subsystem
  (testing "Compass subsystem with all fields"
    (let [compass-data (mg/generate schemas/compass-schema)
          state-map {:protocol-version 1 :compass compass-data}]

      (is (schemas/validate-subsystem :compass compass-data)
          "Generated compass data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/compass-state) "Compass state should be set")

      (when @device/compass-state
        (is (< (Math/abs (- (:azimuth compass-data) (:azimuth @device/compass-state)))
               0.00001)
            "Azimuth should match")
        (is (< (Math/abs (- (:elevation compass-data) (:elevation @device/compass-state)))
               0.00001)
            "Elevation should match")
        (is (= (:calibrating compass-data) (:calibrating @device/compass-state))
            "Calibrating should match")))))

(deftest test-lrf-subsystem
  (testing "LRF subsystem with all fields"
    (let [lrf-data (mg/generate schemas/lrf-schema)
          ;; Remove optional target for simplicity
          lrf-data (dissoc lrf-data :target)
          state-map {:protocol-version 1 :lrf lrf-data}]

      (is (schemas/validate-subsystem :lrf lrf-data)
          "Generated LRF data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/lrf-state) "LRF state should be set")

      (when @device/lrf-state
        (is (= (:is-scanning lrf-data) (:is-scanning @device/lrf-state))
            "Scanning state should match")
        (is (= (:pointer-mode lrf-data) (:pointer-mode @device/lrf-state))
            "Pointer mode should match")))))

(deftest test-time-subsystem
  (testing "Time subsystem with all fields"
    (let [time-data (mg/generate schemas/time-schema)
          state-map {:protocol-version 1 :time time-data}]

      (is (schemas/validate-subsystem :time time-data)
          "Generated time data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/time-state) "Time state should be set")

      (when @device/time-state
        (is (= (:timestamp time-data) (:timestamp @device/time-state))
            "Timestamp should match")
        (is (= (:manual-timestamp time-data) (:manual-timestamp @device/time-state))
            "Manual timestamp should match")
        (is (= (:zone-id time-data) (:zone-id @device/time-state))
            "Zone ID should match")
        (is (= (:use-manual-time time-data) (:use-manual-time @device/time-state))
            "Use manual time should match")))))

(deftest test-rotary-subsystem
  (testing "Rotary subsystem with all fields"
    (let [rotary-data (mg/generate schemas/rotary-schema)
          state-map {:protocol-version 1 :rotary rotary-data}]

      (is (schemas/validate-subsystem :rotary rotary-data)
          "Generated rotary data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/rotary-state) "Rotary state should be set")

      (when @device/rotary-state
        (is (< (Math/abs (- (:azimuth rotary-data) (:azimuth @device/rotary-state)))
               0.00001)
            "Azimuth should match")
        (is (= (:mode rotary-data) (:mode @device/rotary-state))
            "Mode should match")
        (is (= (:is-scanning rotary-data) (:is-scanning @device/rotary-state))
            "Scanning state should match")))))

(deftest test-camera-day-subsystem
  (testing "Day camera subsystem with all fields"
    (let [camera-data (mg/generate schemas/camera-day-schema)
          state-map {:protocol-version 1 :camera-day camera-data}]

      (is (schemas/validate-subsystem :camera-day camera-data)
          "Generated camera data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/camera-day-state) "Day camera state should be set")

      (when @device/camera-day-state
        (is (< (Math/abs (- (:focus-pos camera-data) (:focus-pos @device/camera-day-state)))
               0.00001)
            "Focus position should match")
        (is (= (:fx-mode camera-data) (:fx-mode @device/camera-day-state))
            "FX mode should match")
        (is (= (:auto-focus camera-data) (:auto-focus @device/camera-day-state))
            "Auto focus should match")))))

(deftest test-camera-heat-subsystem
  (testing "Heat camera subsystem with all fields"
    (let [camera-data (mg/generate schemas/camera-heat-schema)
          state-map {:protocol-version 1 :camera-heat camera-data}]

      (is (schemas/validate-subsystem :camera-heat camera-data)
          "Generated camera data should be valid")

      (mock-websocket-flow state-map)

      (is (some? @device/camera-heat-state) "Heat camera state should be set")

      (when @device/camera-heat-state
        (is (< (Math/abs (- (:zoom-pos camera-data) (:zoom-pos @device/camera-heat-state)))
               0.00001)
            "Zoom position should match")
        (is (= (:agc-mode camera-data) (:agc-mode @device/camera-heat-state))
            "AGC mode should match")
        (is (= (:dde-enabled camera-data) (:dde-enabled @device/camera-heat-state))
            "DDE enabled should match")))))

;; ============================================================================
;; Combined Subsystem Tests
;; ============================================================================

(deftest test-multiple-subsystems-update
  (testing "Multiple subsystems update simultaneously"
    (let [system-data (mg/generate schemas/system-schema)
          gps-data (mg/generate schemas/gps-schema)
          compass-data (mg/generate schemas/compass-schema)
          rotary-data (dissoc (mg/generate schemas/rotary-schema) :current-scan-node)
          state-map {:protocol-version 1
                     :system system-data
                     :gps gps-data
                     :compass compass-data
                     :rotary rotary-data}]

      (mock-websocket-flow state-map)

      ;; All subsystems should be updated
      (is (some? @device/system-state) "System state should be set")
      (is (some? @device/gps-state) "GPS state should be set")
      (is (some? @device/compass-state) "Compass state should be set")
      (is (some? @device/rotary-state) "Rotary state should be set")

      ;; Verify no other subsystems were touched
      (is (nil? @device/lrf-state) "LRF state should remain nil")
      (is (nil? @device/camera-day-state) "Day camera state should remain nil")
      (is (nil? @device/camera-heat-state) "Heat camera state should remain nil"))))

(deftest test-full-state-update
  (testing "Complete state with all subsystems"
    (let [;; Generate minimal valid data for all subsystems
          full-state {:protocol-version 1
                      :system (mg/generate schemas/system-schema)
                      :gps (mg/generate schemas/gps-schema)
                      :compass (mg/generate schemas/compass-schema)
                      :lrf (dissoc (mg/generate schemas/lrf-schema) :target)
                      :time (mg/generate schemas/time-schema)
                      :rotary (dissoc (mg/generate schemas/rotary-schema) :current-scan-node)
                      :camera-day (mg/generate schemas/camera-day-schema)
                      :camera-heat (mg/generate schemas/camera-heat-schema)
                      :compass-calibration (mg/generate schemas/compass-calibration-schema)
                      :rec-osd (mg/generate schemas/rec-osd-schema)
                      :day-cam-glass-heater (mg/generate schemas/day-cam-glass-heater-schema)
                      :meteo-internal (mg/generate schemas/meteo-schema)}]

      (mock-websocket-flow full-state)

      ;; All main subsystems should be updated
      (is (some? @device/system-state) "System state should be set")
      (is (some? @device/gps-state) "GPS state should be set")
      (is (some? @device/compass-state) "Compass state should be set")
      (is (some? @device/lrf-state) "LRF state should be set")
      (is (some? @device/time-state) "Time state should be set")
      (is (some? @device/rotary-state) "Rotary state should be set")
      (is (some? @device/camera-day-state) "Day camera state should be set")
      (is (some? @device/camera-heat-state) "Heat camera state should be set"))))

;; ============================================================================
;; Property-Based Tests
;; ============================================================================

(deftest test-property-all-generated-subsystems-valid
  (testing "All generated subsystems are valid according to schemas"
    (doseq [[subsystem-key schema] schemas/all-schemas]
      (testing (str "Subsystem: " subsystem-key)
        (dotimes [_ 5]
          (let [data (mg/generate schema)]
            (is (schemas/validate-subsystem subsystem-key data)
                (str "Generated " subsystem-key " should be valid: "
                     (m/explain schema data)))))))))

(deftest test-property-state-channel-distribution
  (testing "State changes are distributed through channels"
    (let [channel (dispatch/get-state-channel)
          received (atom [])]

      ;; Start collecting states
      (go-loop []
        (when-let [state (<! channel)]
          (swap! received conj state)
          (recur)))

      ;; Send multiple state updates
      (dotimes [i 3]
        (let [state-map {:protocol-version 1
                         :system (assoc (mg/generate schemas/system-schema)
                                        :cpu-temperature (+ 50.0 i))}]
          (mock-websocket-flow state-map)
          (Thread/sleep 50)))

      ;; Wait for delivery
      (Thread/sleep 200)

      (is (>= (count @received) 3) "Should receive at least 3 state updates")
      (is (every? #(= 1 (:protocol-version %)) @received)
          "All states should have protocol version"))))

(deftest test-property-change-detection-optimization
  (testing "Identical states don't trigger atom updates"
    (let [state-map {:protocol-version 1
                     :system (mg/generate schemas/system-schema)}
          update-count (atom 0)]

      ;; Watch for changes
      (add-watch device/system-state ::test-watch
                 (fn [_ _ _ _] (swap! update-count inc)))

      ;; Send same state multiple times
      (dotimes [_ 5]
        (mock-websocket-flow state-map))

      ;; Should only update once
      (is (= 1 @update-count) "Atom should only update once for identical data")

      ;; Cleanup
      (remove-watch device/system-state ::test-watch))))

;; ============================================================================
;; Edge Case Tests
;; ============================================================================

(deftest test-extreme-values
  (testing "System handles extreme but valid values"
    (let [extreme-state {:protocol-version 1
                         :system {:cpu-temperature 149.9
                                  :gpu-temperature -273.0
                                  :cpu-load 0.0
                                  :gpu-load 100.0
                                  :power-consumption 999.9
                                  :loc "JON_GUI_DATA_SYSTEM_LOCALIZATION_EN"
                                  :cur-video-rec-dir-year 2024
                                  :cur-video-rec-dir-month 12
                                  :cur-video-rec-dir-day 31
                                  :cur-video-rec-dir-hour 23
                                  :cur-video-rec-dir-minute 59
                                  :cur-video-rec-dir-second 59
                                  :rec-enabled true
                                  :important-rec-enabled false
                                  :low-disk-space true
                                  :no-disk-space false
                                  :disk-space 0
                                  :tracking false
                                  :vampire-mode true
                                  :stabilization-mode false
                                  :geodesic-mode true
                                  :cv-dumping false}
                         :meteo-internal {:temperature 25.0
                                          :humidity 60.0
                                          :pressure 1013.25}
                         :lrf {:is-scanning false
                               :is-measuring false
                               :measure-id 0
                               :pointer-mode "JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
                               :fog-mode-enabled false
                               :is-refining false}
                         :time {:timestamp 9999999999
                                :manual-timestamp 0
                                :zone-id 12
                                :use-manual-time false}
                         :gps {:longitude 180.0
                               :latitude -90.0
                               :altitude 8848.0
                               :manual-longitude -180.0
                               :manual-latitude 90.0
                               :manual-altitude -433.0
                               :fix-type "JON_GUI_DATA_GPS_FIX_TYPE_3D"
                               :use-manual false}
                         :compass {:azimuth 359.999
                                   :elevation 90.0
                                   :bank -179.999
                                   :offset-azimuth 179.999
                                   :offset-elevation -90.0
                                   :magnetic-declination -180.0
                                   :calibrating true}
                         :rotary {:azimuth 0.0
                                  :azimuth-speed 0.0
                                  :elevation 0.0
                                  :elevation-speed 0.0
                                  :platform-azimuth 0.0
                                  :platform-elevation 0.0
                                  :platform-bank 0.0
                                  :is-moving false
                                  :mode "JON_GUI_DATA_ROTARY_MODE_INITIALIZATION"
                                  :is-scanning false
                                  :is-scanning-paused false
                                  :use-rotary-as-compass false
                                  :scan-target 0
                                  :scan-target-max 0
                                  :sun-azimuth 0.0
                                  :sun-elevation 0.0
                                  :current-scan-node {:index 0
                                                      :day-zoom-table-value 0
                                                      :heat-zoom-table-value 0
                                                      :azimuth 0.0
                                                      :elevation 0.0
                                                      :linger 0.0
                                                      :speed 0.5}}
                         :camera-day {:focus-pos 0.5
                                      :zoom-pos 0.5
                                      :iris-pos 0.5
                                      :infrared-filter false
                                      :zoom-table-pos 0
                                      :zoom-table-pos-max 10
                                      :fx-mode "JON_GUI_DATA_FX_MODE_DAY_DEFAULT"
                                      :auto-focus false
                                      :auto-iris false
                                      :digital-zoom-level 1.0
                                      :clahe-level 0.0}
                         :camera-heat {:zoom-pos 0.5
                                       :agc-mode "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1"
                                       :filter "JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE"
                                       :auto-focus false
                                       :zoom-table-pos 0
                                       :zoom-table-pos-max 10
                                       :dde-level 0
                                       :dde-enabled false
                                       :fx-mode "JON_GUI_DATA_FX_MODE_HEAT_DEFAULT"
                                       :digital-zoom-level 1.0
                                       :clahe-level 0.0}
                         :compass-calibration {:stage 0
                                               :final-stage 1
                                               :target-azimuth 0.0
                                               :target-elevation 0.0
                                               :target-bank 0.0
                                               :status "JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING"}
                         :rec-osd {:heat-osd-enabled false
                                   :day-osd-enabled false
                                   :screen "JON_GUI_DATA_REC_OSD_SCREEN_MAIN"
                                   :heat-crosshair-offset-horizontal 0
                                   :heat-crosshair-offset-vertical 0
                                   :day-crosshair-offset-horizontal 0
                                   :day-crosshair-offset-vertical 0}
                         :day-cam-glass-heater {:status false}
                         :actual-space-time {:timestamp 0}}]

      (is (schemas/validate-state extreme-state)
          (str "Extreme values should be valid. Validation errors: "
               (pr-str (schemas/explain-state extreme-state))))

      (mock-websocket-flow extreme-state)

      ;; Verify values are preserved
      (is (some? @device/system-state))
      (is (some? @device/gps-state))
      (is (some? @device/compass-state))

      (when @device/system-state
        (is (< (Math/abs (- 149.9 (:cpu-temperature @device/system-state))) 0.01)
            "Extreme CPU temp should be preserved"))

      (when @device/gps-state
        (is (< (Math/abs (- 180.0 (:longitude @device/gps-state))) 0.00001)
            "Extreme longitude should be preserved"))

      (when @device/compass-state
        (is (< (Math/abs (- 359.999 (:azimuth @device/compass-state))) 0.001)
            "Extreme azimuth should be preserved")))))