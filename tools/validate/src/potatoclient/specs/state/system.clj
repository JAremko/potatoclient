(ns potatoclient.specs.state.system
  "System message spec matching buf.validate constraints and EDN output format.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.specs.common]
   [potatoclient.malli.registry :as registry]
   [clojure.test.check.generators :as gen]))

;; System message spec based on actual EDN output:
;; {:cpu_load 42.0
;;  :cpu_temperature 42.0
;;  :cur_video_rec_dir_day 8
;;  :cur_video_rec_dir_hour 15
;;  :cur_video_rec_dir_minute 1
;;  :cur_video_rec_dir_month 8
;;  :cur_video_rec_dir_second 32
;;  :cur_video_rec_dir_year 2025
;;  :disk_space 95
;;  :gpu_load 42.0
;;  :gpu_temperature 42.0
;;  :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
;;  :low_disk_space true
;;  :power_consumption 42.0
;;  :rec_enabled true}

;; System localization enum - all localizations from proto
(def system-localization-spec
  [:enum
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA])

(registry/register! :enum/system-localization system-localization-spec)

;; System message spec with all 23 fields from JonGuiDataSystem proto
(def system-message-spec
  [:map {:closed true}
   [:cpu_load [:double {:min 0.0 :max 100.0}]]
   [:cpu_temperature [:double {:min -273.15 :max 150.0}]]
   [:cur_video_rec_dir_day [:int {:min 0 :max 31}]]
   [:cur_video_rec_dir_hour [:int {:min 0 :max 23}]]
   [:cur_video_rec_dir_minute [:int {:min 0 :max 59}]]
   [:cur_video_rec_dir_month [:int {:min 0 :max 12}]]
   [:cur_video_rec_dir_second [:int {:min 0 :max 59}]]
   [:cur_video_rec_dir_year [:int {:min 0 :max 3000}]]
   [:cv_dumping {:optional true} boolean?]
   [:disk_space [:int {:min 0 :max 100}]]  ; Percentage as integer
   [:geodesic_mode {:optional true} boolean?]
   [:gpu_load [:double {:min 0.0 :max 100.0}]]
   [:gpu_temperature [:double {:min -273.15 :max 150.0}]]
   [:important_rec_enabled {:optional true} boolean?]
   [:loc :enum/system-localization]
   [:low_disk_space boolean?]
   [:no_disk_space {:optional true} boolean?]
   [:power_consumption [:double {:min 0.0 :max 1000.0
                                 :gen/gen (gen/double* {:min 0.0 :max 200.0 :NaN? false})}]]
   [:rec_enabled boolean?]
   [:recognition_mode {:optional true} boolean?]
   [:stabilization_mode {:optional true} boolean?]
   [:tracking {:optional true} boolean?]
   [:vampire_mode {:optional true} boolean?]])

(registry/register! :state/system system-message-spec)