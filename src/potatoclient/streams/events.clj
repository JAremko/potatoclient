(ns potatoclient.streams.events
  "Event handling for stream IPC messages."
  (:require
    [potatoclient.logging :as logging]
    [potatoclient.streams.state :as state]
    [potatoclient.streams.specs :as specs]
    [potatoclient.ui.status-bar.messages :as status-msg]))

;; ============================================================================
;; Event Dispatch
;; ============================================================================

(defmulti handle-event
  "Handle events from stream processes"
  (fn [stream-type message] (:type message)))

;; ============================================================================
;; Connection Events
;; ============================================================================

(defmethod handle-event :connection
  [stream-type {:keys [action details timestamp]}]
  (logging/log-info {:id :stream/connection-event
                     :stream stream-type
                     :action action
                     :details details})
  (case action
    :connected
    (do
      (state/set-stream-status! stream-type :running nil)
      (when-let [url (:url details)]
        (logging/log-info {:id :stream/connected
                           :stream stream-type
                           :url url})))

    (:disconnected :timeout :connection-error)
    (do
      (state/set-stream-status! stream-type :stopped nil)
      (when-let [error (:error details)]
        (state/set-stream-error! stream-type error)))

    :reconnecting
    (logging/log-info {:id :stream/reconnecting
                       :stream stream-type
                       :details details})

    ;; Unknown action
    nil))

;; ============================================================================
;; Gesture Events
;; ============================================================================

(defmethod handle-event :gesture
  [stream-type {:keys [gesture-type x y ndc-x ndc-y scroll-amount timestamp]}]
  (logging/log-debug (cond-> {:id :stream/gesture-event
                              :stream stream-type
                              :gesture gesture-type
                              :coords {:x x :y y}}
                       (and ndc-x ndc-y) (assoc :ndc {:x ndc-x :y ndc-y})
                       scroll-amount (assoc :scroll scroll-amount))))

;; ============================================================================
;; Window Events
;; ============================================================================

(defmethod handle-event :window
  [stream-type {:keys [action width height x y delta-x delta-y timestamp]}]
  (logging/log-debug (cond-> {:id :stream/window-event
                              :stream stream-type
                              :action action}
                       (and x y) (assoc :position {:x x :y y})
                       (and width height) (assoc :size {:width width :height height})
                       (and delta-x delta-y) (assoc :delta {:x delta-x :y delta-y})))
  ;; Store window position if moved/resized
  (when (and (= action :window-move) x y)
    (state/set-stream-window-position! stream-type x y))
  (when (and (= action :resize) width height)
    (state/set-stream-window-size! stream-type width height)))

;; ============================================================================
;; Error Events
;; ============================================================================

(defmethod handle-event :error
  [stream-type {:keys [error-message error-code timestamp]}]
  (logging/log-error {:id :stream/error-event
                      :stream stream-type
                      :error error-message
                      :code error-code})
  (state/set-stream-error! stream-type error-message))

;; ============================================================================
;; Stream State Events
;; ============================================================================

(defmethod handle-event :stream-started
  [stream-type {:keys [timestamp]}]
  (logging/log-info {:id :stream/started
                     :stream stream-type})
  (state/set-stream-status! stream-type :running))

(defmethod handle-event :stream-stopped
  [stream-type {:keys [reason timestamp]}]
  (logging/log-info {:id :stream/stopped
                     :stream stream-type
                     :reason reason})
  (state/set-stream-status! stream-type :stopped))

(defmethod handle-event :stream-failed
  [stream-type {:keys [error timestamp]}]
  (logging/log-error {:id :stream/failed
                      :stream stream-type
                      :error error})
  (state/set-stream-error! stream-type error))

;; ============================================================================
;; Default Handler
;; ============================================================================

(defmethod handle-event :default
  [stream-type message]
  (logging/log-warn {:id :stream/unknown-event
                     :stream stream-type
                     :message message}))

;; ============================================================================
;; Message Router
;; ============================================================================

(defn handle-message
  "Route messages from stream IPC"
  {:malli/schema [:=> [:cat :keyword :map] :nil]}
  [stream-type message]
  ;; Validate message structure
  (when (specs/validate-and-log message stream-type)
    (case (:msg-type message)
      :event (handle-event stream-type message)

      :log (let [{:keys [level message timestamp]} message]
             (logging/log-info {:id :stream/log
                                :stream stream-type
                                :level level
                                :msg message}))

      :metric (let [{:keys [name value timestamp]} message]
                (logging/log-debug {:id :stream/metric
                                    :stream stream-type
                                    :metric name
                                    :value value}))

      :command (logging/log-debug {:id :stream/command
                                   :stream stream-type
                                   :command message})

      ;; Unknown message type
      (logging/log-warn {:id :stream/unknown-message
                         :stream stream-type
                         :message message})))
  nil)