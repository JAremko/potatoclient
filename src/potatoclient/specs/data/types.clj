(ns potatoclient.specs.data.types
  "Common types used across protobuf data specs")

;; Common video channel types
(def video-channel
  "Video channel selection"
  [:enum :heat :day])

;; Common direction types  
(def rotary-direction
  "Rotary movement direction"
  [:enum :clockwise :counter-clockwise])

;; Common mode types
(def rotary-mode
  "Rotary operation mode"
  [:enum :stabilized :platform :scanning])

;; System localizations
(def system-localizations
  "Available system languages"
  [:enum :en :ua :cs :ar :fr :de :es])

;; Glass heater modes
(def glass-heater-mode
  "Day camera glass heater operation mode"
  [:enum :auto :manual :off])

;; Time sync status
(def time-sync-status
  "Time synchronization status"
  [:enum :none :ntp :gps :manual])

;; Camera palettes (for thermal camera)
(def thermal-palette
  "Thermal camera color palette"
  [:enum :white-hot :black-hot :rainbow :iron :amber :indigo :tyrian :glory :envy :fusion])

;; GPS fix types
(def fix-type
  "GPS fix type"
  [:enum :no-fix :2d :3d :dgps :rtk])