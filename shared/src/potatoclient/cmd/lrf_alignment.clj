(ns potatoclient.cmd.lrf-alignment
  "LRF Alignment/Calibration command functions for adjusting laser-to-camera alignment.
   Based on the Lrf_calib message structure in jon_shared_cmd_lrf_align.proto."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Day Camera Offset Operations
;; ============================================================================

(>defn set-day-offsets
  "Set the LRF alignment offsets for the day camera.
   X: Horizontal offset in pixels (-1920 to 1920)
   Y: Vertical offset in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  [:screen/pixel-offset-x :screen/pixel-offset-y => :cmd/root]
  (core/create-command 
    {:lrf_calib {:day {:set {:x x :y y}}}}))

(>defn shift-day-offsets
  "Shift the LRF alignment offsets for the day camera by relative amounts.
   X: Horizontal shift in pixels (-1920 to 1920)
   Y: Vertical shift in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  [:screen/pixel-offset-x :screen/pixel-offset-y => :cmd/root]
  (core/create-command 
    {:lrf_calib {:day {:shift {:x x :y y}}}}))

(>defn save-day-offsets
  "Save the current LRF alignment offsets for the day camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command 
    {:lrf_calib {:day {:save {}}}}))

(>defn reset-day-offsets
  "Reset the LRF alignment offsets for the day camera to defaults.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command 
    {:lrf_calib {:day {:reset {}}}}))

;; ============================================================================
;; Heat Camera Offset Operations
;; ============================================================================

(>defn set-heat-offsets
  "Set the LRF alignment offsets for the heat camera.
   X: Horizontal offset in pixels (-1920 to 1920)
   Y: Vertical offset in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  [:screen/pixel-offset-x :screen/pixel-offset-y => :cmd/root]
  (core/create-command 
    {:lrf_calib {:heat {:set {:x x :y y}}}}))

(>defn shift-heat-offsets
  "Shift the LRF alignment offsets for the heat camera by relative amounts.
   X: Horizontal shift in pixels (-1920 to 1920)
   Y: Vertical shift in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  [:screen/pixel-offset-x :screen/pixel-offset-y => :cmd/root]
  (core/create-command 
    {:lrf_calib {:heat {:shift {:x x :y y}}}}))

(>defn save-heat-offsets
  "Save the current LRF alignment offsets for the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command 
    {:lrf_calib {:heat {:save {}}}}))

(>defn reset-heat-offsets
  "Reset the LRF alignment offsets for the heat camera to defaults.
   Returns a fully formed cmd root ready to send."
  []
  [=> :cmd/root]
  (core/create-command 
    {:lrf_calib {:heat {:reset {}}}}))

;; ============================================================================
;; Convenience Functions
;; ============================================================================

(>defn calibrate-day-camera
  "Convenience function to set and save day camera offsets in one operation.
   X: Horizontal offset in pixels (-1920 to 1920)
   Y: Vertical offset in pixels (-1080 to 1080)
   Returns a vector of two cmd roots: [set-command save-command]."
  [x y]
  [:screen/pixel-offset-x :screen/pixel-offset-y => [:vector :cmd/root]]
  [(set-day-offsets x y)
   (save-day-offsets)])

(>defn calibrate-heat-camera
  "Convenience function to set and save heat camera offsets in one operation.
   X: Horizontal offset in pixels (-1920 to 1920)
   Y: Vertical offset in pixels (-1080 to 1080)
   Returns a vector of two cmd roots: [set-command save-command]."
  [x y]
  [:screen/pixel-offset-x :screen/pixel-offset-y => [:vector :cmd/root]]
  [(set-heat-offsets x y)
   (save-heat-offsets)])