(ns potatoclient.cmd.lrf
  "LRF (Laser Range Finder) command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn]]
            [potatoclient.cmd.core :as cmd-core])
  (:import (cmd.Lrf
             JonSharedCmdLrf$DisableFogMode
             JonSharedCmdLrf$EnableFogMode JonSharedCmdLrf$GetMeteo
             JonSharedCmdLrf$Measure JonSharedCmdLrf$NewSession
             JonSharedCmdLrf$RefineOff JonSharedCmdLrf$RefineOn
             JonSharedCmdLrf$Root JonSharedCmdLrf$ScanOff
             JonSharedCmdLrf$ScanOn JonSharedCmdLrf$Start
             JonSharedCmdLrf$Stop JonSharedCmdLrf$TargetDesignatorOff
             JonSharedCmdLrf$TargetDesignatorOnModeA JonSharedCmdLrf$TargetDesignatorOnModeB)))

;; ============================================================================
;; Basic LRF Control Commands
;; ============================================================================

(>defn start
  "Start the LRF subsystem"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setStart (JonSharedCmdLrf$Start/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop
  "Stop the LRF subsystem"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setStop (JonSharedCmdLrf$Stop/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn measure
  "Trigger a single LRF measurement"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setMeasure (JonSharedCmdLrf$Measure/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn new-session
  "Start a new measurement session"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setNewSession (JonSharedCmdLrf$NewSession/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Scan Mode Commands
;; ============================================================================

(>defn scan-on
  "Enable scan mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setScanOn (JonSharedCmdLrf$ScanOn/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn scan-off
  "Disable scan mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setScanOff (JonSharedCmdLrf$ScanOff/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Fog Mode Commands
;; ============================================================================

(>defn enable-fog-mode
  "Enable fog mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setEnableFogMode (JonSharedCmdLrf$EnableFogMode/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-fog-mode
  "Disable fog mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setDisableFogMode (JonSharedCmdLrf$DisableFogMode/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Target Designator Commands
;; ============================================================================

(>defn target-designator-off
  "Turn off target designator"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setTargetDesignatorOff (JonSharedCmdLrf$TargetDesignatorOff/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn target-designator-mode-a
  "Set target designator to mode A"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setTargetDesignatorOnModeA (JonSharedCmdLrf$TargetDesignatorOnModeA/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn target-designator-mode-b
  "Set target designator to mode B"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setTargetDesignatorOnModeB (JonSharedCmdLrf$TargetDesignatorOnModeB/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Refine Mode Commands
;; ============================================================================

(>defn refine-on
  "Enable refine mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setRefineOn (JonSharedCmdLrf$RefineOn/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn refine-off
  "Disable refine mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setRefineOff (JonSharedCmdLrf$RefineOff/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Read-Only Commands
;; ============================================================================

(>defn get-meteo
  "Get LRF meteorological data (read-only safe)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        lrf-root (-> (JonSharedCmdLrf$Root/newBuilder)
                     (.setGetMeteo (JonSharedCmdLrf$GetMeteo/newBuilder)))]
    (.setLrf root-msg lrf-root)
    (cmd-core/send-cmd-message root-msg))
  nil)
