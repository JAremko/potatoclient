(ns potatoclient.specs.cmd
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def root
  "Schema for root"
  [:map [:protocol-version [:maybe :int]] [:session-id [:maybe :int]] [:important [:maybe :boolean]]
   [:from-cv-subsystem [:maybe :boolean]] [:client-type [:not [:enum [0]]]]
   [:payload
    [:oneof
     {:osd [:map [:osd :cmd/root]],
      :ping [:map [:ping :cmd/ping]],
      :system [:map [:system :cmd/root]],
      :noop [:map [:noop :cmd/noop]],
      :cv [:map [:cv :cmd/root]],
      :gps [:map [:gps :cmd/root]],
      :lrf [:map [:lrf :cmd/root]],
      :day-cam-glass-heater [:map [:day-cam-glass-heater :cmd/root]],
      :day-camera [:map [:day-camera :cmd/root]],
      :heat-camera [:map [:heat-camera :cmd/root]],
      :lira [:map [:lira :cmd/root]],
      :lrf-calib [:map [:lrf-calib :cmd/root]],
      :rotary [:map [:rotary :cmd/root]],
      :compass [:map [:compass :cmd/root]],
      :frozen [:map [:frozen :cmd/frozen]]}]]])


(def ping "Schema for ping" [:map])


(def noop "Schema for noop" [:map])


(def frozen "Schema for frozen" [:map])
