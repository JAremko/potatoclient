(ns test.roundtrip.cmd.rotaryplatform
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [test.roundtrip.ser :as types])
  (:import
    cmd.RotaryPlatform.JonSharedCmdRotary$Root
    cmd.RotaryPlatform.JonSharedCmdRotary$Axis
    cmd.RotaryPlatform.JonSharedCmdRotary$SetMode
    cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation
    cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet
    cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth
    cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation
    cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank
    cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo
    cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth
    cmd.RotaryPlatform.JonSharedCmdRotary$Start
    cmd.RotaryPlatform.JonSharedCmdRotary$Stop
    cmd.RotaryPlatform.JonSharedCmdRotary$Halt
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause
    cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth
    cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode
    cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode
    cmd.RotaryPlatform.JonSharedCmdRotary$Elevation
    cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS
    cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS
    cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC))

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
     {:rotate-to-gps [:map [:rotate-to-gps :cmd.rotary-platform/rotate-to-gps]],
      :scan-pause [:map [:scan-pause :cmd.rotary-platform/scan-pause]],
      :rotate-to-ndc [:map [:rotate-to-ndc :cmd.rotary-platform/rotate-to-ndc]],
      :scan-start [:map [:scan-start :cmd.rotary-platform/scan-start]],
      :set-platform-azimuth [:map
                             [:set-platform-azimuth
                              :cmd.rotary-platform/set-platform-azimuth]],
      :scan-stop [:map [:scan-stop :cmd.rotary-platform/scan-stop]],
      :start [:map [:start :cmd.rotary-platform/start]],
      :stop [:map [:stop :cmd.rotary-platform/stop]],
      :set-origin-gps [:map
                       [:set-origin-gps :cmd.rotary-platform/set-origin-gps]],
      :scan-next [:map [:scan-next :cmd.rotary-platform/scan-next]],
      :set-platform-bank
        [:map [:set-platform-bank :cmd.rotary-platform/set-platform-bank]],
      :get-meteo [:map [:get-meteo :cmd.rotary-platform/get-meteo]],
      :set-use-rotary-as-compass
        [:map
         [:set-use-rotary-as-compass
          :cmd.rotary-platform/set-use-rotary-as-compass]],
      :error/message "This oneof field is required",
      :scan-prev [:map [:scan-prev :cmd.rotary-platform/scan-prev]],
      :scan-add-node [:map [:scan-add-node :cmd.rotary-platform/scan-add-node]],
      :set-platform-elevation [:map
                               [:set-platform-elevation
                                :cmd.rotary-platform/set-platform-elevation]],
      :scan-select-node
        [:map [:scan-select-node :cmd.rotary-platform/scan-select-node]],
      :halt [:map [:halt :cmd.rotary-platform/halt]],
      :scan-delete-node
        [:map [:scan-delete-node :cmd.rotary-platform/scan-delete-node]],
      :axis [:map [:axis :cmd.rotary-platform/axis]],
      :scan-unpause [:map [:scan-unpause :cmd.rotary-platform/scan-unpause]],
      :set-mode [:map [:set-mode :cmd.rotary-platform/set-mode]],
      :scan-refresh-node-list [:map
                               [:scan-refresh-node-list
                                :cmd.rotary-platform/scan-refresh-node-list]],
      :scan-update-node
        [:map [:scan-update-node :cmd.rotary-platform/scan-update-node]]}]]])

(def axis-spec
  "Malli spec for axis message"
  [:map [:azimuth {:optional true} :cmd.rotary-platform/azimuth]
   [:elevation {:optional true} :cmd.rotary-platform/elevation]])

(def set-mode-spec
  "Malli spec for set-mode message"
  [:map
   [:mode {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-mode-spec]])

(def set-azimuth-value-spec
  "Malli spec for set-azimuth-value message"
  [:map [:value {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def rotate-azimuth-to-spec
  "Malli spec for rotate-azimuth-to message"
  [:map [:target-value {:optional true} [:and :float [:>= 0] [:< 360]]]
   [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def rotate-azimuth-spec
  "Malli spec for rotate-azimuth message"
  [:map [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def rotate-elevation-spec
  "Malli spec for rotate-elevation message"
  [:map [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def set-elevation-value-spec
  "Malli spec for set-elevation-value message"
  [:map [:value {:optional true} [:and :float [:>= -90] [:<= 90]]]])

(def rotate-elevation-to-spec
  "Malli spec for rotate-elevation-to message"
  [:map [:target-value {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]])

(def rotate-elevation-relative-spec
  "Malli spec for rotate-elevation-relative message"
  [:map [:value {:optional true} :float]
   [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def rotate-elevation-relative-set-spec
  "Malli spec for rotate-elevation-relative-set message"
  [:map [:value {:optional true} :float]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def rotate-azimuth-relative-spec
  "Malli spec for rotate-azimuth-relative message"
  [:map [:value {:optional true} :float]
   [:speed {:optional true} [:and :float [:>= 0] [:<= 1]]]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def rotate-azimuth-relative-set-spec
  "Malli spec for rotate-azimuth-relative-set message"
  [:map [:value {:optional true} :float]
   [:direction {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-rotary-direction-spec]])

(def set-platform-azimuth-spec
  "Malli spec for set-platform-azimuth message"
  [:map [:value {:optional true} [:and :float [:> -360] [:< 360]]]])

(def set-platform-elevation-spec
  "Malli spec for set-platform-elevation message"
  [:map [:value {:optional true} [:and :float [:>= -90] [:<= 90]]]])

(def set-platform-bank-spec
  "Malli spec for set-platform-bank message"
  [:map [:value {:optional true} [:and :float [:>= -180] [:< 180]]]])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def azimuth-spec
  "Malli spec for azimuth message"
  [:map
   [:cmd
    [:oneof
     {:set-value [:map [:set-value :cmd.rotary-platform/set-azimuth-value]],
      :rotate-to [:map [:rotate-to :cmd.rotary-platform/rotate-azimuth-to]],
      :rotate [:map [:rotate :cmd.rotary-platform/rotate-azimuth]],
      :relative [:map [:relative :cmd.rotary-platform/rotate-azimuth-relative]],
      :relative-set
        [:map [:relative-set :cmd.rotary-platform/rotate-azimuth-relative-set]],
      :halt [:map [:halt :cmd.rotary-platform/halt-azimuth]],
      :error/message "This oneof field is required"}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def halt-spec "Malli spec for halt message" [:map])

(def scan-start-spec "Malli spec for scan-start message" [:map])

(def scan-stop-spec "Malli spec for scan-stop message" [:map])

(def scan-pause-spec "Malli spec for scan-pause message" [:map])

(def scan-unpause-spec "Malli spec for scan-unpause message" [:map])

(def halt-azimuth-spec "Malli spec for halt-azimuth message" [:map])

(def halt-elevation-spec "Malli spec for halt-elevation message" [:map])

(def scan-prev-spec "Malli spec for scan-prev message" [:map])

(def scan-next-spec "Malli spec for scan-next message" [:map])

(def scan-refresh-node-list-spec
  "Malli spec for scan-refresh-node-list message"
  [:map])

(def scan-select-node-spec
  "Malli spec for scan-select-node message"
  [:map [:index {:optional true} :int]])

(def scan-delete-node-spec
  "Malli spec for scan-delete-node message"
  [:map [:index {:optional true} :int]])

(def scan-update-node-spec
  "Malli spec for scan-update-node message"
  [:map [:index {:optional true} :int]
   [:day-zoom-table-value {:optional true} :int]
   [:heat-zoom-table-value {:optional true} :int]
   [:azimuth {:optional true} [:and :double [:>= 0] [:< 360]]]
   [:elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:linger {:optional true} [:and :double [:>= 0]]]
   [:speed {:optional true} [:and :double [:> 0] [:<= 1]]]])

(def scan-add-node-spec
  "Malli spec for scan-add-node message"
  [:map [:index {:optional true} :int]
   [:day-zoom-table-value {:optional true} :int]
   [:heat-zoom-table-value {:optional true} :int]
   [:azimuth {:optional true} [:and :double [:>= 0] [:< 360]]]
   [:elevation {:optional true} [:and :double [:>= -90] [:<= 90]]]
   [:linger {:optional true} [:and :double [:>= 0]]]
   [:speed {:optional true} [:and :double [:> 0] [:<= 1]]]])

(def elevation-spec
  "Malli spec for elevation message"
  [:map
   [:cmd
    [:oneof
     {:set-value [:map [:set-value :cmd.rotary-platform/set-elevation-value]],
      :rotate-to [:map [:rotate-to :cmd.rotary-platform/rotate-elevation-to]],
      :rotate [:map [:rotate :cmd.rotary-platform/rotate-elevation]],
      :relative [:map
                 [:relative :cmd.rotary-platform/rotate-elevation-relative]],
      :relative-set [:map
                     [:relative-set
                      :cmd.rotary-platform/rotate-elevation-relative-set]],
      :halt [:map [:halt :cmd.rotary-platform/halt-elevation]],
      :error/message "This oneof field is required"}]]])

(def set-use-rotary-as-compass-spec
  "Malli spec for set-use-rotary-as-compass message"
  [:map [:flag {:optional true} :boolean]])

(def rotate-to-gps-spec
  "Malli spec for rotate-to-gps message"
  [:map [:latitude {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:longitude {:optional true} [:and :float [:>= -180] [:< 180]]]
   [:altitude {:optional true} :float]])

(def set-origin-gps-spec
  "Malli spec for set-origin-gps message"
  [:map [:latitude {:optional true} [:and :float [:>= -90] [:<= 90]]]
   [:longitude {:optional true} [:and :float [:>= -180] [:< 180]]]
   [:altitude {:optional true} :float]])

(def rotate-to-ndc-spec
  "Malli spec for rotate-to-ndc message"
  [:map
   [:channel {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-video-channel-spec]
   [:x {:optional true} [:and :float [:>= -1] [:<= 1]]]
   [:y {:optional true} [:and :float [:>= -1] [:<= 1]]]])

;; =============================================================================
;; Registry Setup
;; =============================================================================

;; Registry for enum and message specs in this namespace
(def registry
  {:cmd.RotaryPlatform/root root-spec,
   :cmd.RotaryPlatform/axis axis-spec,
   :cmd.RotaryPlatform/set-mode set-mode-spec,
   :cmd.RotaryPlatform/set-azimuth-value set-azimuth-value-spec,
   :cmd.RotaryPlatform/rotate-azimuth-to rotate-azimuth-to-spec,
   :cmd.RotaryPlatform/rotate-azimuth rotate-azimuth-spec,
   :cmd.RotaryPlatform/rotate-elevation rotate-elevation-spec,
   :cmd.RotaryPlatform/set-elevation-value set-elevation-value-spec,
   :cmd.RotaryPlatform/rotate-elevation-to rotate-elevation-to-spec,
   :cmd.RotaryPlatform/rotate-elevation-relative rotate-elevation-relative-spec,
   :cmd.RotaryPlatform/rotate-elevation-relative-set
     rotate-elevation-relative-set-spec,
   :cmd.RotaryPlatform/rotate-azimuth-relative rotate-azimuth-relative-spec,
   :cmd.RotaryPlatform/rotate-azimuth-relative-set
     rotate-azimuth-relative-set-spec,
   :cmd.RotaryPlatform/set-platform-azimuth set-platform-azimuth-spec,
   :cmd.RotaryPlatform/set-platform-elevation set-platform-elevation-spec,
   :cmd.RotaryPlatform/set-platform-bank set-platform-bank-spec,
   :cmd.RotaryPlatform/get-meteo get-meteo-spec,
   :cmd.RotaryPlatform/azimuth azimuth-spec,
   :cmd.RotaryPlatform/start start-spec,
   :cmd.RotaryPlatform/stop stop-spec,
   :cmd.RotaryPlatform/halt halt-spec,
   :cmd.RotaryPlatform/scan-start scan-start-spec,
   :cmd.RotaryPlatform/scan-stop scan-stop-spec,
   :cmd.RotaryPlatform/scan-pause scan-pause-spec,
   :cmd.RotaryPlatform/scan-unpause scan-unpause-spec,
   :cmd.RotaryPlatform/halt-azimuth halt-azimuth-spec,
   :cmd.RotaryPlatform/halt-elevation halt-elevation-spec,
   :cmd.RotaryPlatform/scan-prev scan-prev-spec,
   :cmd.RotaryPlatform/scan-next scan-next-spec,
   :cmd.RotaryPlatform/scan-refresh-node-list scan-refresh-node-list-spec,
   :cmd.RotaryPlatform/scan-select-node scan-select-node-spec,
   :cmd.RotaryPlatform/scan-delete-node scan-delete-node-spec,
   :cmd.RotaryPlatform/scan-update-node scan-update-node-spec,
   :cmd.RotaryPlatform/scan-add-node scan-add-node-spec,
   :cmd.RotaryPlatform/elevation elevation-spec,
   :cmd.RotaryPlatform/set-use-rotary-as-compass set-use-rotary-as-compass-spec,
   :cmd.RotaryPlatform/rotate-to-gps rotate-to-gps-spec,
   :cmd.RotaryPlatform/set-origin-gps set-origin-gps-spec,
   :cmd.RotaryPlatform/rotate-to-ndc rotate-to-ndc-spec})

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-axis)
(declare build-set-mode)
(declare build-set-azimuth-value)
(declare build-rotate-azimuth-to)
(declare build-rotate-azimuth)
(declare build-rotate-elevation)
(declare build-set-elevation-value)
(declare build-rotate-elevation-to)
(declare build-rotate-elevation-relative)
(declare build-rotate-elevation-relative-set)
(declare build-rotate-azimuth-relative)
(declare build-rotate-azimuth-relative-set)
(declare build-set-platform-azimuth)
(declare build-set-platform-elevation)
(declare build-set-platform-bank)
(declare build-get-meteo)
(declare build-azimuth)
(declare build-start)
(declare build-stop)
(declare build-halt)
(declare build-scan-start)
(declare build-scan-stop)
(declare build-scan-pause)
(declare build-scan-unpause)
(declare build-halt-azimuth)
(declare build-halt-elevation)
(declare build-scan-prev)
(declare build-scan-next)
(declare build-scan-refresh-node-list)
(declare build-scan-select-node)
(declare build-scan-delete-node)
(declare build-scan-update-node)
(declare build-scan-add-node)
(declare build-elevation)
(declare build-set-use-rotary-as-compass)
(declare build-rotate-to-gps)
(declare build-set-origin-gps)
(declare build-rotate-to-ndc)
(declare parse-root)
(declare parse-axis)
(declare parse-set-mode)
(declare parse-set-azimuth-value)
(declare parse-rotate-azimuth-to)
(declare parse-rotate-azimuth)
(declare parse-rotate-elevation)
(declare parse-set-elevation-value)
(declare parse-rotate-elevation-to)
(declare parse-rotate-elevation-relative)
(declare parse-rotate-elevation-relative-set)
(declare parse-rotate-azimuth-relative)
(declare parse-rotate-azimuth-relative-set)
(declare parse-set-platform-azimuth)
(declare parse-set-platform-elevation)
(declare parse-set-platform-bank)
(declare parse-get-meteo)
(declare parse-azimuth)
(declare parse-start)
(declare parse-stop)
(declare parse-halt)
(declare parse-scan-start)
(declare parse-scan-stop)
(declare parse-scan-pause)
(declare parse-scan-unpause)
(declare parse-halt-azimuth)
(declare parse-halt-elevation)
(declare parse-scan-prev)
(declare parse-scan-next)
(declare parse-scan-refresh-node-list)
(declare parse-scan-select-node)
(declare parse-scan-delete-node)
(declare parse-scan-update-node)
(declare parse-scan-add-node)
(declare parse-elevation)
(declare parse-set-use-rotary-as-compass)
(declare parse-rotate-to-gps)
(declare parse-set-origin-gps)
(declare parse-rotate-to-ndc)
(declare build-root-payload)
(declare build-azimuth-payload)
(declare build-elevation-payload)
(declare parse-root-payload)
(declare parse-azimuth-payload)
(declare parse-elevation-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field
                      (first
                        (filter (fn [[k v]]
                                  (#{:start :stop :axis :set-platform-azimuth
                                     :set-platform-elevation :set-platform-bank
                                     :halt :set-use-rotary-as-compass
                                     :rotate-to-gps :set-origin-gps :set-mode
                                     :rotate-to-ndc :scan-start :scan-stop
                                     :scan-pause :scan-unpause :get-meteo
                                     :scan-prev :scan-next
                                     :scan-refresh-node-list :scan-select-node
                                     :scan-delete-node :scan-update-node
                                     :scan-add-node}
                                   k))
                          (:cmd m)))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-axis
       "Build a Axis protobuf message from a map."
       [m]
       [axis-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Axis/newBuilder)]
         ;; Set regular fields
         (when (contains? m :azimuth)
           (.setAzimuth builder (build-azimuth (get m :azimuth))))
         (when (contains? m :elevation)
           (.setElevation builder (build-elevation (get m :elevation))))
         (.build builder)))

(>defn build-set-mode
       "Build a SetMode protobuf message from a map."
       [m]
       [set-mode-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetMode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :mode)
           (.setMode builder
                     (when-let [v (get m :mode)]
                       (get types/jon-gui-data-rotary-mode-values v))))
         (.build builder)))

(>defn
  build-set-azimuth-value
  "Build a SetAzimuthValue protobuf message from a map."
  [m]
  [set-azimuth-value-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn
  build-rotate-azimuth-to
  "Build a RotateAzimuthTo protobuf message from a map."
  [m]
  [rotate-azimuth-to-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn build-rotate-azimuth
       "Build a RotateAzimuth protobuf message from a map."
       [m]
       [rotate-azimuth-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth/newBuilder)]
         ;; Set regular fields
         (when (contains? m :speed) (.setSpeed builder (get m :speed)))
         (when (contains? m :direction)
           (.setDirection builder
                          (when-let [v (get m :direction)]
                            (get types/jon-gui-data-rotary-direction-values
                                 v))))
         (.build builder)))

(>defn
  build-rotate-elevation
  "Build a RotateElevation protobuf message from a map."
  [m]
  [rotate-elevation-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn
  build-set-elevation-value
  "Build a SetElevationValue protobuf message from a map."
  [m]
  [set-elevation-value-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(>defn
  build-rotate-elevation-to
  "Build a RotateElevationTo protobuf message from a map."
  [m]
  [rotate-elevation-to-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target-value)
      (.setTargetValue builder (get m :target-value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(>defn
  build-rotate-elevation-relative
  "Build a RotateElevationRelative protobuf message from a map."
  [m]
  [rotate-elevation-relative-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn
  build-rotate-elevation-relative-set
  "Build a RotateElevationRelativeSet protobuf message from a map."
  [m]
  [rotate-elevation-relative-set-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn
  build-rotate-azimuth-relative
  "Build a RotateAzimuthRelative protobuf message from a map."
  [m]
  [rotate-azimuth-relative-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn
  build-rotate-azimuth-relative-set
  "Build a RotateAzimuthRelativeSet protobuf message from a map."
  [m]
  [rotate-azimuth-relative-set-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (when (contains? m :direction)
      (.setDirection builder
                     (when-let [v (get m :direction)]
                       (get types/jon-gui-data-rotary-direction-values v))))
    (.build builder)))

(>defn
  build-set-platform-azimuth
  "Build a SetPlatformAzimuth protobuf message from a map."
  [m]
  [set-platform-azimuth-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(>defn
  build-set-platform-elevation
  "Build a SetPlatformElevation protobuf message from a map."
  [m]
  [set-platform-elevation-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(>defn build-set-platform-bank
       "Build a SetPlatformBank protobuf message from a map."
       [m]
       [set-platform-bank-spec => any?]
       (let
         [builder
            (cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank/newBuilder)]
         ;; Set regular fields
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-get-meteo
       "Build a GetMeteo protobuf message from a map."
       [m]
       [get-meteo-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo/newBuilder)]
         (.build builder)))

(>defn build-azimuth
       "Build a Azimuth protobuf message from a map."
       [m]
       [azimuth-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set-value :rotate-to :rotate
                                                  :relative :relative-set :halt}
                                                k))
                                       (:cmd m)))]
           (build-azimuth-payload builder cmd-field))
         (.build builder)))

(>defn build-start
       "Build a Start protobuf message from a map."
       [m]
       [start-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Start/newBuilder)]
         (.build builder)))

(>defn build-stop
       "Build a Stop protobuf message from a map."
       [m]
       [stop-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Stop/newBuilder)]
         (.build builder)))

(>defn build-halt
       "Build a Halt protobuf message from a map."
       [m]
       [halt-spec => any?]
       (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$Halt/newBuilder)]
         (.build builder)))

(>defn build-scan-start
       "Build a ScanStart protobuf message from a map."
       [m]
       [scan-start-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart/newBuilder)]
         (.build builder)))

(>defn build-scan-stop
       "Build a ScanStop protobuf message from a map."
       [m]
       [scan-stop-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop/newBuilder)]
         (.build builder)))

(>defn build-scan-pause
       "Build a ScanPause protobuf message from a map."
       [m]
       [scan-pause-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause/newBuilder)]
         (.build builder)))

(>defn build-scan-unpause
       "Build a ScanUnpause protobuf message from a map."
       [m]
       [scan-unpause-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause/newBuilder)]
         (.build builder)))

(>defn build-halt-azimuth
       "Build a HaltAzimuth protobuf message from a map."
       [m]
       [halt-azimuth-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth/newBuilder)]
         (.build builder)))

(>defn build-halt-elevation
       "Build a HaltElevation protobuf message from a map."
       [m]
       [halt-elevation-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation/newBuilder)]
         (.build builder)))

(>defn build-scan-prev
       "Build a ScanPrev protobuf message from a map."
       [m]
       [scan-prev-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev/newBuilder)]
         (.build builder)))

(>defn build-scan-next
       "Build a ScanNext protobuf message from a map."
       [m]
       [scan-next-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext/newBuilder)]
         (.build builder)))

(>defn
  build-scan-refresh-node-list
  "Build a ScanRefreshNodeList protobuf message from a map."
  [m]
  [scan-refresh-node-list-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList/newBuilder)]
    (.build builder)))

(>defn build-scan-select-node
       "Build a ScanSelectNode protobuf message from a map."
       [m]
       [scan-select-node-spec => any?]
       (let
         [builder
            (cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :index) (.setIndex builder (get m :index)))
         (.build builder)))

(>defn build-scan-delete-node
       "Build a ScanDeleteNode protobuf message from a map."
       [m]
       [scan-delete-node-spec => any?]
       (let
         [builder
            (cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :index) (.setIndex builder (get m :index)))
         (.build builder)))

(>defn
  build-scan-update-node
  "Build a ScanUpdateNode protobuf message from a map."
  [m]
  [scan-update-node-spec => any?]
  (let [builder
          (cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index) (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :linger) (.setLinger builder (get m :linger)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(>defn
  build-scan-add-node
  "Build a ScanAddNode protobuf message from a map."
  [m]
  [scan-add-node-spec => any?]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :index) (.setIndex builder (get m :index)))
    (when (contains? m :day-zoom-table-value)
      (.setDayZoomTableValue builder (get m :day-zoom-table-value)))
    (when (contains? m :heat-zoom-table-value)
      (.setHeatZoomTableValue builder (get m :heat-zoom-table-value)))
    (when (contains? m :azimuth) (.setAzimuth builder (get m :azimuth)))
    (when (contains? m :elevation) (.setElevation builder (get m :elevation)))
    (when (contains? m :linger) (.setLinger builder (get m :linger)))
    (when (contains? m :speed) (.setSpeed builder (get m :speed)))
    (.build builder)))

(>defn build-elevation
       "Build a Elevation protobuf message from a map."
       [m]
       [elevation-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$Elevation/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set-value :rotate-to :rotate
                                                  :relative :relative-set :halt}
                                                k))
                                       (:cmd m)))]
           (build-elevation-payload builder cmd-field))
         (.build builder)))

(>defn
  build-set-use-rotary-as-compass
  "Build a setUseRotaryAsCompass protobuf message from a map."
  [m]
  [set-use-rotary-as-compass-spec => any?]
  (let
    [builder
       (cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag) (.setFlag builder (get m :flag)))
    (.build builder)))

(>defn
  build-rotate-to-gps
  "Build a RotateToGPS protobuf message from a map."
  [m]
  [rotate-to-gps-spec => any?]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (.build builder)))

(>defn
  build-set-origin-gps
  "Build a SetOriginGPS protobuf message from a map."
  [m]
  [set-origin-gps-spec => any?]
  (let [builder (cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS/newBuilder)]
    ;; Set regular fields
    (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
    (when (contains? m :longitude) (.setLongitude builder (get m :longitude)))
    (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
    (.build builder)))

(>defn build-rotate-to-ndc
       "Build a RotateToNDC protobuf message from a map."
       [m]
       [rotate-to-ndc-spec => any?]
       (let [builder
               (cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC/newBuilder)]
         ;; Set regular fields
         (when (contains? m :channel)
           (.setChannel builder
                        (when-let [v (get m :channel)]
                          (get types/jon-gui-data-video-channel-values v))))
         (when (contains? m :x) (.setX builder (get m :x)))
         (when (contains? m :y) (.setY builder (get m :y)))
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-root-payload proto) (assoc :cmd (parse-root-payload proto))))

(>defn parse-axis
       "Parse a Axis protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Axis proto]
       [any? => axis-spec]
       (cond-> {}
         ;; Regular fields
         (.hasAzimuth proto) (assoc :azimuth
                               (parse-azimuth (.getAzimuth proto)))
         (.hasElevation proto) (assoc :elevation
                                 (parse-elevation (.getElevation proto)))))

(>defn parse-set-mode
       "Parse a SetMode protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetMode proto]
       [any? => set-mode-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :mode
                (get types/jon-gui-data-rotary-mode-keywords
                     (.getMode proto)))))

(>defn parse-set-azimuth-value
       "Parse a SetAzimuthValue protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue proto]
       [any? => set-azimuth-value-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-rotate-azimuth-to
       "Parse a RotateAzimuthTo protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo proto]
       [any? => rotate-azimuth-to-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :target-value (.getTargetValue proto))
         true (assoc :speed (.getSpeed proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-rotate-azimuth
       "Parse a RotateAzimuth protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth proto]
       [any? => rotate-azimuth-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :speed (.getSpeed proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-rotate-elevation
       "Parse a RotateElevation protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation proto]
       [any? => rotate-elevation-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :speed (.getSpeed proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-set-elevation-value
       "Parse a SetElevationValue protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue proto]
       [any? => set-elevation-value-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-rotate-elevation-to
       "Parse a RotateElevationTo protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo proto]
       [any? => rotate-elevation-to-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :target-value (.getTargetValue proto))
         true (assoc :speed (.getSpeed proto))))

(>defn parse-rotate-elevation-relative
       "Parse a RotateElevationRelative protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative proto]
       [any? => rotate-elevation-relative-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))
         true (assoc :speed (.getSpeed proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-rotate-elevation-relative-set
       "Parse a RotateElevationRelativeSet protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet proto]
       [any? => rotate-elevation-relative-set-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-rotate-azimuth-relative
       "Parse a RotateAzimuthRelative protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative proto]
       [any? => rotate-azimuth-relative-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))
         true (assoc :speed (.getSpeed proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-rotate-azimuth-relative-set
       "Parse a RotateAzimuthRelativeSet protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet proto]
       [any? => rotate-azimuth-relative-set-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))
         true (assoc :direction
                (get types/jon-gui-data-rotary-direction-keywords
                     (.getDirection proto)))))

(>defn parse-set-platform-azimuth
       "Parse a SetPlatformAzimuth protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth proto]
       [any? => set-platform-azimuth-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-platform-elevation
       "Parse a SetPlatformElevation protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation proto]
       [any? => set-platform-elevation-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-set-platform-bank
       "Parse a SetPlatformBank protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank proto]
       [any? => set-platform-bank-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :value (.getValue proto))))

(>defn parse-get-meteo
       "Parse a GetMeteo protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo proto]
       [any? => get-meteo-spec]
       {})

(>defn parse-azimuth
       "Parse a Azimuth protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
       [any? => azimuth-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-azimuth-payload proto) (assoc :cmd
                                         (parse-azimuth-payload proto))))

(>defn parse-start
       "Parse a Start protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Start proto]
       [any? => start-spec]
       {})

(>defn parse-stop
       "Parse a Stop protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Stop proto]
       [any? => stop-spec]
       {})

(>defn parse-halt
       "Parse a Halt protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Halt proto]
       [any? => halt-spec]
       {})

(>defn parse-scan-start
       "Parse a ScanStart protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart proto]
       [any? => scan-start-spec]
       {})

(>defn parse-scan-stop
       "Parse a ScanStop protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop proto]
       [any? => scan-stop-spec]
       {})

(>defn parse-scan-pause
       "Parse a ScanPause protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause proto]
       [any? => scan-pause-spec]
       {})

(>defn parse-scan-unpause
       "Parse a ScanUnpause protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause proto]
       [any? => scan-unpause-spec]
       {})

(>defn parse-halt-azimuth
       "Parse a HaltAzimuth protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth proto]
       [any? => halt-azimuth-spec]
       {})

(>defn parse-halt-elevation
       "Parse a HaltElevation protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation proto]
       [any? => halt-elevation-spec]
       {})

(>defn parse-scan-prev
       "Parse a ScanPrev protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev proto]
       [any? => scan-prev-spec]
       {})

(>defn parse-scan-next
       "Parse a ScanNext protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext proto]
       [any? => scan-next-spec]
       {})

(>defn parse-scan-refresh-node-list
       "Parse a ScanRefreshNodeList protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList proto]
       [any? => scan-refresh-node-list-spec]
       {})

(>defn parse-scan-select-node
       "Parse a ScanSelectNode protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode proto]
       [any? => scan-select-node-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :index (.getIndex proto))))

(>defn parse-scan-delete-node
       "Parse a ScanDeleteNode protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode proto]
       [any? => scan-delete-node-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :index (.getIndex proto))))

(>defn parse-scan-update-node
       "Parse a ScanUpdateNode protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode proto]
       [any? => scan-update-node-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :index (.getIndex proto))
         true (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
         true (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :linger (.getLinger proto))
         true (assoc :speed (.getSpeed proto))))

(>defn parse-scan-add-node
       "Parse a ScanAddNode protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode proto]
       [any? => scan-add-node-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :index (.getIndex proto))
         true (assoc :day-zoom-table-value (.getDayZoomTableValue proto))
         true (assoc :heat-zoom-table-value (.getHeatZoomTableValue proto))
         true (assoc :azimuth (.getAzimuth proto))
         true (assoc :elevation (.getElevation proto))
         true (assoc :linger (.getLinger proto))
         true (assoc :speed (.getSpeed proto))))

(>defn parse-elevation
       "Parse a Elevation protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
       [any? => elevation-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-elevation-payload proto) (assoc :cmd
                                           (parse-elevation-payload proto))))

(>defn parse-set-use-rotary-as-compass
       "Parse a setUseRotaryAsCompass protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass proto]
       [any? => set-use-rotary-as-compass-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :flag (.getFlag proto))))

(>defn parse-rotate-to-gps
       "Parse a RotateToGPS protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS proto]
       [any? => rotate-to-gps-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :latitude (.getLatitude proto))
         true (assoc :longitude (.getLongitude proto))
         true (assoc :altitude (.getAltitude proto))))

(>defn parse-set-origin-gps
       "Parse a SetOriginGPS protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS proto]
       [any? => set-origin-gps-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :latitude (.getLatitude proto))
         true (assoc :longitude (.getLongitude proto))
         true (assoc :altitude (.getAltitude proto))))

(>defn parse-rotate-to-ndc
       "Parse a RotateToNDC protobuf message to a map."
       [^cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC proto]
       [any? => rotate-to-ndc-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :channel
                (get types/jon-gui-data-video-channel-keywords
                     (.getChannel proto)))
         true (assoc :x (.getX proto))
         true (assoc :y (.getY proto))))

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :axis (.setAxis builder (build-axis value))
    :set-platform-azimuth
      (.setSetPlatformAzimuth builder (build-set-platform-azimuth value))
    :set-platform-elevation
      (.setSetPlatformElevation builder (build-set-platform-elevation value))
    :set-platform-bank (.setSetPlatformBank builder
                                            (build-set-platform-bank value))
    :halt (.setHalt builder (build-halt value))
    :set-use-rotary-as-compass (.setSetUseRotaryAsCompass
                                 builder
                                 (build-set-use-rotary-as-compass value))
    :rotate-to-gps (.setRotateToGps builder (build-rotate-to-gps value))
    :set-origin-gps (.setSetOriginGps builder (build-set-origin-gps value))
    :set-mode (.setSetMode builder (build-set-mode value))
    :rotate-to-ndc (.setRotateToNdc builder (build-rotate-to-ndc value))
    :scan-start (.setScanStart builder (build-scan-start value))
    :scan-stop (.setScanStop builder (build-scan-stop value))
    :scan-pause (.setScanPause builder (build-scan-pause value))
    :scan-unpause (.setScanUnpause builder (build-scan-unpause value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :scan-prev (.setScanPrev builder (build-scan-prev value))
    :scan-next (.setScanNext builder (build-scan-next value))
    :scan-refresh-node-list
      (.setScanRefreshNodeList builder (build-scan-refresh-node-list value))
    :scan-select-node (.setScanSelectNode builder
                                          (build-scan-select-node value))
    :scan-delete-node (.setScanDeleteNode builder
                                          (build-scan-delete-node value))
    :scan-update-node (.setScanUpdateNode builder
                                          (build-scan-update-node value))
    :scan-add-node (.setScanAddNode builder (build-scan-add-node value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn- build-azimuth-payload
        "Build the oneof payload for Azimuth."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
        (case field-key
          :set-value (.setSetValue builder (build-set-azimuth-value value))
          :rotate-to (.setRotateTo builder (build-rotate-azimuth-to value))
          :rotate (.setRotate builder (build-rotate-azimuth value))
          :relative (.setRelative builder (build-rotate-azimuth-relative value))
          :relative-set
            (.setRelativeSet builder (build-rotate-azimuth-relative-set value))
          :halt (.setHalt builder (build-halt-azimuth value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":cmd"}))))

(>defn-
  build-elevation-payload
  "Build the oneof payload for Elevation."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :set-value (.setSetValue builder (build-set-elevation-value value))
    :rotate-to (.setRotateTo builder (build-rotate-elevation-to value))
    :rotate (.setRotate builder (build-rotate-elevation value))
    :relative (.setRelative builder (build-rotate-elevation-relative value))
    :relative-set (.setRelativeSet builder
                                   (build-rotate-elevation-relative-set value))
    :halt (.setHalt builder (build-halt-elevation value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.RotaryPlatform.JonSharedCmdRotary$Root proto]
  [any? => (? map?)]
  (cond
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasAxis proto) {:axis (parse-axis (.getAxis proto))}
    (.hasSetPlatformAzimuth proto) {:set-platform-azimuth
                                      (parse-set-platform-azimuth
                                        (.getSetPlatformAzimuth proto))}
    (.hasSetPlatformElevation proto) {:set-platform-elevation
                                        (parse-set-platform-elevation
                                          (.getSetPlatformElevation proto))}
    (.hasSetPlatformBank proto)
      {:set-platform-bank (parse-set-platform-bank (.getSetPlatformBank proto))}
    (.hasHalt proto) {:halt (parse-halt (.getHalt proto))}
    (.hasSetUseRotaryAsCompass proto) {:set-use-rotary-as-compass
                                         (parse-set-use-rotary-as-compass
                                           (.getSetUseRotaryAsCompass proto))}
    (.hasRotateToGps proto) {:rotate-to-gps (parse-rotate-to-gps
                                              (.getRotateToGps proto))}
    (.hasSetOriginGps proto) {:set-origin-gps (parse-set-origin-gps
                                                (.getSetOriginGps proto))}
    (.hasSetMode proto) {:set-mode (parse-set-mode (.getSetMode proto))}
    (.hasRotateToNdc proto) {:rotate-to-ndc (parse-rotate-to-ndc
                                              (.getRotateToNdc proto))}
    (.hasScanStart proto) {:scan-start (parse-scan-start (.getScanStart proto))}
    (.hasScanStop proto) {:scan-stop (parse-scan-stop (.getScanStop proto))}
    (.hasScanPause proto) {:scan-pause (parse-scan-pause (.getScanPause proto))}
    (.hasScanUnpause proto) {:scan-unpause (parse-scan-unpause (.getScanUnpause
                                                                 proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasScanPrev proto) {:scan-prev (parse-scan-prev (.getScanPrev proto))}
    (.hasScanNext proto) {:scan-next (parse-scan-next (.getScanNext proto))}
    (.hasScanRefreshNodeList proto) {:scan-refresh-node-list
                                       (parse-scan-refresh-node-list
                                         (.getScanRefreshNodeList proto))}
    (.hasScanSelectNode proto) {:scan-select-node (parse-scan-select-node
                                                    (.getScanSelectNode proto))}
    (.hasScanDeleteNode proto) {:scan-delete-node (parse-scan-delete-node
                                                    (.getScanDeleteNode proto))}
    (.hasScanUpdateNode proto) {:scan-update-node (parse-scan-update-node
                                                    (.getScanUpdateNode proto))}
    (.hasScanAddNode proto) {:scan-add-node (parse-scan-add-node
                                              (.getScanAddNode proto))}))

(>defn- parse-azimuth-payload
        "Parse the oneof payload from Azimuth."
        [^cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth proto]
        [any? => (? map?)]
        (cond (.hasSetValue proto) {:set-value (parse-set-azimuth-value
                                                 (.getSetValue proto))}
              (.hasRotateTo proto) {:rotate-to (parse-rotate-azimuth-to
                                                 (.getRotateTo proto))}
              (.hasRotate proto) {:rotate (parse-rotate-azimuth (.getRotate
                                                                  proto))}
              (.hasRelative proto) {:relative (parse-rotate-azimuth-relative
                                                (.getRelative proto))}
              (.hasRelativeSet proto) {:relative-set
                                         (parse-rotate-azimuth-relative-set
                                           (.getRelativeSet proto))}
              (.hasHalt proto) {:halt (parse-halt-azimuth (.getHalt proto))}))

(>defn- parse-elevation-payload
        "Parse the oneof payload from Elevation."
        [^cmd.RotaryPlatform.JonSharedCmdRotary$Elevation proto]
        [any? => (? map?)]
        (cond (.hasSetValue proto) {:set-value (parse-set-elevation-value
                                                 (.getSetValue proto))}
              (.hasRotateTo proto) {:rotate-to (parse-rotate-elevation-to
                                                 (.getRotateTo proto))}
              (.hasRotate proto) {:rotate (parse-rotate-elevation (.getRotate
                                                                    proto))}
              (.hasRelative proto) {:relative (parse-rotate-elevation-relative
                                                (.getRelative proto))}
              (.hasRelativeSet proto) {:relative-set
                                         (parse-rotate-elevation-relative-set
                                           (.getRelativeSet proto))}
              (.hasHalt proto) {:halt (parse-halt-elevation (.getHalt proto))}))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for SetMode
;; Warning: Could not extract spec for field mode

;; Validation helpers for SetAzimuthValue
;; Warning: Could not extract spec for field value

;; Warning: Could not extract spec for field direction

;; Validation helpers for RotateAzimuthTo
;; Warning: Could not extract spec for field target-value

;; Warning: Could not extract spec for field speed

;; Warning: Could not extract spec for field direction

;; Validation helpers for RotateAzimuth
;; Warning: Could not extract spec for field speed

;; Warning: Could not extract spec for field direction

;; Validation helpers for RotateElevation
;; Warning: Could not extract spec for field speed

;; Warning: Could not extract spec for field direction

;; Validation helpers for SetElevationValue
;; Warning: Could not extract spec for field value

;; Validation helpers for RotateElevationTo
;; Warning: Could not extract spec for field target-value

;; Warning: Could not extract spec for field speed

;; Validation helpers for RotateElevationRelative
;; Warning: Could not extract spec for field speed

;; Warning: Could not extract spec for field direction

;; Validation helpers for RotateElevationRelativeSet
;; Warning: Could not extract spec for field direction

;; Validation helpers for RotateAzimuthRelative
;; Warning: Could not extract spec for field speed

;; Warning: Could not extract spec for field direction

;; Validation helpers for RotateAzimuthRelativeSet
;; Warning: Could not extract spec for field direction

;; Validation helpers for SetPlatformAzimuth
;; Warning: Could not extract spec for field value

;; Validation helpers for SetPlatformElevation
;; Warning: Could not extract spec for field value

;; Validation helpers for SetPlatformBank
;; Warning: Could not extract spec for field value

;; Validation helpers for ScanSelectNode
;; Warning: Could not extract spec for field index

;; Validation helpers for ScanDeleteNode
;; Warning: Could not extract spec for field index

;; Validation helpers for ScanUpdateNode
;; Warning: Could not extract spec for field index

;; Warning: Could not extract spec for field day-zoom-table-value

;; Warning: Could not extract spec for field heat-zoom-table-value

;; Warning: Could not extract spec for field azimuth

;; Warning: Could not extract spec for field elevation

;; Warning: Could not extract spec for field linger

;; Warning: Could not extract spec for field speed

;; Validation helpers for ScanAddNode
;; Warning: Could not extract spec for field index

;; Warning: Could not extract spec for field day-zoom-table-value

;; Warning: Could not extract spec for field heat-zoom-table-value

;; Warning: Could not extract spec for field azimuth

;; Warning: Could not extract spec for field elevation

;; Warning: Could not extract spec for field linger

;; Warning: Could not extract spec for field speed

;; Validation helpers for RotateToGPS
;; Warning: Could not extract spec for field latitude

;; Warning: Could not extract spec for field longitude

;; Validation helpers for SetOriginGPS
;; Warning: Could not extract spec for field latitude

;; Warning: Could not extract spec for field longitude

;; Validation helpers for RotateToNDC
;; Warning: Could not extract spec for field channel

;; Warning: Could not extract spec for field x

;; Warning: Could not extract spec for field y