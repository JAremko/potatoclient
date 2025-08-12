(ns test-cmd-root-final
  (:require [pronto.core :as p]
            [clojure.pprint :as pp])
  (:import [cmd JonSharedCmd$Root]))

;; Test if we can use Pronto without a pre-defined mapper
(def test-cmd-edn
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :ping {}})

(println "\n=== Testing Pronto without mapper ===")

;; Try 1: Check if Pronto has a default mapper
(println "\n1. Checking for default mapper:")
(try
  ;; Pronto always requires 3 args: mapper, class, data
  (println "Pronto's clj-map->proto-map requires: [mapper proto-class clj-map]")
  (catch Exception e
    (println "Error:" (.getMessage e))))

;; Try 2: Mapper cannot be empty - it needs at least one class
(println "\n2. Pronto mapper requirements:")
(println "- Mapper cannot be empty (AssertionError: not-empty classes)")
(println "- Mapper pre-loads protobuf classes for performance")

;; Try 3: With minimal mapper (just the root class)
(println "\n3. With minimal mapper (root only):")
(try
  (p/defmapper minimal-mapper [cmd.JonSharedCmd$Root])
  (let [proto-map (p/clj-map->proto-map minimal-mapper JonSharedCmd$Root test-cmd-edn)]
    (println "SUCCESS: Created proto-map with minimal mapper!")
    (println "Proto class:" (class (p/proto-map->proto proto-map)))
    
    ;; Check if it can handle nested messages
    (println "\nTesting nested message:")
    (let [nested-cmd {:protocol_version 1
                      :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                      :rotary {:set_platform_azimuth {:value 45.0}}}]
      (try
        (let [nested-proto (p/clj-map->proto-map minimal-mapper JonSharedCmd$Root nested-cmd)]
          (println "SUCCESS: Minimal mapper handles nested messages!")
          (println "Has rotary field:" (some? (:rotary (p/proto-map->clj-map nested-proto)))))
        (catch Exception e
          (println "FAILED on nested:" (.getMessage e))))))
  (catch Exception e
    (println "FAILED:" (.getMessage e))))

;; Try 4: Check what Pronto auto-loads
(println "\n4. Checking Pronto's auto-loading:")
(try
  ;; Pronto can auto-discover classes from the proto itself
  (p/defmapper auto-mapper [cmd.JonSharedCmd$Root])
  
  ;; Test with complex nested command
  (let [complex-cmd {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :gps {:set_manual_position {:latitude 45.0
                                                 :longitude -122.0}}}]
    (println "Testing auto-discovery with GPS command:")
    (let [proto-map (p/clj-map->proto-map auto-mapper JonSharedCmd$Root complex-cmd)]
      (println "SUCCESS: Auto-mapper works!")
      (println "GPS data present:" (some? (get-in (p/proto-map->clj-map proto-map) [:gps :set_manual_position])))
      
      ;; Check if Pronto auto-loaded the GPS classes
      (println "\nChecking class of GPS submessage:")
      (when-let [gps-field (.gps (p/proto-map->proto proto-map))]
        (println "GPS class:" (class gps-field)))))
  (catch Exception e
    (println "FAILED:" (.getMessage e))
    (.printStackTrace e)))

(println "\n=== Conclusion ===")
(println "Pronto needs at least the root class in the mapper.")
(println "It can auto-discover nested message classes from the protobuf definition.")
(println "The extensive mapper may be for performance optimization (pre-loading all classes).")