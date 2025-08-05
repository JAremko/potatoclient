(ns potatoclient.gesture-specs-proposal
  "Proposal for more precise gesture event specs using :or"
  (:require [malli.core :as m]))

;; ============================================================================
;; Common fields for all gesture events
;; ============================================================================

(def gesture-base
  "Common fields for all gesture events"
  [:map
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type [:enum :heat :day]]])

;; ============================================================================
;; Specific gesture event types
;; ============================================================================

(def tap-event
  "Tap gesture event"
  [:merge gesture-base
   [:map
    [:gesture-type [:= :tap]]
    [:x int?]
    [:y int?]
    [:ndc-x number?]
    [:ndc-y number?]]])

(def double-tap-event
  "Double tap gesture event - includes optional frame timing"
  [:merge gesture-base
   [:map
    [:gesture-type [:= :doubletap]]
    [:x int?]
    [:y int?]
    [:ndc-x number?]
    [:ndc-y number?]
    [:frame-timestamp {:optional true} int?]
    [:frame-duration {:optional true} int?]]])

(def pan-start-event
  "Pan start gesture event"
  [:merge gesture-base
   [:map
    [:gesture-type [:= :panstart]]
    [:x int?]
    [:y int?]
    [:ndc-x number?]
    [:ndc-y number?]]])

(def pan-move-event
  "Pan move gesture event - includes delta values"
  [:merge gesture-base
   [:map
    [:gesture-type [:= :panmove]]
    [:x int?]
    [:y int?]
    [:delta-x int?]
    [:delta-y int?]
    [:ndc-delta-x number?]
    [:ndc-delta-y number?]]])

(def pan-stop-event
  "Pan stop gesture event"
  [:merge gesture-base
   [:map
    [:gesture-type [:= :panstop]]
    [:x int?]
    [:y int?]]])

(def swipe-event
  "Swipe gesture event (if we need it later)"
  [:merge gesture-base
   [:map
    [:gesture-type [:= :swipe]]
    [:direction [:enum :up :down :left :right]]]])

;; ============================================================================
;; Combined gesture event using :or
;; ============================================================================

(def gesture-event
  "Precise gesture event spec using :or for specific types"
  [:or
   tap-event
   double-tap-event
   pan-start-event
   pan-move-event
   pan-stop-event])

;; ============================================================================
;; Example validation
;; ============================================================================

(comment
  ;; Valid tap event
  (m/validate gesture-event
    {:type :gesture
     :gesture-type :tap
     :timestamp 123456
     :canvas-width 1920
     :canvas-height 1080
     :aspect-ratio 1.78
     :stream-type :heat
     :x 100
     :y 200
     :ndc-x 0.5
     :ndc-y -0.5})
  ;; => true
  
  ;; Invalid - tap with delta values
  (m/validate gesture-event
    {:type :gesture
     :gesture-type :tap
     :timestamp 123456
     :canvas-width 1920
     :canvas-height 1080
     :aspect-ratio 1.78
     :stream-type :heat
     :x 100
     :y 200
     :ndc-x 0.5
     :ndc-y -0.5
     :delta-x 10  ; tap shouldn't have deltas!
     :delta-y 20})
  ;; => false
  
  ;; Valid pan-move event
  (m/validate gesture-event
    {:type :gesture
     :gesture-type :panmove
     :timestamp 123456
     :canvas-width 1920
     :canvas-height 1080
     :aspect-ratio 1.78
     :stream-type :heat
     :x 150
     :y 250
     :delta-x 50
     :delta-y 50
     :ndc-delta-x 0.05
     :ndc-delta-y -0.05})
  ;; => true
  
  ;; Get explanation for validation failure
  (m/explain gesture-event
    {:type :gesture
     :gesture-type :tap
     :timestamp 123456
     :canvas-width 1920
     :canvas-height 1080
     :aspect-ratio 1.78
     :stream-type :heat
     ;; Missing required x, y, ndc-x, ndc-y for tap
     }))