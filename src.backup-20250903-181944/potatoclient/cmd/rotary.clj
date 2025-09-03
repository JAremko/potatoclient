(ns potatoclient.cmd.rotary
  "Rotary Platform command functions for controlling azimuth and elevation axes.
   Based on the RotaryPlatform message structure in jon_shared_cmd_rotary.proto."
  (:require
            [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Platform Control
;; ============================================================================

(defn start
  "Start the rotary platform.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:start {}}})) 
 (m/=> start [:=> [:cat] :cmd/root])

(defn stop
  "Stop the rotary platform.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:stop {}}})) 
 (m/=> stop [:=> [:cat] :cmd/root])

(defn halt
  "Halt all rotary platform movement immediately.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:halt {}}})) 
 (m/=> halt [:=> [:cat] :cmd/root])

;; ============================================================================
;; Platform Position Setting
;; ============================================================================

(defn set-platform-azimuth
  "Set the platform azimuth reference angle.
   Value must be between -360 and 360 degrees (exclusive).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:rotary {:set_platform_azimuth {:value value}}})) 
 (m/=> set-platform-azimuth [:=> [:cat [:and [:double {:min -360.0, :max 360.0}] [:> -360.0] [:< 360.0]]] :cmd/root])

(defn set-platform-elevation
  "Set the platform elevation reference angle.
   Value must be between -90 and 90 degrees.
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:rotary {:set_platform_elevation {:value value}}})) 
 (m/=> set-platform-elevation [:=> [:cat :angle/elevation] :cmd/root])

(defn set-platform-bank
  "Set the platform bank reference angle.
   Value must be between -180 and 180 degrees (exclusive).
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:rotary {:set_platform_bank {:value value}}})) 
 (m/=> set-platform-bank [:=> [:cat :angle/bank] :cmd/root])

;; ============================================================================
;; Mode and Configuration
;; ============================================================================

(defn set-mode
  "Set the rotary platform operation mode.
   Mode must be one of the JonGuiDataRotaryMode enum values.
   Returns a fully formed cmd root ready to send."
  [mode]
  (core/create-command
    {:rotary {:set_mode {:mode mode}}})) 
 (m/=> set-mode [:=> [:cat :enum/rotary-mode] :cmd/root])

(defn set-use-rotary-as-compass
  "Enable or disable using rotary platform as compass reference.
   Returns a fully formed cmd root ready to send."
  [use-as-compass?]
  (core/create-command
    {:rotary {:set_use_rotary_as_compass {:flag use-as-compass?}}})) 
 (m/=> set-use-rotary-as-compass [:=> [:cat :boolean] :cmd/root])

;; ============================================================================
;; Azimuth Control (Single Axis)
;; ============================================================================

(defn halt-azimuth
  "Halt azimuth movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:rotary {:axis {:azimuth {:halt {}}}}})) 
 (m/=> halt-azimuth [:=> [:cat] :cmd/root])

(defn set-azimuth-value
  "Set azimuth to specific value with direction.
   Value: 0-360 degrees
   Direction: :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE or _COUNTER_CLOCKWISE
   Returns a fully formed cmd root ready to send."
  [value direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:set_value {:value value
                                           :direction direction}}}}})) 
 (m/=> set-azimuth-value [:=> [:cat :angle/azimuth :enum/rotary-direction] :cmd/root])

(defn rotate-azimuth-to
  "Rotate azimuth to target value at specified speed with direction.
   Target: 0-360 degrees
   Speed: 0.0 to 1.0 (normalized)
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [target-value speed direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:rotate_to {:target_value target-value
                                           :speed speed
                                           :direction direction}}}}})) 
 (m/=> rotate-azimuth-to [:=> [:cat :angle/azimuth :range/normalized :enum/rotary-direction] :cmd/root])

(defn rotate-azimuth
  "Rotate azimuth continuously at specified speed and direction.
   Speed: 0.0 to 1.0 (normalized)
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [speed direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:rotate {:speed speed
                                        :direction direction}}}}})) 
 (m/=> rotate-azimuth [:=> [:cat :range/normalized :enum/rotary-direction] :cmd/root])

(defn rotate-azimuth-relative
  "Rotate azimuth relative to current position.
   Value: -180 to 180 degrees relative change
   Speed: 0.0 to 1.0 (normalized)
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [value speed direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:relative {:value value
                                          :speed speed
                                          :direction direction}}}}})) 
 (m/=> rotate-azimuth-relative [:=> [:cat :angle/relative-azimuth :range/normalized :enum/rotary-direction] :cmd/root])

(defn rotate-azimuth-relative-set
  "Set azimuth relative to current position (immediate).
   Value: -180 to 180 degrees relative change
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [value direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:relative_set {:value value
                                              :direction direction}}}}})) 
 (m/=> rotate-azimuth-relative-set [:=> [:cat :angle/relative-azimuth :enum/rotary-direction] :cmd/root])

;; ============================================================================
;; Elevation Control (Single Axis)
;; ============================================================================

(defn halt-elevation
  "Halt elevation movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:rotary {:axis {:elevation {:halt {}}}}})) 
 (m/=> halt-elevation [:=> [:cat] :cmd/root])

(defn set-elevation-value
  "Set elevation to specific value.
   Value: -90 to 90 degrees
   Returns a fully formed cmd root ready to send."
  [value]
  (core/create-command
    {:rotary {:axis {:elevation {:set_value {:value value}}}}})) 
 (m/=> set-elevation-value [:=> [:cat :angle/elevation] :cmd/root])

(defn rotate-elevation-to
  "Rotate elevation to target value at specified speed.
   Target: -90 to 90 degrees
   Speed: 0.0 to 1.0 (normalized)
   Returns a fully formed cmd root ready to send."
  [target-value speed]
  (core/create-command
    {:rotary {:axis {:elevation {:rotate_to {:target_value target-value
                                             :speed speed}}}}})) 
 (m/=> rotate-elevation-to [:=> [:cat :angle/elevation :range/normalized] :cmd/root])

(defn rotate-elevation
  "Rotate elevation continuously at specified speed and direction.
   Speed: 0.0 to 1.0 (normalized)
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [speed direction]
  (core/create-command
    {:rotary {:axis {:elevation {:rotate {:speed speed
                                          :direction direction}}}}})) 
 (m/=> rotate-elevation [:=> [:cat :range/normalized :enum/rotary-direction] :cmd/root])

(defn rotate-elevation-relative
  "Rotate elevation relative to current position.
   Value: -90 to 90 degrees relative change
   Speed: 0.0 to 1.0 (normalized)
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [value speed direction]
  (core/create-command
    {:rotary {:axis {:elevation {:relative {:value value
                                            :speed speed
                                            :direction direction}}}}})) 
 (m/=> rotate-elevation-relative [:=> [:cat :angle/relative-elevation :range/normalized :enum/rotary-direction] :cmd/root])

(defn rotate-elevation-relative-set
  "Set elevation relative to current position (immediate).
   Value: -90 to 90 degrees relative change
   Direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [value direction]
  (core/create-command
    {:rotary {:axis {:elevation {:relative_set {:value value
                                                :direction direction}}}}})) 
 (m/=> rotate-elevation-relative-set [:=> [:cat :angle/relative-elevation :enum/rotary-direction] :cmd/root])

;; ============================================================================
;; Combined Axis Control
;; ============================================================================

(defn halt-both
  "Halt both azimuth and elevation movement.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:rotary {:axis {:azimuth {:halt {}}
                     :elevation {:halt {}}}}})) 
 (m/=> halt-both [:=> [:cat] :cmd/root])

(defn rotate-both-to
  "Rotate both axes to target positions.
   Azimuth: 0-360 degrees
   Azimuth speed: 0.0 to 1.0
   Azimuth direction: rotation direction enum
   Elevation: -90 to 90 degrees  
   Elevation speed: 0.0 to 1.0
   Returns a fully formed cmd root ready to send."
  [azimuth azimuth-speed azimuth-direction elevation elevation-speed]
  (core/create-command
    {:rotary {:axis {:azimuth {:rotate_to {:target_value azimuth
                                           :speed azimuth-speed
                                           :direction azimuth-direction}}
                     :elevation {:rotate_to {:target_value elevation
                                             :speed elevation-speed}}}}})) 
 (m/=> rotate-both-to [:=> [:cat :angle/azimuth :range/normalized :enum/rotary-direction :angle/elevation :range/normalized] :cmd/root])

(defn rotate-both
  "Rotate both axes continuously.
   Azimuth speed: 0.0 to 1.0
   Azimuth direction: rotation direction enum
   Elevation speed: 0.0 to 1.0
   Elevation direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [azimuth-speed azimuth-direction elevation-speed elevation-direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:rotate {:speed azimuth-speed
                                        :direction azimuth-direction}}
                     :elevation {:rotate {:speed elevation-speed
                                          :direction elevation-direction}}}}})) 
 (m/=> rotate-both [:=> [:cat :range/normalized :enum/rotary-direction :range/normalized :enum/rotary-direction] :cmd/root])

(defn rotate-both-relative
  "Rotate both axes relative to current positions.
   Azimuth: -180 to 180 degrees relative
   Azimuth speed: 0.0 to 1.0
   Azimuth direction: rotation direction enum
   Elevation: -90 to 90 degrees relative
   Elevation speed: 0.0 to 1.0
   Elevation direction: rotation direction enum
   Returns a fully formed cmd root ready to send."
  [azimuth azimuth-speed azimuth-direction elevation elevation-speed elevation-direction]
  (core/create-command
    {:rotary {:axis {:azimuth {:relative {:value azimuth
                                          :speed azimuth-speed
                                          :direction azimuth-direction}}
                     :elevation {:relative {:value elevation
                                            :speed elevation-speed
                                            :direction elevation-direction}}}}})) 
 (m/=> rotate-both-relative [:=> [:cat :angle/relative-azimuth :range/normalized :enum/rotary-direction :angle/relative-elevation :range/normalized :enum/rotary-direction] :cmd/root])

(defn set-both-values
  "Set both axes to specific values.
   Azimuth: 0-360 degrees
   Azimuth direction: rotation direction enum
   Elevation: -90 to 90 degrees
   Returns a fully formed cmd root ready to send."
  [azimuth azimuth-direction elevation]
  (core/create-command
    {:rotary {:axis {:azimuth {:set_value {:value azimuth
                                           :direction azimuth-direction}}
                     :elevation {:set_value {:value elevation}}}}})) 
 (m/=> set-both-values [:=> [:cat :angle/azimuth :enum/rotary-direction :angle/elevation] :cmd/root])

;; ============================================================================
;; GPS Integration
;; ============================================================================

(defn rotate-to-gps
  "Rotate platform to point at GPS coordinates.
   Latitude: -90 to 90 degrees
   Longitude: -180 to 180 degrees
   Altitude: -430 to 100000 meters
   Returns a fully formed cmd root ready to send."
  [latitude longitude altitude]
  (core/create-command
    {:rotary {:rotate_to_gps {:latitude latitude
                              :longitude longitude
                              :altitude altitude}}})) 
 (m/=> rotate-to-gps [:=> [:cat :position/latitude :position/longitude :position/altitude] :cmd/root])

(defn set-origin-gps
  "Set GPS origin reference point for platform.
   Latitude: -90 to 90 degrees
   Longitude: -180 to 180 degrees
   Altitude: -430 to 100000 meters
   Returns a fully formed cmd root ready to send."
  [latitude longitude altitude]
  (core/create-command
    {:rotary {:set_origin_gps {:latitude latitude
                               :longitude longitude
                               :altitude altitude}}})) 
 (m/=> set-origin-gps [:=> [:cat :position/latitude :position/longitude :position/altitude] :cmd/root])

;; ============================================================================
;; NDC (Normalized Device Coordinates) Control
;; ============================================================================

(defn rotate-to-ndc
  "Rotate platform to point at screen coordinates.
   Channel: :JON_GUI_DATA_VIDEO_CHANNEL_DAY or _HEAT
   X: -1.0 to 1.0 (normalized)
   Y: -1.0 to 1.0 (normalized)
   Returns a fully formed cmd root ready to send."
  [channel x y]
  (core/create-command
    {:rotary {:rotate_to_ndc {:channel channel
                              :x x
                              :y y}}})) 
 (m/=> rotate-to-ndc [:=> [:cat :enum/video-channel :screen/ndc-x :screen/ndc-y] :cmd/root])

;; ============================================================================
;; Scan Operations
;; ============================================================================

(defn scan-start
  "Start scanning operation.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_start {}}})) 
 (m/=> scan-start [:=> [:cat] :cmd/root])

(defn scan-stop
  "Stop scanning operation.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_stop {}}})) 
 (m/=> scan-stop [:=> [:cat] :cmd/root])

(defn scan-pause
  "Pause scanning operation.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_pause {}}})) 
 (m/=> scan-pause [:=> [:cat] :cmd/root])

(defn scan-unpause
  "Resume paused scanning operation.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_unpause {}}})) 
 (m/=> scan-unpause [:=> [:cat] :cmd/root])

(defn scan-prev
  "Move to previous scan position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_prev {}}})) 
 (m/=> scan-prev [:=> [:cat] :cmd/root])

(defn scan-next
  "Move to next scan position.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_next {}}})) 
 (m/=> scan-next [:=> [:cat] :cmd/root])

(defn scan-refresh-node-list
  "Refresh the scan node list.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:scan_refresh_node_list {}}})) 
 (m/=> scan-refresh-node-list [:=> [:cat] :cmd/root])

(defn scan-select-node
  "Select a scan node by index.
   Index must be >= 0.
   Returns a fully formed cmd root ready to send."
  [index]
  (core/create-command
    {:rotary {:scan_select_node {:index index}}})) 
 (m/=> scan-select-node [:=> [:cat :proto/int32-positive] :cmd/root])

(defn scan-delete-node
  "Delete a scan node by index.
   Index must be >= 0.
   Returns a fully formed cmd root ready to send."
  [index]
  (core/create-command
    {:rotary {:scan_delete_node {:index index}}})) 
 (m/=> scan-delete-node [:=> [:cat :proto/int32-positive] :cmd/root])

(defn scan-update-node
  "Update a scan node's parameters.
   Index: >= 0
   Day/Heat zoom table values: >= 0
   Azimuth: 0-360 degrees
   Elevation: -90 to 90 degrees
   Linger: >= 0.0 seconds
   Speed: > 0.0 to 1.0 (normalized)
   Returns a fully formed cmd root ready to send."
  [index day-zoom heat-zoom azimuth elevation linger speed]
  (core/create-command
    {:rotary {:scan_update_node {:index index
                                 :DayZoomTableValue day-zoom
                                 :HeatZoomTableValue heat-zoom
                                 :azimuth azimuth
                                 :elevation elevation
                                 :linger linger
                                 :speed speed}}})) 
 (m/=> scan-update-node [:=> [:cat :proto/int32-positive :proto/int32-positive :proto/int32-positive :angle/azimuth :angle/elevation [:double {:min 0.0}] :speed/normalized] :cmd/root])

(defn scan-add-node
  "Add a new scan node.
   Index: >= 0
   Day/Heat zoom table values: >= 0
   Azimuth: 0-360 degrees
   Elevation: -90 to 90 degrees
   Linger: >= 0.0 seconds
   Speed: > 0.0 to 1.0 (normalized)
   Returns a fully formed cmd root ready to send."
  [index day-zoom heat-zoom azimuth elevation linger speed]
  (core/create-command
    {:rotary {:scan_add_node {:index index
                              :DayZoomTableValue day-zoom
                              :HeatZoomTableValue heat-zoom
                              :azimuth azimuth
                              :elevation elevation
                              :linger linger
                              :speed speed}}})) 
 (m/=> scan-add-node [:=> [:cat :proto/int32-positive :proto/int32-positive :proto/int32-positive :angle/azimuth :angle/elevation [:double {:min 0.0}] :speed/normalized] :cmd/root])

;; ============================================================================
;; Meteo Data
;; ============================================================================

(defn get-meteo
  "Request meteorological data from rotary platform.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:rotary {:get_meteo {}}})) 
 (m/=> get-meteo [:=> [:cat] :cmd/root])