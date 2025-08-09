(ns validate.specs.state.rec-osd
  "Recording OSD message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_rec_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Recording OSD screen enum
(def rec-osd-screen-spec
  [:enum
   :jon-gui-data-rec-osd-screen-main
   :jon-gui-data-rec-osd-screen-pip])

(registry/register! :enum/rec-osd-screen rec-osd-screen-spec)

;; JonGuiDataRecOsd message spec based on EDN output:
;; {:day-osd-enabled true
;;  :heat-osd-enabled true
;;  :screen :jon-gui-data-rec-osd-screen-main}

(def rec-osd-message-spec
  [:map {:closed true}
   [:day-osd-enabled :boolean]
   [:heat-osd-enabled :boolean]
   [:screen :enum/rec-osd-screen]])

(registry/register! :state/rec-osd rec-osd-message-spec)