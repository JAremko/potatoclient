(ns potatoclient.proto.cmd.heatcamera
  "Generated protobuf functions."
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

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:zoom :set-agc :set-filter :start :stop :photo :zoom-in :zoom-out :zoom-stop :focus-in :focus-out :focus-stop :calibrate :set-dde-level :enable-dde :disable-dde :set-auto-focus :focus-step-plus :focus-step-minus :set-fx-mode :next-fx-mode :prev-fx-mode :get-meteo :shift-dde :refresh-fx-mode :reset-zoom :save-to-table :set-calib-mode :set-digital-zoom-level :set-clahe-level :shift-clahe-level} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get jon-gui-data-fx-mode-heat-values (get m :mode))))

    (.build builder)))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode/newBuilder)]

    (.build builder)))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode/newBuilder)]

    (.build builder)))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode/newBuilder)]

    (.build builder)))

(defn build-enable-dde
  "Build a EnableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE/newBuilder)]

    (.build builder)))

(defn build-disable-dde
  "Build a DisableDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE/newBuilder)]

    (.build builder)))

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-set-dde-level
  "Build a SetDDELevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-shift-dde
  "Build a ShiftDDE protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-zoom-in
  "Build a ZoomIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn/newBuilder)]

    (.build builder)))

(defn build-zoom-out
  "Build a ZoomOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut/newBuilder)]

    (.build builder)))

(defn build-zoom-stop
  "Build a ZoomStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop/newBuilder)]

    (.build builder)))

(defn build-focus-in
  "Build a FocusIn protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn/newBuilder)]

    (.build builder)))

(defn build-focus-out
  "Build a FocusOut protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut/newBuilder)]

    (.build builder)))

(defn build-focus-stop
  "Build a FocusStop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop/newBuilder)]

    (.build builder)))

(defn build-focus-step-plus
  "Build a FocusStepPlus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus/newBuilder)]

    (.build builder)))

(defn build-focus-step-minus
  "Build a FocusStepMinus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus/newBuilder)]

    (.build builder)))

(defn build-calibrate
  "Build a Calibrate protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate/newBuilder)]

    (.build builder)))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:set-zoom-table-value :next-zoom-table-pos :prev-zoom-table-pos} k)) m))]
      (build-zoom-payload builder cmd-field))
    (.build builder)))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos/newBuilder)]

    (.build builder)))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos/newBuilder)]

    (.build builder)))

(defn build-set-calib-mode
  "Build a SetCalibMode protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode/newBuilder)]

    (.build builder)))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-set-agc
  "Build a SetAGC protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get jon-gui-data-video-channel-heat-agc-modes-values (get m :value))))

    (.build builder)))

(defn build-set-filters
  "Build a SetFilters protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get jon-gui-data-video-channel-heat-filters-values (get m :value))))

    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Start/newBuilder)]

    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Stop/newBuilder)]

    (.build builder)))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Halt/newBuilder)]

    (.build builder)))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$Photo/newBuilder)]

    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo/newBuilder)]

    (.build builder)))

(defn build-set-auto-focus
  "Build a SetAutoFocus protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value)
      (.setValue builder (get m :value)))

    (.build builder)))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom/newBuilder)]

    (.build builder)))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode (get jon-gui-data-fx-mode-heat-keywords (.getMode proto)))))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode proto]
  {})

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode proto]
  {})

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode proto]
  {})

(defn parse-enable-dde
  "Parse a EnableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE proto]
  {})

(defn parse-disable-dde
  "Parse a DisableDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE proto]
  {})

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-dde-level
  "Parse a SetDDELevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-shift-dde
  "Parse a ShiftDDE protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-zoom-in
  "Parse a ZoomIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn proto]
  {})

(defn parse-zoom-out
  "Parse a ZoomOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut proto]
  {})

(defn parse-zoom-stop
  "Parse a ZoomStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop proto]
  {})

(defn parse-focus-in
  "Parse a FocusIn protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn proto]
  {})

(defn parse-focus-out
  "Parse a FocusOut protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut proto]
  {})

(defn parse-focus-stop
  "Parse a FocusStop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop proto]
  {})

(defn parse-focus-step-plus
  "Parse a FocusStepPlus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus proto]
  {})

(defn parse-focus-step-minus
  "Parse a FocusStepMinus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus proto]
  {})

(defn parse-calibrate
  "Parse a Calibrate protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate proto]
  {})

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos proto]
  {})

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos proto]
  {})

(defn parse-set-calib-mode
  "Parse a SetCalibMode protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode proto]
  {})

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-agc
  "Parse a SetAGC protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (get jon-gui-data-video-channel-heat-agc-modes-keywords (.getValue proto)))))

(defn parse-set-filters
  "Parse a SetFilters protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (get jon-gui-data-video-channel-heat-filters-keywords (.getValue proto)))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Stop proto]
  {})

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Halt proto]
  {})

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Photo proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo proto]
  {})

(defn parse-set-auto-focus
  "Parse a SetAutoFocus protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom proto]
  {})

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
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
    :focus-step-minus (.setFocusStepMinus builder (build-focus-step-minus value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :shift-dde (.setShiftDde builder (build-shift-dde value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom value))
    :save-to-table (.setSaveToTable builder (build-save-to-table value))
    :set-calib-mode (.setSetCalibMode builder (build-set-calib-mode value))
    :set-digital-zoom-level (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level value))
    :shift-clahe-level (.setShiftClaheLevel builder (build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn build-zoom-payload
  "Build the oneof payload for Zoom."
  [builder [field-key value]]
  (case field-key
    :set-zoom-table-value (.setSetZoomTableValue builder (build-set-zoom-table-value value))
    :next-zoom-table-pos (.setNextZoomTablePos builder (build-next-zoom-table-pos value))
    :prev-zoom-table-pos (.setPrevZoomTablePos builder (build-prev-zoom-table-pos value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Root proto]
  (cond
    (.hasZoom proto) {:zoom (parse-zoom (.getZoom proto))}
    (.hasSetAgc proto) {:set-agc (parse-set-agc (.getSetAgc proto))}
    (.hasSetFilter proto) {:set-filter (parse-set-filters (.getSetFilter proto))}
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
    (.hasSetDdeLevel proto) {:set-dde-level (parse-set-dde-level (.getSetDdeLevel proto))}
    (.hasEnableDde proto) {:enable-dde (parse-enable-dde (.getEnableDde proto))}
    (.hasDisableDde proto) {:disable-dde (parse-disable-dde (.getDisableDde proto))}
    (.hasSetAutoFocus proto) {:set-auto-focus (parse-set-auto-focus (.getSetAutoFocus proto))}
    (.hasFocusStepPlus proto) {:focus-step-plus (parse-focus-step-plus (.getFocusStepPlus proto))}
    (.hasFocusStepMinus proto) {:focus-step-minus (parse-focus-step-minus (.getFocusStepMinus proto))}
    (.hasSetFxMode proto) {:set-fx-mode (parse-set-fx-mode (.getSetFxMode proto))}
    (.hasNextFxMode proto) {:next-fx-mode (parse-next-fx-mode (.getNextFxMode proto))}
    (.hasPrevFxMode proto) {:prev-fx-mode (parse-prev-fx-mode (.getPrevFxMode proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasShiftDde proto) {:shift-dde (parse-shift-dde (.getShiftDde proto))}
    (.hasRefreshFxMode proto) {:refresh-fx-mode (parse-refresh-fx-mode (.getRefreshFxMode proto))}
    (.hasResetZoom proto) {:reset-zoom (parse-reset-zoom (.getResetZoom proto))}
    (.hasSaveToTable proto) {:save-to-table (parse-save-to-table (.getSaveToTable proto))}
    (.hasSetCalibMode proto) {:set-calib-mode (parse-set-calib-mode (.getSetCalibMode proto))}
    (.hasSetDigitalZoomLevel proto) {:set-digital-zoom-level (parse-set-digital-zoom-level (.getSetDigitalZoomLevel proto))}
    (.hasSetClaheLevel proto) {:set-clahe-level (parse-set-clahe-level (.getSetClaheLevel proto))}
    (.hasShiftClaheLevel proto) {:shift-clahe-level (parse-shift-clahe-level (.getShiftClaheLevel proto))}))

(defn parse-zoom-payload
  "Parse the oneof payload from Zoom."
  [^cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom proto]
  (cond
    (.hasSetZoomTableValue proto) {:set-zoom-table-value (parse-set-zoom-table-value (.getSetZoomTableValue proto))}
    (.hasNextZoomTablePos proto) {:next-zoom-table-pos (parse-next-zoom-table-pos (.getNextZoomTablePos proto))}
    (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos (parse-prev-zoom-table-pos (.getPrevZoomTablePos proto))}))