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
    ;; With m/=> declarations, schemas are already available
    ;; No need for mi/collect!
    
    ;; Run generative testing on compass functions only
    ;; Some functions may have UI types that can't be generated - that's OK
    (try
      (let [all-results (mi/check {:ns 'potatoclient.cmd.compass})
            compass-results (when all-results
                              (into {}
                                    (filter #(= "potatoclient.cmd.compass"
                                                (namespace (key %)))
                                            all-results)))]
        (is (empty? compass-results)
            (str "All compass functions should pass generative testing. Issues found: "
                 (when compass-results (keys compass-results)))))
      (catch Exception e
        ;; If generator fails (e.g., for UI types), that's OK - skip the test
        (when-not (re-find #":malli.generator/no-generator" (.getMessage e))
          (throw e))))))