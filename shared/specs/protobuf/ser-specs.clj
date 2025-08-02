(ns potatoclient.specs.ser "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def JonGuiDataActualSpaceTime "Schema for JonGuiDataActualSpaceTime" [:map [:azimuth [:and [:maybe :float] [:>= 0] [:< 360]]] [:elevation [:and [:maybe :float] [:>= -90] [:<= 90]]] [:bank [:and [:maybe :float] [:>= -180] [:< 180]]] [:latitude [:and [:maybe :float] [:>= -90] [:<= 90]]] [:longitude [:and [:maybe :float] [:>= -180] [:< 180]]] [:altitude [:maybe :double]] [:timestamp [:and [:maybe :int] [:>= "0"]]]])

(def JonGuiDataTimeFormats "Schema for JonGuiDataTimeFormats" [:enum :JON-GUI-DATA-TIME-FORMAT-UNSPECIFIED :JON-GUI-DATA-TIME-FORMAT-H-M-S :JON-GUI-DATA-TIME-FORMAT-Y-m-D-H-M-S])

(def JonGuiDataVideoChannelHeatFilters "Schema for JonGuiDataVideoChannelHeatFilters" [:enum :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-FILTER-UNSPECIFIED :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-FILTER-HOT-WHITE :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-FILTER-HOT-BLACK :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-FILTER-SEPIA :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-FILTER-SEPIA-INVERSE])

(def JonGuiDataLrfScanModes "Schema for JonGuiDataLrfScanModes" [:enum :JON-GUI-DATA-LRF-SCAN-MODE-UNSPECIFIED :JON-GUI-DATA-LRF-SCAN-MODE-1-HZ-CONTINUOUS :JON-GUI-DATA-LRF-SCAN-MODE-4-HZ-CONTINUOUS :JON-GUI-DATA-LRF-SCAN-MODE-10-HZ-CONTINUOUS :JON-GUI-DATA-LRF-SCAN-MODE-20-HZ-CONTINUOUS :JON-GUI-DATA-LRF-SCAN-MODE-100-HZ-CONTINUOUS :JON-GUI-DATA-LRF-SCAN-MODE-200-HZ-CONTINUOUS])

(def JonGuiDataSystemLocalizations "Schema for JonGuiDataSystemLocalizations" [:enum :JON-GUI-DATA-SYSTEM-LOCALIZATION-UNSPECIFIED :JON-GUI-DATA-SYSTEM-LOCALIZATION-EN :JON-GUI-DATA-SYSTEM-LOCALIZATION-UA :JON-GUI-DATA-SYSTEM-LOCALIZATION-AR :JON-GUI-DATA-SYSTEM-LOCALIZATION-CS])

(def JonGuiDataVideoChannelHeatAGCModes "Schema for JonGuiDataVideoChannelHeatAGCModes" [:enum :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-AGC-MODE-UNSPECIFIED :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-AGC-MODE-1 :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-AGC-MODE-2 :JON-GUI-DATA-VIDEO-CHANNEL-HEAT-AGC-MODE-3])

(def JonGuiDataRecOsd "Schema for JonGuiDataRecOsd" [:map [:screen [:maybe :ser/JonGuiDataRecOsdScreen]] [:heat-osd-enabled [:maybe :boolean]] [:day-osd-enabled [:maybe :boolean]] [:heat-crosshair-offset-horizontal [:maybe :int]] [:heat-crosshair-offset-vertical [:maybe :int]] [:day-crosshair-offset-horizontal [:maybe :int]] [:day-crosshair-offset-vertical [:maybe :int]]])

(def JonGuiDataAccumulatorStateIdx "Schema for JonGuiDataAccumulatorStateIdx" [:enum :JON-GUI-DATA-ACCUMULATOR-STATE-UNSPECIFIED :JON-GUI-DATA-ACCUMULATOR-STATE-UNKNOWN :JON-GUI-DATA-ACCUMULATOR-STATE-EMPTY :JON-GUI-DATA-ACCUMULATOR-STATE-1 :JON-GUI-DATA-ACCUMULATOR-STATE-2 :JON-GUI-DATA-ACCUMULATOR-STATE-3 :JON-GUI-DATA-ACCUMULATOR-STATE-4 :JON-GUI-DATA-ACCUMULATOR-STATE-5 :JON-GUI-DATA-ACCUMULATOR-STATE-6 :JON-GUI-DATA-ACCUMULATOR-STATE-FULL :JON-GUI-DATA-ACCUMULATOR-STATE-CHARGING])

(def JonGuiDataFxModeDay "Schema for JonGuiDataFxModeDay" [:enum :JON-GUI-DATA-FX-MODE-DAY-DEFAULT :JON-GUI-DATA-FX-MODE-DAY-A :JON-GUI-DATA-FX-MODE-DAY-B :JON-GUI-DATA-FX-MODE-DAY-C :JON-GUI-DATA-FX-MODE-DAY-D :JON-GUI-DATA-FX-MODE-DAY-E :JON-GUI-DATA-FX-MODE-DAY-F])

(def JonGuiDataRotaryMode "Schema for JonGuiDataRotaryMode" [:enum :JON-GUI-DATA-ROTARY-MODE-UNSPECIFIED :JON-GUI-DATA-ROTARY-MODE-INITIALIZATION :JON-GUI-DATA-ROTARY-MODE-SPEED :JON-GUI-DATA-ROTARY-MODE-POSITION :JON-GUI-DATA-ROTARY-MODE-STABILIZATION :JON-GUI-DATA-ROTARY-MODE-TARGETING :JON-GUI-DATA-ROTARY-MODE-VIDEO-TRACKER])

(def JonGuiDataRotaryDirection "Schema for JonGuiDataRotaryDirection" [:enum :JON-GUI-DATA-ROTARY-DIRECTION-UNSPECIFIED :JON-GUI-DATA-ROTARY-DIRECTION-CLOCKWISE :JON-GUI-DATA-ROTARY-DIRECTION-COUNTER-CLOCKWISE])

(def JonGuiDataRotary "Schema for JonGuiDataRotary" [:map [:azimuth [:and [:maybe :float] [:>= 0] [:< 360]]] [:azimuth-speed [:and [:maybe :float] [:>= -1] [:<= 1]]] [:elevation [:and [:maybe :float] [:>= -90] [:<= 90]]] [:elevation-speed [:and [:maybe :float] [:>= -1] [:<= 1]]] [:platform-azimuth [:and [:maybe :float] [:>= 0] [:< 360]]] [:platform-elevation [:and [:maybe :float] [:>= -90] [:<= 90]]] [:platform-bank [:and [:maybe :float] [:>= -180] [:< 180]]] [:is-moving [:maybe :boolean]] [:mode [:maybe :ser/JonGuiDataRotaryMode]] [:is-scanning [:maybe :boolean]] [:is-scanning-paused [:maybe :boolean]] [:use-rotary-as-compass [:maybe :boolean]] [:scan-target [:and [:maybe :int] [:>= 0]]] [:scan-target-max [:and [:maybe :int] [:>= 0]]] [:sun-azimuth [:and [:maybe :float] [:>= 0] [:< 360]]] [:sun-elevation [:and [:maybe :float] [:>= 0] [:< 360]]] [:current-scan-node [:maybe :ser/ScanNode]]])

(def JonGuiDataCompassCalibration "Schema for JonGuiDataCompassCalibration" [:map [:stage [:and [:maybe :int] [:>= 0]]] [:final-stage [:and [:maybe :int] [:> 0]]] [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]] [:target-bank [:maybe :double]] [:status [:maybe :ser/JonGuiDataCompassCalibrateStatus]]])

(def JonGuiDataCompassUnits "Schema for JonGuiDataCompassUnits" [:enum :JON-GUI-DATA-COMPASS-UNITS-UNSPECIFIED :JON-GUI-DATA-COMPASS-UNITS-DEGREES :JON-GUI-DATA-COMPASS-UNITS-MILS :JON-GUI-DATA-COMPASS-UNITS-GRAD :JON-GUI-DATA-COMPASS-UNITS-MRAD])

(def ScanNode "Schema for ScanNode" [:map [:index [:and [:maybe :int] [:>= 0]]] [:DayZoomTableValue [:and [:maybe :int] [:>= 0]]] [:HeatZoomTableValue [:and [:maybe :int] [:>= 0]]] [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:linger [:maybe :double]] [:speed [:maybe :double]]])

(def JonGuiDataTarget "Schema for JonGuiDataTarget" [:map [:timestamp [:and [:maybe :int] [:>= "0"]]] [:target-longitude [:maybe :double]] [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]] [:observer-longitude [:maybe :double]] [:observer-latitude [:maybe :double]] [:observer-altitude [:maybe :double]] [:observer-azimuth [:maybe :double]] [:observer-elevation [:maybe :double]] [:observer-bank [:maybe :double]] [:distance-2d [:maybe :double]] [:distance-3b [:maybe :double]] [:observer-fix-type [:maybe :ser/JonGuiDataGpsFixType]] [:session-id [:and [:maybe :int] [:>= 0]]] [:target-id [:and [:maybe :int] [:>= 0]]] [:target-color [:maybe :ser/RgbColor]] [:type [:maybe :int]] [:uuid-part1 [:maybe :int]] [:uuid-part2 [:maybe :int]] [:uuid-part3 [:maybe :int]] [:uuid-part4 [:maybe :int]]])

(def JonGuiDataSystem "Schema for JonGuiDataSystem" [:map [:cpu-temperature [:and [:maybe :float] [:>= -273.15] [:<= 150]]] [:gpu-temperature [:and [:maybe :float] [:>= -273.15] [:<= 150]]] [:gpu-load [:and [:maybe :float] [:>= 0] [:<= 100]]] [:cpu-load [:and [:maybe :float] [:>= 0] [:<= 100]]] [:power-consumption [:and [:maybe :float] [:>= 0] [:<= 1000]]] [:loc [:maybe :ser/JonGuiDataSystemLocalizations]] [:cur-video-rec-dir-year [:and [:maybe :int] [:>= 0]]] [:cur-video-rec-dir-month [:and [:maybe :int] [:>= 0]]] [:cur-video-rec-dir-day [:and [:maybe :int] [:>= 0]]] [:cur-video-rec-dir-hour [:and [:maybe :int] [:>= 0]]] [:cur-video-rec-dir-minute [:and [:maybe :int] [:>= 0]]] [:cur-video-rec-dir-second [:and [:maybe :int] [:>= 0]]] [:rec-enabled [:maybe :boolean]] [:important-rec-enabled [:maybe :boolean]] [:low-disk-space [:maybe :boolean]] [:no-disk-space [:maybe :boolean]] [:disk-space [:and [:maybe :int] [:>= 0] [:<= 100]]] [:tracking [:maybe :boolean]] [:vampire-mode [:maybe :boolean]] [:stabilization-mode [:maybe :boolean]] [:geodesic-mode [:maybe :boolean]] [:cv-dumping [:maybe :boolean]]])

(def JonGuiDataRecOsdScreen "Schema for JonGuiDataRecOsdScreen" [:enum :JON-GUI-DATA-REC-OSD-SCREEN-UNSPECIFIED :JON-GUI-DATA-REC-OSD-SCREEN-MAIN :JON-GUI-DATA-REC-OSD-SCREEN-LRF-MEASURE :JON-GUI-DATA-REC-OSD-SCREEN-LRF-RESULT :JON-GUI-DATA-REC-OSD-SCREEN-LRF-RESULT-SIMPLIFIED])

(def JonGuiDataCompassCalibrateStatus "Schema for JonGuiDataCompassCalibrateStatus" [:enum :JON-GUI-DATA-COMPASS-CALIBRATE-STATUS-UNSPECIFIED :JON-GUI-DATA-COMPASS-CALIBRATE-STATUS-NOT-CALIBRATING :JON-GUI-DATA-COMPASS-CALIBRATE-STATUS-CALIBRATING-SHORT :JON-GUI-DATA-COMPASS-CALIBRATE-STATUS-CALIBRATING-LONG :JON-GUI-DATA-COMPASS-CALIBRATE-STATUS-FINISHED :JON-GUI-DATA-COMPASS-CALIBRATE-STATUS-ERROR])

(def JonGuiDataCompass "Schema for JonGuiDataCompass" [:map [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:bank [:maybe :double]] [:offsetAzimuth [:maybe :double]] [:offsetElevation [:maybe :double]] [:magneticDeclination [:maybe :double]] [:calibrating [:maybe :boolean]]])

(def JonGuiDataLrf "Schema for JonGuiDataLrf" [:map [:is-scanning [:maybe :boolean]] [:is-measuring [:maybe :boolean]] [:measure-id [:and [:maybe :int] [:>= 0]]] [:target [:maybe :ser/JonGuiDataTarget]] [:pointer-mode [:maybe :ser/JonGuiDatatLrfLaserPointerModes]] [:fogModeEnabled [:maybe :boolean]] [:is-refining [:maybe :boolean]]])

(def JonGuiDataCameraDay "Schema for JonGuiDataCameraDay" [:map [:focus-pos [:and [:maybe :float] [:>= 0] [:<= 1]]] [:zoom-pos [:and [:maybe :float] [:>= 0] [:<= 1]]] [:iris-pos [:and [:maybe :float] [:>= 0] [:<= 1]]] [:infrared-filter [:maybe :boolean]] [:zoom-table-pos [:and [:maybe :int] [:>= 0]]] [:zoom-table-pos-max [:and [:maybe :int] [:>= 0]]] [:fx-mode [:maybe :ser/JonGuiDataFxModeDay]] [:auto-focus [:maybe :boolean]] [:auto-iris [:maybe :boolean]] [:digital-zoom-level [:and [:maybe :float] [:>= 1]]] [:clahe-level [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def JonGUIState "Schema for JonGUIState" [:map [:protocol-version [:and [:maybe :int] [:> 0]]] [:system [:maybe :ser/JonGuiDataSystem]] [:meteo-internal [:maybe :ser/JonGuiDataMeteo]] [:lrf [:maybe :ser/JonGuiDataLrf]] [:time [:maybe :ser/JonGuiDataTime]] [:gps [:maybe :ser/JonGuiDataGps]] [:compass [:maybe :ser/JonGuiDataCompass]] [:rotary [:maybe :ser/JonGuiDataRotary]] [:camera-day [:maybe :ser/JonGuiDataCameraDay]] [:camera-heat [:maybe :ser/JonGuiDataCameraHeat]] [:compass-calibration [:maybe :ser/JonGuiDataCompassCalibration]] [:rec-osd [:maybe :ser/JonGuiDataRecOsd]] [:day-cam-glass-heater [:maybe :ser/JonGuiDataDayCamGlassHeater]] [:actual-space-time [:maybe :ser/JonGuiDataActualSpaceTime]]])

(def JonGuiDataVideoChannel "Schema for JonGuiDataVideoChannel" [:enum :JON-GUI-DATA-VIDEO-CHANNEL-UNSPECIFIED :JON-GUI-DATA-VIDEO-CHANNEL-HEAT :JON-GUI-DATA-VIDEO-CHANNEL-DAY])

(def JonGuiDataMeteo "Schema for JonGuiDataMeteo" [:map [:temperature [:maybe :float]] [:humidity [:maybe :float]] [:pressure [:maybe :float]]])

(def JonGuiDataGpsFixType "Schema for JonGuiDataGpsFixType" [:enum :JON-GUI-DATA-GPS-FIX-TYPE-UNSPECIFIED :JON-GUI-DATA-GPS-FIX-TYPE-NONE :JON-GUI-DATA-GPS-FIX-TYPE-1D :JON-GUI-DATA-GPS-FIX-TYPE-2D :JON-GUI-DATA-GPS-FIX-TYPE-3D :JON-GUI-DATA-GPS-FIX-TYPE-MANUAL])

(def JonGuiDataCameraHeat "Schema for JonGuiDataCameraHeat" [:map [:zoom-pos [:and [:maybe :float] [:>= 0] [:<= 1]]] [:agc-mode [:maybe :ser/JonGuiDataVideoChannelHeatAGCModes]] [:filter [:maybe :ser/JonGuiDataVideoChannelHeatFilters]] [:auto-focus [:maybe :boolean]] [:zoom-table-pos [:and [:maybe :int] [:>= 0]]] [:zoom-table-pos-max [:and [:maybe :int] [:>= 0]]] [:dde-level [:and [:maybe :int] [:>= 0] [:<= 512]]] [:dde-enabled [:maybe :boolean]] [:fx-mode [:maybe :ser/JonGuiDataFxModeHeat]] [:digital-zoom-level [:and [:maybe :float] [:>= 1]]] [:clahe-level [:and [:maybe :float] [:>= 0] [:<= 1]]]])

(def JonGuiDataGps "Schema for JonGuiDataGps" [:map [:longitude [:maybe :double]] [:latitude [:maybe :double]] [:altitude [:maybe :double]] [:manual-longitude [:maybe :double]] [:manual-latitude [:maybe :double]] [:manual-altitude [:maybe :double]] [:fix-type [:maybe :ser/JonGuiDataGpsFixType]] [:use-manual [:maybe :boolean]]])

(def JonGuiDataFxModeHeat "Schema for JonGuiDataFxModeHeat" [:enum :JON-GUI-DATA-FX-MODE-HEAT-DEFAULT :JON-GUI-DATA-FX-MODE-HEAT-A :JON-GUI-DATA-FX-MODE-HEAT-B :JON-GUI-DATA-FX-MODE-HEAT-C :JON-GUI-DATA-FX-MODE-HEAT-D :JON-GUI-DATA-FX-MODE-HEAT-E :JON-GUI-DATA-FX-MODE-HEAT-F])

(def JonGuiDataClientType "Schema for JonGuiDataClientType" [:enum :JON-GUI-DATA-CLIENT-TYPE-UNSPECIFIED :JON-GUI-DATA-CLIENT-TYPE-INTERNAL-CV :JON-GUI-DATA-CLIENT-TYPE-LOCAL-NETWORK :JON-GUI-DATA-CLIENT-TYPE-CERTIFICATE-PROTECTED :JON-GUI-DATA-CLIENT-TYPE-LIRA])

(def JonGuiDataDayCamGlassHeater "Schema for JonGuiDataDayCamGlassHeater" [:map [:temperature [:maybe :double]] [:status [:maybe :boolean]]])

(def JonGuiDataTime "Schema for JonGuiDataTime" [:map [:timestamp [:and [:maybe :int] [:>= "0"]]] [:manual-timestamp [:and [:maybe :int] [:>= "0"]]] [:zone-id [:maybe :int]] [:use-manual-time [:maybe :boolean]]])

(def JonGuiDataGpsUnits "Schema for JonGuiDataGpsUnits" [:enum :JON-GUI-DATA-GPS-UNITS-UNSPECIFIED :JON-GUI-DATA-GPS-UNITS-DECIMAL-DEGREES :JON-GUI-DATA-GPS-UNITS-DEGREES-MINUTES-SECONDS :JON-GUI-DATA-GPS-UNITS-DEGREES-DECIMAL-MINUTES])

(def JonGuiDatatLrfLaserPointerModes "Schema for JonGuiDatatLrfLaserPointerModes" [:enum :JON-GUI-DATA-LRF-LASER-POINTER-MODE-UNSPECIFIED :JON-GUI-DATA-LRF-LASER-POINTER-MODE-OFF :JON-GUI-DATA-LRF-LASER-POINTER-MODE-ON-1 :JON-GUI-DATA-LRF-LASER-POINTER-MODE-ON-2])

(def RgbColor "Schema for RgbColor" [:map [:red [:and [:maybe :int] [:>= 0] [:<= 255]]] [:green [:and [:maybe :int] [:>= 0] [:<= 255]]] [:blue [:and [:maybe :int] [:>= 0] [:<= 255]]]])