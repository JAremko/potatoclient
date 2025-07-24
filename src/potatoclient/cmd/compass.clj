(ns potatoclient.cmd.compass
  "Compass command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn =>]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import [cmd.Compass
            JonSharedCmdCompass$Root
            JonSharedCmdCompass$Start JonSharedCmdCompass$Stop
            JonSharedCmdCompass$SetDeclination JonSharedCmdCompass$SetOffsetAngles
            JonSharedCmdCompass$CalibrateLong JonSharedCmdCompass$CalibrateShort
            JonSharedCmdCompass$CalibrateNext JonSharedCmdCompass$CalibrateCancel
            JonSharedCmdCompass$GetMeteo]))

;; ============================================================================
;; Basic Compass Control Commands
;; ============================================================================

(>defn start
  "Start the compass subsystem"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setStart (JonSharedCmdCompass$Start/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop
  "Stop the compass subsystem"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setStop (JonSharedCmdCompass$Stop/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Compass Configuration Commands
;; ============================================================================

(>defn set-declination
  "Set magnetic declination angle"
  [angle]
  [::specs/angle => nil?]
  (let [root-msg (cmd-core/create-root-message)
        decl-msg (-> (JonSharedCmdCompass$SetDeclination/newBuilder)
                     (.setAngle angle))
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setDeclination decl-msg))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-offset-angles
  "Set compass offset angles for azimuth and elevation"
  [azimuth-offset elevation-offset]
  [::specs/angle ::specs/angle => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset-msg (-> (JonSharedCmdCompass$SetOffsetAngles/newBuilder)
                       (.setAngleAz azimuth-offset)
                       (.setAngleEl elevation-offset))
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setOffsetAngles offset-msg))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Calibration Commands
;; ============================================================================

(>defn calibrate-long
  "Start long compass calibration process"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setCalibrateLong (JonSharedCmdCompass$CalibrateLong/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn calibrate-short
  "Start short/quick compass calibration process"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setCalibrateShort (JonSharedCmdCompass$CalibrateShort/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn calibrate-next
  "Move to next step in calibration process"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setCalibrateNext (JonSharedCmdCompass$CalibrateNext/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn calibrate-cancel
  "Cancel ongoing calibration process"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setCalibrateCancel (JonSharedCmdCompass$CalibrateCancel/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Read-Only Commands
;; ============================================================================

(>defn get-meteo
  "Get compass meteorological data (read-only safe)"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setGetMeteo (JonSharedCmdCompass$GetMeteo/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)