(ns potatoclient.events.stream
  "Event handling for video stream windows"
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
   [potatoclient.logging :as logging]
   [seesaw.core :as seesaw]
   [potatoclient.state :as state]
   [potatoclient.process :as process]
   [potatoclient.specs])
  (:import
   [javax.swing JFrame JDialog]))

;; Event type definitions
(def window-event-types
  #{:resized :moved :maximized :unmaximized :minimized
    :restored :focused :unfocused :opened :closing})

(def mouse-event-types
  #{:mouse-click :mouse-press :mouse-release :mouse-move
    :mouse-drag-start :mouse-drag :mouse-drag-end
    :mouse-enter :mouse-exit :mouse-wheel})

(def mouse-button-names
  {1 "Left" 2 "Middle" 3 "Right"})

;; Window event formatting
(>defn- format-dimensions
        [width height]
        [int? int? => string?]
        (str width "x" height))

(>defn- format-position
        [x y]
        [int? int? => string?]
        (format "(%d, %d)" x y))

(>defn- format-window-event-details
        "Format window event based on type."
        [{:keys [type width height x y]}]
        [map? => string?]
        (case type
          :resize (str "Resized to " (format-dimensions width height))
          :move (str "Moved to " (format-position x y))
          :focus-gained "Gained focus"
          :focus-lost "Lost focus"
          :minimized "Minimized"
          :restored "Restored"
          :maximized "Maximized"
          :closed "Closed"
          (str type)))

(>defn format-window-event
       "Format a window event for display."
       [event]
       [map? => string?]
       (format-window-event-details event))

;; Navigation event formatting
(>defn- format-coordinates
        "Format coordinates with optional NDC values."
        [{:keys [x y ndcX ndcY]}]
        [map? => string?]
        (if (and ndcX ndcY)
          (format "(%d,%d) [%.3f,%.3f]" x y ndcX ndcY)
          (format "(%d,%d)" x y)))

(>defn- format-mouse-button
        "Format mouse button name."
        [button]
        [int? => string?]
        (get mouse-button-names button (str "Button " button)))

(>defn- format-click-count
        "Format click count if multiple."
        [count]
        [int? => string?]
        (if (> count 1)
          (str " x" count)
          ""))

(>defn- format-wheel-direction
        "Format mouse wheel direction."
        [rotation]
        [number? => string?]
        (if (< rotation 0) "up" "down"))

(>defn- format-navigation-event-details
        "Format navigation event based on type."
        [{:keys [type button clickCount wheelRotation] :as event}]
        [map? => string?]
        (let [coords (format-coordinates event)]
          (case type
            :mouse-click
            (str "Click (" (format-mouse-button button)
                 (format-click-count clickCount) ") @ " coords)

            :mouse-press
            (str "Press (" (format-mouse-button button) ") @ " coords)

            :mouse-release
            (str "Release (" (format-mouse-button button) ") @ " coords)

            :mouse-move
            (str "Move @ " coords)

            :mouse-drag-start
            (str "Drag start @ " coords)

            :mouse-drag
            (str "Dragging @ " coords)

            :mouse-drag-end
            (str "Drag end @ " coords)

            :mouse-enter
            (str "Mouse entered @ " coords)

            :mouse-exit
            (str "Mouse exited @ " coords)

            :mouse-wheel
            (str "Wheel " (format-wheel-direction wheelRotation) " "
                 (Math/abs wheelRotation) " @ " coords)

      ;; Unknown type
            (str type " @ " coords))))

(>defn format-navigation-event
       "Format a navigation/mouse event for display."
       [event]
       [map? => string?]
       (format-navigation-event-details event))

;; Event handlers
(>defn- handle-window-closed
        "Handle window closed event - terminate the associated process."
        [stream-key]
        [:potatoclient.specs/stream-key => nil?]
        (logging/log-stream-event stream-key :window-closed
                                  {:message "Stream window closed by X button"})
        (when-let [stream (state/get-stream stream-key)]
          (future
            (try
              ;; Send shutdown command and stop the stream
              (process/send-command stream {:action "shutdown"})
              (Thread/sleep 100)
              (process/stop-stream stream)
              (state/clear-stream! stream-key)
              (catch Exception e
                (logging/log-error (str "Error terminating " (name stream-key) " stream: " (.getMessage e))))))
          nil))

(>defn handle-response-event
       "Handle a response event from a stream."
       [stream-key msg]
       [:potatoclient.specs/stream-key map? => nil?]
       (logging/log-stream-event stream-key :response
                                 {:status (:status msg)
                                  :data msg})

  ;; Handle specific response types
       (when (= (:status msg) "window-closed")
         (handle-window-closed stream-key))
       nil)

(>defn handle-navigation-event
       "Handle a navigation/mouse event."
       [msg]
       [map? => nil?]
       (let [event (:event msg)]
         (logging/log-stream-event (:streamId msg) :navigation
                                   {:nav-type (:type event)
                                    :message (format-navigation-event event)
                                    :data event})
         nil))

(>defn handle-window-event
       "Handle a window event."
       [msg]
       [map? => nil?]
       (let [event (:event msg)]
         (logging/log-stream-event (:streamId msg) :window
                                   {:event-type (:type event)
                                    :message (format-window-event event)
                                    :data event})
         nil))

;; Utility functions
(>defn stream-connected?
       "Check if a stream is currently connected."
       [stream-key]
       [:potatoclient.specs/stream-key => boolean?]
       (some? (state/get-stream stream-key)))

(>defn all-streams-connected?
       "Check if all streams are connected."
       []
       [=> boolean?]
       (and (stream-connected? :heat)
            (stream-connected? :day)))

