(ns potatoclient.proto.cmd.daycamglassheater
  "Generated protobuf functions."
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

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
                                          (#{:start :stop :turn-on :turn-off
                                             :get-meteo}
                                           k))
                                  m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start/newBuilder)]
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder
          (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop/newBuilder)]
    (.build builder)))

(defn build-turn-on
  "Build a TurnOn protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn/newBuilder)]
    (.build builder)))

(defn build-turn-off
  "Build a TurnOff protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let
    [builder
       (cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo/newBuilder)]
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop proto]
  {})

(defn parse-turn-on
  "Parse a TurnOn protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn proto]
  {})

(defn parse-turn-off
  "Parse a TurnOff protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :turn-on (.setTurnOn builder (build-turn-on value))
    :turn-off (.setTurnOff builder (build-turn-off value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root proto]
  (cond (.hasStart proto) {:start (parse-start (.getStart proto))}
        (.hasStop proto) {:stop (parse-stop (.getStop proto))}
        (.hasTurnOn proto) {:turn-on (parse-turn-on (.getTurnOn proto))}
        (.hasTurnOff proto) {:turn-off (parse-turn-off (.getTurnOff proto))}
        (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo
                                                            proto))}))