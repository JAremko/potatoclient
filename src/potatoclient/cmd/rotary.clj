(ns potatoclient.cmd.rotary
  "Rotary platform command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn | ?]]
            [potatoclient.cmd.core :as cmd-core])
  (:import (cmd.RotaryPlatform
             JonSharedCmdRotary$Azimuth
             JonSharedCmdRotary$Elevation JonSharedCmdRotary$GetMeteo JonSharedCmdRotary$Halt
             JonSharedCmdRotary$HaltAzimuth JonSharedCmdRotary$HaltElevation
             JonSharedCmdRotary$Root
             JonSharedCmdRotary$RotateAzimuth
             JonSharedCmdRotary$RotateAzimuthRelative
             JonSharedCmdRotary$RotateAzimuthRelativeSet JonSharedCmdRotary$RotateAzimuthTo
             JonSharedCmdRotary$RotateElevation JonSharedCmdRotary$RotateElevationRelative
             JonSharedCmdRotary$RotateElevationRelativeSet JonSharedCmdRotary$RotateElevationTo
             JonSharedCmdRotary$RotateToGPS JonSharedCmdRotary$RotateToNDC
             JonSharedCmdRotary$ScanNext JonSharedCmdRotary$ScanPause
             JonSharedCmdRotary$ScanPrev JonSharedCmdRotary$ScanStart
             JonSharedCmdRotary$ScanStop JonSharedCmdRotary$ScanUnpause
             JonSharedCmdRotary$SetAzimuthValue JonSharedCmdRotary$SetElevationValue
             JonSharedCmdRotary$SetMode JonSharedCmdRotary$SetOriginGPS
             JonSharedCmdRotary$SetPlatformAzimuth JonSharedCmdRotary$SetPlatformBank
             JonSharedCmdRotary$SetPlatformElevation JonSharedCmdRotary$Start
             JonSharedCmdRotary$Stop JonSharedCmdRotary$setUseRotaryAsCompass)
           (ser
             JonSharedDataTypes$JonGuiDataRotaryDirection
             JonSharedDataTypes$JonGuiDataRotaryMode)))

;; ============================================================================
;; Basic Commands
;; ============================================================================

(>defn rotary-start
  "Send rotary start command"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setStart (JonSharedCmdRotary$Start/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn rotary-stop
  "Send rotary stop command"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setStop (JonSharedCmdRotary$Stop/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn rotary-halt
  "Send rotary halt command"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setHalt (JonSharedCmdRotary$Halt/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Platform Position Commands
;; ============================================================================

(>defn rotary-set-platform-azimuth
  "Set platform azimuth to specific value"
  [value]
  [:potatoclient.specs/azimuth-degrees => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-azimuth (-> (JonSharedCmdRotary$SetPlatformAzimuth/newBuilder)
                        (.setValue (double value)))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setSetPlatformAzimuth set-azimuth))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn rotary-set-platform-elevation
  "Set platform elevation to specific value"
  [value]
  [:potatoclient.specs/elevation-degrees => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-elevation (-> (JonSharedCmdRotary$SetPlatformElevation/newBuilder)
                          (.setValue (double value)))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setSetPlatformElevation set-elevation))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn rotary-set-platform-bank
  "Set platform bank to specific value"
  [value]
  [:potatoclient.specs/bank-angle => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-bank (-> (JonSharedCmdRotary$SetPlatformBank/newBuilder)
                     (.setValue (double value)))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setSetPlatformBank set-bank))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Axis Halt Commands
;; ============================================================================

(>defn rotary-halt-azimuth
  "Send halt azimuth command"
  []
  [=> nil?]
  (let [azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setHalt (JonSharedCmdRotary$HaltAzimuth/newBuilder)))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg}))
  nil)

(>defn rotary-halt-elevation
  "Send halt elevation command"
  []
  [=> nil?]
  (let [elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setHalt (JonSharedCmdRotary$HaltElevation/newBuilder)))]
    (cmd-core/send-rotary-axis-command {:elevation elevation-msg}))
  nil)

(>defn rotary-halt-elevation-and-azimuth
  "Send halt command for both elevation and azimuth"
  []
  [=> nil?]
  (let [azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setHalt (JonSharedCmdRotary$HaltAzimuth/newBuilder)))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setHalt (JonSharedCmdRotary$HaltElevation/newBuilder)))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg
                                        :elevation elevation-msg}))
  nil)

;; ============================================================================
;; Azimuth Commands
;; ============================================================================

(>defn rotary-azimuth-set-value
  "Set azimuth value with direction"
  [value direction]
  [:potatoclient.specs/azimuth-degrees :potatoclient.specs/rotary-direction => nil?]
  (let [set-value (-> (JonSharedCmdRotary$SetAzimuthValue/newBuilder)
                      (.setValue (double value))
                      (.setDirection direction))
        azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setSetValue set-value))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg}))
  nil)

(>defn rotary-azimuth-rotate-to
  "Rotate azimuth to target value with speed and direction"
  [target-value speed direction]
  [:potatoclient.specs/azimuth-degrees :potatoclient.specs/rotation-speed :potatoclient.specs/rotary-direction => nil?]
  (let [rotate-to (-> (JonSharedCmdRotary$RotateAzimuthTo/newBuilder)
                      (.setTargetValue (double target-value))
                      (.setSpeed (double speed))
                      (.setDirection direction))
        azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setRotateTo rotate-to))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg}))
  nil)

(>defn rotary-azimuth-rotate
  "Rotate azimuth continuously at speed with direction"
  [speed direction]
  [:potatoclient.specs/rotation-speed :potatoclient.specs/rotary-direction => nil?]
  (let [rotate (-> (JonSharedCmdRotary$RotateAzimuth/newBuilder)
                   (.setSpeed (double speed))
                   (.setDirection direction))
        azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setRotate rotate))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg}))
  nil)

(>defn rotary-azimuth-rotate-relative
  "Rotate azimuth relative to current position"
  [value speed direction]
  [number? number? :potatoclient.specs/rotary-direction => nil?]
  (let [relative (-> (JonSharedCmdRotary$RotateAzimuthRelative/newBuilder)
                     (.setValue (double value))
                     (.setSpeed (double speed))
                     (.setDirection direction))
        azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setRelative relative))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg}))
  nil)

(>defn rotary-azimuth-rotate-relative-set
  "Set azimuth value relative to current position"
  [value direction]
  [number? :potatoclient.specs/rotary-direction => nil?]
  (let [relative-set (-> (JonSharedCmdRotary$RotateAzimuthRelativeSet/newBuilder)
                         (.setValue (double value))
                         (.setDirection direction))
        azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setRelativeSet relative-set))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg}))
  nil)

;; ============================================================================
;; Elevation Commands
;; ============================================================================

(>defn rotary-elevation-set-value
  "Set elevation value"
  [value]
  [:potatoclient.specs/elevation-degrees => nil?]
  (let [set-value (-> (JonSharedCmdRotary$SetElevationValue/newBuilder)
                      (.setValue (double value)))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setSetValue set-value))]
    (cmd-core/send-rotary-axis-command {:elevation elevation-msg}))
  nil)

(>defn rotary-elevation-rotate-to
  "Rotate elevation to target value with speed"
  [target-value speed]
  [number? number? => nil?]
  (let [rotate-to (-> (JonSharedCmdRotary$RotateElevationTo/newBuilder)
                      (.setTargetValue (double target-value))
                      (.setSpeed (double speed)))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setRotateTo rotate-to))]
    (cmd-core/send-rotary-axis-command {:elevation elevation-msg}))
  nil)

(>defn rotary-elevation-rotate
  "Rotate elevation continuously at speed with direction"
  [speed direction]
  [number? :potatoclient.specs/rotary-direction => nil?]
  (let [rotate (-> (JonSharedCmdRotary$RotateElevation/newBuilder)
                   (.setSpeed (double speed))
                   (.setDirection direction))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setRotate rotate))]
    (cmd-core/send-rotary-axis-command {:elevation elevation-msg}))
  nil)

(>defn rotary-elevation-rotate-relative
  "Rotate elevation relative to current position"
  [value speed direction]
  [number? number? :potatoclient.specs/rotary-direction => nil?]
  (let [relative (-> (JonSharedCmdRotary$RotateElevationRelative/newBuilder)
                     (.setValue (double value))
                     (.setSpeed (double speed))
                     (.setDirection direction))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setRelative relative))]
    (cmd-core/send-rotary-axis-command {:elevation elevation-msg}))
  nil)

(>defn rotary-elevation-rotate-relative-set
  "Set elevation value relative to current position"
  [value direction]
  [number? :potatoclient.specs/rotary-direction => nil?]
  (let [relative-set (-> (JonSharedCmdRotary$RotateElevationRelativeSet/newBuilder)
                         (.setValue (double value))
                         (.setDirection direction))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setRelativeSet relative-set))]
    (cmd-core/send-rotary-axis-command {:elevation elevation-msg}))
  nil)

;; ============================================================================
;; Combined Axis Commands
;; ============================================================================

(>defn rotate-both-to
  "Rotate both azimuth and elevation to target values"
  [azimuth azimuth-speed azimuth-direction elevation elevation-speed]
  [number? number? :potatoclient.specs/rotary-direction
   number? number? => nil?]
  (let [azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setRotateTo (-> (JonSharedCmdRotary$RotateAzimuthTo/newBuilder)
                                          (.setTargetValue (double azimuth))
                                          (.setSpeed (double azimuth-speed))
                                          (.setDirection azimuth-direction))))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setRotateTo (-> (JonSharedCmdRotary$RotateElevationTo/newBuilder)
                                            (.setTargetValue (double elevation))
                                            (.setSpeed (double elevation-speed)))))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg
                                        :elevation elevation-msg}))
  nil)

(>defn rotate-both
  "Rotate both azimuth and elevation continuously"
  [azimuth-speed azimuth-direction elevation-speed elevation-direction]
  [number? :potatoclient.specs/rotary-direction
   number? :potatoclient.specs/rotary-direction => nil?]
  (let [azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setRotate (-> (JonSharedCmdRotary$RotateAzimuth/newBuilder)
                                        (.setSpeed (double azimuth-speed))
                                        (.setDirection azimuth-direction))))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setRotate (-> (JonSharedCmdRotary$RotateElevation/newBuilder)
                                          (.setSpeed (double elevation-speed))
                                          (.setDirection elevation-direction))))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg
                                        :elevation elevation-msg}))
  nil)

(>defn set-both-to
  "Set both azimuth and elevation to specific values"
  [azimuth elevation azimuth-direction]
  [number? number? :potatoclient.specs/rotary-direction => nil?]
  (let [azimuth-msg (-> (JonSharedCmdRotary$Azimuth/newBuilder)
                        (.setSetValue (-> (JonSharedCmdRotary$SetAzimuthValue/newBuilder)
                                          (.setValue (double azimuth))
                                          (.setDirection azimuth-direction))))
        elevation-msg (-> (JonSharedCmdRotary$Elevation/newBuilder)
                          (.setSetValue (-> (JonSharedCmdRotary$SetElevationValue/newBuilder)
                                            (.setValue (double elevation)))))]
    (cmd-core/send-rotary-axis-command {:azimuth azimuth-msg
                                        :elevation elevation-msg}))
  nil)

;; ============================================================================
;; Other Commands
;; ============================================================================

(>defn set-calculate-base-position-from-compass
  "Set whether to calculate base position from compass"
  [value]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-compass (-> (JonSharedCmdRotary$setUseRotaryAsCompass/newBuilder)
                        (.setFlag value))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setSetUseRotaryAsCompass set-compass))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn get-meteo
  "Request rotary meteo data"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setGetMeteo (JonSharedCmdRotary$GetMeteo/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-rotate-to-gps
  "Set rotation to GPS coordinates"
  [lon lat alt]
  [:potatoclient.specs/gps-longitude :potatoclient.specs/gps-latitude :potatoclient.specs/gps-altitude => nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotate-gps (-> (JonSharedCmdRotary$RotateToGPS/newBuilder)
                       (.setLongitude (double lon))
                       (.setLatitude (double lat))
                       (.setAltitude (double alt)))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setRotateToGps rotate-gps))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-origin-gps
  "Set origin GPS coordinates"
  [lon lat alt]
  [:potatoclient.specs/gps-longitude :potatoclient.specs/gps-latitude :potatoclient.specs/gps-altitude => nil?]
  (let [root-msg (cmd-core/create-root-message)
        origin-gps (-> (JonSharedCmdRotary$SetOriginGPS/newBuilder)
                       (.setLongitude (double lon))
                       (.setLatitude (double lat))
                       (.setAltitude (double alt)))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setSetOriginGps origin-gps))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-rotary-mode
  "Set rotary mode"
  [mode]
  [:potatoclient.specs/rotary-mode => nil?]
  (let [root-msg (cmd-core/create-root-message)
        set-mode (-> (JonSharedCmdRotary$SetMode/newBuilder)
                     (.setMode mode))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setSetMode set-mode))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn rotate-to-ndc
  "Rotate to normalized device coordinates"
  [channel x y]
  [:potatoclient.specs/video-channel :potatoclient.specs/ndc-x :potatoclient.specs/ndc-y => nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotate-ndc (-> (JonSharedCmdRotary$RotateToNDC/newBuilder)
                       (.setChannel channel)
                       (.setX (double x))
                       (.setY (double y)))
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setRotateToNdc rotate-ndc))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Scan Commands
;; ============================================================================

(>defn scan-start
  "Start rotary scan"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setScanStart (JonSharedCmdRotary$ScanStart/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn scan-stop
  "Stop rotary scan"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setScanStop (JonSharedCmdRotary$ScanStop/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn scan-pause
  "Pause rotary scan"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setScanPause (JonSharedCmdRotary$ScanPause/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn scan-unpause
  "Unpause rotary scan"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setScanUnpause (JonSharedCmdRotary$ScanUnpause/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn scan-prev
  "Go to previous scan node"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setScanPrev (JonSharedCmdRotary$ScanPrev/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn scan-next
  "Go to next scan node"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        rotary-root (-> (JonSharedCmdRotary$Root/newBuilder)
                        (.setScanNext (JonSharedCmdRotary$ScanNext/newBuilder)))]
    (.setRotary root-msg rotary-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn string->rotary-mode
  "Convert string to rotary mode enum"
  [value]
  [string? => (? :potatoclient.specs/rotary-mode)]
  (case value
    "init" JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
    "speed" JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
    "position" JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
    "stabilization" JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
    "targeting" JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
    "video_tracker" JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER
    JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED))

(>defn string->rotary-direction
  "Convert string to rotary direction enum"
  [value]
  [string? => :potatoclient.specs/rotary-direction]
  (case (.toLowerCase value)
    "clockwise" JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
    ("counterclockwise" "counter clockwise")
    JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
    JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED))
