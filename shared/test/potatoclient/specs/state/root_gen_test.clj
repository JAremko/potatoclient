(ns potatoclient.specs.state.root-gen-test
  "Generative tests for state.Root spec.
   Tests that generated samples are valid according to Malli specs."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   ;; Load the spec definitions
   [potatoclient.specs.state.root]))

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
          (is (match? {:protocol_version pos-int?} sample)))
        
        (testing "All required subsystems are present"
          (is (match? {:system any?
                       :meteo_internal any?
                       :lrf any?
                       :time any?
                       :gps any?
                       :compass any?
                       :rotary any?
                       :camera_day any?
                       :camera_heat any?
                       :compass_calibration any?
                       :rec_osd any?
                       :day_cam_glass_heater any?
                       :actual_space_time any?}
                      sample)))
        
        (testing "Nested structures are valid"
          (when (:gps sample)
            (is (match? {:gps {:latitude #(<= -90 % 90)
                               :longitude any?
                               :altitude any?
                               :manual_latitude any?
                               :manual_longitude any?
                               :manual_altitude any?
                               :fix_type any?
                               :use_manual any?}}
                       sample)))
          
          (when (:compass sample)
            (is (match? {:compass {:azimuth #(and (>= % 0) (< % 360))}}
                       sample)))
          
          (when (:lrf sample)
            (is (match? {:lrf {:target map?}} sample))
            (when-let [target-color (get-in sample [:lrf :target :target_color])]
              (is (match? {:red #(<= 0 % 255)
                          :green #(<= 0 % 255)
                          :blue #(<= 0 % 255)}
                         target-color))))
          
          (when (:rotary sample)
            (is (match? {:rotary {:current_scan_node map?}} sample))))))))

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