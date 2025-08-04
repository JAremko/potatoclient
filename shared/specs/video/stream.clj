(ns potatoclient.specs.video.stream
  "Shared specifications for video stream messages and commands.
  Used by both the mock video stream tool and the main application."
  (:require [malli.core :as m]))

;; ============================================================================
;; Basic Types
;; ============================================================================

(def ndc-coordinate
  "NDC coordinate in range [-1, 1]"
  [:and :double [:>= -1.0] [:<= 1.0]])

(def pixel-coordinate
  "Pixel coordinate (non-negative integer)"
  [:and :int [:>= 0]])

(def stream-type
  "Video stream type"
  [:enum :heat :day])

(def mouse-button
  "Mouse button identifier"
  [:enum 1 2 3]) ; Left, Right, Middle

(def gesture-type
  "Recognized gesture types"
  [:enum :tap :double-tap :pan-start :pan-move :pan-stop :swipe])

(def rotary-direction
  "Rotation direction"
  [:enum :clockwise :counter-clockwise])

;; ============================================================================
;; Video Stream Commands (what streams send to main process)
;; ============================================================================

(def rotary-goto-ndc-command
  "Command to rotate platform to specific NDC coordinates"
  [:map
   [:rotary [:map
             [:goto-ndc [:map
                        [:channel stream-type]
                        [:x ndc-coordinate]
                        [:y ndc-coordinate]]]]]])

(def cv-start-track-ndc-command
  "Command to start CV tracking at NDC coordinates"
  [:map
   [:cv [:map
         [:start-track-ndc [:map
                           [:channel stream-type]
                           [:x ndc-coordinate]
                           [:y ndc-coordinate]
                           [:frame-time {:optional true} :int]]]]]])

(def rotary-set-velocity-command
  "Command to set rotary platform velocity"
  [:map
   [:rotary [:map
             [:set-velocity [:map
                            [:azimuth-speed :double]
                            [:elevation-speed :double]
                            [:azimuth-direction rotary-direction]
                            [:elevation-direction rotary-direction]]]]]])

(def rotary-halt-command
  "Command to halt all rotary movement"
  [:map
   [:rotary [:map [:halt [:map]]]]])

(def heat-camera-zoom-command
  "Heat camera zoom commands"
  [:or
   [:map [:heat-camera [:map [:next-zoom-table-pos [:map]]]]]
   [:map [:heat-camera [:map [:prev-zoom-table-pos [:map]]]]]])

(def day-camera-zoom-command
  "Day camera zoom commands"
  [:or
   [:map [:day-camera [:map [:next-zoom-table-pos [:map]]]]]
   [:map [:day-camera [:map [:prev-zoom-table-pos [:map]]]]]])

(def video-stream-command
  "Any command that can be sent by video stream"
  [:or 
   rotary-goto-ndc-command
   cv-start-track-ndc-command
   rotary-set-velocity-command
   rotary-halt-command
   heat-camera-zoom-command
   day-camera-zoom-command])

;; ============================================================================
;; Input Events (sent to video stream)
;; ============================================================================

(def mouse-event
  "Mouse event structure"
  [:map
   [:type [:enum :mouse-down :mouse-up :mouse-move :mouse-wheel]]
   [:x pixel-coordinate]
   [:y pixel-coordinate]
   [:button {:optional true} mouse-button]
   [:wheel-rotation {:optional true} :int]
   [:timestamp :int]])

(def frame-data
  "Frame timing data"
  [:map
   [:timestamp :int]
   [:duration :int]])

;; ============================================================================
;; Gesture Events (emitted by video stream)
;; ============================================================================

(def gesture-event
  "Gesture event emitted by video stream"
  [:map
   [:type [:= :gesture]]
   [:gesture-type gesture-type]
   [:timestamp :int]
   [:canvas-width :int]
   [:canvas-height :int]
   [:aspect-ratio :double]
   [:stream-type stream-type]
   [:x {:optional true} pixel-coordinate]
   [:y {:optional true} pixel-coordinate]
   [:ndc-x {:optional true} ndc-coordinate]
   [:ndc-y {:optional true} ndc-coordinate]
   [:delta-x {:optional true} :int]
   [:delta-y {:optional true} :int]
   [:ndc-delta-x {:optional true} :double]
   [:ndc-delta-y {:optional true} :double]
   [:frame-timestamp {:optional true} :int]
   [:frame-duration {:optional true} :int]])

;; ============================================================================
;; Window Events
;; ============================================================================

(def window-event
  "Window event from video stream"
  [:map
   [:type [:= :window]]
   [:data [:map
           [:type [:enum :open :close :resize :minimize :restore :focus :blur]]
           [:stream-id :string]
           [:width {:optional true} :int]
           [:height {:optional true} :int]]]])

;; ============================================================================
;; Test Scenarios
;; ============================================================================

(def test-scenario
  "Test scenario specification for mock video stream"
  [:map
   [:description :string]
   [:canvas [:map [:width :int] [:height :int]]]
   [:stream-type {:optional true :default :heat} stream-type]
   [:zoom-level {:optional true :default 0} [:and :int [:>= 0] [:<= 4]]]
   [:frame-data {:optional true} frame-data]
   [:events [:vector mouse-event]]
   [:expected-commands [:vector video-stream-command]]
   [:expected-gestures {:optional true} [:vector gesture-event]]])

(def test-scenario-batch
  "Batch of test scenarios"
  [:map-of :keyword test-scenario])

;; ============================================================================
;; Control Messages (for mock subprocess)
;; ============================================================================

(def control-message
  "Control message for mock video stream"
  [:map
   [:type [:enum :set-frame-data :set-zoom-level :inject-event :get-status]]
   [:data {:optional true} :any]])

(def mock-status
  "Status response from mock video stream"
  [:map
   [:status [:enum :running :stopped :error]]
   [:stream-type stream-type]
   [:canvas-width :int]
   [:canvas-height :int]
   [:current-zoom :int]
   [:frame-data {:optional true} frame-data]])