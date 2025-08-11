(ns check-registry
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.malli.registry :as registry]
            [potatoclient.specs.state.root]
            [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
 (oneof-edn/register_ONeof-edn-schema!))

(println "Checking registry...")
(println "Registry type:" (type @registry/*registry*))

;; Check if :state/root exists
(println "\nLooking for :state/root...")
(try
  (let [schema (m/schema :state/root)]
    (println "Found :state/root schema:" schema))
  (catch Exception e
    (println "Error getting :state/root:" (.getMessage e))))

;; List some registered schemas
(println "\nSome registered schemas:")
(doseq [k [:proto/protocol-version :state/actual-space-time :state/camera-day
           :state/gps :state/system :state/time]]
  (try
    (if (m/schema k)
      (println "  ✓" k)
      (println "  ✗" k "not found"))
    (catch Exception e
      (println "  ✗" k "error:" (.getMessage e)))))