(ns potatoclient.specs.state.time
  "Time message spec matching buf.validate constraints and EDN output format.
   Based on jon_shared_data_time.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataTime message spec based on EDN output:
;; {:manual_timestamp 1754665407
;;  :timestamp 1754665407}

(def time-message-spec
  [:map {:closed true}
   [:manual_timestamp :time/unix-timestamp]
   [:timestamp :time/unix-timestamp]])

(registry/register! :state/time time-message-spec)