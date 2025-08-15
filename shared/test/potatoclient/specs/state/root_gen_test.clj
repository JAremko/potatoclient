(ns potatoclient.specs.state.root-gen-test
  "Generative tests for state.Root spec.
   Tests that generated samples are valid according to Malli specs."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]))

;; Initialize registry with all specs
(registry/setup-global-registry!)

(deftest generate-valid-state-samples
  (testing "Can generate 5000 valid state samples from Malli specs"
    (let [state-spec (m/schema :state/root)
          samples (doall (mg/sample state-spec {:size 5000}))
          validation-results (mapv #(m/validate state-spec %) samples)
          valid-count (count (filter true? validation-results))
          invalid-samples (keep-indexed 
                         (fn [idx valid?] 
                           (when-not valid? idx))
                         validation-results)]
      
      (is (= 5000 (count samples)) "Should generate 5000 samples")
      
      (is (= 5000 valid-count)
          (format "All 5000 samples should be valid. Invalid samples at indices: %s"
                 (take 10 invalid-samples)))
      
      ;; If there are invalid samples, show why the first one failed
      (when (seq invalid-samples)
        (let [first-invalid-idx (first invalid-samples)
              invalid-sample (nth samples first-invalid-idx)
              explanation (m/explain state-spec invalid-sample)]
          (println "\nFirst invalid sample explanation:")
          (println (me/humanize explanation)))))))

(deftest state-sample-structure-validation
  (testing "Generated state samples have correct structure"
    (let [state-spec (m/schema :state/root)
          samples (mg/sample state-spec {:size 100})]  ; Smaller sample for structure checking
      
      (doseq [sample samples]
        (testing "Sample has required top-level structure"
          (is (map? sample) "Sample should be a map")
          (is (contains? sample :protocol_version) "Should have protocol_version")
          (is (> (:protocol_version sample) 0) "protocol_version should be > 0"))
        
        (testing "All required subsystems are present"
          (is (contains? sample :system) "Should have system")
          (is (contains? sample :meteo_internal) "Should have meteo_internal")
          (is (contains? sample :lrf) "Should have lrf")
          (is (contains? sample :time) "Should have time")
          (is (contains? sample :gps) "Should have gps")
          (is (contains? sample :compass) "Should have compass")
          (is (contains? sample :rotary) "Should have rotary")
          (is (contains? sample :camera_day) "Should have camera_day")
          (is (contains? sample :camera_heat) "Should have camera_heat")
          (is (contains? sample :compass_calibration) "Should have compass_calibration")
          (is (contains? sample :rec_osd) "Should have rec_osd")
          (is (contains? sample :day_cam_glass_heater) "Should have day_cam_glass_heater")
          (is (contains? sample :actual_space_time) "Should have actual_space_time"))
        
        (testing "Nested structures are valid"
          (when (:gps sample)
            (is (every? #(contains? (:gps sample) %)
                       [:latitude :longitude :altitude :manual_latitude 
                        :manual_longitude :manual_altitude :fix_type :use_manual])
                "GPS should have all required fields")
            (is (<= -90 (get-in sample [:gps :latitude]) 90)
                "GPS latitude should be in valid range"))
          
          (when (:compass sample)
            (let [azimuth (get-in sample [:compass :azimuth])]
              (is (and (>= azimuth 0) (< azimuth 360))
                  "Compass azimuth should be in [0, 360)")))
          
          (when (:lrf sample)
            (is (map? (get-in sample [:lrf :target]))
                "LRF should have nested target map")
            (when-let [target-color (get-in sample [:lrf :target :target_color])]
              (is (and (<= 0 (:red target-color) 255)
                      (<= 0 (:green target-color) 255)
                      (<= 0 (:blue target-color) 255))
                  "LRF target color RGB values should be in [0, 255]")))
          
          (when (:rotary sample)
            (is (map? (get-in sample [:rotary :current_scan_node]))
                "Rotary should have nested current_scan_node map")))))))

(deftest generate-samples-performance
  (testing "Sample generation performance"
    (let [state-spec (m/schema :state/root)
          start-time (System/currentTimeMillis)
          samples (doall (mg/sample state-spec {:size 5000}))
          end-time (System/currentTimeMillis)
          duration (- end-time start-time)]
      
      (is (= 5000 (count samples))
          "Should generate 5000 samples")
      
      (println (format "\nGenerated 5000 samples in %d ms (%.1f samples/sec)"
                      duration
                      (/ 5000.0 (/ duration 1000.0))))
      
      (is (< duration 60000)
          "Should generate 5000 samples in less than 60 seconds"))))