(ns potatoclient.logging-test
  (:require [clojure.test :refer :all]
            [potatoclient.logging :as logging]
            [potatoclient.init :as init]))

(deftest test-safe-shutdown
  (testing "Shutdown should handle multiple calls safely"
    ;; Ensure initialized
    (init/ensure-registry!)
    (logging/init!)
    
    ;; First shutdown should work fine
    (is (nil? (logging/shutdown!)))
    
    ;; Second shutdown should not throw NPE
    (is (nil? (logging/shutdown!)))
    
    ;; Third shutdown should also be safe
    (is (nil? (logging/shutdown!)))))

(deftest test-shutdown-without-init
  (testing "Shutdown should handle being called without initialization"
    ;; Reset the state
    (logging/shutdown!)
    
    ;; Calling shutdown without init should not throw
    (is (nil? (logging/shutdown!)))))