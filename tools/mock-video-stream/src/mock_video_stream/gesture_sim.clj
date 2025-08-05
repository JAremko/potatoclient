(ns mock-video-stream.gesture-sim
  "Gesture simulation logic for mock video stream.
  Converts mouse events into gestures and commands."
  (:require [mock-video-stream.core :as core]
            [taoensso.telemere :as log]))

;; ============================================================================
;; Gesture State Machine
;; ============================================================================

(defonce gesture-state 
  (atom {:mouse-down false
         :last-click-time 0
         :last-click-pos nil
         :pan-start-pos nil
         :pan-active false}))

(def DOUBLE_TAP_THRESHOLD 300) ; ms
(def TAP_MOVEMENT_THRESHOLD 5) ; pixels
(def TAP_DURATION_THRESHOLD 300) ; ms

(defn distance [x1 y1 x2 y2]
  (Math/sqrt (+ (* (- x2 x1) (- x2 x1))
                (* (- y2 y1) (- y2 y1)))))

;; ============================================================================
;; Gesture Detection
;; ============================================================================

(defn detect-gesture
  "Process mouse events and detect gestures"
  [event app-state]
  (let [{:keys [type x y timestamp]} event
        state @gesture-state]
    
    (case type
      :mouse-down
      (do
        (swap! gesture-state assoc
               :mouse-down true
               :pan-start-pos {:x x :y y :time timestamp})
        nil) ; No gesture yet
      
      :mouse-move
      (when (:mouse-down state)
        (let [{:keys [x y time]} (:pan-start-pos state)
              moved-distance (distance x y (:x event) (:y event))]
          (cond
            ;; Start pan if moved enough
            (and (not (:pan-active state))
                 (> moved-distance TAP_MOVEMENT_THRESHOLD))
            (do
              (swap! gesture-state assoc :pan-active true)
              {:type :pan-start
               :x x :y y
               :timestamp timestamp})
            
            ;; Continue pan
            (:pan-active state)
            {:type :pan-move
             :x (:x event)
             :y (:y event)
             :delta-x (- (:x event) x)
             :delta-y (- (:y event) y)
             :timestamp timestamp}
            
            :else nil)))
      
      :mouse-up
      (let [{:keys [x y time]} (:pan-start-pos state)
            click-duration (- timestamp time)
            moved-distance (distance x y (:x event) (:y event))
            time-since-last-click (- timestamp (:last-click-time state))]
        
        (swap! gesture-state assoc :mouse-down false)
        
        (cond
          ;; End of pan
          (:pan-active state)
          (do
            (swap! gesture-state assoc :pan-active false :pan-start-pos nil)
            {:type :pan-stop
             :x (:x event)
             :y (:y event)
             :timestamp timestamp})
          
          ;; Double tap
          (and (< moved-distance TAP_MOVEMENT_THRESHOLD)
               (< click-duration TAP_DURATION_THRESHOLD)
               (< time-since-last-click DOUBLE_TAP_THRESHOLD)
               (:last-click-pos state)
               (< (distance x y 
                           (:x (:last-click-pos state)) 
                           (:y (:last-click-pos state)))
                  TAP_MOVEMENT_THRESHOLD))
          (do
            (swap! gesture-state assoc 
                   :last-click-time 0
                   :last-click-pos nil)
            {:type :double-tap
             :x (:x event)
             :y (:y event)
             :timestamp timestamp})
          
          ;; Single tap
          (and (< moved-distance TAP_MOVEMENT_THRESHOLD)
               (< click-duration TAP_DURATION_THRESHOLD))
          (do
            (swap! gesture-state assoc
                   :last-click-time timestamp
                   :last-click-pos {:x (:x event) :y (:y event)})
            {:type :tap
             :x (:x event)
             :y (:y event)
             :timestamp timestamp})
          
          :else nil))
      
      :mouse-wheel
      {:type :wheel
       :x x :y y
       :rotation (:wheel-rotation event)
       :timestamp timestamp}
      
      nil)))

;; ============================================================================
;; Event to Command Pipeline
;; ============================================================================

(defn gesture->command
  "Convert detected gesture to command"
  [gesture app-state]
  (when gesture
    (case (:type gesture)
      :tap (core/generate-tap-command gesture @app-state)
      :double-tap (core/generate-double-tap-command gesture @app-state)
      :pan-move (core/generate-pan-velocity-command gesture @app-state)
      :pan-stop (core/generate-halt-command)
      :wheel (core/generate-zoom-command (:rotation gesture) (:stream-type @app-state))
      nil)))

(defn process-mouse-event
  "Process a mouse event and return any resulting command and gesture event"
  [event app-state]
  (try
    (when-let [gesture (detect-gesture event app-state)]
      (let [command (gesture->command gesture app-state)
            gesture-event (when (#{:tap :double-tap :pan-start :pan-move :pan-stop} (:type gesture))
                           (core/create-gesture-event 
                            (case (:type gesture)
                              :tap :tap
                              :double-tap :double-tap
                              :pan-start :pan-start
                              :pan-move :pan-move
                              :pan-stop :pan-stop)
                            gesture
                            @app-state))]
        {:command command
         :gesture-event gesture-event}))
    (catch Exception e
      (log/error! e "Error processing mouse event" {:event event})
      nil)))

;; ============================================================================
;; Batch Event Processing
;; ============================================================================

(defn process-event-sequence
  "Process a sequence of mouse events and return all generated commands and gestures"
  [events app-state]
  (reduce (fn [acc event]
            (if-let [result (process-mouse-event event app-state)]
              (cond-> acc
                (:command result) (update :commands conj (:command result))
                (:gesture-event result) (update :gesture-events conj (:gesture-event result)))
              acc))
          {:commands []
           :gesture-events []}
          events))