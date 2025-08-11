#!/usr/bin/env clojure

(require '[malli.core :as m])
(require '[potatoclient.malli.registry :as registry])
(require '[potatoclient.specs.oneof-edn :as oneof-edn])
(require '[potatoclient.specs.state.root])
(require '[potatoclient.specs.state.gps])
(require '[potatoclient.specs.state.lrf])

;; Initialize registry
(println "Initializing registry...")
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; Test basic schemas
(println "\nTesting basic schemas:")

;; Test GPS spec
(println "Testing GPS spec...")
(def gps-data
  {:altitude 0.289025
   :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
   :latitude 50.023626
   :longitude 15.815209
   :manual_latitude 50.023604
   :manual_longitude 15.815316})

(try
  (let [result (m/validate :state/gps gps-data)]
    (println "GPS validation:" result))
  (catch Exception e
    (println "GPS validation error:" (.getMessage e))))

;; Test LRF enum
(println "\nTesting LRF enum...")
(try
  (let [result (m/validate :enum/lrf-laser-pointer-mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF)]
    (println "LRF enum validation:" result))
  (catch Exception e
    (println "LRF enum validation error:" (.getMessage e))))

;; Test state root
(println "\nTesting state root schema...")
(try
  (let [schema (m/schema :state/root)]
    (println "State root schema loaded successfully"))
  (catch Exception e
    (println "State root schema error:" (.getMessage e))
    (println "Full error:" e)))

(println "\nChecking registry contents:")
(println "Registry keys:" (keys @registry/registry*))