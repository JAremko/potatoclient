(ns buff-validate.pronto-test
  "Tests for Pronto operations and performance."
  (:require [clojure.test :refer [deftest testing is]]
            [buff-validate.test-harness :as h]
            [pronto.core :as p]))

(deftest test-pronto-immutability
  (testing "Pronto modifications preserve immutability"
    (let [original (h/valid-state)
          original-altitude (get-in original [:gps :altitude])
          modified (p/with-hints [(p/hint original ser.JonSharedData$JonGUIState h/state-mapper)]
                     (p/p-> original
                            (assoc-in [:gps :altitude] 9999.0)))]
      (is (= original-altitude (get-in original [:gps :altitude])))
      (is (= 9999.0 (get-in modified [:gps :altitude]))))))

(deftest test-pronto-batch-updates
  (testing "Batch updates work correctly"
    (let [state (h/valid-state)
          updated (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState h/state-mapper)]
                    (p/p-> state
                           (assoc :protocol_version 5)
                           (assoc-in [:gps :altitude] 1234.5)
                           (assoc-in [:system :cpu_load] 75.0)))]
      (is (= 5 (get updated :protocol_version)))
      (is (= 1234.5 (get-in updated [:gps :altitude])))
      (is (= 75.0 (get-in updated [:system :cpu_load]))))))

(deftest test-performance-characteristics
  (testing "Memoized bytes are fast"
    (let [start (System/nanoTime)]
      (dotimes [_ 1000]
        (h/valid-state-bytes))
      (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
        (is (< elapsed-ms 100)))))
  
  (testing "Pronto modifications are fast"
    (let [state (h/valid-state)
          start (System/nanoTime)]
      (dotimes [_ 100]
        (p/with-hints [(p/hint state ser.JonSharedData$JonGUIState h/state-mapper)]
          (p/p-> state (assoc-in [:gps :altitude] (rand 1000)))))
      (let [elapsed-ms (/ (- (System/nanoTime) start) 1000000.0)]
        (is (< elapsed-ms 100))))))