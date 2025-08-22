(ns clj-stream-spawner.events-test
  "Tests for event handling."
  (:require 
   [clojure.test :refer [deftest is testing]]
   [clj-stream-spawner.events :as events]
   [clojure.string :as str]))

(deftest handle-event-test
  (testing "Gesture event handling"
    ;; Capture printed output
    (let [output (with-out-str
                   (events/handle-event :heat
                                       {:type :gesture
                                        :gesture-type :tap
                                        :x 100
                                        :y 200
                                        :ndc-x 0.5
                                        :ndc-y -0.5}))]
      (is (str/includes? output "HEAT-GESTURE"))
      (is (str/includes? output "tap"))
      (is (str/includes? output "100"))
      (is (str/includes? output "200"))))
  
  (testing "Window event handling with dimensions"
    (let [output (with-out-str
                   (events/handle-event :day
                                       {:type :window
                                        :action :resize
                                        :width 1920
                                        :height 1080}))]
      (is (str/includes? output "DAY-WINDOW"))
      (is (str/includes? output "resize"))
      (is (str/includes? output "1920x1080"))))
  
  (testing "Window event handling with position"
    (let [output (with-out-str
                   (events/handle-event :heat
                                       {:type :window
                                        :action :window-move
                                        :x 50
                                        :y 75}))]
      (is (str/includes? output "HEAT-WINDOW"))
      (is (str/includes? output "window-move"))
      (is (str/includes? output "(50, 75)"))))
  
  (testing "Connection event handling"
    (let [output (with-out-str
                   (events/handle-event :day
                                       {:type :connection
                                        :action :connected
                                        :details "WebSocket established"}))]
      (is (str/includes? output "DAY-CONNECTION"))
      (is (str/includes? output "connected"))
      (is (str/includes? output "WebSocket established")))))

(deftest handle-log-test
  (testing "Log message with all fields"
    (let [output (with-out-str
                   (events/handle-log :heat
                                     {:level :error
                                      :message "Connection failed"
                                      :data {:code 500}}))]
      (is (str/includes? output "HEAT-LOG"))
      (is (str/includes? output "ERROR"))
      (is (str/includes? output "Connection failed"))
      (is (str/includes? output "{:code 500}"))))
  
  (testing "Log message without level defaults to INFO"
    (let [output (with-out-str
                   (events/handle-log :day
                                     {:message "Starting up"}))]
      (is (str/includes? output "DAY-LOG"))
      (is (str/includes? output "INFO"))
      (is (str/includes? output "Starting up")))))

(deftest handle-metric-test
  (testing "Metric message handling"
    (let [output (with-out-str
                   (events/handle-metric :heat
                                        {:msg-type :metric
                                         :timestamp 123456
                                         :fps 30
                                         :bitrate 1000000}))]
      (is (str/includes? output "HEAT-METRIC"))
      (is (str/includes? output "fps"))
      (is (str/includes? output "30"))
      (is (str/includes? output "bitrate"))
      ;; Should not include msg-type and timestamp
      (is (not (str/includes? output "msg-type")))
      (is (not (str/includes? output "timestamp"))))))

(deftest handle-command-test
  (testing "Command message handling"
    (let [output (with-out-str
                   (events/handle-command :day
                                         {:msg-type :command
                                          :timestamp 123456
                                          :action :rotary-goto-ndc
                                          :ndc-x 0.5
                                          :ndc-y -0.5}))]
      (is (str/includes? output "DAY-COMMAND"))
      (is (str/includes? output "rotary-goto-ndc"))
      (is (str/includes? output "ndc-x"))
      (is (str/includes? output "0.5")))))