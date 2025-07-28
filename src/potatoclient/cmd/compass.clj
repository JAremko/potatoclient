(ns potatoclient.cmd.compass
  "Compass command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.specs :as specs])
  (:import (cmd.Compass
             JonSharedCmdCompass$CalibrateCencel
             JonSharedCmdCompass$CalibrateNext JonSharedCmdCompass$CalibrateStartLong
             JonSharedCmdCompass$CalibrateStartShort
             JonSharedCmdCompass$GetMeteo JonSharedCmdCompass$Root
             JonSharedCmdCompass$SetMagneticDeclination JonSharedCmdCompass$SetOffsetAngleAzimuth
             JonSharedCmdCompass$SetOffsetAngleElevation JonSharedCmdCompass$Start
             JonSharedCmdCompass$Stop)))

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
  [::specs/magnetic-declination => nil?]
  (let [root-msg (cmd-core/create-root-message)
        decl-msg (-> (JonSharedCmdCompass$SetMagneticDeclination/newBuilder)
                     (.setValue angle))
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setSetMagneticDeclination decl-msg))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-offset-angle-azimuth
  "Set compass offset angle for azimuth"
  [angle]
  [::specs/magnetic-declination => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset-msg (-> (JonSharedCmdCompass$SetOffsetAngleAzimuth/newBuilder)
                       (.setValue angle))
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setSetOffsetAngleAzimuth offset-msg))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn set-offset-angle-elevation
  "Set compass offset angle for elevation"
  [angle]
  [::specs/elevation-degrees => nil?]
  (let [root-msg (cmd-core/create-root-message)
        offset-msg (-> (JonSharedCmdCompass$SetOffsetAngleElevation/newBuilder)
                       (.setValue angle))
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setSetOffsetAngleElevation offset-msg))]
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
                         (.setStartCalibrateLong (JonSharedCmdCompass$CalibrateStartLong/newBuilder)))]
    (.setCompass root-msg compass-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn calibrate-short
  "Start short/quick compass calibration process"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        compass-root (-> (JonSharedCmdCompass$Root/newBuilder)
                         (.setStartCalibrateShort (JonSharedCmdCompass$CalibrateStartShort/newBuilder)))]
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
                         (.setCalibrateCencel (JonSharedCmdCompass$CalibrateCencel/newBuilder)))]
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
