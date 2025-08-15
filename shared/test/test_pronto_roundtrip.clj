(ns test-pronto-roundtrip
  "Test if Pronto works for round-trip with Malli-generated data"
  (:require
   [pronto.core :as pronto]
   [pronto.utils]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root])
  (:import [cmd JonSharedCmd$Root]))

;; Initialize registry
(registry/setup-global-registry!)

;; Define Pronto mapper at runtime (proto classes loaded dynamically)
(def cmd-mapper
  (eval '(do 
          (pronto.core/defmapper cmd-mapper-internal 
                                [cmd.JonSharedCmd$Root])
          cmd-mapper-internal)))

(defn test-roundtrip []
  (println "\n=== Testing Pronto Round-trip ===\n")
  
  ;; Test 1: Empty proto
  (println "1. Empty proto message:")
  (let [empty-bytes (byte-array [])]
    (try
      (let [proto-msg (JonSharedCmd$Root/parseFrom empty-bytes)
            proto-map (pronto/proto->proto-map cmd-mapper proto-msg)
            edn-data (pronto/proto-map->clj-map proto-map)]
        (println "   Empty proto -> EDN:" edn-data))
      (catch Exception e
        (println "   ERROR:" (.getMessage e)))))
  
  ;; Test 2: Minimal proto with just protocol_version
  (println "\n2. Minimal proto (protocol_version=1):")
  (let [msg (-> (JonSharedCmd$Root/newBuilder)
                 (.setProtocolVersion 1)
                 (.build))
        bytes (.toByteArray msg)]
    (try
      (let [proto-msg (JonSharedCmd$Root/parseFrom bytes)
            proto-map (pronto/proto->proto-map cmd-mapper proto-msg)
            edn-data (pronto/proto-map->clj-map proto-map)]
        (println "   Minimal proto -> EDN:" edn-data))
      (catch Exception e
        (println "   ERROR:" (.getMessage e)))))
  
  ;; Test 3: Malli-generated data round-trip
  (println "\n3. Malli-generated data round-trip:")
  (let [malli-data (mg/generate (m/schema :cmd/root))]
    (println "   Original Malli data keys:" (keys malli-data))
    (try
      ;; EDN -> Proto
      (let [proto-map (pronto/clj-map->proto-map cmd-mapper 
                                                 JonSharedCmd$Root 
                                                 malli-data)
            proto (pronto.utils/proto-map->proto proto-map)
            bytes (.toByteArray proto)]
        (println "   Successfully converted to proto bytes:" (count bytes) "bytes")
        
        ;; Proto -> EDN
        (let [proto-msg (JonSharedCmd$Root/parseFrom bytes)
              proto-map2 (pronto/proto->proto-map cmd-mapper proto-msg)
              edn-data (pronto/proto-map->clj-map proto-map2)]
          (println "   Round-trip data keys:" (keys edn-data))
          (println "   Keys match:" (= (set (keys malli-data)) 
                                       (set (keys edn-data))))))
      (catch Exception e
        (println "   ERROR:" (.getMessage e)))))
  
  (println "\n=== End Test ==="))

(test-roundtrip)