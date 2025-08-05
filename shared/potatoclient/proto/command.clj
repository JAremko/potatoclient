(ns potatoclient.proto.command
  "Commands index - re-exports all command namespaces"
  (:require [potatoclient.proto.cmd.lira :as cmd.lira]
            [potatoclient.proto.cmd.heatcamera :as cmd.heatcamera]
            [potatoclient.proto.cmd.system :as cmd.system]
            [potatoclient.proto.cmd.lrf :as cmd.lrf]
            [potatoclient.proto.cmd.compass :as cmd.compass]
            [potatoclient.proto.cmd.osd :as cmd.osd]
            [potatoclient.proto.cmd.rotaryplatform :as cmd.rotaryplatform]
            [potatoclient.proto.cmd.gps :as cmd.gps]
            [potatoclient.proto.cmd.daycamglassheater :as cmd.daycamglassheater]
            [potatoclient.proto.cmd.cv :as cmd.cv]
            [potatoclient.proto.cmd.daycamera :as cmd.daycamera]
            [potatoclient.proto.cmd.lrf-calib :as cmd.lrf-calib]))

;; Re-export all public functions from sub-namespaces
;; This supports testing without needing to know the internal namespace
;; structure
