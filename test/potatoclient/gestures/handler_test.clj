(ns potatoclient.gestures.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.gestures.handler :as handler]
            [potatoclient.gestures.config :as config]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.process :as process]
            [potatoclient.specs :as specs]))

;; Test fixtures
(def ^:dynamic *captured-commands* (atom []))

(defn reset-test-state
  "Reset test state fixture"
  [f]
  (reset! *captured-commands* [])
  (app-db/reset-to-initial-state!)
  (config/load-gesture-config!)
  (f))

(use-fixtures :each reset-test-state)

;; Mock the process/send-command to capture commands
(defn mock-send-command [_ command]
  (swap! *captured-commands* conj command))

;; Test data
(def test-tap-gesture
  {:type "gesture"
   :gesture-type "tap"
   :timestamp 1234567890
   :canvas-width 1920
   :canvas-height 1080
   :aspect-ratio 1.78
   :stream-type "heat"
   :x 960
   :y 540
   :ndc-x 0.0
   :ndc-y 0.0})

(def test-double-tap-gesture
  {:type "gesture"
   :gesture-type "doubletap"
   :timestamp 1234567890
   :canvas-width 1920
   :canvas-height 1080
   :aspect-ratio 1.78
   :stream-type "day"
   :x 480
   :y 270
   :ndc-x -0.5
   :ndc-y 0.5
   :frame-timestamp 1234567000
   :frame-duration 33})

(def test-pan-start-gesture
  {:type "gesture"
   :gesture-type "panstart"
   :timestamp 1234567890
   :canvas-width 1920
   :canvas-height 1080
   :aspect-ratio 1.78
   :stream-type "heat"
   :x 960
   :y 540
   :ndc-x 0.0
   :ndc-y 0.0})

(def test-pan-move-gesture
  {:type "gesture"
   :gesture-type "panmove"
   :timestamp 1234567990
   :canvas-width 1920
   :canvas-height 1080
   :aspect-ratio 1.78
   :stream-type "heat"
   :x 1000
   :y 580
   :delta-x 40
   :delta-y 40
   :ndc-delta-x 0.1
   :ndc-delta-y -0.1})

(def test-pan-stop-gesture
  {:type "gesture"
   :gesture-type "panstop"
   :timestamp 1234568000
   :canvas-width 1920
   :canvas-height 1080
   :aspect-ratio 1.78
   :stream-type "heat"
   :x 1000
   :y 580})

(def test-swipe-gesture
  {:type "gesture"
   :gesture-type "swipe"
   :timestamp 1234567890
   :canvas-width 1920
   :canvas-height 1080
   :aspect-ratio 1.78
   :stream-type "heat"
   :direction "right"
   :distance 150})

;; Tests

(deftest test-handle-tap-gesture
  "Test tap gesture handling"
  (testing "Tap gesture sends rotary-goto-ndc command"
    (with-redefs [process/send-command mock-send-command]
      (handler/handle-tap-gesture test-tap-gesture)
      (is (= 1 (count @*captured-commands*)))
      (let [cmd (first @*captured-commands*)]
        (is (= "rotary-goto-ndc" (:action cmd)))
        (is (= "heat" (get-in cmd [:params :channel])))
        (is (= 0.0 (get-in cmd [:params :x])))
        (is (= 0.0 (get-in cmd [:params :y])))))))

(deftest test-handle-double-tap-gesture
  "Test double-tap gesture handling"
  (testing "Double-tap gesture sends cv-start-track-ndc command"
    (with-redefs [process/send-command mock-send-command]
      (handler/handle-double-tap-gesture test-double-tap-gesture)
      (is (= 1 (count @*captured-commands*)))
      (let [cmd (first @*captured-commands*)]
        (is (= "cv-start-track-ndc" (:action cmd)))
        (is (= "day" (get-in cmd [:params :channel])))
        (is (= -0.5 (get-in cmd [:params :x])))
        (is (= 0.5 (get-in cmd [:params :y])))
        (is (= 1234567000 (get-in cmd [:params :frame-timestamp])))))))

(deftest test-handle-pan-gestures
  "Test pan gesture sequence handling"
  (testing "Pan gesture flow"
    (with-redefs [process/send-command mock-send-command]
      ;; Start pan
      (handler/handle-pan-start-gesture test-pan-start-gesture)
      (let [pan-state (app-db/get-in-app-db [:gestures :pan])]
        (is (:active pan-state))
        (is (= 0.0 (:start-x pan-state)))
        (is (= 0.0 (:start-y pan-state))))

      ;; Move pan (with time delay to pass throttle)
      (Thread/sleep 120)
      (handler/handle-pan-move-gesture test-pan-move-gesture)
      (is (= 1 (count @*captured-commands*)))
      (let [cmd (first @*captured-commands*)]
        (is (= "rotary-set-velocity" (:action cmd)))
        (is (number? (get-in cmd [:params :azimuth-speed])))
        (is (number? (get-in cmd [:params :elevation-speed])))
        (is (contains? #{:clockwise :counter-clockwise}
                       (keyword (get-in cmd [:params :azimuth-direction]))))
        (is (contains? #{:clockwise :counter-clockwise}
                       (keyword (get-in cmd [:params :elevation-direction])))))

      ;; Stop pan
      (reset! *captured-commands* [])
      (handler/handle-pan-stop-gesture test-pan-stop-gesture)
      (is (= 1 (count @*captured-commands*)))
      (let [cmd (first @*captured-commands*)]
        (is (= "rotary-halt" (:action cmd))))
      (let [pan-state (app-db/get-in-app-db [:gestures :pan])]
        (is (not (:active pan-state)))))))

(deftest test-handle-swipe-gesture
  "Test swipe gesture handling"
  (testing "Swipe gesture is handled without errors"
    (handler/handle-swipe-gesture test-swipe-gesture)
    ;; Swipe doesn't send commands currently, just verify no errors
    (is true)))

(deftest test-handle-gesture-event
  "Test gesture event dispatcher"
  (testing "Gesture event dispatcher routes to correct handlers"
    (with-redefs [process/send-command mock-send-command]
      ;; Test each gesture type
      (handler/handle-gesture-event test-tap-gesture)
      (is (= 1 (count @*captured-commands*)))
      (is (= "rotary-goto-ndc" (:action (first @*captured-commands*))))

      (reset! *captured-commands* [])
      (handler/handle-gesture-event test-double-tap-gesture)
      (is (= 1 (count @*captured-commands*)))
      (is (= "cv-start-track-ndc" (:action (first @*captured-commands*))))

      (reset! *captured-commands* [])
      (handler/handle-gesture-event test-swipe-gesture)
      (is (= 0 (count @*captured-commands*))))))

(deftest test-calculate-rotation-speeds
  "Test rotation speed calculation"
  (testing "Rotation speed calculation"
    (let [config {:max-rotation-speed 1.0
                  :min-rotation-speed 0.0001
                  :ndc-threshold 0.5
                  :dead-zone-radius 0.05
                  :curve-steepness 4.0}]

      (testing "Dead zone filtering"
        (let [[az-speed el-speed] (config/calculate-rotation-speeds 0.01 0.01 config)]
          (is (= 0.0 az-speed))
          (is (= 0.0 el-speed))))

      (testing "Speed calculation outside dead zone"
        (let [[az-speed el-speed] (config/calculate-rotation-speeds 0.2 0.1 config)]
          (is (> az-speed 0.0))
          (is (> el-speed 0.0))
          (is (<= az-speed (:max-rotation-speed config)))
          (is (>= az-speed (:min-rotation-speed config)))))

      (testing "Maximum speed at threshold"
        (let [[az-speed el-speed] (config/calculate-rotation-speeds 0.5 0.0 config)]
          (is (> az-speed 0.0))
          (is (= 0.0 el-speed)))))))

(deftest test-get-speed-config-for-zoom
  "Test speed config retrieval by zoom level"
  (testing "Speed configuration retrieval"
    ;; Mock config to return test data
    (with-redefs [config/get-gesture-config
                  (fn [] {:zoom-speed-config
                          {:heat [{:zoom-table-index 0
                                   :max-rotation-speed 0.1
                                   :min-rotation-speed 0.0001
                                   :ndc-threshold 0.5
                                   :dead-zone-radius 0.05
                                   :curve-steepness 4.0}
                                  {:zoom-table-index 1
                                   :max-rotation-speed 0.25
                                   :min-rotation-speed 0.0001
                                   :ndc-threshold 0.5
                                   :dead-zone-radius 0.05
                                   :curve-steepness 4.0}
                                  {:zoom-table-index 4
                                   :max-rotation-speed 1.0
                                   :min-rotation-speed 0.0001
                                   :ndc-threshold 0.5
                                   :dead-zone-radius 0.05
                                   :curve-steepness 4.0}]
                           :day [{:zoom-table-index 0
                                  :max-rotation-speed 0.05
                                  :min-rotation-speed 0.0001
                                  :ndc-threshold 0.5
                                  :dead-zone-radius 0.05
                                  :curve-steepness 4.0}]}})]

      (testing "Heat camera zoom values"
        (let [config0 (config/get-speed-config-for-zoom-value "heat" 1.0)]  ; 1.0x = index 0
          (is (= 0.1 (:max-rotation-speed config0))))
        (let [config1 (config/get-speed-config-for-zoom-value "heat" 2.0)]  ; 2.0x = index 1
          (is (= 0.25 (:max-rotation-speed config1)))))

      (testing "Day camera zoom values"
        (let [config0 (config/get-speed-config-for-zoom-value "day" 1.0)]  ; 1.0x = index 0
          (is (= 0.05 (:max-rotation-speed config0)))))

      (testing "Default fallback for out of range zoom"
        (let [config (config/get-speed-config-for-zoom-value "heat" 10.0)]  ; 10.0x = clamped to index 4
          ;; Should get config for index 4
          (is (= 1.0 (:max-rotation-speed config)))))

      (testing "Default fallback for unknown camera"
        (let [config (config/get-speed-config-for-zoom-value "unknown" 1.0)]
          (is (= 1.0 (:max-rotation-speed config))))))))

(deftest test-pan-throttling
  "Test pan gesture throttling"
  (testing "Pan move commands are throttled"
    (with-redefs [process/send-command mock-send-command]
      ;; Start pan
      (handler/handle-pan-start-gesture test-pan-start-gesture)

      ;; Rapid pan moves - only first should go through
      (handler/handle-pan-move-gesture test-pan-move-gesture)
      (handler/handle-pan-move-gesture test-pan-move-gesture)
      (handler/handle-pan-move-gesture test-pan-move-gesture)

      (is (= 0 (count @*captured-commands*))
          "Commands should be throttled within 100ms")

      ;; Wait for throttle period
      (Thread/sleep 120)
      (handler/handle-pan-move-gesture test-pan-move-gesture)
      (is (= 1 (count @*captured-commands*))
          "Command should be sent after throttle period"))))