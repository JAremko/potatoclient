(ns potatoclient.specs.state.rec-osd
  "Recording OSD message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_rec_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Recording OSD screen enum
(def rec-osd-screen-spec
  [:enum
   :JON_GUI_DATA_REC_OSD_SCREEN_MAIN
   :JON_GUI_DATA_REC_OSD_SCREEN_PIP])

(registry/register! :enum/rec-osd-screen rec-osd-screen-spec)

;; JonGuiDataRecOsd message spec based on EDN output:
;; {:day_osd_enabled true
;;  :heat_osd_enabled true
;;  :screen :JON_GUI_DATA_REC_OSD_SCREEN_MAIN}

(def rec-osd-message-spec
  [:map {:closed true}
   [:day_osd_enabled boolean?]
   [:heat_osd_enabled boolean?]
   [:screen :enum/rec-osd-screen]])

(registry/register! :state/rec-osd rec-osd-message-spec)