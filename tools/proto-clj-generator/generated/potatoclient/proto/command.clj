(ns potatoclient.proto.command
  "Generated protobuf conversion functions."
  (:require [clojure.string :as str])
  (:import
    [cmd JonSharedCmd$Root JonSharedCmd$Root$PayloadCase]))

;; Forward declarations
(declare build-root parse-root build-root-payload parse-root-payload)

;; Message Converters
(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; Set regular fields
    (when (contains? m :protocol-version)
      (.setProtocolVersion builder (get m :protocol-version)))
    (when (contains? m :session-id)
      (.setSessionId builder (get m :session-id)))
    (when (contains? m :important)
      (.setImportant builder (get m :important)))
    (when (contains? m :from-cv-subsystem)
      (.setFromCvSubsystem builder (get m :from-cv-subsystem)))
    (when (contains? m :client-type)
      (.setClientType builder (get m :client-type)))
    ;; Set oneof payload
    (when-let [payload (first (filter (fn [[k v]] (#{:day-camera :heat-camera :gps :compass :lrf :lrf-calib :rotary :osd :ping :noop :frozen :system :cv :day-cam-glass-heater :lira} k)) m))]
      (build-root-payload builder payload))
    (.build builder)))


(defn build-root-payload
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    :day-camera (.setDayCamera builder field-value)
    :heat-camera (.setHeatCamera builder field-value)
    :gps (.setGps builder field-value)
    :compass (.setCompass builder field-value)
    :lrf (.setLrf builder field-value)
    :lrf-calib (.setLrfCalib builder field-value)
    :rotary (.setRotary builder field-value)
    :osd (.setOsd builder field-value)
    :ping (.setPing builder (.. cmd.JonSharedCmd$Ping newBuilder build))
    :noop (.setNoop builder field-value)
    :frozen (.setFrozen builder field-value)
    :system (.setSystem builder field-value)
    :cv (.setCv builder field-value)
    :day-cam-glass-heater (.setDayCamGlassHeater builder field-value)
    :lira (.setLira builder field-value)
    (throw (ex-info "Unknown payload field" {:field field-key}))))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.JonSharedCmd$Root proto]
  (merge
    {:protocol-version (.getProtocolVersion proto)
     :session-id (.getSessionId proto)
     :important (.getImportant proto)
     :from-cv-subsystem (.getFromCvSubsystem proto)
     :client-type (.getClientType proto)}
    (parse-root-payload proto)))


(defn parse-root-payload
  "Parse the oneof payload."
  [proto]
  (condp = (.getPayloadCase proto)
    JonSharedCmd$Root$PayloadCase/DAY_CAMERA {:day-camera (.getDayCamera proto)}
    JonSharedCmd$Root$PayloadCase/HEAT_CAMERA {:heat-camera (.getHeatCamera proto)}
    JonSharedCmd$Root$PayloadCase/GPS {:gps (.getGps proto)}
    JonSharedCmd$Root$PayloadCase/COMPASS {:compass (.getCompass proto)}
    JonSharedCmd$Root$PayloadCase/LRF {:lrf (.getLrf proto)}
    JonSharedCmd$Root$PayloadCase/LRF_CALIB {:lrf-calib (.getLrfCalib proto)}
    JonSharedCmd$Root$PayloadCase/ROTARY {:rotary (.getRotary proto)}
    JonSharedCmd$Root$PayloadCase/OSD {:osd (.getOsd proto)}
    JonSharedCmd$Root$PayloadCase/PING {:ping {}}
    JonSharedCmd$Root$PayloadCase/NOOP {:noop {}}
    JonSharedCmd$Root$PayloadCase/FROZEN {:frozen {}}
    JonSharedCmd$Root$PayloadCase/SYSTEM {:system (.getSystem proto)}
    JonSharedCmd$Root$PayloadCase/CV {:cv (.getCv proto)}
    JonSharedCmd$Root$PayloadCase/DAY_CAM_GLASS_HEATER {:day-cam-glass-heater (.getDayCamGlassHeater proto)}
    JonSharedCmd$Root$PayloadCase/LIRA {:lira (.getLira proto)}
    {}))
