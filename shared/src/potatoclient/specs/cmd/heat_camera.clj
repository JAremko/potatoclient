(ns potatoclient.specs.cmd.heat-camera
  "Heat Camera command specs matching buf.validate constraints.
   Based on jon_shared_cmd_heat_camera.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Heat camera command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def calibrate-spec [:map {:closed true}])
(def set-palette-spec
  [:map {:closed true}
   [:palette [:enum :WHITE_HOT :BLACK_HOT :RAINBOW]]])
(def set-agc-spec
  [:map {:closed true}
   [:mode [:enum :AUTO :MANUAL]]])

;; Main Heat Camera command spec using oneof
(def heat-camera-command-spec
  [:oneof_edn
   [:calibrate calibrate-spec]
   [:set_palette set-palette-spec]
   [:set_agc set-agc-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/heat-camera heat-camera-command-spec)