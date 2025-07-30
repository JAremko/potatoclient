(ns potatoclient.events.stream
  "Event handling for video stream windows"
  (:require
    [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]
    [potatoclient.logging :as logging]
    [potatoclient.process :as process]
    [potatoclient.specs]
    [potatoclient.state :as state]
    [potatoclient.transit.app-db :as app-db]))

(def mouse-button-names
  "Mapping of mouse button numbers to human-readable names"
  {1 "Left" 2 "Middle" 3 "Right"})

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
  [stream-key]
  [:potatoclient.specs/stream-key => nil?]
  (logging/log-stream-event stream-key :window-closed
                            {:message "Stream window closed by X button"})
  (logging/log-info {:msg (str "Attempting to get stream process for " stream-key)})
  
  ;; Get the full stream process map from app-db instead of the simplified state
  (if-let [stream (app-db/get-stream-process stream-key)]
    (do
      (logging/log-info {:msg (str "Found stream process for " stream-key ", sending shutdown command")})
      (future
        (try
          ;; Send shutdown command and stop the stream
          (logging/log-info {:msg (str "Sending shutdown command to " stream-key)})
          (let [cmd-result (process/send-command stream {:action "shutdown"})]
            (logging/log-info {:msg (str "Shutdown command result for " stream-key ": " cmd-result)}))
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
      (logging/log-info {:msg (str "Stream " stream-key " already stopped/removed")})))
  nil)

(>defn handle-response-event
  "Handle a response event from a stream."
  [stream-key msg]
  [:potatoclient.specs/stream-key map? => nil?]
  (logging/log-stream-event stream-key :response
                            {:action (get msg "action")
                             :data msg})
  
  ;; DEBUG: Log the actual message structure
  (logging/log-debug {:msg (str "Response event - stream-key: " stream-key 
                                ", action: " (get msg "action")
                                ", full msg: " (pr-str msg))})

  ;; Handle specific response types
  (when (= (get msg "action") "window-closed")
    (logging/log-info {:msg (str "Window close detected for " (name stream-key))})
    (handle-window-closed stream-key))
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
          ;; TODO: Implement CV tracking in Transit architecture
          ;; In the new architecture, we would send a CV tracking command via Transit
          (logging/log-warn {:msg "CV tracking not yet implemented in Transit architecture"}))))
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
