(ns potatoclient.transit-integration-test
  "Integration tests for the Transit-based WebSocket architecture"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.commands :as commands]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.subprocess-launcher :as launcher]
            [potatoclient.logging :as logging]))

;; Test configuration
(def test-domain "test.sych.local")
(def test-timeout-ms 5000)

;; Fixtures
(defn reset-app-db-fixture
  "Reset app-db before each test"
  [f]
  (reset! app-db/app-db app-db/initial-state)
  (f)
  (reset! app-db/app-db app-db/initial-state))

(use-fixtures :each reset-app-db-fixture)

;; Helper functions
(defn wait-for-condition
  "Wait for a condition to become true with timeout"
  [pred timeout-ms]
  (let [deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (cond
        (pred) true
        (> (System/currentTimeMillis) deadline) false
        :else (do (Thread/sleep 100) (recur))))))

;; Transit Core Tests
(deftest test-transit-encoding-decoding
  (testing "Transit message encoding and decoding"
    (let [test-data {:action "ping"
                     :params {:test true}
                     :nested {:array [1 2 3]
                              :map {:a 1 :b "two"}}}
          out (java.io.ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer test-data out)
          encoded (.toByteArray out)
          in (java.io.ByteArrayInputStream. encoded)
          reader (transit-core/make-reader in)
          decoded (transit-core/read-message reader)]
      (is (= test-data decoded) "Round-trip encoding should preserve data"))))

(deftest test-message-envelope-creation
  (testing "Message envelope creation"
    (let [envelope (transit-core/create-message :command {:action "test"})]
      (is (= :command (:msg-type envelope)))
      (is (string? (:msg-id envelope)))
      (is (pos-int? (:timestamp envelope)))
      (is (= {:action "test"} (:payload envelope))))))

;; App-DB Tests
(deftest test-app-db-structure
  (testing "App-db initial structure"
    (is (map? @app-db/app-db))
    (is (contains? @app-db/app-db :server-state))
    (is (contains? @app-db/app-db :app-state))
    (is (contains? @app-db/app-db :validation))
    (is (contains? @app-db/app-db :rate-limits))))

(deftest test-app-db-updates
  (testing "App-db update functions"
    ;; Test theme update
    (app-db/set-theme! :sol-dark)
    (is (= :sol-dark (app-db/get-theme)))

    ;; Test locale update
    (app-db/set-locale! :ukrainian)
    (is (= :ukrainian (app-db/get-locale)))

    ;; Test connection update
    (app-db/set-connection-state! true test-domain 50)
    (is (= test-domain (app-db/get-connection-url)))
    (is (true? (app-db/connected?))))

  (deftest test-subsystem-updates
    (testing "Server state subsystem updates"
      (let [gps-data {:latitude 51.5074
                      :longitude -0.1278
                      :altitude 35.0
                      :fix-type "3D"
                      :satellites 8
                      :hdop 1.2
                      :use-manual false}]
        (app-db/update-subsystem! :gps gps-data)
        (is (= gps-data (app-db/get-subsystem-state :gps)))))))

;; Subprocess Launcher Tests (Mock)
(deftest test-subprocess-creation
  (testing "Subprocess data structure"
    ;; This would need a mock WebSocket URL to test properly
    ;; For now, just test the structure
    (is (fn? launcher/start-command-subprocess))
    (is (fn? launcher/start-state-subprocess))))

;; Command API Tests
(deftest test-command-functions-exist
  (testing "All command functions are defined"
    (is (fn? commands/ping))
    (is (fn? commands/noop))
    (is (fn? commands/frozen))
    (is (fn? commands/set-localization))
    (is (fn? commands/set-recording))
    (is (fn? commands/rotary-goto))
    (is (fn? commands/day-camera-zoom))))

;; WebSocket handling is now in Kotlin subprocesses

;; Integration Test (Would need mock server)
(deftest ^:integration test-full-transit-flow
  (testing "Full Transit flow with mock data"
    ;; This test demonstrates the flow but would need a mock server
    ;; to actually run the WebSocket connections

    ;; 1. Initialize the system (would start subprocesses)
    ;; (let [cmd-proc (launcher/start-command-subprocess test-domain)
    ;;       state-proc (launcher/start-state-subprocess test-domain)]

    ;; 2. Wait for connection
    ;; (is (wait-for-condition #(app-db/connected?) test-timeout-ms))

    ;; 3. Send a command via subprocess
    ;; (launcher/send-message! cmd-proc (commands/ping))

    ;; 4. Simulate state update
    (app-db/update-subsystem! :system {:battery-level 85
                                       :localization "en"
                                       :recording false})

    ;; 5. Verify state
    (is (= 85 (get-in (app-db/get-subsystem-state :system) [:battery-level])))

    ;; 6. Cleanup
    ;; (launcher/stop-subprocess! cmd-proc)
    ;; (launcher/stop-subprocess! state-proc)
    ;; )
    ))

;; Rate Limiting Tests
(deftest test-rate-limit-tracking
  (testing "Rate limit metrics"
    (app-db/set-max-rate-hz! 30)
    (app-db/update-rate-metrics! 25.5 false)
    ;; Simulate 10 dropped updates
    (dotimes [_ 10]
      (app-db/update-rate-metrics! 25.5 true))
    (let [limits (app-db/get-rate-limits)]
      (is (= 30 (:max-rate-hz limits)))
      (is (= 25.5 (:current-rate limits)))
      (is (= 10 (:dropped-updates limits))))))

;; Validation Tests
(deftest test-validation-tracking
  (testing "Validation error tracking"
    (app-db/add-validation-error! :malli :gps [{:field "latitude"
                                                :constraint "must be between -90 and 90"
                                                :value -91}])
    (let [errors (:errors (app-db/get-validation-state))]
      (is (= 1 (count errors)))
      (is (= :gps (:subsystem (first errors)))))))

;; Watch Tests
(deftest test-app-db-watchers
  (testing "App-db change notifications"
    (let [changes (atom [])
          watch-key ::test-watcher]
      ;; Add watcher
      (add-watch app-db/app-db watch-key
                 (fn [_ _ old new]
                   (swap! changes conj {:old old :new new})))

      ;; Make changes
      (app-db/set-theme! :sol-light)
      (app-db/set-locale! :ukrainian)

      ;; Verify watcher was called
      (is (>= (count @changes) 2))

      ;; Cleanup
      (remove-watch app-db/app-db watch-key))))

;; Performance Tests
(deftest ^:performance test-rapid-updates
  (testing "Rapid state updates don't cause issues"
    (let [start-time (System/currentTimeMillis)]
      ;; Simulate rapid GPS updates
      (dotimes [i 100]
        (app-db/update-subsystem! :gps {:latitude (+ 50.0 (* 0.01 i))
                                        :longitude (+ -1.0 (* 0.01 i))
                                        :altitude 100.0
                                        :fix-type "3D"
                                        :satellites 10
                                        :hdop 1.0
                                        :use-manual false}))
      (let [elapsed (- (System/currentTimeMillis) start-time)]
        (is (< elapsed 1000) "100 updates should complete within 1 second")))))

;; Error Handling Tests
(deftest test-error-handling
  (testing "System handles errors gracefully"
    ;; Command validation happens in Guardrails at runtime
    ;; Invalid subsystem returns nil
    (is (nil? (app-db/get-subsystem-state :invalid-subsystem)))))