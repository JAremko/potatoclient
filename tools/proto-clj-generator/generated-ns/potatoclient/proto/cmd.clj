(ns potatoclient.proto.cmd
  "Generated protobuf functions."
  (:import
   cmd.JonSharedCmd$Root
   cmd.JonSharedCmd$Ping
   cmd.JonSharedCmd$Noop
   cmd.JonSharedCmd$Frozen))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

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
      (.setClientType builder (get jon-gui-data-client-type-values (get m :client-type))))

    ;; Handle oneof: payload
    (when-let [payload-field (first (filter (fn [[k v]] (#{:day-camera :heat-camera :gps :compass :lrf :lrf-calib :rotary :osd :ping :noop :frozen :system :cv :day-cam-glass-heater :lira} k)) m))]
      (build-root-payload builder payload-field))
    (.build builder)))

(defn build-ping
  "Build a Ping protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Ping/newBuilder)]

    (.build builder)))

(defn build-noop
  "Build a Noop protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Noop/newBuilder)]

    (.build builder)))

(defn build-frozen
  "Build a Frozen protobuf message from a map."
  [m]
  (let [builder (cmd.JonSharedCmd$Frozen/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.JonSharedCmd$Root proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :protocol-version (.getProtocolVersion proto))
    true (assoc :session-id (.getSessionId proto))
    true (assoc :important (.getImportant proto))
    true (assoc :from-cv-subsystem (.getFromCvSubsystem proto))
    true (assoc :client-type (get jon-gui-data-client-type-keywords (.getClientType proto)))

    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-ping
  "Parse a Ping protobuf message to a map."
  [^cmd.JonSharedCmd$Ping proto]
  {})

(defn parse-noop
  "Parse a Noop protobuf message to a map."
  [^cmd.JonSharedCmd$Noop proto]
  {})

(defn parse-frozen
  "Parse a Frozen protobuf message to a map."
  [^cmd.JonSharedCmd$Frozen proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :day-camera (.setDayCamera builder (build-root value))
    :heat-camera (.setHeatCamera builder (build-root value))
    :gps (.setGps builder (build-root value))
    :compass (.setCompass builder (build-root value))
    :lrf (.setLrf builder (build-root value))
    :lrf-calib (.setLrfCalib builder (build-root value))
    :rotary (.setRotary builder (build-root value))
    :osd (.setOsd builder (build-root value))
    :ping (.setPing builder (build-ping value))
    :noop (.setNoop builder (build-noop value))
    :frozen (.setFrozen builder (build-frozen value))
    :system (.setSystem builder (build-root value))
    :cv (.setCv builder (build-root value))
    :day-cam-glass-heater (.setDayCamGlassHeater builder (build-root value))
    :lira (.setLira builder (build-root value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":payload"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.JonSharedCmd$Root proto]
  (cond
    (.hasDayCamera proto) {:day-camera (parse-root (.getDayCamera proto))}
    (.hasHeatCamera proto) {:heat-camera (parse-root (.getHeatCamera proto))}
    (.hasGps proto) {:gps (parse-root (.getGps proto))}
    (.hasCompass proto) {:compass (parse-root (.getCompass proto))}
    (.hasLrf proto) {:lrf (parse-root (.getLrf proto))}
    (.hasLrfCalib proto) {:lrf-calib (parse-root (.getLrfCalib proto))}
    (.hasRotary proto) {:rotary (parse-root (.getRotary proto))}
    (.hasOsd proto) {:osd (parse-root (.getOsd proto))}
    (.hasPing proto) {:ping (parse-ping (.getPing proto))}
    (.hasNoop proto) {:noop (parse-noop (.getNoop proto))}
    (.hasFrozen proto) {:frozen (parse-frozen (.getFrozen proto))}
    (.hasSystem proto) {:system (parse-root (.getSystem proto))}
    (.hasCv proto) {:cv (parse-root (.getCv proto))}
    (.hasDayCamGlassHeater proto) {:day-cam-glass-heater (parse-root (.getDayCamGlassHeater proto))}
    (.hasLira proto) {:lira (parse-root (.getLira proto))}))