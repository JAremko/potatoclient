(ns mock-video-stream.scenarios
  "Pre-defined test scenarios for mock video stream testing."
  (:require [potatoclient.specs.video.stream :as video-specs]
            [malli.core :as m]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [taoensso.telemere :as log]))

;; ============================================================================
;; Test Scenarios
;; ============================================================================

(def scenarios
  {:tap-center 
   {:description "Single tap at screen center"
    :canvas {:width 800 :height 600}
    :events [{:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1000}
             {:type :mouse-up :x 400 :y 300 :button 1 :timestamp 1050}]
    :expected-commands [{:rotary {:goto-ndc {:channel :heat :x 0.0 :y 0.0}}}]}
   
   :tap-top-left
   {:description "Single tap at top-left corner"
    :canvas {:width 800 :height 600}
    :events [{:type :mouse-down :x 0 :y 0 :button 1 :timestamp 1000}
             {:type :mouse-up :x 0 :y 0 :button 1 :timestamp 1050}]
    :expected-commands [{:rotary {:goto-ndc {:channel :heat :x -1.0 :y 1.0}}}]}
   
   :tap-bottom-right
   {:description "Single tap at bottom-right corner"
    :canvas {:width 800 :height 600}
    :events [{:type :mouse-down :x 799 :y 599 :button 1 :timestamp 1000}
             {:type :mouse-up :x 799 :y 599 :button 1 :timestamp 1050}]
    :expected-commands [{:rotary {:goto-ndc {:channel :heat :x 1.0 :y -1.0}}}]}
   
   :double-tap-track
   {:description "Double tap to start CV tracking"
    :canvas {:width 800 :height 600}
    :events [{:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1000}
             {:type :mouse-up :x 400 :y 300 :button 1 :timestamp 1050}
             {:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1200}
             {:type :mouse-up :x 400 :y 300 :button 1 :timestamp 1250}]
    :frame-data {:timestamp 12345 :duration 33}
    :expected-commands [{:cv {:start-track-ndc {:channel :heat :x 0.0 :y 0.0 :frame-time 12345}}}]}
   
   :pan-rotate-right
   {:description "Pan gesture to rotate right"
    :canvas {:width 800 :height 600}
    :zoom-level 2
    :events [{:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1000}
             {:type :mouse-move :x 420 :y 300 :timestamp 1050}
             {:type :mouse-move :x 440 :y 300 :timestamp 1100}
             {:type :mouse-up :x 440 :y 300 :timestamp 1150}]
    :expected-commands [{:rotary {:set-velocity {:azimuth-speed 0.1 
                                                :elevation-speed 0.0
                                                :azimuth-direction :clockwise
                                                :elevation-direction :clockwise}}}
                       {:rotary {:halt {}}}]}
   
   :pan-diagonal
   {:description "Pan gesture diagonally"
    :canvas {:width 800 :height 600}
    :zoom-level 2
    :events [{:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1000}
             {:type :mouse-move :x 420 :y 280 :timestamp 1050}
             {:type :mouse-move :x 440 :y 260 :timestamp 1100}
             {:type :mouse-up :x 440 :y 260 :timestamp 1150}]
    :expected-commands [{:rotary {:set-velocity {:azimuth-speed 0.1
                                                :elevation-speed 0.1
                                                :azimuth-direction :clockwise
                                                :elevation-direction :counter-clockwise}}}
                       {:rotary {:halt {}}}]}
   
   :zoom-in-heat
   {:description "Mouse wheel zoom in on heat camera"
    :canvas {:width 800 :height 600}
    :stream-type :heat
    :events [{:type :mouse-wheel :x 400 :y 300 :wheel-rotation -1 :timestamp 1000}]
    :expected-commands [{:heat-camera {:next-zoom-table-pos {}}}]}
   
   :zoom-out-day
   {:description "Mouse wheel zoom out on day camera"
    :canvas {:width 800 :height 600}
    :stream-type :day
    :events [{:type :mouse-wheel :x 400 :y 300 :wheel-rotation 1 :timestamp 1000}]
    :expected-commands [{:day-camera {:prev-zoom-table-pos {}}}]}
   
   :complex-interaction
   {:description "Complex interaction sequence"
    :canvas {:width 800 :height 600}
    :zoom-level 1
    :frame-data {:timestamp 55555 :duration 33}
    :events [;; First tap
             {:type :mouse-down :x 200 :y 200 :button 1 :timestamp 1000}
             {:type :mouse-up :x 200 :y 200 :button 1 :timestamp 1050}
             ;; Wait a bit
             ;; Pan gesture
             {:type :mouse-down :x 400 :y 300 :button 1 :timestamp 2000}
             {:type :mouse-move :x 450 :y 250 :timestamp 2050}
             {:type :mouse-move :x 500 :y 200 :timestamp 2100}
             {:type :mouse-up :x 500 :y 200 :timestamp 2150}
             ;; Zoom
             {:type :mouse-wheel :x 400 :y 300 :wheel-rotation -1 :timestamp 3000}]
    :expected-commands [{:rotary {:goto-ndc {:channel :heat :x -0.5 :y 0.333}}}
                       {:rotary {:set-velocity {:azimuth-speed 0.125
                                               :elevation-speed 0.125
                                               :azimuth-direction :clockwise
                                               :elevation-direction :counter-clockwise}}}
                       {:rotary {:halt {}}}
                       {:heat-camera {:next-zoom-table-pos {}}}]}
   
   :rapid-taps
   {:description "Multiple rapid taps (should not trigger double-tap)"
    :canvas {:width 800 :height 600}
    :events [{:type :mouse-down :x 100 :y 100 :button 1 :timestamp 1000}
             {:type :mouse-up :x 100 :y 100 :button 1 :timestamp 1050}
             {:type :mouse-down :x 200 :y 200 :button 1 :timestamp 1400}
             {:type :mouse-up :x 200 :y 200 :button 1 :timestamp 1450}
             {:type :mouse-down :x 300 :y 300 :button 1 :timestamp 1800}
             {:type :mouse-up :x 300 :y 300 :button 1 :timestamp 1850}]
    :expected-commands [{:rotary {:goto-ndc {:channel :heat :x -0.75 :y 0.667}}}
                       {:rotary {:goto-ndc {:channel :heat :x -0.5 :y 0.333}}}
                       {:rotary {:goto-ndc {:channel :heat :x -0.25 :y 0.0}}}]}})

;; ============================================================================
;; Scenario Validation
;; ============================================================================

(defn validate-scenario
  "Validate a scenario against the spec"
  [scenario-name scenario]
  (if (m/validate video-specs/test-scenario scenario)
    {:valid true :name scenario-name}
    {:valid false 
     :name scenario-name
     :errors (m/explain video-specs/test-scenario scenario)}))

(defn validate-all-scenarios
  "Validate all scenarios and return results"
  []
  (let [results (map (fn [[name scenario]]
                      (validate-scenario name scenario))
                    scenarios)]
    {:total (count results)
     :valid (count (filter :valid results))
     :invalid (filter (complement :valid) results)}))

;; ============================================================================
;; Scenario Export
;; ============================================================================

(defn export-scenario
  "Export a single scenario to JSON"
  [scenario-name scenario output-dir]
  (let [file-path (io/file output-dir (str (name scenario-name) ".json"))
        json-data (json/write-str scenario)]
    (io/make-parents file-path)
    (spit file-path json-data)
    (log/info! "Exported scenario" {:name scenario-name :path (.getPath file-path)})))

(defn export-all-scenarios
  "Export all scenarios to JSON files"
  [output-dir]
  (doseq [[name scenario] scenarios]
    (export-scenario name scenario output-dir))
  (log/info! "Exported all scenarios" {:count (count scenarios) :dir output-dir}))

;; ============================================================================
;; Scenario Execution
;; ============================================================================

(defn get-scenario
  "Get a scenario by name"
  [scenario-name]
  (get scenarios scenario-name))

(defn list-scenarios
  "List all available scenarios"
  []
  (keys scenarios))