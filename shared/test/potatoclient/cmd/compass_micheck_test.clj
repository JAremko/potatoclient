(ns potatoclient.cmd.compass-micheck-test
  "Test compass functions using malli.instrument/check for generative testing."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.instrument :as mi]
   [potatoclient.cmd.compass :as compass]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

(deftest compass-generative-test-with-mi-check
  (testing "Compass functions pass generative testing with mi/check"
    ;; Collect schemas from compass namespace
    (mi/collect! {:ns ['potatoclient.cmd.compass]})
    
    ;; Run generative testing on all collected functions
    (let [result (mi/check)]
      (is (nil? result) 
          (str "All compass functions should pass generative testing. Issues found: " 
               (when result (keys result)))))))