(ns potatoclient.specs.ser "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def jon-gui-data-video-channel-heat-filters "Schema for jon-gui-data-video-channel-heat-filters" [:enum :jon-gui-data-video-channel-heat-filter-unspecified :jon-gui-data-video-channel-heat-filter-hot-white :jon-gui-data-video-channel-heat-filter-hot-black :jon-gui-data-video-channel-heat-filter-sepia :jon-gui-data-video-channel-heat-filter-sepia-inverse])

(def jon-gui-data-video-channel "Schema for jon-gui-data-video-channel" [:enum :jon-gui-data-video-channel-unspecified :jon-gui-data-video-channel-heat :jon-gui-data-video-channel-day])

(def jon-gui-data-video-channel-heat-agc-modes "Schema for jon-gui-data-video-channel-heat-agc-modes" [:enum :jon-gui-data-video-channel-heat-agc-mode-unspecified :jon-gui-data-video-channel-heat-agc-mode-1 :jon-gui-data-video-channel-heat-agc-mode-2 :jon-gui-data-video-channel-heat-agc-mode-3])

(def jon-gui-data-target "Schema for jon-gui-data-target" [:map [:timestamp [:and [:maybe :int] [:>= 0.0]]] [:target-longitude [:maybe :double]] [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]] [:observer-longitude [:maybe :double]] [:observer-latitude [:maybe :double]] [:observer-altitude [:maybe :double]] [:observer-azimuth [:maybe :double]] [:observer-elevation [:maybe :double]] [:observer-bank [:maybe :double]] [:distance-2d [:maybe :double]] [:distance-3b [:maybe :double]] [:observer-fix-type [:not [:enum [0]]]] [:session-id [:>= 0]] [:target-id [:>= 0]] [:target-color [:maybe :ser/rgb-color]] [:type [:maybe :int]] [:uuid-part1 [:maybe :int]] [:uuid-part2 [:maybe :int]] [:uuid-part3 [:maybe :int]] [:uuid-part4 [:maybe :int]]])

(def jon-gui-data-client-type "Schema for jon-gui-data-client-type" [:enum :jon-gui-data-client-type-unspecified :jon-gui-data-client-type-internal-cv :jon-gui-data-client-type-local-network :jon-gui-data-client-type-certificate-protected :jon-gui-data-client-type-lira])

(def jon-gui-data-camera-day "Schema for jon-gui-data-camera-day" [:map [:focus-pos [:and [:maybe :double] [:>= 0] [:<= 1]]] [:zoom-pos [:and [:maybe :double] [:>= 0] [:<= 1]]] [:iris-pos [:and [:maybe :double] [:>= 0] [:<= 1]]] [:infrared-filter [:maybe :boolean]] [:zoom-table-pos [:>= 0]] [:zoom-table-pos-max [:>= 0]] [:fx-mode [:maybe :ser/jon-gui-data-fx-mode-day]] [:auto-focus [:maybe :boolean]] [:auto-iris [:maybe :boolean]] [:digital-zoom-level [:>= 1]] [:clahe-level [:and [:maybe :double] [:>= 0] [:<= 1]]]])

(def jon-gui-data-system-localizations "Schema for jon-gui-data-system-localizations" [:enum :jon-gui-data-system-localization-unspecified :jon-gui-data-system-localization-en :jon-gui-data-system-localization-ua :jon-gui-data-system-localization-ar :jon-gui-data-system-localization-cs])

(def jon-gui-data-fx-mode-heat "Schema for jon-gui-data-fx-mode-heat" [:enum :jon-gui-data-fx-mode-heat-default :jon-gui-data-fx-mode-heat-a :jon-gui-data-fx-mode-heat-b :jon-gui-data-fx-mode-heat-c :jon-gui-data-fx-mode-heat-d :jon-gui-data-fx-mode-heat-e :jon-gui-data-fx-mode-heat-f])

(def jon-gui-datat-lrf-laser-pointer-modes "Schema for jon-gui-datat-lrf-laser-pointer-modes" [:enum :jon-gui-data-lrf-laser-pointer-mode-unspecified :jon-gui-data-lrf-laser-pointer-mode-off :jon-gui-data-lrf-laser-pointer-mode-on-1 :jon-gui-data-lrf-laser-pointer-mode-on-2])

(def jon-gui-data-fx-mode-day "Schema for jon-gui-data-fx-mode-day" [:enum :jon-gui-data-fx-mode-day-default :jon-gui-data-fx-mode-day-a :jon-gui-data-fx-mode-day-b :jon-gui-data-fx-mode-day-c :jon-gui-data-fx-mode-day-d :jon-gui-data-fx-mode-day-e :jon-gui-data-fx-mode-day-f])

(def jon-gui-data-camera-heat "Schema for jon-gui-data-camera-heat" [:map [:zoom-pos [:and [:maybe :double] [:>= 0] [:<= 1]]] [:agc-mode [:not [:enum [0]]]] [:filter [:not [:enum [0]]]] [:auto-focus [:maybe :boolean]] [:zoom-table-pos [:>= 0]] [:zoom-table-pos-max [:>= 0]] [:dde-level [:and [:maybe :int] [:>= 0] [:<= 512]]] [:dde-enabled [:maybe :boolean]] [:fx-mode [:maybe :ser/jon-gui-data-fx-mode-heat]] [:digital-zoom-level [:>= 1]] [:clahe-level [:and [:maybe :double] [:>= 0] [:<= 1]]]])

(def jon-gui-data-rotary "Schema for jon-gui-data-rotary" [:map [:azimuth [:and [:maybe :double] [:>= 0] [:< 360]]] [:azimuth-speed [:and [:maybe :double] [:>= -1] [:<= 1]]] [:elevation [:and [:maybe :double] [:>= -90] [:<= 90]]] [:elevation-speed [:and [:maybe :double] [:>= -1] [:<= 1]]] [:platform-azimuth [:and [:maybe :double] [:>= 0] [:< 360]]] [:platform-elevation [:and [:maybe :double] [:>= -90] [:<= 90]]] [:platform-bank [:and [:maybe :double] [:>= -180] [:< 180]]] [:is-moving [:maybe :boolean]] [:mode [:not [:enum [0]]]] [:is-scanning [:maybe :boolean]] [:is-scanning-paused [:maybe :boolean]] [:use-rotary-as-compass [:maybe :boolean]] [:scan-target [:>= 0]] [:scan-target-max [:>= 0]] [:sun-azimuth [:and [:maybe :double] [:>= 0] [:< 360]]] [:sun-elevation [:and [:maybe :double] [:>= 0] [:< 360]]] [:current-scan-node [:maybe :ser/scan-node]]])

(def jon-gui-data-day-cam-glass-heater "Schema for jon-gui-data-day-cam-glass-heater" [:map [:temperature [:maybe :double]] [:status [:maybe :boolean]]])

(def jon-gui-data-accumulator-state-idx "Schema for jon-gui-data-accumulator-state-idx" [:enum :jon-gui-data-accumulator-state-unspecified :jon-gui-data-accumulator-state-unknown :jon-gui-data-accumulator-state-empty :jon-gui-data-accumulator-state-1 :jon-gui-data-accumulator-state-2 :jon-gui-data-accumulator-state-3 :jon-gui-data-accumulator-state-4 :jon-gui-data-accumulator-state-5 :jon-gui-data-accumulator-state-6 :jon-gui-data-accumulator-state-full :jon-gui-data-accumulator-state-charging])

(def jon-gui-data-time "Schema for jon-gui-data-time" [:map [:timestamp [:and [:maybe :int] [:>= 0.0]]] [:manual-timestamp [:and [:maybe :int] [:>= 0.0]]] [:zone-id [:maybe :int]] [:use-manual-time [:maybe :boolean]]])

(def jon-gui-data-gps-fix-type "Schema for jon-gui-data-gps-fix-type" [:enum :jon-gui-data-gps-fix-type-unspecified :jon-gui-data-gps-fix-type-none :jon-gui-data-gps-fix-type-1-d :jon-gui-data-gps-fix-type-2-d :jon-gui-data-gps-fix-type-3-d :jon-gui-data-gps-fix-type-manual])

(def jon-gui-data-gps "Schema for jon-gui-data-gps" [:map [:longitude [:maybe :double]] [:latitude [:maybe :double]] [:altitude [:maybe :double]] [:manual-longitude [:maybe :double]] [:manual-latitude [:maybe :double]] [:manual-altitude [:maybe :double]] [:fix-type [:not [:enum [0]]]] [:use-manual [:maybe :boolean]]])

(def jon-gui-data-compass "Schema for jon-gui-data-compass" [:map [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:bank [:maybe :double]] [:offsetAzimuth [:maybe :double]] [:offsetElevation [:maybe :double]] [:magneticDeclination [:maybe :double]] [:calibrating [:maybe :boolean]]])

(def jon-gui-data-time-formats "Schema for jon-gui-data-time-formats" [:enum :jon-gui-data-time-format-unspecified :jon-gui-data-time-format-h-m-s :jon-gui-data-time-format-y-m-d-h-m-s])

(def jon-gui-data-compass-units "Schema for jon-gui-data-compass-units" [:enum :jon-gui-data-compass-units-unspecified :jon-gui-data-compass-units-degrees :jon-gui-data-compass-units-mils :jon-gui-data-compass-units-grad :jon-gui-data-compass-units-mrad])

(def jon-gui-state "Schema for jon-gui-state" [:map [:protocol-version [:maybe :int]] [:system [:maybe :ser/jon-gui-data-system]] [:meteo-internal [:maybe :ser/jon-gui-data-meteo]] [:lrf [:maybe :ser/jon-gui-data-lrf]] [:time [:maybe :ser/jon-gui-data-time]] [:gps [:maybe :ser/jon-gui-data-gps]] [:compass [:maybe :ser/jon-gui-data-compass]] [:rotary [:maybe :ser/jon-gui-data-rotary]] [:camera-day [:maybe :ser/jon-gui-data-camera-day]] [:camera-heat [:maybe :ser/jon-gui-data-camera-heat]] [:compass-calibration [:maybe :ser/jon-gui-data-compass-calibration]] [:rec-osd [:maybe :ser/jon-gui-data-rec-osd]] [:day-cam-glass-heater [:maybe :ser/jon-gui-data-day-cam-glass-heater]] [:actual-space-time [:maybe :ser/jon-gui-data-actual-space-time]]])

(def jon-gui-data-meteo "Schema for jon-gui-data-meteo" [:map [:temperature [:maybe :double]] [:humidity [:maybe :double]] [:pressure [:maybe :double]]])

(def jon-gui-data-rec-osd "Schema for jon-gui-data-rec-osd" [:map [:screen [:not [:enum [0]]]] [:heat-osd-enabled [:maybe :boolean]] [:day-osd-enabled [:maybe :boolean]] [:heat-crosshair-offset-horizontal [:maybe :int]] [:heat-crosshair-offset-vertical [:maybe :int]] [:day-crosshair-offset-horizontal [:maybe :int]] [:day-crosshair-offset-vertical [:maybe :int]]])

(def jon-gui-data-rotary-mode "Schema for jon-gui-data-rotary-mode" [:enum :jon-gui-data-rotary-mode-unspecified :jon-gui-data-rotary-mode-initialization :jon-gui-data-rotary-mode-speed :jon-gui-data-rotary-mode-position :jon-gui-data-rotary-mode-stabilization :jon-gui-data-rotary-mode-targeting :jon-gui-data-rotary-mode-video-tracker])

(def jon-gui-data-compass-calibrate-status "Schema for jon-gui-data-compass-calibrate-status" [:enum :jon-gui-data-compass-calibrate-status-unspecified :jon-gui-data-compass-calibrate-status-not-calibrating :jon-gui-data-compass-calibrate-status-calibrating-short :jon-gui-data-compass-calibrate-status-calibrating-long :jon-gui-data-compass-calibrate-status-finished :jon-gui-data-compass-calibrate-status-error])

(def jon-gui-data-compass-calibration "Schema for jon-gui-data-compass-calibration" [:map [:stage [:maybe :int]] [:final-stage [:maybe :int]] [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]] [:target-bank [:maybe :double]] [:status [:not [:enum [0]]]]])

(def rgb-color "Schema for rgb-color" [:map [:red [:maybe :int]] [:green [:maybe :int]] [:blue [:maybe :int]]])

(def jon-gui-data-lrf "Schema for jon-gui-data-lrf" [:map [:is-scanning [:maybe :boolean]] [:is-measuring [:maybe :boolean]] [:measure-id [:>= 0]] [:target [:maybe :ser/jon-gui-data-target]] [:pointer-mode [:maybe :ser/jon-gui-datat-lrf-laser-pointer-modes]] [:fogModeEnabled [:maybe :boolean]] [:is-refining [:maybe :boolean]]])

(def jon-gui-data-lrf-scan-modes "Schema for jon-gui-data-lrf-scan-modes" [:enum :jon-gui-data-lrf-scan-mode-unspecified :jon-gui-data-lrf-scan-mode-1-hz-continuous :jon-gui-data-lrf-scan-mode-4-hz-continuous :jon-gui-data-lrf-scan-mode-10-hz-continuous :jon-gui-data-lrf-scan-mode-20-hz-continuous :jon-gui-data-lrf-scan-mode-100-hz-continuous :jon-gui-data-lrf-scan-mode-200-hz-continuous])

(def jon-gui-data-gps-units "Schema for jon-gui-data-gps-units" [:enum :jon-gui-data-gps-units-unspecified :jon-gui-data-gps-units-decimal-degrees :jon-gui-data-gps-units-degrees-minutes-seconds :jon-gui-data-gps-units-degrees-decimal-minutes])

(def jon-gui-data-system "Schema for jon-gui-data-system" [:map [:cpu-temperature [:and [:maybe :double] [:>= -273.15] [:<= 150]]] [:gpu-temperature [:and [:maybe :double] [:>= -273.15] [:<= 150]]] [:gpu-load [:and [:maybe :double] [:>= 0] [:<= 100]]] [:cpu-load [:and [:maybe :double] [:>= 0] [:<= 100]]] [:power-consumption [:and [:maybe :double] [:>= 0] [:<= 1000]]] [:loc [:not [:enum [0]]]] [:cur-video-rec-dir-year [:>= 0]] [:cur-video-rec-dir-month [:>= 0]] [:cur-video-rec-dir-day [:>= 0]] [:cur-video-rec-dir-hour [:>= 0]] [:cur-video-rec-dir-minute [:>= 0]] [:cur-video-rec-dir-second [:>= 0]] [:rec-enabled [:maybe :boolean]] [:important-rec-enabled [:maybe :boolean]] [:low-disk-space [:maybe :boolean]] [:no-disk-space [:maybe :boolean]] [:disk-space [:and [:maybe :int] [:>= 0] [:<= 100]]] [:tracking [:maybe :boolean]] [:vampire-mode [:maybe :boolean]] [:stabilization-mode [:maybe :boolean]] [:geodesic-mode [:maybe :boolean]] [:cv-dumping [:maybe :boolean]]])

(def jon-gui-data-rotary-direction "Schema for jon-gui-data-rotary-direction" [:enum :jon-gui-data-rotary-direction-unspecified :jon-gui-data-rotary-direction-clockwise :jon-gui-data-rotary-direction-counter-clockwise])

(def jon-gui-data-actual-space-time "Schema for jon-gui-data-actual-space-time" [:map [:azimuth [:and [:maybe :double] [:>= 0] [:< 360]]] [:elevation [:and [:maybe :double] [:>= -90] [:<= 90]]] [:bank [:and [:maybe :double] [:>= -180] [:< 180]]] [:latitude [:and [:maybe :double] [:>= -90] [:<= 90]]] [:longitude [:and [:maybe :double] [:>= -180] [:< 180]]] [:altitude [:maybe :double]] [:timestamp [:and [:maybe :int] [:>= 0.0]]]])

(def scan-node "Schema for scan-node" [:map [:index [:>= 0]] [:DayZoomTableValue [:>= 0]] [:HeatZoomTableValue [:>= 0]] [:azimuth [:maybe :double]] [:elevation [:maybe :double]] [:linger [:maybe :double]] [:speed [:maybe :double]]])

(def jon-gui-data-rec-osd-screen "Schema for jon-gui-data-rec-osd-screen" [:enum :jon-gui-data-rec-osd-screen-unspecified :jon-gui-data-rec-osd-screen-main :jon-gui-data-rec-osd-screen-lrf-measure :jon-gui-data-rec-osd-screen-lrf-result :jon-gui-data-rec-osd-screen-lrf-result-simplified])