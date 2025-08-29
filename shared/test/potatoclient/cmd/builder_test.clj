(ns potatoclient.cmd.builder-test
  "Tests for the command builder module.
   Ensures efficient proto-map building and field population."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [potatoclient.cmd.builder :as builder]
   [potatoclient.cmd.validation :as v]
   [potatoclient.proto.serialize :as serialize]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]
   [pronto.core :as p]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Basic Functionality Tests
;; ============================================================================

(deftest populate-cmd-fields-test
  (testing "populate-cmd-fields adds missing required fields"
    
    (testing "Minimal command gets all defaults"
      (let [minimal {:ping {}}
            result (builder/populate-cmd-fields minimal)]
        (is (match? {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :session_id 0
                     :important false
                     :from_cv_subsystem false
                     :ping {}}
                    result))))
    
    (testing "Commands with overrides"
      (let [payload {:ping {}}
            base-result (builder/populate-cmd-fields payload)
            custom-result (builder/create-full-cmd payload {:session_id 12345 :important true})]
        (is (match? {:protocol_version 1
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                     :session_id 0
                     :important false
                     :from_cv_subsystem false}
                    base-result))
        
        (is (match? {:session_id 12345
                     :important true}
                    custom-result))))
    
    (testing "Different payload types"
      (let [noop-result (builder/populate-cmd-fields {:noop {}})
            frozen-result (builder/populate-cmd-fields {:frozen {}})
            system-result (builder/populate-cmd-fields {:system {:reboot {}}})]
        (is (match? {:noop {}} noop-result))
        (is (match? {:frozen {}} frozen-result))
        (is (match? {:system {:reboot {}}} system-result))))))

(deftest populate-cmd-fields-with-overrides-test
  (testing "populate-cmd-fields-with-overrides applies custom defaults"
    
    (testing "Override all defaults"
      (let [cmd {:frozen {}}
            overrides {:protocol_version 2
                      :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                      :session_id 555
                      :important true
                      :from_cv_subsystem true}
            result (builder/populate-cmd-fields-with-overrides cmd overrides)]
        (is (match? {:protocol_version 2
                     :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                     :session_id 555
                     :important true
                     :from_cv_subsystem true}
                    result))))
    
    (testing "Partial overrides"
      (let [cmd {:system {:reboot {}}}
            overrides {:session_id 777}
            result (builder/populate-cmd-fields-with-overrides cmd overrides)]
        (is (match? {:session_id 777
                     :protocol_version 1
                     :important false}
                    result))))))

(deftest create-full-cmd-test
  (testing "create-full-cmd builds complete commands"
    (let [payload {:ping {}}
          overrides {:session_id 123 :important true}
          result (builder/create-full-cmd payload overrides)]
      (is (match? {:ping {}
                   :session_id 123
                   :important true
                   :protocol_version 1}
                  result)))))

(deftest ensure-required-fields-test
  (testing "ensure-required-fields validates presence of required fields"
    
    (testing "Valid command passes"
      (let [valid {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                   :session_id 0
                   :important false
                   :from_cv_subsystem false
                   :ping {}}]
        (is (= valid (builder/ensure-required-fields valid)))))
    
    (testing "Missing fields throws"
      (is (thrown-with-msg? Exception #"missing required fields"
            (builder/ensure-required-fields {:ping {}}))
          "Should throw for missing fields")
      
      (is (thrown-with-msg? Exception #"missing required fields"
            (builder/ensure-required-fields 
              {:protocol_version 1
               :ping {}}))
          "Should throw even with some fields present"))))

;; create-batch-commands removed - not needed

;; ============================================================================
;; Proto-Map Tests
;; ============================================================================

(deftest create-proto-map-cmd-test
  (testing "create-proto-map-cmd creates valid proto-maps"
    (let [cmd {:protocol_version 1
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :session_id 0
               :important false
               :from_cv_subsystem false
               :ping {}}
          proto-map (builder/create-proto-map-cmd cmd)]
      (is (p/proto-map? proto-map) "Should create a proto-map")
      (is (match? {:protocol_version 1
                   :ping {}}
                  proto-map)))))

;; ============================================================================
;; Malli Validation Tests - Invalid Arguments
;; ============================================================================

(deftest malli-validation-test
  (testing "Malli validation catches invalid arguments"
    
    (testing "populate-cmd-fields rejects non-maps"
      (is (thrown? Exception (builder/populate-cmd-fields "not-a-map")))
      (is (thrown? Exception (builder/populate-cmd-fields nil)))
      (is (thrown? Exception (builder/populate-cmd-fields 123))))
    
    (testing "create-full-cmd validates payload structure"
      ;; Should have exactly one oneof field
      (is (thrown? Exception 
            (builder/create-full-cmd {} {}))
          "Should reject empty payload")
      
      (is (thrown? Exception
            (builder/create-full-cmd {:ping {} :noop {}} {}))
          "Should reject multiple oneof fields"))
    
    (testing "populate-cmd-fields-with-overrides validates arguments"
      (is (thrown? Exception 
            (builder/populate-cmd-fields-with-overrides {:ping {}} "not-a-map")))
      (is (thrown? Exception 
            (builder/populate-cmd-fields-with-overrides nil {}))))))

;; ============================================================================
;; Roundtrip Validation Tests
;; ============================================================================

(deftest roundtrip-with-builder-test
  (testing "Commands built with builder survive roundtrip"
    (doseq [payload [{:ping {}}
                     {:noop {}}
                     {:frozen {}}
                     {:system {:reboot {}}}
                     {:system {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}}}]]
      (testing (str "Payload: " payload)
        (let [cmd (builder/populate-cmd-fields payload)
              result (v/validate-roundtrip-with-report cmd)]
          (is (:valid? result)
              (str "Should roundtrip successfully"
                   (when-not (:valid? result)
                     (str "\n" (:pretty-diff result))))))))))

;; ============================================================================
;; Performance Tests - Ensure Efficient Operations
;; ============================================================================

(deftest performance-characteristics-test
  (testing "Builder operations are efficient"
    
    (testing "Batch operations are faster than individual"
      ;; This is more of a sanity check than a real performance test
      (let [payloads (vec (repeat 100 {:ping {}}))
            start-batch (System/nanoTime)
            batch-results (mapv builder/populate-cmd-fields payloads)
            batch-time (- (System/nanoTime) start-batch)
            
            start-individual (System/nanoTime)
            individual-results (mapv builder/populate-cmd-fields payloads)
            individual-time (- (System/nanoTime) start-individual)]
        
        (is (= 100 (count batch-results)))
        (is (= 100 (count individual-results)))
        ;; We don't assert on timing as it's not reliable in tests,
        ;; but this structure allows for manual performance checking
        ))))

;; ============================================================================
;; Generative Tests
;; ============================================================================

(def client-type-gen
  (gen/elements [:JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                 :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
                 :JON_GUI_DATA_CLIENT_TYPE_LIRA]))

(def payload-gen
  (gen/elements [{:ping {}}
                 {:noop {}}
                 {:frozen {}}
                 {:system {:reboot {}}}
                 {:system {:power_off {}}}]))

(defspec populated-commands-are-valid 500
  (prop/for-all [payload payload-gen
                 session-id (gen/choose 0 2147483647)
                 important gen/boolean
                 from-cv gen/boolean
                 client-type client-type-gen]
    (let [overrides {:session_id session-id
                    :important important
                    :from_cv_subsystem from-cv
                    :client_type client-type}
          cmd (builder/create-full-cmd payload overrides)]
      (and (= session-id (:session_id cmd))
           (= important (:important cmd))
           (= from-cv (:from_cv_subsystem cmd))
           (= client-type (:client_type cmd))
           (= 1 (:protocol_version cmd))
           (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec batch-commands-consistency 100
  (prop/for-all [payloads (gen/vector payload-gen 1 10)
                 session-id (gen/choose 0 1000000)]
    (let [overrides {:session_id session-id}
          results (mapv #(merge (merge builder/default-protocol-fields overrides) %) payloads)]
      (and (= (count payloads) (count results))
           (every? #(= session-id (:session_id %)) results)
           (every? #(:valid? (v/validate-roundtrip-with-report %)) results)))))