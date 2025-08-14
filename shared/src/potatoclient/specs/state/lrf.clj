(ns potatoclient.specs.state.lrf
  "LRF (Laser Range Finder) message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; RgbColor message spec
(def rgb-color-spec
  [:map {:closed true}
   [:red [:int {:min 0 :max 255}]]
   [:green [:int {:min 0 :max 255}]]
   [:blue [:int {:min 0 :max 255}]]])

(registry/register! :lrf/rgb-color rgb-color-spec)

;; JonGuiDataTarget message spec
;; All 21 fields from proto definition

(def lrf-target-spec
  [:map {:closed true}
   [:timestamp [:int {:min 0}]]
   [:target_longitude :position/longitude]
   [:target_latitude :position/latitude]
   [:target_altitude :double]
   [:observer_longitude :position/longitude]
   [:observer_latitude :position/latitude]
   [:observer_altitude :double]
   [:observer_azimuth :angle/azimuth]
   [:observer_elevation :angle/elevation]
   [:observer_bank :angle/bank]
   [:distance_2d :distance/meters]
   [:distance_3b :distance/meters]
   [:observer_fix_type :enum/gps-fix-type]
   [:session_id [:int {:min 0}]]
   [:target_id [:int {:min 0}]]
   [:target_color :lrf/rgb-color]
   [:type :int]
   [:uuid_part1 :int]
   [:uuid_part2 :int]
   [:uuid_part3 :int]
   [:uuid_part4 :int]])

(registry/register! :lrf/target lrf-target-spec)

;; JonGuiDataLrf message spec
;; All 7 fields from proto definition

(def lrf-message-spec
  [:map {:closed true}
   [:is_scanning :boolean]
   [:is_measuring :boolean]
   [:measure_id [:int {:min 0}]]
   [:target :lrf/target]
   [:pointer_mode :enum/lrf-laser-pointer-modes]
   [:fogModeEnabled :boolean]
   [:is_refining :boolean]])

(registry/register! :state/lrf lrf-message-spec)