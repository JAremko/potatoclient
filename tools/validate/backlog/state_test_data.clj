(ns validate.state-test-data
  "Real state data from state-explorer for testing"
  (:require [pronto.core :as pronto]))

;; Real state data captured from state-explorer
(def sample-state-edn
  {:ser.JonSharedData$JonGUIState
   {:actual-space-time {:ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
                        {:altitude 0.289371
                         :azimuth 256.62
                         :elevation 7.04
                         :latitude 50.023632
                         :longitude 15.81521
                         :timestamp 1754664759}}
    :camera-day {:ser.JonSharedDataCameraDay$JonGuiDataCameraDay
                 {:clahe-level 0.16
                  :digital-zoom-level 1.0
                  :focus-pos 1.0
                  :fx-mode :jon-gui-data-fx-mode-day-a
                  :infrared-filter true
                  :iris-pos 0.03
                  :zoom-pos 0.59938735
                  :zoom-table-pos 3
                  :zoom-table-pos-max 4}}
    :camera-heat {:ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat
                  {:agc-mode :jon-gui-data-video-channel-heat-agc-mode-2
                   :clahe-level 0.5
                   :digital-zoom-level 1.0
                   :filter :jon-gui-data-video-channel-heat-filter-hot-white
                   :fx-mode :jon-gui-data-fx-mode-heat-a
                   :zoom-table-pos 3
                   :zoom-table-pos-max 4}}
    :compass {:ser.JonSharedDataCompass$JonGuiDataCompass
              {:azimuth 333.50625
               :bank 0.84375
               :elevation 3.54375}}
    :compass-calibration {:ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
                          {:final-stage 12
                           :status :jon-gui-data-compass-calibrate-status-not-calibrating
                           :target-azimuth 56.25
                           :target-bank -5.625
                           :target-elevation 6.75}}
    :day-cam-glass-heater {:ser.JonSharedDataGlassHeater$JonGuiDataGlassHeater {}}
    :gps {:ser.JonSharedDataGps$JonGuiDataGps
          {:altitude 0.289371
           :fix-type :jon-gui-data-gps-fix-type-3d
           :latitude 50.023632
           :longitude 15.815209999999999
           :manual-latitude 50.023604
           :manual-longitude 15.815316}}
    :lrf {:ser.JonSharedDataLRF$JonGuiDataLRF
          {:measure-id 52
           :pointer-mode :jon-gui-data-lrf-laser-pointer-mode-off
           :target {:ser.JonSharedDataLRF$JonGuiDataLRFTarget
                    {:observer-azimuth 356.40000000000003
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
                     :uuid-part4 879344611}}}}
    :meteo-internal {:ser.JonSharedDataMeteoInternal$JonGuiDataMeteoInternal {}}
    :protocol-version 1
    :rec-osd {:ser.JonSharedDataRecOSD$JonGuiDataRecOSD
              {:day-osd-enabled true
               :heat-osd-enabled true
               :screen :jon-gui-data-rec-osd-screen-main}}
    :rotary {:ser.JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatform
             {:azimuth 333.50626
              :current-scan-node {:ser.JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatformScanNode
                                  {:azimuth 0.001
                                   :dayzoomtablevalue 1
                                   :elevation 0.001
                                   :heatzoomtablevalue 1
                                   :index 1
                                   :linger 0.001
                                   :speed 0.001}}
              :mode :jon-gui-data-rotary-mode-position
              :platform-azimuth 256.62
              :platform-elevation 7.04
              :scan-target 1
              :scan-target-max 1}}
    :system {:ser.JonSharedDataSystem$JonGuiDataSystem
             {:cpu-load 42.0
              :cpu-temperature 42.0
              :cur-video-rec-dir-day 8
              :cur-video-rec-dir-hour 14
              :cur-video-rec-dir-minute 51
              :cur-video-rec-dir-month 8
              :cur-video-rec-dir-second 32
              :cur-video-rec-dir-year 2025
              :disk-space 95
              :gpu-load 42.0
              :gpu-temperature 42.0
              :loc :jon-gui-data-system-localization-en
              :low-disk-space true
              :power-consumption 42.0
              :rec-enabled true}}
    :time {:ser.JonSharedDataTime$JonGuiDataTime
           {:manual-timestamp 1754664759
            :timestamp 1754664759}}}})

;; Minimal valid state with all required fields
(def minimal-state-edn
  {:ser.JonSharedData$JonGUIState
   {:protocol-version 1
    :time {:ser.JonSharedDataTime$JonGuiDataTime
           {:timestamp 1234567890
            :manual-timestamp 0
            :zone-id 0
            :use-manual-time false}}
    :system {:ser.JonSharedDataSystem$JonGuiDataSystem
             {:cpu-temp 50.0
              :cpu-load 25.0
              :memory-used 1000000
              :memory-total 2000000}}
    :gps {:ser.JonSharedDataGps$JonGuiDataGps
          {:latitude 0.0
           :longitude 0.0
           :altitude 0.0}}
    :compass {:ser.JonSharedDataCompass$JonGuiDataCompass
              {:heading 0.0
               :pitch 0.0
               :roll 0.0}}
    :rotary {:ser.JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatform
             {:azimuth 0.0
              :elevation 0.0}}
    :camera-day {:ser.JonSharedDataCameraDay$JonGuiDataCameraDay
                 {:zoom 1.0
                  :focus 100.0}}
    :camera-heat {:ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat
                  {:zoom 1.0
                   :palette 0}}
    :day-cam-glass-heater {:ser.JonSharedDataGlassHeater$JonGuiDataGlassHeater
                           {:power-on false
                            :temperature 20.0}}
    :meteo-internal {:ser.JonSharedDataMeteoInternal$JonGuiDataMeteoInternal
                     {:temperature 25.0
                      :pressure 1013.25
                      :humidity 50.0}}
    :lrf {:ser.JonSharedDataLRF$JonGuiDataLRF
          {:distance 0.0
           :measuring false}}
    :compass-calibration {:ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
                          {:calibrated false
                           :progress 0}}
    :rec-osd {:ser.JonSharedDataRecOSD$JonGuiDataRecOSD
              {:recording false
               :osd-enabled true}}
    :actual-space-time {:ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime
                        {:utc-timestamp-ms 1234567890000
                         :time-zone-offset-ms 0}}}})

(defn create-state-from-edn
  "Create a protobuf message from EDN using Pronto"
  [edn-map]
  ;; Need to set up Pronto mapper first
  (eval `(pronto/defmapper state-mapper [ser.JonSharedData$JonGUIState]))
  (let [mapper (eval 'state-mapper)]
    (pronto/clj-map->proto mapper edn-map)))

(defn get-sample-state-bytes
  "Get byte array of sample state message"
  []
  (-> sample-state-edn
      create-state-from-edn
      (.toByteArray)))

(defn get-minimal-state-bytes
  "Get byte array of minimal valid state message"
  []
  (-> minimal-state-edn
      create-state-from-edn
      (.toByteArray)))