(ns potatoclient.specs.data.time
  "Malli specs for time data matching jon_shared_data_time.proto validation constraints")

;; From jon_shared_data_time.proto validation constraints

(def timestamp
  "Unix timestamp in milliseconds (non-negative)"
  [:int {:min 0}])

(def time-data
  "Complete time data structure"
  [:map
   [:timestamp {:optional true} timestamp]
   [:manual-timestamp {:optional true} timestamp]
   [:zone-id {:optional true} int?]
   [:use-manual-time {:optional true} boolean?]])