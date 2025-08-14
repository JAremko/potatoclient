(ns potatoclient.specs.state.time
  "Time message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_time.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataTime message spec
;; All 4 fields from proto definition

(def time-message-spec
  [:map {:closed true}
   [:timestamp [:int {:min 0}]]
   [:manual_timestamp [:int {:min 0}]]
   [:zone_id :int]
   [:use_manual_time :boolean]])

(registry/register! :state/time time-message-spec)