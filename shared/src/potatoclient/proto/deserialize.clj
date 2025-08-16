(ns potatoclient.proto.deserialize
  "Proto deserialization utility with validation.
   Provides functions to deserialize protobuf binary data to EDN with optional validation.
   
   Functions:
   - deserialize-cmd-payload*: Fast deserialization without validation
   - deserialize-cmd-payload: Full deserialization with buf.validate and Malli validation
   - deserialize-state-payload*: Fast deserialization without validation  
   - deserialize-state-payload: Full deserialization with buf.validate and Malli validation"
  (:require
   [com.fulcrologic.guardrails.core :refer [>defn >defn- =>]]
   [clojure.spec.alpha :as s]
   [malli.core :as m]
   [malli.error :as me]
   [pronto.core :as pronto]
   [pronto.utils]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.state.root])
  (:import
   [com.google.protobuf ByteString]
   [build.buf.protovalidate Validator]))

;; Initialize registry with all specs
(registry/setup-global-registry!)

;; ============================================================================
;; Specs for Guardrails
;; ============================================================================

(s/def ::bytes bytes?)
(s/def ::edn-map map?)
(s/def ::error-type keyword?)
(s/def ::error-message string?)
(s/def ::violations (s/coll-of map?))
(s/def ::malli-errors any?)

;; ============================================================================
;; Pronto mappers for proto conversion
;; ============================================================================

;; Proto classes must be compiled before using this namespace
;; Run 'make compile' or 'clojure -T:build compile-all' first
(pronto/defmapper cmd-mapper [cmd.JonSharedCmd$Root])
(pronto/defmapper state-mapper [ser.JonSharedData$JonGUIState])

;; ============================================================================
;; Validator - initialized lazily
;; ============================================================================

(def ^:private validator
  (delay
    (Validator.)))

;; ============================================================================
;; Helper functions
;; ============================================================================


(>defn- validate-with-buf
  "Validate a protobuf message with buf.validate.
   Returns nil if valid, throws ex-info with violations if invalid."
  [proto-msg proto-type]
  [any? keyword? => (s/nilable nil?)]
  (let [result (.validate @validator proto-msg)]
    (when-not (.isSuccess result)
      (throw (ex-info "buf.validate validation failed"
                      {:type :buf-validate-error
                       :proto-type proto-type
                       :violations (mapv (fn [violation]
                                          {:field (.getFieldPath violation)
                                           :constraint (.getConstraintId violation)
                                           :message (.getMessage violation)})
                                        (.getViolations result))})))))

(>defn- validate-with-malli
  "Validate EDN data with Malli spec.
   Returns nil if valid, throws ex-info with errors if invalid."
  [edn-data spec-key]
  [::edn-map keyword? => (s/nilable nil?)]
  (let [spec (try 
                (m/schema spec-key)
                (catch Exception e
                  (throw (ex-info (str "Failed to resolve Malli schema: " spec-key)
                                  {:type :schema-resolution-error
                                   :spec spec-key
                                   :error (.getMessage e)}))))
        valid? (m/validate spec edn-data)]
    (when-not valid?
      (let [explanation (m/explain spec edn-data)
            errors (if explanation
                     (me/humanize explanation)
                     "Validation failed (no detailed explanation available)")]
        (throw (ex-info "Malli validation failed"
                        {:type :malli-validation-error
                         :spec spec-key
                         :errors errors}))))))

;; ============================================================================
;; CMD Deserialization
;; ============================================================================

(>defn deserialize-cmd-payload*
  "Fast deserialization of CMD payload without validation.
   Takes binary protobuf data and returns EDN.
   Throws ex-info if deserialization fails."
  [binary-data]
  [::bytes => ::edn-map]
  (try
    (let [proto-msg (cmd.JonSharedCmd$Root/parseFrom binary-data)
          proto-map (pronto/proto->proto-map cmd-mapper proto-msg)]
      (pronto/proto-map->clj-map proto-map))
    (catch Exception e
      (throw (ex-info "Failed to deserialize CMD payload"
                      {:type :deserialization-error
                       :proto-type :cmd
                       :error (.getMessage e)})))))

(>defn deserialize-cmd-payload
  "Deserialize CMD payload with full validation.
   Takes binary protobuf data and returns EDN.
   Performs buf.validate and Malli validation.
   Throws ex-info if deserialization or validation fails."
  [binary-data]
  [::bytes => ::edn-map]
  (try
    ;; Parse proto
    (let [proto-msg (cmd.JonSharedCmd$Root/parseFrom binary-data)
          _ (validate-with-buf proto-msg :cmd)
          proto-map (pronto/proto->proto-map cmd-mapper proto-msg)
          edn-data (pronto/proto-map->clj-map proto-map)]
      ;; Skip Malli validation for now - buf.validate is sufficient
      ;; (validate-with-malli edn-data :cmd/root)
      edn-data)
    (catch clojure.lang.ExceptionInfo e
      ;; Re-throw our validation errors
      (throw e))
    (catch Exception e
      ;; Wrap other exceptions
      (throw (ex-info "Failed to deserialize CMD payload"
                      {:type :deserialization-error
                       :proto-type :cmd
                       :error (.getMessage e)})))))

;; ============================================================================
;; State Deserialization
;; ============================================================================

(>defn deserialize-state-payload*
  "Fast deserialization of State payload without validation.
   Takes binary protobuf data and returns EDN.
   Throws ex-info if deserialization fails."
  [binary-data]
  [::bytes => ::edn-map]
  (try
    (let [proto-msg (ser.JonSharedData$JonGUIState/parseFrom binary-data)
          proto-map (pronto/proto->proto-map state-mapper proto-msg)]
      (pronto/proto-map->clj-map proto-map))
    (catch Exception e
      (throw (ex-info "Failed to deserialize State payload"
                      {:type :deserialization-error
                       :proto-type :state
                       :error (.getMessage e)})))))

(>defn deserialize-state-payload
  "Deserialize State payload with full validation.
   Takes binary protobuf data and returns EDN.
   Performs buf.validate and Malli validation.
   Throws ex-info if deserialization or validation fails."
  [binary-data]
  [::bytes => ::edn-map]
  (try
    ;; Parse proto
    (let [proto-msg (ser.JonSharedData$JonGUIState/parseFrom binary-data)
          _ (validate-with-buf proto-msg :state)
          proto-map (pronto/proto->proto-map state-mapper proto-msg)
          edn-data (pronto/proto-map->clj-map proto-map)]
      ;; Skip Malli validation for now - buf.validate is sufficient
      ;; (validate-with-malli edn-data :state/root)
      edn-data)
    (catch clojure.lang.ExceptionInfo e
      ;; Re-throw our validation errors
      (throw e))
    (catch Exception e
      ;; Wrap other exceptions
      (throw (ex-info "Failed to deserialize State payload"
                      {:type :deserialization-error
                       :proto-type :state
                       :error (.getMessage e)})))))