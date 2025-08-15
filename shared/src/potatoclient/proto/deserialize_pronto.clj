(ns potatoclient.proto.deserialize-pronto
  "Proto deserialization using Pronto - alternative implementation.
   This uses the same runtime eval approach as the generative tests."
  (:require
   [com.fulcrologic.guardrails.core :refer [>defn >defn- =>]]
   [clojure.spec.alpha :as s]
   [malli.core :as m]
   [malli.error :as me]
   [pronto.core :as pronto]
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

;; ============================================================================
;; Pronto mappers - initialized at runtime using eval
;; ============================================================================

(def ^:dynamic *cmd-mapper* nil)
(def ^:dynamic *state-mapper* nil)

(defn- ensure-mappers!
  "Initialize the pronto mappers at runtime if not already done."
  []
  (when (nil? *cmd-mapper*)
    (alter-var-root #'*cmd-mapper*
                    (constantly 
                     (eval '(do 
                             (pronto.core/defmapper cmd-mapper-internal 
                                                   [cmd.JonSharedCmd$Root])
                             cmd-mapper-internal)))))
  (when (nil? *state-mapper*)
    (alter-var-root #'*state-mapper*
                    (constantly
                     (eval '(do
                             (pronto.core/defmapper state-mapper-internal
                                                   [ser.JonSharedData$JonGUIState])
                             state-mapper-internal))))))

;; ============================================================================
;; Deserialization with Pronto
;; ============================================================================

(>defn deserialize-cmd-with-pronto
  "Deserialize CMD payload using Pronto (runtime eval approach)."
  [binary-data]
  [::bytes => ::edn-map]
  (ensure-mappers!)
  (try
    ;; Parse the proto message
    (let [proto-msg (cmd.JonSharedCmd$Root/parseFrom binary-data)
          ;; Use eval with backtick to convert to proto-map
          proto-map (eval `(pronto/proto->proto-map ~*cmd-mapper* ~proto-msg))]
      ;; Convert proto-map to EDN
      (eval `(pronto/proto-map->clj-map ~*cmd-mapper* ~proto-map)))
    (catch Exception e
      (throw (ex-info "Failed to deserialize CMD with Pronto"
                      {:error (.getMessage e)})))))

(>defn deserialize-state-with-pronto
  "Deserialize State payload using Pronto (runtime eval approach)."
  [binary-data]
  [::bytes => ::edn-map]
  (ensure-mappers!)
  (try
    ;; Parse the proto message
    (let [proto-msg (ser.JonSharedData$JonGUIState/parseFrom binary-data)
          ;; Use eval with backtick to convert to proto-map
          proto-map (eval `(pronto/proto->proto-map ~*state-mapper* ~proto-msg))]
      ;; Convert proto-map to EDN
      (eval `(pronto/proto-map->clj-map ~*state-mapper* ~proto-map)))
    (catch Exception e
      (throw (ex-info "Failed to deserialize State with Pronto"
                      {:error (.getMessage e)})))))