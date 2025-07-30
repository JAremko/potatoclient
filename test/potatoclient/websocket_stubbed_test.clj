(ns potatoclient.websocket-stubbed-test
  "WebSocket tests using stubbed communication - no real server needed"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.core.async :as async]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.cmd.rotary :as rotary]
            [potatoclient.test-utils :as test-utils]))

(deftest test-single-command-stubbed
  (testing "Can send a single command through stubbed WebSocket"
    (test-utils/with-mock-websocket
      (fn [{:keys [commands-ch errors states]}]
        ;; Send a ping command
        (cmd/send-cmd-ping)

        ;; Get commands with short timeout (no network delays!)
        (let [commands (test-utils/get-commands commands-ch 100)]
          (is (= 1 (count commands)) "Should receive exactly one command")
          (when (= 1 (count commands))
            (let [cmd (first commands)]
              (is (true? (.hasPing cmd)) "Should be a ping command")
              (is (= 1 (.getProtocolVersion cmd)) "Should have protocol version 1"))))

        ;; Verify no errors
        (is (empty? @errors) "Should have no errors")))))

(deftest test-multiple-commands-stubbed
  (testing "Can send multiple commands in quick succession"
    (test-utils/with-mock-websocket
      (fn [{:keys [commands-ch]}]
        ;; Send multiple commands quickly
        (cmd/send-cmd-ping)
        (cmd/send-cmd-noop)
        (cmd/send-cmd-frozen)

        ;; Get all commands
        (let [commands (test-utils/get-commands commands-ch 100)]
          (is (= 3 (count commands)) "Should receive all three commands")
          (when (= 3 (count commands))
            (is (true? (.hasPing (nth commands 0))) "First should be ping")
            (is (true? (.hasNoop (nth commands 1))) "Second should be noop")
            (is (true? (.hasFrozen (nth commands 2))) "Third should be frozen")))))))

(deftest test-rotary-set-value-commands-stubbed
  (testing "Can send rotary set value commands"
    (test-utils/with-mock-websocket
      (fn [{:keys [commands-ch]}]
        ;; Use the rotary namespace functions

        ;; Send rotary set platform commands
        (rotary/rotary-set-platform-azimuth 45.0)
        (rotary/rotary-set-platform-elevation 30.0)

        (let [commands (test-utils/get-commands commands-ch 100)]
          (is (= 2 (count commands)) "Should receive two commands")
          (when (>= (count commands) 2)
            (let [cmd1 (first commands)
                  cmd2 (second commands)]
              (is (true? (.hasRotary cmd1)) "First should have rotary command")
              (is (true? (.hasRotary cmd2)) "Second should have rotary command")
              (when (.hasRotary cmd1)
                (let [rotary (.getRotary cmd1)]
                  (is (true? (.hasSetPlatformAzimuth rotary)) "First should be set platform azimuth")
                  (when (.hasSetPlatformAzimuth rotary)
                    (is (= 45.0 (.getValue (.getSetPlatformAzimuth rotary))) "Should have correct azimuth value"))))
              (when (.hasRotary cmd2)
                (let [rotary (.getRotary cmd2)]
                  (is (true? (.hasSetPlatformElevation rotary)) "Second should be set platform elevation")
                  (when (.hasSetPlatformElevation rotary)
                    (is (= 30.0 (.getValue (.getSetPlatformElevation rotary))) "Should have correct elevation value")))))))))))

(deftest test-read-only-mode-stubbed
  (testing "Read-only mode blocks non-allowed commands"
    (test-utils/with-mock-websocket
      (fn [{:keys [commands-ch]}]

        ;; Enable read-only mode
        (cmd/set-read-only-mode! true)

        (try
          ;; Try to send various commands
          (cmd/send-cmd-noop)  ; Should be blocked
          (rotary/rotary-set-platform-azimuth 45.0)  ; Should be blocked
          (cmd/send-cmd-ping)  ; Should be allowed
          (cmd/send-cmd-frozen)  ; Should be allowed

          (let [commands (test-utils/get-commands commands-ch 100)]
            (is (= 2 (count commands)) "Should only receive ping and frozen")
            (when (= 2 (count commands))
              (is (true? (.hasPing (first commands))) "First should be ping")
              (is (true? (.hasFrozen (second commands))) "Second should be frozen")))

          (finally
            ;; Reset read-only mode
            (cmd/set-read-only-mode! false)))))))

(deftest test-state-reception-stubbed
  (testing "Can receive state updates through mock"
    (test-utils/with-mock-websocket
      (fn [{:keys [send-state-fn states]}]
        ;; Send a test state
        (let [test-state (test-utils/create-test-state)]
          (send-state-fn test-state)

          ;; Give a tiny bit of time for async processing
          (Thread/sleep 10)

          ;; Check we received it
          (is (= 1 (count @states)) "Should receive one state update")
          (when (= 1 (count @states))
            ;; The state is received as bytes, so we'd need to parse it
            (is (bytes? (first @states)) "State should be received as bytes")))))))

(deftest test-connection-lifecycle-stubbed
  (testing "Mock manager tracks connection state"
    (let [mock-ctx (test-utils/create-mock-websocket-manager nil nil)]
      (try
        (is (false? @(:connected? mock-ctx)) "Should start disconnected")

        (.start (:manager mock-ctx))
        (is (true? @(:connected? mock-ctx)) "Should be connected after start")
        (is (true? (.isConnected (:manager mock-ctx))) "isConnected should return true")

        (.stop (:manager mock-ctx))
        (is (false? @(:connected? mock-ctx)) "Should be disconnected after stop")
        (is (false? (.isConnected (:manager mock-ctx))) "isConnected should return false")

        (finally
          (when (.isConnected (:manager mock-ctx))
            (.stop (:manager mock-ctx))))))))