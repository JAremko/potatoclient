(ns potatoclient.transit.quick-registry-test
  (:require [clojure.test :refer [deftest is testing]])
  (:import [potatoclient.transit ActionRegistry]))

(deftest test-registry-loads
  (testing "Registry loads with all command modules"
    ;; Force class initialization
    (Class/forName "potatoclient.transit.ActionRegistry")
    
    (let [all-commands (ActionRegistry/getAllActionNames)]
      (println "Total commands loaded:" (count all-commands))
      (println "Sample commands:" (take 10 (sort all-commands)))
      
      ;; Should have many more than just basic commands
      (is (> (count all-commands) 10)
          (str "Only " (count all-commands) " commands loaded")))))