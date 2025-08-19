(ns potatoclient.cmd.compass-debug-test
  "Debug test to understand compass roundtrip issues."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.compass :as compass]
   [potatoclient.cmd.validation :as v]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

(deftest debug-compass-start-test
  (testing "Debug compass start command"
    (let [cmd (compass/start)
          _ (println "Original cmd:" cmd)
          result (v/validate-roundtrip-with-report cmd)]
      (println "Validation result:" (:valid? result))
      (when-not (:valid? result)
        (println "Pretty diff:")
        (println (:pretty-diff result)))
      (is (:valid? result) "Should pass roundtrip validation"))))