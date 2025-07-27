(ns potatoclient.proto-constraints-test
  "Tests to verify that buf.validate constraints in proto files are properly
   enforced at runtime when using buf.validate-enabled bindings."
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.proto :as proto])
  (:import (cmd JonSharedCmd$Root
                JonSharedCmd$Ping)
           (cmd.DayCamera JonSharedCmdDayCamera$SetValue
                          JonSharedCmdDayCamera$Move)
           (ser JonSharedDataTypes$JonGuiDataClientType)))

(deftest test-proto-messages-can-be-created
  (testing "Proto messages can be created with constraint-violating values"
    (testing "Protocol version 0 (should be >= 1 per proto constraint)"
      (let [cmd (-> (JonSharedCmd$Root/newBuilder)
                    (.setProtocolVersion 0) ; Would violate constraint
                    (.setSessionId 12345)
                    (.setImportant false)
                    (.setFromCvSubsystem false)
                    (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
                    (.setPing (JonSharedCmd$Ping/newBuilder))
                    (.build))]
        (is (= 0 (.getProtocolVersion cmd))
            "Can create message with protocol version 0 (constraints not enforced)")))

    (testing "DayCamera SetValue > 1.0 (should be <= 1.0 per proto constraint)"
      (let [value-msg (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                          (.setValue 1.5) ; Would violate constraint
                          (.build))]
        (is (> (.getValue value-msg) 1.0)
            "Can create SetValue with value > 1.0 (constraints not enforced)")))

    (testing "DayCamera SetValue < 0.0 (should be >= 0.0 per proto constraint)"
      (let [value-msg (-> (JonSharedCmdDayCamera$SetValue/newBuilder)
                          (.setValue -0.1) ; Would violate constraint
                          (.build))]
        (is (< (.getValue value-msg) 0.0)
            "Can create SetValue with negative value (constraints not enforced)")))

    (testing "DayCamera Move speed > 1.0 (should be <= 1.0 per proto constraint)"
      (let [move-msg (-> (JonSharedCmdDayCamera$Move/newBuilder)
                         (.setTargetValue 0.5)
                         (.setSpeed 2.0) ; Would violate constraint
                         (.build))]
        (is (> (.getSpeed move-msg) 1.0)
            "Can create Move with speed > 1.0 (constraints not enforced)")))))

(deftest test-application-level-validation
  (testing "Application can implement its own validation based on proto constraints"
    (testing "Example validation function for SetValue"
      (let [validate-set-value
            (fn [value]
              (and (>= value 0.0) (<= value 1.0)))

            valid-value 0.5
            invalid-high 1.5
            invalid-low -0.1]

        (is (validate-set-value valid-value)
            "0.5 is valid")
        (is (not (validate-set-value invalid-high))
            "1.5 is invalid (too high)")
        (is (not (validate-set-value invalid-low))
            "-0.1 is invalid (too low)")))

    (testing "Example validation function for protocol version"
      (let [validate-protocol-version
            (fn [version]
              (>= version 1))

            valid-version 1
            invalid-version 0]

        (is (validate-protocol-version valid-version)
            "Version 1 is valid")
        (is (not (validate-protocol-version invalid-version))
            "Version 0 is invalid")))))

(deftest test-proto-serialization-works
  (testing "Proto serialization enforces constraints with buf.validate"
    (testing "Valid values serialize successfully"
      (let [cmd {:protocol-version 1 ; Valid: >= 1
                 :session-id 12345
                 :important false
                 :from-cv-subsystem false
                 :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
                 :ping {}}
            serialized (proto/serialize-cmd cmd)]
        (is (bytes? serialized)
            "Can serialize command with valid values")))

    (testing "Invalid values are rejected by buf.validate"
      (let [cmd {:protocol-version 0 ; Violates constraint: must be >= 1
                 :session-id 12345
                 :important false
                 :from-cv-subsystem false
                 :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
                 :ping {}}]
        (is (thrown-with-msg? Exception #"Failed to serialize command"
                              (proto/serialize-cmd cmd))
            "Buf.validate rejects constraint-violating values")))))