(ns potatoclient.simple-subprocess-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.lang ProcessBuilder]))

(deftest test-subprocess-startup
  (testing "Can start subprocess in test mode"
    (let [java-exe (if-let [java-home (System/getProperty "java.home")]
                     (str java-home "/bin/java")
                     "java")
          classpath (System/getProperty "java.class.path")
          cmd [java-exe "-cp" classpath
               "--enable-native-access=ALL-UNNAMED"
               "potatoclient.kotlin.transit.CommandSubprocessKt"
               "--test-mode"]
          pb (ProcessBuilder. ^java.util.List cmd)
          _ (println "Starting subprocess with command:" (str/join " " cmd))
          process (.start pb)
          error-reader (io/reader (.getErrorStream process))
          input-reader (io/reader (.getInputStream process))]

      ;; Read any immediate output
      (println "=== Reading subprocess output ===")
      (future
        (loop []
          (when-let [line (.readLine error-reader)]
            (println "STDERR:" line)
            (recur))))

      (future
        (loop []
          (when-let [line (.readLine input-reader)]
            (println "STDOUT:" line)
            (recur))))

      ;; Give it time to start
      (Thread/sleep 2000)

      ;; Check if process is still alive
      (is (.isAlive process) "Process should be running")

      ;; Clean up
      (.destroyForcibly process)
      (is true "Test completed"))))