(ns generator.test-registry
  "Setup combined registry for tests"
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [test.roundtrip.ser :as ser]
            [test.roundtrip.cmd :as cmd]
            [test.roundtrip.cmd.lira :as lira]
            [test.roundtrip.cmd.heatcamera :as heatcamera]
            [test.roundtrip.cmd.daycamera :as daycamera]
            [test.roundtrip.cmd.system :as system]
            [test.roundtrip.cmd.lrf :as lrf]
            [test.roundtrip.cmd.compass :as compass]
            [test.roundtrip.cmd.osd :as osd]
            [test.roundtrip.cmd.rotaryplatform :as rotaryplatform]
            [test.roundtrip.cmd.gps :as gps]
            [test.roundtrip.cmd.daycamglassheater :as daycamglassheater]
            [test.roundtrip.cmd.cv :as cv]
            [test.roundtrip.cmd.lrf-calib :as lrf-calib]))

(defn setup-registry!
  "Setup the combined registry for all namespaces"
  []
  (let [combined-registry (merge
                           {:oneof oneof/-oneof-schema}
                           ser/registry
                           cmd/registry
                           lira/registry
                           heatcamera/registry
                           daycamera/registry
                           system/registry
                           lrf/registry
                           compass/registry
                           osd/registry
                           rotaryplatform/registry
                           gps/registry
                           daycamglassheater/registry
                           cv/registry
                           lrf-calib/registry)]
    (mr/set-default-registry!
     (mr/composite-registry
      (m/default-schemas)
      combined-registry))))