(ns test-suites.ipc-suite
  "Test suite for IPC tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.ipc.*-test"]}))