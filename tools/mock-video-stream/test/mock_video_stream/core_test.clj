(ns mock-video-stream.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [mock-video-stream.core :as core]
            [potatoclient.specs.video.stream :as video-specs]
            [malli.core :as m]))

(deftest ndc-conversion-test
  (testing "Pixel to NDC conversion"
    ;; Center of 800x600 canvas
    (is (= {:x 0.0 :y 0.0} 
           (core/pixel->ndc 400 300 800 600)))
    
    ;; Top-left corner
    (is (= {:x -1.0 :y 1.0}
           (core/pixel->ndc 0 0 800 600)))
    
    ;; Bottom-right corner (with rounding)
    (let [result (core/pixel->ndc 799 599 800 600)]
      (is (< 0.99 (:x result) 1.0))
      (is (< -1.0 (:y result) -0.99))))
  
  (testing "NDC to pixel conversion"
    ;; Center
    (is (= {:x 400 :y 300}
           (core/ndc->pixel 0.0 0.0 800 600)))
    
    ;; Corners
    (is (= {:x 0 :y 0}
           (core/ndc->pixel -1.0 1.0 800 600)))
    (is (= {:x 800 :y 600}
           (core/ndc->pixel 1.0 -1.0 800 600))))
  
  (testing "Pixel delta to NDC delta"
    ;; 40 pixel movement in 800 width = 0.1 NDC units
    (is (= {:x 0.1 :y 0.0}
           (core/pixel-delta->ndc 40 0 800 600)))
    
    ;; 30 pixel movement in 600 height = 0.1 NDC units (inverted Y)
    (is (= {:x 0.0 :y -0.1}
           (core/pixel-delta->ndc 0 30 600 600)))))

(deftest command-generation-test
  (testing "Tap command generation"
    (let [gesture {:x 400 :y 300}
          state {:stream-type :heat :canvas {:width 800 :height 600}}
          cmd (core/generate-tap-command gesture state)]
      (is (= {:rotary {:goto-ndc {:channel :heat :x 0.0 :y 0.0}}} cmd))
      (is (m/validate video-specs/video-stream-command cmd))))
  
  (testing "Double-tap command generation"
    (let [gesture {:x 200 :y 150}
          state {:stream-type :day 
                 :canvas {:width 800 :height 600}
                 :frame-data {:timestamp 12345 :duration 33}}
          cmd (core/generate-double-tap-command gesture state)]
      (is (= {:cv {:start-track-ndc {:channel :day :x -0.5 :y 0.5 :frame-time 12345}}} cmd))
      (is (m/validate video-specs/video-stream-command cmd))))
  
  (testing "Pan velocity command generation"
    (let [gesture {:delta-x 40 :delta-y -30}
          state {:zoom-level 2 :canvas {:width 800 :height 600}}
          cmd (core/generate-pan-velocity-command gesture state)]
      (is (contains? (:rotary cmd) :set-velocity))
      (let [velocity (get-in cmd [:rotary :set-velocity])]
        (is (pos? (:azimuth-speed velocity)))
        (is (pos? (:elevation-speed velocity)))
        (is (= :clockwise (:azimuth-direction velocity)))
        (is (= :clockwise (:elevation-direction velocity))))
      (is (m/validate video-specs/video-stream-command cmd))))
  
  (testing "Halt command generation"
    (let [cmd (core/generate-halt-command)]
      (is (= {:rotary {:halt {}}} cmd))
      (is (m/validate video-specs/video-stream-command cmd))))
  
  (testing "Zoom command generation"
    ;; Zoom in on heat camera
    (is (= {:heat-camera {:next-zoom-table-pos {}}}
           (core/generate-zoom-command -1 :heat)))
    
    ;; Zoom out on day camera
    (is (= {:day-camera {:prev-zoom-table-pos {}}}
           (core/generate-zoom-command 1 :day)))))

(deftest gesture-event-generation-test
  (testing "Gesture event creation"
    (let [state {:canvas {:width 800 :height 600}
                 :stream-type :heat
                 :frame-data {:timestamp 55555 :duration 33}}
          event (core/create-gesture-event :tap {:x 400 :y 300} state)]
      (is (= :gesture (:type event)))
      (is (= :tap (:gesture-type event)))
      (is (= 400 (:x event)))
      (is (= 300 (:y event)))
      (is (= 0.0 (:ndc-x event)))
      (is (= 0.0 (:ndc-y event)))
      (is (= 800 (:canvas-width event)))
      (is (= 600 (:canvas-height event)))
      (is (= (/ 4.0 3.0) (:aspect-ratio event)))
      (is (= :heat (:stream-type event)))
      (is (= 55555 (:frame-timestamp event)))
      (is (= 33 (:frame-duration event)))
      (is (m/validate video-specs/gesture-event event)))))

(deftest message-handling-test
  (testing "Control message handling"
    ;; Set frame data
    (let [msg {:msg-type :control
               :msg-id "test-123"
               :timestamp 1234567890
               :payload {:type :set-frame-data
                        :data {:timestamp 99999 :duration 16}}}]
      (is (= {:status :ok} (core/handle-message msg)))
      (is (= {:timestamp 99999 :duration 16} (:frame-data @core/state))))
    
    ;; Get status
    (reset! core/state {:stream-type :day
                       :canvas {:width 1920 :height 1080}
                       :zoom-level 3
                       :frame-data {:timestamp 12345 :duration 33}})
    (let [msg {:msg-type :control
               :msg-id "test-456"
               :timestamp 1234567890
               :payload {:type :get-status}}
          response (core/handle-message msg)]
      (is (= :running (:status response)))
      (is (= :day (:stream-type response)))
      (is (= 1920 (:canvas-width response)))
      (is (= 1080 (:canvas-height response)))
      (is (= 3 (:current-zoom response)))))
  
  (testing "Unknown message handling"
    (let [msg {:msg-type :unknown
               :msg-id "test-789"
               :timestamp 1234567890
               :payload {}}
          response (core/handle-message msg)]
      (is (= :error (:status response)))
      (is (string? (:message response))))))