(ns test-fixtures
  "Generate test fixtures for separated namespace mode"
  (:require [generator.core :as core]
            [clojure.java.io :as io]))

(defn generate-test-fixtures!
  "Generate code for testing"
  []
  (println "Generating test fixtures...")
  
  ;; Generate separated namespace mode
  (println "Generating separated namespace mode to test-output...")
  (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                      :output-dir "test-output"
                      :namespace-prefix "potatoclient.proto"
                      :debug? false})
  
  (println "Test fixtures generated successfully!"))

(defn setup-test-classpath!
  "Add generated code to classpath for tests"
  []
  ;; This would need to be called before running tests
  ;; For now, tests should add the appropriate paths to their deps.edn
  )