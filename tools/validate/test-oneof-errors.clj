#!/usr/bin/env clojure

(ns test-oneof-errors
  "Test error handling for oneof_edn"
  (:require
   [malli.core :as m]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Testing oneof_edn Error Handling ===\n")

(let [schema [:oneof_edn [:a :string] [:b :int]]]
  
  (println "1. Test basic explain:")
  (let [result (m/explain schema {})]
    (println "  Empty map explain:" result))
  
  (println "\n2. Test explain with multiple fields:")
  (let [result (m/explain schema {:a "x" :b 1})]
    (println "  Multiple fields explain:" result))
  
  (println "\n3. Test explain with extra keys:")
  (let [result (m/explain schema {:a "x" :c 1})]
    (println "  Extra keys explain:" result))
  
  (println "\n4. Test humanize (may fail):")
  (try
    (let [result (me/humanize (m/explain schema {}))]
      (println "  Humanized empty:" result))
    (catch Exception e
      (println "  Error humanizing:" (.getMessage e))
      (println "  Stack:" (take 5 (.getStackTrace e)))))
  
  (println "\n5. Raw errors without humanize:")
  (let [errors (:errors (m/explain schema {:a "x" :b 1}))]
    (println "  Raw errors:" errors))
  
  (println "\n6. Test with nested schema:")
  (let [nested [:oneof_edn
               [:simple :string]
               [:complex [:map [:x :int]]]]]
    (println "  Nested explain:" (m/explain nested {:complex {:x "wrong"}}))
    (try
      (println "  Nested humanize:" (me/humanize (m/explain nested {:complex {:x "wrong"}})))
      (catch Exception e
        (println "  Error:" (.getMessage e))))))

(System/exit 0)