(ns potatoclient.events.stream
  "Event handling for video stream windows"
  (:require
    [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
    [potatoclient.logging :as logging]
    [potatoclient.process :as process]
    [potatoclient.state :as state]
    [potatoclient.transit.app-db :as app-db]
    [potatoclient.ui-specs]))

(def mouse-button-names
  "Mapping of mouse button numbers to human-readable names"
  {1 "Left" 2 "Middle" 3 "Right"})

(>defn- format-dimensions
  "Format width and height as a dimension string."
  [width height]
  [int? int? => string?]
  (str width "x" height))

(>defn- format-position
  "Format x,y coordinates as a position string."
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
           (^[double] Math/abs wheelRotation) " @ " coords)

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
  [stream-id]
  [string? => nil?]
  ;; Convert stream-id (e.g., "heat-video") to stream-key (e.g., :heat)
  (let [stream-key (case stream-id
                     "heat-video" :heat
                     "day-video" :day
                     ;; Default case - try to parse it
                     (keyword (first (clojure.string/split stream-id #"-"))))]
    (logging/log-stream-event stream-key :window-closed
                              {:message "Stream window closed by X button"
                               :stream-id stream-id})
    (logging/log-info {:msg (str "Attempting to get stream process for " stream-key " (stream-id: " stream-id ")")})

    ;; Get the full stream process map from app-db instead of the simplified state
    (if-let [stream (app-db/get-stream-process stream-key)]
      (do
        (logging/log-info {:msg (str "Found stream process for " stream-key ", sending shutdown command")})
        (future
          (try
            ;; Send shutdown control message and stop the stream
            (logging/log-info {:msg (str "Sending shutdown control message to " stream-key)})
            (let [cmd-result (process/send-control stream {:action "shutdown"})]
              (logging/log-info {:msg (str "Shutdown control result for " stream-key ": " cmd-result)}))
            (Thread/sleep 100)

            (logging/log-info {:msg (str "Stopping stream process for " stream-key)})
            (let [stop-result (process/stop-stream stream)]
              (logging/log-info {:msg (str "Stop stream result for " stream-key ": " stop-result)}))

            (logging/log-info {:msg (str "Clearing stream state for " stream-key)})
            (state/clear-stream! stream-key)
            (app-db/remove-stream-process! stream-key)
            (logging/log-info {:msg (str "Successfully terminated " stream-key " stream via X button")})
            (catch Exception e
              (logging/log-error {:msg (str "Error terminating " (name stream-key) " stream: " (.getMessage e))})))))
      ;; Check if stream was already stopped
      (if-let [state-info (state/get-stream stream-key)]
        (logging/log-info {:msg (str "Stream " stream-key " already being stopped, state: " state-info)})
        (logging/log-info {:msg (str "Stream " stream-key " already stopped/removed")}))))
  nil)

(>defn handle-response-event
  "Handle a response event from a stream."
  [stream-key msg]
  [:potatoclient.ui-specs/stream-key map? => nil?]
  (logging/log-stream-event stream-key :response
                            {:action (:action msg)
                             :data msg})

  ;; DEBUG: Log the actual message structure
  (logging/log-debug {:msg (str "Response event - stream-key: " stream-key
                                ", action: " (:action msg)
                                ", full msg: " (pr-str msg))})

  ;; Handle specific response types
  (when (= (:action msg) "window-closed")
    (logging/log-info {:msg (str "Window close detected for " (name stream-key))})
    ;; For response events, we already have the stream-key, convert to stream-id
    (let [stream-id (case stream-key
                      :heat "heat-video"
                      :day "day-video"
                      (str (name stream-key) "-video"))]
      (handle-window-closed stream-id)))
  nil)

(>defn handle-navigation-event
  "Handle a navigation/mouse event."
  [msg]
  [map? => nil?]
  (let [event (:event msg)
        stream-id (:streamId msg)]
    (when event
      (logging/log-stream-event stream-id :navigation
                                {:nav-type (:type event)
                                 :message (format-navigation-event event)
                                 :data event})

      ;; Check for double-click (left button with clickCount > 1)
      (when (and (= (:type event) :mouse-click)
                 (= (:button event) 1)  ; Left button
                 (> (:clickCount event) 1))  ; Double-click
        (let [{:keys [ndcX ndcY frameTimestamp]} event]
          (logging/log-info {:msg (str "Double-click detected at NDC (" ndcX ", " ndcY ") "
                                       "with frame timestamp: " frameTimestamp
                                       " on " (name stream-id) " channel")})
          ;; CV tracking is now handled by gesture system
          ;; Double-tap gestures are detected in VideoStreamManager and sent as gesture events
          ;; The gesture handler (gestures.handler/handle-double-tap-gesture) sends the cv-start-track-ndc command
          (logging/log-debug {:msg "Double-click detected - should be handled as double-tap gesture"}))))
    nil))

(>defn handle-window-event
  "Handle a window event."
  [msg]
  [map? => nil?]
  ;; Window events have the event data directly in the payload
  (let [window-type (:type msg)
        stream-id (:streamId msg)]
    (logging/log-stream-event stream-id :window
                              {:event-type window-type
                               :message (str "Window event: " window-type)
                               :data msg})
    ;; Check if this is a window close event
    (when (= window-type :close)
      (logging/log-info {:msg (str "Window close event detected for " (name stream-id))})
      (handle-window-closed stream-id))
    nil))
