(ns potatoclient.transit-test
  "Comprehensive tests for Transit-based architecture"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.schemas :as schemas]
            [potatoclient.transit.handlers :as handlers]
            [malli.core :as m]
            [clojure.core.async :as async]))

;; Test fixtures
(defn reset-app-db-fixture [f]
  (app-db/reset-to-initial-state!)
  (f)
  (app-db/reset-to-initial-state!))

(use-fixtures :each reset-app-db-fixture)

;; Transit core tests
(deftest transit-message-encoding-test
  (testing "Message envelope creation"
    (let [msg (transit-core/create-message :test {:data "value"})]
      (is (and (contains? msg :msg-type)
               (contains? msg :msg-id)
               (contains? msg :timestamp)
               (contains? msg :payload)))
      (is (= :test (:msg-type msg)))
      (is (string? (:msg-id msg)))
      (is (pos-int? (:timestamp msg)))
      (is (= {:data "value"} (:payload msg)))))

  (testing "Round-trip encoding/decoding"
    (let [original {:test "data" :number 42 :nested {:key "value"}}
          encoded (transit-core/encode-to-bytes original)
          decoded (transit-core/decode-from-bytes encoded)]
      (is (bytes? encoded))
      (is (= original decoded))))

  (testing "Message type predicates"
    (let [state-msg (transit-core/create-message :state {:system {:battery 85}})
          cmd-msg (transit-core/create-message :command {:action "test"})
          ctrl-msg (transit-core/create-message :control {:action :shutdown})]
      (is (transit-core/state-message? state-msg))
      (is (transit-core/command-message? cmd-msg))
      (is (transit-core/control-message? ctrl-msg)))))

;; App-db tests
(deftest app-db-structure-test
  (testing "Initial state validation"
    (let [state @app-db/app-db]
      (is (schemas/validate-app-db state))
      (is (map? (:server-state state)))
      (is (map? (:app-state state)))
      (is (map? (:validation state)))
      (is (map? (:rate-limits state)))))

  (testing "State updates"
    (let [new-system-state {:battery-level 75
                            :localization "uk"
                            :recording true}]
      (app-db/update-subsystem! :system new-system-state)
      (is (= 75 (get-in @app-db/app-db [:server-state :system :battery-level])))
      (is (= "uk" (get-in @app-db/app-db [:server-state :system :localization])))))

  (testing "Connection state"
    (app-db/set-connection-state! true "wss://test.local" 42)
    (is (app-db/connected?))
    (is (= "wss://test.local" (app-db/get-connection-url)))
    (is (= 42 (get-in @app-db/app-db [:app-state :connection :latency-ms])))))

;; Schema validation tests
(deftest schema-validation-test
  (testing "GPS schema validation"
    (let [valid-gps {:latitude 51.5074
                     :longitude -0.1278
                     :altitude 35.0
                     :fix-type "3D"
                     :satellites 8
                     :hdop 1.2
                     :use-manual false}
          invalid-gps (assoc valid-gps :latitude 200)] ; Invalid latitude
      (is (schemas/validate-subsystem :gps valid-gps))
      (is (not (schemas/validate-subsystem :gps invalid-gps)))))

  (testing "Rate limits schema"
    (let [valid-limits {:max-rate-hz 30
                        :current-rate 25.5
                        :dropped-updates 10
                        :last-update-time 1234567890}
          invalid-limits (assoc valid-limits :max-rate-hz 150)] ; Too high
      (is (m/validate schemas/rate-limits-schema valid-limits))
      (is (not (m/validate schemas/rate-limits-schema invalid-limits)))))

  (testing "Message envelope schemas"
    (let [state-msg {:msg-type :state
                     :msg-id "123"
                     :timestamp 1234567890
                     :payload {:system {}}}
          invalid-msg (dissoc state-msg :msg-id)]
      (is (schemas/validate-state-message state-msg))
      (is (not (schemas/validate-message invalid-msg))))))

;; Handler tests
(deftest message-handler-test
  (testing "Handler registration"
    (let [called (atom false)
          test-handler (fn [msg] (reset! called true))]
      (handlers/register-handler! :test test-handler)
      ;; Would need to trigger handler through message processing
      (handlers/unregister-handler! :test)))

  (testing "State message handling"
    (let [state-update {:system {:battery-level 90}}
          msg (transit-core/create-message :state state-update)]
      ;; Simulate state message handling
      (#'handlers/handle-state-message msg)
      (is (= 90 (get-in @app-db/app-db [:server-state :system :battery-level])))))

  (testing "Validation error handling"
    (let [error-msg {:msg-type :validation-error
                     :msg-id "123"
                     :timestamp 1234567890
                     :payload {:source :buf-validate
                               :subsystem :gps
                               :errors [{:field "latitude"
                                         :constraint "range"
                                         :value -100}]}}]
      (#'handlers/handle-validation-error error-msg)
      (is (= 1 (count (get-in @app-db/app-db [:validation :errors])))))))

;; Rate metrics tests
(deftest rate-metrics-test
  (testing "Rate metric updates"
    (app-db/update-rate-metrics! 25.5 false)
    (let [limits (app-db/get-rate-limits)]
      (is (= 25.5 (:current-rate limits)))
      (is (= 0 (:dropped-updates limits))))

    (app-db/update-rate-metrics! 30.0 true)
    (let [limits (app-db/get-rate-limits)]
      (is (= 30.0 (:current-rate limits)))
      (is (= 1 (:dropped-updates limits))))))

;; Process state tests
(deftest process-state-test
  (testing "Process state management"
    (app-db/set-process-state! :state-proc 12345 :running)
    (is (app-db/process-running? :state-proc))
    (is (= 12345 (get-in @app-db/app-db [:app-state :processes :state-proc :pid])))

    (app-db/set-process-state! :state-proc nil :stopped)
    (is (not (app-db/process-running? :state-proc)))))

;; UI state tests
(deftest ui-state-test
  (testing "Theme management"
    (app-db/set-theme! :sol-light)
    (is (= :sol-light (app-db/get-theme))))

  (testing "Locale management"
    (app-db/set-locale! :ukrainian)
    (is (= :ukrainian (app-db/get-locale))))

  (testing "Read-only mode"
    (app-db/set-read-only-mode! true)
    (is (app-db/read-only-mode?))))

;; Validation control tests
(deftest validation-control-test
  (testing "Validation error tracking"
    (app-db/add-validation-error! :malli :gps [{:field "latitude" :error "out of range"}])
    (let [validation (app-db/get-validation-state)]
      (is (= 1 (count (:errors validation))))
      (is (= 1 (get-in validation [:stats :failed-validations]))))

    (app-db/reset-validation-errors!)
    (is (empty? (get-in @app-db/app-db [:validation :errors])))))

;; Watch handler tests
(deftest watch-handler-test
  (testing "State change notifications"
    (let [changes (atom [])
          watch-fn (fn [k ref old new]
                     (swap! changes conj {:old (:battery-level (:system (:server-state old)))
                                          :new (:battery-level (:system (:server-state new)))}))]
      (app-db/add-watch-handler :test-watch watch-fn)
      (app-db/update-subsystem! :system {:battery-level 80})
      (app-db/update-subsystem! :system {:battery-level 70})
      (app-db/remove-watch-handler :test-watch)

      (is (= 2 (count @changes)))
      (is (= 80 (:new (first @changes)))))))

;; Integration test
(deftest integration-test
  (testing "Complete state update flow"
    ;; Initial state
    (let [initial-state @app-db/app-db]
      (is (schemas/validate-app-db initial-state))

      ;; Simulate state updates from Kotlin
      (let [gps-update {:latitude 51.5074
                        :longitude -0.1278
                        :altitude 100.0
                        :fix-type "3D"
                        :satellites 12
                        :hdop 0.9
                        :use-manual false}
            system-update {:battery-level 95
                           :localization "en"
                           :recording false}]

        ;; Update state
        (app-db/update-subsystem! :gps gps-update)
        (app-db/update-subsystem! :system system-update)

        ;; Verify updates
        (is (= 51.5074 (get-in @app-db/app-db [:server-state :gps :latitude])))
        (is (= 95 (get-in @app-db/app-db [:server-state :system :battery-level])))

        ;; Validate final state
        (is (schemas/validate-app-db @app-db/app-db))))))