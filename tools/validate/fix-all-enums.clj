(ns fix-all-enums
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

;; Define all correct enum values based on proto-explorer results
(def enum-replacements
  {;; GPS Fix Type
   ":JON_GUI_DATA_GPS_FIX_TYPE_1d" ":JON_GUI_DATA_GPS_FIX_TYPE_1D"
   ":JON_GUI_DATA_GPS_FIX_TYPE_2d" ":JON_GUI_DATA_GPS_FIX_TYPE_2D"
   ":JON_GUI_DATA_GPS_FIX_TYPE_3d" ":JON_GUI_DATA_GPS_FIX_TYPE_3D"
   
   ;; Heat AGC Mode
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1"
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2"
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3"
   
   ;; Heat Filters - fix any remaining issues
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK"
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE"
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA"
   ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE" ":JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE"
   
   ;; Compass Calibrate Status
   ":JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_calibrating" ":JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_NOT_CALIBRATING"
   ":JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_short" ":JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_SHORT"
   ":JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_long" ":JON_GUI_DATA_COMPASS_CALIBRATE_STATUS_CALIBRATING_LONG"
   
   ;; LRF Laser Pointer Mode
   ":JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF" ":JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF"
   ":JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1" ":JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1"
   ":JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2" ":JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2"
   
   ;; LRF Scan modes - these might not exist, let's remove them if needed
   ":JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_continuous" ":JON_GUI_DATA_LRF_SCAN_MODE_1_HZ_CONTINUOUS"
   ":JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_continuous" ":JON_GUI_DATA_LRF_SCAN_MODE_4_HZ_CONTINUOUS"
   ":JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_continuous" ":JON_GUI_DATA_LRF_SCAN_MODE_10_HZ_CONTINUOUS"
   ":JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_continuous" ":JON_GUI_DATA_LRF_SCAN_MODE_20_HZ_CONTINUOUS"
   ":JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_continuous" ":JON_GUI_DATA_LRF_SCAN_MODE_100_HZ_CONTINUOUS"
   ":JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_continuous" ":JON_GUI_DATA_LRF_SCAN_MODE_200_HZ_CONTINUOUS"
   
   ;; System Localization - add missing ones
   ":JON_GUI_DATA_SYSTEM_LOCALIZATION_HE" ":JON_GUI_DATA_SYSTEM_LOCALIZATION_CS"  ; or keep HE if that's what's used
   ":JON_GUI_DATA_SYSTEM_LOCALIZATION_RU" ":JON_GUI_DATA_SYSTEM_LOCALIZATION_UA"  ; or keep RU if that's what's used
   })

(defn fix-file [file-path]
  (let [content (slurp file-path)
        fixed (reduce (fn [s [old new]]
                       (str/replace s old new))
                     content
                     enum-replacements)]
    (when (not= content fixed)
      (spit file-path fixed)
      (println "Fixed:" file-path))))

;; Fix all spec files
(doseq [file (file-seq (io/file "/home/jare/git/potatoclient/shared/src/potatoclient/specs"))]
  (when (and (.isFile file) 
             (.endsWith (.getName file) ".clj"))
    (fix-file (.getPath file))))

(println "Done fixing enum values!")