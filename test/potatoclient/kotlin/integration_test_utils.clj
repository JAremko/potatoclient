(ns potatoclient.kotlin.integration-test-utils
  "Utilities for testing Clojure commands with Kotlin validation"
  (:require [clojure.java.shell :as shell]
            [clojure.data.json :as json]
            [potatoclient.transit.core :as transit-core])
  (:import [java.io File]
           [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(defn write-transit-to-temp-file
  "Write a Transit command to a temporary file"
  [command]
  (let [temp-file (File/createTempFile "transit-test-" ".msgpack")
        out (java.io.FileOutputStream. temp-file)
        writer (transit-core/make-writer out)]
    (transit-core/write-message! writer command out)
    (.close out)
    temp-file))

(defn validate-command-with-kotlin
  "Send command through Kotlin validation and return result map"
  [command]
  (let [temp-file (write-transit-to-temp-file command)]
    (try
      ;; Run the Kotlin validator
      (let [result (shell/sh "java" 
                            "-cp" "src/kotlin/build/libs/potatoclient-kotlin.jar:test/kotlin/build/libs/test-kotlin.jar"
                            "potatoclient.kotlin.transit.MalliPayloadValidator"
                            (.getAbsolutePath temp-file))]
        (if (= 0 (:exit result))
          ;; Parse JSON output
          (try
            (json/read-str (:out result) :key-fn keyword)
            (catch Exception e
              {:success false
               :error (str "Failed to parse JSON: " (.getMessage e) 
                          "\nOutput: " (:out result))}))
          {:success false
           :error (str "Kotlin process failed with exit code " (:exit result)
                      "\nStderr: " (:err result)
                      "\nStdout: " (:out result))}))
      (finally
        (.delete temp-file)))))

(defn run-kotlin-test-processor
  "Run commands through the TestCommandProcessor"
  [commands]
  (let [temp-dir (Files/createTempDirectory "kotlin-test-" (into-array FileAttribute []))
        results (atom [])]
    (try
      ;; Write each command to a file
      (doseq [[idx command] (map-indexed vector commands)]
        (let [cmd-file (File. (.toFile temp-dir) (str "cmd-" idx ".msgpack"))
              out (java.io.FileOutputStream. cmd-file)
              writer (transit-core/make-writer out)]
          (transit-core/write-message! writer command out)
          (.close out)
          
          ;; Run through TestCommandProcessor
          (let [result (shell/sh "java"
                                "-cp" "src/kotlin/build/libs/potatoclient-kotlin.jar:test/kotlin/build/libs/test-kotlin.jar"
                                "potatoclient.kotlin.transit.TestCommandProcessorKt"
                                "--validate"
                                (.getAbsolutePath cmd-file))]
            (swap! results conj
                   (if (= 0 (:exit result))
                     (try
                       (merge {:command command}
                              (json/read-str (:out result) :key-fn keyword))
                       (catch Exception e
                         {:command command
                          :success false
                          :error (str "JSON parse error: " (.getMessage e))}))
                     {:command command
                      :success false
                      :error (str "Exit code: " (:exit result) 
                                 "\nStderr: " (:err result))})))))
      @results
      (finally
        ;; Clean up temp directory
        (doseq [file (.listFiles (.toFile temp-dir))]
          (.delete file))
        (.delete (.toFile temp-dir))))))

;; Helper to check if Kotlin test infrastructure is available
(defn kotlin-tests-available?
  []
  (and (.exists (File. "src/kotlin/build/libs/potatoclient-kotlin.jar"))
       (.exists (File. "test/kotlin/potatoclient/kotlin/transit/MalliPayloadValidator.kt"))))