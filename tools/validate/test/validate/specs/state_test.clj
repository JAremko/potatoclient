(ns validate.specs.state-test
  "Test State specs against actual EDN data from state-explorer"
  (:require
   [clojure.test :refer [deftest testing is]]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [malli.core :as m]
   [malli.error :as me]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.root]))

;; Initialize the global registry with oneof-edn schema
(defn init-registry! []
  (registry/setup-global-registry!
    (oneof-edn/register-oneof-edn-schema!)))

;; Load sample EDN data
(def sample-state-edn
  {:actual-space-time {:altitude 0.291143
                       :azimuth 256.62
                       :elevation 7.04
                       :latitude 50.02363
                       :longitude 15.815215
                       :timestamp 1754665407}
   :camera-day {:clahe-level 0.16
                :digital-zoom-level 1.0
                :focus-pos 1.0
                :fx-mode :jon-gui-data-fx-mode-day-a
                :infrared-filter true
                :iris-pos 0.03
                :zoom-pos 0.59938735
                :zoom-table-pos 3
                :zoom-table-pos-max 4}
   :camera-heat {:agc-mode :jon-gui-data-video-channel-heat-agc-mode-2
                 :clahe-level 0.5
                 :digital-zoom-level 1.0
                 :filter :jon-gui-data-video-channel-heat-filter-hot-white
                 :fx-mode :jon-gui-data-fx-mode-heat-a
                 :zoom-table-pos 3
                 :zoom-table-pos-max 4}
   :compass {:azimuth 335.3625
             :bank 0.7312500000000001
             :elevation 3.6}
   :compass-calibration {:final-stage 12
                         :status :jon-gui-data-compass-calibrate-status-not-calibrating
                         :target-azimuth 56.25
                         :target-bank -5.625
                         :target-elevation 6.75}
   :day-cam-glass-heater {}
   :gps {:altitude 0.291143
         :fix-type :jon-gui-data-gps-fix-type-3d
         :latitude 50.023629
         :longitude 15.815214999999998
         :manual-latitude 50.023604
         :manual-longitude 15.815316}
   :lrf {:measure-id 52
         :pointer-mode :jon-gui-data-lrf-laser-pointer-mode-off
         :target {:observer-azimuth 356.40000000000003
                  :observer-elevation -0.675
                  :observer-fix-type :jon-gui-data-gps-fix-type-2d
                  :observer-latitude 8.0
                  :observer-longitude 7.0
                  :target-id 52
                  :target-latitude 50.023638999999996
                  :target-longitude 15.815211999999999
                  :timestamp 1754576916
                  :uuid-part1 -494581931
                  :uuid-part2 -224575107
                  :uuid-part3 -1771114019
                  :uuid-part4 879344611}}
   :meteo-internal {}
   :protocol-version 1
   :rec-osd {:day-osd-enabled true
             :heat-osd-enabled true
             :screen :jon-gui-data-rec-osd-screen-main}
   :rotary {:azimuth 335.3625
            :current-scan-node {:azimuth 0.001
                                :dayzoomtablevalue 1
                                :elevation 0.001
                                :heatzoomtablevalue 1
                                :index 1
                                :linger 0.001
                                :speed 0.001}
            :mode :jon-gui-data-rotary-mode-position
            :platform-azimuth 256.62
            :platform-elevation 7.04
            :scan-target 1
            :scan-target-max 1}
   :system {:cpu-load 42.0
            :cpu-temperature 42.0
            :cur-video-rec-dir-day 8
            :cur-video-rec-dir-hour 15
            :cur-video-rec-dir-minute 1
            :cur-video-rec-dir-month 8
            :cur-video-rec-dir-second 32
            :cur-video-rec-dir-year 2025
            :disk-space 95
            :gpu-load 42.0
            :gpu-temperature 42.0
            :loc :jon-gui-data-system-localization-en
            :low-disk-space true
            :power-consumption 42.0
            :rec-enabled true}
   :time {:manual-timestamp 1754665407
          :timestamp 1754665407}})

(deftest test-state-specs
  (init-registry!)
  
  (testing "Individual message validation"
    (testing "GPS message"
      (let [gps-data (:gps sample-state-edn)]
        (is (m/validate :state/gps gps-data)
            (str "GPS validation failed: " (me/humanize (m/explain :state/gps gps-data))))))
    
    (testing "System message"
      (let [system-data (:system sample-state-edn)]
        (is (m/validate :state/system system-data)
            (str "System validation failed: " (me/humanize (m/explain :state/system system-data))))))
    
    (testing "Compass message"
      (let [compass-data (:compass sample-state-edn)]
        (is (m/validate :state/compass compass-data)
            (str "Compass validation failed: " (me/humanize (m/explain :state/compass compass-data))))))
    
    (testing "Camera Day message"
      (let [camera-day-data (:camera-day sample-state-edn)]
        (is (m/validate :state/camera-day camera-day-data)
            (str "Camera Day validation failed: " (me/humanize (m/explain :state/camera-day camera-day-data))))))
    
    (testing "Camera Heat message"
      (let [camera-heat-data (:camera-heat sample-state-edn)]
        (is (m/validate :state/camera-heat camera-heat-data)
            (str "Camera Heat validation failed: " (me/humanize (m/explain :state/camera-heat camera-heat-data))))))
    
    (testing "LRF message"
      (let [lrf-data (:lrf sample-state-edn)]
        (is (m/validate :state/lrf lrf-data)
            (str "LRF validation failed: " (me/humanize (m/explain :state/lrf lrf-data))))))
    
    (testing "Rotary message"
      (let [rotary-data (:rotary sample-state-edn)]
        (is (m/validate :state/rotary rotary-data)
            (str "Rotary validation failed: " (me/humanize (m/explain :state/rotary rotary-data)))))))
  
  (testing "Complete State root validation"
    (is (m/validate :state/root sample-state-edn)
        (str "Root State validation failed: " 
             (me/humanize (m/explain :state/root sample-state-edn))))))

(deftest test-closed-maps
  (init-registry!)
  
  (testing "Closed maps reject extra keys"
    (let [gps-with-extra (assoc (:gps sample-state-edn) :extra-key "should fail")]
      (is (not (m/validate :state/gps gps-with-extra))
          "GPS spec should reject extra keys"))
    
    (let [system-with-typo (-> (:system sample-state-edn)
                               (dissoc :cpu-load)
                               (assoc :cpu_load 42.0))]  ; underscore instead of dash
      (is (not (m/validate :state/system system-with-typo))
          "System spec should reject field name typos"))))

(deftest test-generators
  (init-registry!)
  
  (testing "Can generate valid GPS data"
    (let [generated (mg/generate :state/gps)]
      (is (m/validate :state/gps generated)
          (str "Generated GPS invalid: " generated))))
  
  (testing "Can generate valid System data"
    (let [generated (mg/generate :state/system)]
      (is (m/validate :state/system generated)
          (str "Generated System invalid: " generated))))
  
  (testing "Generated GPS coordinates are within buf.validate constraints"
    (dotimes [_ 100]
      (let [generated (mg/generate :state/gps)]
        (is (<= -90 (:latitude generated) 90)
            "Latitude out of range")
        (is (<= -180 (:longitude generated) 180)
            "Longitude out of range")
        (is (<= -433 (:altitude generated) 8848.86)
            "Altitude out of range")))))