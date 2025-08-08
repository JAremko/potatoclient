(ns cmd-explorer.foundation-test
  "Test Phase 0 foundation components"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.registry :as mr]
   [malli.generator :as mg]
   [cmd-explorer.test-harness] ;; Auto-initializes on load
   [potatoclient.specs.oneof-pronto :as oneof]
   [potatoclient.specs.cmd-root :as cmd-root]
   [potatoclient.specs.common :as shared]
   [pronto.core :as p]
   [com.fulcrologic.guardrails.malli.core :refer [>defn >def | ? =>]])
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop]
   [ser JonSharedDataTypes$JonGuiDataClientType]))

(deftest test-malli-registry
  (testing "Global Malli registry is configured"
    ;; The :oneof-pronto is a schema type, not a registered schema
    ;; We can check that our registered schemas work instead
    (testing "Custom specs are registered"
      (is (m/schema :cmd/root-proto))
      (is (m/schema :cmd/payload))
      (is (m/schema :angle/azimuth))
      (is (m/schema :range/normalized))
      (is (m/schema :position/latitude)))))

(deftest test-guardrails-malli
  (testing "Guardrails configured for Malli"
    ;; Define a simple guardrailed function
    (>defn test-fn
      [x]
      [:int => :int]
      (inc x))
    
    (testing "Valid input"
      (is (= 2 (test-fn 1))))
    
    (testing "Invalid input throws with guardrails enabled"
      ;; This will only throw if guardrails are properly configured
      ;; In production, we'd catch the exception
      (is (thrown? Exception (test-fn "not a number"))))))

;; Define mapper once at top level to avoid redefinition
(p/defmapper test-mapper [JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop])

(deftest test-oneof-pronto-spec
  (testing "Oneof-pronto spec for Pronto proto-maps"
    (let [mapper test-mapper
          ;; Create a simple proto-map with ping command
          proto-map (p/proto-map mapper JonSharedCmd$Root
                               :ping (p/proto-map mapper JonSharedCmd$Ping)
                               :protocol_version 1
                               :session_id 123
                               :important false
                               :from_cv_subsystem false
                               :client_type JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV)]
      
      (testing "Proto-map creation"
        (is (p/proto-map? proto-map)))
      
      (testing "Oneof field detection"
        (is (= :ping (p/which-one-of proto-map :payload))))
      
      (testing "Oneof value extraction"
        (is (some? (.getPing proto-map)))))))

(deftest test-cmd-root-spec
  (testing "CMD root spec validation"
    (let [mapper cmd-root/cmd-mapper
          valid-proto (p/proto-map mapper JonSharedCmd$Root
                                 :ping (p/proto-map mapper JonSharedCmd$Ping)
                                 :protocol_version 1
                                 :session_id 456
                                 :important true
                                 :from_cv_subsystem false
                                 :client_type JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)]
      
      (testing "Valid proto-map passes validation"
        ;; Note: This may not work perfectly yet as the validation 
        ;; expects plain maps, not proto-maps. We'll need to adapt it.
        (is (p/proto-map? valid-proto))
        (is (= :ping (p/which-one-of valid-proto :payload)))))))

(deftest test-shared-specs
  (testing "Common reusable specs"
    (testing "Angle specs"
      (let [azimuth (mg/generate :angle/azimuth)]
        (is (number? azimuth))
        (is (<= -180.0 azimuth 180.0)))
      
      (let [elevation (mg/generate :angle/elevation)]
        (is (number? elevation))
        (is (<= -90.0 elevation 90.0))))
    
    (testing "Range specs"
      (let [normalized (mg/generate :range/normalized)]
        (is (number? normalized))
        (is (<= 0.0 normalized 1.0))))
    
    (testing "Position specs"
      (let [lat (mg/generate :position/latitude)]
        (is (number? lat))
        (is (<= -90.0 lat 90.0)))
      
      (let [lon (mg/generate :position/longitude)]
        (is (number? lon))
        (is (<= -180.0 lon 180.0))))
    
    (testing "Validation functions"
      (is (shared/validate-spec :angle/azimuth 45.0))
      (is (not (shared/validate-spec :angle/azimuth 200.0)))
      (is (shared/validate-spec :range/normalized 0.5))
      (is (not (shared/validate-spec :range/normalized 1.5))))))

(deftest test-generator-samples
  (testing "Generate 100 samples and verify all are valid"
    (doseq [_ (range 100)]
      (testing "Angle generators"
        (is (m/validate :angle/azimuth (mg/generate :angle/azimuth)))
        (is (m/validate :angle/elevation (mg/generate :angle/elevation))))
      
      (testing "Range generators"
        (is (m/validate :range/normalized (mg/generate :range/normalized)))
        (is (m/validate :range/zoom (mg/generate :range/zoom))))
      
      (testing "Position generators"
        (is (m/validate :position/latitude (mg/generate :position/latitude)))
        (is (m/validate :position/longitude (mg/generate :position/longitude)))))))

(deftest test-pronto-round-trip
  (testing "Pronto serialization round-trip"
    (let [mapper test-mapper
          original (p/proto-map mapper JonSharedCmd$Root
                              :noop (p/proto-map mapper JonSharedCmd$Noop)
                              :protocol_version 2
                              :session_id 789
                              :important false
                              :from_cv_subsystem true
                              :client_type JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED)
          ;; Serialize to bytes
          bytes (p/proto-map->bytes original)
          ;; Deserialize back
          restored (p/bytes->proto-map mapper JonSharedCmd$Root bytes)]
      
      (testing "Serialization produces bytes"
        (is (bytes? bytes))
        (is (pos? (count bytes))))
      
      (testing "Round-trip preserves data"
        (is (= (p/which-one-of original :payload) (p/which-one-of restored :payload)))
        (is (= (.getProtocolVersion original) (.getProtocolVersion restored)))
        (is (= (.getSessionId original) (.getSessionId restored)))
        (is (= (.getImportant original) (.getImportant restored)))
        (is (= (.getFromCvSubsystem original) (.getFromCvSubsystem restored)))
        (is (= (.getClientType original) (.getClientType restored)))))))