(ns potatoclient.cmd-oneof-test
  "Test to understand how Pronto handles oneof fields in cmd.Root"
  (:require
   [clojure.test :refer [deftest is testing]]
   [pronto.core :as pronto]
   [potatoclient.test-harness :as harness]))

;; Ensure harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Define mapper for cmd.Root
(pronto/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

(deftest test-oneof-payload-structure
  (testing "Testing different payload structures for cmd.Root - documenting Pronto's oneof behavior"
    
    (testing "EXPECTED FAILURE: Payload wrapper with all fields set to nil except one"
      (let [edn-with-all-nils {:protocol_version 1
                                :session_id 1
                                :important false
                                :from_cv_subsystem false
                                :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                :payload {:osd nil
                                          :ping {}  ; Only ping is active
                                          :system nil
                                          :noop nil
                                          :cv nil
                                          :gps nil
                                          :lrf nil
                                          :lira nil
                                          :lrf_calib nil
                                          :rotary nil
                                          :day_cam_glass_heater nil
                                          :heat_camera nil
                                          :compass nil
                                          :day_camera nil
                                          :frozen nil}}]
        (println "\n=== Testing payload with all fields (nils except one) ===")
        (println "EDN input:" edn-with-all-nils)
        (println "Expected to fail: Pronto does not support :payload wrapper")
        (try
          (let [proto-map (pronto/clj-map->proto-map cmd-mapper cmd.JonSharedCmd$Root edn-with-all-nils)
                proto (pronto/proto-map->proto proto-map)]
            (println "Unexpected success! Proto created:" proto)
            (is false "Should have failed - :payload wrapper is not supported"))
          (catch Exception e
            (println "Failed as expected with error:" (.getMessage e))
            (is (re-find #"No such field :payload" (.getMessage e))
                "Should fail with 'No such field :payload' error")))))
    
    (testing "EXPECTED FAILURE: Payload wrapper with only the active field"
      (let [edn-only-active {:protocol_version 1
                              :session_id 1
                              :important false
                              :from_cv_subsystem false
                              :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                              :payload {:ping {}}}]  ; Only the active field present
        (println "\n=== Testing payload with only active field ===")
        (println "EDN input:" edn-only-active)
        (println "Expected to fail: Pronto does not support :payload wrapper")
        (try
          (let [proto-map (pronto/clj-map->proto-map cmd-mapper cmd.JonSharedCmd$Root edn-only-active)
                proto (pronto/proto-map->proto proto-map)]
            (println "Unexpected success! Proto created:" proto)
            (is false "Should have failed - :payload wrapper is not supported"))
          (catch Exception e
            (println "Failed as expected with error:" (.getMessage e))
            (is (re-find #"No such field :payload" (.getMessage e))
                "Should fail with 'No such field :payload' error")))))
    
    (testing "CORRECT: Direct field at root level (no :payload wrapper)"
      (let [edn-direct {:protocol_version 1
                         :session_id 1
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}]  ; Direct field, no :payload wrapper
        (println "\n=== Testing direct field (no :payload wrapper) ===")
        (println "EDN input:" edn-direct)
        (println "This is the correct format - Pronto expects flattened oneof fields")
        (try
          (let [proto-map (pronto/clj-map->proto-map cmd-mapper cmd.JonSharedCmd$Root edn-direct)
                proto (pronto/proto-map->proto proto-map)]
            (println "Success! Proto created:" proto)
            (is (some? proto) "Should successfully convert with flattened structure"))
          (catch Exception e
            (println "Unexpected failure:" (.getMessage e))
            (is false "Should not fail with flattened structure")))))
    
    (testing "CORRECT: Testing with a more complex payload (system command)"
      (let [edn-system {:protocol_version 1
                         :session_id 1
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :system {:start_all {}}}]  ; System command directly
        (println "\n=== Testing system command (direct) ===")
        (println "EDN input:" edn-system)
        (println "This is the correct format - flattened oneof field")
        (try
          (let [proto-map (pronto/clj-map->proto-map cmd-mapper cmd.JonSharedCmd$Root edn-system)
                proto (pronto/proto-map->proto proto-map)]
            (println "Success! Proto created:" proto)
            (is (some? proto) "Should successfully convert with flattened structure"))
          (catch Exception e
            (println "Unexpected failure:" (.getMessage e))
            (is false "Should not fail with flattened structure")))))))