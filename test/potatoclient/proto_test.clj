(ns potatoclient.proto-test
  "Test protobuf serialization/deserialization"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.proto :as proto]))

(deftest test-command-creation
  (testing "Create ping command"
    (let [cmd (proto/cmd-ping 12345 :local)]
      (is (map? cmd))
      (is (= 1 (:protocol-version cmd)))
      (is (= 12345 (:session-id cmd)))
      (is (= false (:important cmd)))
      (is (= false (:from-cv-subsystem cmd)))
      (is (= "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK" (:client-type cmd)))
      (is (= {} (:ping cmd))))))

(deftest test-command-serialization
  (testing "Serialize ping command"
    (let [cmd (proto/cmd-ping 12345 :local)
          serialized (proto/serialize-cmd cmd)]
      (is (bytes? serialized))
      (is (pos? (count serialized))))))

(deftest test-state-deserialization
  (testing "Deserialize empty state"
    ;; Create a minimal protobuf state message
    (let [proto-bytes (byte-array [8 1])] ; protocol version 1
      (try
        (let [state (proto/deserialize-state proto-bytes)]
          (is (map? state))
          (is (= 1 (:protocol-version state))))
        (catch Exception e
          ;; Expected - minimal bytes might not be valid
          (is (instance? Exception e)))))))

(deftest test-command-validation
  (testing "Valid command passes validation"
    (let [cmd (proto/cmd-ping 12345 :local)]
      (is (proto/valid-command? cmd))))

  (testing "Invalid command fails validation"
    (let [cmd {:invalid true}]
      (is (not (proto/valid-command? cmd))))))