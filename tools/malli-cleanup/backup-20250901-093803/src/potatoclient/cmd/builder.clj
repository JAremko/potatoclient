(ns potatoclient.cmd.builder
  "Efficient command building utilities using Pronto performance patterns.
   Provides functions to populate missing fields in proto-maps."
  (:require
   [pronto.core :as p]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root]) ; Load the specs
  (:import
   [cmd JonSharedCmd$Root]))

;; Initialize registry to access specs
(registry/setup-global-registry!)

;; ============================================================================
;; Default Values
;; ============================================================================

(def default-protocol-fields
  "Default values for required protocol fields"
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :session_id 0  ; Placeholder - server overrides this anyway
   :important false
   :from_cv_subsystem false})

;; ============================================================================
;; Efficient Proto-Map Building
;; ============================================================================

(defn populate-cmd-fields
  "Takes a command payload (just the oneof part like {:ping {}}) and 
   returns a complete cmd root with all required protocol fields populated.
   
   This is the primary function for creating valid cmd roots from payloads." {:malli/schema [:=> [:cat [:map {:closed false}]] :cmd/root]}
  [payload]
  ;; Stricter validation - must be a non-nil map
  ;; Merge the payload with default protocol fields to create a complete cmd
  (merge default-protocol-fields payload))

(defn populate-cmd-fields-with-overrides
  "Populate missing fields with specific override values.
   Useful for testing different configurations.
   
   Takes a command and a map of override values for the protocol fields." {:malli/schema [:=> [:cat [:map {:closed false}] [:map {:closed false}]] :cmd/root]}
  [cmd overrides]
  ;; Stricter validation - both must be non-nil maps
  (let [fields-to-use (merge default-protocol-fields overrides)
        complete-cmd (merge fields-to-use cmd)]
    complete-cmd))

(defn create-full-cmd
  "Create a full command from a payload with custom field values.
   Most efficient way to create a command when you know all values upfront.
   
   Example:
   (create-full-cmd {:ping {}} 
                    {:session_id 12345 :important true})" {:malli/schema [:=> [:cat :cmd/payload [:map {:closed false}]] :cmd/root]}
  [payload-cmd field-overrides]
  ;; Validate payload structure and require non-nil map for overrides
  (populate-cmd-fields-with-overrides payload-cmd field-overrides))

(defn create-proto-map-cmd
  "Create a proto-map command efficiently.
   Uses Pronto's proto-map constructor with all fields at once (fastest).
   
   This is the most efficient way to create a proto-map command." {:malli/schema [:=> [:cat :cmd/root] :any]}
  [cmd]
  ;; Find the oneof field that's present
  (let [oneof-fields #{:day_camera :heat_camera :gps :compass :lrf 
                       :lrf_calib :rotary :osd :ping :noop :frozen 
                       :system :cv :day_cam_glass_heater :lira}
        oneof-entry (first (filter (fn [[k v]] 
                                     (and (contains? oneof-fields k) 
                                          (some? v)))
                                   cmd))
        [oneof-key oneof-val] oneof-entry]
    ;; Create proto-map with all fields in one constructor call (most efficient per Pronto docs)
    (p/proto-map serialize/cmd-mapper 
                 JonSharedCmd$Root
                 :protocol_version (:protocol_version cmd)
                 :client_type (:client_type cmd)
                 :session_id (:session_id cmd)
                 :important (:important cmd)
                 :from_cv_subsystem (:from_cv_subsystem cmd)
                 oneof-key oneof-val)))

(defn update-proto-map-cmd
  "Efficiently update fields in an existing proto-map command.
   Uses p-> with hints for optimal performance.
   
   Example:
   (update-proto-map-cmd my-proto-cmd {:session_id 999 :important true})" {:malli/schema [:=> [:cat :any [:map]] :any]}
  [proto-cmd updates]
  ;; Use p-> with hints for efficient updates
  (p/with-hints [(p/hint proto-cmd JonSharedCmd$Root serialize/cmd-mapper)]
    (reduce-kv (fn [cmd k v]
                 (p/p-> cmd (assoc k v)))
               proto-cmd
               updates)))

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(defn ensure-required-fields
  "Ensure a command has all required fields populated.
   Returns the command if valid, throws if missing required fields." {:malli/schema [:=> [:cat [:map]] :cmd/root]}
  [cmd]
  (let [required-fields [:protocol_version :client_type :session_id 
                         :important :from_cv_subsystem]
        missing-fields (remove #(contains? cmd %) required-fields)]
    (if (empty? missing-fields)
      cmd
      (throw (ex-info "Command missing required fields"
                      {:missing-fields missing-fields
                       :command cmd})))))