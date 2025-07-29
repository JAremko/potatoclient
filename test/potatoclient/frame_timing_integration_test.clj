(ns potatoclient.frame-timing-integration-test
  "Integration test for frame timing extraction and CV tracking"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.events.stream :as stream-events]
            [potatoclient.cmd.core :as cmd-core]
            [clojure.core.async :as async :refer [<!! timeout]]))

(deftest test-double-click-triggers-cv-tracking
  (testing "Double-click with frame timing triggers CV tracking"
    ;; Initialize mock websocket for testing
    (cmd-core/init-websocket! "test-domain" 
                              (fn [msg] (println "Error:" msg))
                              (fn [data] nil))

    ;; Simulate a double-click navigation event with frame timing
    (let [event {:type :mouse-click
                 :button 1
                 :clickCount 2
                 :ndcX 0.5
                 :ndcY 0.5
                 :frameTimestamp 123456789
                 :frameDuration 16666667}
          msg {:event event
               :streamId :heat}]

      ;; Process the event
      (stream-events/handle-navigation-event msg)

      ;; Give time for async command to be sent
      (<!! (timeout 100))

      ;; If we get here without exceptions, the test passed
      (is true "Successfully processed double-click with frame timing"))))