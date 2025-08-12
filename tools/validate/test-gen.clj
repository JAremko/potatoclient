(ns test-gen
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [clojure.test.check.generators :as gen]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Debugging generator ===\n")

(let [schema [:oneof_edn
              [:field-a :string]
              [:field-b :int]]
      parsed-schema (m/schema schema)]
  
  (println "Schema:" schema)
  (println "Parsed schema:" parsed-schema)
  (println "Children:" (m/children parsed-schema))
  (println "Type of first child:" (type (first (m/children parsed-schema))))
  
  ;; Try manual generation
  (println "\nManual generation:")
  (let [children (m/children parsed-schema)]
    (println "Children count:" (count children))
    (doseq [[idx [k v]] (map-indexed vector children)]
      (println (str "  Child " idx ": key=" k " schema=" v " type=" (type v)))))
  
  ;; Test if we can generate from child
  (let [[field-key field-schema] (first (m/children parsed-schema))]
    (println "\nGenerating from first child:")
    (println "  Key:" field-key)
    (println "  Schema:" field-schema)
    (try
      (println "  Generated:" (mg/generate field-schema))
      (catch Exception e
        (println "  Error:" (.getMessage e))))))