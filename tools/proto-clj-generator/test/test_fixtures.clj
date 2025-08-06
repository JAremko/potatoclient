(ns test-fixtures
  "Generate test fixtures for both single and namespaced modes"
  (:require [generator.core :as core]
            [clojure.java.io :as io]))

(defn generate-test-fixtures!
  "Generate code in both modes for testing"
  []
  (println "Generating test fixtures...")
  
  ;; Generate single-file mode for backward compatibility tests
  (println "Generating single-file mode to test-output-single...")
  (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                      :output-dir "test-output-single"
                      :namespace-prefix "potatoclient.proto"
                      :namespace-mode :single
                      :debug? false})
  
  ;; Generate namespace-split mode 
  (println "Generating namespace-split mode to test-output-ns...")
  (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                      :output-dir "test-output-ns"
                      :namespace-prefix "potatoclient.proto"
                      :namespace-split? true
                      :debug? false})
  
  (println "Test fixtures generated successfully!"))

(defn setup-test-classpath!
  "Add generated code to classpath for tests"
  []
  ;; This would need to be called before running tests
  ;; For now, tests should add the appropriate paths to their deps.edn
  )