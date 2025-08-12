(ns test-cmd-gen
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load specs
   [potatoclient.specs.common]
   [potatoclient.specs.cmd.root]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "Testing cmd/root generation...")

(try
  (let [generated (mg/generate :cmd/root)]
    (println "Generated:" generated)
    (println "Valid?" (m/validate :cmd/root generated)))
  (catch Exception e
    (println "Error:" (.getMessage e))
    (.printStackTrace e)))