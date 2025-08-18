(ns test-suites.oneof-suite
  (:require [clojure.test :refer [run-tests]]
            [potatoclient.malli.oneof-test]
            [potatoclient.oneof-validation-test]
            [potatoclient.oneof-base-fields-test]
            [potatoclient.oneof-merge-test]
            [potatoclient.cmd-oneof-test]))

(defn run-oneof-tests []
  (println "\n========================================")
  (println "Running Oneof Tests")
  (println "========================================\n")
  (let [results (run-tests 'potatoclient.malli.oneof-test
                          'potatoclient.oneof-validation-test
                          'potatoclient.oneof-base-fields-test
                          'potatoclient.oneof-merge-test
                          'potatoclient.cmd-oneof-test)]
    (println "\n========================================")
    (println "Oneof Tests Complete")
    (println "========================================\n")
    results))

(defn -main [& _args]
  (let [{:keys [fail error]} (run-oneof-tests)]
    (System/exit (if (zero? (+ fail error)) 0 1))))