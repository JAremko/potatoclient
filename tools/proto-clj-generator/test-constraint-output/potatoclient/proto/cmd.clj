(ns potatoclient.proto.cmd
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [potatoclient.proto.cmd.lira :as lira]
            [potatoclient.proto.cmd.rotaryplatform :as rotaryplatform]
            [potatoclient.proto.cmd.system :as system]
            [potatoclient.proto.cmd.lrf-calib :as lrf-calib]
            [potatoclient.proto.cmd.gps :as gps]
            [potatoclient.proto.cmd.heatcamera :as heatcamera]
            [potatoclient.proto.cmd.cv :as cv]
            [potatoclient.proto.cmd.daycamera :as daycamera]
            [potatoclient.proto.cmd.daycamglassheater :as daycamglassheater]
            [potatoclient.proto.cmd.lrf :as lrf]
            [potatoclient.proto.cmd.compass :as compass]
            [potatoclient.proto.ser :as types]
            [potatoclient.proto.cmd.osd :as osd])
  (:import cmd.JonSharedCmd$Root
           cmd.JonSharedCmd$Ping
           cmd.JonSharedCmd$Noop
           cmd.JonSharedCmd$Frozen))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def root-spec
  "Malli spec for root message"
  [:map [:protocol-version [:> 0]] [:session-id [:maybe :int]]
   [:important [:maybe :boolean]] [:from-cv-subsystem [:maybe :boolean]]
   [:client-type [:maybe :ser/jon-gui-data-client-type]]
   [:payload
    [:altn #:error{:message "This oneof field is required"}
     {:osd [:map [:osd :cmd.osd/root]],
      :ping [:map [:ping :cmd/ping]],
      :system [:map [:system :cmd.system/root]],
      :noop [:map [:noop :cmd/noop]],
      :cv [:map [:cv :cmd.cv/root]],
      :gps [:map [:gps :cmd.gps/root]],
      :lrf [:map [:lrf :cmd.lrf/root]],
      :day-cam-glass-heater
        [:map [:day-cam-glass-heater :cmd.day-cam-glass-heater/root]],
      :day-camera [:map [:day-camera :cmd.day-camera/root]],
      :heat-camera [:map [:heat-camera :cmd.heat-camera/root]],
      :lira [:map [:lira :cmd.lira/root]],
      :lrf-calib [:map [:lrf-calib :cmd.lrf-calib/root]],
      :rotary [:map [:rotary :cmd.rotary-platform/root]],
      :compass [:map [:compass :cmd.compass/root]],
      :frozen [:map [:frozen :cmd/frozen]]}]]])

(def ping-spec "Malli spec for ping message" [:map])

(def noop-spec "Malli spec for noop message" [:map])

(def frozen-spec "Malli spec for frozen message" [:map])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-ping)
(declare build-noop)
(declare build-frozen)
(declare parse-root)
(declare parse-ping)
(declare parse-noop)
(declare parse-frozen)
(declare build-root-payload)
(declare parse-root-payload)

(>defn
  build-root
  "Build a Root protobuf message from a map."
  [m]
  [root-spec => any?]
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :session-id) (.setSessionId builder (get m :session-id)))
    (when (contains? m :important) (.setImportant builder (get m :important)))
    (when (contains? m :from-cv-subsystem)
      (.setFromCvSubsystem builder (get m :from-cv-subsystem)))
    (when (contains? m :client-type)
      (.setClientType builder
                      (get types/jon-gui-data-client-type-values
                           (get m :client-type))))
    ;; Handle oneof: payload
    (when-let [payload-field
                 (first (filter (fn [[k v]]
                                  (#{:day-camera :heat-camera :gps :compass :lrf
                                     :lrf-calib :rotary :osd :ping :noop :frozen
                                     :system :cv :day-cam-glass-heater :lira}
                                   k))
                          m))]
      (build-root-payload builder payload-field))
    (.build builder)))

(>defn build-ping
       "Build a Ping protobuf message from a map."
       [m]
       [ping-spec => any?]
       (let [builder (cmd.JonSharedCmd$Ping/newBuilder)] (.build builder)))

(>defn build-noop
       "Build a Noop protobuf message from a map."
       [m]
       [noop-spec => any?]
       (let [builder (cmd.JonSharedCmd$Noop/newBuilder)] (.build builder)))

(>defn build-frozen
       "Build a Frozen protobuf message from a map."
       [m]
       [frozen-spec => any?]
       (let [builder (cmd.JonSharedCmd$Frozen/newBuilder)] (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.JonSharedCmd$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :protocol-version (.getProtocolVersion proto))
         true (assoc :session-id (.getSessionId proto))
         true (assoc :important (.getImportant proto))
         true (assoc :from-cv-subsystem (.getFromCvSubsystem proto))
         true (assoc :client-type
                (get types/jon-gui-data-client-type-keywords
                     (.getClientType proto)))
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-ping
       "Parse a Ping protobuf message to a map."
       [^cmd.JonSharedCmd$Ping proto]
       [any? => ping-spec]
       {})

(>defn parse-noop
       "Parse a Noop protobuf message to a map."
       [^cmd.JonSharedCmd$Noop proto]
       [any? => noop-spec]
       {})

(>defn parse-frozen
       "Parse a Frozen protobuf message to a map."
       [^cmd.JonSharedCmd$Frozen proto]
       [any? => frozen-spec]
       {})

(>defn- build-root-payload
        "Build the oneof payload for Root."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
        (case field-key
          :day-camera (.setDayCamera builder (daycamera/build-root value))
          :heat-camera (.setHeatCamera builder (heatcamera/build-root value))
          :gps (.setGps builder (gps/build-root value))
          :compass (.setCompass builder (compass/build-root value))
          :lrf (.setLrf builder (lrf/build-root value))
          :lrf-calib (.setLrfCalib builder (lrf-calib/build-root value))
          :rotary (.setRotary builder (rotaryplatform/build-root value))
          :osd (.setOsd builder (osd/build-root value))
          :ping (.setPing builder (build-ping value))
          :noop (.setNoop builder (build-noop value))
          :frozen (.setFrozen builder (build-frozen value))
          :system (.setSystem builder (system/build-root value))
          :cv (.setCv builder (cv/build-root value))
          :day-cam-glass-heater
            (.setDayCamGlassHeater builder (daycamglassheater/build-root value))
          :lira (.setLira builder (lira/build-root value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":payload"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.JonSharedCmd$Root proto]
  [any? => (? map?)]
  (cond (.hasDayCamera proto) {:day-camera (daycamera/parse-root (.getDayCamera
                                                                   proto))}
        (.hasHeatCamera proto) {:heat-camera (heatcamera/parse-root
                                               (.getHeatCamera proto))}
        (.hasGps proto) {:gps (gps/parse-root (.getGps proto))}
        (.hasCompass proto) {:compass (compass/parse-root (.getCompass proto))}
        (.hasLrf proto) {:lrf (lrf/parse-root (.getLrf proto))}
        (.hasLrfCalib proto) {:lrf-calib (lrf-calib/parse-root (.getLrfCalib
                                                                 proto))}
        (.hasRotary proto) {:rotary (rotaryplatform/parse-root (.getRotary
                                                                 proto))}
        (.hasOsd proto) {:osd (osd/parse-root (.getOsd proto))}
        (.hasPing proto) {:ping (parse-ping (.getPing proto))}
        (.hasNoop proto) {:noop (parse-noop (.getNoop proto))}
        (.hasFrozen proto) {:frozen (parse-frozen (.getFrozen proto))}
        (.hasSystem proto) {:system (system/parse-root (.getSystem proto))}
        (.hasCv proto) {:cv (cv/parse-root (.getCv proto))}
        (.hasDayCamGlassHeater proto) {:day-cam-glass-heater
                                         (daycamglassheater/parse-root
                                           (.getDayCamGlassHeater proto))}
        (.hasLira proto) {:lira (lira/parse-root (.getLira proto))}))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for Root
(defn valid-root-protocol-version?
  "Validate protocol-version field of Root - has validation constraints"
  [value]
  (m/validate (second (first (filter #(= (first %) :protocol-version)
                               (drop 1 root-spec))))
              value))

(defn valid-root-client-type?
  "Validate client-type field of Root - has validation constraints"
  [value]
  (m/validate (second (first (filter #(= (first %) :client-type)
                               (drop 1 root-spec))))
              value))