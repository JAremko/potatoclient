(ns test-oneof
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Testing oneof_edn ===\n")

;; Test basic validation
(let [schema [:oneof_edn
              [:field-a :string]
              [:field-b :int]
              [:field-c :boolean]]]
  
  (println "Test 1: Valid cases")
  (println "  {:field-a \"test\"} =>" (m/validate schema {:field-a "test"}))
  (println "  {:field-b 42} =>" (m/validate schema {:field-b 42}))
  (println "  {:field-c true} =>" (m/validate schema {:field-c true}))
  
  (println "\nTest 2: Invalid - multiple non-nil")
  (println "  {:field-a \"test\" :field-b 42} =>" 
           (m/validate schema {:field-a "test" :field-b 42}))
  
  (println "\nTest 3: Invalid - no non-nil fields")
  (println "  {} =>" (m/validate schema {}))
  
  (println "\nTest 4: Valid with nil fields (Pronto style)")
  (println "  {:field-a \"test\" :field-b nil :field-c nil} =>"
           (m/validate schema {:field-a "test" :field-b nil :field-c nil}))
  
  (println "\nTest 5: Invalid - extra keys")
  (println "  {:field-a \"test\" :extra 123} =>"
           (m/validate schema {:field-a "test" :extra 123}))
  
  (println "\nTest 6: Generator")
  (dotimes [i 5]
    (let [generated (mg/generate schema)]
      (println (str "  Generated " i ": " generated " valid? " 
                   (m/validate schema generated))))))