(ns test-suites.oneof-suite
  "Test suite for oneof tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main
  "Entry point for running the oneof schema test suite.
  Executes tests for the custom :oneof Malli schema type used for
  protobuf oneOf field validation."
  [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.malli.oneof-test"]}))