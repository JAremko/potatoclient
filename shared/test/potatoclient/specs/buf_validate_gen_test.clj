(ns potatoclient.specs.buf-validate-gen-test
  "Simplified test to verify that generated Malli samples are valid.
   This test generates samples and checks basic validity without
   requiring proto compilation."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.state.root]))

;; Initialize registry with all specs
(registry/setup-global-registry!)

(deftest generate-valid-state-samples
  (testing "Can generate valid state samples from Malli specs"
    (let [state-spec (m/schema :state/root)
          ;; Generate 100 samples
          samples (mg/sample state-spec {:size 100})]
      
      ;; Basic checks
      (is (= 100 (count samples)) "Should generate 100 samples")
      
      ;; Validate each sample against the spec
      (doseq [[idx sample] (map-indexed vector samples)]
        (is (m/validate state-spec sample)
            (format "Sample %d should be valid according to Malli spec" idx)))
      
      ;; Check structure of samples
      (doseq [sample samples]
        (testing "Sample structure"
          (is (map? sample) "Sample should be a map")
          (is (contains? sample :protocol_version) "Should have protocol_version")
          
          ;; Check some key subsystems if present
          (when (:gps sample)
            (is (number? (get-in sample [:gps :latitude]))
                "GPS latitude should be a number")
            (is (<= -90 (get-in sample [:gps :latitude]) 90)
                "GPS latitude should be in valid range"))
          
          (when (:compass sample)
            (let [azimuth (get-in sample [:compass :azimuth])]
              (when azimuth
                (is (number? azimuth) "Compass azimuth should be a number")
                (is (and (>= azimuth 0) (< azimuth 360))
                    "Compass azimuth should be in [0, 360)")))))))))

(deftest validate-specific-constraints
  (testing "Malli specs enforce buf.validate-compatible constraints"
    
    (testing "GPS constraints"
      (let [gps-spec (m/schema [:map {:closed true}
                                [:latitude :position/latitude]
                                [:longitude :position/longitude]
                                [:altitude :position/altitude]])]
        
        ;; Valid GPS data
        (is (m/validate gps-spec {:latitude 45.0
                                  :longitude -122.0
                                  :altitude 100.0})
            "Valid GPS data should pass")
        
        ;; Invalid latitude (> 90)
        (is (not (m/validate gps-spec {:latitude 91.0
                                       :longitude 0.0
                                       :altitude 0.0}))
            "Latitude > 90 should fail")
        
        ;; Invalid latitude (< -90)
        (is (not (m/validate gps-spec {:latitude -91.0
                                       :longitude 0.0
                                       :altitude 0.0}))
            "Latitude < -90 should fail")
        
        ;; Invalid longitude (> 180) - Note: spec requires < 180
        (is (not (m/validate gps-spec {:latitude 0.0
                                       :longitude 180.0
                                       :altitude 0.0}))
            "Longitude >= 180 should fail")))
    
    (testing "Compass azimuth constraints"
      (let [compass-spec (m/schema [:map {:closed true}
                                    [:azimuth :angle/azimuth]])]
        
        ;; Valid azimuth
        (is (m/validate compass-spec {:azimuth 180.0})
            "Valid azimuth should pass")
        
        ;; Invalid azimuth (>= 360)
        (is (not (m/validate compass-spec {:azimuth 360.0}))
            "Azimuth >= 360 should fail")
        
        ;; Invalid azimuth (< 0)
        (is (not (m/validate compass-spec {:azimuth -1.0}))
            "Azimuth < 0 should fail")))
    
    (testing "Temperature constraints"
      (let [temp-spec (m/schema :temperature/component)]
        
        ;; Valid temperature
        (is (m/validate temp-spec 25.0)
            "Normal temperature should pass")
        
        ;; Invalid - below absolute zero
        (is (not (m/validate temp-spec -274.0))
            "Temperature below absolute zero should fail")
        
        ;; Invalid - too high
        (is (not (m/validate temp-spec 151.0))
            "Temperature > 150 should fail")))))

(deftest generate-samples-performance
  (testing "Sample generation performance"
    (let [state-spec (m/schema :state/root)
          start-time (System/currentTimeMillis)
          samples (mg/sample state-spec {:size 1000})
          end-time (System/currentTimeMillis)
          duration (- end-time start-time)]
      
      (is (= 1000 (count samples))
          "Should generate 1000 samples")
      
      (println (format "\nGenerated 1000 samples in %d ms (%.1f samples/sec)"
                      duration
                      (/ 1000.0 (/ duration 1000.0))))
      
      ;; Check that generation is reasonably fast
      (is (< duration 30000)
          "Should generate 1000 samples in less than 30 seconds"))))