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

;; Root spec that directly represents the protobuf structure
;; The commands are at the top level (not nested under a :command field)
;; This matches the Pronto EDN output where one command field is non-nil
(def jon-shared-cmd-root-spec
  [:map {:closed true}
   ;; Required fields
   [:protocol_version :proto/protocol-version]
   [:client_type :proto/client-type]
   ;; Optional metadata fields  
   [:session_id {:optional true} :int]
   [:important {:optional true} :boolean]
   [:from_cv_subsystem {:optional true} :boolean]
   ;; All command fields as optional (oneof behavior enforced by custom validator)
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

;; Alternative: If we want to enforce the oneof at schema level,
;; we need to extract just the command fields as a separate oneof
(def jon-shared-cmd-root-spec-with-validation
  [:and
   jon-shared-cmd-root-spec
   [:fn {:error/message "must have exactly one command field"}
    (fn [value]
      (let [command-fields #{:cv :day_camera :heat_camera :gps :compass :lrf 
                            :lrf_calib :rotary :osd :ping :noop :frozen 
                            :system :day_cam_glass_heater :lira}
            present-cmds (filter #(contains? value %) command-fields)
            non-nil-cmds (filter #(some? (get value %)) present-cmds)]
        (= 1 (count non-nil-cmds))))]])

;; Custom generator that ensures exactly one command
(defmethod mg/-schema-generator :cmd/root [_ options]
  (gen/bind
   (gen/tuple
    ;; Required fields
    (mg/generator :proto/protocol-version options)
    (mg/generator :proto/client-type options)
    ;; Optional fields
    (gen/elements [nil (gen/generate (mg/generator :int options))])
    (gen/elements [nil true false])
    (gen/elements [nil true false])
    ;; Pick one command
    (gen/elements [:cv :day_camera :heat_camera :gps :compass :lrf 
                  :lrf_calib :rotary :osd :ping :noop :frozen 
                  :system :day_cam_glass_heater :lira]))
   (fn [[pv ct session important from-cv cmd-key]]
     (gen/fmap
      (fn [cmd-value]
        (cond-> {:protocol_version pv
                 :client_type ct
                 cmd-key cmd-value}
          session (assoc :session_id session)
          (some? important) (assoc :important important)
          (some? from-cv) (assoc :from_cv_subsystem from-cv)))
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

(registry/register! :cmd/root jon-shared-cmd-root-spec-with-validation)
(registry/register! :jon_shared_cmd_root jon-shared-cmd-root-spec-with-validation)