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

;; Define the command fields as a set for validation
(def command-fields
  #{:cv :day_camera :heat_camera :gps :compass :lrf :lrf_calib 
    :rotary :osd :ping :noop :frozen :system :day_cam_glass_heater :lira})

;; The flat spec that matches protobuf structure
(def jon-shared-cmd-root-spec
  [:map {:closed true}
   ;; Required fields
   [:protocol_version :proto/protocol-version]
   [:client_type :proto/client-type]
   ;; Optional metadata fields  
   [:session_id {:optional true} :int]
   [:important {:optional true} :boolean]
   [:from_cv_subsystem {:optional true} :boolean]
   ;; All command fields as optional (oneof behavior enforced by validator)
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
   [:lira {:optional true} [:maybe :cmd/lira]]])

;; Add validation for exactly one command
(def jon-shared-cmd-root-spec-with-validation
  [:and
   jon-shared-cmd-root-spec
   [:fn {:error/message "must have exactly one command field"}
    (fn [value]
      (let [present-cmds (filter #(contains? value %) command-fields)
            non-nil-cmds (filter #(some? (get value %)) present-cmds)]
        (= 1 (count non-nil-cmds))))]])

;; Generator using oneof_edn internally (much cleaner!)
;; We create a temporary nested structure for generation, then flatten it
(defmethod mg/-schema-generator :cmd/root [_ options]
  (let [;; Internal schema with oneof_edn for generation
        internal-schema [:map {:closed true}
                        [:protocol_version :proto/protocol-version]
                        [:client_type :proto/client-type]
                        [:session_id {:optional true} :int]
                        [:important {:optional true} :boolean]
                        [:from_cv_subsystem {:optional true} :boolean]
                        ;; Use oneof_edn for command generation!
                        [:_command [:oneof_edn
                                   [:cv :cmd/cv]
                                   [:day_camera :cmd/day-camera]
                                   [:heat_camera :cmd/heat-camera]
                                   [:gps :cmd/gps]
                                   [:compass :cmd/compass]
                                   [:lrf :cmd/lrf]
                                   [:lrf_calib :cmd/lrf-align]
                                   [:rotary :cmd/rotary]
                                   [:osd :cmd/osd]
                                   [:ping :cmd/ping]
                                   [:noop :cmd/noop]
                                   [:frozen :cmd/frozen]
                                   [:system :cmd/system]
                                   [:day_cam_glass_heater :cmd/day-cam-glass-heater]
                                   [:lira :cmd/lira]]]]
        ;; Generate from internal schema
        internal-gen (mg/generator internal-schema options)]
    ;; Transform: merge command field into root level
    (gen/fmap
     (fn [generated]
       (let [command (:_command generated)
             base (dissoc generated :_command)]
         (merge base command)))
     internal-gen)))

(registry/register! :cmd/root jon-shared-cmd-root-spec-with-validation)
(registry/register! :jon_shared_cmd_root jon-shared-cmd-root-spec-with-validation)