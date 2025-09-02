(ns test-suites.cmd-suite
  "Test suite for command tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.cmd.*-test"]}))