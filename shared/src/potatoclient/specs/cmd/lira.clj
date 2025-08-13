(ns potatoclient.specs.cmd.lira
  "LIRA command specs matching buf.validate constraints.
   Based on jon_shared_cmd_lira.proto.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.malli.oneof-edn :as oneof-edn]))

;; LIRA command specs - based on proto-explorer findings
;; This is a oneof structure with one command type

;; Target specification
(def lira-target-spec
  [:map {:closed true}
   [:x [:double]]
   [:y [:double]]
   [:width [:double]]
   [:height [:double]]])

;; Refine target command
(def refine-target-spec
  [:map {:closed true}
   [:target lira-target-spec]])

;; Main LIRA command spec using oneof
(def lira-command-spec
  [:oneof_edn
   [:refine_target refine-target-spec]])

(registry/register! :cmd/lira lira-command-spec)