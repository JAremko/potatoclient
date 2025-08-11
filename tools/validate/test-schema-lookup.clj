(ns test-schema-lookup
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.malli.registry :as registry]
            ;; Load all specs
            [potatoclient.specs.state.root]
            [potatoclient.specs.state.actual-space-time]
            [potatoclient.specs.state.camera-day]
            [potatoclient.specs.state.camera-heat]
            [potatoclient.specs.state.compass]
            [potatoclient.specs.state.compass-calibration]
            [potatoclient.specs.state.day-cam-glass-heater]
            [potatoclient.specs.state.gps]
            [potatoclient.specs.state.lrf]
            [potatoclient.specs.state.meteo-internal]
            [potatoclient.specs.state.rec-osd]
            [potatoclient.specs.state.rotary]
            [potatoclient.specs.state.system]
            [potatoclient.specs.state.time]
            [potatoclient.specs.common]
            [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(println "Initializing registry...")
(registry/setup-global-registry!
 (oneof-edn/register_ONeof-edn-schema!))

(println "\nTrying to get :state/root schema...")
(try
  (let [schema (m/schema :state/root)]
    (println "Success! Schema type:" (type schema)))
  (catch Exception e
    (println "Failed:" (.getMessage e))
    (.printStackTrace e)))