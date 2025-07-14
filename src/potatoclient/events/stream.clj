(ns potatoclient.events.stream
  "Stream-specific event handling for video windows.
  
  Processes window events, navigation/mouse events, and stream responses
  from the video subprocesses."
  (:require [potatoclient.events.log :as log]
            [potatoclient.state :as state]
            [potatoclient.process :as process]
            [clojure.data.json :as json]
            [seesaw.core :as seesaw]
            [clojure.spec.alpha :as s]))

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

;; Specs
(s/def ::event-type keyword?)
(s/def ::x number?)
(s/def ::y number?)
(s/def ::ndcX number?)
(s/def ::ndcY number?)
(s/def ::button #{1 2 3})
(s/def ::clickCount pos-int?)
(s/def ::wheelRotation number?)

(s/def ::window-event
  (s/keys :req-un [::type]
          :opt-un [::width ::height ::x ::y]))

(s/def ::navigation-event
  (s/keys :req-un [::type ::x ::y]
          :opt-un [::ndcX ::ndcY ::button ::clickCount ::wheelRotation]))

;; Window event formatting
(defn- format-dimensions [width height]
  (str width "x" height))

(defn- format-position [x y]
  (format "(%d, %d)" x y))

(defn- format-window-event-details
  "Format window event based on type."
  [event]
  (let [{:keys [type width height x y]} event]
    (case (keyword type)
      :resized (str "Resized to " (format-dimensions width height))
      :moved (str "Moved to " (format-position x y))
      :maximized "Maximized"
      :unmaximized "Restored from maximized"
      :minimized "Minimized to taskbar"
      :restored "Restored from taskbar"
      :focused "Got focus"
      :unfocused "Lost focus"
      :opened "Window opened"
      :closing "Window closing"
      ;; Unknown type - show raw data
      (str type " " (json/write-str (dissoc event :type))))))

(defn format-window-event
  "Format a window event for display."
  [event]
  {:pre [(s/valid? ::window-event event)]}
  (format-window-event-details event))

;; Navigation event formatting
(defn- format-coordinates
  "Format coordinates with optional NDC values."
  [{:keys [x y ndcX ndcY]}]
  (if (and ndcX ndcY)
    (format "%d,%d [NDC: %.3f,%.3f]" x y ndcX ndcY)
    (str x "," y)))

(defn- format-mouse-button
  "Format mouse button name."
  [button]
  (get mouse-button-names button (str button)))

(defn- format-click-count
  "Format click count if multiple."
  [click-count]
  (if (> click-count 1)
    (str " x" click-count)
    ""))

(defn- format-wheel-direction
  "Format mouse wheel direction."
  [rotation]
  (if (pos? rotation) "down" "up"))

(defn- format-navigation-event-details
  "Format navigation event based on type."
  [event]
  (let [{:keys [type button clickCount wheelRotation] :as evt} event
        coords (format-coordinates evt)]
    (case (keyword type)
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

(defn format-navigation-event
  "Format a navigation/mouse event for display."
  [event]
  {:pre [(s/valid? ::navigation-event event)]}
  (format-navigation-event-details event))

;; Event handlers
(defn- handle-window-closed
  "Handle window closed event - terminate the associated process."
  [stream-key]
  (when-let [stream (state/get-stream stream-key)]
    (future
      (try
        (process/stop-stream stream)
        (state/clear-stream! stream-key)
        (seesaw/invoke-later
         (when-let [btn (state/get-ui-element stream-key)]
           (seesaw/text! btn (str (name stream-key) " Stream OFF"))
           (seesaw/config! btn :selected? false)))
        (catch Exception e
          (log/log-error (name stream-key)
                        "Error handling window close"
                        :exception e))))))

(defn handle-response-event
  "Handle a response event from a stream."
  [stream-key msg]
  {:pre [(keyword? stream-key)
         (map? msg)]}
  (log/add-log-entry!
   {:time (System/currentTimeMillis)
    :stream (:streamId msg)
    :type "RESPONSE"
    :message (:status msg)
    :raw-data msg})
  
  ;; Handle specific response types
  (when (= (:status msg) "window-closed")
    (handle-window-closed stream-key)))

(defn handle-navigation-event
  "Handle a navigation/mouse event."
  [msg]
  {:pre [(map? msg)
         (map? (:event msg))]}
  (let [event (:event msg)]
    (log/add-log-entry!
     {:time (System/currentTimeMillis)
      :stream (:streamId msg)
      :type "NAV"
      :message (format-navigation-event event)
      :raw-data msg
      :nav-type (keyword (:type event))})))

(defn handle-window-event
  "Handle a window event."
  [msg]
  {:pre [(map? msg)
         (map? (:event msg))]}
  (let [event (:event msg)]
    (log/add-log-entry!
     {:time (System/currentTimeMillis)
      :stream (:streamId msg)
      :type "WINDOW"
      :message (format-window-event event)
      :raw-data msg
      :event-type (keyword (:type event))})))

;; Utility functions
(defn stream-connected?
  "Check if a stream is currently connected."
  [stream-key]
  (some? (state/get-stream stream-key)))

(defn all-streams-connected?
  "Check if all streams are connected."
  []
  (and (stream-connected? :heat)
       (stream-connected? :day)))