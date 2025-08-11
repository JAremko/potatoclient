(ns potatoclient.specs.state.lrf
  "LRF (Laser Range Finder) message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LRF laser pointer mode enum (from JonGuiDatatLrfLaserPointerModes)
(def lrf-laser-pointer-mode-spec
  [:enum
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2])

(registry/register! :enum/lrf-laser-pointer-mode lrf-laser-pointer-mode-spec)

;; LRF target message spec (JonGuiDataTarget) based on proto definition:
;; Has 21 fields including distances, colors, and complete observer/target info

(def lrf-target-spec
  [:map {:closed true}
   [:distance_2d {:optional true} [:double {:min 0.0 :max 500000.0}]] ; decimeters
   [:distance_3b {:optional true} [:double {:min 0.0 :max 500000.0}]] ; decimeters
   [:observer_altitude {:optional true} :position/altitude]
   [:observer_azimuth :angle/azimuth]
   [:observer_bank {:optional true} [:double {:min -180.0 :max 179.999999}]]
   [:observer_elevation :angle/elevation]
   [:observer_fix_type :enum/gps-fix-type]
   [:observer_latitude :position/latitude]
   [:observer_longitude :position/longitude]
   [:session_id {:optional true} [:int {:min 0}]]
   [:target_altitude {:optional true} :position/altitude]
   [:target_color {:optional true} [:map {:closed true}
                                    [:red [:int {:min 0 :max 255}]]
                                    [:green [:int {:min 0 :max 255}]]
                                    [:blue [:int {:min 0 :max 255}]]]]
   [:target_id {:optional true} [:int {:min 0}]]
   [:target_latitude :position/latitude]
   [:target_longitude :position/longitude]
   [:timestamp :time/unix-timestamp]
   [:type {:optional true} [:int {:min 0}]]
   [:uuid_part1 {:optional true} :int]
   [:uuid_part2 {:optional true} :int]
   [:uuid_part3 {:optional true} :int]
   [:uuid_part4 {:optional true} :int]])

(registry/register! :lrf/target lrf-target-spec)

;; JonGuiDataLrf message spec based on proto definition:
;; Has 7 fields including scanning/measuring/refining states and fog mode

(def lrf-message-spec
  [:map {:closed true}
   [:fogModeEnabled {:optional true} :boolean]
   [:is_measuring {:optional true} :boolean]
   [:is_refining {:optional true} :boolean]
   [:is_scanning {:optional true} :boolean]
   [:measure_id {:optional true} [:int {:min 0}]]
   [:pointer_mode :enum/lrf-laser-pointer-mode]
   [:target :lrf/target]])

(registry/register! :state/lrf lrf-message-spec)