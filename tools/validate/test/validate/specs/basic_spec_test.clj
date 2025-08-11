(ns validate.specs.basic-spec-test
  "Basic tests for spec validation without complex property-based testing.
   Start simple to ensure everything is working."
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [potatoclient.specs.common]))

;; Initialize the global registry
(defn init-registry! []
  (registry/setup-global-registry!
    (oneof-edn/register_ONeof-edn-schema!)))

(init-registry!)

;; ============================================================================
;; BASIC SPEC TESTS
;; ============================================================================

(deftest test-common-specs
  (testing "Common specs are registered and work"
    (testing "Protocol version spec"
      (is (m/validate :proto/protocol-version 1))
      (is (m/validate :proto/protocol-version 100))
      (is (not (m/validate :proto/protocol-version 0)))
      (is (not (m/validate :proto/protocol-version -1))))
    
    (testing "Client type spec"
      (is (m/validate :proto/client-type :jon-gui-data-client-type-local-network))
      (is (m/validate :proto/client-type :jon-gui-data-client-type-internal-cv))
      (is (not (m/validate :proto/client-type :jon-gui-data-client-type-unspecified))))
    
    (testing "GPS position specs"
      (is (m/validate :position/latitude 0.0))
      (is (m/validate :position/latitude 90.0))
      (is (m/validate :position/latitude -90.0))
      (is (not (m/validate :position/latitude 91.0)))
      (is (not (m/validate :position/latitude -91.0)))
      
      (is (m/validate :position/longitude 0.0))
      (is (m/validate :position/longitude 180.0))
      (is (m/validate :position/longitude -180.0))
      (is (not (m/validate :position/longitude 181.0)))
      (is (not (m/validate :position/longitude -181.0)))
      
      (is (m/validate :position/altitude 0.0))
      (is (m/validate :position/altitude 8848.86))
      (is (m/validate :position/altitude -433.0))
      (is (not (m/validate :position/altitude 9000.0)))
      (is (not (m/validate :position/altitude -500.0))))
    
    (testing "Rotary speed spec"
      (is (m/validate :rotary/speed 0.001))
      (is (m/validate :rotary/speed 0.5))
      (is (m/validate :rotary/speed 1.0))
      (is (not (m/validate :rotary/speed 0.0)))
      (is (not (m/validate :rotary/speed 1.001)))
      (is (not (m/validate :rotary/speed -0.1))))))

(deftest test-basic-generation
  (testing "Basic spec generation works"
    (testing "Can generate protocol version"
      (let [generated (mg/generate :proto/protocol-version)]
        (is (number? generated))
        (is (pos? generated))
        (is (m/validate :proto/protocol-version generated))))
    
    (testing "Can generate client type"
      (let [generated (mg/generate :proto/client-type)]
        (is (keyword? generated))
        (is (m/validate :proto/client-type generated))
        (is (not= :jon-gui-data-client-type-unspecified generated))))
    
    (testing "Can generate GPS coordinates"
      (dotimes [_ 10]
        (let [lat (mg/generate :position/latitude)
              lon (mg/generate :position/longitude)
              alt (mg/generate :position/altitude)]
          (is (<= -90 lat 90))
          (is (<= -180 lon 180))
          (is (<= -433 alt 8848.86))
          (is (m/validate :position/latitude lat))
          (is (m/validate :position/longitude lon))
          (is (m/validate :position/altitude alt)))))
    
    (testing "Can generate rotary speeds"
      (dotimes [_ 10]
        (let [speed (mg/generate :rotary/speed)]
          (is (< 0 speed))
          (is (<= speed 1.0))
          (is (m/validate :rotary/speed speed)))))))

(deftest test-enum-specs
  (testing "Enum specs work correctly"
    (testing "GPS fix type enum"
      (is (m/validate :enum/gps-fix-type :jon-gui-data-gps-fix-type-3d))
      (is (m/validate :enum/gps-fix-type :jon-gui-data-gps-fix-type-manual))
      (is (not (m/validate :enum/gps-fix-type :invalid-value))))
    
    (testing "Rotary mode enum"
      (is (m/validate :enum/rotary-mode :jon-gui-data-rotary-mode-position))
      (is (m/validate :enum/rotary-mode :jon-gui-data-rotary-mode-stabilization))
      (is (not (m/validate :enum/rotary-mode :jon-gui-data-rotary-mode-unspecified))))))

(deftest test-composite-specs
  (testing "Composite specs work"
    (testing "GPS position composite"
      (let [valid-pos {:latitude 45.5
                       :longitude -122.6
                       :altitude 100.0}]
        (is (m/validate :composite/gps-position valid-pos)))
      
      (let [invalid-pos {:latitude 91.0  ; Out of range
                         :longitude -122.6
                         :altitude 100.0}]
        (is (not (m/validate :composite/gps-position invalid-pos)))))
    
    (testing "Compass orientation composite"
      (let [valid-orientation {:azimuth 180.0
                               :elevation 45.0
                               :bank 0.0}]
        (is (m/validate :composite/compass-orientation valid-orientation)))
      
      (let [invalid-orientation {:azimuth 361.0  ; Out of range
                                 :elevation 45.0
                                 :bank 0.0}]
        (is (not (m/validate :composite/compass-orientation invalid-orientation)))))))

(deftest test-closed-maps
  (testing "Closed maps reject extra keys"
    (testing "GPS position with extra key"
      (let [pos-with-extra {:latitude 45.5
                            :longitude -122.6
                            :altitude 100.0
                            :extra-key "should fail"}]
        (is (not (m/validate :composite/gps-position pos-with-extra))
            "Closed map should reject extra keys")))
    
    (testing "GPS position with typo"
      (let [pos-with-typo {:latitued 45.5  ; Typo: latitued instead of latitude
                           :longitude -122.6
                           :altitude 100.0}]
        (is (not (m/validate :composite/gps-position pos-with-typo))
            "Closed map should reject typos in field names")))))

(deftest test-spec-generation-constraints
  (testing "Generated values respect constraints"
    (testing "Protocol version is always positive"
      (dotimes [_ 50]
        (let [pv (mg/generate :proto/protocol-version)]
          (is (pos? pv) "Protocol version must be > 0"))))
    
    (testing "Client type never generates UNSPECIFIED"
      (let [samples (repeatedly 50 #(mg/generate :proto/client-type))
            has-unspecified? (some #(= :jon-gui-data-client-type-unspecified %) samples)]
        (is (not has-unspecified?) "Should never generate UNSPECIFIED client type")))
    
    (testing "Rotary speed is always positive and <= 1"
      (dotimes [_ 50]
        (let [speed (mg/generate :rotary/speed)]
          (is (> speed 0) "Speed must be > 0")
          (is (<= speed 1.0) "Speed must be <= 1"))))))