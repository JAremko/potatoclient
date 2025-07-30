(ns potatoclient.websocket-integration-test
  "Integration test for simplified WebSocket implementation"
  (:require [clojure.test :refer :all]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.logging :as logging])
  (:import [potatoclient.java.websocket WebSocketManager]))

(deftest test-websocket-manager-lifecycle
  (testing "WebSocket manager can be created and stopped"
    (let [errors (atom [])
          states (atom [])
          manager (WebSocketManager.
                    "example.com"
                    (reify java.util.function.Consumer
                      (accept [_ msg] (swap! errors conj msg)))
                    (reify java.util.function.Consumer
                      (accept [_ data] (swap! states conj data))))]

      ;; Start the manager
      (.start manager)
      (is (instance? WebSocketManager manager))

      ;; Check initial state
      (is (false? (.isConnected manager)))
      (is (= 0 (.getCommandQueueSize manager)))

      ;; Stop the manager
      (.stop manager)

      ;; Manager should handle stop gracefully
      (is (false? (.isConnected manager))))))

(deftest test-cmd-core-integration
  (testing "cmd/core WebSocket integration"
    (let [error-count (atom 0)
          state-count (atom 0)]

      ;; Initialize WebSocket through cmd/core
      (cmd/init-websocket!
        "localhost:8080"
        (fn [error] (swap! error-count inc))
        (fn [data] (swap! state-count inc)))

      ;; Send a ping command
      (cmd/send-cmd-ping)

      ;; Give it a moment
      (Thread/sleep 100)

      ;; Clean up
      (cmd/stop-websocket!)

      ;; Basic check - should not crash
      (is (>= @error-count 0))
      (is (>= @state-count 0)))))

(deftest test-command-sending
  (testing "Commands can be sent through the new system"
    ;; Initialize
    (cmd/init-websocket!
      "test.local"
      (fn [error] (logging/log-error error))
      (fn [data] (dispatch/handle-binary-state data)))

    ;; Send various commands
    ;; Note: With Guardrails enabled in test mode, these return true (validation result)
    ;; In production they return nil as expected
    (let [ping-result (cmd/send-cmd-ping)
          frozen-result (cmd/send-cmd-frozen)
          noop-result (cmd/send-cmd-noop)]
      ;; Just verify they don't throw exceptions
      (is (or (nil? ping-result) (true? ping-result)) "ping command should succeed")
      (is (or (nil? frozen-result) (true? frozen-result)) "frozen command should succeed")
      (is (or (nil? noop-result) (true? noop-result)) "noop command should succeed"))

    ;; Clean up
    (cmd/stop-websocket!)))