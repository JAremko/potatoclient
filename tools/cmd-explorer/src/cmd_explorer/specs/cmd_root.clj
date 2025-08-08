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
;; Include all subsystem root classes as well for complete mapping
(p/defmapper cmd-mapper 
  [JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen
   JonSharedCmdDayCamera$Root
   JonSharedCmdHeatCamera$Root
   JonSharedCmdGps$Root
   JonSharedCmdCompass$Root
   JonSharedCmdLrf$Root
   JonSharedCmdLrfAlign$Root
   JonSharedCmdRotary$Root
   JonSharedCmdOsd$Root
   JonSharedCmdSystem$Root
   JonSharedCmdCv$Root
   JonSharedCmdDayCamGlassHeater$Root
   JonSharedCmdLira$Root])

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
;; This spec validates the payload oneof field with proper instance checks
(def cmd-root-proto-spec
  "Spec for validating JonSharedCmd$Root proto-maps.
   Uses oneof-pronto to ensure exactly one payload field is set."
  [:fn
   {:error/message "Must be a valid JonSharedCmd$Root proto-map"}
   (fn [value]
     (and (p/proto-map? value)
          (instance? JonSharedCmd$Root (.pmap_getProto value))
          ;; Must have exactly one payload field set
          (some? (p/which-one-of value :payload))))])

;; Payload oneof spec using instance checks for each field type
(def cmd-payload-spec
  [:oneof-pronto
   {:proto-class JonSharedCmd$Root
    :proto-mapper cmd-mapper
    :oneof-name :payload
    ;; Simple commands
    :ping [:fn #(instance? JonSharedCmd$Ping %)]
    :noop [:fn #(instance? JonSharedCmd$Noop %)]
    :frozen [:fn #(instance? JonSharedCmd$Frozen %)]
    ;; Subsystem commands
    :day_camera [:fn #(instance? JonSharedCmdDayCamera$Root %)]
    :heat_camera [:fn #(instance? JonSharedCmdHeatCamera$Root %)]
    :gps [:fn #(instance? JonSharedCmdGps$Root %)]
    :compass [:fn #(instance? JonSharedCmdCompass$Root %)]
    :lrf [:fn #(instance? JonSharedCmdLrf$Root %)]
    :lrf_calib [:fn #(instance? JonSharedCmdLrfAlign$Root %)]
    :rotary [:fn #(instance? JonSharedCmdRotary$Root %)]
    :osd [:fn #(instance? JonSharedCmdOsd$Root %)]
    :system [:fn #(instance? JonSharedCmdSystem$Root %)]
    :cv [:fn #(instance? JonSharedCmdCv$Root %)]
    :day_cam_glass_heater [:fn #(instance? JonSharedCmdDayCamGlassHeater$Root %)]
    :lira [:fn #(instance? JonSharedCmdLira$Root %)]
    :error/message "Exactly one command payload must be set"}])

;; Register specs globally
(registry/register! :cmd/root-proto cmd-root-proto-spec)
(registry/register! :cmd/payload cmd-payload-spec)

(defn validate-cmd-root
  "Validate a JonCommand proto-map against the cmd-root-spec"
  [proto-map]
  (m/validate :cmd/root-proto proto-map))

(defn explain-cmd-root
  "Explain validation errors for a JonCommand proto-map"
  [proto-map]
  (m/explain :cmd/root-proto proto-map))

(defn get-command-type
  "Get the active command type from a JonSharedCmd$Root proto-map"
  [proto-map]
  (p/which-one-of proto-map :payload))

(defn create-ping-command
  "Create a simple ping command"
  []
  (p/proto-map cmd-mapper JonSharedCmd$Root
               :protocol_version 1
               :ping (p/proto-map cmd-mapper JonSharedCmd$Ping)))

(defn create-noop-command
  "Create a simple noop command"
  []
  (p/proto-map cmd-mapper JonSharedCmd$Root
               :protocol_version 1
               :noop (p/proto-map cmd-mapper JonSharedCmd$Noop)))

(defn create-frozen-command
  "Create a simple frozen command"
  []
  (p/proto-map cmd-mapper JonSharedCmd$Root
               :protocol_version 1
               :frozen (p/proto-map cmd-mapper JonSharedCmd$Frozen)))