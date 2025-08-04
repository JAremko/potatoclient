(ns potatoclient.transit.simple-malli-validation-test
  "Simple test to verify Malli generation works with our command API"
  (:require [clojure.test :refer [deftest testing is]]
            [malli.generator :as mg]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit-core])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; Test that we can generate valid commands and they serialize properly
(deftest test-malli-generates-valid-commands
  (testing "Basic command generation and validation"
    ;; Generate some rotary goto commands
    (let [samples (mg/sample [:map
                              [:azimuth [:double {:min 0.0 :max 360.0}]]
                              [:elevation [:double {:min -30.0 :max 90.0}]]]
                             {:size 5})]
      (doseq [sample samples]
        (testing (str "Sample: " sample)
          ;; Create command
          (let [command (cmd/rotary-goto sample)]
            ;; Verify structure
            (is (map? command))
            (is (contains? command :rotary))
            (is (= (:azimuth sample) (get-in command [:rotary :goto :azimuth])))
            (is (= (:elevation sample) (get-in command [:rotary :goto :elevation])))

            ;; Test Transit serialization
            (let [baos (ByteArrayOutputStream.)
                  writer (transit-core/make-writer baos)]
              (transit-core/write-message! writer command baos)
              (let [bais (ByteArrayInputStream. (.toByteArray baos))
                    reader (transit-core/make-reader bais)
                    decoded (transit-core/read-message reader)]
                (is (= command decoded) "Command should survive Transit roundtrip"))))))))

  (testing "CV command generation with keywords"
    (let [samples (mg/sample [:map
                              [:channel [:enum :heat :day]]
                              [:x [:and double? [:>= -1.0] [:<= 1.0]]]
                              [:y [:and double? [:>= -1.0] [:<= 1.0]]]]
                             {:size 5})]
      (doseq [sample samples]
        (let [command (cmd/cv-start-track-ndc
                        (:channel sample)
                        (:x sample)
                        (:y sample)
                        nil)]
          (is (keyword? (get-in command [:cv :start-track-ndc :channel])))
          (is (number? (get-in command [:cv :start-track-ndc :x])))
          (is (number? (get-in command [:cv :start-track-ndc :y])))))))

  (testing "Enum command generation"
    ;; Test palette generation
    (let [palettes (mg/sample [:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic]
                              {:size 10})]
      (is (= 10 (count palettes)))
      (is (every? keyword? palettes))
      (doseq [palette palettes]
        (let [command (cmd/heat-camera-palette palette)]
          (is (map? command))
          (is (number? (get-in command [:heat-camera :set-color-palette :index]))))))))