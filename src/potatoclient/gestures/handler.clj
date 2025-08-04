(ns potatoclient.gestures.handler
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
            [potatoclient.gestures.config :as config :refer [calculate-rotation-speeds]]
            [potatoclient.logging :as logging]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.commands :as commands]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.subprocess-launcher :as subprocess]
            [potatoclient.ui-specs :as specs]))

(>defn handle-tap-gesture
  "Handle single tap gesture - rotate camera to NDC position"
  [{:keys [ndc-x ndc-y stream-type]}]
  [::specs/gesture-event => nil?]
  (logging/log-info {:msg "Tap gesture" :ndc-x ndc-x :ndc-y ndc-y :stream stream-type})
  (let [channel (if (= stream-type :heat) :heat :day)]
    (subprocess/send-message :cmd
                             (transit-core/create-message :command
                                                          (commands/rotary-goto-ndc channel ndc-x ndc-y))))
  nil)

(>defn handle-double-tap-gesture
  "Handle double tap gesture - start CV tracking at NDC position"
  [{:keys [ndc-x ndc-y stream-type frame-timestamp]}]
  [::specs/gesture-event => nil?]
  (logging/log-info {:msg "Double-tap gesture" :ndc-x ndc-x :ndc-y ndc-y :stream stream-type})
  (let [channel (if (= stream-type :heat) :heat :day)]
    (subprocess/send-message :cmd
                             (transit-core/create-message :command
                                                          (commands/cv-start-track-ndc channel ndc-x ndc-y frame-timestamp))))
  nil)

(>defn handle-pan-start-gesture
  "Handle pan gesture start"
  [{:keys [ndc-x ndc-y]}]
  [::specs/gesture-event => nil?]
  (logging/log-debug {:msg "Pan start" :ndc-x ndc-x :ndc-y ndc-y})
  (app-db/update-in-app-db! [:gestures :pan]
                            {:active true
                             :start-x ndc-x
                             :start-y ndc-y
                             :last-update (System/currentTimeMillis)})
  nil)

(>defn handle-pan-move-gesture
  "Handle pan gesture movement - send rotary velocity commands"
  [{:keys [ndc-delta-x ndc-delta-y stream-type]}]
  [::specs/gesture-event => nil?]
  (let [pan-state (app-db/get-in-app-db [:gestures :pan])
        now (System/currentTimeMillis)]
    (when (and (:active pan-state)
               (> (- now (:last-update pan-state)) 100)) ; Throttle to ~10Hz
      (let [;; Get current zoom value for the active stream
            camera-key (if (= stream-type :heat) :camera-heat :camera-day)
            zoom-value (app-db/get-in-app-db [camera-key :zoom] 1.0)
            speed-config (config/get-speed-config-for-zoom-value stream-type zoom-value)
            [az-speed el-speed] (calculate-rotation-speeds
                                  ndc-delta-x ndc-delta-y speed-config)
            ;; Determine rotation directions based on delta signs
            az-direction (if (pos? ndc-delta-x) :clockwise :counter-clockwise)
            el-direction (if (pos? ndc-delta-y) :clockwise :counter-clockwise)]
        (subprocess/send-message :cmd
                                 (transit-core/create-message :command
                                                              (commands/rotary-set-velocity az-speed el-speed az-direction el-direction)))
        (app-db/update-in-app-db! [:gestures :pan :last-update] now))))
  nil)

(>defn handle-pan-stop-gesture
  "Handle pan gesture stop"
  [_gesture]
  [::specs/gesture-event => nil?]
  (logging/log-debug {:msg "Pan stop"})
  (subprocess/send-message :command
                           (transit-core/create-message :command
                                                        (commands/rotary-halt)))
  (app-db/update-in-app-db! [:gestures :pan] {:active false})
  nil)

(>defn handle-swipe-gesture
  "Handle swipe gesture"
  [{:keys [direction]}]
  [::specs/gesture-event => nil?]
  (logging/log-info {:msg "Swipe gesture" :direction direction})
  ;; Can be used for UI navigation or other actions
  nil)

(>defn handle-gesture-event
  "Main gesture event dispatcher"
  [event]
  [::specs/gesture-event => nil?]
  ;; With automatic Transit keyword conversion, event keys should already be keywords
  (let [gesture-type (:gesture-type event)]
    (case gesture-type
      :tap (handle-tap-gesture event)
      :doubletap (handle-double-tap-gesture event)
      :panstart (handle-pan-start-gesture event)
      :panmove (handle-pan-move-gesture event)
      :panstop (handle-pan-stop-gesture event)
      :swipe (handle-swipe-gesture event)
      (logging/log-warn {:msg "Unknown gesture type" :type gesture-type})))
  nil)

;; Helper functions - these are imported from config namespace, no need to redefine