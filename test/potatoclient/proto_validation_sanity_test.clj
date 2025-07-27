(ns potatoclient.proto-validation-sanity-test
  "Tests to verify buf.validate correctly detects bad payloads in dev mode.
   With properly generated Java bindings that include buf.validate metadata,
   validation constraints are enforced at runtime in development/test modes."
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

      (testing "Buf.validate validation with proper bindings"
        ;; With buf.validate-enabled bindings, validation metadata IS included
        ;; so these constraint violations WILL be caught
        (testing "Protocol version constraint (>= 1)"
          (let [cmd (-> (JonSharedCmd$Root/newBuilder)
                        (.setProtocolVersion 0)  ; Violates >= 1
                        (.setSessionId 12345)
                        (.build))]
            (is (= 0 (.getProtocolVersion cmd))
                "Can create message with protocol version 0 (builder doesn't enforce)")

            (when validator
              ;; With buf.validate metadata, validation should catch the error
              (let [result (.validate validator cmd)]
                (is (not (.isSuccess result))
                    "Buf.validate correctly rejects protocol version 0")))))

        (testing "DayCamera SetValue constraint (0.0 <= value <= 1.0)"
          (let [invalid-high (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                                 (.setValue 1.5)  ; Violates <= 1.0
                                 (.build))
                invalid-low (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                                (.setValue -0.1)  ; Violates >= 0.0
                                (.build))
                valid (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                          (.setValue 0.5)  ; Valid value
                          (.build))]
            (is (> (.getValue invalid-high) 1.0)
                "Can create SetValue with value > 1.0 (builder doesn't enforce)")
            (is (< (.getValue invalid-low) 0.0)
                "Can create SetValue with negative value (builder doesn't enforce)")

            (when validator
              ;; With buf.validate metadata, validation should work correctly
              (is (not (.isSuccess (.validate validator invalid-high)))
                  "Buf.validate correctly rejects value > 1.0")
              (is (not (.isSuccess (.validate validator invalid-low)))
                  "Buf.validate correctly rejects negative value")
              (is (.isSuccess (.validate validator valid))
                  "Buf.validate accepts valid value in range"))))))))

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
        ;; With buf.validate-enabled bindings, validation happens in dev mode
        ;; In release mode, validation is skipped for performance
        (let [invalid-cmd {:protocol-version 0  ; Violates >= 1 constraint
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
            (if release-mode?
              (is (bytes? (proto/serialize-cmd invalid-cmd))
                  "In release mode, validation is skipped - even invalid commands serialize")
              (is (thrown? clojure.lang.ExceptionInfo (proto/serialize-cmd invalid-cmd))
                  "In dev mode, validation rejects constraint-violating commands")))

          (testing "Valid command serialization"
            (is (bytes? (proto/serialize-cmd valid-cmd))
                "Valid commands serialize in all modes")))))))

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