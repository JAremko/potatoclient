(ns test-suites.serialization-suite
  "Test suite for serialization tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.proto.*-test"]}))