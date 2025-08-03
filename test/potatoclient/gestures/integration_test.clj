(ns potatoclient.gestures.integration-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.gestures.handler :as handler]
            [potatoclient.gestures.config :as config]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.transit.commands :as commands]
            [potatoclient.process :as process]
            [potatoclient.ipc :as ipc]
            [potatoclient.ui-specs :as specs]
            [malli.core :as m])
  (:import (potatoclient.java.transit MessageKeys)))

;; Test fixtures
(def ^:dynamic *captured-commands* (atom []))
(def ^:dynamic *captured-events* (atom []))

(defn reset-test-state [f]
  (reset! *captured-commands* [])
  (reset! *captured-events* [])
  (app-db/reset-to-initial-state!)
  (config/load-gesture-config!)
  (f))

(use-fixtures :each reset-test-state)

;; Mock functions
(defn mock-send-message [subprocess-type message]
  (when (= subprocess-type :command)
    (swap! *captured-commands* conj (:payload message))))

(defn mock-handle-event [event]
  (swap! *captured-events* conj event))

;; Integration test scenarios

(deftest test-complete-tap-flow
  (testing "Complete tap gesture flow from video stream to command"
    (with-redefs [potatoclient.transit.subprocess-launcher/send-message mock-send-message]
      ;; Simulate gesture event from video stream
      (let [gesture-payload {:type :gesture
                             :gesture-type :tap
                             :timestamp 1234567890
                             :canvas-width 1920
                             :canvas-height 1080
                             :aspect-ratio 1.78
                             :stream-type :heat
                             :x 960
                             :y 540
                             :ndc-x 0.0
                             :ndc-y 0.0}
            ;; Convert to string keys for IPC
            event-payload (into {} (map (fn [[k v]] [(name k) v]) gesture-payload))]

        ;; Process through IPC dispatcher with proper envelope
        (ipc/dispatch-message :heat {:msg-type :event
                                     :payload (assoc gesture-payload :type :gesture)})

        ;; Verify command was generated
        (is (= 1 (count @*captured-commands*)))
        (let [cmd (first @*captured-commands*)]
          (is (= "rotary-goto-ndc" (:action cmd)))
          (is (= "heat" (get-in cmd [:params :channel])))
          (is (= 0.0 (get-in cmd [:params :x])))
          (is (= 0.0 (get-in cmd [:params :y]))))))))

(deftest test-complete-pan-flow
  (testing "Complete pan gesture flow with multiple updates"
    (with-redefs [potatoclient.transit.subprocess-launcher/send-message mock-send-message]
      ;; Pan start
      (let [pan-start {:type :gesture
                       :gesture-type :panstart
                       :timestamp 1000
                       :canvas-width 1920
                       :canvas-height 1080
                       :aspect-ratio 1.78
                       :stream-type :day
                       :x 500
                       :y 500
                       :ndc-x -0.48
                       :ndc-y -0.07}]
        (ipc/dispatch-message :day {:msg-type :event
                                    :payload (assoc pan-start :type :gesture)})

        ;; Verify pan state
        (let [pan-state (app-db/get-in-app-db [:gestures :pan])]
          (is (:active pan-state))
          (is (= -0.48 (:start-x pan-state))))

        ;; Pan move after throttle period
        (Thread/sleep 120)
        (let [pan-move {:type :gesture
                        :gesture-type :panmove
                        :timestamp 1150
                        :canvas-width 1920
                        :canvas-height 1080
                        :aspect-ratio 1.78
                        :stream-type :day
                        :x 600
                        :y 550
                        :delta-x 100
                        :delta-y 50
                        :ndc-delta-x 0.104
                        :ndc-delta-y -0.093}]
          (ipc/dispatch-message :day {:msg-type :event
                                      :payload (assoc pan-move :type :gesture)}))

        ;; Should have velocity command
        (is (>= (count @*captured-commands*) 1))
        (let [cmd (last @*captured-commands*)]
          (is (= "rotary-set-velocity" (:action cmd)))
          (is (number? (get-in cmd [:params :azimuth-speed])))
          (is (number? (get-in cmd [:params :elevation-speed]))))

        ;; Pan stop
        (reset! *captured-commands* [])
        (let [pan-stop {:type :gesture
                        :gesture-type :panstop
                        :timestamp 1300
                        :canvas-width 1920
                        :canvas-height 1080
                        :aspect-ratio 1.78
                        :stream-type :day
                        :x 600
                        :y 550}]
          (ipc/dispatch-message :day {:msg-type :event
                                      :payload (assoc pan-stop :type :gesture)}))

        ;; Should have halt command
        (is (= 1 (count @*captured-commands*)))
        (is (= "rotary-halt" (:action (first @*captured-commands*))))

        ;; Pan state should be inactive
        (is (false? (app-db/get-in-app-db [:gestures :pan :active])))))))

(deftest test-double-tap-with-frame-timing
  (testing "Double-tap includes frame timestamp for CV tracking"
    (with-redefs [potatoclient.transit.subprocess-launcher/send-message mock-send-message]
      (let [gesture {:type :gesture
                     :gesture-type :doubletap
                     :timestamp 1234567890
                     :canvas-width 1920
                     :canvas-height 1080
                     :aspect-ratio 1.78
                     :stream-type :heat
                     :x 1000
                     :y 600
                     :ndc-x 0.042
                     :ndc-y -0.111
                     :frame-timestamp 1234567000
                     :frame-duration 33}]

        (ipc/dispatch-message :heat {:msg-type :event
                                     :payload (assoc gesture :type :gesture)})

        (is (= 1 (count @*captured-commands*)))
        (let [cmd (first @*captured-commands*)]
          (is (= "cv-start-track-ndc" (:action cmd)))
          (is (= 1234567000 (get-in cmd [:params :frame-timestamp]))))))))

(deftest test-gesture-validation
  (testing "Gesture events are validated against specs"
    (let [valid-gesture {:type :gesture
                         :gesture-type :tap
                         :timestamp 123
                         :canvas-width 1920
                         :canvas-height 1080
                         :aspect-ratio 1.78
                         :stream-type :heat
                         :x 100
                         :y 100
                         :ndc-x 0.5
                         :ndc-y -0.5}]
      ;; Valid gesture
      (is (m/validate specs/gesture-event valid-gesture))

      ;; Invalid gesture type
      (let [invalid-gesture (assoc valid-gesture :gesture-type :invalid)]
        (is (not (m/validate specs/gesture-event invalid-gesture))))

      ;; Missing required fields
      (let [incomplete-gesture (dissoc valid-gesture :timestamp)]
        (is (not (m/validate specs/gesture-event incomplete-gesture)))))))

(deftest test-forward-command-request
  (testing "Forward command requests from video stream"
    (with-redefs [potatoclient.transit.subprocess-launcher/send-message mock-send-message]
      (let [command {:action "rotary-halt"
                     :params {}}]

        ;; Simulate request from video stream
        (ipc/dispatch-message :heat {:msg-type :request
                                     :payload {:action "forward-command"
                                               :command {:action "rotary-halt"
                                                         :params {}}}})

        ;; Command should be forwarded
        (is (= 1 (count @*captured-commands*)))
        (let [forwarded (first @*captured-commands*)]
          (is (= "rotary-halt" (:action forwarded))))))))

(deftest test-gesture-config-loading
  (testing "Gesture configuration is properly loaded"
    (let [config (config/get-gesture-config)]
      (is (map? config))
      (is (contains? config :gesture-config))
      (is (contains? config :zoom-speed-config))

      ;; Check some specific values
      (is (= 20 (get-in config [:gesture-config :move-threshold])))
      (is (= 300 (get-in config [:gesture-config :double-tap-threshold])))

      ;; Check zoom configs exist
      (is (seq (get-in config [:zoom-speed-config :heat])))
      (is (seq (get-in config [:zoom-speed-config :day]))))))

(deftest test-stream-type-routing
  (testing "Gestures are routed to correct camera based on stream type"
    (with-redefs [potatoclient.transit.subprocess-launcher/send-message mock-send-message]
      ;; Heat camera tap
      (handler/handle-tap-gesture {:type :gesture
                                   :gesture-type :tap
                                   :timestamp 123
                                   :canvas-width 1920
                                   :canvas-height 1080
                                   :aspect-ratio 1.78
                                   :stream-type :heat
                                   :ndc-x 0.5
                                   :ndc-y 0.5})

      (let [heat-cmd (last @*captured-commands*)]
        (is (= "heat" (get-in heat-cmd [:params :channel]))))

      ;; Day camera tap
      (handler/handle-tap-gesture {:type :gesture
                                   :gesture-type :tap
                                   :timestamp 456
                                   :canvas-width 1920
                                   :canvas-height 1080
                                   :aspect-ratio 1.78
                                   :stream-type :day
                                   :ndc-x -0.5
                                   :ndc-y -0.5})

      (let [day-cmd (last @*captured-commands*)]
        (is (= "day" (get-in day-cmd [:params :channel])))))))

(deftest test-pan-speed-by-zoom
  (testing "Pan speed varies by zoom level"
    (with-redefs [potatoclient.transit.subprocess-launcher/send-message mock-send-message]
      ;; Setup zoom values in app-db (1.0x and 3.0x zoom)
      (app-db/update-in-app-db! [:camera-heat :zoom] 1.0)  ; 1.0x zoom = table index 0, max speed 0.1
      (app-db/update-in-app-db! [:camera-day :zoom] 3.0)   ; 3.0x zoom = table index 2, max speed 0.5

      ;; Start pan
      (handler/handle-pan-start-gesture {:type :gesture
                                         :gesture-type :panstart
                                         :timestamp 1000
                                         :canvas-width 1920
                                         :canvas-height 1080
                                         :aspect-ratio 1.78
                                         :stream-type :heat
                                         :ndc-x 0.0
                                         :ndc-y 0.0})

      ;; Pan with heat camera at zoom 0
      (Thread/sleep 120)
      (handler/handle-pan-move-gesture {:type :gesture
                                        :gesture-type :panmove
                                        :timestamp 1150
                                        :canvas-width 1920
                                        :canvas-height 1080
                                        :aspect-ratio 1.78
                                        :stream-type :heat
                                        :ndc-delta-x 0.4
                                        :ndc-delta-y 0.0})

      (let [heat-cmd (last @*captured-commands*)
            heat-speed (get-in heat-cmd [:params :azimuth-speed])
            heat-zoom (app-db/get-in-app-db [:camera-heat :zoom])]
        (is (= 1.0 heat-zoom) "Heat zoom should be 1.0")

        ;; Stop heat camera pan
        (handler/handle-pan-stop-gesture {:type :gesture
                                          :gesture-type :panstop
                                          :timestamp 1200
                                          :canvas-width 1920
                                          :canvas-height 1080
                                          :aspect-ratio 1.78
                                          :stream-type :heat
                                          :x 1000
                                          :y 580})

        ;; Reset and test day camera at zoom 3
        (reset! *captured-commands* [])
        (handler/handle-pan-start-gesture {:type :gesture
                                           :gesture-type :panstart
                                           :timestamp 2000
                                           :canvas-width 1920
                                           :canvas-height 1080
                                           :aspect-ratio 1.78
                                           :stream-type :day
                                           :ndc-x 0.0
                                           :ndc-y 0.0})

        (Thread/sleep 120)
        (handler/handle-pan-move-gesture {:type :gesture
                                          :gesture-type :panmove
                                          :timestamp 2150
                                          :canvas-width 1920
                                          :canvas-height 1080
                                          :aspect-ratio 1.78
                                          :stream-type :day
                                          :ndc-delta-x 0.4
                                          :ndc-delta-y 0.0})

        (let [day-cmd (last @*captured-commands*)
              day-speed (get-in day-cmd [:params :azimuth-speed])
              day-zoom (app-db/get-in-app-db [:camera-day :zoom])]
          (is (= 3.0 day-zoom) "Day zoom should be 3.0")

          ;; Day at zoom index 2 should have higher speed than heat at zoom index 0
          (is (> day-speed heat-speed)))))))