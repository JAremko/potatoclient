(ns test.enum.cmd.daycamglassheater
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root
           cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start
           cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop
           cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn
           cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff
           cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo))

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
     {:start [:map [:start :cmd.day-cam-glass-heater/start]],
      :stop [:map [:stop :cmd.day-cam-glass-heater/stop]],
      :turn-on [:map [:turn-on :cmd.day-cam-glass-heater/turn-on]],
      :turn-off [:map [:turn-off :cmd.day-cam-glass-heater/turn-off]],
      :get-meteo [:map [:get-meteo :cmd.day-cam-glass-heater/get-meteo]],
      :error/message "This oneof field is required"}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def turn-on-spec "Malli spec for turn-on message" [:map])

(def turn-off-spec "Malli spec for turn-off message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-start)
(declare build-stop)
(declare build-turn-on)
(declare build-turn-off)
(declare build-get-meteo)
(declare parse-root)
(declare parse-start)
(declare parse-stop)
(declare parse-turn-on)
(declare parse-turn-off)
(declare parse-get-meteo)
(declare build-root-payload)
(declare parse-root-payload)

(>defn
  build-root
  "Build a Root protobuf message from a map."
  [m]
  [root-spec => any?]
  (let [builder
          (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:start :stop :turn-on :turn-off
                                             :get-meteo}
                                           k))
                                  (:cmd m)))]
      (build-root-cmd builder cmd-field))
    (.build builder)))

(>defn
  build-start
  "Build a Start protobuf message from a map."
  [m]
  [start-spec => any?]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start/newBuilder)]
    (.build builder)))

(>defn
  build-stop
  "Build a Stop protobuf message from a map."
  [m]
  [stop-spec => any?]
  (let [builder
          (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop/newBuilder)]
    (.build builder)))

(>defn
  build-turn-on
  "Build a TurnOn protobuf message from a map."
  [m]
  [turn-on-spec => any?]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn/newBuilder)]
    (.build builder)))

(>defn
  build-turn-off
  "Build a TurnOff protobuf message from a map."
  [m]
  [turn-off-spec => any?]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff/newBuilder)]
    (.build builder)))

(>defn
  build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  [get-meteo-spec => any?]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo/newBuilder)]
    (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-start
       "Parse a Start protobuf message to a map."
       [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start proto]
       [any? => start-spec]
       {})

(>defn parse-stop
       "Parse a Stop protobuf message to a map."
       [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop proto]
       [any? => stop-spec]
       {})

(>defn parse-turn-on
       "Parse a TurnOn protobuf message to a map."
       [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn proto]
       [any? => turn-on-spec]
       {})

(>defn parse-turn-off
       "Parse a TurnOff protobuf message to a map."
       [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff proto]
       [any? => turn-off-spec]
       {})

(>defn parse-get-meteo
       "Parse a GetMeteo protobuf message to a map."
       [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo proto]
       [any? => get-meteo-spec]
       {})

(>defn- build-root-payload
        "Build the oneof payload for Root."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
        (case field-key
          :start (.setStart builder (build-start value))
          :stop (.setStop builder (build-stop value))
          :turn-on (.setTurnOn builder (build-turn-on value))
          :turn-off (.setTurnOff builder (build-turn-off value))
          :get-meteo (.setGetMeteo builder (build-get-meteo value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":cmd"}))))

(>defn- parse-root-payload
        "Parse the oneof payload from Root."
        [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
        [any? => (? map?)]
        (cond (.hasStart proto) {:start (parse-start (.getStart proto))}
              (.hasStop proto) {:stop (parse-stop (.getStop proto))}
              (.hasTurnOn proto) {:turn-on (parse-turn-on (.getTurnOn proto))}
              (.hasTurnOff proto) {:turn-off (parse-turn-off (.getTurnOff
                                                               proto))}
              (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo
                                                                  proto))}))