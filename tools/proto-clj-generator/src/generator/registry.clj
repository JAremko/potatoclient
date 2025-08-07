(ns generator.registry
  "Global Malli registry setup for the proto-clj-generator.
  This ensures all schemas can reference each other and built-in schemas like :map."
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [com.fulcrologic.guardrails.malli.registry :as gr.reg]
            [potatoclient.specs.malli-oneof :as oneof]
            [potatoclient.proto.conversion :as conv]))

(defonce registry-atom (atom nil))

(defn setup-global-registry!
  "Set up the global default registry with all schemas needed for the generator."
  []
  ;; Only set it once
  (when-not @registry-atom
    (let [composite-registry (mr/composite-registry
                               ;; Built-in Malli schemas (:map, :vector, :string, etc.)
                               (m/default-schemas)
                               ;; Custom :oneof schema from shared specs
                               {:oneof oneof/-oneof-schema}
                               ;; String conversion specs with generators
                               conv/conversion-schemas)]
      (reset! registry-atom composite-registry)
      ;; Set as the default Malli registry
      (mr/set-default-registry! composite-registry)
      ;; Also merge into guardrails registry
      (gr.reg/merge-schemas! {:oneof oneof/-oneof-schema}
                             conv/conversion-schemas)))))

(defn sanity-check-registry!
  "Run sanity checks to ensure the registry is properly configured."
  []
  (println "Running registry sanity checks...")
  ;; For now, just check that basic operations work
  (assert (m/schema? (m/schema [:map [:foo :string]]))
          "Cannot create basic map schema")
  (assert (m/validate [:map [:foo :string]] {:foo "bar"})
          "Cannot validate basic map")
  ;; Check string conversion specs are available
  (assert (m/schema? (m/schema :potatoclient.proto.string-conversion-specs/KebabCaseString))
          "Cannot resolve KebabCaseString schema")
  (assert (m/validate :potatoclient.proto.string-conversion-specs/KebabCaseString "kebab-case")
          "Cannot validate kebab-case string")
  (println "Registry sanity checks passed!")
  true)

;; Initialize on namespace load
(setup-global-registry!)

;; Run sanity checks after initialization
(try
  (sanity-check-registry!)
  (catch Exception e
    (println "Registry sanity check failed:" (.getMessage e))
    (throw e)))