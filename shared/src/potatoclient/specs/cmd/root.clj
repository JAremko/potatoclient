(ns potatoclient.specs.cmd.root
  "Root Command message spec (JonSharedCmd.Root) using oneof structure.
   Based on jon_shared_cmd.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Import all command specs
   [potatoclient.specs.cmd.common]
   [potatoclient.specs.cmd.compass]
   [potatoclient.specs.cmd.cv]
   [potatoclient.specs.cmd.day-cam-glass-heater]
   [potatoclient.specs.cmd.day-camera]
   [potatoclient.specs.cmd.gps]
   [potatoclient.specs.cmd.heat-camera]
   [potatoclient.specs.cmd.lira]
   [potatoclient.specs.cmd.lrf]
   [potatoclient.specs.cmd.lrf-align]
   [potatoclient.specs.cmd.osd]
   [potatoclient.specs.cmd.rotary]
   [potatoclient.specs.cmd.system]))

;; Command field keys
(def command-fields
  #{:cv :day_camera :heat_camera :gps :compass :lrf :lrf_calib 
    :rotary :osd :ping :noop :frozen :system :day_cam_glass_heater :lira})

;; Custom generator that creates valid cmd/root messages
(defn cmd-root-generator [_schema options]
  (gen/bind
   (gen/tuple
    ;; Required fields
    (mg/generator :proto/protocol-version options)
    (mg/generator :proto/client-type options)
    ;; Pick one command
    (gen/elements (vec command-fields)))
   (fn [[pv ct cmd-key]]
     (gen/fmap
      (fn [cmd-value]
        (merge
         {:protocol_version pv
          :client_type ct}
         {cmd-key cmd-value}))
      ;; Generate value for the selected command
      (mg/generator 
       (case cmd-key
         :cv :cmd/cv
         :day_camera :cmd/day-camera
         :heat_camera :cmd/heat-camera
         :gps :cmd/gps
         :compass :cmd/compass
         :lrf :cmd/lrf
         :lrf_calib :cmd/lrf-align
         :rotary :cmd/rotary
         :osd :cmd/osd
         :ping :cmd/ping
         :noop :cmd/noop
         :frozen :cmd/frozen
         :system :cmd/system
         :day_cam_glass_heater :cmd/day-cam-glass-heater
         :lira :cmd/lira)
       options)))))

;; Validation function for exactly one command
(defn validate-oneof-command [value]
  (let [present-cmds (filter #(contains? value %) command-fields)
        non-nil-cmds (filter #(some? (get value %)) present-cmds)]
    (= 1 (count non-nil-cmds))))

(def jon-shared-cmd-root-spec
  [:and
   {:gen/gen cmd-root-generator}
   [:map {:closed true}
    ;; Required fields
    [:protocol_version :proto/protocol-version]
    [:client_type :proto/client-type]
    ;; Optional metadata fields  
    [:session_id {:optional true} :int]
    [:important {:optional true} :boolean]
    [:from_cv_subsystem {:optional true} :boolean]
    ;; All command fields as optional
    [:cv {:optional true} [:maybe :cmd/cv]]
    [:day_camera {:optional true} [:maybe :cmd/day-camera]]
    [:heat_camera {:optional true} [:maybe :cmd/heat-camera]]
    [:gps {:optional true} [:maybe :cmd/gps]]
    [:compass {:optional true} [:maybe :cmd/compass]]
    [:lrf {:optional true} [:maybe :cmd/lrf]]
    [:lrf_calib {:optional true} [:maybe :cmd/lrf-align]]
    [:rotary {:optional true} [:maybe :cmd/rotary]]
    [:osd {:optional true} [:maybe :cmd/osd]]
    [:ping {:optional true} [:maybe :cmd/ping]]
    [:noop {:optional true} [:maybe :cmd/noop]]
    [:frozen {:optional true} [:maybe :cmd/frozen]]
    [:system {:optional true} [:maybe :cmd/system]]
    [:day_cam_glass_heater {:optional true} [:maybe :cmd/day-cam-glass-heater]]
    [:lira {:optional true} [:maybe :cmd/lira]]]
   ;; Custom validation: exactly one command field must be present
   [:fn {:error/message "must have exactly one command field"}
    validate-oneof-command]])

(registry/register! :cmd/root jon-shared-cmd-root-spec)
(registry/register! :jon_shared_cmd_root jon-shared-cmd-root-spec)