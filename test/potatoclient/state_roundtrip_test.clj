(ns potatoclient.state-roundtrip-test
  "Tests for state message roundtrip: Protobuf → Transit → Clojure
  
  This tests the reverse flow from commands:
  - Server sends protobuf state
  - Kotlin converts to Transit using GeneratedStateHandlers
  - Clojure receives and updates app-db"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.transit.app-db :as app-db]
            [potatoclient.logging :as logging]
            [malli.generator :as mg])
  (:import [ser JonSharedData JonSharedData$JonGUIState
                JonSharedDataTypes$JonGuiDataMeteo
                JonSharedDataCompass$JonGuiDataCompass
                JonSharedDataCameraDay$JonGuiDataCameraDay
                JonSharedDataGps$JonGuiDataGps
                JonSharedDataCameraHeat$JonGuiDataCameraHeat
                JonSharedDataLrf$JonGuiDataLrf
                JonSharedDataRotary$JonGuiDataRotary
                JonSharedDataSystem$JonGuiDataSystem]
           [potatoclient.kotlin.transit.generated GeneratedStateHandlers]))

;; Reset app-db between tests
(use-fixtures :each (fn [f]
                      (app-db/reset-to-initial-state!)
                      (f)))

(deftest test-basic-state-extraction
  (testing "Can extract Transit data from simple state message"
    (let [;; Build a minimal state message
          state-builder (JonSharedData$JonGUIState/newBuilder)
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      (is (instance? java.util.Map transit-data) "Transit data should be a Map")
      ;; Has-data field is not extracted in the generated handlers
      ;; Only subsystem data is extracted
      (is (empty? transit-data)))))

(deftest test-meteo-state-extraction
  (testing "Meteo (weather) state extraction with fields"
    (let [;; Build meteo state
          meteo-builder (JonSharedDataTypes$JonGuiDataMeteo/newBuilder)
          _ (.setTemperature meteo-builder 25.5)
          _ (.setHumidity meteo-builder 60.0)
          _ (.setPressure meteo-builder 1013.25)
          
          ;; Build main state with meteo
          state-builder (JonSharedData$JonGUIState/newBuilder)
          _ (.setMeteoInternal state-builder meteo-builder)
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      (is (contains? transit-data "meteo-internal"))
      (let [meteo (get transit-data "meteo-internal")]
        (is (= 25.5 (get meteo "temperature")))
        (is (= 60.0 (get meteo "humidity")))
        (is (= 1013.25 (get meteo "pressure")))))))

(deftest test-gps-state-extraction
  (testing "GPS state with location data"
    (let [;; Build GPS state
          gps-builder (JonSharedDataGps$JonGuiDataGps/newBuilder)
          _ (.setLatitude gps-builder 37.7749)
          _ (.setLongitude gps-builder -122.4194)
          _ (.setAltitude gps-builder 52.0)
          
          ;; Build main state
          state-builder (JonSharedData$JonGUIState/newBuilder)
          _ (.setGps state-builder gps-builder)
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      (is (contains? transit-data "gps"))
      (let [gps (get transit-data "gps")]
        (is (= 37.7749 (get gps "latitude")))
        (is (= -122.4194 (get gps "longitude")))
        (is (= 52.0 (get gps "altitude")))))))

(deftest test-rotary-platform-state
  (testing "Rotary platform state with angles and speeds"
    (let [;; Build rotary state
          rotary-builder (JonSharedDataRotary$JonGuiDataRotary/newBuilder)
          _ (.setAzimuth rotary-builder 45.0)
          _ (.setElevation rotary-builder -10.0)
          _ (.setAzimuthSpeed rotary-builder 2.5)
          _ (.setElevationSpeed rotary-builder 0.0)
          _ (.setIsMoving rotary-builder true)
          
          ;; Build main state
          state-builder (JonSharedData$JonGUIState/newBuilder)
          _ (.setRotary state-builder rotary-builder)
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      (is (contains? transit-data "rotary"))
      (let [rotary (get transit-data "rotary")]
        (is (= 45.0 (get rotary "azimuth")))
        (is (= -10.0 (get rotary "elevation")))
        (is (= 2.5 (get rotary "azimuth-speed")))
        (is (= true (get rotary "is-moving")))))))

(deftest test-heat-camera-state
  (testing "Heat camera state with settings"
    (let [;; Build heat camera state
          heat-builder (JonSharedDataCameraHeat$JonGuiDataCameraHeat/newBuilder)
          _ (.setZoomTablePos heat-builder 2)
          _ (.setDigitalZoomLevel heat-builder 1.5)
          _ (.setAutoFocus heat-builder true)
          _ (.setDdeEnabled heat-builder true)
          
          ;; Build main state
          state-builder (JonSharedData$JonGUIState/newBuilder)
          _ (.setCameraHeat state-builder heat-builder)
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      (is (contains? transit-data "camera-heat"))
      (let [heat (get transit-data "camera-heat")]
        (is (= 2 (get heat "zoom-table-pos")))
        (is (= 1.5 (get heat "digital-zoom-level")))
        (is (= true (get heat "auto-focus")))
        (is (= true (get heat "dde-enabled")))))))

(deftest test-full-state-roundtrip
  (testing "Complete state with multiple subsystems"
    (let [;; Build a comprehensive state message
          state-builder (JonSharedData$JonGUIState/newBuilder)
          
          ;; Add weather/meteo
          meteo-builder (JonSharedDataTypes$JonGuiDataMeteo/newBuilder)
          _ (.setTemperature meteo-builder 20.0)
          _ (.setHumidity meteo-builder 50.0)
          _ (.setMeteoInternal state-builder meteo-builder)
          
          ;; Add GPS
          gps-builder (JonSharedDataGps$JonGuiDataGps/newBuilder)
          _ (.setLatitude gps-builder 40.7128)
          _ (.setLongitude gps-builder -74.0060)
          _ (.setGps state-builder gps-builder)
          
          ;; Add rotary
          rotary-builder (JonSharedDataRotary$JonGuiDataRotary/newBuilder)
          _ (.setAzimuth rotary-builder 180.0)
          _ (.setElevation rotary-builder 0.0)
          _ (.setRotary state-builder rotary-builder)
          
          ;; Add system info
          system-builder (JonSharedDataSystem$JonGuiDataSystem/newBuilder)
          _ (.setRecEnabled system-builder true)
          _ (.setTracking system-builder false)
          _ (.setSystem state-builder system-builder)
          
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      ;; Verify all subsystems are present
      (is (contains? transit-data "meteo-internal"))
      (is (contains? transit-data "gps"))
      (is (contains? transit-data "rotary"))
      (is (contains? transit-data "system"))
      
      ;; Verify some nested values
      (is (= 20.0 (get-in transit-data ["meteo-internal" "temperature"])))
      (is (= 40.7128 (get-in transit-data ["gps" "latitude"])))
      (is (= 180.0 (get-in transit-data ["rotary" "azimuth"])))
      (is (= true (get-in transit-data ["system" "rec-enabled"])))
      (is (= false (get-in transit-data ["system" "tracking"]))))))

(deftest test-empty-state-handling
  (testing "Empty state message handling"
    (let [;; Build empty state
          state-builder (JonSharedData$JonGUIState/newBuilder)
          state-proto (.build state-builder)
          
          ;; Extract Transit data
          transit-data (.extractState GeneratedStateHandlers/INSTANCE state-proto)]
      
      (is (instance? java.util.Map transit-data) "Transit data should be a Map")
      ;; Should not have any subsystems
      (is (empty? transit-data)))))