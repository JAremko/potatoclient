(ns potatoclient.transit.command-sender
  "Universal command sender that uses metadata to specify protobuf types.
  
  Instead of action tags, we send the full EDN structure with metadata
  indicating the protobuf message type. Transit handlers on the Kotlin
  side use this metadata to instantiate the correct Java class."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.transit.core :as transit]
            [potatoclient.specs :as specs]
            [malli.core :as m]
            [taoensso.telemere :as t]))

;; =============================================================================
;; Protobuf Type Registry
;; =============================================================================

(def proto-type-registry
  "Maps from semantic paths to protobuf Java class names.
  The path represents the logical structure, not the action name."
  {;; Basic commands
   [:root :ping] "cmd.JonSharedCmd$Root"
   [:root :noop] "cmd.JonSharedCmd$Root"
   [:root :frozen] "cmd.JonSharedCmd$Root"
   
   ;; System commands
   [:root :set-gps-manual] "cmd.JonSharedCmd$Root"
   [:root :set-recording] "cmd.JonSharedCmd$Root"
   [:root :set-localization] "cmd.JonSharedCmd$Root"
   
   ;; Rotary platform - note the nested structure
   [:rotary-platform :goto] "cmd.JonSharedCmdRotaryPlatform$Root"
   [:rotary-platform :start] "cmd.JonSharedCmdRotaryPlatform$Root"
   [:rotary-platform :stop] "cmd.JonSharedCmdRotaryPlatform$Root"
   [:rotary-platform :halt] "cmd.JonSharedCmdRotaryPlatform$Root"
   [:rotary-platform :set-velocity] "cmd.JonSharedCmdRotaryPlatform$Root"
   
   ;; Heat camera
   [:heat-camera :zoom] "cmd.JonSharedCmdHeatCamera$Root"
   [:heat-camera :calibrate] "cmd.JonSharedCmdHeatCamera$Root"
   [:heat-camera :palette] "cmd.JonSharedCmdHeatCamera$Root"
   [:heat-camera :photo] "cmd.JonSharedCmdHeatCamera$Root"
   
   ;; Day camera
   [:day-camera :zoom] "cmd.JonSharedCmdDayCamera$Root"
   [:day-camera :focus] "cmd.JonSharedCmdDayCamera$Root"
   [:day-camera :photo] "cmd.JonSharedCmdDayCamera$Root"
   
   ;; CV commands
   [:cv :start-track-ndc] "cmd.JonSharedCmdCv$Root"
   [:cv :stop-track] "cmd.JonSharedCmdCv$Root"
   
   ;; LRF calibration - channel specific
   [:lrf-calib :day :set] "cmd.JonSharedCmdLrfCalib$Root"
   [:lrf-calib :day :save] "cmd.JonSharedCmdLrfCalib$Root"
   [:lrf-calib :heat :set] "cmd.JonSharedCmdLrfCalib$Root"
   [:lrf-calib :heat :save] "cmd.JonSharedCmdLrfCalib$Root"})

;; =============================================================================
;; Command Building Functions
;; =============================================================================

(defn- attach-proto-metadata
  "Attach protobuf type metadata to a command map.
  The metadata includes the full type path for the Kotlin handler."
  [command-map type-path]
  (let [proto-class (get proto-type-registry type-path)]
    (if proto-class
      (with-meta command-map
        {:proto-type proto-class
         :proto-path type-path})
      (throw (ex-info "Unknown protobuf type path"
                      {:path type-path
                       :command command-map})))))

;; =============================================================================
;; Universal Command Sender
;; =============================================================================

(>defn send-command
  "Universal command sender that validates and sends any protobuf command.
  
  The command-map should match the protobuf structure exactly, with the
  type-path indicating which protobuf message type to use.
  
  Examples:
  ;; Simple command
  (send-command {:ping {}} [:root :ping])
  
  ;; Nested command with parameters
  (send-command {:goto {:azimuth 180.0 :elevation 45.0}}
                [:rotary-platform :goto])
  
  ;; Deeply nested with channel selection
  (send-command {:day {:set {:x-offset 10 :y-offset -5}}}
                [:lrf-calib :day :set])"
  [command-map type-path]
  [map? vector? => map?]
  (let [;; Look up the spec for this command type
        spec-key (case (first type-path)
                   :root (keyword "cmd" (name (second type-path)))
                   :rotary-platform (keyword "cmd.RotaryPlatform" (name (second type-path)))
                   :heat-camera (keyword "cmd.HeatCamera" (name (second type-path)))
                   :day-camera (keyword "cmd.DayCamera" (name (second type-path)))
                   :cv (keyword "cmd.Cv" (name (second type-path)))
                   :lrf-calib (keyword "cmd.LrfCalib" 
                                       (str (name (second type-path)) "-" 
                                            (name (nth type-path 2))))
                   (throw (ex-info "Unknown command category" 
                                   {:category (first type-path)})))]
    
    ;; Validate with Guardrails/Malli in development
    (when-let [spec (specs/get-spec spec-key)]
      (when-not (m/validate spec command-map {:registry (specs/proto-registry)})
        (throw (ex-info "Command validation failed"
                        {:command command-map
                         :type-path type-path
                         :spec spec-key
                         :errors (m/explain spec command-map 
                                           {:registry (specs/proto-registry)})}))))
    
    ;; Attach metadata and create Transit message
    (let [metadata-command (attach-proto-metadata command-map type-path)]
      {:msg-type "command"
       :msg-id (str (random-uuid))
       :timestamp (System/currentTimeMillis)
       :payload metadata-command})))

;; =============================================================================
;; Convenience Functions for Common Commands
;; =============================================================================

(>defn ping
  "Send a ping command"
  []
  [=> map?]
  (send-command {:ping {}} [:root :ping]))

(>defn rotary-goto
  "Move rotary platform to position"
  [azimuth elevation]
  [number? number? => map?]
  (send-command {:goto {:azimuth azimuth :elevation elevation}}
                [:rotary-platform :goto]))

(>defn heat-camera-zoom
  "Set heat camera zoom"
  [zoom]
  [number? => map?]
  (send-command {:zoom {:zoom zoom}}
                [:heat-camera :zoom]))

(>defn cv-start-track
  "Start CV tracking at NDC coordinates"
  [channel x y & [frame-timestamp]]
  [keyword? number? number? (? int?) => map?]
  (send-command {:start-track-ndc {:channel (name channel)
                                   :x x
                                   :y y
                                   :frame-timestamp (or frame-timestamp 0)}}
                [:cv :start-track-ndc]))

(>defn lrf-calibrate-day
  "Calibrate LRF for day channel"
  [cmd x-offset y-offset]
  [keyword? number? number? => map?]
  (send-command {:day {cmd {:x-offset x-offset :y-offset y-offset}}}
                [:lrf-calib :day cmd]))

;; =============================================================================
;; Example Usage
;; =============================================================================

(comment
  ;; The beauty is we can send ANY protobuf structure
  ;; The metadata tells Kotlin which class to use
  
  ;; Simple commands
  (ping)
  
  ;; Commands with the same field names but different types
  ;; Both have "start" but map to different Java classes
  (send-command {:start {}} [:rotary-platform :start])
  (send-command {:start {}} [:video-recording :start])
  
  ;; Deeply nested structures
  (send-command {:day {:offsets {:set {:x-offset 10 :y-offset -5}}}}
                [:lrf-calib :day :offsets :set])
  
  ;; The Kotlin side receives the metadata and knows exactly
  ;; which protobuf class to instantiate!
  )