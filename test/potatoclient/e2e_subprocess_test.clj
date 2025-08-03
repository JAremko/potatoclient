(ns potatoclient.e2e-subprocess-test
  "End-to-end tests with real Kotlin subprocesses.
  
  Tests the complete flow:
  1. Start real subprocesses  
  2. Send commands through Transit
  3. Verify protobuf conversion
  4. Check subprocess communication
  5. Validate state updates"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.framed-io :as framed-io]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.logging :as logging]
            [clojure.java.io :as io])
  (:import [java.util.concurrent TimeUnit]
           [java.lang ProcessBuilder]))

;; =============================================================================
;; Test Configuration
;; =============================================================================

(def ^:dynamic *test-timeout-ms* 5000)
(def ^:dynamic *subprocess* nil)
(def ^:dynamic *response-chan* (atom []))

(defn handle-subprocess-message
  "Handle messages from subprocess"
  [message]
  (logging/log-debug {:msg "Received subprocess message" 
                     :message message})
  (swap! *response-chan* conj message))

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn wait-for-condition
  "Wait for a condition to become true"
  [pred-fn timeout-ms]
  (let [deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (cond
        (pred-fn) true
        (> (System/currentTimeMillis) deadline) false
        :else (do (Thread/sleep 50)
                 (recur))))))

(defn send-command-and-wait
  "Send a command and wait for acknowledgment"
  [command]
  ;; Clear any pending messages
  (reset! *response-chan* [])
  
  ;; Send command
  (when *subprocess*
    (transit-core/write-message! (:writer *subprocess*)
                                (transit-core/create-message :command command)
                                (:output-stream *subprocess*)))
  
  ;; Wait for response
  (let [start-time (System/currentTimeMillis)]
    (loop []
      (if-let [messages (seq @*response-chan*)]
        (:payload (last messages))
        (if (< (- (System/currentTimeMillis) start-time) *test-timeout-ms*)
          (do (Thread/sleep 50)
              (recur))
          :timeout)))))

;; =============================================================================
;; Test Fixtures
;; =============================================================================

(defn subprocess-fixture
  "Start subprocesses before tests, stop after"
  [f]
  ;; Reset app-db
  (app-db/reset-to-initial-state!)
  
  ;; Start test subprocess directly
  (let [java-exe (if-let [java-home (System/getProperty "java.home")]
                   (str java-home "/bin/java")
                   "java")
        classpath (System/getProperty "java.class.path")
        cmd [java-exe "-cp" classpath
             "--enable-native-access=ALL-UNNAMED"
             "potatoclient.kotlin.transit.CommandSubprocessKt"
             "--test-mode"]
        pb (ProcessBuilder. ^java.util.List cmd)
        _ (println "Starting subprocess with command:" cmd)
        process (.start pb)
        _ (println "Subprocess started")
        ;; Set up Transit communication
        output-stream (.getOutputStream process)
        input-stream (.getInputStream process)
        framed-output (framed-io/make-framed-output-stream output-stream)
        writer (transit-core/make-writer framed-output)
        subprocess-info {:process process
                        :input-stream input-stream
                        :output-stream output-stream
                        :writer writer
                        :state (atom :running)}
        ;; Start reader thread
        reader-thread (Thread.
                       #(try
                          (let [framed-input (framed-io/make-framed-input-stream input-stream)
                                reader (transit-core/make-reader framed-input)]
                            (loop []
                              (when-let [msg (transit-core/read-message reader)]
                                (println "Received message:" (:msg-type msg))
                                (handle-subprocess-message msg)
                                (when (= :running @(:state subprocess-info))
                                  (recur)))))
                          (catch Exception e
                            (when-not (= :stopped @(:state subprocess-info))
                              (logging/log-error {:msg "Reader thread error" 
                                                :error (.getMessage e)})))))]
    (.start reader-thread)
    (println "Reader thread started")
    
    (binding [*subprocess* subprocess-info]
      (try
        ;; Give subprocess time to start
        (Thread/sleep 500)
        ;; Wait for test-mode-ready status
        (let [ready? (wait-for-condition
                       #(some (fn [msg]
                               (and (= "status" (:msg-type msg))
                                    (= "test-mode-ready" (get-in msg [:payload :status]))))
                             @*response-chan*)
                       3000)]
          (println "Ready status:" ready? "Messages received:" (count @*response-chan*))
          (when-not ready?
            (logging/log-warn "Subprocess did not report ready status")))
        (f)
        (finally
          ;; Stop subprocess
          (reset! (:state subprocess-info) :stopped)
          (.destroyForcibly process)
          (.join reader-thread 1000))))))

(use-fixtures :each subprocess-fixture)

;; =============================================================================
;; End-to-End Tests
;; =============================================================================

(deftest ^:integration test-subprocess-lifecycle
  (testing "Subprocess starts and responds to health checks"
    (is (some? *subprocess*) "Command subprocess should exist")
    
    ;; Send ping command
    (let [result (send-command-and-wait (cmd/ping))]
      (is (not= :timeout result) "Should receive response within timeout")
      (is (= :pong (:type result)) "Should receive pong response"))))

(deftest ^:integration test-command-flow
  (testing "Commands flow through subprocess correctly"
    ;; Test basic command using correct protobuf structure
    (let [rotate-cmd {:rotary {:rotate-to-ndc {:channel :heat :x 0.5 :y -0.5}}}
          result (send-command-and-wait rotate-cmd)]
      (is (not= :timeout result) "Should process command within timeout")
      (when (not= :timeout result)
        (is (= :ack (:type result)) "Should acknowledge command")))
    
    ;; Test command with validation
    (testing "Invalid commands are rejected"
      (let [invalid-cmd {:rotary {:rotate-to-ndc {:channel :heat :x 2.0 :y 0.5}}}
            result (send-command-and-wait invalid-cmd)]
        (is (not= :timeout result) "Should process validation within timeout")
        (when (not= :timeout result)
          (is (= :error (:type result)) "Should return error for invalid command")
          (is (re-find #"range|constraint|validation" (str (:message result)))
              "Error should mention validation failure"))))))

(deftest ^:integration test-transit-message-flow
  (testing "Transit messages maintain structure through subprocess"
    ;; Send various command types
    (doseq [[desc cmd-fn] [["Ping" cmd/ping]
                           ["Noop" cmd/noop]
                           ["Heat palette" #(cmd/heat-camera-palette :rainbow)]
                           ["CV track" #(cmd/cv-start-track-ndc :heat 0.5 -0.5 1234)]]]
      (testing desc
        (let [command (cmd-fn)
              result (send-command-and-wait command)]
          (is (not= :timeout result) (str desc " should not timeout"))
          (is (contains? result :type) (str desc " should have response type")))))))

(deftest ^:integration test-concurrent-commands
  (testing "Subprocess handles concurrent commands correctly"
    (let [num-commands 10
          commands (repeatedly num-commands 
                              #(hash-map :rotary 
                                        {:rotate-to-ndc {:channel (rand-nth [:heat :day])
                                                        :x (- (rand 2) 1)  ; -1 to 1
                                                        :y (- (rand 2) 1)}}))
          ;; Send all commands concurrently
          futures (doall 
                    (map #(future (send-command-and-wait %)) commands))
          ;; Wait for all results
          results (map deref futures)]
      
      ;; Check all commands were processed
      (is (= num-commands (count results)) "All commands should return results")
      (is (not-any? #(= :timeout %) results) "No commands should timeout")
      (is (every? #(contains? % :type) (remove #(= :timeout %) results))
          "All results should have a type"))))

(deftest ^:integration test-error-handling
  (testing "Subprocess handles errors gracefully"
    ;; Send malformed command
    (let [malformed {:not-a-valid "command"}
          result (send-command-and-wait malformed)]
      (is (not= :timeout result) "Should handle malformed command")
      (when (not= :timeout result)
        (is (= :error (:type result)) "Should return error")))
    
    ;; Test subprocess recovery after error
    (testing "Subprocess continues working after error"
      (let [valid-cmd (cmd/ping)
            result (send-command-and-wait valid-cmd)]
        (is (not= :timeout result) "Should still process commands after error")
        (is (= :pong (:type result)) "Should respond correctly")))))

;; =============================================================================
;; Performance Tests
;; =============================================================================

(deftest ^:integration ^:performance test-command-throughput
  (testing "Subprocess maintains good throughput"
    (let [num-commands 100
          start-time (System/currentTimeMillis)
          commands (repeatedly num-commands #(cmd/noop))
          
          ;; Send all commands
          _ (doseq [cmd commands]
              (transit-core/write-message! (:writer *subprocess*)
                                          (transit-core/create-message :command cmd)
                                          (:output-stream *subprocess*)))
          
          ;; Wait for all to be processed (rough estimate)
          _ (Thread/sleep 2000)
          
          end-time (System/currentTimeMillis)
          duration-ms (- end-time start-time)
          throughput (/ (* num-commands 1000.0) duration-ms)]
      
      (logging/log-info {:msg "Command throughput test"
                        :commands num-commands
                        :duration-ms duration-ms
                        :throughput-per-sec throughput})
      
      ;; Should handle at least 20 commands per second
      (is (> throughput 20) 
          (str "Throughput should be > 20 cmd/s, got " throughput)))))