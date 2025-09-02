(ns test-suites.malli-suite
  "Test suite for malli spec tests"
  (:require [cognitect.test-runner.api :as test-runner]))

(defn -main [& args]
  (test-runner/test {:paths ["test"]
                      :patterns ["potatoclient.specs.*-test"
                                 "potatoclient.malli.*-test"]}))