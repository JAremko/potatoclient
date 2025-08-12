(ns debug-gen
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.registry :as mr]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

(println "\n=== Debugging generator lookup ===\n")

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; Try to look up the IntoSchema
(println "Looking up :oneof_edn in registry:")
(let [current-registry (mr/registry)]
  (println "  Registry type:" (type current-registry))
  (println "  Has :oneof_edn?" (contains? current-registry :oneof_edn))
  (println "  :oneof_edn value:" (get current-registry :oneof_edn))
  (println "  Type of value:" (type (get current-registry :oneof_edn))))

;; Try to create a schema and then generate
(println "\nCreating schema and calling generator directly:")
(let [schema (m/schema [:oneof_edn [:a :string] [:b :int]])]
  (println "  Schema created")
  (println "  Schema type:" (type schema))
  
  ;; Try to call our generator directly
  (println "\nCalling our generator method directly:")
  (let [gen-fn (get (methods mg/-schema-generator) :oneof_edn)]
    (if gen-fn
      (do
        (println "  Found generator method")
        (try
          (let [gen (gen-fn schema {})]
            (println "  Generator created:" (type gen))
            (let [value (mg/generate gen)]
              (println "  Generated value:" value)))
          (catch Exception e
            (println "  Error calling generator:" (.getMessage e)))))
      (println "  No generator method found!"))))