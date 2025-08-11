(ns validate.specs.all-specs-test
  "Comprehensive tests for all State and Command specs.
   Validates that all specs generate valid data that passes buf.validate."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.test-validator :as tv]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [pronto.core :as p]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.gps]
   [potatoclient.specs.state.system]
   [potatoclient.specs.state.compass]
   [potatoclient.specs.state.rotary]
   [potatoclient.specs.state.camera-day]
   [potatoclient.specs.state.camera-heat]
   [potatoclient.specs.state.time]
   [potatoclient.specs.state.lrf]
   [potatoclient.specs.state.rec-osd]
   [potatoclient.specs.state.actual-space-time]
   [potatoclient.specs.state.day-cam-glass-heater]
   [potatoclient.specs.state.compass-calibration]
   [potatoclient.specs.state.meteo-internal]
   [potatoclient.specs.state.root]
   [potatoclient.specs.cmd.common]
   [potatoclient.specs.cmd.compass]
   [potatoclient.specs.cmd.cv]
   [potatoclient.specs.cmd.day-camera]
   [potatoclient.specs.cmd.day-cam-glass-heater]
   [potatoclient.specs.cmd.gps]
   [potatoclient.specs.cmd.heat-camera]
   [potatoclient.specs.cmd.lira]
   [potatoclient.specs.cmd.lrf]
   [potatoclient.specs.cmd.lrf-align]
   [potatoclient.specs.cmd.osd]
   [potatoclient.specs.cmd.rotary]
   [potatoclient.specs.cmd.system]
   [potatoclient.specs.cmd.root])
  (:import 
   [ser JonSharedData$JonGUIState 
    JonSharedDataGps$JonGuiDataGps
    JonSharedDataSystem$JonGuiDataSystem
    JonSharedDataCompass$JonGuiDataCompass
    JonSharedDataRotary$JonGuiDataRotary
    JonSharedDataCameraDay$JonGuiDataCameraDay
    JonSharedDataCameraHeat$JonGuiDataCameraHeat
    JonSharedDataTime$JonGuiDataTime
    JonSharedDataLrf$JonGuiDataLrf
    JonSharedDataRecOsd$JonGuiDataRecOsd]
   [cmd JonSharedCmd$Root]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; ============================================================================
;; STATE MESSAGE TESTS
;; ============================================================================

(deftest test-all-state-specs
  (testing "All State message specs generate valid data"
    
    (testing "GPS spec generates valid data"
      (dotimes [_ 10]
        (let [gps-data (mg/generate :state/gps)]
          (is (m/validate :state/gps gps-data)
              "Generated GPS data should validate against spec"))))
    
    (testing "System spec generates valid data"
      (dotimes [_ 10]
        (let [sys-data (mg/generate :state/system)]
          (is (m/validate :state/system sys-data)
              "Generated System data should validate against spec"))))
    
    (testing "Compass spec generates valid data"
      (dotimes [_ 10]
        (let [compass-data (mg/generate :state/compass)]
          (is (m/validate :state/compass compass-data)
              "Generated Compass data should validate against spec"))))
    
    (testing "Rotary spec generates valid data"
      (dotimes [_ 10]
        (let [rotary-data (mg/generate :state/rotary)]
          (is (m/validate :state/rotary rotary-data)
              "Generated Rotary data should validate against spec"))))
    
    (testing "Time spec generates valid data"
      (dotimes [_ 10]
        (let [time-data (mg/generate :state/time)]
          (is (m/validate :state/time time-data)
              "Generated Time data should validate against spec"))))
    
    (testing "Complete State root generates valid data"
      (dotimes [_ 5]
        (let [state-data (mg/generate :state/root)]
          (is (m/validate :state/root state-data)
              "Generated State root should validate against spec")
          (is (> (:protocol-version state-data) 0)
              "Protocol version should be > 0"))))))

;; ============================================================================
;; COMMAND MESSAGE TESTS
;; ============================================================================

(deftest test-all-cmd-specs
  (testing "All Command message specs generate valid data"
    
    (testing "Simple commands validate"
      (let [ping-cmd {:protocol-version 1
                     :client-type :jon-gui-data-client-type-local-network
                     :cmd {:ping {}}}
            noop-cmd {:protocol-version 1
                     :client-type :jon-gui-data-client-type-internal-cv
                     :cmd {:noop {}}}
            frozen-cmd {:protocol-version 1
                       :client-type :jon-gui-data-client-type-lira
                       :cmd {:frozen {:flag true}}}]
        
        (is (m/validate :cmd/root ping-cmd) "Ping command should validate")
        (is (m/validate :cmd/root noop-cmd) "Noop command should validate")
        (is (m/validate :cmd/root frozen-cmd) "Frozen command should validate")))
    
    (testing "GPS commands validate with constraints"
      (let [valid-gps {:protocol-version 1
                      :client-type :jon-gui-data-client-type-local-network
                      :cmd {:gps {:set-manual-gps {:latitude 45.5
                                                   :longitude -122.6
                                                   :altitude 100.0}}}}
            invalid-gps {:protocol-version 1
                        :client-type :jon-gui-data-client-type-local-network
                        :cmd {:gps {:set-manual-gps {:latitude 91.0  ; Invalid
                                                     :longitude -122.6
                                                     :altitude 100.0}}}}]
        
        (is (m/validate :cmd/root valid-gps)
            "GPS command with valid coordinates should validate")
        (is (not (m/validate :cmd/root invalid-gps))
            "GPS command with invalid latitude should not validate")))
    
    (testing "Rotary commands validate with speed constraints"
      (let [valid-rotary {:protocol-version 1
                         :client-type :jon-gui-data-client-type-local-network
                         :cmd {:rotary {:rotate-azimuth {:direction :jon-gui-rotary-direction-left
                                                         :speed 0.5}}}}
            invalid-rotary {:protocol-version 1
                           :client-type :jon-gui-data-client-type-local-network
                           :cmd {:rotary {:rotate-azimuth {:direction :jon-gui-rotary-direction-left
                                                           :speed 0.0}}}}]  ; Invalid speed
        
        (is (m/validate :cmd/root valid-rotary)
            "Rotary command with valid speed should validate")
        (is (not (m/validate :cmd/root invalid-rotary))
            "Rotary command with speed 0 should not validate")))
    
    (testing "Protocol version constraint"
      (let [invalid-cmd {:protocol-version 0  ; Invalid
                        :client-type :jon-gui-data-client-type-local-network
                        :cmd {:ping {}}}
            valid-cmd {:protocol-version 1
                      :client-type :jon-gui-data-client-type-local-network
                      :cmd {:ping {}}}]
        
        (is (not (m/validate :cmd/root invalid-cmd))
            "Command with protocol-version 0 should not validate")
        (is (m/validate :cmd/root valid-cmd)
            "Command with protocol-version 1 should validate")))
    
    (testing "Oneof exclusivity"
      (let [one-cmd {:protocol-version 1
                    :client-type :jon-gui-data-client-type-local-network
                    :cmd {:ping {}}}
            ;; Can't actually create two commands in oneof with our spec
            no-cmd {:protocol-version 1
                   :client-type :jon-gui-data-client-type-local-network
                   :cmd {}}]
        
        (is (m/validate :cmd/root one-cmd)
            "Command with exactly one payload should validate")
        (is (not (m/validate :cmd/root no-cmd))
            "Command with no payload should not validate")))))

;; ============================================================================
;; BUF.VALIDATE INTEGRATION TESTS
;; ============================================================================

(deftest test-buf-validate-integration
  (testing "Malli-generated data passes buf.validate"
    
    (testing "GPS sub-message validates with test validator"
      (let [gps-data (mg/generate :state/gps)
            proto-map (p/proto-map h/state-mapper JonSharedDataGps$JonGuiDataGps
                                  :latitude (:latitude gps-data)
                                  :longitude (:longitude gps-data)
                                  :altitude (:altitude gps-data)
                                  :fix_type (-> gps-data :fix-type name
                                               (clojure.string/replace "-" "_")
                                               (clojure.string/upper-case)
                                               keyword)
                                  :manual_latitude (:manual-latitude gps-data)
                                  :manual_longitude (:manual-longitude gps-data)
                                  :manual_altitude (get gps-data :manual-altitude 0.0)
                                  :use_manual (get gps-data :use-manual false))
            result (tv/validate-proto-map proto-map)]
        
        (is (:valid? result)
            (str "GPS proto-map should validate. Violations: " (:violations result)))))
    
    (testing "Complete State validates with production validator"
      (let [state (h/valid-state)
            binary (p/proto-map->bytes state)
            result (v/validate-binary binary :type :state)]
        
        (is (:valid? result)
            (str "Complete state should validate. Violations: " (:violations result)))))
    
    (testing "Commands validate with production validator"
      (let [ping-proto (p/proto-map h/cmd-mapper JonSharedCmd$Root
                                   :protocol_version 1
                                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                   :ping {})
            binary (p/proto-map->bytes ping-proto)
            result (v/validate-binary binary :type :cmd)]
        
        (is (:valid? result)
            (str "Ping command should validate. Violations: " (:violations result)))))))

;; ============================================================================
;; CONSTRAINT BOUNDARY TESTS
;; ============================================================================

(deftest test-constraint-boundaries
  (testing "Boundary values for constrained fields"
    
    (testing "GPS coordinate boundaries"
      (let [boundaries [{:latitude 90.0 :longitude 180.0 :altitude 8848.86}
                       {:latitude -90.0 :longitude -180.0 :altitude -433.0}
                       {:latitude 0.0 :longitude 0.0 :altitude 0.0}]]
        (doseq [coords boundaries]
          (let [gps-data (merge (mg/generate :state/gps) coords)]
            (is (m/validate :state/gps gps-data)
                (str "GPS boundary values should validate: " coords))))))
    
    (testing "Rotary speed boundaries"
      (let [valid-speeds [0.001 0.1 0.5 0.999 1.0]
            invalid-speeds [0.0 -0.1 1.001 2.0]]
        
        (doseq [speed valid-speeds]
          (let [scan-node {:speed speed :azimuth 0.0 :elevation 0.0
                          :linger 0.5 :index 1
                          :dayzoomtablevalue 2 :heatzoomtablevalue 2}
                rotary-data (assoc (mg/generate :state/rotary)
                                  :current-scan-node scan-node)]
            (is (m/validate :state/rotary rotary-data)
                (str "Rotary with speed " speed " should validate"))))
        
        (doseq [speed invalid-speeds]
          (let [scan-node {:speed speed :azimuth 0.0 :elevation 0.0
                          :linger 0.5 :index 1
                          :dayzoomtablevalue 2 :heatzoomtablevalue 2}
                rotary-data (assoc (mg/generate :state/rotary)
                                  :current-scan-node scan-node)]
            (is (not (m/validate :state/rotary rotary-data))
                (str "Rotary with invalid speed " speed " should not validate"))))))
    
    (testing "Angle boundaries"
      (let [compass-boundaries [{:azimuth 0.0 :elevation -90.0 :bank -180.0}
                               {:azimuth 359.999 :elevation 90.0 :bank 180.0}]]
        (doseq [angles compass-boundaries]
          (let [compass-data (merge (mg/generate :state/compass) angles)]
            (is (m/validate :state/compass compass-data)
                (str "Compass angles should validate: " angles))))))))