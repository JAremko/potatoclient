(ns potatoclient.working-subprocess-test
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.framed-io :as framed-io]
            [clojure.java.io :as io])
  (:import [java.lang ProcessBuilder]
           [java.io PipedInputStream PipedOutputStream]))

(deftest test-subprocess-with-proper-io
  (testing "Subprocess works when we provide proper stdin"
    ;; Create piped streams so we control stdin
    (let [subprocess-stdin (PipedInputStream.)
          our-output (PipedOutputStream. subprocess-stdin)

          java-exe (if-let [java-home (System/getProperty "java.home")]
                     (str java-home "/bin/java")
                     "java")
          classpath (System/getProperty "java.class.path")
          cmd [java-exe "-cp" classpath
               "--enable-native-access=ALL-UNNAMED"
               "potatoclient.kotlin.transit.CommandSubprocessKt"
               "--test-mode"]
          pb (ProcessBuilder. ^java.util.List cmd)
          _ (.redirectInput pb ProcessBuilder$Redirect/from subprocess-stdin)
          process (.start pb)]

      (try
        ;; Set up reading from subprocess
        (let [subprocess-output (.getInputStream process)
              subprocess-error (.getErrorStream process)
              messages (atom [])]

          ;; Start error reader
          (future
            (with-open [reader (io/reader subprocess-error)]
              (loop []
                (when-let [line (.readLine reader)]
                  (println "STDERR:" line)
                  (recur)))))

          ;; Start output reader in a separate thread
          (let [reader-future
                (future
                  (try
                    (let [framed-input (framed-io/make-framed-input-stream subprocess-output)
                          reader (transit-core/make-reader framed-input)]
                      (dotimes [_ 10]  ; Try to read up to 10 messages
                        (when-let [msg (try
                                         (transit-core/read-message reader)
                                         (catch Exception e
                                           (println "Read error:" (.getMessage e))
                                           nil))]
                          (println "Received:" (:msg-type msg) "-" (get-in msg [:payload :status]))
                          (swap! messages conj msg))))
                    (catch Exception e
                      (println "Reader exception:" (.getMessage e)))))]

            ;; Give subprocess time to start
            (Thread/sleep 500)

            ;; Now send a command to unblock the subprocess
            (let [framed-output (framed-io/make-framed-output-stream our-output)
                  writer (transit-core/make-writer framed-output)]
              (println "Sending ping command...")
              (transit-core/write-message! writer
                                           (transit-core/create-message :command (cmd/ping))
                                           our-output))

            ;; Wait a bit for processing
            (Thread/sleep 1000)

            ;; Check results
            (let [msgs @messages]
              (println "Total messages received:" (count msgs))
              (is (pos? (count msgs)) "Should receive messages")
              (is (some #(= "test-mode-ready" (get-in % [:payload :status])) msgs)
                  "Should receive test-mode-ready")
              (is (some #(= "pong" (get-in % [:payload :type])) msgs)
                  "Should receive pong response"))))

        (finally
          (.close our-output)
          (.destroyForcibly process))))))