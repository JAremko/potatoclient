(ns generator.registry
  "Global Malli registry setup for the proto-clj-generator.
  This ensures all schemas can reference each other and built-in schemas like :map."
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]))

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
                               {:oneof oneof/-oneof-schema})]
      (reset! registry-atom composite-registry)
      ;; Set as the default registry
      (mr/set-default-registry! composite-registry))))

(defn sanity-check-registry!
  "Run sanity checks to ensure the registry is properly configured."
  []
  (println "Running registry sanity checks...")
  ;; For now, just check that basic operations work
  (assert (m/schema? (m/schema [:map [:foo :string]]))
          "Cannot create basic map schema")
  (assert (m/validate [:map [:foo :string]] {:foo "bar"})
          "Cannot validate basic map")
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