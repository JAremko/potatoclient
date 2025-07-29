(ns run-tests
  (:require [clojure.test :as test]
            [potatoclient.websocket-full-integration-test]))

(defn -main []
  (let [results (test/run-all-tests #"potatoclient\..*test")]
    (System/exit (if (and (zero? (:fail results))
                          (zero? (:error results)))
                   0
                   1))))