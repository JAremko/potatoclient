(ns potatoclient.specs.cmd.lrf
  "LRF (Laser Range Finder) command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lrf.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; LRF command specs - simplified placeholders
;; This is a oneof structure with multiple command types

(def fire-spec [:map {:closed true}])
(def stop-spec [:map {:closed true}])
(def set-mode-spec
  [:map {:closed true}
   [:mode [:enum :RANGE :POINTER]]])

;; Main LRF command spec using oneof
(def lrf-command-spec
  [:oneof_edn
   [:fire fire-spec]
   [:stop stop-spec]
   [:set_mode set-mode-spec]
   ;; Add more commands as needed
   ])

(registry/register! :cmd/lrf lrf-command-spec)