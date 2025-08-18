(ns potatoclient.cmd.validation
  "Validation utilities for command roundtrip testing.
   Uses proto templates to ensure consistent comparison."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [lambdaisland.deep-diff2 :as ddiff]
   [pronto.core :as p]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.proto.deserialize :as deserialize]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root]) ; Load the specs
  (:import
   [cmd JonSharedCmd$Root]))

;; Initialize registry to access specs
(registry/setup-global-registry!)

;; ============================================================================
;; Proto Template Creation
;; ============================================================================

(>defn create-cmd-template
  "Create a base proto-map template with all fields present.
   This gives us a map with all oneof fields set to nil/default values."
  []
  [=> [:map]]
  ;; Create an empty proto instance and convert to EDN
  ;; This will have all fields with their default values
  (let [empty-proto (cmd.JonSharedCmd$Root/getDefaultInstance)
        proto-map (p/proto->proto-map serialize/cmd-mapper empty-proto)]
    (p/proto-map->clj-map proto-map)))

(def ^:private cmd-template
  "Cached template for cmd root with all fields."
  (delay (create-cmd-template)))

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(>defn normalize-cmd
  "Normalize a command by merging it with the template.
   This ensures both original and roundtrip have the same keys.
   Also normalizes nested oneofs like system commands."
  [cmd]
  [:cmd/root => :cmd/root]
  (let [normalized (merge @cmd-template cmd)]
    ;; If system command is present, normalize its nested oneof too
    (if-let [system-cmd (:system normalized)]
      (let [;; System command oneofs
            system-oneofs #{:start_all :stop_all :reboot :power_off :localization
                           :reset_configs :start_rec :stop_rec :mark_rec_important
                           :unmark_rec_important :enter_transport 
                           :geodesic_mode_enable :geodesic_mode_disable}
            ;; Add nil values for all system oneofs
            system-template (zipmap system-oneofs (repeat nil))
            ;; Merge with actual system command
            normalized-system (merge system-template system-cmd)]
        (assoc normalized :system normalized-system))
      normalized)))

(>defn validate-roundtrip
  "Validate that a command survives serialization/deserialization.
   Returns true if valid, throws with detailed diff if not."
  [original-cmd]
  [:cmd/root => :boolean]
  (let [;; Serialize to binary
        binary (serialize/serialize-cmd-payload original-cmd)
        ;; Deserialize back
        roundtrip (deserialize/deserialize-cmd-payload binary)
        ;; Normalize both by merging with template
        normalized-original (normalize-cmd original-cmd)
        normalized-roundtrip (normalize-cmd roundtrip)]
    (if (= normalized-original normalized-roundtrip)
      true
      (let [diff (ddiff/diff normalized-original normalized-roundtrip)]
        (throw (ex-info "Roundtrip validation failed"
                        {:original normalized-original
                         :roundtrip normalized-roundtrip
                         :diff (ddiff/pretty-print diff)}))))))

(>defn validate-roundtrip-with-report
  "Validate roundtrip and return a detailed report.
   Returns {:valid? true} or {:valid? false :diff <diff>}."
  [original-cmd]
  [:cmd/root => [:map]]
  (let [;; Serialize to binary
        binary (serialize/serialize-cmd-payload original-cmd)
        ;; Deserialize back
        roundtrip (deserialize/deserialize-cmd-payload binary)
        ;; Normalize both by merging with template
        normalized-original (normalize-cmd original-cmd)
        normalized-roundtrip (normalize-cmd roundtrip)]
    (if (= normalized-original normalized-roundtrip)
      {:valid? true}
      {:valid? false
       :diff (ddiff/diff normalized-original normalized-roundtrip)
       :pretty-diff (with-out-str 
                      (ddiff/pretty-print 
                        (ddiff/diff normalized-original normalized-roundtrip)))})))

;; ============================================================================
;; Test Helpers
;; ============================================================================

(>defn roundtrip-test
  "Helper for tests - performs roundtrip and returns normalized result."
  [cmd-root]
  [:cmd/root => :cmd/root]
  (let [full-cmd (merge {:protocol_version 1
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :session_id 0
                         :important false
                         :from_cv_subsystem false}
                        cmd-root)
        ;; Serialize to binary
        binary (serialize/serialize-cmd-payload full-cmd)
        ;; Deserialize back
        roundtrip (deserialize/deserialize-cmd-payload binary)]
    ;; Return normalized roundtrip for comparison
    (normalize-cmd roundtrip)))

(>defn assert-roundtrip
  "Assert that a command survives roundtrip.
   Throws with pretty diff if validation fails."
  [cmd]
  [:cmd/root => :nil]
  (let [result (validate-roundtrip-with-report cmd)]
    (when-not (:valid? result)
      (throw (ex-info (str "Roundtrip validation failed:\n" (:pretty-diff result))
                      {:diff (:diff result)}))))
  nil)