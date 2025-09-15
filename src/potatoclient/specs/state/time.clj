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
  "Time state spec - system time synchronization and manual time override status"
  [:map {:closed true}
   [:timestamp :time/unix-timestamp]
   [:manual_timestamp :time/unix-timestamp]
   [:zone_id :proto/int32]
   [:use_manual_time :boolean]])

(registry/register-spec! :state/time time-message-spec)