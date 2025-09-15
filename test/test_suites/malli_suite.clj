(ns test-suites.malli-suite
  "Test suite for malli spec tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main
  "Entry point for running the Malli schema and specification test suite.
  Executes all tests matching patterns 'potatoclient.specs.*-test' and
  'potatoclient.malli.*-test' including schema validation and registry tests."
  [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.specs.*-test"
                                 "potatoclient.malli.*-test"]}))