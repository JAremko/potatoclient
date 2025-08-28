(ns test-suites.ipc-suite
  "Test suite for IPC functionality"
  (:require
   [clojure.test :as test]
   [potatoclient.ipc.transit-test]
   [potatoclient.ipc.core-test]))

(defn -main []
  (let [results (test/run-tests 
                 'potatoclient.ipc.transit-test
                 'potatoclient.ipc.core-test)]
    (System/exit (if (zero? (+ (:fail results) (:error results))) 0 1))))