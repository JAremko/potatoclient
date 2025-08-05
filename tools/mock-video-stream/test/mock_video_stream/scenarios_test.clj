(ns mock-video-stream.scenarios-test
  (:require [clojure.test :refer [deftest testing is]]
            [mock-video-stream.scenarios :as scenarios]
            [mock-video-stream.gesture-sim :as gesture-sim]
            [mock-video-stream.core :as core]
            [malli.core :as m]
            [potatoclient.specs.video.stream :as video-specs]))

(deftest scenario-validation-test
  (testing "All scenarios are valid"
    (let [results (scenarios/validate-all-scenarios)]
      (is (= (:total results) (:valid results)))
      (is (empty? (:invalid results))))))

(deftest scenario-specs-test
  (testing "Each scenario matches the test-scenario spec"
    (doseq [[name scenario] scenarios/scenarios]
      (testing (str "Scenario: " name)
        (is (m/validate video-specs/test-scenario scenario)
            (str "Scenario " name " should validate against spec"))))))

(deftest scenario-execution-test
  (testing "Tap center scenario"
    (let [scenario (scenarios/get-scenario :tap-center)]
      (reset! core/state
              {:stream-type :heat
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data {:timestamp 0 :duration 33}})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)]
        (is (= (:expected-commands scenario) (:commands result))))))
  
  (testing "Double tap track scenario"
    (let [scenario (scenarios/get-scenario :double-tap-track)]
      (reset! core/state
              {:stream-type :heat
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data (:frame-data scenario)})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)]
        (is (= (:expected-commands scenario) (:commands result))))))
  
  (testing "Pan rotate right scenario"
    (let [scenario (scenarios/get-scenario :pan-rotate-right)]
      (reset! core/state
              {:stream-type :heat
               :canvas (:canvas scenario)
               :zoom-level (:zoom-level scenario)
               :frame-data {:timestamp 0 :duration 33}})
      (reset! gesture-sim/gesture-state
              {:mouse-down false :last-click-time 0 :last-click-pos nil
               :pan-start-pos nil :pan-active false})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)
            commands (:commands result)]
        ;; Should have velocity command and halt
        (is (= 2 (count commands)))
        (is (contains? (:rotary (first commands)) :set-velocity))
        (is (= {:rotary {:halt {}}} (second commands))))))
  
  (testing "Zoom scenarios"
    ;; Heat camera zoom in
    (let [scenario (scenarios/get-scenario :zoom-in-heat)]
      (reset! core/state
              {:stream-type (:stream-type scenario)
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data {:timestamp 0 :duration 33}})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)]
        (is (= (:expected-commands scenario) (:commands result)))))
    
    ;; Day camera zoom out
    (let [scenario (scenarios/get-scenario :zoom-out-day)]
      (reset! core/state
              {:stream-type (:stream-type scenario)
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data {:timestamp 0 :duration 33}})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)]
        (is (= (:expected-commands scenario) (:commands result))))))

(deftest corner-case-scenarios-test
  (testing "Rapid taps don't trigger double tap"
    (let [scenario (scenarios/get-scenario :rapid-taps)]
      (reset! core/state
              {:stream-type :heat
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data {:timestamp 0 :duration 33}})
      (reset! gesture-sim/gesture-state
              {:mouse-down false :last-click-time 0 :last-click-pos nil
               :pan-start-pos nil :pan-active false})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)]
        ;; Should have 3 separate tap commands
        (is (= 3 (count (:commands result))))
        (is (every? #(contains? (:rotary %) :goto-ndc) (:commands result)))))))

(deftest ndc-coordinate-scenarios-test
  (testing "Corner taps produce correct NDC coordinates"
    ;; Top-left
    (let [scenario (scenarios/get-scenario :tap-top-left)]
      (reset! core/state
              {:stream-type :heat
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data {:timestamp 0 :duration 33}})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)
            ndc (get-in (first (:commands result)) [:rotary :goto-ndc])]
        (is (= -1.0 (:x ndc)))
        (is (= 1.0 (:y ndc)))))
    
    ;; Bottom-right (approximate due to pixel boundaries)
    (let [scenario (scenarios/get-scenario :tap-bottom-right)]
      (reset! core/state
              {:stream-type :heat
               :canvas (:canvas scenario)
               :zoom-level 0
               :frame-data {:timestamp 0 :duration 33}})
      (let [result (gesture-sim/process-event-sequence (:events scenario) core/state)
            ndc (get-in (first (:commands result)) [:rotary :goto-ndc])]
        (is (< 0.99 (:x ndc) 1.0))
        (is (< -1.0 (:y ndc) -0.99))))))