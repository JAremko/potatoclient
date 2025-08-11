(ns test-malli-direct
  (:require [malli.core :as m]
            [malli.error :as me]
            [clojure.edn :as edn]
            [potatoclient.malli.registry :as registry]
            [potatoclient.specs.common]  ;; Load common specs first
            [potatoclient.specs.state.root]
            [potatoclient.specs.oneof-edn :as oneof-edn]
            [clojure.pprint :as pprint]))

;; Initialize registry
(registry/setup-global-registry!
 (oneof-edn/register_ONeof-edn-schema!))

(defn test-direct []
  (println "Testing Malli validation directly...")
  
  ;; Read the EDN file
  (let [edn-data (edn/read-string (slurp "test/resources/valid-state.edn"))
        spec (m/schema :state/root)]
    
    (println "\nEDN keys:" (keys edn-data))
    (println "\nValidating with Malli...")
    
    (if (m/validate spec edn-data)
      (println "✓ Validation passed!")
      (do
        (println "✗ Validation failed!")
        (let [explanation (m/explain spec edn-data)
              humanized (me/humanize explanation)]
          (println "\nHumanized errors:")
          (pprint/pprint humanized))))))

(test-direct)