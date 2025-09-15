(ns test-suites.ipc-suite
  "Test suite for IPC tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main
  "Entry point for running the IPC (Inter-Process Communication) test suite.
  Executes all tests matching the pattern 'potatoclient.ipc.*-test' including
  Unix domain socket communication and message handler tests."
  [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.ipc.*-test"]}))