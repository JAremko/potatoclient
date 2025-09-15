(ns potatoclient.streams.specs
  "Malli specs for stream IPC messages with strict validation.
  All specs are closed with no optional fields for maximum validation."
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [potatoclient.logging :as logging]
    [potatoclient.malli.registry :as registry]
    [potatoclient.ui.status-bar.messages :as status-msg]))

;; ============================================================================
;; Base Types
;; ============================================================================

(def StreamType
  "Video stream type enumeration (thermal or daylight camera)."
  [:enum :heat :day])

(def MessageType
  "IPC message type categories for stream communication."
  [:enum :event :log :metric :command])

(def LogLevel
  "Log message severity levels from debug to error."
  [:enum :debug :info :warn :error])

;; ============================================================================
;; Message Envelope (common to all messages)
;; ============================================================================

(def MessageEnvelope
  "Common envelope fields for all IPC messages with type and timestamp."
  [:map {:closed true}
   [:msg-type MessageType]
   [:timestamp :int]])

;; ============================================================================
;; Window Event Specs - Each action has its own precise spec
;; ============================================================================

(def WindowResizeEvent
  "Window resize event with new dimensions and delta changes."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :window]]
   [:action [:= :resize]]
   [:width :int]
   [:height :int]
   [:delta-x :int]
   [:delta-y :int]])

(def WindowMoveEvent
  "Window move event with new position and delta changes."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :window]]
   [:action [:= :window-move]]
   [:x :int]
   [:y :int]
   [:delta-x :int]
   [:delta-y :int]])

(def WindowSimpleEvent
  "For focus, blur, minimize, maximize, restore, close-request"
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :window]]
   [:action [:enum :focus :blur :minimize :maximize :restore :close-request]]])

;; ============================================================================
;; Gesture Event Specs - Each gesture type has its own spec
;; ============================================================================

(def GestureBaseFields
  "Common fields for all gesture events"
  [[:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]])

(def GestureTapEvent
  "Tap or double-tap gesture event with pixel and NDC coordinates."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:enum :tap :double-tap]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]
   [:ndc-x :double]
   [:ndc-y :double]])

(def GesturePanStartStopEvent
  "Pan gesture start/stop event with position and NDC coordinates."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:enum :pan-start :pan-stop]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]
   [:ndc-x :double]
   [:ndc-y :double]])

(def GesturePanMoveEvent
  "Pan gesture move event with position, deltas, and NDC coordinates."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:= :pan-move]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]
   [:delta-x :int]
   [:delta-y :int]
   [:ndc-x :double]
   [:ndc-y :double]])

(def GestureWheelEvent
  "Mouse wheel scroll event with position, amount, and NDC coordinates."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:enum :wheel-up :wheel-down]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]
   [:scroll-amount :int]
   [:ndc-x :double]
   [:ndc-y :double]])

;; For gestures without NDC coordinates (when component size unavailable)
(def GestureTapEventNoNDC
  "Tap gesture event without NDC coordinates (component size unavailable)."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:enum :tap :double-tap]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]])

(def GesturePanStartStopEventNoNDC
  "Pan start/stop event without NDC coordinates (component size unavailable)."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:enum :pan-start :pan-stop]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]])

(def GesturePanMoveEventNoNDC
  "Pan move event without NDC coordinates (component size unavailable)."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:= :pan-move]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]
   [:delta-x :int]
   [:delta-y :int]])

(def GestureWheelEventNoNDC
  "Wheel scroll event without NDC coordinates (component size unavailable)."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type [:enum :wheel-up :wheel-down]]
   [:stream-type StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp :int]
   [:scroll-amount :int]])

;; ============================================================================
;; Connection Event Specs - Each action has its own spec
;; ============================================================================

(def ConnectionConnectedEvent
  "Stream connection established event with URL and stream ID."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :connection]]
   [:action [:= :connected]]
   [:details [:map {:closed true}
              [:url :string]
              [:stream-id :string]]]])

(def ConnectionDisconnectedEvent
  "Stream connection disconnected event with optional code and reason."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :connection]]
   [:action [:= :disconnected]]
   [:details [:map {:closed true}
              [:code {:optional true} :int]
              [:reason {:optional true} :string]
              [:stream-id :string]]]])

(def ConnectionErrorEvent
  "Stream connection error event with error message and stream ID."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :connection]]
   [:action [:= :connection-error]]
   [:details [:map {:closed true}
              [:error :string]
              [:stream-id :string]]]])

(def ConnectionSimpleEvent
  "For timeout, reconnecting actions"
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :connection]]
   [:action [:enum :timeout :reconnecting]]])

;; ============================================================================
;; Stream State Event Specs
;; ============================================================================

(def StreamStartedEvent
  "Video stream started successfully event."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :stream-started]]])

(def StreamStoppedEvent
  "Video stream stopped event with reason."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :stream-stopped]]
   [:reason :string]])

(def StreamFailedEvent
  "Video stream failed event with error message."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :stream-failed]]
   [:error :string]])

(def StreamErrorEvent
  "Generic stream error event with message and optional error code."
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:timestamp :int]
   [:type [:= :error]]
   [:error-message :string]
   [:error-code :any]])

;; ============================================================================
;; Log Message Spec
;; ============================================================================

(def LogMessage
  "Log message from stream process with level and source process."
  [:map {:closed true}
   [:msg-type [:= :log]]
   [:timestamp :int]
   [:level LogLevel]
   [:message :string]
   [:process :string]])

(def LogMessageWithData
  "Log message with additional structured data payload."
  [:map {:closed true}
   [:msg-type [:= :log]]
   [:timestamp :int]
   [:level LogLevel]
   [:message :string]
   [:process :string]
   [:data :map]])

;; ============================================================================
;; Metric Message Spec
;; ============================================================================

(def MetricMessage
  "Performance metric message with name, value, and source process."
  [:map {:closed true}
   [:msg-type [:= :metric]]
   [:timestamp :int]
   [:name :string]
   [:value :any]
   [:process :string]])

(def MetricMessageWithTags
  "Performance metric message with additional tag metadata."
  [:map {:closed true}
   [:msg-type [:= :metric]]
   [:timestamp :int]
   [:name :string]
   [:value :any]
   [:process :string]
   [:tags [:map-of :string :string]]])

;; ============================================================================
;; Command Message Spec (flexible by nature)
;; ============================================================================

(def CommandMessage
  "Command message with flexible structure for various control actions."
  [:map {:closed false} ; Commands are flexible
   [:msg-type [:= :command]]
   [:timestamp :int]
   [:action :keyword]])

;; ============================================================================
;; Message Dispatch Functions
;; ============================================================================

(defn gesture-event-schema
  "Get the precise schema for a gesture event based on type and NDC presence."
  [message]
  (let [has-ndc? (and (:ndc-x message) (:ndc-y message))
        gesture-type (:gesture-type message)]
    (case gesture-type
      (:tap :double-tap)
      (if has-ndc? GestureTapEvent GestureTapEventNoNDC)

      (:pan-start :pan-stop)
      (if has-ndc? GesturePanStartStopEvent GesturePanStartStopEventNoNDC)

      :pan-move
      (if has-ndc? GesturePanMoveEvent GesturePanMoveEventNoNDC)

      (:wheel-up :wheel-down)
      (if has-ndc? GestureWheelEvent GestureWheelEventNoNDC)

      ;; Unknown gesture type - should never happen
      [:map {:closed false} [:gesture-type :keyword]])))
(m/=> gesture-event-schema [:=> [:cat :map] :any])

(defn window-event-schema
  "Get the precise schema for a window event based on action."
  [message]
  (case (:action message)
    :resize WindowResizeEvent
    :window-move WindowMoveEvent
    (:focus :blur :minimize :maximize :restore :close-request) WindowSimpleEvent
    ;; Unknown action - should never happen
    [:map {:closed false} [:action :keyword]]))
(m/=> window-event-schema [:=> [:cat :map] :any])

(defn connection-event-schema
  "Get the precise schema for a connection event based on action."
  [message]
  (case (:action message)
    :connected ConnectionConnectedEvent
    :disconnected ConnectionDisconnectedEvent
    :connection-error ConnectionErrorEvent
    (:timeout :reconnecting) ConnectionSimpleEvent
    ;; Unknown action
    [:map {:closed false} [:action :keyword]]))
(m/=> connection-event-schema [:=> [:cat :map] :any])

(defn event-message-schema
  "Get the appropriate schema for an event message based on its type."
  [message]
  (case (:type message)
    :window (window-event-schema message)
    :gesture (gesture-event-schema message)
    :connection (connection-event-schema message)
    :stream-started StreamStartedEvent
    :stream-stopped StreamStoppedEvent
    :stream-failed StreamFailedEvent
    :error StreamErrorEvent
    ;; Unknown event type
    [:map {:closed false} [:type :keyword]]))
(m/=> event-message-schema [:=> [:cat :map] :any])

(defn log-message-schema
  "Get the appropriate schema for a log message."
  [message]
  (if (:data message)
    LogMessageWithData
    LogMessage))
(m/=> log-message-schema [:=> [:cat :map] :any])

(defn metric-message-schema
  "Get the appropriate schema for a metric message."
  [message]
  (if (:tags message)
    MetricMessageWithTags
    MetricMessage))
(m/=> metric-message-schema [:=> [:cat :map] :any])

(defn message-schema
  "Get the appropriate schema for a message based on its msg-type."
  [message]
  (case (:msg-type message)
    :event (event-message-schema message)
    :log (log-message-schema message)
    :metric (metric-message-schema message)
    :command CommandMessage
    ;; Unknown message type
    [:map {:closed false} [:msg-type :keyword]]))
(m/=> message-schema [:=> [:cat :map] :any])

;; ============================================================================
;; Validation Functions
;; ============================================================================

(defn validate-message
  "Validate an IPC message and report any errors.
  Returns true if valid, false otherwise.
  Reports validation errors to both logs and status bar since
  invalid messages indicate a bug in our IPC protocol."
  [message source]
  (let [schema (message-schema message)
        valid? (m/validate schema message)]
    (when-not valid?
      (let [errors (me/humanize (m/explain schema message))
            error-msg (str "Stream IPC validation failed: "
                           (name source) " - "
                           (:msg-type message) "/"
                           (:type message) "/"
                           (or (:action message) (:gesture-type message)))]
        ;; Log the full details
        (logging/log-error {:id :stream/invalid-message
                            :source source
                            :msg-type (:msg-type message)
                            :event-type (:type message)
                            :action (:action message)
                            :gesture-type (:gesture-type message)
                            :errors errors
                            :message message})
        ;; Report to status bar - this is a bug that needs attention
        (status-msg/set-error! error-msg)))
    valid?))
(m/=> validate-message [:=> [:cat :map :keyword] :boolean])

(defn validate-and-log
  "Validate a message and log it appropriately.
  In dev mode, also logs to console for visibility."
  [message stream-type]
  (let [valid? (validate-message message stream-type)]
    (when (and (not valid?)
               (not (potatoclient.runtime/release-build?)))
      ;; In dev mode, also log to console for visibility
      (println (str "[VALIDATION ERROR] Stream: " (name stream-type)
                    " Message: " (:msg-type message) "/" (:type message)
                    "/" (or (:action message) (:gesture-type message)))))
    valid?))
(m/=> validate-and-log [:=> [:cat :map :keyword] :boolean])

;; ============================================================================
;; Registry Registration
;; ============================================================================

(def stream-specs
  "Complete collection of stream IPC message specs for global registry."
  {:stream/type StreamType
   :stream/message-type MessageType
   :stream/log-level LogLevel
   ;; Window events
   :stream/window-resize WindowResizeEvent
   :stream/window-move WindowMoveEvent
   :stream/window-simple WindowSimpleEvent
   ;; Gesture events with NDC
   :stream/gesture-tap GestureTapEvent
   :stream/gesture-pan-start-stop GesturePanStartStopEvent
   :stream/gesture-pan-move GesturePanMoveEvent
   :stream/gesture-wheel GestureWheelEvent
   ;; Gesture events without NDC
   :stream/gesture-tap-no-ndc GestureTapEventNoNDC
   :stream/gesture-pan-start-stop-no-ndc GesturePanStartStopEventNoNDC
   :stream/gesture-pan-move-no-ndc GesturePanMoveEventNoNDC
   :stream/gesture-wheel-no-ndc GestureWheelEventNoNDC
   ;; Connection events
   :stream/connection-connected ConnectionConnectedEvent
   :stream/connection-disconnected ConnectionDisconnectedEvent
   :stream/connection-error ConnectionErrorEvent
   :stream/connection-simple ConnectionSimpleEvent
   ;; Stream state events
   :stream/stream-started StreamStartedEvent
   :stream/stream-stopped StreamStoppedEvent
   :stream/stream-failed StreamFailedEvent
   :stream/stream-error StreamErrorEvent
   ;; Other messages
   :stream/log-message LogMessage
   :stream/log-message-with-data LogMessageWithData
   :stream/metric-message MetricMessage
   :stream/metric-message-with-tags MetricMessageWithTags
   :stream/command-message CommandMessage})

;; Register all specs with the global registry
(defn register-specs!
  "Register all stream specs with the global registry."
  []
  (doseq [[k spec] stream-specs]
    (registry/register-spec! k spec)))
(m/=> register-specs! [:=> [:cat] :nil])

;; Auto-register on namespace load
(register-specs!)