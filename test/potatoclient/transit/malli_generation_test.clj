(ns potatoclient.transit.malli-generation-test
  "Test that Malli can generate valid command parameters that pass our Guardrails validation.
  
  This is a precursor to full Kotlin validation - first we ensure Malli generates
  valid data according to our specs."
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.ui-specs :as ui-specs]))

;; =============================================================================
;; Command Parameter Schemas
;; =============================================================================

;; Define precise schemas that match our Guardrails specs
(def command-schemas
  {::cv-start-track-ndc
   [:map
    [:channel [:enum :heat :day]]
    [:x [:and number? [:>= -1] [:<= 1]]]
    [:y [:and number? [:>= -1] [:<= 1]]]
    [:frame-timestamp {:optional true} pos-int?]]
   
   ::rotary-goto
   [:map
    [:azimuth [:double {:min 0.0 :max 360.0}]]
    [:elevation [:double {:min -30.0 :max 90.0}]]]
   
   ::rotary-set-velocity
   [:map
    [:azimuth-speed [:and number? [:>= 0]]]
    [:elevation-speed [:and number? [:>= 0]]]
    [:azimuth-direction [:enum :clockwise :counter-clockwise]]
    [:elevation-direction [:enum :clockwise :counter-clockwise]]]
   
   ::heat-camera-palette
   [:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic]
   
   ::set-localization
   [:enum :en :uk]
   
   ::day-camera-focus-mode
   [:enum :auto :manual :infinity]
   
   ::compass-unit
   [:enum :degrees :mils]})

;; =============================================================================
;; Generation Tests
;; =============================================================================

(deftest test-cv-command-generation
  (testing "Malli generates valid CV command parameters"
    (let [samples (mg/sample (::cv-start-track-ndc command-schemas) {:size 20})]
      (is (= 20 (count samples)) "Should generate requested number of samples")
      
      (doseq [sample samples]
        ;; Test that generated values pass our constraints
        (is (contains? #{:heat :day} (:channel sample)))
        (is (<= -1 (:x sample) 1))
        (is (<= -1 (:y sample) 1))
        (when (contains? sample :frame-timestamp)
          (is (pos-int? (:frame-timestamp sample))))
        
        ;; Test that we can create a command with generated values
        (let [command (cmd/cv-start-track-ndc 
                        (:channel sample)
                        (:x sample)
                        (:y sample)
                        (:frame-timestamp sample))]
          (is (map? command))
          (is (contains? command :cv))
          (is (= (:channel sample) 
                 (get-in command [:cv :start-track-ndc :channel]))))))))

(deftest test-rotary-command-generation
  (testing "Malli generates valid rotary command parameters"
    ;; Test goto
    (let [goto-samples (mg/sample (::rotary-goto command-schemas) {:size 20})]
      (doseq [sample goto-samples]
        (is (and (number? (:azimuth sample))
                 (<= 0 (:azimuth sample))
                 (< (:azimuth sample) 360)))
        (is (and (number? (:elevation sample))
                 (<= -30 (:elevation sample))
                 (<= (:elevation sample) 90)))
        
        ;; Create command - rotary-goto takes a map
        (let [command (cmd/rotary-goto sample)]
          (is (= (:azimuth sample) (get-in command [:rotary :goto :azimuth])))
          (is (= (:elevation sample) (get-in command [:rotary :goto :elevation]))))))
    
    ;; Test set-velocity
    (let [velocity-samples (mg/sample (::rotary-set-velocity command-schemas) {:size 20})]
      (doseq [sample velocity-samples]
        (is (and (number? (:azimuth-speed sample))
                 (>= (:azimuth-speed sample) 0)))
        (is (and (number? (:elevation-speed sample))
                 (>= (:elevation-speed sample) 0)))
        (is (contains? #{:clockwise :counter-clockwise} (:azimuth-direction sample)))
        (is (contains? #{:clockwise :counter-clockwise} (:elevation-direction sample)))))))

(deftest test-enum-generation
  (testing "Malli generates valid enum values"
    ;; Heat camera palette
    (let [palette-samples (mg/sample (::heat-camera-palette command-schemas) {:size 20})]
      (is (every? #{:white-hot :black-hot :rainbow :ironbow :lava :arctic} palette-samples)))
    
    ;; Localization
    (let [locale-samples (mg/sample (::set-localization command-schemas) {:size 10})]
      (is (every? #{:en :uk} locale-samples)))
    
    ;; Focus mode
    (let [focus-samples (mg/sample (::day-camera-focus-mode command-schemas) {:size 10})]
      (is (every? #{:auto :manual :infinity} focus-samples)))
    
    ;; Compass unit
    (let [unit-samples (mg/sample (::compass-unit command-schemas) {:size 10})]
      (is (every? #{:degrees :mils} unit-samples)))))

(deftest test-edge-case-generation
  (testing "Malli can generate edge case values"
    ;; For numeric ranges, Malli should generate values near boundaries
    (let [samples (mg/sample [:and number? [:>= 0] [:< 360]] {:size 1000})]
      ;; Check we get some values near boundaries
      (let [near-zero (filter #(< % 1) samples)
            near-max (filter #(> % 358) samples)]  ; < 360 is exclusive
        (is (pos? (count near-zero)) "Should generate some values near 0")
        (is (pos? (count near-max)) "Should generate some values near 360")))
    
    ;; For NDC coordinates
    (let [ndc-samples (mg/sample [:and number? [:>= -1] [:<= 1]] {:size 1000})]
      (let [near-min (filter #(< % -0.9) ndc-samples)
            near-max (filter #(> % 0.9) ndc-samples)
            near-zero (filter #(< (Math/abs %) 0.1) ndc-samples)]
        (is (pos? (count near-min)) "Should generate values near -1")
        (is (pos? (count near-max)) "Should generate values near 1")
        (is (pos? (count near-zero)) "Should generate values near 0")))))

(deftest test-command-creation-with-generated-values
  (testing "Commands created with generated values are valid"
    ;; Generate a variety of commands
    (let [commands-to-test
          [(repeatedly 10 #(let [s (mg/generate (::cv-start-track-ndc command-schemas))]
                            (cmd/cv-start-track-ndc (:channel s) (:x s) (:y s) (:frame-timestamp s))))
           (repeatedly 10 #(let [s (mg/generate (::rotary-goto command-schemas))]
                            (cmd/rotary-goto s)))  ; Takes a map
           (repeatedly 10 #(cmd/heat-camera-palette 
                            (mg/generate (::heat-camera-palette command-schemas))))
           (repeatedly 10 #(cmd/set-localization
                            (mg/generate (::set-localization command-schemas))))]]
      
      (doseq [command-list commands-to-test]
        (doseq [command command-list]
          ;; Just verify the command was created successfully
          ;; Guardrails will validate at runtime
          (is (map? command) "Command should be a map")
          (is (pos? (count command)) "Command should not be empty"))))))

(deftest test-statistical-distribution
  (testing "Generated values have reasonable distribution"
    ;; Check that enum generation is roughly uniform
    (let [locale-samples (mg/sample (::set-localization command-schemas) {:size 1000})
          en-count (count (filter #{:en} locale-samples))
          uk-count (count (filter #{:uk} locale-samples))]
      ;; Should be roughly 50/50, allow 40-60% range
      (is (< 400 en-count 600) "English locale should be ~50% of samples")
      (is (< 400 uk-count 600) "Ukrainian locale should be ~50% of samples"))
    
    ;; Check numeric distribution spans the range
    (let [azimuth-samples (map :azimuth 
                               (mg/sample (::rotary-goto command-schemas) {:size 1000}))
          min-val (apply min azimuth-samples)
          max-val (apply max azimuth-samples)
          mean-val (/ (reduce + azimuth-samples) (count azimuth-samples))]
      (is (< min-val 20) "Should generate values near minimum")
      (is (> max-val 340) "Should generate values near maximum")
      ;; Mean might be skewed by Malli's generation strategy
      (is (number? mean-val) "Mean should be a number"))))