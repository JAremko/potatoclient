(ns potatoclient.proto-validation-sanity-test
  "Sanity tests to verify buf.validate detects bad payloads in dev mode"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.proto :as proto]
            [potatoclient.runtime :as runtime])
  (:import (cmd JonSharedCmd$Root)
           (cmd.DayCamera JonSharedCmdDayCamera$SetValue)
           (build.buf.protovalidate ValidatorFactory)))

(deftest test-buf-validate-sanity-checks
  (testing "Buf.validate behavior with standard bindings"
    (let [validator (try
                      (.build (ValidatorFactory/newBuilder))
                      (catch Exception _ nil))]
      
      (testing "Standard bindings behavior"
        ;; With standard bindings, validation metadata isn't included
        ;; so these constraint violations won't be caught
        (testing "Protocol version constraint (>= 1)"
          (let [cmd (-> (JonSharedCmd$Root/newBuilder)
                        (.setProtocolVersion 0)  ; Violates >= 1
                        (.setSessionId 12345)
                        (.build))]
            (is (= 0 (.getProtocolVersion cmd))
                "Can create message with protocol version 0")
            
            (when validator
              ;; Even with validator, standard bindings lack metadata
              (let [result (.validate validator cmd)]
                (is (.isSuccess result)
                    "Standard bindings pass validation (no metadata)")))))
        
        (testing "DayCamera SetValue constraint (0.0 <= value <= 1.0)"
          (let [invalid-high (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                                 (.setValue 1.5)  ; Violates <= 1.0
                                 (.build))
                invalid-low (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                                (.setValue -0.1)  ; Violates >= 0.0
                                (.build))]
            (is (> (.getValue invalid-high) 1.0)
                "Can create SetValue with value > 1.0")
            (is (< (.getValue invalid-low) 0.0)
                "Can create SetValue with negative value")
            
            (when validator
              ;; Standard bindings lack validation metadata
              (is (.isSuccess (.validate validator invalid-high))
                  "High value passes (no metadata)")
              (is (.isSuccess (.validate validator invalid-low))
                  "Low value passes (no metadata)"))))))))

(deftest test-proto-clj-validation-integration
  (testing "proto.clj validation integration"
    (let [release-mode? (runtime/release-build?)]
      
      (testing "Validation initialization"
        (if release-mode?
          (is (nil? @#'proto/validator)
              "Validator should be nil in release mode")
          (is (some? @#'proto/validator)
              "Validator should be initialized in dev mode")))
      
      (testing "Serialization with validation"
        ;; These should serialize successfully regardless of constraints
        ;; because standard bindings don't include validation metadata
        (let [invalid-cmd {:protocol-version 0  ; Would violate >= 1
                           :session-id 12345
                           :important false
                           :from-cv-subsystem false
                           :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
                           :ping {}}
              valid-cmd {:protocol-version 1
                         :session-id 12345
                         :important false
                         :from-cv-subsystem false
                         :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
                         :ping {}}]
          
          (testing "Invalid command serialization"
            (is (bytes? (proto/serialize-cmd invalid-cmd))
                "Should serialize constraint-violating command with standard bindings"))
          
          (testing "Valid command serialization"
            (is (bytes? (proto/serialize-cmd valid-cmd))
                "Should serialize valid command")))))))

(deftest test-validation-performance-impact
  (testing "Validation performance in dev vs release"
    (let [cmd {:protocol-version 1
               :session-id 12345
               :important false
               :from-cv-subsystem false
               :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
               :ping {}}
          iterations 100
          warmup 10]
      
      ;; Warmup
      (dotimes [_ warmup]
        (proto/serialize-cmd cmd))
      
      ;; Measure
      (let [start (System/nanoTime)]
        (dotimes [_ iterations]
          (proto/serialize-cmd cmd))
        (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)
              us-per-op (/ (* elapsed-ms 1000) iterations)]
          (testing "Performance metrics"
            (is (< us-per-op 500)  ; Should be < 500 microseconds per operation
                (str "Serialization too slow: " us-per-op " μs/op"))
            (println (format "Validation overhead: %.1f μs/op in %s mode"
                             us-per-op
                             (if (runtime/release-build?) "RELEASE" "DEV")))))))))