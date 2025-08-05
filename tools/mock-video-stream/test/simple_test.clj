(ns simple-test
  (:require [clojure.test :refer [deftest testing is run-tests]])
  (:import [potatoclient.video NDCConverter]))

(deftest ndc-converter-test
  (testing "Basic NDC conversion"
    (let [ndc (NDCConverter/pixelToNDC 400 300 800 600)]
      (is (= 0.0 (.x ndc)))
      (is (= 0.0 (.y ndc))))
    
    (let [ndc (NDCConverter/pixelToNDC 0 0 800 600)]
      (is (= -1.0 (.x ndc)))
      (is (= 1.0 (.y ndc))))))

(deftest command-format-test
  (testing "Command structure"
    (let [cmd {:rotary {:goto-ndc {:channel :heat :x 0.0 :y 0.0}}}]
      (is (map? cmd))
      (is (= :heat (get-in cmd [:rotary :goto-ndc :channel]))))))

(defn -main []
  (let [results (run-tests)]
    (println "\nTests run:" (:test results))
    (println "Passed:" (:pass results))
    (println "Failed:" (:fail results))
    (System/exit (if (zero? (:fail results)) 0 1))))