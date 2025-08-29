(ns potatoclient.cmd.compass-micheck-test
  "Test compass functions using malli.instrument/check for generative testing."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [malli.instrument :as mi]
   [potatoclient.cmd.compass :as compass]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

(deftest compass-generative-test-with-mi-check
  (testing "Compass functions pass generative testing with mi/check"
    ;; Clear any previous instrumentation and collected schemas
    (mi/unstrument!)
    
    ;; Fresh collection - only compass namespace
    (mi/collect! {:ns ['potatoclient.cmd.compass] :mode :replace})
    
    ;; Run generative testing on compass functions only  
    ;; Filter results to only show compass namespace issues
    (let [all-results (mi/check)
          compass-results (when all-results
                           (into {} 
                                 (filter #(= "potatoclient.cmd.compass" 
                                            (namespace (key %))) 
                                         all-results)))]
      (is (empty? compass-results) 
          (str "All compass functions should pass generative testing. Issues found: " 
               (when compass-results (keys compass-results)))))))