(ns potatoclient.streams.specs
  "Malli specs for stream IPC messages and validation."
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [potatoclient.logging :as logging]
    [potatoclient.malli.registry :as registry]
    [potatoclient.ui.status-bar.messages :as status-msg]))

;; ============================================================================
;; Base Specs
;; ============================================================================

(def StreamType
  [:enum :heat :day])

(def MessageType
  [:enum :event :log :metric :command])

(def LogLevel
  [:enum :debug :info :warn :error])

;; ============================================================================
;; Window Event Specs
;; ============================================================================

(def WindowAction
  [:enum :minimize :maximize :restore :resize :focus :blur :window-move :close-request])

(def WindowEventMessage
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:type [:= :window]]
   [:action WindowAction]
   ;; Optional fields depending on action
   [:width {:optional true} :int]
   [:height {:optional true} :int]
   [:x {:optional true} :int]
   [:y {:optional true} :int]
   [:delta-x {:optional true} :int]
   [:delta-y {:optional true} :int]])

;; ============================================================================
;; Gesture Event Specs
;; ============================================================================

(def GestureType
  [:enum :tap :double-tap :pan-start :pan-move :pan-stop :wheel-up :wheel-down])

(def GestureEventMessage
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:type [:= :gesture]]
   [:gesture-type GestureType]
   [:stream-type {:optional true} StreamType]
   [:x :int]
   [:y :int]
   [:frame-timestamp {:optional true} :int]
   ;; Optional NDC coordinates
   [:ndc-x {:optional true} :double]
   [:ndc-y {:optional true} :double]
   ;; Optional for pan gestures
   [:delta-x {:optional true} :int]
   [:delta-y {:optional true} :int]
   ;; Optional for wheel gestures
   [:scroll-amount {:optional true} :int]])

;; ============================================================================
;; Connection Event Specs
;; ============================================================================

(def ConnectionAction
  [:enum :connected :disconnected :timeout :reconnecting :connection-error])

(def ConnectionDetails
  [:map {:closed true}
   [:url {:optional true} :string]
   [:stream-id {:optional true} :string]
   [:code {:optional true} :int]
   [:reason {:optional true} :string]
   [:error {:optional true} :string]])

(def ConnectionEventMessage
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:type [:= :connection]]
   [:action ConnectionAction]
   [:details {:optional true} ConnectionDetails]])

;; ============================================================================
;; Stream State Event Specs
;; ============================================================================

(def StreamStateEventMessage
  [:map {:closed true}
   [:msg-type [:= :event]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:type [:enum :stream-started :stream-stopped :stream-failed :error]]
   ;; Optional fields for different event types
   [:reason {:optional true} :string]
   [:error {:optional true} :string]
   [:error-message {:optional true} :string]
   [:error-code {:optional true} :any]])

;; ============================================================================
;; Log Message Spec
;; ============================================================================

(def LogMessage
  [:map {:closed true}
   [:msg-type [:= :log]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:level LogLevel]
   [:message :string]
   [:process {:optional true} :string]
   [:data {:optional true} :map]])

;; ============================================================================
;; Metric Message Spec
;; ============================================================================

(def MetricMessage
  [:map {:closed true}
   [:msg-type [:= :metric]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:name :string]
   [:value :any]
   [:process {:optional true} :string]
   [:tags {:optional true} [:map-of :string :string]]])

;; ============================================================================
;; Command Message Spec
;; ============================================================================

(def CommandMessage
  [:map {:closed true}
   [:msg-type [:= :command]]
   [:msg-id {:optional true} :string]
   [:timestamp :int]
   [:action :keyword]
   ;; Command-specific data is flexible
   [:* [:any :any]]])

;; ============================================================================
;; Event Message Dispatch
;; ============================================================================

(defn event-message-schema
  "Get the appropriate schema for an event message based on its type."
  [message]
  (case (:type message)
    :window WindowEventMessage
    :gesture GestureEventMessage
    :connection ConnectionEventMessage
    (:stream-started :stream-stopped :stream-failed :error) StreamStateEventMessage
    ;; Unknown event type - use a generic schema
    [:map {:closed false}
     [:msg-type [:= :event]]
     [:type :keyword]]))

;; ============================================================================
;; Message Dispatch
;; ============================================================================

(defn message-schema
  "Get the appropriate schema for a message based on its msg-type."
  [message]
  (case (:msg-type message)
    :event (event-message-schema message)
    :log LogMessage
    :metric MetricMessage
    :command CommandMessage
    ;; Unknown message type
    [:map {:closed false}
     [:msg-type :keyword]]))

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
                           (:type message))]
        ;; Log the full details
        (logging/log-error {:id :stream/invalid-message
                            :source source
                            :msg-type (:msg-type message)
                            :event-type (:type message)
                            :errors errors
                            :message message})
        ;; Report to status bar - this is a bug that needs attention
        (status-msg/set-error! error-msg)))
    valid?))

(defn validate-and-log
  "Validate a message and log it appropriately based on environment.
  In dev mode, logs all validation failures.
  In production, only logs critical validation failures."
  [message stream-type]
  (let [valid? (validate-message message stream-type)]
    (when (and (not valid?)
               (not (potatoclient.runtime/release-build?)))
      ;; In dev mode, also log to console for visibility
      (println (str "[VALIDATION ERROR] Stream: " (name stream-type)
                    " Message type: " (:msg-type message)
                    " Event type: " (:type message))))
    valid?))

;; ============================================================================
;; Registry Registration
;; ============================================================================

(def stream-specs
  "All stream-related specs for registry."
  {:stream/type StreamType
   :stream/message-type MessageType
   :stream/log-level LogLevel
   :stream/window-action WindowAction
   :stream/window-event WindowEventMessage
   :stream/gesture-type GestureType
   :stream/gesture-event GestureEventMessage
   :stream/connection-action ConnectionAction
   :stream/connection-event ConnectionEventMessage
   :stream/stream-state-event StreamStateEventMessage
   :stream/log-message LogMessage
   :stream/metric-message MetricMessage
   :stream/command-message CommandMessage})

;; Register all specs with the global registry
(defn register-specs!
  "Register all stream specs with the global registry."
  []
  (doseq [[k spec] stream-specs]
    (registry/register! k spec)))

;; Auto-register on namespace load
(register-specs!)