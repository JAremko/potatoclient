(ns potatoclient.transit.commands
  "Transit-based command API that replaces direct protobuf command building.
  
  This namespace provides all the command functions previously in cmd.core
  but uses Transit messages instead of protobuf builders."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.transit.websocket-manager :as ws-manager]
            [potatoclient.logging :as logging]))

;; ============================================================================
;; Basic Commands
;; ============================================================================

(>defn ping
  "Send a ping command (heartbeat/keepalive)"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "ping"}))

(>defn noop
  "Send a no-operation command"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "noop"}))

(>defn frozen
  "Send a frozen command (marks important state)"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "frozen"}))

;; ============================================================================
;; System Commands
;; ============================================================================

(>defn set-localization
  "Set system localization"
  [locale]
  [[:enum "en" "uk"] => boolean?]
  (ws-manager/send-command! {:action "set-localization"
                             :params {:locale locale}}))

(>defn set-recording
  "Enable/disable recording"
  [enabled?]
  [boolean? => boolean?]
  (ws-manager/send-command! {:action "set-recording"
                             :params {:enabled enabled?}}))

;; ============================================================================
;; GPS Commands
;; ============================================================================

(>defn set-gps-manual
  "Set manual GPS position"
  [{:keys [use-manual latitude longitude altitude]}]
  [[:map
    [:use-manual boolean?]
    [:latitude {:optional true} [:double {:min -90.0 :max 90.0}]]
    [:longitude {:optional true} [:double {:min -180.0 :max 180.0}]]
    [:altitude {:optional true} [:double {:min -1000.0 :max 10000.0}]]]
   => boolean?]
  (ws-manager/send-command! {:action "set-gps-manual"
                             :params {:use-manual use-manual
                                      :latitude latitude
                                      :longitude longitude
                                      :altitude altitude}}))

;; ============================================================================
;; Compass Commands
;; ============================================================================

(>defn set-compass-unit
  "Set compass unit (degrees or mils)"
  [unit]
  [[:enum "degrees" "mils"] => boolean?]
  (ws-manager/send-command! {:action "set-compass-unit"
                             :params {:unit unit}}))

;; ============================================================================
;; LRF (Laser Range Finder) Commands
;; ============================================================================

(>defn lrf-single-measurement
  "Trigger single LRF measurement"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "lrf-single-measurement"}))

(>defn lrf-continuous-start
  "Start continuous LRF measurements"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "lrf-continuous-start"}))

(>defn lrf-continuous-stop
  "Stop continuous LRF measurements"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "lrf-continuous-stop"}))

;; ============================================================================
;; Rotary Platform Commands
;; ============================================================================

(>defn rotary-goto
  "Move rotary platform to specific position"
  [{:keys [azimuth elevation]}]
  [[:map
    [:azimuth [:double {:min 0.0 :max 360.0}]]
    [:elevation [:double {:min -30.0 :max 90.0}]]]
   => boolean?]
  (ws-manager/send-command! {:action "rotary-goto"
                             :params {:azimuth azimuth
                                      :elevation elevation}}))

(>defn rotary-stop
  "Stop rotary platform movement"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "rotary-stop"}))

(>defn rotary-set-velocity
  "Set rotary platform velocity"
  [{:keys [azimuth-velocity elevation-velocity]}]
  [[:map
    [:azimuth-velocity [:double {:min -180.0 :max 180.0}]]
    [:elevation-velocity [:double {:min -90.0 :max 90.0}]]]
   => boolean?]
  (ws-manager/send-command! {:action "rotary-set-velocity"
                             :params {:azimuth-velocity azimuth-velocity
                                      :elevation-velocity elevation-velocity}}))

;; ============================================================================
;; Day Camera Commands
;; ============================================================================

(>defn day-camera-zoom
  "Set day camera zoom level"
  [zoom]
  [[:double {:min 1.0 :max 50.0}] => boolean?]
  (ws-manager/send-command! {:action "day-camera-zoom"
                             :params {:zoom zoom}}))

(>defn day-camera-focus
  "Set day camera focus mode"
  [{:keys [mode distance]}]
  [[:map
    [:mode [:enum "auto" "manual" "infinity"]]
    [:distance {:optional true} [:double {:min 0.0}]]]
   => boolean?]
  (ws-manager/send-command! {:action "day-camera-focus"
                             :params {:mode mode
                                      :distance distance}}))

(>defn day-camera-photo
  "Take a photo with day camera"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "day-camera-photo"}))

;; ============================================================================
;; Heat Camera Commands
;; ============================================================================

(>defn heat-camera-zoom
  "Set heat camera zoom level"
  [zoom]
  [[:double {:min 1.0 :max 8.0}] => boolean?]
  (ws-manager/send-command! {:action "heat-camera-zoom"
                             :params {:zoom zoom}}))

(>defn heat-camera-calibrate
  "Trigger heat camera calibration (NUC)"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "heat-camera-calibrate"}))

(>defn heat-camera-palette
  "Set heat camera color palette"
  [palette]
  [[:enum "white-hot" "black-hot" "rainbow" "ironbow" "lava" "arctic"] => boolean?]
  (ws-manager/send-command! {:action "heat-camera-palette"
                             :params {:palette palette}}))

(>defn heat-camera-photo
  "Take a photo with heat camera"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "heat-camera-photo"}))

;; ============================================================================
;; Glass Heater Commands
;; ============================================================================

(>defn glass-heater-on
  "Turn on glass heater"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "glass-heater-on"}))

(>defn glass-heater-off
  "Turn off glass heater"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "glass-heater-off"}))

;; ============================================================================
;; OSD (On-Screen Display) Commands
;; ============================================================================

(>defn osd-enable-day
  "Enable OSD on day camera"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "osd-enable-day"}))

(>defn osd-disable-day
  "Disable OSD on day camera"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "osd-disable-day"}))

(>defn osd-enable-heat
  "Enable OSD on heat camera"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "osd-enable-heat"}))

(>defn osd-disable-heat
  "Disable OSD on heat camera"
  []
  [=> boolean?]
  (ws-manager/send-command! {:action "osd-disable-heat"}))

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn set-read-only-mode!
  "Set read-only mode (only ping and frozen commands allowed)"
  [enabled?]
  [boolean? => nil?]
  (ws-manager/set-read-only-mode! enabled?)
  (logging/log-info "Read-only mode changed" {:enabled enabled?})
  nil)

(>defn set-state-rate-limit!
  "Set maximum rate for state updates (Hz)"
  [rate-hz]
  [[:int {:min 1 :max 120}] => nil?]
  (ws-manager/set-state-rate-limit! rate-hz)
  (logging/log-info "State rate limit changed" {:rate-hz rate-hz})
  nil)