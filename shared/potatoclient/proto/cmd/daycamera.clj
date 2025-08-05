(ns potatoclient.proto.cmd.daycamera
  "Generated protobuf functions."
  (:require [potatoclient.proto.ser :as types])
  (:import cmd.DayCamera.JonSharedCmdDayCamera$SetValue
           cmd.DayCamera.JonSharedCmdDayCamera$Move
           cmd.DayCamera.JonSharedCmdDayCamera$Offset
           cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel
           cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel
           cmd.DayCamera.JonSharedCmdDayCamera$Root
           cmd.DayCamera.JonSharedCmdDayCamera$GetPos
           cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode
           cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode
           cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode
           cmd.DayCamera.JonSharedCmdDayCamera$HaltAll
           cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode
           cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel
           cmd.DayCamera.JonSharedCmdDayCamera$Focus
           cmd.DayCamera.JonSharedCmdDayCamera$Zoom
           cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos
           cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos
           cmd.DayCamera.JonSharedCmdDayCamera$SetIris
           cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter
           cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris
           cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue
           cmd.DayCamera.JonSharedCmdDayCamera$Stop
           cmd.DayCamera.JonSharedCmdDayCamera$Start
           cmd.DayCamera.JonSharedCmdDayCamera$Photo
           cmd.DayCamera.JonSharedCmdDayCamera$Halt
           cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo
           cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom
           cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus
           cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable
           cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-set-value)
(declare build-move)
(declare build-offset)
(declare build-set-clahe-level)
(declare build-shift-clahe-level)
(declare build-root)
(declare build-get-pos)
(declare build-next-fx-mode)
(declare build-prev-fx-mode)
(declare build-refresh-fx-mode)
(declare build-halt-all)
(declare build-set-fx-mode)
(declare build-set-digital-zoom-level)
(declare build-focus)
(declare build-zoom)
(declare build-next-zoom-table-pos)
(declare build-prev-zoom-table-pos)
(declare build-set-iris)
(declare build-set-infra-red-filter)
(declare build-set-auto-iris)
(declare build-set-zoom-table-value)
(declare build-stop)
(declare build-start)
(declare build-photo)
(declare build-halt)
(declare build-get-meteo)
(declare build-reset-zoom)
(declare build-reset-focus)
(declare build-save-to-table)
(declare build-save-to-table-focus)
(declare parse-set-value)
(declare parse-move)
(declare parse-offset)
(declare parse-set-clahe-level)
(declare parse-shift-clahe-level)
(declare parse-root)
(declare parse-get-pos)
(declare parse-next-fx-mode)
(declare parse-prev-fx-mode)
(declare parse-refresh-fx-mode)
(declare parse-halt-all)
(declare parse-set-fx-mode)
(declare parse-set-digital-zoom-level)
(declare parse-focus)
(declare parse-zoom)
(declare parse-next-zoom-table-pos)
(declare parse-prev-zoom-table-pos)
(declare parse-set-iris)
(declare parse-set-infra-red-filter)
(declare parse-set-auto-iris)
(declare parse-set-zoom-table-value)
(declare parse-stop)
(declare parse-start)
(declare parse-photo)
(declare parse-halt)
(declare parse-get-meteo)
(declare parse-reset-zoom)
(declare parse-reset-focus)
(declare parse-save-to-table)
(declare parse-save-to-table-focus)
(declare build-root-payload)
(declare build-focus-payload)
(declare build-zoom-payload)
(declare parse-root-payload)
(declare parse-focus-payload)
(declare parse-zoom-payload)

(defn build-set-value
  "Build a SetValue protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-move
  "Build a Move protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Move/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(defn build-offset
  "Build a Offset protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Offset/newBuilder)]
    ;; Set regular fields
    (when (contains? m :offset-value)
      (.setOffsetValue builder (get m :offset-value)))
    (.build builder)))

(defn build-set-clahe-level
  "Build a SetClaheLevel protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-shift-clahe-level
  "Build a ShiftClaheLevel protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter
                                  (fn [[k v]]
                                    (#{:focus :zoom :set-iris
                                       :set-infra-red-filter :start :stop :photo
                                       :set-auto-iris :halt-all :set-fx-mode
                                       :next-fx-mode :prev-fx-mode :get-meteo
                                       :refresh-fx-mode :set-digital-zoom-level
                                       :set-clahe-level :shift-clahe-level}
                                     k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-get-pos
  "Build a GetPos protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetPos/newBuilder)]
    (.build builder)))

(defn build-next-fx-mode
  "Build a NextFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode/newBuilder)]
    (.build builder)))

(defn build-prev-fx-mode
  "Build a PrevFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode/newBuilder)]
    (.build builder)))

(defn build-refresh-fx-mode
  "Build a RefreshFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode/newBuilder)]
    (.build builder)))

(defn build-halt-all
  "Build a HaltAll protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$HaltAll/newBuilder)]
    (.build builder)))

(defn build-set-fx-mode
  "Build a SetFxMode protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder
                (get types/jon-gui-data-fx-mode-day-values (get m :mode))))
    (.build builder)))

(defn build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-focus
  "Build a Focus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Focus/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-value :move :halt :offset
                                             :reset-focus :save-to-table-focus}
                                           k))
                                  m))]
      (build-focus-payload builder cmd-field))
    (.build builder)))

(defn build-zoom
  "Build a Zoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Zoom/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:set-value :move :halt
                                             :set-zoom-table-value
                                             :next-zoom-table-pos
                                             :prev-zoom-table-pos :offset
                                             :reset-zoom :save-to-table}
                                           k))
                                  m))]
      (build-zoom-payload builder cmd-field))
    (.build builder)))

(defn build-next-zoom-table-pos
  "Build a NextZoomTablePos protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos/newBuilder)]
    (.build builder)))

(defn build-prev-zoom-table-pos
  "Build a PrevZoomTablePos protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos/newBuilder)]
    (.build builder)))

(defn build-set-iris
  "Build a SetIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-infra-red-filter
  "Build a SetInfraRedFilter protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-auto-iris
  "Build a SetAutoIris protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-zoom-table-value
  "Build a SetZoomTableValue protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Stop/newBuilder)]
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Start/newBuilder)]
    (.build builder)))

(defn build-photo
  "Build a Photo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Photo/newBuilder)]
    (.build builder)))

(defn build-halt
  "Build a Halt protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Halt/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-reset-zoom
  "Build a ResetZoom protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom/newBuilder)]
    (.build builder)))

(defn build-reset-focus
  "Build a ResetFocus protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus/newBuilder)]
    (.build builder)))

(defn build-save-to-table
  "Build a SaveToTable protobuf message from a map."
  [m]
  (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable/newBuilder)]
    (.build builder)))

(defn build-save-to-table-focus
  "Build a SaveToTableFocus protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus/newBuilder)]
    (.build builder)))

(defn parse-set-value
  "Parse a SetValue protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-move
  "Parse a Move protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Move proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :target-value (.getTargetValue proto))
    true (assoc :speed (.getSpeed proto))))

(defn parse-offset
  "Parse a Offset protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Offset proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :offset-value (.getOffsetValue proto))))

(defn parse-set-clahe-level
  "Parse a SetClaheLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-shift-clahe-level
  "Parse a ShiftClaheLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-get-pos
  "Parse a GetPos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetPos proto]
  {})

(defn parse-next-fx-mode
  "Parse a NextFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode proto]
  {})

(defn parse-prev-fx-mode
  "Parse a PrevFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode proto]
  {})

(defn parse-refresh-fx-mode
  "Parse a RefreshFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode proto]
  {})

(defn parse-halt-all
  "Parse a HaltAll protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$HaltAll proto]
  {})

(defn parse-set-fx-mode
  "Parse a SetFxMode protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode
           (get types/jon-gui-data-fx-mode-day-keywords (.getMode proto)))))

(defn parse-set-digital-zoom-level
  "Parse a SetDigitalZoomLevel protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-focus
  "Parse a Focus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-focus-payload proto))))

(defn parse-zoom
  "Parse a Zoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-zoom-payload proto))))

(defn parse-next-zoom-table-pos
  "Parse a NextZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos proto]
  {})

(defn parse-prev-zoom-table-pos
  "Parse a PrevZoomTablePos protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos proto]
  {})

(defn parse-set-iris
  "Parse a SetIris protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetIris proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-infra-red-filter
  "Parse a SetInfraRedFilter protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-auto-iris
  "Parse a SetAutoIris protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-zoom-table-value
  "Parse a SetZoomTableValue protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Stop proto]
  {})

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Start proto]
  {})

(defn parse-photo
  "Parse a Photo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Photo proto]
  {})

(defn parse-halt
  "Parse a Halt protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Halt proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo proto]
  {})

(defn parse-reset-zoom
  "Parse a ResetZoom protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom proto]
  {})

(defn parse-reset-focus
  "Parse a ResetFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus proto]
  {})

(defn parse-save-to-table
  "Parse a SaveToTable protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable proto]
  {})

(defn parse-save-to-table-focus
  "Parse a SaveToTableFocus protobuf message to a map."
  [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :focus (.setFocus builder (build-focus value))
    :zoom (.setZoom builder (build-zoom value))
    :set-iris (.setSetIris builder (build-set-iris value))
    :set-infra-red-filter
      (.setSetInfraRedFilter builder (build-set-infra-red-filter value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :photo (.setPhoto builder (build-photo value))
    :set-auto-iris (.setSetAutoIris builder (build-set-auto-iris value))
    :halt-all (.setHaltAll builder (build-halt-all value))
    :set-fx-mode (.setSetFxMode builder (build-set-fx-mode value))
    :next-fx-mode (.setNextFxMode builder (build-next-fx-mode value))
    :prev-fx-mode (.setPrevFxMode builder (build-prev-fx-mode value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :refresh-fx-mode (.setRefreshFxMode builder (build-refresh-fx-mode value))
    :set-digital-zoom-level
      (.setSetDigitalZoomLevel builder (build-set-digital-zoom-level value))
    :set-clahe-level (.setSetClaheLevel builder (build-set-clahe-level value))
    :shift-clahe-level (.setShiftClaheLevel builder
                                            (build-shift-clahe-level value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-focus-payload
  "Build the oneof payload for Focus."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-value value))
    :move (.setMove builder (build-move value))
    :halt (.setHalt builder (build-halt value))
    :offset (.setOffset builder (build-offset value))
    :reset-focus (.setResetFocus builder (build-reset-focus value))
    :save-to-table-focus
      (.setSaveToTableFocus builder (build-save-to-table-focus value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn build-zoom-payload
  "Build the oneof payload for Zoom."
  [builder [field-key value]]
  (case field-key
    :set-value (.setSetValue builder (build-set-value value))
    :move (.setMove builder (build-move value))
    :halt (.setHalt builder (build-halt value))
    :set-zoom-table-value
      (.setSetZoomTableValue builder (build-set-zoom-table-value value))
    :next-zoom-table-pos
      (.setNextZoomTablePos builder (build-next-zoom-table-pos value))
    :prev-zoom-table-pos
      (.setPrevZoomTablePos builder (build-prev-zoom-table-pos value))
    :offset (.setOffset builder (build-offset value))
    :reset-zoom (.setResetZoom builder (build-reset-zoom value))
    :save-to-table (.setSaveToTable builder (build-save-to-table value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  (cond (.hasFocus proto) {:focus (parse-focus (.getFocus proto))}
        (.hasZoom proto) {:zoom (parse-zoom (.getZoom proto))}
        (.hasSetIris proto) {:set-iris (parse-set-iris (.getSetIris proto))}
        (.hasSetInfraRedFilter proto) {:set-infra-red-filter
                                         (parse-set-infra-red-filter
                                           (.getSetInfraRedFilter proto))}
        (.hasStart proto) {:start (parse-start (.getStart proto))}
        (.hasStop proto) {:stop (parse-stop (.getStop proto))}
        (.hasPhoto proto) {:photo (parse-photo (.getPhoto proto))}
        (.hasSetAutoIris proto) {:set-auto-iris (parse-set-auto-iris
                                                  (.getSetAutoIris proto))}
        (.hasHaltAll proto) {:halt-all (parse-halt-all (.getHaltAll proto))}
        (.hasSetFxMode proto) {:set-fx-mode (parse-set-fx-mode (.getSetFxMode
                                                                 proto))}
        (.hasNextFxMode proto) {:next-fx-mode (parse-next-fx-mode
                                                (.getNextFxMode proto))}
        (.hasPrevFxMode proto) {:prev-fx-mode (parse-prev-fx-mode
                                                (.getPrevFxMode proto))}
        (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
        (.hasRefreshFxMode proto)
          {:refresh-fx-mode (parse-refresh-fx-mode (.getRefreshFxMode proto))}
        (.hasSetDigitalZoomLevel proto) {:set-digital-zoom-level
                                           (parse-set-digital-zoom-level
                                             (.getSetDigitalZoomLevel proto))}
        (.hasSetClaheLevel proto)
          {:set-clahe-level (parse-set-clahe-level (.getSetClaheLevel proto))}
        (.hasShiftClaheLevel proto) {:shift-clahe-level (parse-shift-clahe-level
                                                          (.getShiftClaheLevel
                                                            proto))}))

(defn parse-focus-payload
  "Parse the oneof payload from Focus."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
  (cond (.hasSetValue proto) {:set-value (parse-set-value (.getSetValue proto))}
        (.hasMove proto) {:move (parse-move (.getMove proto))}
        (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
        (.hasOffset proto) {:offset (parse-offset (.getOffset proto))}
        (.hasResetFocus proto) {:reset-focus (parse-reset-focus (.getResetFocus
                                                                  proto))}
        (.hasSaveToTableFocus proto) {:save-to-table-focus
                                        (parse-save-to-table-focus
                                          (.getSaveToTableFocus proto))}))

(defn parse-zoom-payload
  "Parse the oneof payload from Zoom."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
  (cond (.hasSetValue proto) {:set-value (parse-set-value (.getSetValue proto))}
        (.hasMove proto) {:move (parse-move (.getMove proto))}
        (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
        (.hasSetZoomTableValue proto) {:set-zoom-table-value
                                         (parse-set-zoom-table-value
                                           (.getSetZoomTableValue proto))}
        (.hasNextZoomTablePos proto) {:next-zoom-table-pos
                                        (parse-next-zoom-table-pos
                                          (.getNextZoomTablePos proto))}
        (.hasPrevZoomTablePos proto) {:prev-zoom-table-pos
                                        (parse-prev-zoom-table-pos
                                          (.getPrevZoomTablePos proto))}
        (.hasOffset proto) {:offset (parse-offset (.getOffset proto))}
        (.hasResetZoom proto) {:reset-zoom (parse-reset-zoom (.getResetZoom
                                                               proto))}
        (.hasSaveToTable proto) {:save-to-table (parse-save-to-table
                                                  (.getSaveToTable proto))}))