(ns potatoclient.specs.cmd.cv
  "CV (Computer Vision) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_cv.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; CV command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def track-spec
  [:map {:closed true}
   [:x [:int]]
   [:y [:int]]])

(def stop-tracking-spec [:map {:closed true}])

;; Main CV command spec using oneof
(def cv-command-spec
  [:oneof_edn
   [:track track-spec]
   [:stop_tracking stop-tracking-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/cv cv-command-spec)