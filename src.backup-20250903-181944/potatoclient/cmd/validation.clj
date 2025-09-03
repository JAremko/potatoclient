(ns potatoclient.cmd.validation
  "Validation utilities for command roundtrip testing.
   Uses proto templates to ensure consistent comparison."
  (:require
            [malli.core :as m]
    [lambdaisland.deep-diff2 :as ddiff]
    [pronto.core :as p]
    [potatoclient.proto.serialize :as serialize]
    [potatoclient.proto.deserialize :as deserialize]
    [potatoclient.init :as init]
    [potatoclient.specs.cmd.root]) ; Load the specs
  (:import
    [cmd JonSharedCmd$Root]))

;; Ensure registry is initialized
(init/ensure-registry!)

;; ============================================================================
;; Proto Template Creation
;; ============================================================================

(defn create-cmd-template
  "Create a base proto-map template with all fields present.
   This gives us a map with all oneof fields set to nil/default values."
  []
  ;; Create an empty proto instance and convert to EDN
  ;; This will have all fields with their default values
  (let [empty-proto (cmd.JonSharedCmd$Root/getDefaultInstance)
        proto-map (p/proto->proto-map serialize/cmd-mapper empty-proto)]
    (p/proto-map->clj-map proto-map))) 
 (m/=> create-cmd-template [:=> [:cat] [:map]])

(def ^:private cmd-template
  "Cached template for cmd root with all fields."
  (delay (create-cmd-template)))

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(defn- remove-nil-values
  "Recursively remove all keys with nil values from a map.
   Used for test comparison only - nil values are valid in oneofs."
  [m]
  (into {}
        (for [[k v] m
              :when (not (nil? v))]
          [k (if (map? v)
               (remove-nil-values v)
               v)]))) 
 (m/=> remove-nil-values [:=> [:cat :map] :map])

(defn remove-nil-oneof-fields
  "Remove nil oneof fields from a command for comparison purposes.
   Proto deserialization adds nil for all oneof fields, which is valid
   but makes comparison difficult. We remove nils for testing purposes only."
  [cmd]
  (remove-nil-values cmd)) 
 (m/=> remove-nil-oneof-fields [:=> [:cat :cmd/root] :map])

(defn validate-roundtrip
  "Validate that a command survives serialization/deserialization.
   Returns true if valid, throws with detailed diff if not."
  [original-cmd]
  (let [normalized-original (remove-nil-oneof-fields original-cmd)
        normalized-roundtrip (-> original-cmd
                                serialize/serialize-cmd-payload
                                deserialize/deserialize-cmd-payload
                                remove-nil-oneof-fields)]
    (if (= normalized-original normalized-roundtrip)
      true
      (let [diff (ddiff/diff normalized-original normalized-roundtrip)]
        (throw (ex-info "Roundtrip validation failed"
                        {:original normalized-original
                         :roundtrip normalized-roundtrip
                         :diff (ddiff/pretty-print diff)})))))) 
 (m/=> validate-roundtrip [:=> [:cat :cmd/root] :boolean])

(defn validate-roundtrip-with-report
  "Validate roundtrip and return a detailed report.
   Returns {:valid? true} or {:valid? false :diff <diff>}."
  [original-cmd]
  (let [normalized-original (remove-nil-oneof-fields original-cmd)
        normalized-roundtrip (-> original-cmd
                                serialize/serialize-cmd-payload
                                deserialize/deserialize-cmd-payload
                                remove-nil-oneof-fields)
        diff (ddiff/diff normalized-original normalized-roundtrip)]
    (if (= normalized-original normalized-roundtrip)
      {:valid? true}
      {:valid? false
       :diff diff
       :pretty-diff (with-out-str (ddiff/pretty-print diff))}))) 
 (m/=> validate-roundtrip-with-report [:=> [:cat :cmd/root] [:map]])

;; ============================================================================
;; Test Helpers
;; ============================================================================

(defn roundtrip-test
  "Helper for tests - performs roundtrip and returns normalized result."
  [cmd-root]
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
    ;; Return roundtrip with nil oneof fields removed for comparison
    (remove-nil-oneof-fields roundtrip))) 
 (m/=> roundtrip-test [:=> [:cat :cmd/root] :cmd/root])

(defn assert-roundtrip
  "Assert that a command survives roundtrip.
   Throws with pretty diff if validation fails."
  [cmd]
  (let [result (validate-roundtrip-with-report cmd)]
    (when-not (:valid? result)
      (throw (ex-info (str "Roundtrip validation failed:\n" (:pretty-diff result))
                      {:diff (:diff result)}))))
  nil) 
 (m/=> assert-roundtrip [:=> [:cat :cmd/root] :nil])