(ns validate.specs.state.rotary
  "Rotary message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Scan node spec based on actual EDN output:
;; {:azimuth 0.001
;;  :dayzoomtablevalue 1
;;  :elevation 0.001
;;  :heatzoomtablevalue 1
;;  :index 1
;;  :linger 0.001
;;  :speed 0.001}

(def scan-node-spec
  [:map {:closed true}
   [:azimuth :rotary/azimuth]
   [:dayzoomtablevalue [:int {:min 0}]]
   [:elevation :rotary/elevation]
   [:heatzoomtablevalue [:int {:min 0}]]
   [:index [:int {:min 0}]]
   [:linger [:double {:min 0.0}]]
   [:speed :rotary/speed]])

(registry/register! :rotary/scan-node scan-node-spec)

;; Rotary message spec based on actual EDN output:
;; {:azimuth 335.3625
;;  :current-scan-node {...}
;;  :mode :jon-gui-data-rotary-mode-position
;;  :platform-azimuth 256.62
;;  :platform-elevation 7.04
;;  :scan-target 1
;;  :scan-target-max 1}

(def rotary-message-spec
  [:map {:closed true}
   [:azimuth :angle/azimuth]
   [:current-scan-node :rotary/scan-node]
   [:mode :enum/rotary-mode]
   [:platform-azimuth :rotary/platform-azimuth]
   [:platform-elevation :rotary/elevation]
   [:scan-target [:int {:min 0}]]
   [:scan-target-max [:int {:min 0}]]])

(registry/register! :state/rotary rotary-message-spec)