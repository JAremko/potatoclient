(ns test-reg
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

(println "\n=== Checking registration ===\n")

;; Initialize registry
(println "Before registration:")
(println "  Methods on -schema-generator:" (keys (methods mg/-schema-generator)))

(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\nAfter registration:")
(println "  Methods on -schema-generator:" (keys (methods mg/-schema-generator)))
(println "  Has :oneof_edn?" (contains? (methods mg/-schema-generator) :oneof_edn))

;; Check if we can find the schema type
(let [reg (m/default-schemas)]
  (println "\nDefault schemas has :oneof_edn?" (contains? reg :oneof_edn))
  (println "Type of :oneof_edn schema:" (type (get reg :oneof_edn))))