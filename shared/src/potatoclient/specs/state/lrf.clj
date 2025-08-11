(ns potatoclient.specs.state.lrf
  "LRF (Laser Range Finder) message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LRF laser pointer mode enum
(def lrf-laser-pointer-mode-spec
  [:enum
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_off
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_on
   :JON_GUI_DATA_LRF_LASER_POINTER_MODE_blink])

(registry/register! :enum/lrf-laser-pointer-mode lrf-laser-pointer-mode-spec)

;; LRF target message spec based on EDN output:
;; {:observer_azimuth 356.40000000000003
;;  :observer_elevation -0.675
;;  :observer_fix_type :JON_GUI_DATA_GPS_FIX_TYPE_2D
;;  :observer_latitude 8.0
;;  :observer_longitude 7.0
;;  :target_id 52
;;  :target_latitude 50.023638999999996
;;  :target_longitude 15.815211999999999
;;  :timestamp 1754576916
;;  :uuid_part1 -494581931
;;  :uuid_part2 -224575107
;;  :uuid_part3 -1771114019
;;  :uuid_part4 879344611}

(def lrf-target-spec
  [:map {:closed true}
   [:observer_azimuth :angle/azimuth]
   [:observer_elevation :angle/elevation]
   [:observer_fix_type :enum/gps-fix-type]
   [:observer_latitude :position/latitude]
   [:observer_longitude :position/longitude]
   [:target_id [:int {:min 0}]]
   [:target_latitude :position/latitude]
   [:target_longitude :position/longitude]
   [:timestamp :time/unix-timestamp]
   [:uuid_part1 :int]
   [:uuid_part2 :int]
   [:uuid_part3 :int]
   [:uuid_part4 :int]])

(registry/register! :lrf/target lrf-target-spec)

;; JonGuiDataLrf message spec based on EDN output:
;; {:measure_id 52
;;  :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_off
;;  :target {...}}

(def lrf-message-spec
  [:map {:closed true}
   [:measure_id [:int {:min 0}]]
   [:pointer_mode :enum/lrf-laser-pointer-mode]
   [:target :lrf/target]])

(registry/register! :state/lrf lrf-message-spec)