(ns test.proto.ns.cmd.gps
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import cmd.Gps.JonSharedCmdGps$Root
           cmd.Gps.JonSharedCmdGps$Start
           cmd.Gps.JonSharedCmdGps$Stop
           cmd.Gps.JonSharedCmdGps$GetMeteo
           cmd.Gps.JonSharedCmdGps$SetUseManualPosition
           cmd.Gps.JonSharedCmdGps$SetManualPosition))

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
     {:start [:map [:start :cmd.gps/start]],
      :stop [:map [:stop :cmd.gps/stop]],
      :set-manual-position
        [:map [:set-manual-position :cmd.gps/set-manual-position]],
      :set-use-manual-position
        [:map [:set-use-manual-position :cmd.gps/set-use-manual-position]],
      :get-meteo [:map [:get-meteo :cmd.gps/get-meteo]]}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def set-use-manual-position-spec
  "Malli spec for set-use-manual-position message"
  [:map [:flag [:maybe :boolean]]])

(def set-manual-position-spec
  "Malli spec for set-manual-position message"
  [:map [:latitude [:maybe :float]] [:longitude [:maybe :float]]
   [:altitude [:maybe :float]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-start)
(declare build-stop)
(declare build-get-meteo)
(declare build-set-use-manual-position)
(declare build-set-manual-position)
(declare parse-root)
(declare parse-start)
(declare parse-stop)
(declare parse-get-meteo)
(declare parse-set-use-manual-position)
(declare parse-set-manual-position)
(declare build-root-payload)
(declare parse-root-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => #(instance? cmd.Gps.JonSharedCmdGps$Root %)]
       (let [builder (cmd.Gps.JonSharedCmdGps$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field
                      (first (filter (fn [[k v]]
                                       (#{:start :stop :set-manual-position
                                          :set-use-manual-position :get-meteo}
                                        k))
                               m))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-start
       "Build a Start protobuf message from a map."
       [m]
       [start-spec => #(instance? cmd.Gps.JonSharedCmdGps$Start %)]
       (let [builder (cmd.Gps.JonSharedCmdGps$Start/newBuilder)]
         (.build builder)))

(>defn build-stop
       "Build a Stop protobuf message from a map."
       [m]
       [stop-spec => #(instance? cmd.Gps.JonSharedCmdGps$Stop %)]
       (let [builder (cmd.Gps.JonSharedCmdGps$Stop/newBuilder)]
         (.build builder)))

(>defn build-get-meteo
       "Build a GetMeteo protobuf message from a map."
       [m]
       [get-meteo-spec => #(instance? cmd.Gps.JonSharedCmdGps$GetMeteo %)]
       (let [builder (cmd.Gps.JonSharedCmdGps$GetMeteo/newBuilder)]
         (.build builder)))

(>defn build-set-use-manual-position
       "Build a SetUseManualPosition protobuf message from a map."
       [m]
       [set-use-manual-position-spec =>
        #(instance? cmd.Gps.JonSharedCmdGps$SetUseManualPosition %)]
       (let [builder (cmd.Gps.JonSharedCmdGps$SetUseManualPosition/newBuilder)]
         ;; Set regular fields
         (when (contains? m :flag) (.setFlag builder (get m :flag)))
         (.build builder)))

(>defn build-set-manual-position
       "Build a SetManualPosition protobuf message from a map."
       [m]
       [set-manual-position-spec =>
        #(instance? cmd.Gps.JonSharedCmdGps$SetManualPosition %)]
       (let [builder (cmd.Gps.JonSharedCmdGps$SetManualPosition/newBuilder)]
         ;; Set regular fields
         (when (contains? m :latitude) (.setLatitude builder (get m :latitude)))
         (when (contains? m :longitude)
           (.setLongitude builder (get m :longitude)))
         (when (contains? m :altitude) (.setAltitude builder (get m :altitude)))
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.Gps.JonSharedCmdGps$Root proto]
       [#(instance? cmd.Gps.JonSharedCmdGps$Root %) => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-start
       "Parse a Start protobuf message to a map."
       [^cmd.Gps.JonSharedCmdGps$Start proto]
       [any? => start-spec]
       {})

(>defn parse-stop
       "Parse a Stop protobuf message to a map."
       [^cmd.Gps.JonSharedCmdGps$Stop proto]
       [any? => stop-spec]
       {})

(>defn parse-get-meteo
       "Parse a GetMeteo protobuf message to a map."
       [^cmd.Gps.JonSharedCmdGps$GetMeteo proto]
       [any? => get-meteo-spec]
       {})

(>defn parse-set-use-manual-position
       "Parse a SetUseManualPosition protobuf message to a map."
       [^cmd.Gps.JonSharedCmdGps$SetUseManualPosition proto]
       [#(instance? cmd.Gps.JonSharedCmdGps$SetUseManualPosition %) =>
        set-use-manual-position-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :flag (.getFlag proto))))

(>defn parse-set-manual-position
       "Parse a SetManualPosition protobuf message to a map."
       [^cmd.Gps.JonSharedCmdGps$SetManualPosition proto]
       [#(instance? cmd.Gps.JonSharedCmdGps$SetManualPosition %) =>
        set-manual-position-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :latitude (.getLatitude proto))
         true (assoc :longitude (.getLongitude proto))
         true (assoc :altitude (.getAltitude proto))))

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [#(instance? cmd.Gps.JonSharedCmdGps$Root$Builder %) [:tuple keyword? any?] =>
   #(instance? cmd.Gps.JonSharedCmdGps$Root$Builder %)]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :set-manual-position
      (.setSetManualPosition builder (build-set-manual-position value))
    :set-use-manual-position
      (.setSetUseManualPosition builder (build-set-use-manual-position value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn- parse-root-payload
        "Parse the oneof payload from Root."
        [^cmd.Gps.JonSharedCmdGps$Root proto]
        [#(instance? cmd.Gps.JonSharedCmdGps$Root %) => (? map?)]
        (cond (.hasStart proto) {:start (parse-start (.getStart proto))}
              (.hasStop proto) {:stop (parse-stop (.getStop proto))}
              (.hasSetManualPosition proto) {:set-manual-position
                                               (parse-set-manual-position
                                                 (.getSetManualPosition proto))}
              (.hasSetUseManualPosition proto)
                {:set-use-manual-position (parse-set-use-manual-position
                                            (.getSetUseManualPosition proto))}
              (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo
                                                                  proto))}))