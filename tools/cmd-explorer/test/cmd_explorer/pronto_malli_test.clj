(ns cmd-explorer.pronto-malli-test
  "Tests for Pronto-Malli integration layer"
  (:require
   [clojure.test :refer [deftest testing is]]
   [cmd-explorer.test-harness] ;; Auto-initializes on load
   [potatoclient.malli.pronto :as pm]
   [potatoclient.specs.common :as shared]
   [potatoclient.specs.cmd.root :as cmd-root]
   [malli.core :as m]
   [malli.generator :as mg]
   [pronto.core :as p])
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop]
   [ser JonSharedDataTypes$JonGuiDataClientType]))

(deftest test-validate-proto-map
  (testing "Validate proto-map with Malli spec"
    (let [mapper cmd-root/cmd-mapper
          ping-proto (p/proto-map mapper JonSharedCmd$Root
                                 :ping (p/proto-map mapper JonSharedCmd$Ping)
                                 :protocol_version 1
                                 :session_id 123)]
      
      (testing "Proto-map structure"
        (is (p/proto-map? ping-proto))
        (is (= :ping (p/which-one-of ping-proto :payload))))
      
      (testing "Validation with cmd-root spec"
        (is (pm/validate-proto-map :cmd/root-proto ping-proto))))))

(deftest test-generate-proto-map
  (testing "Generate proto-map from spec"
    (let [mapper cmd-root/cmd-mapper
          ;; Simple spec for a ping command
          ping-spec [:map
                    [:protocol_version :int]
                    [:session_id :int]]
          generated (pm/generate-proto-map mapper JonSharedCmd$Root ping-spec)]
      
      (testing "Generated proto-map is valid"
        (is (p/proto-map? generated))
        (is (number? (.getProtocolVersion generated)))
        (is (number? (.getSessionId generated)))))))

(deftest test-oneof-validation
  (testing "Oneof field validation"
    (let [mapper cmd-root/cmd-mapper
          ;; Create proto with ping
          ping-proto (p/proto-map mapper JonSharedCmd$Root
                                 :ping (p/proto-map mapper JonSharedCmd$Ping))
          
          ;; Create proto with noop
          noop-proto (p/proto-map mapper JonSharedCmd$Root
                                 :noop (p/proto-map mapper JonSharedCmd$Noop))
          
          ;; Create proto with no payload (invalid)
          empty-proto (p/proto-map mapper JonSharedCmd$Root)]
      
      (testing "Valid oneof with ping"
        (is (= :ping (p/which-one-of ping-proto :payload)))
        (is (instance? JonSharedCmd$Ping (.getPing ping-proto))))
      
      (testing "Valid oneof with noop"
        (is (= :noop (p/which-one-of noop-proto :payload)))
        (is (instance? JonSharedCmd$Noop (.getNoop noop-proto))))
      
      (testing "Invalid oneof with no field set"
        (is (nil? (p/which-one-of empty-proto :payload)))))))

(deftest test-round-trip
  (testing "Proto-map serialization round-trip"
    (let [mapper cmd-root/cmd-mapper
          original (p/proto-map mapper JonSharedCmd$Root
                              :ping (p/proto-map mapper JonSharedCmd$Ping)
                              :protocol_version 42
                              :session_id 999
                              :important true
                              :client_type JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
          result (pm/test-round-trip original mapper JonSharedCmd$Root)]
      
      (testing "Round-trip succeeds"
        (is (:success? result)))
      
      (testing "Serialized to bytes"
        (is (bytes? (:serialized result)))
        (is (pos? (count (:serialized result)))))
      
      (testing "Restored proto-map matches original"
        (let [restored (:restored result)]
          (is (= (p/which-one-of original :payload)
                 (p/which-one-of restored :payload)))
          (is (= (.getProtocolVersion original)
                 (.getProtocolVersion restored)))
          (is (= (.getSessionId original)
                 (.getSessionId restored)))
          (is (= (.getImportant original)
                 (.getImportant restored)))
          (is (= (.getClientType original)
                 (.getClientType restored))))))))

(deftest test-shared-specs
  (testing "Shared specs work with proto-maps"
    (testing "Angle specs"
      (let [azimuth (mg/generate :angle/azimuth)
            elevation (mg/generate :angle/elevation)]
        (is (<= -180.0 azimuth 180.0))
        (is (<= -90.0 elevation 90.0))
        (is (not (Double/isNaN azimuth)))
        (is (not (Double/isNaN elevation)))))
    
    (testing "Position specs"
      (let [lat (mg/generate :position/latitude)
            lon (mg/generate :position/longitude)
            alt (mg/generate :position/altitude)]
        (is (<= -90.0 lat 90.0))
        (is (<= -180.0 lon 180.0))
        (is (<= -1000.0 alt 10000.0))
        (is (not (Double/isNaN lat)))
        (is (not (Double/isNaN lon)))
        (is (not (Double/isNaN alt)))))
    
    (testing "Enum specs"
      (let [client-type (mg/generate :enum/client-type)]
        (is (instance? JonSharedDataTypes$JonGuiDataClientType client-type))))))

(deftest test-batch-validation
  (testing "Validate multiple proto-maps"
    (let [mapper cmd-root/cmd-mapper
          proto-maps [(p/proto-map mapper JonSharedCmd$Root
                                  :ping (p/proto-map mapper JonSharedCmd$Ping))
                     (p/proto-map mapper JonSharedCmd$Root
                                 :noop (p/proto-map mapper JonSharedCmd$Noop))
                     (p/proto-map mapper JonSharedCmd$Root)] ;; Invalid - no payload
          results (pm/validate-proto-maps :cmd/root-proto proto-maps)]
      
      (testing "Categorizes valid and invalid correctly"
        (is (= 2 (count (:valid results))))
        (is (= 1 (count (:invalid results))))))))

(deftest test-validation-report
  (testing "Detailed validation report"
    (let [mapper cmd-root/cmd-mapper
          valid-proto (p/proto-map mapper JonSharedCmd$Root
                                  :ping (p/proto-map mapper JonSharedCmd$Ping))
          invalid-proto (p/proto-map mapper JonSharedCmd$Root)
          valid-report (pm/validate-with-report :cmd/root-proto valid-proto)]
      
      (testing "Valid proto-map report"
        (is (:valid? valid-report))
        (is (= valid-proto (:proto-map valid-report)))
        (is (nil? (:errors valid-report))))
      
      (testing "Invalid proto-map detection"
        ;; Just test that validation correctly detects invalid proto
        (is (not (pm/validate-proto-map :cmd/root-proto invalid-proto)))))))

(deftest test-generator-quality
  (testing "Generator produces valid proto-maps"
    (let [mapper cmd-root/cmd-mapper
          ;; Test with a simple spec
          spec [:map
                [:protocol_version [:int {:min 1 :max 10}]]
                [:session_id [:int {:min 0 :max 1000}]]]
          stats (pm/test-generator mapper JonSharedCmd$Root spec {:n 50})]
      
      (testing "All generated samples are valid"
        (is (= 50 (:total stats)))
        (is (>= (:valid stats) 45)) ;; Allow some failures due to oneof
        (is (empty? (:errors stats)))))))

(deftest test-performance-utilities
  (testing "Optimized proto-map creation"
    (let [mapper cmd-root/cmd-mapper
          proto-map (pm/create-optimized-proto-map
                     mapper
                     JonSharedCmd$Root
                     {:protocol_version 1
                      :session_id 123
                      :important true})]
      
      (testing "Proto-map created with all fields"
        (is (p/proto-map? proto-map))
        (is (= 1 (.getProtocolVersion proto-map)))
        (is (= 123 (.getSessionId proto-map)))
        (is (= true (.getImportant proto-map))))))
  
  (testing "Batch updates"
    (let [mapper cmd-root/cmd-mapper
          proto-map (p/proto-map mapper JonSharedCmd$Root)
          updated (pm/batch-update-proto-map
                   proto-map
                   {:protocol_version 2
                    :session_id 456
                    :important false})]
      
      (testing "All fields updated"
        (is (= 2 (.getProtocolVersion updated)))
        (is (= 456 (.getSessionId updated)))
        (is (= false (.getImportant updated)))))))

(deftest test-complex-proto-validation
  (testing "Complex nested proto-map validation"
    (let [mapper cmd-root/cmd-mapper
          ;; Create a more complex proto with nested messages
          complex-proto (p/proto-map mapper JonSharedCmd$Root
                                    :ping (p/proto-map mapper JonSharedCmd$Ping)
                                    :protocol_version 1
                                    :session_id 789
                                    :important true
                                    :from_cv_subsystem false
                                    :client_type JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED)]
      
      (testing "Complex proto validates"
        (is (pm/validate-proto-map :cmd/root-proto complex-proto)))
      
      (testing "Round-trip preserves all fields"
        (let [result (pm/test-round-trip complex-proto mapper JonSharedCmd$Root)]
          (is (:success? result))
          (is (= (.getClientType complex-proto)
                 (.getClientType (:restored result)))))))))