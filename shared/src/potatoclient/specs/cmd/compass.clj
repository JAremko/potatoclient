(ns potatoclient.specs.cmd.compass
  "Compass command specs matching buf.validate constraints.
   Based on jon_shared_cmd_compass.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Compass command specs - placeholder implementation
;; This is a oneof structure with multiple command types

(def set-magnetic-declination-spec
  [:map {:closed true}
   [:value [:float {:min -180.0 :max 180.0}]]])

(def calibrate-spec [:map {:closed true}])
(def stop-calibration-spec [:map {:closed true}])

;; Main compass command spec using oneof
(def compass-command-spec
  [:oneof_edn
   [:set_magnetic_declination set-magnetic-declination-spec]
   [:calibrate calibrate-spec]
   [:stop_calibration stop-calibration-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/compass compass-command-spec)