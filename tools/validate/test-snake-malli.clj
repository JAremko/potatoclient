(ns test-snake-malli
  (:require [validate.validator :as v]
            [pronto.core :as p]
            [malli.core :as m]
            [potatoclient.malli.registry :as registry]
            [potatoclient.specs.state.root]
            [potatoclient.specs.cmd.root]
            [potatoclient.specs.oneof-edn :as oneof-edn]
            [clojure.pprint :as pprint])
  (:import [ser JonSharedData$JonGUIState]))

;; Initialize registry
(registry/setup-global-registry!
 (oneof-edn/register_ONeof-edn-schema!))

(defn test-malli-validation []
  (println "Testing Malli validation with snake_case specs...")
  
  ;; Create a simple valid state
  (let [state-proto (p/proto-map v/state-mapper ser.JonSharedData$JonGUIState
                                 :protocol_version 1
                                 :gps (p/proto-map v/state-mapper JonSharedDataGps
                                                  :latitude 50.0
                                                  :longitude 15.0
                                                  :altitude 100.0
                                                  :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D)
                                 :system (p/proto-map v/state-mapper JonSharedDataSystem
                                                     :cpu_load 50.0
                                                     :gpu_load 30.0
                                                     :rec_enabled true)
                                 :time (p/proto-map v/state-mapper JonSharedDataTime
                                                   :timestamp 1754664759))
        state-bytes (p/proto-map->bytes state-proto)
        result (v/validate-binary state-bytes :type :state)]
    
    (println "\nValidation result:")
    (println "  Overall valid?:" (:valid? result))
    (println "  buf.validate valid?:" (get-in result [:buf-validate :valid?]))
    (println "  Malli valid?:" (get-in result [:malli :valid?]))
    
    (when-not (get-in result [:malli :valid?])
      (println "\nMalli violations:")
      (doseq [v (get-in result [:malli :violations])]
        (println "  -" (:field v) ":" (:message v))))
    
    ;; Let's also test the spec directly
    (println "\nTesting Malli spec directly...")
    (let [spec (m/schema :state/root)
          ;; Get EDN from proto-bytes
          state-proto (p/bytes->proto-map v/state-mapper 
                                          ser.JonSharedData$JonGUIState
                                          state-bytes)
          edn-data (p/proto-map->clj-map state-proto)]
      
      (println "EDN data keys (first 5):" (take 5 (keys edn-data)))
      
      (if (m/validate spec edn-data)
        (println "✓ Direct Malli validation passed!")
        (do
          (println "✗ Direct Malli validation failed!")
          (let [explanation (m/explain spec edn-data)]
            (println "Errors:")
            (pprint/pprint (:errors explanation))))))))

(test-malli-validation)