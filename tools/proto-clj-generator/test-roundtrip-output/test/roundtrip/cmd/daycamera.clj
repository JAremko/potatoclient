(ns test.roundtrip.cmd.daycamera
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [test.roundtrip.ser :as types])
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
;; Malli Specs
;; =============================================================================

(def set-value-spec
  "Malli spec for set-value message"
  [:map [:value {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def move-spec
  "Malli spec for move message"
  [:map [:target-value {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def offset-spec
  "Malli spec for offset message"
  [:map [:offset-value {:optional true} [:and :float [:>= -1] [:<= 1]]]])

(def set-clahe-level-spec
  "Malli spec for set-clahe-level message"
  [:map [:value {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def shift-clahe-level-spec
  "Malli spec for shift-clahe-level message"
  [:map [:value {:optional true} [:and :float [:>= -1] [:<= 1]]]])

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:oneof
     {:zoom [:map [:zoom :cmd.day-camera/zoom]],
      :set-infra-red-filter
        [:map [:set-infra-red-filter :cmd.day-camera/set-infra-red-filter]],
      :set-clahe-level [:map
                        [:set-clahe-level :cmd.day-camera/set-clahe-level]],
      :prev-fx-mode [:map [:prev-fx-mode :cmd.day-camera/prev-fx-mode]],
      :start [:map [:start :cmd.day-camera/start]],
      :halt-all [:map [:halt-all :cmd.day-camera/halt-all]],
      :set-digital-zoom-level
        [:map [:set-digital-zoom-level :cmd.day-camera/set-digital-zoom-level]],
      :stop [:map [:stop :cmd.day-camera/stop]],
      :photo [:map [:photo :cmd.day-camera/photo]],
      :get-meteo [:map [:get-meteo :cmd.day-camera/get-meteo]],
      :error/message "This oneof field is required",
      :focus [:map [:focus :cmd.day-camera/focus]],
      :set-fx-mode [:map [:set-fx-mode :cmd.day-camera/set-fx-mode]],
      :set-iris [:map [:set-iris :cmd.day-camera/set-iris]],
      :refresh-fx-mode [:map
                        [:refresh-fx-mode :cmd.day-camera/refresh-fx-mode]],
      :set-auto-iris [:map [:set-auto-iris :cmd.day-camera/set-auto-iris]],
      :next-fx-mode [:map [:next-fx-mode :cmd.day-camera/next-fx-mode]],
      :shift-clahe-level
        [:map [:shift-clahe-level :cmd.day-camera/shift-clahe-level]]}]]])

(def get-pos-spec "Malli spec for get-pos message" [:map])

(def next-fx-mode-spec "Malli spec for next-fx-mode message" [:map])

(def prev-fx-mode-spec "Malli spec for prev-fx-mode message" [:map])

(def refresh-fx-mode-spec "Malli spec for refresh-fx-mode message" [:map])

(def halt-all-spec "Malli spec for halt-all message" [:map])

(def set-fx-mode-spec
  "Malli spec for set-fx-mode message"
  [:map
   [:mode {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-fx-mode-day-spec]])

(def set-digital-zoom-level-spec
  "Malli spec for set-digital-zoom-level message"
  [:map [:value {:optional true} [:and :float [:>= 1]]]])

(def focus-spec
  "Malli spec for focus message"
  [:map
   [:cmd
    [:oneof
     {:set-value [:map [:set-value :cmd.day-camera/set-value]],
      :move [:map [:move :cmd.day-camera/move]],
      :halt [:map [:halt :cmd.day-camera/halt]],
      :offset [:map [:offset :cmd.day-camera/offset]],
      :reset-focus [:map [:reset-focus :cmd.day-camera/reset-focus]],
      :save-to-table-focus
        [:map [:save-to-table-focus :cmd.day-camera/save-to-table-focus]],
      :error/message "This oneof field is required"}]]])

(def zoom-spec
  "Malli spec for zoom message"
  [:map
   [:cmd
    [:oneof
     {:prev-zoom-table-pos
        [:map [:prev-zoom-table-pos :cmd.day-camera/prev-zoom-table-pos]],
      :offset [:map [:offset :cmd.day-camera/offset]],
      :move [:map [:move :cmd.day-camera/move]],
      :reset-zoom [:map [:reset-zoom :cmd.day-camera/reset-zoom]],
      :next-zoom-table-pos
        [:map [:next-zoom-table-pos :cmd.day-camera/next-zoom-table-pos]],
      :error/message "This oneof field is required",
      :set-value [:map [:set-value :cmd.day-camera/set-value]],
      :set-zoom-table-value
        [:map [:set-zoom-table-value :cmd.day-camera/set-zoom-table-value]],
      :halt [:map [:halt :cmd.day-camera/halt]],
      :save-to-table [:map [:save-to-table :cmd.day-camera/save-to-table]]}]]])

(def next-zoom-table-pos-spec
  "Malli spec for next-zoom-table-pos message"
  [:map])

(def prev-zoom-table-pos-spec
  "Malli spec for prev-zoom-table-pos message"
  [:map])

(def set-iris-spec
  "Malli spec for set-iris message"
  [:map [:value {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def set-infra-red-filter-spec
  "Malli spec for set-infra-red-filter message"
  [:map [:value {:optional true} :boolean]])

(def set-auto-iris-spec
  "Malli spec for set-auto-iris message"
  [:map [:value {:optional true} :boolean]])

(def set-zoom-table-value-spec
  "Malli spec for set-zoom-table-value message"
  [:map [:value {:optional true} :int]])

(def stop-spec "Malli spec for stop message" [:map])

(def start-spec "Malli spec for start message" [:map])

(def photo-spec "Malli spec for photo message" [:map])

(def halt-spec "Malli spec for halt message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def reset-zoom-spec "Malli spec for reset-zoom message" [:map])

(def reset-focus-spec "Malli spec for reset-focus message" [:map])

(def save-to-table-spec "Malli spec for save-to-table message" [:map])

(def save-to-table-focus-spec
  "Malli spec for save-to-table-focus message"
  [:map])

;; =============================================================================
;; Registry Setup
;; =============================================================================

;; Registry for enum and message specs in this namespace
(def registry
  {:cmd.DayCamera/set-value set-value-spec,
   :cmd.DayCamera/move move-spec,
   :cmd.DayCamera/offset offset-spec,
   :cmd.DayCamera/set-clahe-level set-clahe-level-spec,
   :cmd.DayCamera/shift-clahe-level shift-clahe-level-spec,
   :cmd.DayCamera/root root-spec,
   :cmd.DayCamera/get-pos get-pos-spec,
   :cmd.DayCamera/next-fx-mode next-fx-mode-spec,
   :cmd.DayCamera/prev-fx-mode prev-fx-mode-spec,
   :cmd.DayCamera/refresh-fx-mode refresh-fx-mode-spec,
   :cmd.DayCamera/halt-all halt-all-spec,
   :cmd.DayCamera/set-fx-mode set-fx-mode-spec,
   :cmd.DayCamera/set-digital-zoom-level set-digital-zoom-level-spec,
   :cmd.DayCamera/focus focus-spec,
   :cmd.DayCamera/zoom zoom-spec,
   :cmd.DayCamera/next-zoom-table-pos next-zoom-table-pos-spec,
   :cmd.DayCamera/prev-zoom-table-pos prev-zoom-table-pos-spec,
   :cmd.DayCamera/set-iris set-iris-spec,
   :cmd.DayCamera/set-infra-red-filter set-infra-red-filter-spec,
   :cmd.DayCamera/set-auto-iris set-auto-iris-spec,
   :cmd.DayCamera/set-zoom-table-value set-zoom-table-value-spec,
   :cmd.DayCamera/stop stop-spec,
   :cmd.DayCamera/start start-spec,
   :cmd.DayCamera/photo photo-spec,
   :cmd.DayCamera/halt halt-spec,
   :cmd.DayCamera/get-meteo get-meteo-spec,
   :cmd.DayCamera/reset-zoom reset-zoom-spec,
   :cmd.DayCamera/reset-focus reset-focus-spec,
   :cmd.DayCamera/save-to-table save-to-table-spec,
   :cmd.DayCamera/save-to-table-focus save-to-table-focus-spec})

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

(>defn build-set-value
       "Build a SetValue protobuf message from a map."
       [m]
       [set-value-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetValue/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-move
       "Build a Move protobuf message from a map."
       [m]
       [move-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Move/newBuilder)]
         ;; Set regular fields
         (when (contains? m :target-value)
           (.setTargetValue builder (get m :target-value)))
         (when (contains? m :speed) (.setSpeed builder (get m :speed)))
         (.build builder)))

(>defn build-offset
       "Build a Offset protobuf message from a map."
       [m]
       [offset-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Offset/newBuilder)]
         ;; Set regular fields
         (when (contains? m :offset-value)
           (.setOffsetValue builder (get m :offset-value)))
         (.build builder)))

(>defn build-set-clahe-level
       "Build a SetClaheLevel protobuf message from a map."
       [m]
       [set-clahe-level-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-shift-clahe-level
       "Build a ShiftClaheLevel protobuf message from a map."
       [m]
       [shift-clahe-level-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first
                                (filter
                                  (fn [[k v]]
                                    (#{:focus :zoom :set-iris
                                       :set-infra-red-filter :start :stop :photo
                                       :set-auto-iris :halt-all :set-fx-mode
                                       :next-fx-mode :prev-fx-mode :get-meteo
                                       :refresh-fx-mode :set-digital-zoom-level
                                       :set-clahe-level :shift-clahe-level}
                                     k))
                                  (:cmd m)))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-get-pos
       "Build a GetPos protobuf message from a map."
       [m]
       [get-pos-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetPos/newBuilder)]
         (.build builder)))

(>defn build-next-fx-mode
       "Build a NextFxMode protobuf message from a map."
       [m]
       [next-fx-mode-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode/newBuilder)]
         (.build builder)))

(>defn build-prev-fx-mode
       "Build a PrevFxMode protobuf message from a map."
       [m]
       [prev-fx-mode-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode/newBuilder)]
         (.build builder)))

(>defn build-refresh-fx-mode
       "Build a RefreshFxMode protobuf message from a map."
       [m]
       [refresh-fx-mode-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode/newBuilder)]
         (.build builder)))

(>defn build-halt-all
       "Build a HaltAll protobuf message from a map."
       [m]
       [halt-all-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$HaltAll/newBuilder)]
         (.build builder)))

(>defn build-set-fx-mode
       "Build a SetFxMode protobuf message from a map."
       [m]
       [set-fx-mode-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :mode)
           (.setMode builder
                     (when-let [v (get m :mode)]
                       (get types/jon-gui-data-fx-mode-day-values v))))
         (.build builder)))

(>defn
  build-set-digital-zoom-level
  "Build a SetDigitalZoomLevel protobuf message from a map."
  [m]
  [set-digital-zoom-level-spec => any?]
  (let [builder
          (cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(>defn build-focus
       "Build a Focus protobuf message from a map."
       [m]
       [focus-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Focus/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set-value :move :halt :offset
                                                  :reset-focus
                                                  :save-to-table-focus}
                                                k))
                                       (:cmd m)))]
           (build-focus-payload builder cmd-field))
         (.build builder)))

(>defn build-zoom
       "Build a Zoom protobuf message from a map."
       [m]
       [zoom-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Zoom/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set-value :move :halt
                                                  :set-zoom-table-value
                                                  :next-zoom-table-pos
                                                  :prev-zoom-table-pos :offset
                                                  :reset-zoom :save-to-table}
                                                k))
                                       (:cmd m)))]
           (build-zoom-payload builder cmd-field))
         (.build builder)))

(>defn build-next-zoom-table-pos
       "Build a NextZoomTablePos protobuf message from a map."
       [m]
       [next-zoom-table-pos-spec => any?]
       (let
         [builder
            (cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos/newBuilder)]
         (.build builder)))

(>defn build-prev-zoom-table-pos
       "Build a PrevZoomTablePos protobuf message from a map."
       [m]
       [prev-zoom-table-pos-spec => any?]
       (let
         [builder
            (cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos/newBuilder)]
         (.build builder)))

(>defn build-set-iris
       "Build a SetIris protobuf message from a map."
       [m]
       [set-iris-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$SetIris/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-set-infra-red-filter
       "Build a SetInfraRedFilter protobuf message from a map."
       [m]
       [set-infra-red-filter-spec => any?]
       (let
         [builder
            (cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-set-auto-iris
       "Build a SetAutoIris protobuf message from a map."
       [m]
       [set-auto-iris-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-set-zoom-table-value
       "Build a SetZoomTableValue protobuf message from a map."
       [m]
       [set-zoom-table-value-spec => any?]
       (let
         [builder
            (cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-stop
       "Build a Stop protobuf message from a map."
       [m]
       [stop-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Stop/newBuilder)]
         (.build builder)))

(>defn build-start
       "Build a Start protobuf message from a map."
       [m]
       [start-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Start/newBuilder)]
         (.build builder)))

(>defn build-photo
       "Build a Photo protobuf message from a map."
       [m]
       [photo-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Photo/newBuilder)]
         (.build builder)))

(>defn build-halt
       "Build a Halt protobuf message from a map."
       [m]
       [halt-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$Halt/newBuilder)]
         (.build builder)))

(>defn build-get-meteo
       "Build a GetMeteo protobuf message from a map."
       [m]
       [get-meteo-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo/newBuilder)]
         (.build builder)))

(>defn build-reset-zoom
       "Build a ResetZoom protobuf message from a map."
       [m]
       [reset-zoom-spec => any?]
       (let [builder (cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom/newBuilder)]
         (.build builder)))

(>defn build-reset-focus
       "Build a ResetFocus protobuf message from a map."
       [m]
       [reset-focus-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus/newBuilder)]
         (.build builder)))

(>defn build-save-to-table
       "Build a SaveToTable protobuf message from a map."
       [m]
       [save-to-table-spec => any?]
       (let [builder
               (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable/newBuilder)]
         (.build builder)))

(>defn build-save-to-table-focus
       "Build a SaveToTableFocus protobuf message from a map."
       [m]
       [save-to-table-focus-spec => any?]
       (let
         [builder
            (cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus/newBuilder)]
         (.build builder)))

(>defn parse-set-value
       "Parse a SetValue protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetValue proto]
       [any? => set-value-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-move
       "Parse a Move protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Move proto]
       [any? => move-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :target-value (.getTargetValue proto))
         true (assoc :speed (.getSpeed proto))))

(>defn parse-offset
       "Parse a Offset protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Offset proto]
       [any? => offset-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :offset-value (.getOffsetValue proto))))

(>defn parse-set-clahe-level
       "Parse a SetClaheLevel protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel proto]
       [any? => set-clahe-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-shift-clahe-level
       "Parse a ShiftClaheLevel protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel proto]
       [any? => shift-clahe-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-root-payload proto) (assoc :cmd (parse-root-payload proto))))

(>defn parse-get-pos
       "Parse a GetPos protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$GetPos proto]
       [any? => get-pos-spec]
       {})

(>defn parse-next-fx-mode
       "Parse a NextFxMode protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode proto]
       [any? => next-fx-mode-spec]
       {})

(>defn parse-prev-fx-mode
       "Parse a PrevFxMode protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode proto]
       [any? => prev-fx-mode-spec]
       {})

(>defn parse-refresh-fx-mode
       "Parse a RefreshFxMode protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode proto]
       [any? => refresh-fx-mode-spec]
       {})

(>defn parse-halt-all
       "Parse a HaltAll protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$HaltAll proto]
       [any? => halt-all-spec]
       {})

(>defn parse-set-fx-mode
       "Parse a SetFxMode protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode proto]
       [any? => set-fx-mode-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :mode
                (get types/jon-gui-data-fx-mode-day-keywords
                     (.getMode proto)))))

(>defn parse-set-digital-zoom-level
       "Parse a SetDigitalZoomLevel protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel proto]
       [any? => set-digital-zoom-level-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-focus
       "Parse a Focus protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
       [any? => focus-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-focus-payload proto) (assoc :cmd (parse-focus-payload proto))))

(>defn parse-zoom
       "Parse a Zoom protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
       [any? => zoom-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-zoom-payload proto) (assoc :cmd (parse-zoom-payload proto))))

(>defn parse-next-zoom-table-pos
       "Parse a NextZoomTablePos protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos proto]
       [any? => next-zoom-table-pos-spec]
       {})

(>defn parse-prev-zoom-table-pos
       "Parse a PrevZoomTablePos protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos proto]
       [any? => prev-zoom-table-pos-spec]
       {})

(>defn parse-set-iris
       "Parse a SetIris protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetIris proto]
       [any? => set-iris-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-infra-red-filter
       "Parse a SetInfraRedFilter protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter proto]
       [any? => set-infra-red-filter-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-auto-iris
       "Parse a SetAutoIris protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris proto]
       [any? => set-auto-iris-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-zoom-table-value
       "Parse a SetZoomTableValue protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue proto]
       [any? => set-zoom-table-value-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-stop
       "Parse a Stop protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Stop proto]
       [any? => stop-spec]
       {})

(>defn parse-start
       "Parse a Start protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Start proto]
       [any? => start-spec]
       {})

(>defn parse-photo
       "Parse a Photo protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Photo proto]
       [any? => photo-spec]
       {})

(>defn parse-halt
       "Parse a Halt protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$Halt proto]
       [any? => halt-spec]
       {})

(>defn parse-get-meteo
       "Parse a GetMeteo protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo proto]
       [any? => get-meteo-spec]
       {})

(>defn parse-reset-zoom
       "Parse a ResetZoom protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom proto]
       [any? => reset-zoom-spec]
       {})

(>defn parse-reset-focus
       "Parse a ResetFocus protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus proto]
       [any? => reset-focus-spec]
       {})

(>defn parse-save-to-table
       "Parse a SaveToTable protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable proto]
       [any? => save-to-table-spec]
       {})

(>defn parse-save-to-table-focus
       "Parse a SaveToTableFocus protobuf message to a map."
       [^cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus proto]
       [any? => save-to-table-focus-spec]
       {})

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
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

(>defn- build-focus-payload
        "Build the oneof payload for Focus."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
        (case field-key
          :set-value (.setSetValue builder (build-set-value value))
          :move (.setMove builder (build-move value))
          :halt (.setHalt builder (build-halt value))
          :offset (.setOffset builder (build-offset value))
          :reset-focus (.setResetFocus builder (build-reset-focus value))
          :save-to-table-focus
            (.setSaveToTableFocus builder (build-save-to-table-focus value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":cmd"}))))

(>defn- build-zoom-payload
        "Build the oneof payload for Zoom."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
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
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":cmd"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamera.JonSharedCmdDayCamera$Root proto]
  [any? => (? map?)]
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

(>defn- parse-focus-payload
        "Parse the oneof payload from Focus."
        [^cmd.DayCamera.JonSharedCmdDayCamera$Focus proto]
        [any? => (? map?)]
        (cond (.hasSetValue proto) {:set-value (parse-set-value (.getSetValue
                                                                  proto))}
              (.hasMove proto) {:move (parse-move (.getMove proto))}
              (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
              (.hasOffset proto) {:offset (parse-offset (.getOffset proto))}
              (.hasResetFocus proto) {:reset-focus (parse-reset-focus
                                                     (.getResetFocus proto))}
              (.hasSaveToTableFocus proto) {:save-to-table-focus
                                              (parse-save-to-table-focus
                                                (.getSaveToTableFocus proto))}))

(>defn- parse-zoom-payload
        "Parse the oneof payload from Zoom."
        [^cmd.DayCamera.JonSharedCmdDayCamera$Zoom proto]
        [any? => (? map?)]
        (cond (.hasSetValue proto) {:set-value (parse-set-value (.getSetValue
                                                                  proto))}
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
              (.hasResetZoom proto) {:reset-zoom (parse-reset-zoom
                                                   (.getResetZoom proto))}
              (.hasSaveToTable proto)
                {:save-to-table (parse-save-to-table (.getSaveToTable proto))}))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for SetValue
;; Warning: Could not extract spec for field value

;; Validation helpers for Move
;; Warning: Could not extract spec for field target-value

;; Warning: Could not extract spec for field speed

;; Validation helpers for Offset
;; Warning: Could not extract spec for field offset-value

;; Validation helpers for SetClaheLevel
;; Warning: Could not extract spec for field value

;; Validation helpers for ShiftClaheLevel
;; Warning: Could not extract spec for field value

;; Validation helpers for SetFxMode
;; Warning: Could not extract spec for field mode

;; Validation helpers for SetDigitalZoomLevel
;; Warning: Could not extract spec for field value

;; Validation helpers for SetIris
;; Warning: Could not extract spec for field value

;; Validation helpers for SetZoomTableValue
;; Warning: Could not extract spec for field value