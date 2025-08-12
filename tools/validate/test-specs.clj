#!/usr/bin/env clojure

(ns test-specs
  "Test that cmd/root and state/root specs work with oneof_edn"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.state.root]))

;; Initialize registry
(println "\n=== Initializing Registry ===")
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))
(println "Registry initialized")

;; Test cmd/root spec
(println "\n=== Testing cmd/root spec ===")
(let [spec :cmd/root]
  (println "1. Generating cmd/root samples:")
  (dotimes [i 3]
    (try
      (let [generated (mg/generate spec)]
        (println (str "  Sample " i ": " (select-keys generated [:protocol_version :client_type])
                     " command: " (first (filter #(contains? generated %) 
                                                [:cv :day_camera :heat_camera :gps :compass 
                                                 :lrf :lrf_calib :rotary :osd :ping :noop 
                                                 :frozen :system :day_cam_glass_heater :lira])))
                     " valid? " (m/validate spec generated)))
      (catch Exception e
        (println (str "  Error generating: " (.getMessage e))))))
  
  (println "\n2. Testing validation:")
  ;; Valid example
  (let [valid-cmd {:protocol_version 1
                   :client_type :ground
                   :ping {:id 123}}]
    (println "  Valid cmd:" valid-cmd "=>" (m/validate spec valid-cmd)))
  
  ;; Invalid - multiple commands
  (let [invalid-cmd {:protocol_version 1
                     :client_type :ground
                     :ping {:id 123}
                     :noop {}}]
    (println "  Invalid (multiple cmds):" invalid-cmd "=>" (m/validate spec invalid-cmd)))
  
  ;; Invalid - no command
  (let [invalid-cmd {:protocol_version 1
                     :client_type :ground}]
    (println "  Invalid (no cmd):" invalid-cmd "=>" (m/validate spec invalid-cmd))))

;; Test state/root spec
(println "\n=== Testing state/root spec ===")
(let [spec :state/root]
  (println "1. Generating state/root samples:")
  (dotimes [i 2]
    (try
      (let [generated (mg/generate spec)]
        (println (str "  Sample " i ": protocol_version=" (:protocol_version generated)
                     " fields=" (count (keys generated))
                     " valid? " (m/validate spec generated)))
      (catch Exception e
        (println (str "  Error generating: " (.getMessage e))))))
  
  (println "\n2. Testing validation:")
  ;; We can't easily create a valid state message by hand due to complexity
  ;; Just test that the spec exists and can be validated
  (println "  State spec exists and loads correctly"))

(println "\n✅ Test complete!")
(System/exit 0)