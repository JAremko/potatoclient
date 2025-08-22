(ns clj-stream-spawner.events
  "Event handling for IPC messages from video streams."
  (:require 
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
   [malli.core :as m]
   [taoensso.telemere :as t]))

;; ============================================================================
;; Specs
;; ============================================================================

(def StreamType
  [:enum :heat :day])

(def Message
  [:map-of :keyword :any])

;; ============================================================================
;; Event Handlers
;; ============================================================================

(defmulti handle-event
  "Handle event messages based on event type."
  (fn [stream-type message]
    (:type message)))

(defmethod handle-event :gesture
  [stream-type {:keys [gesture-type x y ndc-x ndc-y scroll-amount] :as message}]
  (let [base-msg (format "[%s-GESTURE] %s at pixel(%s, %s)"
                         (.toUpperCase (name stream-type))
                         (name gesture-type)
                         x y)
        ndc-msg (if (and ndc-x ndc-y)
                  (format " ndc(%.3f, %.3f)" ndc-x ndc-y)
                  "")
        scroll-msg (if scroll-amount
                     (format " scroll:%d" scroll-amount)
                     "")]
    (println (str base-msg ndc-msg scroll-msg))))

(defmethod handle-event :window
  [stream-type {:keys [action width height x y delta-x delta-y] :as message}]
  (let [details (cond
                  (and width height) (format " %dx%d" width height)
                  (and x y) (format " at (%d, %d)" x y)
                  (and delta-x delta-y) (format " delta(%d, %d)" delta-x delta-y)
                  :else "")]
    (println (format "[%s-WINDOW] %s%s"
                     (.toUpperCase (name stream-type))
                     (name action)
                     details))))

(defmethod handle-event :connection
  [stream-type {:keys [action details] :as message}]
  (println (format "[%s-CONNECTION] %s%s"
                   (.toUpperCase (name stream-type))
                   (name action)
                   (if details (str " - " details) ""))))

(defmethod handle-event :default
  [stream-type message]
  (t/log! :warn (format "[%s] Unknown event type: %s"
                        (name stream-type)
                        (:type message))))

;; ============================================================================
;; Log Handler
;; ============================================================================

(>defn handle-log
  "Handle log messages from streams."
  [stream-type message]
  [StreamType Message => :any]
  (let [{:keys [level message data]} message
        level-str (when level (.toUpperCase (name level)))
        data-str (when data (str " | " data))]
    (println (format "[%s-LOG] [%s] %s%s"
                     (.toUpperCase (name stream-type))
                     (or level-str "INFO")
                     message
                     (or data-str "")))))

;; ============================================================================
;; Metric Handler
;; ============================================================================

(>defn handle-metric
  "Handle metric messages from streams."
  [stream-type message]
  [StreamType Message => :any]
  (println (format "[%s-METRIC] %s"
                   (.toUpperCase (name stream-type))
                   (dissoc message :msg-type :timestamp))))

;; ============================================================================
;; Command Handler
;; ============================================================================

(>defn handle-command
  "Handle command messages from streams.
   These are typically commands that need to be forwarded to the server."
  [stream-type message]
  [StreamType Message => :any]
  (let [{:keys [action] :as payload} (dissoc message :msg-type :timestamp)]
    (println (format "[%s-COMMAND] %s: %s"
                     (.toUpperCase (name stream-type))
                     (name action)
                     payload))
    ;; In a real implementation, you would forward these to the server
    ;; For now, just log them
    (t/log! :info (str "Command received from " (name stream-type) ": " action))))