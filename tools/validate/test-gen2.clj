(ns test-gen2
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Testing generator directly ===\n")

(let [schema [:oneof_edn
              [:field-a :string]
              [:field-b :int]]]
  
  (println "Attempting to generate...")
  (try
    (let [generated (mg/generate schema)]
      (println "Generated:" generated)
      (println "Valid?" (m/validate schema generated)))
    (catch Exception e
      (println "Error:" (.getMessage e))
      (.printStackTrace e))))