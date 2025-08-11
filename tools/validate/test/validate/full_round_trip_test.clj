(ns validate.full-round-trip-test
  "Full round-trip validation tests with deep equality checks.
   Ensures that what goes in equals what comes out."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [pronto.core :as p]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.root]
   [potatoclient.specs.cmd.root])
  (:import 
   [ser JonSharedData$JonGUIState]
   [cmd JonSharedCmd$Root]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register-oneof-edn-schema!))

;; ============================================================================
;; SANITY CHECK - COMMON SPECS
;; ============================================================================

(deftest test-common-specs-have-generators
  (testing "All common specs have working generators"
    
    (testing "GPS coordinate specs generate valid values"
      (dotimes [_ 10]
        (let [lat (mg/generate :position/latitude)
              lon (mg/generate :position/longitude)
              alt (mg/generate :position/altitude)]
          (is (and (>= lat -90.0) (<= lat 90.0))
              (str "Latitude should be in range: " lat))
          (is (and (>= lon -180.0) (<= lon 180.0))
              (str "Longitude should be in range: " lon))
          (is (and (>= alt -433.0) (<= alt 8848.86))
              (str "Altitude should be in range: " alt)))))
    
    (testing "Angle specs generate valid values"
      (dotimes [_ 10]
        (let [azimuth (mg/generate :angle/azimuth)
              elevation (mg/generate :angle/elevation)
              bank (mg/generate :angle/bank)]
          (is (and (>= azimuth 0.0) (<= azimuth 360.0))
              (str "Azimuth should be in range: " azimuth))
          (is (and (>= elevation -90.0) (<= elevation 90.0))
              (str "Elevation should be in range: " elevation))
          (is (and (>= bank -180.0) (<= bank 180.0))
              (str "Bank should be in range: " bank)))))
    
    (testing "Rotary speed spec generates valid values"
      (dotimes [_ 10]
        (let [speed (mg/generate :rotary/speed)]
          (is (and (> speed 0.0) (<= speed 1.0))
              (str "Rotary speed should be > 0 and <= 1: " speed)))))
    
    (testing "Protocol version spec generates valid values"
      (dotimes [_ 10]
        (let [version (mg/generate :proto/protocol-version)]
          (is (> version 0)
              (str "Protocol version should be > 0: " version)))))))

;; ============================================================================
;; SIMPLE ROUND-TRIP TEST
;; ============================================================================

(deftest test-simple-round-trips
  (testing "Simple messages round-trip correctly"
    
    (testing "Ping command round-trip"
      (let [ping-proto (p/proto-map h/cmd-mapper JonSharedCmd$Root
                                   :protocol_version 1
                                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                   :ping {})
            binary (p/proto-map->bytes ping-proto)
            parsed (p/bytes->proto-map h/cmd-mapper JonSharedCmd$Root binary)
            result (into {} parsed)]
        
        (is (= 1 (:protocol_version result))
            "Protocol version should be preserved")
        (is (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type result))
            "Client type should be preserved")
        (is (= {} (:ping result))
            "Ping payload should be preserved")
        (is (nil? (:noop result))
            "Other commands should be nil")))
    
    (testing "GPS command round-trip"
      (let [gps-proto (p/proto-map h/cmd-mapper JonSharedCmd$Root
                                  :protocol_version 1
                                  :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                  :gps {:set-manual-gps {:latitude 45.5
                                                        :longitude -122.6
                                                        :altitude 100.0}})
            binary (p/proto-map->bytes gps-proto)
            parsed (p/bytes->proto-map h/cmd-mapper JonSharedCmd$Root binary)
            result (into {} parsed)]
        
        (is (= 45.5 (get-in result [:gps :set-manual-gps :latitude]))
            "GPS latitude should be preserved")
        (is (= -122.6 (get-in result [:gps :set-manual-gps :longitude]))
            "GPS longitude should be preserved")
        (is (= 100.0 (get-in result [:gps :set-manual-gps :altitude]))
            "GPS altitude should be preserved")))))

;; ============================================================================
;; NESTED STATE ROUND-TRIP
;; ============================================================================

(deftest test-nested-state-round-trip
  (testing "Deeply nested state preserves all data"
    (let [;; Start with valid state
          state (h/valid-state)
          ;; Update some nested values
          updated (p/p-> state
                        (assoc :protocol_version 42)
                        (assoc-in [:gps :latitude] 37.7749)
                        (assoc-in [:gps :longitude] -122.4194)
                        (assoc-in [:system :cpu_load] 85.5)
                        (assoc-in [:rotary :current_scan_node :speed] 0.75))
          ;; Round-trip
          binary (p/proto-map->bytes updated)
          parsed (p/bytes->proto-map h/state-mapper JonSharedData$JonGUIState binary)
          result (into {} parsed)]
      
      (is (= 42 (:protocol_version result))
          "Protocol version should be preserved")
      (is (= 37.7749 (get-in result [:gps :latitude]))
          "GPS latitude should be preserved")
      (is (= -122.4194 (get-in result [:gps :longitude]))
          "GPS longitude should be preserved")
      (is (= 85.5 (get-in result [:system :cpu_load]))
          "System CPU load should be preserved")
      (is (= 0.75 (get-in result [:rotary :current_scan_node :speed]))
          "Nested scan node speed should be preserved"))))

;; ============================================================================
;; NEGATIVE TESTS
;; ============================================================================

(deftest test-invalid-data-handling
  (testing "Invalid data is rejected by Malli specs"
    
    (testing "GPS with invalid coordinates"
      (is (not (m/validate :position/latitude 91.0))
          "Latitude > 90 should not validate")
      (is (not (m/validate :position/longitude 181.0))
          "Longitude > 180 should not validate")
      (is (not (m/validate :position/altitude 9000.0))
          "Altitude > 8848.86 should not validate"))
    
    (testing "Invalid rotary speed"
      (is (not (m/validate :rotary/speed 0.0))
          "Speed 0 should not validate")
      (is (not (m/validate :rotary/speed 1.001))
          "Speed > 1 should not validate")
      (is (not (m/validate :rotary/speed -0.1))
          "Negative speed should not validate"))
    
    (testing "Invalid protocol version"
      (is (not (m/validate :proto/protocol-version 0))
          "Protocol version 0 should not validate")
      (is (not (m/validate :proto/protocol-version -1))
          "Negative protocol version should not validate"))))

;; ============================================================================
;; BUF.VALIDATE INTEGRATION
;; ============================================================================

(deftest test-buf-validate-integration
  (testing "Round-tripped data passes buf.validate"
    
    (testing "Valid state passes buf.validate"
      (let [state (h/valid-state)
            binary (p/proto-map->bytes state)
            validation (v/validate-binary binary :type :state)]
        (is (:valid? validation)
            (str "Valid state should pass buf.validate. "
                 "Violations: " (:violations validation)))))
    
    (testing "Valid command passes buf.validate"
      (let [cmd (p/proto-map h/cmd-mapper JonSharedCmd$Root
                            :protocol_version 1
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                            :ping {})
            binary (p/proto-map->bytes cmd)
            validation (v/validate-binary binary :type :cmd)]
        (is (:valid? validation)
            (str "Valid command should pass buf.validate. "
                 "Violations: " (:violations validation)))))
    
    (testing "Round-tripped state still validates"
      (let [state (h/valid-state)
            ;; Round-trip
            binary1 (p/proto-map->bytes state)
            parsed (p/bytes->proto-map h/state-mapper JonSharedData$JonGUIState binary1)
            ;; Convert back to binary
            binary2 (p/proto-map->bytes parsed)
            validation (v/validate-binary binary2 :type :state)]
        (is (:valid? validation)
            (str "Round-tripped state should still pass buf.validate. "
                 "Violations: " (:violations validation)))
        (is (= (seq binary1) (seq binary2))
            "Binary should be identical after round-trip")))))

;; ============================================================================
;; GENERATED DATA ROUND-TRIPS
;; ============================================================================

(deftest test-generated-data-round-trips
  (testing "Malli-generated data round-trips successfully"
    
    (testing "Generated GPS data"
      (dotimes [_ 5]
        (let [lat (mg/generate :position/latitude)
              lon (mg/generate :position/longitude)
              alt (mg/generate :position/altitude)
              ;; Create proto with generated values
              gps-proto (p/proto-map h/state-mapper 
                                    ser.JonSharedDataGps$JonGuiDataGps
                                    :latitude lat
                                    :longitude lon
                                    :altitude alt
                                    :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                    :manual_latitude 0.0
                                    :manual_longitude 0.0)
              ;; Round-trip
              binary (p/proto-map->bytes gps-proto)
              parsed (p/bytes->proto-map h/state-mapper 
                                        ser.JonSharedDataGps$JonGuiDataGps 
                                        binary)
              result (into {} parsed)]
          
          ;; Check values are preserved (with floating point tolerance)
          (is (< (Math/abs (- lat (:latitude result))) 0.000001)
              (str "Latitude should be preserved: " lat " vs " (:latitude result)))
          (is (< (Math/abs (- lon (:longitude result))) 0.000001)
              (str "Longitude should be preserved: " lon " vs " (:longitude result)))
          (is (< (Math/abs (- alt (:altitude result))) 0.01)
              (str "Altitude should be preserved: " alt " vs " (:altitude result))))))
    
    (testing "Generated angle data"
      (dotimes [_ 5]
        (let [azimuth (mg/generate :angle/azimuth)
              elevation (mg/generate :angle/elevation)
              bank (mg/generate :angle/bank)
              ;; Create compass proto with generated values
              compass-proto (p/proto-map h/state-mapper 
                                        ser.JonSharedDataCompass$JonGuiDataCompass
                                        :azimuth azimuth
                                        :elevation elevation
                                        :bank bank)
              ;; Round-trip
              binary (p/proto-map->bytes compass-proto)
              parsed (p/bytes->proto-map h/state-mapper 
                                        ser.JonSharedDataCompass$JonGuiDataCompass 
                                        binary)
              result (into {} parsed)]
          
          (is (< (Math/abs (- azimuth (:azimuth result))) 0.000001)
              (str "Azimuth should be preserved: " azimuth " vs " (:azimuth result)))
          (is (< (Math/abs (- elevation (:elevation result))) 0.000001)
              (str "Elevation should be preserved: " elevation " vs " (:elevation result)))
          (is (< (Math/abs (- bank (:bank result))) 0.000001)
              (str "Bank should be preserved: " bank " vs " (:bank result))))))))