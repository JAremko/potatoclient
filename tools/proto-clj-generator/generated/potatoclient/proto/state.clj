(ns potatoclient.proto.state
  "Generated protobuf conversion functions."
  (:require [clojure.string :as str])
  (:import
    [ser JonSharedDataTypes$JonGuiDataMeteo JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes JonSharedDataTypes$JonGuiDataGpsUnits JonSharedDataTypes$JonGuiDataGpsFixType JonSharedDataTypes$JonGuiDataCompassUnits JonSharedDataTypes$JonGuiDataAccumulatorStateIdx JonSharedDataTypes$JonGuiDataTimeFormats JonSharedDataTypes$JonGuiDataRotaryDirection JonSharedDataTypes$JonGuiDataLrfScanModes JonSharedDataTypes$JonGuiDatatLrfLaserPointerModes JonSharedDataTypes$JonGuiDataCompassCalibrateStatus JonSharedDataTypes$JonGuiDataRotaryMode JonSharedDataTypes$JonGuiDataVideoChannel JonSharedDataTypes$JonGuiDataRecOsdScreen JonSharedDataTypes$JonGuiDataFxModeDay JonSharedDataTypes$JonGuiDataFxModeHeat JonSharedDataTypes$JonGuiDataSystemLocalizations JonSharedDataTypes$JonGuiDataClientType JonSharedDataTime$JonGuiDataTime JonSharedDataSystem$JonGuiDataSystem JonSharedDataLrf$JonGuiDataLrf JonSharedDataLrf$JonGuiDataTarget JonSharedDataLrf$RgbColor JonSharedDataGps$JonGuiDataGps JonSharedDataCompass$JonGuiDataCompass JonSharedDataCompassCalibration$JonGuiDataCompassCalibration JonSharedDataRotary$JonGuiDataRotary JonSharedDataRotary$ScanNode JonSharedDataCameraDay$JonGuiDataCameraDay JonSharedDataCameraHeat$JonGuiDataCameraHeat JonSharedDataRecOsd$JonGuiDataRecOsd JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime JonSharedData$JonGUIState]))

;; Forward declarations
(declare build-jon-gui-data-meteo parse-jon-gui-data-meteo build-jon-gui-data-time parse-jon-gui-data-time build-jon-gui-data-system parse-jon-gui-data-system build-jon-gui-data-lrf parse-jon-gui-data-lrf build-jon-gui-data-target parse-jon-gui-data-target build-rgb-color parse-rgb-color build-jon-gui-data-gps parse-jon-gui-data-gps build-jon-gui-data-compass parse-jon-gui-data-compass build-jon-gui-data-compass-calibration parse-jon-gui-data-compass-calibration build-jon-gui-data-rotary parse-jon-gui-data-rotary build-scan-node parse-scan-node build-jon-gui-data-camera-day parse-jon-gui-data-camera-day build-jon-gui-data-camera-heat parse-jon-gui-data-camera-heat build-jon-gui-data-rec-osd parse-jon-gui-data-rec-osd build-jon-gui-data-day-cam-glass-heater parse-jon-gui-data-day-cam-glass-heater build-jon-gui-data-actual-space-time parse-jon-gui-data-actual-space-time build-jon-gui-state parse-jon-gui-state)

;; Message Converters
(defn build-jon-gui-data-meteo
  "Build a JonGuiDataMeteo protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataTypes$JonGuiDataMeteo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :humidity)
      (.setHumidity builder (get m :humidity)))
    (when (contains? m :pressure)
      (.setPressure builder (get m :pressure)))
    (.build builder)))

(defn parse-jon-gui-data-meteo
  "Parse a JonGuiDataMeteo protobuf message to a map."
  [^ser.JonSharedDataTypes$JonGuiDataMeteo proto]
  (merge
    {:temperature (.getTemperature proto)
     :humidity (.getHumidity proto)
     :pressure (.getPressure proto)}
))

(defn build-jon-gui-data-time
  "Build a JonGuiDataTime protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataTime$JonGuiDataTime/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :manual-timestamp)
      (.setManualTimestamp builder (get m :manual-timestamp)))
    (when (contains? m :zone-id)
      (.setZoneId builder (get m :zone-id)))
    (when (contains? m :use-manual-time)
      (.setUseManualTime builder (get m :use-manual-time)))
    (.build builder)))

(defn parse-jon-gui-data-time
  "Parse a JonGuiDataTime protobuf message to a map."
  [^ser.JonSharedDataTime$JonGuiDataTime proto]
  (merge
    {:timestamp (.getTimestamp proto)
     :manual-timestamp (.getManualTimestamp proto)
     :zone-id (.getZoneId proto)
     :use-manual-time (.getUseManualTime proto)}
))

(defn build-jon-gui-data-system
  "Build a JonGuiDataSystem protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataSystem$JonGuiDataSystem/newBuilder)]
    ;; Set regular fields
    (when (contains? m :cpu-temperature)
      (.setCpuTemperature builder (get m :cpu-temperature)))
    (when (contains? m :gpu-temperature)
      (.setGpuTemperature builder (get m :gpu-temperature)))
    (when (contains? m :gpu-load)
      (.setGpuLoad builder (get m :gpu-load)))
    (when (contains? m :cpu-load)
      (.setCpuLoad builder (get m :cpu-load)))
    (when (contains? m :power-consumption)
      (.setPowerConsumption builder (get m :power-consumption)))
    (when (contains? m :loc)
      (.setLoc builder (get m :loc)))
    (when (contains? m :cur-video-rec-dir-year)
      (.setCurVideoRecDirYear builder (get m :cur-video-rec-dir-year)))
    (when (contains? m :cur-video-rec-dir-month)
      (.setCurVideoRecDirMonth builder (get m :cur-video-rec-dir-month)))
    (when (contains? m :cur-video-rec-dir-day)
      (.setCurVideoRecDirDay builder (get m :cur-video-rec-dir-day)))
    (when (contains? m :cur-video-rec-dir-hour)
      (.setCurVideoRecDirHour builder (get m :cur-video-rec-dir-hour)))
    (when (contains? m :cur-video-rec-dir-minute)
      (.setCurVideoRecDirMinute builder (get m :cur-video-rec-dir-minute)))
    (when (contains? m :cur-video-rec-dir-second)
      (.setCurVideoRecDirSecond builder (get m :cur-video-rec-dir-second)))
    (when (contains? m :rec-enabled)
      (.setRecEnabled builder (get m :rec-enabled)))
    (when (contains? m :important-rec-enabled)
      (.setImportantRecEnabled builder (get m :important-rec-enabled)))
    (when (contains? m :low-disk-space)
      (.setLowDiskSpace builder (get m :low-disk-space)))
    (when (contains? m :no-disk-space)
      (.setNoDiskSpace builder (get m :no-disk-space)))
    (when (contains? m :disk-space)
      (.setDiskSpace builder (get m :disk-space)))
    (when (contains? m :tracking)
      (.setTracking builder (get m :tracking)))
    (when (contains? m :vampire-mode)
      (.setVampireMode builder (get m :vampire-mode)))
    (when (contains? m :stabilization-mode)
      (.setStabilizationMode builder (get m :stabilization-mode)))
    (when (contains? m :geodesic-mode)
      (.setGeodesicMode builder (get m :geodesic-mode)))
    (when (contains? m :cv-dumping)
      (.setCvDumping builder (get m :cv-dumping)))
    (.build builder)))

(defn parse-jon-gui-data-system
  "Parse a JonGuiDataSystem protobuf message to a map."
  [^ser.JonSharedDataSystem$JonGuiDataSystem proto]
  (merge
    {:cpu-temperature (.getCpuTemperature proto)
     :gpu-temperature (.getGpuTemperature proto)
     :gpu-load (.getGpuLoad proto)
     :cpu-load (.getCpuLoad proto)
     :power-consumption (.getPowerConsumption proto)
     :loc (.getLoc proto)
     :cur-video-rec-dir-year (.getCurVideoRecDirYear proto)
     :cur-video-rec-dir-month (.getCurVideoRecDirMonth proto)
     :cur-video-rec-dir-day (.getCurVideoRecDirDay proto)
     :cur-video-rec-dir-hour (.getCurVideoRecDirHour proto)
     :cur-video-rec-dir-minute (.getCurVideoRecDirMinute proto)
     :cur-video-rec-dir-second (.getCurVideoRecDirSecond proto)
     :rec-enabled (.getRecEnabled proto)
     :important-rec-enabled (.getImportantRecEnabled proto)
     :low-disk-space (.getLowDiskSpace proto)
     :no-disk-space (.getNoDiskSpace proto)
     :disk-space (.getDiskSpace proto)
     :tracking (.getTracking proto)
     :vampire-mode (.getVampireMode proto)
     :stabilization-mode (.getStabilizationMode proto)
     :geodesic-mode (.getGeodesicMode proto)
     :cv-dumping (.getCvDumping proto)}
))

(defn build-jon-gui-data-lrf
  "Build a JonGuiDataLrf protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataLrf$JonGuiDataLrf/newBuilder)]
    ;; Set regular fields
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-measuring)
      (.setIsMeasuring builder (get m :is-measuring)))
    (when (contains? m :measure-id)
      (.setMeasureId builder (get m :measure-id)))
    (when (contains? m :target)
      (.setTarget builder (get m :target)))
    (when (contains? m :pointer-mode)
      (.setPointerMode builder (get m :pointer-mode)))
    (when (contains? m :fog-mode-enabled)
      (.setFogModeEnabled builder (get m :fog-mode-enabled)))
    (when (contains? m :is-refining)
      (.setIsRefining builder (get m :is-refining)))
    (.build builder)))

(defn parse-jon-gui-data-lrf
  "Parse a JonGuiDataLrf protobuf message to a map."
  [^ser.JonSharedDataLrf$JonGuiDataLrf proto]
  (merge
    {:is-scanning (.getIsScanning proto)
     :is-measuring (.getIsMeasuring proto)
     :measure-id (.getMeasureId proto)
     :target (.getTarget proto)
     :pointer-mode (.getPointerMode proto)
     :fog-mode-enabled (.getFogModeEnabled proto)
     :is-refining (.getIsRefining proto)}
))

(defn build-jon-gui-data-target
  "Build a JonGuiDataTarget protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataLrf$JonGuiDataTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :target-longitude)
      (.setTargetLongitude builder (get m :target-longitude)))
    (when (contains? m :target-latitude)
      (.setTargetLatitude builder (get m :target-latitude)))
    (when (contains? m :target-altitude)
      (.setTargetAltitude builder (get m :target-altitude)))
    (when (contains? m :observer-longitude)
      (.setObserverLongitude builder (get m :observer-longitude)))
    (when (contains? m :observer-latitude)
      (.setObserverLatitude builder (get m :observer-latitude)))
    (when (contains? m :observer-altitude)
      (.setObserverAltitude builder (get m :observer-altitude)))
    (when (contains? m :observer-azimuth)
      (.setObserverAzimuth builder (get m :observer-azimuth)))
    (when (contains? m :observer-elevation)
      (.setObserverElevation builder (get m :observer-elevation)))
    (when (contains? m :observer-bank)
      (.setObserverBank builder (get m :observer-bank)))
    (when (contains? m :distance-2d)
      (.setDistance2d builder (get m :distance-2d)))
    (when (contains? m :distance-3b)
      (.setDistance3b builder (get m :distance-3b)))
    (when (contains? m :observer-fix-type)
      (.setObserverFixType builder (get m :observer-fix-type)))
    (when (contains? m :session-id)
      (.setSessionId builder (get m :session-id)))
    (when (contains? m :target-id)
      (.setTargetId builder (get m :target-id)))
    (when (contains? m :target-color)
      (.setTargetColor builder (get m :target-color)))
    (when (contains? m :type)
      (.setType builder (get m :type)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))
    (.build builder)))

(defn parse-jon-gui-data-target
  "Parse a JonGuiDataTarget protobuf message to a map."
  [^ser.JonSharedDataLrf$JonGuiDataTarget proto]
  (merge
    {:timestamp (.getTimestamp proto)
     :target-longitude (.getTargetLongitude proto)
     :target-latitude (.getTargetLatitude proto)
     :target-altitude (.getTargetAltitude proto)
     :observer-longitude (.getObserverLongitude proto)
     :observer-latitude (.getObserverLatitude proto)
     :observer-altitude (.getObserverAltitude proto)
     :observer-azimuth (.getObserverAzimuth proto)
     :observer-elevation (.getObserverElevation proto)
     :observer-bank (.getObserverBank proto)
     :distance-2d (.getDistance2d proto)
     :distance-3b (.getDistance3b proto)
     :observer-fix-type (.getObserverFixType proto)
     :session-id (.getSessionId proto)
     :target-id (.getTargetId proto)
     :target-color (.getTargetColor proto)
     :type (.getType proto)
     :uuid-part-1 (.getUuidPart1 proto)
     :uuid-part-2 (.getUuidPart2 proto)
     :uuid-part-3 (.getUuidPart3 proto)
     :uuid-part-4 (.getUuidPart4 proto)}
))

(defn build-rgb-color
  "Build a RgbColor protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataLrf$RgbColor/newBuilder)]
    ;; Set regular fields
    (when (contains? m :red)
      (.setRed builder (get m :red)))
    (when (contains? m :green)
      (.setGreen builder (get m :green)))
    (when (contains? m :blue)
      (.setBlue builder (get m :blue)))
    (.build builder)))

(defn parse-rgb-color
  "Parse a RgbColor protobuf message to a map."
  [^ser.JonSharedDataLrf$RgbColor proto]
  (merge
    {:red (.getRed proto)
     :green (.getGreen proto)
     :blue (.getBlue proto)}
))

(defn build-jon-gui-data-gps
  "Build a JonGuiDataGps protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataGps$JonGuiDataGps/newBuilder)]
    ;; Set regular fields
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))
    (when (contains? m :manual-longitude)
      (.setManualLongitude builder (get m :manual-longitude)))
    (when (contains? m :manual-latitude)
      (.setManualLatitude builder (get m :manual-latitude)))
    (when (contains? m :manual-altitude)
      (.setManualAltitude builder (get m :manual-altitude)))
    (when (contains? m :fix-type)
      (.setFixType builder (get m :fix-type)))
    (when (contains? m :use-manual)
      (.setUseManual builder (get m :use-manual)))
    (.build builder)))

(defn parse-jon-gui-data-gps
  "Parse a JonGuiDataGps protobuf message to a map."
  [^ser.JonSharedDataGps$JonGuiDataGps proto]
  (merge
    {:longitude (.getLongitude proto)
     :latitude (.getLatitude proto)
     :altitude (.getAltitude proto)
     :manual-longitude (.getManualLongitude proto)
     :manual-latitude (.getManualLatitude proto)
     :manual-altitude (.getManualAltitude proto)
     :fix-type (.getFixType proto)
     :use-manual (.getUseManual proto)}
))

(defn build-jon-gui-data-compass
  "Build a JonGuiDataCompass protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataCompass$JonGuiDataCompass/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :bank)
      (.setBank builder (get m :bank)))
    (when (contains? m :offset-azimuth)
      (.setOffsetAzimuth builder (get m :offset-azimuth)))
    (when (contains? m :offset-elevation)
      (.setOffsetElevation builder (get m :offset-elevation)))
    (when (contains? m :magnetic-declination)
      (.setMagneticDeclination builder (get m :magnetic-declination)))
    (when (contains? m :calibrating)
      (.setCalibrating builder (get m :calibrating)))
    (.build builder)))

(defn parse-jon-gui-data-compass
  "Parse a JonGuiDataCompass protobuf message to a map."
  [^ser.JonSharedDataCompass$JonGuiDataCompass proto]
  (merge
    {:azimuth (.getAzimuth proto)
     :elevation (.getElevation proto)
     :bank (.getBank proto)
     :offset-azimuth (.getOffsetAzimuth proto)
     :offset-elevation (.getOffsetElevation proto)
     :magnetic-declination (.getMagneticDeclination proto)
     :calibrating (.getCalibrating proto)}
))

(defn build-jon-gui-data-compass-calibration
  "Build a JonGuiDataCompassCalibration protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration/newBuilder)]
    ;; Set regular fields
    (when (contains? m :stage)
      (.setStage builder (get m :stage)))
    (when (contains? m :final-stage)
      (.setFinalStage builder (get m :final-stage)))
    (when (contains? m :target-azimuth)
      (.setTargetAzimuth builder (get m :target-azimuth)))
    (when (contains? m :target-elevation)
      (.setTargetElevation builder (get m :target-elevation)))
    (when (contains? m :target-bank)
      (.setTargetBank builder (get m :target-bank)))
    (when (contains? m :status)
      (.setStatus builder (get m :status)))
    (.build builder)))

(defn parse-jon-gui-data-compass-calibration
  "Parse a JonGuiDataCompassCalibration protobuf message to a map."
  [^ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration proto]
  (merge
    {:stage (.getStage proto)
     :final-stage (.getFinalStage proto)
     :target-azimuth (.getTargetAzimuth proto)
     :target-elevation (.getTargetElevation proto)
     :target-bank (.getTargetBank proto)
     :status (.getStatus proto)}
))

(defn build-jon-gui-data-rotary
  "Build a JonGuiDataRotary protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataRotary$JonGuiDataRotary/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :azimuth-speed)
      (.setAzimuthSpeed builder (get m :azimuth-speed)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :elevation-speed)
      (.setElevationSpeed builder (get m :elevation-speed)))
    (when (contains? m :platform-azimuth)
      (.setPlatformAzimuth builder (get m :platform-azimuth)))
    (when (contains? m :platform-elevation)
      (.setPlatformElevation builder (get m :platform-elevation)))
    (when (contains? m :platform-bank)
      (.setPlatformBank builder (get m :platform-bank)))
    (when (contains? m :is-moving)
      (.setIsMoving builder (get m :is-moving)))
    (when (contains? m :mode)
      (.setMode builder (get m :mode)))
    (when (contains? m :is-scanning)
      (.setIsScanning builder (get m :is-scanning)))
    (when (contains? m :is-scanning-paused)
      (.setIsScanningPaused builder (get m :is-scanning-paused)))
    (when (contains? m :use-rotary-as-compass)
      (.setUseRotaryAsCompass builder (get m :use-rotary-as-compass)))
    (when (contains? m :scan-target)
      (.setScanTarget builder (get m :scan-target)))
    (when (contains? m :scan-target-max)
      (.setScanTargetMax builder (get m :scan-target-max)))
    (when (contains? m :sun-azimuth)
      (.setSunAzimuth builder (get m :sun-azimuth)))
    (when (contains? m :sun-elevation)
      (.setSunElevation builder (get m :sun-elevation)))
    (when (contains? m :current-scan-node)
      (.setCurrentScanNode builder (get m :current-scan-node)))
    (.build builder)))

(defn parse-jon-gui-data-rotary
  "Parse a JonGuiDataRotary protobuf message to a map."
  [^ser.JonSharedDataRotary$JonGuiDataRotary proto]
  (merge
    {:azimuth (.getAzimuth proto)
     :azimuth-speed (.getAzimuthSpeed proto)
     :elevation (.getElevation proto)
     :elevation-speed (.getElevationSpeed proto)
     :platform-azimuth (.getPlatformAzimuth proto)
     :platform-elevation (.getPlatformElevation proto)
     :platform-bank (.getPlatformBank proto)
     :is-moving (.getIsMoving proto)
     :mode (.getMode proto)
     :is-scanning (.getIsScanning proto)
     :is-scanning-paused (.getIsScanningPaused proto)
     :use-rotary-as-compass (.getUseRotaryAsCompass proto)
     :scan-target (.getScanTarget proto)
     :scan-target-max (.getScanTargetMax proto)
     :sun-azimuth (.getSunAzimuth proto)
     :sun-elevation (.getSunElevation proto)
     :current-scan-node (.getCurrentScanNode proto)}
))

(defn build-scan-node
  "Build a ScanNode protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataRotary$ScanNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index)
      (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :linger)
      (.setLinger builder (get m :linger)))
    (when (contains? m :speed)
      (.setSpeed builder (get m :speed)))
    (.build builder)))

(defn parse-scan-node
  "Parse a ScanNode protobuf message to a map."
  [^ser.JonSharedDataRotary$ScanNode proto]
  (merge
    {:index (.getIndex proto)
     :day-zoom-table-value (.getDayZoomTableValue proto)
     :heat-zoom-table-value (.getHeatZoomTableValue proto)
     :azimuth (.getAzimuth proto)
     :elevation (.getElevation proto)
     :linger (.getLinger proto)
     :speed (.getSpeed proto)}
))

(defn build-jon-gui-data-camera-day
  "Build a JonGuiDataCameraDay protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataCameraDay$JonGuiDataCameraDay/newBuilder)]
    ;; Set regular fields
    (when (contains? m :focus-pos)
      (.setFocusPos builder (get m :focus-pos)))
    (when (contains? m :zoom-pos)
      (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :iris-pos)
      (.setIrisPos builder (get m :iris-pos)))
    (when (contains? m :infrared-filter)
      (.setInfraredFilter builder (get m :infrared-filter)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :fx-mode)
      (.setFxMode builder (get m :fx-mode)))
    (when (contains? m :auto-focus)
      (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :auto-iris)
      (.setAutoIris builder (get m :auto-iris)))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))
    (.build builder)))

(defn parse-jon-gui-data-camera-day
  "Parse a JonGuiDataCameraDay protobuf message to a map."
  [^ser.JonSharedDataCameraDay$JonGuiDataCameraDay proto]
  (merge
    {:focus-pos (.getFocusPos proto)
     :zoom-pos (.getZoomPos proto)
     :iris-pos (.getIrisPos proto)
     :infrared-filter (.getInfraredFilter proto)
     :zoom-table-pos (.getZoomTablePos proto)
     :zoom-table-pos-max (.getZoomTablePosMax proto)
     :fx-mode (.getFxMode proto)
     :auto-focus (.getAutoFocus proto)
     :auto-iris (.getAutoIris proto)
     :digital-zoom-level (.getDigitalZoomLevel proto)
     :clahe-level (.getClaheLevel proto)}
))

(defn build-jon-gui-data-camera-heat
  "Build a JonGuiDataCameraHeat protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat/newBuilder)]
    ;; Set regular fields
    (when (contains? m :zoom-pos)
      (.setZoomPos builder (get m :zoom-pos)))
    (when (contains? m :agc-mode)
      (.setAgcMode builder (get m :agc-mode)))
    (when (contains? m :filter)
      (.setFilter builder (get m :filter)))
    (when (contains? m :auto-focus)
      (.setAutoFocus builder (get m :auto-focus)))
    (when (contains? m :zoom-table-pos)
      (.setZoomTablePos builder (get m :zoom-table-pos)))
    (when (contains? m :zoom-table-pos-max)
      (.setZoomTablePosMax builder (get m :zoom-table-pos-max)))
    (when (contains? m :dde-level)
      (.setDdeLevel builder (get m :dde-level)))
    (when (contains? m :dde-enabled)
      (.setDdeEnabled builder (get m :dde-enabled)))
    (when (contains? m :fx-mode)
      (.setFxMode builder (get m :fx-mode)))
    (when (contains? m :digital-zoom-level)
      (.setDigitalZoomLevel builder (get m :digital-zoom-level)))
    (when (contains? m :clahe-level)
      (.setClaheLevel builder (get m :clahe-level)))
    (.build builder)))

(defn parse-jon-gui-data-camera-heat
  "Parse a JonGuiDataCameraHeat protobuf message to a map."
  [^ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat proto]
  (merge
    {:zoom-pos (.getZoomPos proto)
     :agc-mode (.getAgcMode proto)
     :filter (.getFilter proto)
     :auto-focus (.getAutoFocus proto)
     :zoom-table-pos (.getZoomTablePos proto)
     :zoom-table-pos-max (.getZoomTablePosMax proto)
     :dde-level (.getDdeLevel proto)
     :dde-enabled (.getDdeEnabled proto)
     :fx-mode (.getFxMode proto)
     :digital-zoom-level (.getDigitalZoomLevel proto)
     :clahe-level (.getClaheLevel proto)}
))

(defn build-jon-gui-data-rec-osd
  "Build a JonGuiDataRecOsd protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataRecOsd$JonGuiDataRecOsd/newBuilder)]
    ;; Set regular fields
    (when (contains? m :screen)
      (.setScreen builder (get m :screen)))
    (when (contains? m :heat-osd-enabled)
      (.setHeatOsdEnabled builder (get m :heat-osd-enabled)))
    (when (contains? m :day-osd-enabled)
      (.setDayOsdEnabled builder (get m :day-osd-enabled)))
    (when (contains? m :heat-crosshair-offset-horizontal)
      (.setHeatCrosshairOffsetHorizontal builder (get m :heat-crosshair-offset-horizontal)))
    (when (contains? m :heat-crosshair-offset-vertical)
      (.setHeatCrosshairOffsetVertical builder (get m :heat-crosshair-offset-vertical)))
    (when (contains? m :day-crosshair-offset-horizontal)
      (.setDayCrosshairOffsetHorizontal builder (get m :day-crosshair-offset-horizontal)))
    (when (contains? m :day-crosshair-offset-vertical)
      (.setDayCrosshairOffsetVertical builder (get m :day-crosshair-offset-vertical)))
    (.build builder)))

(defn parse-jon-gui-data-rec-osd
  "Parse a JonGuiDataRecOsd protobuf message to a map."
  [^ser.JonSharedDataRecOsd$JonGuiDataRecOsd proto]
  (merge
    {:screen (.getScreen proto)
     :heat-osd-enabled (.getHeatOsdEnabled proto)
     :day-osd-enabled (.getDayOsdEnabled proto)
     :heat-crosshair-offset-horizontal (.getHeatCrosshairOffsetHorizontal proto)
     :heat-crosshair-offset-vertical (.getHeatCrosshairOffsetVertical proto)
     :day-crosshair-offset-horizontal (.getDayCrosshairOffsetHorizontal proto)
     :day-crosshair-offset-vertical (.getDayCrosshairOffsetVertical proto)}
))

(defn build-jon-gui-data-day-cam-glass-heater
  "Build a JonGuiDataDayCamGlassHeater protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater/newBuilder)]
    ;; Set regular fields
    (when (contains? m :temperature)
      (.setTemperature builder (get m :temperature)))
    (when (contains? m :status)
      (.setStatus builder (get m :status)))
    (.build builder)))

(defn parse-jon-gui-data-day-cam-glass-heater
  "Parse a JonGuiDataDayCamGlassHeater protobuf message to a map."
  [^ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater proto]
  (merge
    {:temperature (.getTemperature proto)
     :status (.getStatus proto)}
))

(defn build-jon-gui-data-actual-space-time
  "Build a JonGuiDataActualSpaceTime protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime/newBuilder)]
    ;; Set regular fields
    (when (contains? m :azimuth)
      (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation)
      (.setElevation builder (get m :elevation)))
    (when (contains? m :bank)
      (.setBank builder (get m :bank)))
    (when (contains? m :latitude)
      (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude)
      (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude)
      (.setAltitude builder (get m :altitude)))
    (when (contains? m :timestamp)
      (.setTimestamp builder (get m :timestamp)))
    (.build builder)))

(defn parse-jon-gui-data-actual-space-time
  "Parse a JonGuiDataActualSpaceTime protobuf message to a map."
  [^ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime proto]
  (merge
    {:azimuth (.getAzimuth proto)
     :elevation (.getElevation proto)
     :bank (.getBank proto)
     :latitude (.getLatitude proto)
     :longitude (.getLongitude proto)
     :altitude (.getAltitude proto)
     :timestamp (.getTimestamp proto)}
))

(defn build-jon-gui-state
  "Build a JonGUIState protobuf message from a map."
  [m]
  (let [builder (ser.JonSharedData$JonGUIState/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :system)
      (.setSystem builder (get m :system)))
    (when (contains? m :meteo-internal)
      (.setMeteoInternal builder (get m :meteo-internal)))
    (when (contains? m :lrf)
      (.setLrf builder (get m :lrf)))
    (when (contains? m :time)
      (.setTime builder (get m :time)))
    (when (contains? m :gps)
      (.setGps builder (get m :gps)))
    (when (contains? m :compass)
      (.setCompass builder (get m :compass)))
    (when (contains? m :rotary)
      (.setRotary builder (get m :rotary)))
    (when (contains? m :camera-day)
      (.setCameraDay builder (get m :camera-day)))
    (when (contains? m :camera-heat)
      (.setCameraHeat builder (get m :camera-heat)))
    (when (contains? m :compass-calibration)
      (.setCompassCalibration builder (get m :compass-calibration)))
    (when (contains? m :rec-osd)
      (.setRecOsd builder (get m :rec-osd)))
    (when (contains? m :day-cam-glass-heater)
      (.setDayCamGlassHeater builder (get m :day-cam-glass-heater)))
    (when (contains? m :actual-space-time)
      (.setActualSpaceTime builder (get m :actual-space-time)))
    (.build builder)))

(defn parse-jon-gui-state
  "Parse a JonGUIState protobuf message to a map."
  [^ser.JonSharedData$JonGUIState proto]
  (merge
    {:protocol-version (.getProtocolVersion proto)
     :system (.getSystem proto)
     :meteo-internal (.getMeteoInternal proto)
     :lrf (.getLrf proto)
     :time (.getTime proto)
     :gps (.getGps proto)
     :compass (.getCompass proto)
     :rotary (.getRotary proto)
     :camera-day (.getCameraDay proto)
     :camera-heat (.getCameraHeat proto)
     :compass-calibration (.getCompassCalibration proto)
     :rec-osd (.getRecOsd proto)
     :day-cam-glass-heater (.getDayCamGlassHeater proto)
     :actual-space-time (.getActualSpaceTime proto)}
))
