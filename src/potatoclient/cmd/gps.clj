(ns potatoclient.cmd.gps
  "GPS command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import (cmd.Gps
             JonSharedCmdGps$GetMeteo
             JonSharedCmdGps$Root JonSharedCmdGps$SetManualPosition
             JonSharedCmdGps$SetUseManualPosition JonSharedCmdGps$Start
             JonSharedCmdGps$Stop)))

;; ============================================================================
;; Basic GPS Commands
;; ============================================================================

(>defn start
  "Start GPS module"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        gps-root (-> (JonSharedCmdGps$Root/newBuilder)
                     (.setStart (JonSharedCmdGps$Start/newBuilder)))]
    (.setGps root-msg gps-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop
  "Stop GPS module"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        gps-root (-> (JonSharedCmdGps$Root/newBuilder)
                     (.setStop (JonSharedCmdGps$Stop/newBuilder)))]
    (.setGps root-msg gps-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Manual Position Commands
;; ============================================================================

(>defn set-manual-position
  "Set manual GPS position"
  [latitude longitude altitude]
  [::specs/gps-latitude ::specs/gps-longitude ::specs/gps-altitude => nil?]
  (let [root-msg (cmd-core/create-root-message)
        position (-> (JonSharedCmdGps$SetManualPosition/newBuilder)
                     (.setLatitude (float latitude))
                     (.setLongitude (float longitude))
                     (.setAltitude (float altitude)))
        gps-root (-> (JonSharedCmdGps$Root/newBuilder)
                     (.setSetManualPosition position))]
    (.setGps root-msg gps-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-use-manual-position
  "Enable or disable use of manual GPS position"
  [use-manual]
  [boolean? => nil?]
  (let [root-msg (cmd-core/create-root-message)
        use-manual-msg (-> (JonSharedCmdGps$SetUseManualPosition/newBuilder)
                           (.setFlag use-manual))
        gps-root (-> (JonSharedCmdGps$Root/newBuilder)
                     (.setSetUseManualPosition use-manual-msg))]
    (.setGps root-msg gps-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Meteo Data Commands
;; ============================================================================

(>defn get-meteo
  "Request GPS meteorological data (read-only safe)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        gps-root (-> (JonSharedCmdGps$Root/newBuilder)
                     (.setGetMeteo (JonSharedCmdGps$GetMeteo/newBuilder)))]
    (.setGps root-msg gps-root)
    (cmd-core/send-cmd-message root-msg))
  nil)
