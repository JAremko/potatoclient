(ns potatoclient.state.server.ingress-retry-test
  "Test exponential backoff and persistent retry logic"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.state.server.ingress :as ingress]))

(deftest test-calculate-backoff-delay
  (testing "Exponential backoff calculation"
    ;; Access the private function via var
    (let [calc-backoff #'ingress/calculate-backoff-delay
          calculate-backoff (fn [attempt] (@calc-backoff attempt))]
      
      ;; Test base case
      (let [delay (calculate-backoff 0)]
        (is (and (>= delay 950) (<= delay 1050))
            "First attempt should be ~1 second"))
      
      ;; Test exponential growth
      (let [delay (calculate-backoff 1)]
        (is (and (>= delay 1900) (<= delay 2100))
            "Second attempt should be ~2 seconds"))
      
      (let [delay (calculate-backoff 2)]
        (is (and (>= delay 3800) (<= delay 4200))
            "Third attempt should be ~4 seconds"))
      
      ;; Test max delay cap
      (let [delay (calculate-backoff 10)]
        (is (<= delay 31500)
            "Delay should not exceed 30 seconds + jitter"))
      
      ;; Test that jitter is applied
      (let [delays (repeatedly 10 #(calculate-backoff 2))]
        (is (> (count (distinct delays)) 1)
            "Multiple calls should produce different values due to jitter")))))

(deftest test-connection-stats-tracking
  (testing "Connection statistics updates"
    (let [config {:domain "test.local"
                  :throttle-ms 100
                  :timeout-ms 2000}
          manager (ingress/create-manager config)
          stats (:connection-stats manager)]
      
      ;; Initial state
      (is (= :disconnected (:status @stats)))
      (is (= 0 (:attempts @stats)))
      (is (= 0 (:consecutive-failures @stats)))
      
      ;; Update stats via private function
      (let [update-stats! #'ingress/update-connection-stats!]
        
        ;; Attempting connection
        (@update-stats! manager :attempting)
        (is (= :connecting (:status @stats)))
        (is (= 1 (:attempts @stats)))
        
        ;; Connection failed
        (@update-stats! manager :failed :error (Exception. "test error"))
        (is (= :disconnected (:status @stats)))
        (is (= 1 (:consecutive-failures @stats)))
        (is (= "test error" (:last-error @stats)))
        
        ;; Another attempt
        (@update-stats! manager :attempting)
        (is (= 2 (:attempts @stats)))
        
        ;; Connection succeeded
        (@update-stats! manager :connected)
        (is (= :connected (:status @stats)))
        (is (= 0 (:consecutive-failures @stats))
            "Consecutive failures should reset on success")
        (is (nil? (:last-error @stats))
            "Last error should clear on success")))))

(deftest test-manager-lifecycle
  (testing "Manager can be started and stopped"
    (let [config {:domain "test.local"
                  :throttle-ms 100
                  :timeout-ms 2000}
          manager (ingress/create-manager config)]
      
      ;; Initial state
      (is (not @(:running? manager)))
      (is (not (ingress/connected? manager)))
      
      ;; Start manager
      (ingress/start manager)
      (is @(:running? manager))
      
      ;; Stop manager
      (ingress/stop manager)
      (is (not @(:running? manager)))
      (is (= :disconnected (:status (ingress/get-connection-stats manager)))))))