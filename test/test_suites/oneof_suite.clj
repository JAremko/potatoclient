(ns test-suites.oneof-suite
  "Test suite for oneof tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.malli.oneof-test"]}))