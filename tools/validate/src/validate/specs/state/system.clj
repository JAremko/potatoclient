(ns validate.specs.state.system
  "System message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [clojure.test.check.generators :as gen]))

;; System message spec based on actual EDN output:
;; {:cpu-load 42.0
;;  :cpu-temperature 42.0
;;  :cur-video-rec-dir-day 8
;;  :cur-video-rec-dir-hour 15
;;  :cur-video-rec-dir-minute 1
;;  :cur-video-rec-dir-month 8
;;  :cur-video-rec-dir-second 32
;;  :cur-video-rec-dir-year 2025
;;  :disk-space 95
;;  :gpu-load 42.0
;;  :gpu-temperature 42.0
;;  :loc :jon-gui-data-system-localization-en
;;  :low-disk-space true
;;  :power-consumption 42.0
;;  :rec-enabled true}

;; System localization enum (need to query proto-explorer when needed)
(def system-localization-spec
  [:enum
   :jon-gui-data-system-localization-en
   :jon-gui-data-system-localization-he
   :jon-gui-data-system-localization-ru])

(registry/register! :enum/system-localization system-localization-spec)

(def system-message-spec
  [:map {:closed true}
   [:cpu-load :percentage]
   [:cpu-temperature :temperature/celsius]
   [:cur-video-rec-dir-day [:int {:min 1 :max 31}]]
   [:cur-video-rec-dir-hour [:int {:min 0 :max 23}]]
   [:cur-video-rec-dir-minute [:int {:min 0 :max 59}]]
   [:cur-video-rec-dir-month [:int {:min 1 :max 12}]]
   [:cur-video-rec-dir-second [:int {:min 0 :max 59}]]
   [:cur-video-rec-dir-year [:int {:min 2000 :max 2100}]]
   [:disk-space [:int {:min 0 :max 100}]]  ; Percentage as integer
   [:gpu-load :percentage]
   [:gpu-temperature :temperature/celsius]
   [:loc :enum/system-localization]
   [:low-disk-space :boolean]
   [:power-consumption [:double {:min 0.0 :max 1000.0
                                 :gen/gen (gen/double* {:min 0.0 :max 200.0 :NaN? false})}]]
   [:rec-enabled :boolean]])

(registry/register! :state/system system-message-spec)