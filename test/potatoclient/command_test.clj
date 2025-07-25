(ns potatoclient.command-test
  "Simple tests for command generation"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.cmd.core :as cmd-core]
            [potatoclient.proto :as proto])
  (:import [cmd JonSharedCmd$Root]))

(deftest test-basic-commands
  (testing "Basic command creation"
    ;; Test that we can create basic command messages
    (let [root-msg (cmd-core/create-root-message)]
      (is (some? root-msg) "Should create root message")
      (is (= 1 (.getProtocolVersion root-msg)) "Should have protocol version 1")

      ;; Add ping
      (.setPing root-msg (cmd.JonSharedCmd$Ping/newBuilder))

      ;; Convert to bytes
      (let [proto-bytes (cmd-core/encode-cmd-message root-msg)]
        (is (bytes? proto-bytes) "Should encode to bytes")
        (is (pos? (count proto-bytes)) "Should have content")

        ;; Parse back
        (let [parsed (JonSharedCmd$Root/parseFrom proto-bytes)]
          (is (.hasPing parsed) "Should have ping")
          (is (= 1 (.getProtocolVersion parsed)) "Should preserve protocol version"))))))