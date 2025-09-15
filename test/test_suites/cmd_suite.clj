(ns test-suites.cmd-suite
  "Test suite for command tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main
  "Entry point for running the command system test suite.
  Executes all tests matching the pattern 'potatoclient.cmd.*-test' including
  builder tests, validation tests, and negative test cases."
  [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.cmd.*-test"]}))