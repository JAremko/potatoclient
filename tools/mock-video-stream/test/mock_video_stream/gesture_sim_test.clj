(ns mock-video-stream.gesture-sim-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [mock-video-stream.gesture-sim :as gesture-sim]
            [mock-video-stream.core :as core]))

;; Reset gesture state before each test
(use-fixtures :each
  (fn [f]
    (reset! gesture-sim/gesture-state 
            {:mouse-down false
             :last-click-time 0
             :last-click-pos nil
             :pan-start-pos nil
             :pan-active false})
    (reset! core/state
            {:stream-type :heat
             :canvas {:width 800 :height 600}
             :zoom-level 0
             :frame-data {:timestamp 0 :duration 33}})
    (f)))

(deftest gesture-detection-test
  (testing "Single tap detection"
    ;; Mouse down
    (let [down-event {:type :mouse-down :x 400 :y 300 :timestamp 1000}
          result (gesture-sim/detect-gesture down-event core/state)]
      (is (nil? result))
      (is (:mouse-down @gesture-sim/gesture-state))
      (is (= {:x 400 :y 300 :time 1000} (:pan-start-pos @gesture-sim/gesture-state))))
    
    ;; Mouse up - completes tap
    (let [up-event {:type :mouse-up :x 400 :y 300 :timestamp 1050}
          result (gesture-sim/detect-gesture up-event core/state)]
      (is (= :tap (:type result)))
      (is (= 400 (:x result)))
      (is (= 300 (:y result)))
      (is (not (:mouse-down @gesture-sim/gesture-state)))))
  
  (testing "Double tap detection"
    ;; First tap
    (gesture-sim/detect-gesture {:type :mouse-down :x 400 :y 300 :timestamp 1000} core/state)
    (gesture-sim/detect-gesture {:type :mouse-up :x 400 :y 300 :timestamp 1050} core/state)
    
    ;; Second tap within threshold
    (gesture-sim/detect-gesture {:type :mouse-down :x 402 :y 298 :timestamp 1200} core/state)
    (let [result (gesture-sim/detect-gesture {:type :mouse-up :x 402 :y 298 :timestamp 1250} core/state)]
      (is (= :double-tap (:type result)))
      (is (= 402 (:x result)))
      (is (= 298 (:y result)))))
  
  (testing "Pan gesture detection"
    ;; Start pan
    (gesture-sim/detect-gesture {:type :mouse-down :x 400 :y 300 :timestamp 1000} core/state)
    
    ;; Move beyond threshold
    (let [result (gesture-sim/detect-gesture {:type :mouse-move :x 410 :y 300 :timestamp 1050} core/state)]
      (is (= :pan-start (:type result)))
      (is (= 400 (:x result)))
      (is (= 300 (:y result)))
      (is (:pan-active @gesture-sim/gesture-state)))
    
    ;; Continue pan
    (let [result (gesture-sim/detect-gesture {:type :mouse-move :x 420 :y 290 :timestamp 1100} core/state)]
      (is (= :pan-move (:type result)))
      (is (= 420 (:x result)))
      (is (= 290 (:y result)))
      (is (= 20 (:delta-x result)))
      (is (= -10 (:delta-y result))))
    
    ;; End pan
    (let [result (gesture-sim/detect-gesture {:type :mouse-up :x 430 :y 280 :timestamp 1150} core/state)]
      (is (= :pan-stop (:type result)))
      (is (not (:pan-active @gesture-sim/gesture-state)))))
  
  (testing "Mouse wheel detection"
    (let [wheel-event {:type :mouse-wheel :x 400 :y 300 :wheel-rotation -1 :timestamp 1000}
          result (gesture-sim/detect-gesture wheel-event core/state)]
      (is (= :wheel (:type result)))
      (is (= -1 (:rotation result))))))

(deftest gesture-to-command-test
  (testing "Tap gesture generates goto command"
    (let [gesture {:type :tap :x 400 :y 300 :timestamp 1000}
          command (gesture-sim/gesture->command gesture core/state)]
      (is (= {:rotary {:goto-ndc {:channel :heat :x 0.0 :y 0.0}}} command))))
  
  (testing "Double-tap generates track command"
    (let [gesture {:type :double-tap :x 200 :y 150 :timestamp 1000}
          _ (swap! core/state assoc :frame-data {:timestamp 55555 :duration 33})
          command (gesture-sim/gesture->command gesture core/state)]
      (is (= {:cv {:start-track-ndc {:channel :heat :x -0.5 :y 0.5 :frame-time 55555}}} command))))
  
  (testing "Pan generates velocity command"
    (let [gesture {:type :pan-move :x 420 :y 280 :delta-x 20 :delta-y -20 :timestamp 1000}
          _ (swap! core/state assoc :zoom-level 2)
          command (gesture-sim/gesture->command gesture core/state)]
      (is (contains? (:rotary command) :set-velocity))
      (let [vel (get-in command [:rotary :set-velocity])]
        (is (pos? (:azimuth-speed vel)))
        (is (pos? (:elevation-speed vel)))
        (is (= :clockwise (:azimuth-direction vel)))
        (is (= :clockwise (:elevation-direction vel))))))
  
  (testing "Pan stop generates halt"
    (let [gesture {:type :pan-stop :x 430 :y 280 :timestamp 1000}
          command (gesture-sim/gesture->command gesture core/state)]
      (is (= {:rotary {:halt {}}} command))))
  
  (testing "Wheel generates zoom command"
    (let [gesture {:type :wheel :x 400 :y 300 :rotation -1 :timestamp 1000}
          command (gesture-sim/gesture->command gesture core/state)]
      (is (= {:heat-camera {:next-zoom-table-pos {}}} command)))))

(deftest process-mouse-event-test
  (testing "Complete tap sequence"
    (let [events [{:type :mouse-down :x 100 :y 100 :button 1 :timestamp 1000}
                  {:type :mouse-up :x 100 :y 100 :button 1 :timestamp 1050}]
          result1 (gesture-sim/process-mouse-event (first events) core/state)
          result2 (gesture-sim/process-mouse-event (second events) core/state)]
      (is (nil? result1))
      (is (some? (:command result2)))
      (is (some? (:gesture-event result2)))
      (is (= :tap (get-in result2 [:gesture-event :gesture-type])))))
  
  (testing "Complete pan sequence"
    ;; Reset state
    (reset! gesture-sim/gesture-state 
            {:mouse-down false :last-click-time 0 :last-click-pos nil 
             :pan-start-pos nil :pan-active false})
    
    (let [events [{:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1000}
                  {:type :mouse-move :x 420 :y 300 :timestamp 1050}
                  {:type :mouse-move :x 440 :y 300 :timestamp 1100}
                  {:type :mouse-up :x 440 :y 300 :timestamp 1150}]
          results (map #(gesture-sim/process-mouse-event % core/state) events)]
      ;; Mouse down - no output
      (is (nil? (first results)))
      ;; First move - pan start
      (is (= :pan-start (get-in (second results) [:gesture-event :gesture-type])))
      ;; Second move - velocity command
      (is (contains? (get-in (nth results 2) [:command :rotary]) :set-velocity))
      ;; Mouse up - halt command
      (is (= {:rotary {:halt {}}} (:command (last results)))))))

(deftest batch-event-processing-test
  (testing "Process scenario events"
    (let [events [{:type :mouse-down :x 200 :y 200 :button 1 :timestamp 1000}
                  {:type :mouse-up :x 200 :y 200 :button 1 :timestamp 1050}
                  {:type :mouse-wheel :x 400 :y 300 :wheel-rotation -1 :timestamp 2000}]
          result (gesture-sim/process-event-sequence events core/state)]
      (is (= 2 (count (:commands result))))
      (is (= 1 (count (:gesture-events result))))
      ;; First command is tap
      (is (contains? (:rotary (first (:commands result))) :goto-ndc))
      ;; Second command is zoom
      (is (contains? (:heat-camera (second (:commands result))) :next-zoom-table-pos))
      ;; Gesture event is tap
      (is (= :tap (get-in result [:gesture-events 0 :gesture-type]))))))