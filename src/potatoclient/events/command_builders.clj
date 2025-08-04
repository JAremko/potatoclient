(ns potatoclient.events.command-builders
  "Helper functions to create commands from video stream events.
  
  This namespace provides Guardrails-validated functions that convert
  gesture and navigation events from video streams into appropriate commands.
  Modeled after the TypeScript command builders in examples/frontend/ts/cmd/cmdSender/"
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.logging :as logging]
            [potatoclient.transit.commands :as commands]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.subprocess-launcher :as subprocess]
            [potatoclient.ui-specs :as specs]))

;; ============================================================================
;; Command Builder Functions
;; ============================================================================

(>defn build-rotate-to-ndc-command
  "Build a command to rotate the platform to an NDC position.
  
  This is triggered by a single tap gesture on the video stream.
  Similar to TypeScript: RotateToNDC(channel, ndcX, ndcY)"
  [stream-type ndc-x ndc-y]
  [::specs/stream-type number? number? => map?]
  (let [channel (case stream-type
                  :heat :heat
                  :day :day)]
    (commands/rotary-goto-ndc channel ndc-x ndc-y)))

(>defn build-cv-track-command
  "Build a command to start CV tracking at an NDC position.
  
  This is triggered by a double-tap gesture on the video stream.
  Similar to TypeScript: CVStartTrackNDC(channel, x, y)"
  [stream-type ndc-x ndc-y frame-timestamp]
  [::specs/stream-type number? number? (? int?) => map?]
  (let [channel (case stream-type
                  :heat :heat
                  :day :day)]
    (commands/cv-start-track-ndc channel ndc-x ndc-y frame-timestamp)))

(>defn build-rotary-velocity-command
  "Build a command to set rotary platform velocity.
  
  This is used during pan gestures to control platform movement.
  The speed is calculated based on zoom level and gesture delta."
  [azimuth-speed elevation-speed azimuth-direction elevation-direction]
  [number? number? [:enum :clockwise :counter-clockwise]
   [:enum :clockwise :counter-clockwise] => map?]
  (commands/rotary-set-velocity azimuth-speed elevation-speed
                                azimuth-direction elevation-direction))

(>defn build-rotary-halt-command
  "Build a command to halt all rotary movement.
  
  This is triggered when a pan gesture ends."
  []
  [=> map?]
  (commands/rotary-halt))

(>defn build-zoom-command
  "Build a zoom command for the appropriate camera.
  
  This could be triggered by zoom gestures or UI controls."
  [stream-type zoom-value]
  [::specs/stream-type number? => map?]
  (case stream-type
    :heat (commands/heat-camera-zoom zoom-value)
    :day (commands/day-camera-zoom zoom-value)))

;; ============================================================================
;; Event to Command Conversion
;; ============================================================================

(>defn gesture-to-command
  "Convert a gesture event into the appropriate command.
  
  This function maps gesture types to their corresponding commands,
  similar to the TypeScript InteractionHandler.handleInteraction()"
  [{:keys [gesture-type stream-type ndc-x ndc-y ndc-delta-x ndc-delta-y
           frame-timestamp]}]
  [::specs/gesture-event => (? map?)]
  (case gesture-type
    :tap
    (build-rotate-to-ndc-command stream-type ndc-x ndc-y)

    :doubletap
    (build-cv-track-command stream-type ndc-x ndc-y frame-timestamp)

    :panstart
    nil ; Pan start just updates state, no command needed

    :panmove
    ;; Calculate speeds based on current zoom and deltas
    (let [;; This would use the speed calculation logic based on zoom
          az-speed (Math/abs ndc-delta-x)
          el-speed (Math/abs ndc-delta-y)
          az-direction (if (pos? ndc-delta-x) :clockwise :counter-clockwise)
          el-direction (if (pos? ndc-delta-y) :clockwise :counter-clockwise)]
      (build-rotary-velocity-command az-speed el-speed az-direction el-direction))

    :panstop
    (build-rotary-halt-command)

    :swipe
    nil ; Swipes could trigger UI actions, not platform commands

    ;; Unknown gesture type
    (do
      (logging/log-warn {:msg "Unknown gesture type" :type gesture-type})
      nil)))

;; ============================================================================
;; High-level Command Sending Functions
;; ============================================================================

(>defn send-command-from-gesture
  "Send a command based on a gesture event.
  
  This is the main entry point for converting gestures to commands
  and sending them to the command subprocess."
  [gesture-event]
  [::specs/gesture-event => boolean?]
  (if-let [command (gesture-to-command gesture-event)]
    (do
      (logging/log-debug {:msg "Sending command from gesture"
                          :gesture-type (:gesture-type gesture-event)
                          :stream-type (:stream-type gesture-event)
                          :command command})
      (subprocess/send-message :cmd
                               (transit-core/create-message :command command)))
    true))

(>defn send-rotate-to-ndc
  "Send a rotate-to-NDC command (single tap handler).
  
  Convenience function that matches the TypeScript pattern."
  [stream-type ndc-x ndc-y]
  [::specs/stream-type number? number? => boolean?]
  (let [command (build-rotate-to-ndc-command stream-type ndc-x ndc-y)]
    (subprocess/send-message :cmd
                             (transit-core/create-message :command command))))

(>defn send-cv-track
  "Send a CV track command (double-tap handler).
  
  Convenience function that matches the TypeScript pattern."
  [stream-type ndc-x ndc-y frame-timestamp]
  [::specs/stream-type number? number? (? int?) => boolean?]
  (let [command (build-cv-track-command stream-type ndc-x ndc-y frame-timestamp)]
    (subprocess/send-message :cmd
                             (transit-core/create-message :command command))))

(>defn send-rotary-velocity
  "Send a rotary velocity command (pan handler).
  
  Used during pan gestures to control platform movement speed."
  [azimuth-speed elevation-speed azimuth-direction elevation-direction]
  [number? number? [:enum :clockwise :counter-clockwise]
   [:enum :clockwise :counter-clockwise] => boolean?]
  (let [command (build-rotary-velocity-command azimuth-speed elevation-speed
                                               azimuth-direction elevation-direction)]
    (subprocess/send-message :cmd
                             (transit-core/create-message :command command))))

(>defn send-rotary-halt
  "Send a rotary halt command (pan stop handler).
  
  Stops all platform movement when pan gesture ends."
  []
  [=> boolean?]
  (let [command (build-rotary-halt-command)]
    (subprocess/send-message :cmd
                             (transit-core/create-message :command command))))

;; ============================================================================
;; Navigation Event Handlers
;; ============================================================================

(>defn navigation-to-command
  "Convert a navigation (mouse) event into a command if applicable.
  
  Most navigation events are for UI feedback, but some may trigger commands."
  [{:keys [nav-type button click-count ndc-x ndc-y]}]
  [map? => (? map?)]
  ;; Currently navigation events are mostly for UI feedback
  ;; Double-clicks are handled as gestures instead
  nil)

;; ============================================================================
;; Window Event Handlers  
;; ============================================================================

(>defn window-event-to-action
  "Process window events and determine if any action is needed.
  
  Most window events are informational, but close events need handling."
  [{:keys [type stream-id]}]
  [map? => (? keyword?)]
  (case type
    :close :terminate-stream
    :minimize :pause-stream
    :restore :resume-stream
    nil))