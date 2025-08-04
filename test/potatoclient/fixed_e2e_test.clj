(ns potatoclient.fixed_e2e_test
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.framed-io :as framed-io]
            [potatoclient.logging :as logging]
            [clojure.java.io :as io])
  (:import [java.lang ProcessBuilder]))

(deftest test-subprocess-communication
  (testing "Can communicate with subprocess in test mode"
    (let [java-exe (if-let [java-home (System/getProperty "java.home")]
                     (str java-home "/bin/java")
                     "java")
          classpath (System/getProperty "java.class.path")
          cmd [java-exe "-cp" classpath
               "--enable-native-access=ALL-UNNAMED"
               "potatoclient.kotlin.transit.CommandSubprocessKt"
               "--test-mode"]
          pb (ProcessBuilder. ^java.util.List cmd)
          process (.start pb)
          ;; Get streams
          output-stream (.getOutputStream process)
          input-stream (.getInputStream process)
          error-stream (.getErrorStream process)
          ;; Wrap for Transit
          framed-output (framed-io/make-framed-output-stream output-stream)
          writer (transit-core/make-writer framed-output)
          messages (atom [])]

      (try
        ;; Start error reader first
        (future
          (with-open [reader (io/reader error-stream)]
            (loop []
              (when-let [line (.readLine reader)]
                (println "STDERR:" line)
                (recur)))))

        ;; IMPORTANT: Send a message immediately to prevent the subprocess from blocking
        (println "Sending initial ping to unblock subprocess...")
        (transit-core/write-message! writer
                                     (transit-core/create-message :command (cmd/ping))
                                     output-stream)

        ;; Now start the reader thread
        (let [reader-thread
              (Thread.
                #(try
                   (let [framed-input (framed-io/make-framed-input-stream input-stream)
                         reader (transit-core/make-reader framed-input)]
                     (loop [count 0]
                       (when (< count 10)  ; Read up to 10 messages
                         (when-let [msg (try
                                          (transit-core/read-message reader)
                                          (catch Exception e
                                            (when-not (.contains (.getMessage e) "Stream closed")
                                              (println "Read error:" (.getMessage e)))
                                            nil))]
                           (println "Received message" count ":" (get msg "msg-type"))
                           (when-let [payload (get msg "payload")]
                             (println "  Payload:" payload))
                           (swap! messages conj msg)
                           (recur (inc count))))))
                   (catch Exception e
                     (println "Reader thread error:" (.getMessage e)))))]

          (.start reader-thread)

          ;; Give it time to process
          (Thread/sleep 1000)

          ;; Send another command
          (println "Sending rotate command...")
          (transit-core/write-message! writer
                                       (transit-core/create-message
                                         :command
                                         {:rotary {:rotate-to-ndc {:channel :heat :x 0.5 :y -0.5}}})
                                       output-stream)

          ;; Wait for responses
          (Thread/sleep 1000)

          ;; Stop the reader thread
          (.interrupt reader-thread)
          (.join reader-thread 500)

          ;; Check results
          (let [msgs @messages]
            (println "\n=== Test Results ===")
            (println "Total messages received:" (count msgs))
            (println "Message types:" (map #(get % "msg-type") msgs))

            (is (pos? (count msgs)) "Should receive at least one message")

            ;; Check for expected message types
            (let [status-msgs (filter #(= :status (get % "msg-type")) msgs)
                  response-msgs (filter #(= :response (get % "msg-type")) msgs)]

              (println "Status messages:" (count status-msgs))
              (doseq [msg status-msgs]
                (println "  -" (get-in msg ["payload" :status])))

              (println "Response messages:" (count response-msgs))
              (doseq [msg response-msgs]
                (println "  -" (get-in msg ["payload" :type])))

              (is (some #(= "test-mode-ready" (get-in % ["payload" :status])) msgs)
                  "Should receive test-mode-ready status")
              (is (some #(= "pong" (get-in % ["payload" "type"])) msgs)
                  "Should receive pong response"))))

        (finally
          (.destroyForcibly process))))))