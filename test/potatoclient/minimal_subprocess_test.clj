(ns potatoclient.minimal-subprocess-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io])
  (:import [java.lang ProcessBuilder]))

(deftest test-subprocess-basic
  (testing "Subprocess starts and outputs something"
    (let [java-exe (if-let [java-home (System/getProperty "java.home")]
                     (str java-home "/bin/java")
                     "java")
          classpath (System/getProperty "java.class.path")
          cmd [java-exe "-cp" classpath
               "--enable-native-access=ALL-UNNAMED"
               "potatoclient.kotlin.transit.CommandSubprocessKt"
               "--test-mode"]
          pb (ProcessBuilder. ^java.util.List cmd)
          _ (.redirectErrorStream pb true)  ; Merge stderr into stdout
          process (.start pb)
          reader (io/reader (.getInputStream process))
          output (atom [])]

      ;; Read for 2 seconds
      (let [start (System/currentTimeMillis)]
        (while (< (- (System/currentTimeMillis) start) 2000)
          (when (.ready reader)
            (let [line (.readLine reader)]
              (println "OUTPUT:" line)
              (swap! output conj line)))
          (Thread/sleep 10)))

      (.destroyForcibly process)

      (println "Total lines:" (count @output))
      (is (pos? (count @output)) "Should produce some output"))))