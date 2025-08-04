(ns potatoclient.debug-subprocess-test
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.framed-io :as framed-io]
            [clojure.java.io :as io])
  (:import [java.lang ProcessBuilder]))

(deftest test-subprocess-messages
  (testing "Can receive messages from subprocess"
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
          output-stream (.getOutputStream process)
          input-stream (.getInputStream process)
          error-stream (.getErrorStream process)]

      ;; Print stderr
      (future
        (with-open [reader (io/reader error-stream)]
          (loop []
            (when-let [line (.readLine reader)]
              (println "STDERR:" line)
              (recur)))))

      ;; Read Transit messages
      (try
        (let [framed-input (framed-io/make-framed-input-stream input-stream)
              reader (transit-core/make-reader framed-input)
              messages (atom [])]

          ;; Read first few messages
          (dotimes [_ 5]
            (when-let [msg (try
                             (transit-core/read-message reader)
                             (catch Exception e
                               (println "Error reading:" (.getMessage e))
                               nil))]
              (println "Received message:")
              (println "  Type:" (:msg-type msg))
              (println "  Payload:" (:payload msg))
              (swap! messages conj msg)))

          ;; Check what we got
          (println "\nTotal messages received:" (count @messages))
          (let [status-messages (filter #(= "status" (:msg-type %)) @messages)]
            (println "Status messages:" (count status-messages))
            (doseq [msg status-messages]
              (println "  Status:" (get-in msg [:payload :status]))))

          (is (pos? (count @messages)) "Should receive some messages")
          (is (some #(= "test-mode-ready" (get-in % [:payload :status])) @messages)
              "Should receive test-mode-ready status"))

        (finally
          (.destroyForcibly process))))))