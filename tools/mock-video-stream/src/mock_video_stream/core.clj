(ns mock-video-stream.core
  "Core functionality for mock video stream subprocess.
  Handles message processing and command generation."
  (:require [potatoclient.specs.video.stream :as video-specs]
            [malli.core :as m]
            [taoensso.telemere :as log])
  (:import [potatoclient.video NDCConverter]))

;; ============================================================================
;; State Management
;; ============================================================================

(defonce state 
  (atom {:stream-type :heat
         :canvas {:width 800 :height 600}
         :zoom-level 0
         :frame-data {:timestamp 0 :duration 33}
         :gesture-state nil}))

;; ============================================================================
;; NDC Conversion Wrappers
;; ============================================================================

(defn pixel->ndc
  "Convert pixel coordinates to NDC using shared Java converter"
  [x y width height]
  (let [ndc (NDCConverter/pixelToNDC x y width height)]
    {:x (.x ndc) :y (.y ndc)}))

(defn ndc->pixel
  "Convert NDC coordinates to pixel using shared Java converter"
  [ndc-x ndc-y width height]
  (let [pixel (NDCConverter/ndcToPixel ndc-x ndc-y width height)]
    {:x (.x pixel) :y (.y pixel)}))

(defn pixel-delta->ndc
  "Convert pixel delta to NDC delta"
  [delta-x delta-y width height]
  (let [ndc (NDCConverter/pixelDeltaToNDC delta-x delta-y width height)]
    {:x (.x ndc) :y (.y ndc)}))

;; ============================================================================
;; Command Generation
;; ============================================================================

(defn generate-tap-command
  "Generate rotary goto command from tap gesture"
  [{:keys [x y]} {:keys [stream-type canvas]}]
  (let [ndc (pixel->ndc x y (:width canvas) (:height canvas))]
    {:rotary {:goto-ndc {:channel stream-type
                         :x (:x ndc)
                         :y (:y ndc)}}}))

(defn generate-double-tap-command
  "Generate CV tracking command from double-tap gesture"
  [{:keys [x y]} {:keys [stream-type canvas frame-data]}]
  (let [ndc (pixel->ndc x y (:width canvas) (:height canvas))]
    {:cv {:start-track-ndc {:channel stream-type
                           :x (:x ndc)
                           :y (:y ndc)
                           :frame-time (:timestamp frame-data)}}}))

(defn generate-pan-velocity-command
  "Generate velocity command from pan movement"
  [{:keys [delta-x delta-y]} {:keys [zoom-level canvas]}]
  (let [ndc-delta (pixel-delta->ndc delta-x delta-y 
                                    (:width canvas) (:height canvas))
        ;; Simple speed calculation based on zoom
        speed-multiplier (+ 0.1 (* zoom-level 0.2))
        az-speed (* (Math/abs (:x ndc-delta)) speed-multiplier)
        el-speed (* (Math/abs (:y ndc-delta)) speed-multiplier)
        az-dir (if (pos? delta-x) :clockwise :counter-clockwise)
        el-dir (if (pos? delta-y) :counter-clockwise :clockwise)]
    {:rotary {:set-velocity {:azimuth-speed az-speed
                            :elevation-speed el-speed
                            :azimuth-direction az-dir
                            :elevation-direction el-dir}}}))

(defn generate-halt-command
  "Generate halt command"
  []
  {:rotary {:halt {}}})

(defn generate-zoom-command
  "Generate zoom command based on wheel direction"
  [wheel-rotation stream-type]
  (let [camera-key (if (= stream-type :heat) :heat-camera :day-camera)
        zoom-dir (if (neg? wheel-rotation) :next-zoom-table-pos :prev-zoom-table-pos)]
    {camera-key {zoom-dir {}}}))

;; ============================================================================
;; Gesture Event Generation
;; ============================================================================

(defn create-gesture-event
  "Create a gesture event with all required fields"
  [gesture-type {:keys [x y delta-x delta-y]} state]
  (let [{:keys [canvas stream-type frame-data]} state
        width (:width canvas)
        height (:height canvas)
        base-event {:type :gesture
                    :gesture-type gesture-type
                    :timestamp (System/currentTimeMillis)
                    :canvas-width width
                    :canvas-height height
                    :aspect-ratio (double (/ width height))
                    :stream-type stream-type}]
    (cond-> base-event
      (and x y) (merge {:x x :y y}
                      (let [ndc (pixel->ndc x y width height)]
                        {:ndc-x (:x ndc) :ndc-y (:y ndc)}))
      (and delta-x delta-y) (merge {:delta-x delta-x :delta-y delta-y}
                                  (let [ndc-delta (pixel-delta->ndc delta-x delta-y width height)]
                                    {:ndc-delta-x (:x ndc-delta) 
                                     :ndc-delta-y (:y ndc-delta)}))
      frame-data (merge {:frame-timestamp (:timestamp frame-data)
                        :frame-duration (:duration frame-data)}))))

;; ============================================================================
;; Message Handling
;; ============================================================================

(defmulti handle-control-message 
  "Handle control messages for the mock"
  (fn [msg _] (get-in msg [:payload :type])))

(defmethod handle-control-message :set-frame-data
  [msg state]
  (swap! state assoc :frame-data (get-in msg [:payload :data]))
  {:status :ok})

(defmethod handle-control-message :set-zoom-level
  [msg state]
  (swap! state assoc :zoom-level (get-in msg [:payload :level]))
  {:status :ok})

(defmethod handle-control-message :get-status
  [_ state]
  (let [s @state]
    {:status :running
     :stream-type (:stream-type s)
     :canvas-width (get-in s [:canvas :width])
     :canvas-height (get-in s [:canvas :height])
     :current-zoom (:zoom-level s)
     :frame-data (:frame-data s)}))

(defmethod handle-control-message :default
  [msg _]
  (log/warn! "Unknown control message type" {:msg msg})
  {:status :error :message "Unknown control message type"})

(defn handle-message
  "Main message handler for mock video stream"
  [msg]
  (try
    (case (:msg-type msg)
      :control (handle-control-message msg state)
      :request {:status :error :message "Mock doesn't handle requests"}
      :command {:status :ok :message "Command acknowledged"}
      (do
        (log/warn! "Unknown message type" {:msg-type (:msg-type msg)})
        {:status :error :message "Unknown message type"}))
    (catch Exception e
      (log/error! e "Error handling message" {:msg msg})
      {:status :error :message (.getMessage e)})))