(ns potatoclient.cmd.test-helpers
  "Helper functions for command tests - convert keywords to protobuf enums"
  (:import [ser
            JonSharedDataTypes$JonGuiDataRotaryDirection
            JonSharedDataTypes$JonGuiDataRotaryMode
            JonSharedDataTypes$JonGuiDataVideoChannel
            JonSharedDataTypes$JonGuiDataFxModeDay
            JonSharedDataTypes$JonGuiDataFxModeHeat
            JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes
            JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters
            JonSharedDataTypes$JonGuiDataSystemLocalizations]))

;; Rotary Direction Conversions
(def keyword->rotary-direction
  "Convert keyword to rotary direction enum"
  {:normal   JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED
   :cw       JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   :ccw      JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
   ;; For elevation, use clockwise for up and counter-clockwise for down
   :up       JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   :down     JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
   ;; For azimuth, map left/right to ccw/cw
   :left     JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
   :right    JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   ;; Shortest doesn't exist, use unspecified
   :shortest JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED})

;; Rotary Mode Conversions
(def keyword->rotary-mode
  "Convert keyword to rotary mode enum"
  {:initialization   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
   :speed           JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
   :position        JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
   :stabilization   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
   :targeting       JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
   :video-tracker   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER})

;; Video Channel Conversions
(def keyword->video-channel
  "Convert keyword to video channel enum"
  {:day  JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY
   :heat JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT})

;; Heat AGC Mode Conversions
(def keyword->heat-agc-mode
  "Convert keyword to heat AGC mode enum"
  {:agc-1 JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
   :agc-2 JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
   :agc-3 JonSharedDataTypes$JonGuiDataVideoChannelHeatAGCModes/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3})

;; Heat Filter Conversions
(def keyword->heat-filter
  "Convert keyword to heat filter enum"
  {:hot-white      JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
   :hot-black      JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
   :sepia          JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
   :sepia-inverse  JonSharedDataTypes$JonGuiDataVideoChannelHeatFilters/JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE})

;; System Localization Conversions
(def keyword->localization
  "Convert keyword to localization enum"
  {:en JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
   :ua JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
   :ar JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
   :cs JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS})

;; Day FX Mode Conversions
(def keyword->day-fx-mode
  "Convert keyword to day camera FX mode enum"
  {:day-a JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_A
   :day-b JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_B
   :day-c JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_C
   :day-d JonSharedDataTypes$JonGuiDataFxModeDay/JON_GUI_DATA_FX_MODE_DAY_D})

;; Heat FX Mode Conversions
(def keyword->heat-fx-mode
  "Convert keyword to heat camera FX mode enum"
  {:heat-a JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_A
   :heat-b JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_B
   :heat-c JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_C
   :heat-d JonSharedDataTypes$JonGuiDataFxModeHeat/JON_GUI_DATA_FX_MODE_HEAT_D})

;; Helper functions to use the conversions
(defn direction [kw]
  (get keyword->rotary-direction kw))

(defn mode [kw]
  (get keyword->rotary-mode kw))

(defn channel [kw]
  (get keyword->video-channel kw))

(defn agc-mode [kw]
  (get keyword->heat-agc-mode kw))

(defn heat-filter [kw]
  (get keyword->heat-filter kw))

(defn localization [kw]
  (get keyword->localization kw))

(defn day-fx [kw]
  (get keyword->day-fx-mode kw))

(defn heat-fx [kw]
  (get keyword->heat-fx-mode kw))