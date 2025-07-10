(ns potatoclient.events.stream
  "Stream-specific event handling (window, navigation, responses)"
  (:require [potatoclient.events.log :as log]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [clojure.data.json :as json]
            [seesaw.core :as seesaw]))

(defn format-window-event
  "Format a window event for display"
  [event]
  (let [type (:type event)
        details (dissoc event :type)]
    (case type
      "resized" (str "Resized to " (:width details) "x" (:height details))
      "moved" (str "Moved to (" (:x details) ", " (:y details) ")")
      "maximized" "Maximized"
      "unmaximized" "Restored from maximized"
      "minimized" "Minimized to taskbar"
      "restored" "Restored from taskbar"
      "focused" "Got focus"
      "unfocused" "Lost focus"
      "opened" "Window opened"
      "closing" "Window closing"
      (str type " " (json/write-str details)))))

(defn format-navigation-event
  "Format a navigation/mouse event for display"
  [event]
  (let [type (:type event)
        x (:x event)
        y (:y event)
        ndc-x (:ndcX event)
        ndc-y (:ndcY event)
        button (:button event)
        click-count (:clickCount event)
        wheel (:wheelRotation event)
        ; Format coordinates with NDC if available
        coords (if (and ndc-x ndc-y)
                 (format "%d,%d [NDC: %.3f,%.3f]" x y ndc-x ndc-y)
                 (str x "," y))]
    (case type
      "mouse-click" (str "Click (" (case button 1 "Left" 2 "Middle" 3 "Right" button) 
                        (if (> click-count 1) (str " x" click-count) "") ") @ " coords)
      "mouse-press" (str "Press (" (case button 1 "Left" 2 "Middle" 3 "Right" button) ") @ " coords)
      "mouse-release" (str "Release (" (case button 1 "Left" 2 "Middle" 3 "Right" button) ") @ " coords)
      "mouse-move" (str "Move @ " coords)
      "mouse-drag-start" (str "Drag start @ " coords)
      "mouse-drag" (str "Dragging @ " coords)
      "mouse-drag-end" (str "Drag end @ " coords)
      "mouse-enter" (str "Mouse entered @ " coords)
      "mouse-exit" (str "Mouse exited @ " coords)
      "mouse-wheel" (str "Wheel " (if (pos? wheel) "down" "up") " " (Math/abs wheel) " @ " coords)
      (str type " @ " coords))))

(defn handle-response-event
  "Handle a response event from a stream"
  [stream-key msg]
  (log/add-log-entry! {:time (System/currentTimeMillis)
                       :stream (:streamId msg)
                       :type "RESPONSE"
                       :message (:status msg)
                       :raw-data msg})
  
  ;; Handle window-closed event - terminate the process
  (when (= (:status msg) "window-closed")
    (when-let [stream (state/get-stream stream-key)]
      (future
        (process/stop-stream stream)
        (state/clear-stream! stream-key)
        (seesaw/invoke-later
          (when-let [btn (state/get-ui-element stream-key)]
            (seesaw/text! btn (str (name stream-key) " Stream OFF"))
            (seesaw/config! btn :selected? false)))))))

(defn handle-navigation-event
  "Handle a navigation/mouse event"
  [msg]
  (log/add-log-entry! {:time (System/currentTimeMillis)
                       :stream (:streamId msg)
                       :type "NAV"
                       :message (format-navigation-event (:event msg))
                       :raw-data msg
                       :nav-type (get-in msg [:event :type])}))

(defn handle-window-event
  "Handle a window event"
  [msg]
  (log/add-log-entry! {:time (System/currentTimeMillis)
                       :stream (:streamId msg)
                       :type "WINDOW"
                       :message (format-window-event (:event msg))
                       :raw-data msg
                       :event-type (get-in msg [:event :type])}))