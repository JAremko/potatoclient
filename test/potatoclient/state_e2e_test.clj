(ns potatoclient.state-e2e-test
  "End-to-end tests for StateSubprocess with Transit"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.logging :as logging]
            [clojure.java.io :as io])
  (:import (java.io ByteArrayInputStream DataOutputStream)
           (java.lang Process ProcessBuilder)
           (java.util.concurrent TimeUnit)
           (ser JonSharedData$JonGUIState JonSharedDataTypes$JonGuiDataMeteo
                JonSharedDataGps$JonGuiDataGps JonSharedDataRotary$JonGuiDataRotaryPlatform)))

;; Test helpers
(defn- start-state-subprocess-test-mode
  "Start a state subprocess in test mode for e2e testing"
  []
  (let [java-exe "java"
        classpath (System/getProperty "java.class.path")
        cmd [java-exe "-cp" classpath
             "potatoclient.kotlin.transit.StateSubprocessKt"
             "test-mode"]
        pb (ProcessBuilder. cmd)]
    (.start pb)))

(defn- read-transit-from-process
  "Read a single Transit message from process output"
  [^Process process]
  (let [in (.getInputStream process)
        length-bytes (byte-array 4)]
    (when (= 4 (.read in length-bytes))
      (let [length (bit-or (bit-shift-left (bit-and (aget length-bytes 0) 0xFF) 24)
                           (bit-shift-left (bit-and (aget length-bytes 1) 0xFF) 16)
                           (bit-shift-left (bit-and (aget length-bytes 2) 0xFF) 8)
                           (bit-and (aget length-bytes 3) 0xFF))
            msg-bytes (byte-array length)]
        (when (= length (.read in msg-bytes))
          (let [reader (transit-core/make-reader (ByteArrayInputStream. msg-bytes))]
            (transit-core/read-message reader)))))))

(defn- send-protobuf-to-process
  "Send a protobuf message to the process (length-prefixed)"
  [^Process process proto-msg]
  (let [proto-bytes (.toByteArray proto-msg)
        output (.getOutputStream process)
        data-out (DataOutputStream. output)]
    (.writeInt data-out (count proto-bytes))
    (.write data-out proto-bytes)
    (.flush data-out)))

(defn- wait-for-test-mode-ready
  "Wait for the test-mode-ready status message"
  [^Process process]
  (let [status (read-transit-from-process process)]
    (and (map? status)
         (= "status" (get status "msg-type"))
         (= "test-mode-ready" (get-in status ["payload" "status"])))))

(defn- wait-for-unique-messages
  "Wait for n unique messages from the process"
  [^Process process n timeout-ms]
  (let [start-time (System/currentTimeMillis)
        seen-ids (atom #{})]
    (loop []
      (when (< (- (System/currentTimeMillis) start-time) timeout-ms)
        (when-let [msg (read-transit-from-process process)]
          (swap! seen-ids conj (get msg "msg-id"))
          (if (>= (count @seen-ids) n)
            (count @seen-ids)
            (recur)))))))

;; Tests
(deftest test-state-subprocess-basic
  (testing "State subprocess basic Transit communication"
    (let [process (start-state-subprocess-test-mode)]
      (try
        ;; Wait for subprocess to be ready
        (is (wait-for-test-mode-ready process))

        ;; Create a simple protobuf state
        (let [meteo (-> (JonSharedDataTypes$JonGuiDataMeteo/newBuilder)
                        (.setTemperature 25.5)
                        (.setHumidity 60.0)
                        (.setPressure 1013.25)
                        .build)
              state-builder (JonSharedData$JonGUIState/newBuilder)
              _ (.setMeteoInternal state-builder meteo)
              state-proto (.build state-builder)]

          ;; Send protobuf to subprocess
          (send-protobuf-to-process process state-proto)

          ;; Read Transit response
          (let [response (read-transit-from-process process)]
            (is (map? response))
            (is (= "state-update" (get response "msg-type")))
            (is (string? (get response "msg-id")))
            (is (number? (get response "timestamp")))

            ;; Check payload
            (let [payload (get response "payload")]
              (is (map? payload))
              (is (contains? payload "state"))

              ;; The state should be a map (from the StateSubprocess wrapper)
              (let [state (get payload "state")]
                (is (map? state) "State should be a map")))))

        (finally
          (.destroy process)
          (is (.waitFor process 2 TimeUnit/SECONDS)))))))

(deftest test-state-subprocess-gps
  (testing "State subprocess handles GPS state correctly"
    (let [process (start-state-subprocess-test-mode)]
      (try
        ;; Wait for subprocess to be ready
        (is (wait-for-test-mode-ready process))

        ;; Create GPS state
        (let [gps (-> (JonSharedDataGps$JonGuiDataGps/newBuilder)
                      (.setLatitude 40.7128)
                      (.setLongitude -74.0060)
                      (.setAltitude 10.0)
                      .build)
              state-builder (JonSharedData$JonGUIState/newBuilder)
              _ (.setGps state-builder gps)
              state-proto (.build state-builder)]

          ;; Send and receive
          (send-protobuf-to-process process state-proto)

          (let [response (read-transit-from-process process)
                state (get-in response ["payload" "state"])]
            (is (map? state))
            ;; Verify GPS data was extracted (LinkedHashMap from GeneratedStateHandlers)
            (when (instance? java.util.Map state)
              (is (contains? state "gps"))
              (when-let [gps (get state "gps")]
                (let [gps (get state "gps")]
                  (is (= 40.7128 (get gps "latitude")))
                  (is (= -74.0060 (get gps "longitude")))
                  (is (= 10.0 (get gps "altitude")))))))

          (finally
            (.destroy process)
            (is (.waitFor process 2 TimeUnit/SECONDS)))))))

  (deftest test-state-subprocess-rate-limiting
    (testing "State subprocess respects rate limiting"
      (let [process (start-state-subprocess-test-mode)]
        (try
        ;; Wait for subprocess to be ready
          (is (wait-for-test-mode-ready process))

        ;; Send multiple state messages rapidly
          (let [state-builder (JonSharedData$JonGUIState/newBuilder)
                state-proto (.build state-builder)

              ;; Send 10 messages
                _ (dotimes [_ 10]
                    (send-protobuf-to-process process state-proto))

              ;; Read responses - should be rate limited
                responses (atom [])
                start-time (System/currentTimeMillis)]

          ;; Collect responses for 1 second
            (while (< (- (System/currentTimeMillis) start-time) 1000)
              (when-let [msg (read-transit-from-process process)]
                (swap! responses conj msg)))

          ;; With 30Hz rate limit, we should get around 30 messages in 1 second
          ;; Allow some variance
            (is (> (count @responses) 20))
            (is (< (count @responses) 40))

          ;; All should be state-update messages
            (is (every? #(= "state-update" (get % "msg-type")) @responses)))

          (finally
            (.destroy process)
            (is (.waitFor process 2 TimeUnit/SECONDS)))))))

  (deftest test-state-subprocess-multiple-state-types
    (testing "State subprocess handles mixed subsystem data"
      (let [process (start-state-subprocess-test-mode)]
        (try
        ;; Wait for subprocess to be ready
          (is (wait-for-test-mode-ready process))

        ;; Create complex state with multiple subsystems
          (let [meteo (-> (JonSharedDataTypes$JonGuiDataMeteo/newBuilder)
                          (.setTemperature 22.0)
                          .build)
                gps (-> (JonSharedDataGps$JonGuiDataGps/newBuilder)
                        (.setLatitude 51.5074)
                        (.setLongitude -0.1278)
                        .build)
                rotary (-> (JonSharedDataRotary$JonGuiDataRotaryPlatform/newBuilder)
                           (.setAzimuth 45.0)
                           (.setElevation 30.0)
                           .build)
                state-builder (JonSharedData$JonGUIState/newBuilder)
                _ (doto state-builder
                    (.setMeteoInternal meteo)
                    (.setGps gps)
                    (.setRotary rotary))
                state-proto (.build state-builder)]

          ;; Send and receive
            (send-protobuf-to-process process state-proto)

            (let [response (read-transit-from-process process)
                  state (get-in response ["payload" "state"])]

            ;; Check for expected subsystems
              (is (contains? state "meteo-internal") "Should have meteo data")
              (is (contains? state "gps") "Should have GPS data")
              (is (contains? state "rotary") "Should have rotary data")
              (is (contains? state "system") "Should have system data")))

          (finally
            (.destroy process)
            (is (.waitFor process 2 TimeUnit/SECONDS)))))))

  (deftest test-state-subprocess-error-handling
    (testing "State subprocess handles invalid protobuf gracefully"
      (let [process (start-state-subprocess-test-mode)]
        (try
        ;; Wait for subprocess to be ready
          (is (wait-for-test-mode-ready process))

        ;; Send invalid protobuf data
          (let [output (.getOutputStream process)
                data-out (DataOutputStream. output)
                garbage-data (byte-array [1 2 3 4 5 6 7 8])]

          ;; Send length-prefixed garbage
            (.writeInt data-out (count garbage-data))
            (.write data-out garbage-data)
            (.flush data-out)

          ;; Should get an error message
            (let [response (read-transit-from-process process)]
              (is (= "error" (get response "msg-type")))
              (is (string? (get-in response ["payload" "message"])))
              (is (re-find #"[Pp]arse" (get-in response ["payload" "message"])))))

          (finally
            (.destroy process)
            (is (.waitFor process 2 TimeUnit/SECONDS)))))))

;; Test fixture to ensure clean subprocess lifecycle
  (defn- subprocess-fixture [f]
    (logging/log-info "Starting state subprocess e2e tests")
    (f)
    (logging/log-info "Completed state subprocess e2e tests"))

  (use-fixtures :once subprocess-fixture)

  (deftest test-state-subprocess-initialization
    (testing "State subprocess sends test-mode-ready status"
      (let [process (start-state-subprocess-test-mode)]
        (try
        ;; We should get a status message immediately
          (let [status (read-transit-from-process process)]
            (is (map? status))
            (is (= "status" (get status "msg-type")))
            (is (= "test-mode-ready" (get-in status ["payload" "status"]))))
          (catch Exception e
            (is false (str "Failed to start subprocess: " (.getMessage e))))
          (finally
            (.destroy process)
            (is (.waitFor process 2 TimeUnit/SECONDS)))))))

  (deftest test-state-subprocess-message-rate
    (testing "State subprocess message rate calculation"
      (let [process (start-state-subprocess-test-mode)]
        (try
        ;; Wait for subprocess to be ready
          (is (wait-for-test-mode-ready process))

        ;; Create simple state
          (let [state-builder (JonSharedData$JonGUIState/newBuilder)
                state-proto (.build state-builder)

              ;; Send 100 state messages rapidly
                _ (dotimes [_ 100]
                    (send-protobuf-to-process process state-proto))

              ;; Wait for unique messages and check rate
                unique-count (wait-for-unique-messages process 30 2000)

              ;; Calculate effective rate
                received-rate (when unique-count
                                (/ unique-count 2.0))]  ; messages per second

            (is (number? unique-count) "Should have received messages")
            (is (>= unique-count 25) (str "Should receive at least 25 unique messages in 2s, got " unique-count))
            (is (<= unique-count 70) (str "Should not exceed 70 messages in 2s (rate limited), got " unique-count))
            (is (number? received-rate) "Should have calculated a rate"))

          (finally
            (.destroy process)))))))
