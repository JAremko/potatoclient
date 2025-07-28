(ns potatoclient.cmd.glass-heater
  "Glass heater command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn]]
            [potatoclient.cmd.core :as cmd-core])
  (:import (cmd.DayCamGlassHeater
             JonSharedCmdDayCamGlassHeater$GetMeteo
             JonSharedCmdDayCamGlassHeater$Root JonSharedCmdDayCamGlassHeater$Start
             JonSharedCmdDayCamGlassHeater$Stop JonSharedCmdDayCamGlassHeater$TurnOff
             JonSharedCmdDayCamGlassHeater$TurnOn)))

;; ============================================================================
;; Glass Heater Control Commands
;; ============================================================================

(>defn start
  "Start the glass heater subsystem"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heater-root (-> (JonSharedCmdDayCamGlassHeater$Root/newBuilder)
                        (.setStart (JonSharedCmdDayCamGlassHeater$Start/newBuilder)))]
    (.setDayCamGlassHeater root-msg heater-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop
  "Stop the glass heater subsystem"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heater-root (-> (JonSharedCmdDayCamGlassHeater$Root/newBuilder)
                        (.setStop (JonSharedCmdDayCamGlassHeater$Stop/newBuilder)))]
    (.setDayCamGlassHeater root-msg heater-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn on
  "Turn the glass heater on"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heater-root (-> (JonSharedCmdDayCamGlassHeater$Root/newBuilder)
                        (.setTurnOn (JonSharedCmdDayCamGlassHeater$TurnOn/newBuilder)))]
    (.setDayCamGlassHeater root-msg heater-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn off
  "Turn the glass heater off"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heater-root (-> (JonSharedCmdDayCamGlassHeater$Root/newBuilder)
                        (.setTurnOff (JonSharedCmdDayCamGlassHeater$TurnOff/newBuilder)))]
    (.setDayCamGlassHeater root-msg heater-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Read-Only Commands
;; ============================================================================

(>defn get-meteo
  "Get glass heater status (read-only safe)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        heater-root (-> (JonSharedCmdDayCamGlassHeater$Root/newBuilder)
                        (.setGetMeteo (JonSharedCmdDayCamGlassHeater$GetMeteo/newBuilder)))]
    (.setDayCamGlassHeater root-msg heater-root)
    (cmd-core/send-cmd-message root-msg))
  nil)
