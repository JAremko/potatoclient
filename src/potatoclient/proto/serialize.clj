(ns potatoclient.proto.serialize
  "Proto serialization utility with validation.
   Provides functions to serialize EDN data to protobuf binary with optional validation.
   
   Functions:
   - serialize-cmd-payload*: Fast serialization without validation
   - serialize-cmd-payload: Full serialization with Malli and buf.validate validation
   - serialize-state-payload*: Fast serialization without validation  
   - serialize-state-payload: Full serialization with Malli and buf.validate validation"
  (:require
    ;; Initialize Malli registry first - required for m/=> declarations
    [potatoclient.malli.init]
    [malli.core :as m]
    [malli.error :as me]
    [pronto.core :as pronto]
    [pronto.utils]
    [potatoclient.init :as init]
    [potatoclient.specs.cmd.root]
    [potatoclient.specs.state.root])
  (:import
    [com.google.protobuf ByteString]
    [build.buf.protovalidate Validator ValidatorFactory]))

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
    (.build (ValidatorFactory/newBuilder))))

;; ============================================================================
;; Helper functions
;; ============================================================================

(defn- validate-with-malli
  "Validate EDN data with Malli spec.
   Returns nil if valid, throws ex-info with errors if invalid."
  [edn-data spec-key]
  (init/ensure-registry!)
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
                         :errors errors
                         :raw-explanation explanation})))))) 
 (m/=> validate-with-malli [:=> [:cat [:map] :keyword] :any])

(defn- validate-with-buf
  "Validate a protobuf message with buf.validate.
   Returns nil if valid, throws ex-info with violations if invalid."
  [proto-msg proto-type]
  (let [result (.validate @validator proto-msg)]
    (when-not (.isSuccess result)
      (throw (ex-info "buf.validate validation failed"
                      {:type :buf-validate-error
                       :proto-type proto-type
                       :violations (mapv (fn [violation]
                                           (let [proto-violation (.toProto violation)]
                                             {:field (str (.getField proto-violation))
                                              :constraint (.getRuleId proto-violation)
                                              :message (.getMessage proto-violation)}))
                                         (.getViolations result))}))))) 
 (m/=> validate-with-buf [:=> [:cat :any :keyword] :any])

;; ============================================================================
;; CMD Serialization
;; ============================================================================

(defn serialize-cmd-payload*
  "Fast serialization of CMD payload without validation.
   Takes EDN data and returns binary protobuf data.
   Throws ex-info if serialization fails."
  [edn-data]
  (try
    (let [proto-map (pronto/clj-map->proto-map cmd-mapper
                                               cmd.JonSharedCmd$Root
                                               edn-data)
          proto-msg (pronto.utils/proto-map->proto proto-map)]
      (.toByteArray proto-msg))
    (catch Exception e
      (throw (ex-info "Failed to serialize CMD payload"
                      {:type :serialization-error
                       :proto-type :cmd
                       :error (.getMessage e)}))))) 
 (m/=> serialize-cmd-payload* [:=> [:cat :map] :bytes])

(defn serialize-cmd-payload
  "Serialize CMD payload with full validation.
   Takes EDN data and returns binary protobuf data.
   Performs Malli validation before serialization and buf.validate after.
   Throws ex-info if validation or serialization fails."
  [edn-data]
  (try
    ;; Validate EDN with Malli first
    (validate-with-malli edn-data :cmd/root)

    ;; Convert to proto
    (let [proto-map (pronto/clj-map->proto-map cmd-mapper
                                               cmd.JonSharedCmd$Root
                                               edn-data)
          proto-msg (pronto.utils/proto-map->proto proto-map)]

      ;; Validate proto with buf.validate
      (validate-with-buf proto-msg :cmd)

      ;; Return binary data
      (.toByteArray proto-msg))
    (catch clojure.lang.ExceptionInfo e
      ;; Re-throw our validation errors
      (throw e))
    (catch Exception e
      ;; Wrap other exceptions
      (throw (ex-info "Failed to serialize CMD payload"
                      {:type :serialization-error
                       :proto-type :cmd
                       :error (.getMessage e)}))))) 
 (m/=> serialize-cmd-payload [:=> [:cat :map] :bytes])

;; ============================================================================
;; State Serialization
;; ============================================================================

(defn serialize-state-payload*
  "Fast serialization of State payload without validation.
   Takes EDN data and returns binary protobuf data.
   Throws ex-info if serialization fails."
  [edn-data]
  (try
    (let [proto-map (pronto/clj-map->proto-map state-mapper
                                               ser.JonSharedData$JonGUIState
                                               edn-data)
          proto-msg (pronto.utils/proto-map->proto proto-map)]
      (.toByteArray proto-msg))
    (catch Exception e
      (throw (ex-info "Failed to serialize State payload"
                      {:type :serialization-error
                       :proto-type :state
                       :error (or (.getMessage e) (str e))
                       :cause e}))))) 
 (m/=> serialize-state-payload* [:=> [:cat :map] :bytes])

(defn serialize-state-payload
  "Serialize State payload with full validation.
   Takes EDN data and returns binary protobuf data.
   Performs Malli validation before serialization and buf.validate after.
   Throws ex-info if validation or serialization fails."
  [edn-data]
  (try
    ;; Validate EDN with Malli first
    (validate-with-malli edn-data :state/root)

    ;; Convert to proto
    (let [proto-map (pronto/clj-map->proto-map state-mapper
                                               ser.JonSharedData$JonGUIState
                                               edn-data)
          proto-msg (pronto.utils/proto-map->proto proto-map)]

      ;; Validate proto with buf.validate
      (validate-with-buf proto-msg :state)

      ;; Return binary data
      (.toByteArray proto-msg))
    (catch clojure.lang.ExceptionInfo e
      ;; Re-throw our validation errors
      (throw e))
    (catch Exception e
      ;; Wrap other exceptions
      (throw (ex-info "Failed to serialize State payload"
                      {:type :serialization-error
                       :proto-type :state
                       :error (or (.getMessage e) (str e))
                       :cause e}))))) 
 (m/=> serialize-state-payload [:=> [:cat :map] :bytes])