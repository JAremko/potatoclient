(ns potatoclient.transit.commands
  "Transit-based command API using new nested format matching protobuf structure.
  
  This namespace provides all command functions with the new nested structure
  that maps directly to protobuf hierarchy."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]))

;; ============================================================================
;; Basic Commands
;; ============================================================================

(>defn ping
  "Create a ping command (heartbeat/keepalive)"
  []
  [=> map?]
  {:ping {}})

(>defn noop
  "Create a no-operation command"
  []
  [=> map?]
  {:noop {}})

(>defn frozen
  "Create a frozen command (marks important state)"
  []
  [=> map?]
  {:frozen {}})

;; ============================================================================
;; System Commands
;; ============================================================================

(>defn set-localization
  "Set system localization"
  [locale]
  [[:enum :en :uk] => map?]
  {:system {:localization {:loc locale}}})

(>defn start-recording
  "Start recording"
  []
  [=> map?]
  {:system {:start-rec {}}})

(>defn stop-recording
  "Stop recording"
  []
  [=> map?]
  {:system {:stop-rec {}}})

;; ============================================================================
;; GPS Commands
;; ============================================================================

(>defn set-gps-manual
  "Set manual GPS position and optionally enable manual mode"
  [{:keys [use-manual latitude longitude altitude]}]
  [[:map
    [:use-manual boolean?]
    [:latitude {:optional true} [:double {:min -90.0 :max 90.0}]]
    [:longitude {:optional true} [:double {:min -180.0 :max 180.0}]]
    [:altitude {:optional true} [:double {:min -1000.0 :max 10000.0}]]]
   => map?]
  (if use-manual
    {:gps {:set-use-manual-position {:flag true}}}
    ;; When coordinates provided, set manual position
    (if (and latitude longitude altitude)
      {:gps {:set-manual-position {:latitude latitude
                                   :longitude longitude
                                   :altitude altitude}}}
      {:gps {:set-use-manual-position {:flag false}}})))

;; ============================================================================
;; Compass Commands
;; ============================================================================

(>defn set-compass-unit
  "Set compass unit (degrees or mils) - Note: may need mapping to actual command"
  [_unit]
  [[:enum :degrees :mils] => map?]
  ;; Note: Couldn't find specific compass unit command in keyword tree
  ;; This might need to be mapped to a different command
  {:compass {:calibrate {}}})  ; Placeholder - needs correct mapping

;; ============================================================================
;; LRF (Laser Range Finder) Commands
;; ============================================================================

(>defn lrf-single-measurement
  "Trigger single LRF measurement"
  []
  [=> map?]
  {:lrf {:measure {}}})

(>defn lrf-continuous-start
  "Start continuous LRF measurements"
  []
  [=> map?]
  {:lrf {:scan-on {}}})

(>defn lrf-continuous-stop
  "Stop continuous LRF measurements"
  []
  [=> map?]
  {:lrf {:scan-off {}}})

;; ============================================================================
;; Rotary Platform Commands
;; ============================================================================

(>defn rotary-goto
  "Move rotary platform to specific position"
  [{:keys [azimuth elevation]}]
  [[:map
    [:azimuth [:double {:min 0.0 :max 360.0}]]
    [:elevation [:double {:min -30.0 :max 90.0}]]]
   => map?]
  {:rotary {:goto {:azimuth azimuth
                   :elevation elevation}}})

(>defn rotary-stop
  "Stop rotary platform movement"
  []
  [=> map?]
  {:rotary {:stop {}}})

(>defn rotary-halt
  "Halt all rotary movement"
  []
  [=> map?]
  {:rotary {:halt {}}})

(>defn rotary-goto-ndc
  "Command to rotate camera to NDC position"
  [channel ndc-x ndc-y]
  [keyword? number? number? => map?]
  {:rotary {:goto-ndc {:channel channel
                       :x ndc-x
                       :y ndc-y}}})

(>defn rotary-set-velocity
  "Set rotary platform velocity with speed and direction"
  [azimuth-speed elevation-speed azimuth-direction elevation-direction]
  [number? number? keyword? keyword? => map?]
  {:rotary {:set-velocity {:azimuth-speed azimuth-speed
                           :elevation-speed elevation-speed
                           :azimuth-direction azimuth-direction
                           :elevation-direction elevation-direction}}})

;; ============================================================================
;; Computer Vision Commands
;; ============================================================================

(>defn cv-start-track-ndc
  "Start CV tracking at NDC position"
  [channel ndc-x ndc-y frame-timestamp]
  [keyword? number? number? (? int?) => map?]
  {:cv {:start-track-ndc {:channel channel
                          :x ndc-x
                          :y ndc-y
                          :frame-time frame-timestamp}}})

;; ============================================================================
;; Day Camera Commands
;; ============================================================================

(>defn day-camera-zoom
  "Set day camera zoom level"
  [zoom]
  [[:double {:min 1.0 :max 50.0}] => map?]
  {:day-camera {:zoom {:set-value {:value zoom}}}})

(>defn day-camera-focus
  "Set day camera focus mode"
  [{:keys [mode distance]}]
  [[:map
    [:mode [:enum :auto :manual :infinity]]
    [:distance {:optional true} [:double {:min 0.0}]]]
   => map?]
  ;; Map focus modes to appropriate commands
  (case mode
    :auto {:day-camera {:focus {:reset-focus {}}}}
    :manual {:day-camera {:focus {:set-value {:value (or distance 0.0)}}}}
    :infinity {:day-camera {:focus {:set-value {:value 999999.0}}}}))

(>defn day-camera-photo
  "Take a photo with day camera"
  []
  [=> map?]
  {:day-camera {:photo {}}})

;; ============================================================================
;; Heat Camera Commands
;; ============================================================================

(>defn heat-camera-zoom
  "Set heat camera zoom level"
  [zoom]
  [[:double {:min 1.0 :max 8.0}] => map?]
  {:heat-camera {:zoom {:set-value {:value zoom}}}})

(>defn heat-camera-calibrate
  "Trigger heat camera calibration (NUC)"
  []
  [=> map?]
  {:heat-camera {:nuc {}}})

(>defn heat-camera-palette
  "Set heat camera color palette"
  [palette]
  [[:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic] => map?]
  ;; Map palette names to indices (assuming an index-based system)
  (let [palette-index (case palette
                        :white-hot 0
                        :black-hot 1
                        :rainbow 2
                        :ironbow 3
                        :lava 4
                        :arctic 5
                        0)]
    {:heat-camera {:set-color-palette {:index palette-index}}}))

(>defn heat-camera-photo
  "Take a photo with heat camera"
  []
  [=> map?]
  {:heat-camera {:photo {}}})

;; ============================================================================
;; Glass Heater Commands
;; ============================================================================

(>defn glass-heater-on
  "Turn on glass heater"
  []
  [=> map?]
  {:day-cam-glass-heater {:turn-on {}}})

(>defn glass-heater-off
  "Turn off glass heater"
  []
  [=> map?]
  {:day-cam-glass-heater {:turn-off {}}})

;; ============================================================================
;; OSD (On-Screen Display) Commands
;; ============================================================================

(>defn osd-enable-day
  "Enable OSD on day camera"
  []
  [=> map?]
  {:osd {:enable-day-osd {}}})

(>defn osd-disable-day
  "Disable OSD on day camera"
  []
  [=> map?]
  {:osd {:disable-day-osd {}}})

(>defn osd-enable-heat
  "Enable OSD on heat camera"
  []
  [=> map?]
  {:osd {:enable-heat-osd {}}})

(>defn osd-disable-heat
  "Disable OSD on heat camera"
  []
  [=> map?]
  {:osd {:disable-heat-osd {}}})