(ns cmd-explorer.specs.cmd-root
  "Core specs for JonCommand protobuf validation"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [cmd-explorer.registry :as registry]
   [cmd-explorer.specs.oneof-payload :as oneof]
   [pronto.core :as p])
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
   [cmd.DayCamera JonSharedCmdDayCamera$Root]
   [cmd.HeatCamera JonSharedCmdHeatCamera$Root]
   [cmd.Gps JonSharedCmdGps$Root]
   [cmd.Compass JonSharedCmdCompass$Root]
   [cmd.Lrf JonSharedCmdLrf$Root]
   [cmd.Lrf_calib JonSharedCmdLrfAlign$Root]
   [cmd.RotaryPlatform JonSharedCmdRotary$Root]
   [cmd.OSD JonSharedCmdOsd$Root]
   [cmd.System JonSharedCmdSystem$Root]
   [cmd.CV JonSharedCmdCv$Root]
   [cmd.DayCamGlassHeater JonSharedCmdDayCamGlassHeater$Root]
   [cmd.Lira JonSharedCmdLira$Root]
   [ser JonSharedDataTypes$JonGuiDataClientType]))

;; Create Pronto mapper for command messages
(p/defmapper cmd-mapper 
  [JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen])

;; Client type enum spec
(def client-type-spec
  [:enum
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA])

;; Register client type spec
(registry/register! :cmd/client-type client-type-spec)

;; Simple command specs (no fields)
(def ping-spec [:map])
(def noop-spec [:map])
(def frozen-spec [:map])

;; Register simple command specs
(registry/register! :cmd/ping ping-spec)
(registry/register! :cmd/noop noop-spec)
(registry/register! :cmd/frozen frozen-spec)

;; Placeholder specs for complex commands (to be defined in respective modules)
(registry/register! :cmd/day-camera-root [:map])
(registry/register! :cmd/heat-camera-root [:map])
(registry/register! :cmd/gps-root [:map])
(registry/register! :cmd/compass-root [:map])
(registry/register! :cmd/lrf-root [:map])
(registry/register! :cmd/lrf-calib-root [:map])
(registry/register! :cmd/rotary-root [:map])
(registry/register! :cmd/osd-root [:map])
(registry/register! :cmd/system-root [:map])
(registry/register! :cmd/cv-root [:map])
(registry/register! :cmd/day-cam-glass-heater-root [:map])
(registry/register! :cmd/lira-root [:map])

;; Define the cmd-root-spec for JonCommand validation
(def cmd-root-spec
  [:map
   [:protocol_version [:int {:min 1}]]
   [:session_id [:int {:min 0}]]
   [:important :boolean]
   [:from_cv_subsystem :boolean]
   [:client_type :cmd/client-type]
   ;; The payload field uses our custom oneof-pronto spec
   [:payload
    [:oneof-pronto
     {:proto-class JonSharedCmd$Root
      :proto-mapper cmd-mapper
      :day_camera :cmd/day-camera-root
      :heat_camera :cmd/heat-camera-root
      :gps :cmd/gps-root
      :compass :cmd/compass-root
      :lrf :cmd/lrf-root
      :lrf_calib :cmd/lrf-calib-root
      :rotary :cmd/rotary-root
      :osd :cmd/osd-root
      :ping :cmd/ping
      :noop :cmd/noop
      :frozen :cmd/frozen
      :system :cmd/system-root
      :cv :cmd/cv-root
      :day_cam_glass_heater :cmd/day-cam-glass-heater-root
      :lira :cmd/lira-root
      :error/message "Exactly one command payload must be set"}]]])

;; Register the cmd-root-spec globally
(registry/register! :cmd/root cmd-root-spec)

(defn validate-cmd-root
  "Validate a JonCommand proto-map against the cmd-root-spec"
  [proto-map]
  (m/validate :cmd/root proto-map))

(defn explain-cmd-root
  "Explain validation errors for a JonCommand proto-map"
  [proto-map]
  (m/explain :cmd/root proto-map))

(defn generate-cmd-root
  "Generate a sample JonCommand proto-map"
  []
  (mg/generate :cmd/root))