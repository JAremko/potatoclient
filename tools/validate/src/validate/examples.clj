(ns validate.examples
  "Example usage of Malli specs for State and Command messages"
  (:require
   [malli.core :as m]
   [malli.error :as me]
   [malli.generator :as mg]
   [malli.util :as mu]
   [potatoclient.malli.registry :as registry]
   [validate.specs.oneof-edn :as oneof-edn]
   [potatoclient.specs.common]
   [validate.specs.state.root]
   [validate.specs.cmd.root]
   [clojure.pprint :as pp]))

(defn init!
  "Initialize the global Malli registry"
  []
  (registry/setup-global-registry!
    (oneof-edn/register-oneof-edn-schema!)))

(defn validate-state
  "Validate a State message (EDN format) against specs"
  [state-data]
  (if (m/validate :state/root state-data)
    {:valid? true
     :message "State message is valid"}
    {:valid? false
     :errors (me/humanize (m/explain :state/root state-data))}))

(defn generate-valid-state
  "Generate a random valid State message"
  []
  (mg/generate :state/root {:seed 42 :size 10}))

(defn generate-valid-gps
  "Generate a random valid GPS message with buf.validate constraints"
  []
  (mg/generate :state/gps))

(defn validate-command
  "Validate a Command message against specs"
  [cmd-data]
  (if (m/validate :cmd/root cmd-data)
    {:valid? true
     :message "Command message is valid"}
    {:valid? false
     :errors (me/humanize (m/explain :cmd/root cmd-data))}))

(defn generate-simple-command
  "Generate a simple ping command"
  []
  {:protocol-version 1
   :client-type :jon-gui-data-client-type-internal-cv
   :cmd {:ping {}}})

(defn validate-with-details
  "Validate and show detailed path to errors"
  [spec data]
  (if-let [explanation (m/explain spec data)]
    (do
      (println "Validation failed!")
      (println "Errors:")
      (doseq [error (:errors explanation)]
        (println "  Path:" (:path error))
        (println "  Value:" (:value error))
        (println "  Error:" (-> error :schema second)))
      false)
    (do
      (println "Validation passed!")
      true)))

(defn check-buf-validate-constraints
  "Demonstrate that generated values satisfy buf.validate constraints"
  []
  (println "Checking buf.validate constraints with 100 generated GPS messages...")
  (let [results (for [i (range 100)]
                  (let [gps (mg/generate :state/gps)]
                    {:valid? (and (<= -90 (:latitude gps) 90)
                                  (<= -180 (:longitude gps) 180)
                                  (<= -433 (:altitude gps) 8848.86))
                     :gps gps}))]
    (if (every? :valid? results)
      (println "✓ All 100 generated GPS messages satisfy buf.validate constraints!")
      (println "✗ Some generated values violate constraints"))))

(defn closed-map-example
  "Demonstrate that closed maps catch typos and extra fields"
  []
  (init!)
  (println "\n=== Closed Map Validation Example ===\n")
  
  ;; Valid GPS
  (let [valid-gps {:altitude 100.0
                   :fix-type :jon-gui-data-gps-fix-type-3d
                   :latitude 50.0
                   :longitude 15.0
                   :manual-latitude 50.0
                   :manual-longitude 15.0}]
    (println "Valid GPS:")
    (pp/pprint valid-gps)
    (println "Result:" (if (m/validate :state/gps valid-gps) "✓ VALID" "✗ INVALID"))
    (println))
  
  ;; GPS with typo (latitude misspelled)
  (let [typo-gps {:altitude 100.0
                  :fix-type :jon-gui-data-gps-fix-type-3d
                  :latitute 50.0  ; <-- typo!
                  :longitude 15.0
                  :manual-latitude 50.0
                  :manual-longitude 15.0}]
    (println "GPS with typo (latitute instead of latitude):")
    (pp/pprint typo-gps)
    (println "Result:" (if (m/validate :state/gps typo-gps) "✓ VALID" "✗ INVALID"))
    (when-let [errors (m/explain :state/gps typo-gps)]
      (println "Errors:" (me/humanize errors)))
    (println))
  
  ;; GPS with extra field
  (let [extra-gps {:altitude 100.0
                   :fix-type :jon-gui-data-gps-fix-type-3d
                   :latitude 50.0
                   :longitude 15.0
                   :manual-latitude 50.0
                   :manual-longitude 15.0
                   :extra-field "should not be here"}]  ; <-- extra field!
    (println "GPS with extra field:")
    (pp/pprint extra-gps)
    (println "Result:" (if (m/validate :state/gps extra-gps) "✓ VALID" "✗ INVALID"))
    (when-let [errors (m/explain :state/gps extra-gps)]
      (println "Errors:" (me/humanize errors)))))

(defn run-examples
  "Run all example demonstrations"
  []
  (init!)
  
  (println "\n=== Malli Spec Validation Examples ===\n")
  
  ;; 1. Generate valid GPS
  (println "1. Generated valid GPS message:")
  (pp/pprint (generate-valid-gps))
  (println)
  
  ;; 2. Validate a simple command
  (println "2. Validating a simple ping command:")
  (let [cmd (generate-simple-command)]
    (pp/pprint cmd)
    (println "Result:" (validate-command cmd)))
  (println)
  
  ;; 3. Check buf.validate constraints
  (check-buf-validate-constraints)
  (println)
  
  ;; 4. Demonstrate closed maps
  (closed-map-example))

(comment
  ;; Run this in REPL:
  (run-examples)
  
  ;; Or individual functions:
  (init!)
  (generate-valid-gps)
  (closed-map-example)
  )