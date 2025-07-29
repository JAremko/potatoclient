(ns potatoclient.test-utils
  "Test utilities for stubbing WebSocket communication"
  (:require [clojure.core.async :as async]
            [clojure.spec.alpha :as s]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.cmd.core :as cmd])
  (:import (cmd JonSharedCmd$Root)
           (ser JonSharedData$JonGUIState)))

;; ============================================================================
;; Mock WebSocket Manager
;; ============================================================================

(defn create-mock-websocket-manager
  "Create a mock WebSocket manager that captures commands and can send state updates.
   Returns a map with :manager, :commands-ch, :send-state-fn"
  [error-callback state-callback]
  (let [commands-ch (async/chan 100)
        connected? (atom false)
        manager (proxy [potatoclient.java.websocket.WebSocketManager] 
                  ["mock-domain" nil nil]
                  (start []
                    (reset! connected? true))
                  (stop []
                    (reset! connected? false)
                    (async/close! commands-ch))
                  (sendCommand [^JonSharedCmd$Root command]
                    (when @connected?
                      (async/put! commands-ch command)
                      true))
                  (isConnected []
                    @connected?))
        send-state-fn (fn [^JonSharedData$JonGUIState state]
                       (when (and @connected? state-callback)
                         (state-callback (.toByteArray state))))]
    {:manager manager
     :commands-ch commands-ch
     :send-state-fn send-state-fn
     :connected? connected?}))

;; ============================================================================
;; Test Helpers
;; ============================================================================

(>defn with-mock-websocket
  "Execute test function with a mock WebSocket manager.
   The test-fn receives the mock context map."
  [test-fn]
  [ifn? => any?]
  (let [errors (atom [])
        states (atom [])
        mock-ctx (create-mock-websocket-manager
                   (fn [error] (swap! errors conj error))
                   (fn [state] (swap! states conj state)))]
    ;; Install the mock manager
    (reset! @#'cmd/websocket-manager (:manager mock-ctx))
    (try
      (.start (:manager mock-ctx))
      (test-fn (assoc mock-ctx
                      :errors errors
                      :states states))
      (finally
        (.stop (:manager mock-ctx))
        (reset! @#'cmd/websocket-manager nil)))))

(>defn get-commands
  "Get all commands sent to the mock manager.
   Returns a vector of commands received so far."
  [commands-ch timeout-ms]
  [any? pos-int? => vector?]
  (let [commands (atom [])]
    (loop []
      (let [[cmd ch] (async/alts!! [commands-ch 
                                    (async/timeout timeout-ms)])]
        (when (and cmd (= ch commands-ch))
          (swap! commands conj cmd)
          (recur))))
    @commands))

(>defn await-command
  "Wait for a specific command type with timeout.
   Returns the command if found, nil if timeout."
  [commands-ch predicate-fn timeout-ms]
  [any? ifn? pos-int? => (? any?)]
  (let [deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (let [remaining (- deadline (System/currentTimeMillis))]
        (if (pos? remaining)
          (let [[cmd ch] (async/alts!! [commands-ch 
                                        (async/timeout remaining)])]
            (cond
              (nil? cmd) nil  ; Channel closed or timeout
              (= ch commands-ch)
              (if (predicate-fn cmd)
                cmd
                (recur))
              :else nil))
          nil)))))

;; ============================================================================
;; State Builders
;; ============================================================================

(defn create-test-state
  "Create a minimal test GUI state"
  []
  (-> (ser.JonSharedData$JonGUIState/newBuilder)
      (.build)))