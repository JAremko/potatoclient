(ns potatoclient.transit-handler-test
  "Integration tests for Transit handlers in command and state subprocesses"
  (:require [clojure.test :refer :all]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.validation :as validation]
            [potatoclient.specs :as specs]
            [clojure.java.io :as io])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(deftest test-transit-write-handlers
  (testing "Transit handlers can serialize protobuf messages"
    ;; This test verifies that the Transit handlers are working correctly
    ;; by checking that we can write and read messages with proper types

    (let [;; Create a test message that would come from protobuf
          test-state {:system {:battery-level 85
                               :is-recording true
                               :localization :en}
                      :rotary {:azimuth 45.5
                               :elevation 30.0
                               :mode :stabilized}
                      :gps {:latitude 40.7128
                            :longitude -74.0060
                            :fix-type :3d}}

          ;; Wrap in message envelope
          message (transit-core/create-message :state-update test-state)

          ;; Serialize and deserialize
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer message out)

          ;; Read it back
          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]

      ;; Verify structure
      (is (map? result))
      (is (= :state-update (:msg-type result)))
      (is (string? (:msg-id result)))
      (is (number? (:timestamp result)))

      ;; Verify payload - keywords should be preserved
      (let [payload (:payload result)]
        (is (= 85 (get-in payload [:system :battery-level])))
        (is (= true (get-in payload [:system :is-recording])))
        (is (= :en (get-in payload [:system :localization])))
        (is (= :stabilized (get-in payload [:rotary :mode])))
        (is (= :3d (get-in payload [:gps :fix-type])))))))

(deftest test-command-building-with-handlers
  (testing "Commands can be built using Transit patterns"
    ;; This simulates how CommandSubprocess would receive and process commands

    (let [;; Create a rotary goto command
          command-msg {:action "rotary-goto-ndc"
                       :params {:channel "heat"
                                :x 0.5
                                :y -0.5}}

          ;; Wrap in envelope
          message (transit-core/create-message :command command-msg)

          ;; Validate the message
          [valid? errors] (validation/validate-message message)]

      (is valid? (str "Message validation failed: " errors))
      (is (= :command (:msg-type message)))
      (is (= "rotary-goto-ndc" (get-in message [:payload :action]))))))

(deftest test-event-message-handling
  (testing "Event messages use proper keyword types"
    (let [;; Create a gesture event
          gesture-event {:type :gesture
                         :gesture-type :double-tap
                         :x 100
                         :y 200
                         :canvas-width 800
                         :canvas-height 600}

          ;; Wrap in envelope
          message (transit-core/create-message :event gesture-event)

          ;; Serialize and deserialize
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer message out)

          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]

      ;; Verify event structure with keywords
      (is (= :event (:msg-type result)))
      (is (= :gesture (get-in result [:payload :type])))
      (is (= :double-tap (get-in result [:payload :gesture-type])))
      (is (= 100 (get-in result [:payload :x])))
      (is (= 200 (get-in result [:payload :y]))))))

(deftest test-control-message-type
  (testing "Control messages use CONTROL type not RESPONSE"
    (let [control-msg {:action "shutdown"}
          message (transit-core/create-message :control control-msg)

          [valid? errors] (validation/validate-message message)]

      (is valid? (str "Control message validation failed: " errors))
      (is (= :control (:msg-type message)))
      (is (= "shutdown" (get-in message [:payload :action]))))))

(deftest test-error-message-with-keywords
  (testing "Error messages preserve text but use keywords for types"
    (let [error-msg {:context "WebSocket error"
                     :error "Connection refused"  ; Text preserved
                     :class "java.net.ConnectException"
                     :process "state"}

          message (transit-core/create-message :error error-msg)

          ;; Serialize and deserialize
          out (ByteArrayOutputStream.)
          writer (transit-core/make-writer out)
          _ (transit-core/write-message! writer message out)

          in (ByteArrayInputStream. (.toByteArray out))
          reader (transit-core/make-reader in)
          result (transit-core/read-message reader)]

      ;; Error text should be preserved as strings
      (is (= :error (:msg-type result)))
      (is (= "Connection refused" (get-in result [:payload :error])))
      (is (= "WebSocket error" (get-in result [:payload :context])))
      ;; But process type could be keyword
      (is (= "state" (get-in result [:payload :process]))))))

(deftest test-comprehensive-state-message
  (testing "Full state message with all data types"
    (let [full-state {:timestamp 1234567890
                      :protocol-version 1
                      :system {:battery-level 95
                               :is-recording false
                               :localization :ua
                               :transport-mode false}
                      :rotary {:azimuth 180.0
                               :elevation 45.0
                               :azimuth-velocity 0.0
                               :elevation-velocity 0.0
                               :mode :platform}
                      :gps {:latitude 50.4501
                            :longitude 30.5234
                            :altitude 150.0
                            :fix-type :dgps
                            :satellites 12}
                      :compass {:heading 270.0
                                :pitch 0.0
                                :roll 0.0
                                :calibrated true}
                      :lrf {:distance 1250.5
                            :measuring false
                            :continuous-mode false}
                      :time {:utc-offset 120
                             :sync-status :gps}
                      :camera-heat {:enabled true
                                    :zoom {:current-index 2
                                           :total-positions 5}
                                    :settings {:palette :white-hot}}
                      :camera-day {:enabled true
                                   :zoom {:current-index 0
                                          :total-positions 10}
                                   :settings {:exposure-mode :auto}}}

          message (transit-core/create-message :state-update full-state)
          [valid? errors] (validation/validate-message message)]

      (is valid? (str "Full state validation failed: " errors))

      ;; Verify all enum values are keywords
      (is (= :ua (get-in message [:payload :system :localization])))
      (is (= :platform (get-in message [:payload :rotary :mode])))
      (is (= :dgps (get-in message [:payload :gps :fix-type])))
      (is (= :gps (get-in message [:payload :time :sync-status])))
      (is (= :white-hot (get-in message [:payload :camera-heat :settings :palette])))
      (is (= :auto (get-in message [:payload :camera-day :settings :exposure-mode]))))))

;; Test fixture for subprocess communication
(defn- create-test-subprocess
  "Create a mock subprocess for testing Transit communication"
  [subprocess-type handler-fn]
  ;; This would create actual subprocess in real integration test
  ;; For now, we just test the Transit serialization
  {:type subprocess-type
   :handler handler-fn})

(deftest test-subprocess-message-flow
  (testing "Message flow through subprocess protocol"
    ;; This test simulates the full message flow
    (let [messages (atom [])

          ;; Mock subprocess that collects messages
          mock-subprocess (create-test-subprocess
                            :command
                            (fn [msg] (swap! messages conj msg)))

          ;; Send a command
          cmd-msg (transit-core/create-message
                    :command
                    {:action "ping"})

          ;; In real test, would send via subprocess
          ;; For now, just verify message structure
          [valid? errors] (validation/validate-message cmd-msg)]

      (is valid? (str "Command message invalid: " errors))
      (is (= :command (:msg-type cmd-msg)))
      (is (= "ping" (get-in cmd-msg [:payload :action]))))))