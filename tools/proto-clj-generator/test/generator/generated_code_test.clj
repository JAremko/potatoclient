(ns generator.generated-code-test
  "Test that generated code works correctly"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.proto.cmd.gps :as gps]
            [potatoclient.proto.command :as command]))

(deftest gps-command-test
  (testing "GPS start command"
    (let [start-cmd (gps/build-start {})
          root-cmd (gps/build-root {:cmd {:start start-cmd}})
          parsed (gps/parse-root root-cmd)]
      (is (some? root-cmd))
      (is (map? parsed))
      (is (contains? parsed :cmd))
      (is (contains? (:cmd parsed) :start))))
  
  (testing "GPS set-manual-position command"
    (let [set-pos-cmd (gps/build-set-manual-position
                       {:latitude 45.5
                        :longitude -122.6
                        :altitude 100.0})
          root-cmd (gps/build-root {:cmd {:set-manual-position set-pos-cmd}})
          parsed (gps/parse-root root-cmd)]
      (is (some? set-pos-cmd))
      (is (= 45.5 (.getLatitude set-pos-cmd)))
      (is (= -122.6 (.getLongitude set-pos-cmd)))
      (is (= 100.0 (.getAltitude set-pos-cmd)))
      (is (contains? (:cmd parsed) :set-manual-position))))
  
  (testing "Command index re-exports"
    ;; Test that command namespace re-exports work
    (is (= gps/build-start command/build-start))
    (is (= gps/parse-start command/parse-start))
    (is (= gps/build-set-manual-position command/build-set-manual-position))
    (is (= gps/parse-set-manual-position command/parse-set-manual-position))))