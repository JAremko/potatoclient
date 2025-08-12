(ns validate.test-minimal-harness-test
  "Test suite to verify the minimal harness works correctly"
  (:require [clojure.test :refer [deftest testing is run-tests]]
            [validate.test-harness :as h]
            [pronto.core :as p]
            [pronto.schema :as schema])
  (:import [cmd JonSharedCmd$Root]
           [ser JonSharedData$JonGUIState]))

(deftest test-minimal-mappers
  (testing "Minimal mappers can create proto-maps"
    
    (testing "Command mapper with just root class"
      (let [ping-cmd {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :ping {}}]
        (is (some? (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root ping-cmd))
            "Should create ping command with minimal mapper")))
    
    (testing "Complex nested command"
      (let [rotary-cmd {:protocol_version 1
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :rotary {:set_platform_azimuth {:value 45.0}}}]
        (let [proto-map (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root rotary-cmd)]
          (is (some? proto-map) "Should create rotary command")
          (is (some? (:rotary proto-map)) "Should have rotary field")
          
          ;; Verify Pronto discovered the nested classes
          (let [proto-instance (p/proto-map->proto proto-map)]
            (is (.hasRotary proto-instance) "Proto should have rotary")
            (when (.hasRotary proto-instance)
              (let [rotary-instance (.getRotary proto-instance)]
                (is (.hasSetPlatformAzimuth rotary-instance) "Rotary should have set_platform_azimuth")))))))
    
    (testing "Different command types"
      (let [test-commands [{:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :gps {:set_manual_position {:latitude 45.0 :longitude -122.0}}}
                          
                          {:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :compass {:set_magnetic_declination {:value 10.5}}}
                          
                          {:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :lrf {:measure {}}}
                          
                          {:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :system {:start_all {}}}
                          
                          {:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :lira {:refine_target {:target {:target_latitude 45.0
                                                          :target_longitude -122.0
                                                          :target_altitude 100.0
                                                          :target_azimuth 180.0
                                                          :target_elevation 0.0
                                                          :distance 1000.0
                                                          :timestamp 1754664759
                                                          :uuid_part1 123
                                                          :uuid_part2 456
                                                          :uuid_part3 789
                                                          :uuid_part4 101}}}}]]
        
        (doseq [cmd test-commands]
          (let [proto-map (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd)]
            (is (some? proto-map) (str "Should create command: " (keys cmd)))))))))

(deftest test-state-mapper
  (testing "State mapper with just root class"
    (let [state-data {:protocol_version 1
                     :gps {:latitude 45.0
                          :longitude -122.0
                          :altitude 100.0
                          :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                          :manual_latitude 0.0
                          :manual_longitude 0.0}
                     :system {:cpu_load 42.0
                             :cpu_temperature 55.0
                             :cur_video_rec_dir_day 15
                             :cur_video_rec_dir_hour 14
                             :cur_video_rec_dir_minute 30
                             :cur_video_rec_dir_month 8
                             :cur_video_rec_dir_second 45
                             :cur_video_rec_dir_year 2025
                             :disk_space 75
                             :gpu_load 30.0
                             :gpu_temperature 60.0
                             :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
                             :low_disk_space false
                             :power_consumption 150.0
                             :rec_enabled true}}]
      (let [proto-map (p/clj-map->proto-map h/state-mapper JonSharedData$JonGUIState state-data)]
        (is (some? proto-map) "Should create state with minimal mapper")
        (is (some? (:gps proto-map)) "Should have GPS field")
        (is (some? (:system proto-map)) "Should have system field")))))

(deftest test-class-discovery
  (testing "Can discover classes from proto-map"
    (let [cmd {:protocol_version 1
              :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
              :rotary {:set_platform_azimuth {:value 45.0}}}
          proto-map (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd)
          discovered-classes (h/discover-message-classes proto-map)]
      
      (is (contains? discovered-classes JonSharedCmd$Root)
          "Should discover root class")
      (is (> (count discovered-classes) 1)
          "Should discover nested classes"))))

(deftest test-schema-introspection
  (testing "Schema reveals all message types"
    (let [cmd {:protocol_version 1
              :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
              :gps {:set_manual_position {:latitude 45.0 :longitude -122.0}}}
          proto-map (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd)
          root-schema (schema/schema proto-map)]
      
      (is (contains? root-schema :payload/gps)
          "Schema should show GPS field")
      (is (class? (get root-schema :payload/gps))
          "GPS field should be a class")
      
      ;; Check nested message schema
      (when-let [gps-field (:gps proto-map)]
        (let [gps-schema (schema/schema gps-field)]
          (is (map? gps-schema) "GPS should have a schema")
          (is (contains? gps-schema :cmd/set_manual_position)
              "GPS schema should show set_manual_position command"))))))

(deftest test-round-trip
  (testing "Round-trip with minimal mapper"
    (let [cmd {:protocol_version 1
              :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
              :ping {}}
          proto-map (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd)
          binary (p/proto-map->bytes proto-map)
          proto-map-2 (p/bytes->proto-map h/cmd-mapper JonSharedCmd$Root binary)
          edn-2 (p/proto-map->clj-map proto-map-2)]
      
      (is (= (:protocol_version edn-2) 1) "Protocol version should round-trip")
      (is (= (:client_type edn-2) :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
          "Client type should round-trip")
      (is (some? (:ping edn-2)) "Ping field should round-trip"))))

(deftest test-performance-comparison
  (testing "Minimal mapper performance is acceptable"
    (let [cmd {:protocol_version 1
              :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
              :rotary {:set_platform_azimuth {:value 45.0}}}
          iterations 100]
      
      (println "\nMinimal mapper performance test:")
      (let [start (System/currentTimeMillis)]
        (dotimes [_ iterations]
          (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd))
        (let [elapsed (- (System/currentTimeMillis) start)
              per-iter (/ elapsed (double iterations))]
          (println (format "  %d iterations in %dms (%.2fms per iteration)" 
                          iterations elapsed per-iter))
          (is (< per-iter 10) "Should be less than 10ms per iteration"))))))

;; Run the tests
(println "\n=== Testing Minimal Harness ===")
(run-tests)