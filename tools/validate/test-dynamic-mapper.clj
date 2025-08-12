(ns test-dynamic-mapper
  (:require [pronto.core :as p]
            [pronto.schema :as schema]
            [pronto.utils :as u]
            [clojure.pprint :as pp])
  (:import [cmd JonSharedCmd$Root]))

(println "\n=== Testing Dynamic Class Discovery with Pronto ===")

;; Start with a minimal mapper - just the root class
(p/defmapper minimal-mapper [cmd.JonSharedCmd$Root])

;; Test data
(def test-cmd
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :rotary {:set_platform_azimuth {:value 45.0}}})

(println "\n1. Creating proto-map with minimal mapper:")
(try
  (let [proto-map (p/clj-map->proto-map minimal-mapper JonSharedCmd$Root test-cmd)]
    (println "SUCCESS: Created proto-map!")
    
    ;; Get the schema to see what classes Pronto discovered
    (println "\n2. Exploring schema of the created proto-map:")
    (let [root-schema (schema/schema proto-map)]
      (println "Root schema keys:" (keys root-schema))
      (println "\nField types:")
      (doseq [[field-name field-type] root-schema]
        (println (format "  %s -> %s" field-name 
                        (if (class? field-type)
                          (.getName field-type)
                          field-type))))
      
      ;; Check if we can get nested message schemas
      (when-let [rotary-field (:rotary proto-map)]
        (println "\n3. Rotary submessage schema:")
        (let [rotary-schema (schema/schema rotary-field)]
          (println "Rotary schema keys:" (keys rotary-schema))
          (doseq [[field-name field-type] rotary-schema]
            (println (format "  %s -> %s" field-name
                            (if (class? field-type)
                              (.getName field-type)
                              field-type))))))
      
      ;; Try to get the actual Java proto instance
      (println "\n4. Getting actual proto classes:")
      (let [proto-instance (p/proto-map->proto proto-map)]
        (println "Root proto class:" (.getName (class proto-instance)))
        (when (.hasRotary proto-instance)
          (let [rotary-instance (.getRotary proto-instance)]
            (println "Rotary proto class:" (.getName (class rotary-instance)))
            (when (.hasSetPlatformAzimuth rotary-instance)
              (let [cmd-instance (.getSetPlatformAzimuth rotary-instance)]
                (println "SetPlatformAzimuth class:" (.getName (class cmd-instance)))))))))
    
    ;; Now test if we can dynamically build a mapper from discovered classes
    (println "\n5. Building dynamic mapper from discovered classes:")
    
    ;; Function to collect all message classes from a proto
    (defn collect-proto-classes [proto-instance]
      (let [classes (atom #{(class proto-instance)})]
        ;; This would need reflection to walk all fields
        ;; For now, just showing the concept
        @classes))
    
    (let [proto-instance (p/proto-map->proto proto-map)
          discovered-classes (collect-proto-classes proto-instance)]
      (println "Discovered classes:" discovered-classes)))
  
  (catch Exception e
    (println "ERROR:" (.getMessage e))
    (.printStackTrace e)))

(println "\n=== Testing with different command types ===")

;; Test with GPS command
(def gps-cmd
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :gps {:set_manual_position {:latitude 45.0 :longitude -122.0}}})

(println "\n6. Testing GPS command:")
(try
  (let [proto-map (p/clj-map->proto-map minimal-mapper JonSharedCmd$Root gps-cmd)
        proto-instance (p/proto-map->proto proto-map)]
    (println "Created GPS command successfully!")
    (when (.hasGps proto-instance)
      (let [gps-instance (.getGps proto-instance)]
        (println "GPS proto class:" (.getName (class gps-instance)))
        (when (.hasSetManualPosition gps-instance)
          (let [pos-instance (.getSetManualPosition gps-instance)]
            (println "SetManualPosition class:" (.getName (class pos-instance))))))))
  (catch Exception e
    (println "ERROR:" (.getMessage e))))

(println "\n=== Key Insights ===")
(println "1. Pronto can work with just the root class in the mapper")
(println "2. It discovers nested message classes from the protobuf definition")
(println "3. The extensive mapper in test_harness.clj is likely for:")
(println "   - Performance optimization (pre-loading all classes)")
(println "   - Avoiding reflection costs at runtime")
(println "   - Ensuring all classes are available in the classloader")
(println "\n4. For dynamic/exploratory work, minimal mapper is fine")
(println "5. For production/tests with many iterations, pre-loaded mapper is better")