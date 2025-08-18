(ns potatoclient.cmd.builder
  "Efficient command building utilities using Pronto performance patterns.
   Provides functions to populate missing fields in proto-maps."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
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
   :session_id 42  ; Placeholder - server overrides this anyway
   :important false
   :from_cv_subsystem false})

;; ============================================================================
;; Efficient Proto-Map Building
;; ============================================================================

(>defn populate-cmd-fields
  "Efficiently populate missing required fields in a command proto-map.
   Uses Pronto's p-> macro with hints for optimal performance.
   
   Takes a partial command (with at least the oneof payload) and returns
   a complete command with all required fields populated.
   
   This function is designed to be used both in tests and production."
  [cmd]
  [:cmd/payload => :cmd/root]
  ;; First, create a proto-map with all the data at once (fastest approach per Pronto docs)
  ;; We merge defaults with the provided command, so any provided values override defaults
  (let [complete-cmd (merge default-protocol-fields cmd)]
    ;; Create proto-map with all fields in one go (most efficient)
    (if (p/proto-map? cmd)
      ;; If already a proto-map, use p-> with hints for efficient updates
      (p/p-> (p/hint cmd JonSharedCmd$Root serialize/cmd-mapper)
             ;; Only update fields that need updating
             (cond-> 
               (nil? (get cmd :protocol_version)) (assoc :protocol_version 1)
               (nil? (get cmd :client_type)) (assoc :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
               (nil? (get cmd :session_id)) (assoc :session_id 0)
               (nil? (get cmd :important)) (assoc :important false)
               (nil? (get cmd :from_cv_subsystem)) (assoc :from_cv_subsystem false)))
      ;; If it's a regular map, just return the merged map
      complete-cmd)))

(>defn populate-cmd-fields-with-overrides
  "Populate missing fields with specific override values.
   Useful for testing different configurations.
   
   Takes a command and a map of override values for the protocol fields."
  [cmd overrides]
  [:cmd/payload [:map] => :cmd/root]
  (let [fields-to-use (merge default-protocol-fields overrides)
        complete-cmd (merge fields-to-use cmd)]
    complete-cmd))

(>defn create-full-cmd
  "Create a full command from a payload with custom field values.
   Most efficient way to create a command when you know all values upfront.
   
   Example:
   (create-full-cmd {:ping {}} 
                    {:session_id 12345 :important true})"
  [payload-cmd field-overrides]
  [:cmd/payload [:map] => :cmd/root]
  (populate-cmd-fields-with-overrides payload-cmd field-overrides))

(>defn create-proto-map-cmd
  "Create a proto-map command efficiently.
   Uses Pronto's proto-map constructor with all fields at once (fastest).
   
   This is the most efficient way to create a proto-map command."
  [cmd]
  [:cmd/root => :any]
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

(>defn update-proto-map-cmd
  "Efficiently update fields in an existing proto-map command.
   Uses p-> with hints for optimal performance.
   
   Example:
   (update-proto-map-cmd my-proto-cmd {:session_id 999 :important true})"
  [proto-cmd updates]
  [:any [:map] => :any]
  ;; Use p-> with hints for efficient updates
  (p/with-hints [(p/hint proto-cmd JonSharedCmd$Root serialize/cmd-mapper)]
    (reduce-kv (fn [cmd k v]
                 (p/p-> cmd (assoc k v)))
               proto-cmd
               updates)))

;; ============================================================================
;; Batch Operations
;; ============================================================================

(>defn create-batch-commands
  "Efficiently create multiple commands with shared protocol fields.
   Useful for tests or batch operations.
   
   Example:
   (create-batch-commands [{:ping {}} {:noop {}} {:frozen {}}]
                          {:session_id 12345})"
  [payload-cmds field-overrides]
  [[:vector :cmd/payload] [:map] => [:vector :cmd/root]]
  (let [fields-to-use (merge default-protocol-fields field-overrides)]
    (mapv #(merge fields-to-use %) payload-cmds)))

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(>defn ensure-required-fields
  "Ensure a command has all required fields populated.
   Returns the command if valid, throws if missing required fields."
  [cmd]
  [[:map] => :cmd/root]
  (let [required-fields [:protocol_version :client_type :session_id 
                         :important :from_cv_subsystem]
        missing-fields (remove #(contains? cmd %) required-fields)]
    (if (empty? missing-fields)
      cmd
      (throw (ex-info "Command missing required fields"
                      {:missing-fields missing-fields
                       :command cmd})))))