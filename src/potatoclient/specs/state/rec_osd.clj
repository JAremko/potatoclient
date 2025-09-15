(ns potatoclient.specs.state.rec-osd
  "Recording OSD message spec matching buf.validate constraints and EDN output.
   Based on jon_shared_data_rec_osd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
    [potatoclient.specs.common]
    [potatoclient.malli.registry :as registry]))

;; JonGuiDataRecOsd message spec
;; All 7 fields from proto definition

(def rec-osd-message-spec
  "Recording OSD state spec - on-screen display configuration for recording sessions"
  [:map {:closed true}
   [:screen :enum/rec-osd-screen]
   [:heat_osd_enabled :boolean]
   [:day_osd_enabled :boolean]
   [:heat_crosshair_offset_horizontal :proto/int32]
   [:heat_crosshair_offset_vertical :proto/int32]
   [:day_crosshair_offset_horizontal :proto/int32]
   [:day_crosshair_offset_vertical :proto/int32]])

(registry/register-spec! :state/rec-osd rec-osd-message-spec)