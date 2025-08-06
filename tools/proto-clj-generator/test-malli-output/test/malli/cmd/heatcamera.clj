(ns test.malli.cmd.heatcamera
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [test.malli.ser :as types])
  (:import
    cmd.HeatCamera.JonSharedCmdHeatCamera$Root
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel
    cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel
    cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode
    cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode
    cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode
    cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE
    cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel
    cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE
    cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn
    cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut
    cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop
    cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn
    cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut
    cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop
    cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus
    cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus
    cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate
    cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom
    cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos
    cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters
    cmd.HeatCamera.JonSharedCmdHeatCamera$Start
    cmd.HeatCamera.JonSharedCmdHeatCamera$Stop
    cmd.HeatCamera.JonSharedCmdHeatCamera$Halt
    cmd.HeatCamera.JonSharedCmdHeatCamera$Photo
    cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo
    cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus
    cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom
    cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:oneof
     {:set-dde-level [:map [:set-dde-level :cmd.heatcamera/set-dde-level]],
      :set-calib-mode [:map [:set-calib-mode :cmd.heatcamera/set-calib-mode]],
      :zoom [:map [:zoom :cmd.heatcamera/zoom]],
      :set-agc [:map [:set-agc :cmd.heatcamera/set-agc]],
      :shift-dde [:map [:shift-dde :cmd.heatcamera/shift-dde]],
      :set-filter [:map [:set-filter :cmd.heatcamera/set-filters]],
      :set-clahe-level [:map
                        [:set-clahe-level :cmd.heatcamera/set-clahe-level]],
      :disable-dde [:map [:disable-dde :cmd.heatcamera/disable-dde]],
      :prev-fx-mode [:map [:prev-fx-mode :cmd.heatcamera/prev-fx-mode]],
      :start [:map [:start :cmd.heatcamera/start]],
      :focus-step-minus [:map
                         [:focus-step-minus :cmd.heatcamera/focus-step-minus]],
      :set-digital-zoom-level
        [:map [:set-digital-zoom-level :cmd.heatcamera/set-digital-zoom-level]],
      :enable-dde [:map [:enable-dde :cmd.heatcamera/enable-dde]],
      :focus-stop [:map [:focus-stop :cmd.heatcamera/focus-stop]],
      :stop [:map [:stop :cmd.heatcamera/stop]],
      :reset-zoom [:map [:reset-zoom :cmd.heatcamera/reset-zoom]],
      :zoom-out [:map [:zoom-out :cmd.heatcamera/zoom-out]],
      :photo [:map [:photo :cmd.heatcamera/photo]],
      :zoom-in [:map [:zoom-in :cmd.heatcamera/zoom-in]],
      :get-meteo [:map [:get-meteo :cmd.heatcamera/get-meteo]],
      :focus-step-plus [:map
                        [:focus-step-plus :cmd.heatcamera/focus-step-plus]],
      :set-fx-mode [:map [:set-fx-mode :cmd.heatcamera/set-fx-mode]],
      :refresh-fx-mode [:map
                        [:refresh-fx-mode :cmd.heatcamera/refresh-fx-mode]],
      :focus-out [:map [:focus-out :cmd.heatcamera/focus-out]],
      :set-auto-focus [:map [:set-auto-focus :cmd.heatcamera/set-auto-focus]],
      :zoom-stop [:map [:zoom-stop :cmd.heatcamera/zoom-stop]],
      :save-to-table [:map [:save-to-table :cmd.heatcamera/save-to-table]],
      :next-fx-mode [:map [:next-fx-mode :cmd.heatcamera/next-fx-mode]],
      :calibrate [:map [:calibrate :cmd.heatcamera/calibrate]],
      :shift-clahe-level
        [:map [:shift-clahe-level :cmd.heatcamera/shift-clahe-level]],
      :focus-in [:map [:focus-in :cmd.heatcamera/focus-in]]}]]])

(def set-fx-mode-spec
  "Malli spec for set-fx-mode message"
  [:map [:mode [:maybe :ser/jon-gui-data-fx-mode-heat]]])

(def set-clahe-level-spec
  "Malli spec for set-clahe-level message"
  [:map [:value [:maybe :float]]])

(def shift-clahe-level-spec
  "Malli spec for shift-clahe-level message"
  [:map [:value [:maybe :float]]])

(def next-fx-mode-spec "Malli spec for next-fx-mode message" [:map])

(def prev-fx-mode-spec "Malli spec for prev-fx-mode message" [:map])

(def refresh-fx-mode-spec "Malli spec for refresh-fx-mode message" [:map])

(def enable-dde-spec "Malli spec for enable-dde message" [:map])

(def disable-dde-spec "Malli spec for disable-dde message" [:map])

(def set-value-spec
  "Malli spec for set-value message"
  [:map [:value [:maybe :float]]])

(def set-dde-level-spec
  "Malli spec for set-dde-level message"
  [:map [:value [:maybe :int]]])

(def set-digital-zoom-level-spec
  "Malli spec for set-digital-zoom-level message"
  [:map [:value [:maybe :float]]])

(def shift-dde-spec
  "Malli spec for shift-dde message"
  [:map [:value [:maybe :int]]])

(def zoom-in-spec "Malli spec for zoom-in message" [:map])

(def zoom-out-spec "Malli spec for zoom-out message" [:map])

(def zoom-stop-spec "Malli spec for zoom-stop message" [:map])

(def focus-in-spec "Malli spec for focus-in message" [:map])

(def focus-out-spec "Malli spec for focus-out message" [:map])

(def focus-stop-spec "Malli spec for focus-stop message" [:map])

(def focus-step-plus-spec "Malli spec for focus-step-plus message" [:map])

(def focus-step-minus-spec "Malli spec for focus-step-minus message" [:map])

(def calibrate-spec "Malli spec for calibrate message" [:map])

(def zoom-spec
  "Malli spec for zoom message"
  [:map
   [:cmd
    [:oneof
     {:set-zoom-table-value
        [:map [:set-zoom-table-value :cmd.heatcamera/set-zoom-table-value]],
      :next-zoom-table-pos
        [:map [:next-zoom-table-pos :cmd.heatcamera/next-zoom-table-pos]],
      :prev-zoom-table-pos
        [:map [:prev-zoom-table-pos :cmd.heatcamera/prev-zoom-table-pos]]}]]])

(def next-zoom-table-pos-spec
  "Malli spec for next-zoom-table-pos message"
  [:map])

(def prev-zoom-table-pos-spec
  "Malli spec for prev-zoom-table-pos message"
  [:map])

(def set-calib-mode-spec "Malli spec for set-calib-mode message" [:map])

(def set-zoom-table-value-spec
  "Malli spec for set-zoom-table-value message"
  [:map [:value [:maybe :int]]])

(def set-agc-spec
  "Malli spec for set-agc message"
  [:map [:value [:maybe :ser/jon-gui-data-video-channel-heat-agc-modes]]])

(def set-filters-spec
  "Malli spec for set-filters message"
  [:map [:value [:maybe :ser/jon-gui-data-video-channel-heat-filters]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def halt-spec "Malli spec for halt message" [:map])

(def photo-spec "Malli spec for photo message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def set-auto-focus-spec
  "Malli spec for set-auto-focus message"
  [:map [:value [:maybe :boolean]]])

(def reset-zoom-spec "Malli spec for reset-zoom message" [:map])

(def save-to-table-spec "Malli spec for save-to-table message" [:map])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-set-fx-mode)
(declare build-set-clahe-level)
(declare build-shift-clahe-level)
(declare build-next-fx-mode)
(declare build-prev-fx-mode)
(declare build-refresh-fx-mode)
(declare build-enable-dde)
(declare build-disable-dde)
(declare build-set-value)
(declare build-set-dde-level)
(declare build-set-digital-zoom-level)
(declare build-shift-dde)
(declare build-zoom-in)
(declare build-zoom-out)
(declare build-zoom-stop)
(declare build-focus-in)
(declare build-focus-out)
(declare build-focus-stop)
(declare build-focus-step-plus)
(declare build-focus-step-minus)
(declare build-calibrate)
(declare build-zoom)
(declare build-next-zoom-table-pos)
(declare build-prev-zoom-table-pos)
(declare build-set-calib-mode)
(declare build-set-zoom-table-value)
(declare build-set-agc)
(declare build-set-filters)
(declare build-start)
(declare build-stop)
(declare build-halt)
(declare build-photo)
(declare build-get-meteo)
(declare build-set-auto-focus)
(declare build-reset-zoom)
(declare build-save-to-table)
(declare parse-root)
(declare parse-set-fx-mode)
(declare parse-set-clahe-level)
(declare parse-shift-clahe-level)
(declare parse-next-fx-mode)
(declare parse-prev-fx-mode)
(declare parse-refresh-fx-mode)
(declare parse-enable-dde)
(declare parse-disable-dde)
(declare parse-set-value)
(declare parse-set-dde-level)
(declare parse-set-digital-zoom-level)
(declare parse-shift-dde)
(declare parse-zoom-in)
(declare parse-zoom-out)
(declare parse-zoom-stop)
(declare parse-focus-in)
(declare parse-focus-out)
(declare parse-focus-stop)
(declare parse-focus-step-plus)
(declare parse-focus-step-minus)
(declare parse-calibrate)
(declare parse-zoom)
(declare parse-next-zoom-table-pos)
(declare parse-prev-zoom-table-pos)
(declare parse-set-calib-mode)
(declare parse-set-zoom-table-value)
(declare parse-set-agc)
(declare parse-set-filters)
(declare parse-start)
(declare parse-stop)
(declare parse-halt)
(declare parse-photo)
(declare parse-get-meteo)
(declare parse-set-auto-focus)
(declare parse-reset-zoom)
(declare parse-save-to-table)
(declare build-root-payload)
(declare build-zoom-payload)
(declare parse-root-payload)
(declare parse-zoom-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Root %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field
                      (first (filter
                               (fn [[k v]]
                                 (#{:zoom :set-agc :set-filter :start :stop
                                    :photo :zoom-in :zoom-out :zoom-stop
                                    :focus-in :focus-out :focus-stop :calibrate
                                    :set-dde-level :enable-dde :disable-dde
                                    :set-auto-focus :focus-step-plus
                                    :focus-step-minus :set-fx-mode :next-fx-mode
                                    :prev-fx-mode :get-meteo :shift-dde
                                    :refresh-fx-mode :reset-zoom :save-to-table
                                    :set-calib-mode :set-digital-zoom-level
                                    :set-clahe-level :shift-clahe-level}
                                  k))
                               m))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn
  build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  [set-fx-mode-spec =>
   #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode %)]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder
                (get types/jon-gui-data-fx-mode-heat-values (get m :mode))))
    (.build builder)))

(>defn build-set-clahe-level
       "Build a SetClaheLevel protobuf message from a map."
       [m]
       [set-clahe-level-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-shift-clahe-level
       "Build a ShiftClaheLevel protobuf message from a map."
       [m]
       [shift-clahe-level-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel %)]
       (let
         [builder
            (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-next-fx-mode
       "Build a NextFxMode protobuf message from a map."
       [m]
       [next-fx-mode-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode/newBuilder)]
         (.build builder)))

(>defn build-prev-fx-mode
       "Build a PrevFxMode protobuf message from a map."
       [m]
       [prev-fx-mode-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode/newBuilder)]
         (.build builder)))

(>defn build-refresh-fx-mode
       "Build a RefreshFxMode protobuf message from a map."
       [m]
       [refresh-fx-mode-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode/newBuilder)]
         (.build builder)))

(>defn build-enable-dde
       "Build a EnableDDE protobuf message from a map."
       [m]
       [enable-dde-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE/newBuilder)]
         (.build builder)))

(>defn build-disable-dde
       "Build a DisableDDE protobuf message from a map."
       [m]
       [disable-dde-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE/newBuilder)]
         (.build builder)))

(>defn build-set-value
       "Build a SetValue protobuf message from a map."
       [m]
       [set-value-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-set-dde-level
       "Build a SetDDELevel protobuf message from a map."
       [m]
       [set-dde-level-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn
  build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  [set-digital-zoom-level-spec =>
   #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel %)]
  (let
    [builder
       (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(>defn build-shift-dde
       "Build a ShiftDDE protobuf message from a map."
       [m]
       [shift-dde-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-zoom-in
       "Build a ZoomIn protobuf message from a map."
       [m]
       [zoom-in-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn/newBuilder)]
         (.build builder)))

(>defn build-zoom-out
       "Build a ZoomOut protobuf message from a map."
       [m]
       [zoom-out-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut/newBuilder)]
         (.build builder)))

(>defn build-zoom-stop
       "Build a ZoomStop protobuf message from a map."
       [m]
       [zoom-stop-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop/newBuilder)]
         (.build builder)))

(>defn build-focus-in
       "Build a FocusIn protobuf message from a map."
       [m]
       [focus-in-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn/newBuilder)]
         (.build builder)))

(>defn build-focus-out
       "Build a FocusOut protobuf message from a map."
       [m]
       [focus-out-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut/newBuilder)]
         (.build builder)))

(>defn build-focus-stop
       "Build a FocusStop protobuf message from a map."
       [m]
       [focus-stop-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop/newBuilder)]
         (.build builder)))

(>defn build-focus-step-plus
       "Build a FocusStepPlus protobuf message from a map."
       [m]
       [focus-step-plus-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus/newBuilder)]
         (.build builder)))

(>defn build-focus-step-minus
       "Build a FocusStepMinus protobuf message from a map."
       [m]
       [focus-step-minus-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus %)]
       (let
         [builder
            (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus/newBuilder)]
         (.build builder)))

(>defn build-calibrate
       "Build a Calibrate protobuf message from a map."
       [m]
       [calibrate-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate/newBuilder)]
         (.build builder)))

(>defn build-zoom
       "Build a Zoom protobuf message from a map."
       [m]
       [zoom-spec => #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set-zoom-table-value
                                                  :next-zoom-table-pos
                                                  :prev-zoom-table-pos}
                                                k))
                                       m))]
           (build-zoom-payload builder cmd-field))
         (.build builder)))

(>defn build-next-zoom-table-pos
       "Build a NextZoomTablePos protobuf message from a map."
       [m]
       [next-zoom-table-pos-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos %)]
       (let
         [builder
            (cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos/newBuilder)]
         (.build builder)))

(>defn build-prev-zoom-table-pos
       "Build a PrevZoomTablePos protobuf message from a map."
       [m]
       [prev-zoom-table-pos-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos %)]
       (let
         [builder
            (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos/newBuilder)]
         (.build builder)))

(>defn build-set-calib-mode
       "Build a SetCalibMode protobuf message from a map."
       [m]
       [set-calib-mode-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode/newBuilder)]
         (.build builder)))

(>defn
  build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  [set-zoom-table-value-spec =>
   #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue %)]
  (let [builder
          (cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(>defn
  build-set-agc
  "Build a SetAGC protobuf message from a map."
  [m]
  [set-agc-spec => #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC %)]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder
                 (get types/jon-gui-data-video-channel-heat-agc-modes-values
                      (get m :value))))
    (.build builder)))

(>defn build-set-filters
       "Build a SetFilters protobuf message from a map."
       [m]
       [set-filters-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value)
           (.setValue builder
                      (get types/jon-gui-data-video-channel-heat-filters-values
                           (get m :value))))
         (.build builder)))

(>defn build-start
       "Build a Start protobuf message from a map."
       [m]
       [start-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Start %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Start/newBuilder)]
         (.build builder)))

(>defn build-stop
       "Build a Stop protobuf message from a map."
       [m]
       [stop-spec => #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Stop %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Stop/newBuilder)]
         (.build builder)))

(>defn build-halt
       "Build a Halt protobuf message from a map."
       [m]
       [halt-spec => #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Halt %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Halt/newBuilder)]
         (.build builder)))

(>defn build-photo
       "Build a Photo protobuf message from a map."
       [m]
       [photo-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Photo %)]
       (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Photo/newBuilder)]
         (.build builder)))

(>defn build-get-meteo
       "Build a GetMeteo protobuf message from a map."
       [m]
       [get-meteo-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo/newBuilder)]
         (.build builder)))

(>defn build-set-auto-focus
       "Build a SetAutoFocus protobuf message from a map."
       [m]
       [set-auto-focus-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-reset-zoom
       "Build a ResetZoom protobuf message from a map."
       [m]
       [reset-zoom-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom/newBuilder)]
         (.build builder)))

(>defn build-save-to-table
       "Build a SaveToTable protobuf message from a map."
       [m]
       [save-to-table-spec =>
        #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable %)]
       (let [builder
               (cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable/newBuilder)]
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Root %) => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-set-fx-mode
       "Parse a SetFxMode protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode %) =>
        set-fx-mode-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :mode
                (get types/jon-gui-data-fx-mode-heat-keywords
                     (.getMode proto)))))

(>defn parse-set-clahe-level
       "Parse a SetClaheLevel protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel %) =>
        set-clahe-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-shift-clahe-level
       "Parse a ShiftClaheLevel protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel %) =>
        shift-clahe-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-next-fx-mode
       "Parse a NextFxMode protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode proto]
       [any? => next-fx-mode-spec]
       {})

(>defn parse-prev-fx-mode
       "Parse a PrevFxMode protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode proto]
       [any? => prev-fx-mode-spec]
       {})

(>defn parse-refresh-fx-mode
       "Parse a RefreshFxMode protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode proto]
       [any? => refresh-fx-mode-spec]
       {})

(>defn parse-enable-dde
       "Parse a EnableDDE protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE proto]
       [any? => enable-dde-spec]
       {})

(>defn parse-disable-dde
       "Parse a DisableDDE protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE proto]
       [any? => disable-dde-spec]
       {})

(>defn parse-set-value
       "Parse a SetValue protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue %) =>
        set-value-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-dde-level
       "Parse a SetDDELevel protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel %) =>
        set-dde-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-digital-zoom-level
       "Parse a SetDigitalZoomLevel protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel %)
        => set-digital-zoom-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-shift-dde
       "Parse a ShiftDDE protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE %) =>
        shift-dde-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-zoom-in
       "Parse a ZoomIn protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn proto]
       [any? => zoom-in-spec]
       {})

(>defn parse-zoom-out
       "Parse a ZoomOut protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut proto]
       [any? => zoom-out-spec]
       {})

(>defn parse-zoom-stop
       "Parse a ZoomStop protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop proto]
       [any? => zoom-stop-spec]
       {})

(>defn parse-focus-in
       "Parse a FocusIn protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn proto]
       [any? => focus-in-spec]
       {})

(>defn parse-focus-out
       "Parse a FocusOut protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut proto]
       [any? => focus-out-spec]
       {})

(>defn parse-focus-stop
       "Parse a FocusStop protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop proto]
       [any? => focus-stop-spec]
       {})

(>defn parse-focus-step-plus
       "Parse a FocusStepPlus protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus proto]
       [any? => focus-step-plus-spec]
       {})

(>defn parse-focus-step-minus
       "Parse a FocusStepMinus protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus proto]
       [any? => focus-step-minus-spec]
       {})

(>defn parse-calibrate
       "Parse a Calibrate protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate proto]
       [any? => calibrate-spec]
       {})

(>defn parse-zoom
       "Parse a Zoom protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom %) => zoom-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-zoom-payload proto))))

(>defn parse-next-zoom-table-pos
       "Parse a NextZoomTablePos protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos proto]
       [any? => next-zoom-table-pos-spec]
       {})

(>defn parse-prev-zoom-table-pos
       "Parse a PrevZoomTablePos protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos proto]
       [any? => prev-zoom-table-pos-spec]
       {})

(>defn parse-set-calib-mode
       "Parse a SetCalibMode protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode proto]
       [any? => set-calib-mode-spec]
       {})

(>defn parse-set-zoom-table-value
       "Parse a SetZoomTableValue protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue %)
        => set-zoom-table-value-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-agc
       "Parse a SetAGC protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC %) =>
        set-agc-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value
                (get types/jon-gui-data-video-channel-heat-agc-modes-keywords
                     (.getValue proto)))))

(>defn parse-set-filters
       "Parse a SetFilters protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters %) =>
        set-filters-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value
                (get types/jon-gui-data-video-channel-heat-filters-keywords
                     (.getValue proto)))))

(>defn parse-start
       "Parse a Start protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Start proto]
       [any? => start-spec]
       {})

(>defn parse-stop
       "Parse a Stop protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Stop proto]
       [any? => stop-spec]
       {})

(>defn parse-halt
       "Parse a Halt protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Halt proto]
       [any? => halt-spec]
       {})

(>defn parse-photo
       "Parse a Photo protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$Photo proto]
       [any? => photo-spec]
       {})

(>defn parse-get-meteo
       "Parse a GetMeteo protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo proto]
       [any? => get-meteo-spec]
       {})

(>defn parse-set-auto-focus
       "Parse a SetAutoFocus protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus proto]
       [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus %) =>
        set-auto-focus-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-reset-zoom
       "Parse a ResetZoom protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom proto]
       [any? => reset-zoom-spec]
       {})

(>defn parse-save-to-table
       "Parse a SaveToTable protobuf message to a map."
       [^cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable proto]
       [any? => save-to-table-spec]
       {})

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Root$Builder %)
   [:tuple keyword? any?] =>
   #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Root$Builder %)]
  (case field-key
    :zoom (.setZoom builder (build-zoom value))
    :set-agc (.setSetAgc builder (build-set-agc value))
    :set-filter (.setSetFilter builder (build-set-filters value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :photo (.setPhoto builder (build-photo value))
    :zoom-in (.setZoomIn builder (build-zoom-in value))
    :zoom-out (.setZoomOut builder (build-zoom-out value))
    :zoom-stop (.setZoomStop builder (build-zoom-stop value))
    :focus-in (.setFocusIn builder (build-focus-in value))
    :focus-out (.setFocusOut builder (build-focus-out value))
    :focus-stop (.setFocusStop builder (build-focus-stop value))
    :calibrate (.setCalibrate builder (build-calibrate value))
    :set-dde-level (.setSetDdeLevel builder (build-set-dde-level value))
    :enable-dde (.setEnableDde builder (build-enable-dde value))
    :disable-dde (.setDisableDde builder (build-disable-dde value))
    :set-auto-focus (.setSetAutoFocus builder (build-set-auto-focus value))
    :focus-step-plus (.setFocusStepPlus builder (build-focus-step-plus value))
    :focus-step-minus (.setFocusStepMinus builder
                                          (build-focus-step-minus value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :shift-dde (.setShiftDde builder (build-shift-dde value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom value))
    :save-to-table (.setSaveToTable builder (build-save-to-table value))
    :set-calib-mode (.setSetCalibMode builder (build-set-calib-mode value))
    :set-digital-zoom-level
      (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level value))
    :shift-clahe-level (.setShiftClaheLevel builder
                                            (build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn- build-zoom-payload
        "Build the oneof payload for Zoom."
        [builder [field-key value]]
        [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom$Builder %)
         [:tuple keyword? any?] =>
         #(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom$Builder %)]
        (case field-key
          :set-zoom-table-value
            (.setSetZoomTableValue builder (build-set-zoom-table-value value))
          :next-zoom-table-pos
            (.setNextZoomTablePos builder (build-next-zoom-table-pos value))
          :prev-zoom-table-pos
            (.setPrevZoomTablePos builder (build-prev-zoom-table-pos value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":cmd"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Root %) => (? map?)]
  (cond
    (.hasZoom proto) {:zoom (parse-zoom (.getZoom proto))}
    (.hasSetAgc proto) {:set-agc (parse-set-agc (.getSetAgc proto))}
    (.hasSetFilter proto) {:set-filter (parse-set-filters (.getSetFilter
                                                            proto))}
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasPhoto proto) {:photo (parse-photo (.getPhoto proto))}
    (.hasZoomIn proto) {:zoom-in (parse-zoom-in (.getZoomIn proto))}
    (.hasZoomOut proto) {:zoom-out (parse-zoom-out (.getZoomOut proto))}
    (.hasZoomStop proto) {:zoom-stop (parse-zoom-stop (.getZoomStop proto))}
    (.hasFocusIn proto) {:focus-in (parse-focus-in (.getFocusIn proto))}
    (.hasFocusOut proto) {:focus-out (parse-focus-out (.getFocusOut proto))}
    (.hasFocusStop proto) {:focus-stop (parse-focus-stop (.getFocusStop proto))}
    (.hasCalibrate proto) {:calibrate (parse-calibrate (.getCalibrate proto))}
    (.hasSetDdeLevel proto) {:set-dde-level (parse-set-dde-level
                                              (.getSetDdeLevel proto))}
    (.hasEnableDde proto) {:enable-dde (parse-enable-dde (.getEnableDde proto))}
    (.hasDisableDde proto) {:disable-dde (parse-disable-dde (.getDisableDde
                                                              proto))}
    (.hasSetAutoFocus proto) {:set-auto-focus (parse-set-auto-focus
                                                (.getSetAutoFocus proto))}
    (.hasFocusStepPlus proto) {:focus-step-plus (parse-focus-step-plus
                                                  (.getFocusStepPlus proto))}
    (.hasFocusStepMinus proto) {:focus-step-minus (parse-focus-step-minus
                                                    (.getFocusStepMinus proto))}
    (.hasSetFxMode proto) {:set-fx-mode (parse-set-fx-mode (.getSetFxMode
                                                             proto))}
    (.hasNextFxMode proto) {:next-fx-mode (parse-next-fx-mode (.getNextFxMode
                                                                proto))}
    (.hasPrevFxMode proto) {:prev-fx-mode (parse-prev-fx-mode (.getPrevFxMode
                                                                proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasShiftDde proto) {:shift-dde (parse-shift-dde (.getShiftDde proto))}
    (.hasRefreshFxMode proto) {:refresh-fx-mode (parse-refresh-fx-mode
                                                  (.getRefreshFxMode proto))}
    (.hasResetZoom proto) {:reset-zoom (parse-reset-zoom (.getResetZoom proto))}
    (.hasSaveToTable proto) {:save-to-table (parse-save-to-table
                                              (.getSaveToTable proto))}
    (.hasSetCalibMode proto) {:set-calib-mode (parse-set-calib-mode
                                                (.getSetCalibMode proto))}
    (.hasSetDigitalZoomLevel proto) {:set-digital-zoom-level
                                       (parse-set-digital-zoom-level
                                         (.getSetDigitalZoomLevel proto))}
    (.hasSetClaheLevel proto) {:set-clahe-level (parse-set-clahe-level
                                                  (.getSetClaheLevel proto))}
    (.hasShiftClaheLevel proto) {:shift-clahe-level (parse-shift-clahe-level
                                                      (.getShiftClaheLevel
                                                        proto))}))

(>defn- parse-zoom-payload
        "Parse the oneof payload from Zoom."
        [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
        [#(instance? cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom %) => (? map?)]
        (cond (.hasSetZoomTableValue proto) {:set-zoom-table-value
                                               (parse-set-zoom-table-value
                                                 (.getSetZoomTableValue proto))}
              (.hasNextZoomTablePos proto) {:next-zoom-table-pos
                                              (parse-next-zoom-table-pos
                                                (.getNextZoomTablePos proto))}
              (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos
                                              (parse-prev-zoom-table-pos
                                                (.getPrevZoomTablePos proto))}))