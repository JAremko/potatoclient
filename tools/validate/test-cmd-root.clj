(ns test-cmd-root
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load specs
   [potatoclient.specs.common]
   [potatoclient.specs.cmd.root]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Testing cmd/root ===\n")

;; Test validation
(let [valid-example {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :ping {}}]
  (println "Valid example:" valid-example)
  (println "Validates?" (m/validate :cmd/root valid-example))
  (println))

;; Test generation
(println "Testing generation...")
(try
  (dotimes [i 5]
    (let [generated (mg/generate :cmd/root)]
      (println (str "Generated " i ": " generated))
      (println "  Valid?" (m/validate :cmd/root generated))))
  (catch Exception e
    (println "Error:" (.getMessage e))
    (.printStackTrace e)))