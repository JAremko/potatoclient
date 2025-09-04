(ns potatoclient.cmd.lrf-alignment
  "LRF Alignment/Calibration command functions for adjusting laser-to-camera alignment.
   Based on the Lrf_calib message structure in jon_shared_cmd_lrf_align.proto."
  (:require
    [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Day Camera Offset Operations
;; ============================================================================

(defn set-day-offsets
  "Set the LRF alignment offsets for the day camera.
   X: Horizontal offset in pixels (-1920 to 1920)
   Y: Vertical offset in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  (core/create-command
    {:lrf_calib {:day {:set {:x x :y y}}}}))
(m/=> set-day-offsets [:=> [:cat :screen/pixel-offset-x :screen/pixel-offset-y] :cmd/root])

(defn shift-day-offsets
  "Shift the LRF alignment offsets for the day camera by relative amounts.
   X: Horizontal shift in pixels (-1920 to 1920)
   Y: Vertical shift in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  (core/create-command
    {:lrf_calib {:day {:shift {:x x :y y}}}}))
(m/=> shift-day-offsets [:=> [:cat :screen/pixel-offset-x :screen/pixel-offset-y] :cmd/root])

(defn save-day-offsets
  "Save the current LRF alignment offsets for the day camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:lrf_calib {:day {:save {}}}}))
(m/=> save-day-offsets [:=> [:cat] :cmd/root])

(defn reset-day-offsets
  "Reset the LRF alignment offsets for the day camera to defaults.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:lrf_calib {:day {:reset {}}}}))
(m/=> reset-day-offsets [:=> [:cat] :cmd/root])

;; ============================================================================
;; Heat Camera Offset Operations
;; ============================================================================

(defn set-heat-offsets
  "Set the LRF alignment offsets for the heat camera.
   X: Horizontal offset in pixels (-1920 to 1920)
   Y: Vertical offset in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  (core/create-command
    {:lrf_calib {:heat {:set {:x x :y y}}}}))
(m/=> set-heat-offsets [:=> [:cat :screen/pixel-offset-x :screen/pixel-offset-y] :cmd/root])

(defn shift-heat-offsets
  "Shift the LRF alignment offsets for the heat camera by relative amounts.
   X: Horizontal shift in pixels (-1920 to 1920)
   Y: Vertical shift in pixels (-1080 to 1080)
   Returns a fully formed cmd root ready to send."
  [x y]
  (core/create-command
    {:lrf_calib {:heat {:shift {:x x :y y}}}}))
(m/=> shift-heat-offsets [:=> [:cat :screen/pixel-offset-x :screen/pixel-offset-y] :cmd/root])

(defn save-heat-offsets
  "Save the current LRF alignment offsets for the heat camera.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:lrf_calib {:heat {:save {}}}}))
(m/=> save-heat-offsets [:=> [:cat] :cmd/root])

(defn reset-heat-offsets
  "Reset the LRF alignment offsets for the heat camera to defaults.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command
    {:lrf_calib {:heat {:reset {}}}}))
(m/=> reset-heat-offsets [:=> [:cat] :cmd/root])
