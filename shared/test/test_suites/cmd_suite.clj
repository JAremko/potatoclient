(ns test-suites.cmd-suite
  (:require [clojure.test :refer [run-tests]]
            [potatoclient.cmd.root-test]
            [potatoclient.cmd.negative-test]
            [potatoclient.cmd.validation-test]
            [potatoclient.cmd.builder-test]
            [potatoclient.cmd.generative-test]
            [potatoclient.cmd.integration-test]))

(defn run-cmd-tests []
  (println "\n========================================")
  (println "Running Command Tests")
  (println "========================================\n")
  (let [results (run-tests 'potatoclient.cmd.root-test
                          'potatoclient.cmd.negative-test
                          'potatoclient.cmd.validation-test
                          'potatoclient.cmd.builder-test
                          'potatoclient.cmd.generative-test
                          'potatoclient.cmd.integration-test)]
    (println "\n========================================")
    (println "Command Tests Complete")
    (println "========================================\n")
    results))

(defn -main [& _args]
  (let [{:keys [fail error]} (run-cmd-tests)]
    (System/exit (if (zero? (+ fail error)) 0 1))))