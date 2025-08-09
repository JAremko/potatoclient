(ns validate.specs.state.lrf
  "LRF (Laser Range Finder) message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; LRF laser pointer mode enum
(def lrf-laser-pointer-mode-spec
  [:enum
   :jon-gui-data-lrf-laser-pointer-mode-off
   :jon-gui-data-lrf-laser-pointer-mode-on
   :jon-gui-data-lrf-laser-pointer-mode-blink])

(registry/register! :enum/lrf-laser-pointer-mode lrf-laser-pointer-mode-spec)

;; LRF target message spec based on EDN output:
;; {:observer-azimuth 356.40000000000003
;;  :observer-elevation -0.675
;;  :observer-fix-type :jon-gui-data-gps-fix-type-2d
;;  :observer-latitude 8.0
;;  :observer-longitude 7.0
;;  :target-id 52
;;  :target-latitude 50.023638999999996
;;  :target-longitude 15.815211999999999
;;  :timestamp 1754576916
;;  :uuid-part1 -494581931
;;  :uuid-part2 -224575107
;;  :uuid-part3 -1771114019
;;  :uuid-part4 879344611}

(def lrf-target-spec
  [:map {:closed true}
   [:observer-azimuth :angle/azimuth]
   [:observer-elevation :angle/elevation]
   [:observer-fix-type :enum/gps-fix-type]
   [:observer-latitude :position/latitude]
   [:observer-longitude :position/longitude]
   [:target-id [:int {:min 0}]]
   [:target-latitude :position/latitude]
   [:target-longitude :position/longitude]
   [:timestamp :time/unix-timestamp]
   [:uuid-part1 :int]
   [:uuid-part2 :int]
   [:uuid-part3 :int]
   [:uuid-part4 :int]])

(registry/register! :lrf/target lrf-target-spec)

;; JonGuiDataLrf message spec based on EDN output:
;; {:measure-id 52
;;  :pointer-mode :jon-gui-data-lrf-laser-pointer-mode-off
;;  :target {...}}

(def lrf-message-spec
  [:map {:closed true}
   [:measure-id [:int {:min 0}]]
   [:pointer-mode :enum/lrf-laser-pointer-mode]
   [:target :lrf/target]])

(registry/register! :state/lrf lrf-message-spec)