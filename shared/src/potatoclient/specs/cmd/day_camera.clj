(ns potatoclient.specs.cmd.day-camera
  "Day Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_day_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Day camera command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def zoom-in-spec [:map {:closed true}])
(def zoom-out-spec [:map {:closed true}])
(def set-zoom-spec
  [:map {:closed true}
   [:level [:int {:min 1 :max 30}]]])
(def focus-spec
  [:map {:closed true}
   [:distance [:float {:min 0.0}]]])

;; Main Day Camera command spec using oneof
(def day-camera-command-spec
  [:oneof_edn
   [:zoom_in zoom-in-spec]
   [:zoom_out zoom-out-spec]
   [:set_zoom set-zoom-spec]
   [:focus focus-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/day-camera day-camera-command-spec)