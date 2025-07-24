(ns potatoclient.cmd.system
  "System-level command functions for PotatoClient"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn ? =>]]
            [potatoclient.cmd.core :as cmd-core])
  (:import [cmd.System
            JonSharedCmdSystem$Root
            JonSharedCmdSystem$Reboot JonSharedCmdSystem$PowerOff
            JonSharedCmdSystem$ResetConfigs JonSharedCmdSystem$StartALl
            JonSharedCmdSystem$StopALl JonSharedCmdSystem$StartRec
            JonSharedCmdSystem$StopRec JonSharedCmdSystem$MarkRecImportant
            JonSharedCmdSystem$UnmarkRecImportant JonSharedCmdSystem$EnterTransport
            JonSharedCmdSystem$EnableGeodesicMode JonSharedCmdSystem$DisableGeodesicMode
            JonSharedCmdSystem$SetLocalization]
           [data JonSharedDataTypes$JonGuiDataSystemLocalizations]))

;; ============================================================================
;; System Control Commands
;; ============================================================================

(>defn reboot
  "Reboot the system"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setReboot (JonSharedCmdSystem$Reboot/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn power-off
  "Power off the system"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setPowerOff (JonSharedCmdSystem$PowerOff/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn reset-configs
  "Reset system configurations to defaults"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setResetConfigs (JonSharedCmdSystem$ResetConfigs/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn enter-transport
  "Enter transport mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setEnterTransport (JonSharedCmdSystem$EnterTransport/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Subsystem Control Commands
;; ============================================================================

(>defn start-all
  "Start all subsystems"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setStartAll (JonSharedCmdSystem$StartALl/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop-all
  "Stop all subsystems"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setStopAll (JonSharedCmdSystem$StopALl/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Recording Commands
;; ============================================================================

(>defn start-rec
  "Start recording"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setStartRec (JonSharedCmdSystem$StartRec/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn stop-rec
  "Stop recording"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setStopRec (JonSharedCmdSystem$StopRec/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn mark-rec-important
  "Mark current recording as important"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setMarkRecImportant (JonSharedCmdSystem$MarkRecImportant/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn unmark-rec-important
  "Unmark current recording as important"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setUnmarkRecImportant (JonSharedCmdSystem$UnmarkRecImportant/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Mode Commands
;; ============================================================================

(>defn enable-geodesic-mode
  "Enable geodesic mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setGeodesicModeEnable (JonSharedCmdSystem$EnableGeodesicMode/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

(>defn disable-geodesic-mode
  "Disable geodesic mode"
  []
  [=> nil?]
  (let [root-msg (cmd-core/create-root-message)
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setGeodesicModeDisable (JonSharedCmdSystem$DisableGeodesicMode/newBuilder)))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Localization Commands
;; ============================================================================

(>defn set-localization
  "Set system localization"
  [localization]
  [[:fn {:error/message "must be a JonGuiDataSystemLocalizations enum"}
    #(instance? JonSharedDataTypes$JonGuiDataSystemLocalizations %)] => nil?]
  (let [root-msg (cmd-core/create-root-message)
        loc-msg (-> (JonSharedCmdSystem$SetLocalization/newBuilder)
                    (.setLoc localization))
        system-root (-> (JonSharedCmdSystem$Root/newBuilder)
                        (.setLocalization loc-msg))]
    (.setSystem root-msg system-root)
    (cmd-core/send-cmd-message root-msg))
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn string->localization
  "Convert string to localization enum value"
  [value]
  [string? => (? [:fn {:error/message "must be a JonGuiDataSystemLocalizations enum"}
                  #(instance? JonSharedDataTypes$JonGuiDataSystemLocalizations %)])]
  (case value
    "en" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
    "english" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
    "ua" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
    "ukrainian" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
    "ar" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
    "arabic" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
    "cs" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
    "czech" JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_CS
    nil))