(ns potatoclient.specs.state.system
  "System message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]))

;; JonGuiDataSystem message spec
;; All 23 fields from proto definition

(def system-message-spec
  [:map {:closed true}
   [:cpu_temperature :temperature/component]
   [:gpu_temperature :temperature/component]
   [:gpu_load :percentage]
   [:cpu_load :percentage]
   [:power_consumption [:double {:min 0.0 :max 1000.0}]]
   [:loc :enum/system-localizations]
   [:cur_video_rec_dir_year [:int {:min 0 :max 9999}]]
   [:cur_video_rec_dir_month [:int {:min 0 :max 12}]]
   [:cur_video_rec_dir_day [:int {:min 0 :max 31}]]
   [:cur_video_rec_dir_hour [:int {:min 0 :max 23}]]
   [:cur_video_rec_dir_minute [:int {:min 0 :max 59}]]
   [:cur_video_rec_dir_second [:int {:min 0 :max 59}]]
   [:rec_enabled :boolean]
   [:important_rec_enabled :boolean]
   [:low_disk_space :boolean]
   [:no_disk_space :boolean]
   [:disk_space [:int {:min 0 :max 100}]]
   [:tracking :boolean]
   [:vampire_mode :boolean]
   [:stabilization_mode :boolean]
   [:geodesic_mode :boolean]
   [:cv_dumping :boolean]
   [:recognition_mode :boolean]])

(registry/register! :state/system system-message-spec)