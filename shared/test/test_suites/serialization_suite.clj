(ns test-suites.serialization-suite
  (:require [clojure.test :refer [run-tests]]
            [potatoclient.proto.serialize-test]
            [potatoclient.proto.deserialize-test]))

(defn run-serialization-tests []
  (println "\n========================================")
  (println "Running Serialization/Deserialization Tests")
  (println "========================================\n")
  (let [results (run-tests 'potatoclient.proto.serialize-test
                          'potatoclient.proto.deserialize-test)]
    (println "\n========================================")
    (println "Serialization/Deserialization Tests Complete")
    (println "========================================\n")
    results))

(defn -main [& _args]
  (let [{:keys [fail error]} (run-serialization-tests)]
    (System/exit (if (zero? (+ fail error)) 0 1))))