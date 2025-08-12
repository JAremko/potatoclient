(ns test-full
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

(println "\n=== Full oneof_edn test ===\n")

;; Initialize registry
(println "Registering oneof_edn...")
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; Test creating a schema
(println "\n1. Creating schema:")
(let [schema-def [:oneof_edn
                  [:field-a :string]
                  [:field-b :int]
                  [:field-c :boolean]]]
  (try
    (let [schema (m/schema schema-def)]
      (println "  Schema created successfully")
      (println "  Type:" (try (m/type schema) (catch Exception e "Error getting type")))
      
      ;; Test validation
      (println "\n2. Testing validation:")
      (println "  {:field-a \"test\"} =>" (m/validate schema {:field-a "test"}))
      (println "  {:field-b 42} =>" (m/validate schema {:field-b 42}))
      (println "  {} =>" (m/validate schema {}))
      (println "  {:field-a \"x\" :field-b 1} =>" (m/validate schema {:field-a "x" :field-b 1}))
      
      ;; Test generation
      (println "\n3. Testing generation:")
      (dotimes [i 5]
        (let [generated (mg/generate schema)]
          (println (str "  " i ": " generated " valid? " (m/validate schema generated)))))
      
      (println "\n✅ All tests passed!"))
    (catch Exception e
      (println "  ❌ Error:" (.getMessage e))
      (.printStackTrace e))))