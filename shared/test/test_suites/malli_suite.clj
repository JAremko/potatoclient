(ns test-suites.malli-suite
  (:require [clojure.test :refer [run-tests]]
            [potatoclient.malli.registry-test]
            [potatoclient.specs.cmd.root-test]
            [potatoclient.specs.cmd.root-validate-test]
            [potatoclient.specs.state.root-test]
            [potatoclient.specs.state.root-validate-test]
            [potatoclient.specs.state.root-gen-test]))

(defn run-malli-tests []
  (println "\n========================================")
  (println "Running Malli Spec Tests")
  (println "========================================\n")
  (let [results (run-tests 'potatoclient.malli.registry-test
                          'potatoclient.specs.cmd.root-test
                          'potatoclient.specs.cmd.root-validate-test
                          'potatoclient.specs.state.root-test
                          'potatoclient.specs.state.root-validate-test
                          'potatoclient.specs.state.root-gen-test)]
    (println "\n========================================")
    (println "Malli Spec Tests Complete")
    (println "========================================\n")
    results))

(defn -main [& _args]
  (let [{:keys [fail error]} (run-malli-tests)]
    (System/exit (if (zero? (+ fail error)) 0 1))))