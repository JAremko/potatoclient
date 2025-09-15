(ns test-suites.serialization-suite
  "Test suite for serialization tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main
  "Entry point for running the protobuf serialization test suite.
  Executes all tests matching the pattern 'potatoclient.proto.*-test' including
  EDN to protobuf conversion and roundtrip serialization tests."
  [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.proto.*-test"]}))