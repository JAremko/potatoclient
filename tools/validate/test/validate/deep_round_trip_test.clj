(ns validate.deep-round-trip-test
  "Comprehensive round-trip tests with deep equality verification.
   Tests that what goes in equals what comes out, including:
   - Generated data round-trips
   - Manually created deeply nested payloads
   - Negative tests for invalid data
   - Sanity checks for edge cases"
  (:require
   [clojure.test :refer [deftest testing is]]
   [clojure.walk :as walk]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [validate.test-validator :as tv]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [pronto.core :as p]
   [clojure.tools.logging :as log]
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
    JonSharedDataRotary$JonGuiDataRotary]
   [cmd JonSharedCmd$Root]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; ============================================================================
;; DEEP EQUALITY HELPERS
;; ============================================================================

(defn normalize-for-comparison
  "Normalize EDN data for comparison by:
   - Converting keywords to consistent format
   - Handling nil vs missing keys
   - Normalizing floating point precision"
  [data]
  (walk/postwalk
   (fn [x]
     (cond
       ;; Normalize keywords (snake_case vs kebab-case)
       (keyword? x) (keyword (namespace x)
                            (clojure.string/replace (name x) "_" "-"))
       
       ;; Normalize floats to avoid precision issues
       (float? x) (Double/parseDouble (format "%.6f" x))
       (double? x) (Double/parseDouble (format "%.6f" x))
       
       ;; Remove nil values from maps (Pronto style)
       (map? x) (into {} (remove (comp nil? val) x))
       
       :else x))
   data))

(defn deep-equal?
  "Check if two data structures are deeply equal after normalization"
  [expected actual]
  (= (normalize-for-comparison expected)
     (normalize-for-comparison actual)))

(defn explain-difference
  "Explain the difference between two data structures"
  [expected actual path]
  (cond
    (and (map? expected) (map? actual))
    (let [expected-keys (set (keys expected))
          actual-keys (set (keys actual))
          missing-keys (clojure.set/difference expected-keys actual-keys)
          extra-keys (clojure.set/difference actual-keys expected-keys)
          common-keys (clojure.set/intersection expected-keys actual-keys)]
      (concat
       (when (seq missing-keys)
         [{:path path :issue :missing_keys :keys missing-keys}])
       (when (seq extra-keys)
         [{:path path :issue :extra_keys :keys extra-keys}])
       (mapcat (fn [k]
                 (when (not= (get expected k) (get actual k))
                   (explain-difference (get expected k) (get actual k)
                                     (conj path k))))
               common-keys)))
    
    (and (sequential? expected) (sequential? actual))
    (if (not= (count expected) (count actual))
      [{:path path :issue :count_mismatch 
        :expected_count (count expected)
        :actual_count (count actual)}]
      (mapcat (fn [i]
                (explain-difference (nth expected i) (nth actual i)
                                  (conj path i)))
              (range (count expected))))
    
    (not= expected actual)
    [{:path path :issue :value_mismatch :expected expected :actual actual}]
    
    :else nil))

;; ============================================================================
;; ROUND-TRIP FUNCTIONS
;; ============================================================================

(defn state-round-trip
  "Complete round-trip for State message:
   EDN -> Proto-map -> Binary -> Proto-map -> EDN"
  [state-edn]
  (let [;; Step 1: Create proto-map directly from EDN data
        ;; Ensure we have protocol_version if not provided
        complete-state (if (:protocol_version state-edn)
                         state-edn
                         (assoc state-edn :protocol_version 1))
        ;; Create proto-map using clj-map->proto-map
        updated-state (p/clj-map->proto-map h/state-mapper 
                                           JonSharedData$JonGUIState
                                           complete-state)
        
        ;; Step 2: Proto-map to binary
        binary (p/proto-map->bytes updated-state)
        
        ;; Step 3: Binary to proto-map
        parsed-proto (p/bytes->proto-map h/state-mapper 
                                        JonSharedData$JonGUIState 
                                        binary)
        
        ;; Step 4: Proto-map to EDN
        result-edn (into {} parsed-proto)]
    
    {:original state-edn
     :result result-edn
     :normalized_original (normalize-for-comparison state-edn)
     :normalized_result (normalize-for-comparison result-edn)
     :binary_size (count binary)
     :equal? (deep-equal? state-edn result-edn)}))

(defn cmd-round-trip
  "Complete round-trip for Command message:
   EDN -> Proto-map -> Binary -> Proto-map -> EDN"
  [cmd-edn]
  (let [;; Extract the command payload
        cmd-type (first (keys (:cmd cmd-edn)))
        cmd-payload (get-in cmd-edn [:cmd cmd-type])
        
        ;; Step 1: Create the command directly with the right fields
        ;; Build the command map with the right payload
        cmd-map (merge {:protocol_version (:protocol_version cmd-edn)
                       :client_type (-> cmd-edn :client_type name
                                      (clojure.string/replace "-" "_")
                                      (clojure.string/upper-case)
                                      keyword)}
                      {cmd-type cmd-payload})
        
        ;; Create proto-map from the complete map
        updated-cmd (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd-map)
        
        ;; Step 2: Proto-map to binary
        binary (p/proto-map->bytes updated-cmd)
        
        ;; Step 3: Binary to proto-map
        parsed-proto (p/bytes->proto-map h/cmd-mapper 
                                        JonSharedCmd$Root 
                                        binary)
        
        ;; Step 4: Proto-map to EDN
        result-edn (into {} parsed-proto)]
    
    {:original cmd-edn
     :result result-edn
     :normalized_original (normalize-for-comparison cmd-edn)
     :normalized_result (normalize-for-comparison result-edn)
     :binary_size (count binary)
     :equal? (deep-equal? cmd-edn result-edn)}))

;; ============================================================================
;; MANUALLY CREATED NESTED PAYLOADS
;; ============================================================================

(def manual-nested-state
  "Manually created deeply nested State payload with all fields"
  {:protocol_version 1
   :system {:cpu_load 75.5
            :cpu_temperature 65.3
            :gpu_load 82.1
            :gpu_temperature 72.8
            :disk_space 45
            :power_consumption 250.5
            :rec_enabled true
            :low_disk_space false
            :cur_video_rec_dir_year 2025
            :cur_video_rec_dir_month 1
            :cur_video_rec_dir_day 11
            :cur_video_rec_dir_hour 15
            :cur_video_rec_dir_minute 30
            :cur_video_rec_dir_second 45
            :loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}
   :gps {:latitude 37.7749
         :longitude -122.4194
         :altitude 52.0
         :manual_latitude 37.7749
         :manual_longitude -122.4194
         :manual_altitude 52.0
         :fix_type :JON_GUI_DATA_GPS_FIX_TYPE-3d
         :use_manual false}
   :compass {:azimuth 45.5
             :elevation 10.2
             :bank -5.3}
   :rotary {:azimuth 180.0
            :elevation 45.0
            :mode :JON_GUI_DATA_ROTARY_MODE_STABILIZATION
            :platform_azimuth 180.0
            :platform_elevation 45.0
            :scan_target 5
            :scan_target_max 10
            :current_scan_node {:speed 0.5
                               :azimuth 90.0
                               :elevation 0.0
                               :linger 2.0
                               :index 3
                               :dayzoomtablevalue 2
                               :heatzoomtablevalue 2}}
   :camera_day {:zoom 2.5
                :focus 0.75
                :fx_mode :JON_GUI_FX_MODE_DAY_REGULAR
                :defog_enable false
                :brightness 0.5
                :contrast 0.5
                :sharpness 0.5
                :saturate 0.5}
   :camera_heat {:zoom 3.0
                 :focus 0.8
                 :fx_mode :JON_GUI_FX_MODE_HEAT_WHITE_HOT
                 :brightness 0.6
                 :contrast 0.6
                 :sharpness 0.4}
   :time {:timestamp 1754664759
          :manual_timestamp 1754664759}
   :lrf {:measure_id 42
         :pointer_mode :JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON}
   :compass_calibration {:final_stage 3
                        :status :JON_GUI_COMPASS_CALIBRATE_STATUS_RUNNING
                        :target_azimuth 90.0
                        :target_bank 0.0
                        :target_elevation 0.0}
   :rec_osd {:osd_mode :JON_GUI_OSD_MODE_FULL
             :show_compass true
             :show_gps true
             :show_time true}
   :day_cam_glass_heater {:power 50
                          :temperature 25.5
                          :enabled true}
   :actual_space_time {:x 100.5
                       :y 200.3
                       :z 50.2
                       :timestamp 1754664759}
   :meteo_internal {}})

(def manual-nested-cmd
  "Manually created Command with nested payload"
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :cmd {:rotary {:start_scan {:nodes [{:speed 0.25
                                       :azimuth 0.0
                                       :elevation -10.0
                                       :linger 1.0
                                       :index 1
                                       :dayzoomtablevalue 1
                                       :heatzoomtablevalue 1}
                                      {:speed 0.5
                                       :azimuth 90.0
                                       :elevation 0.0
                                       :linger 2.0
                                       :index 2
                                       :dayzoomtablevalue 2
                                       :heatzoomtablevalue 2}
                                      {:speed 0.75
                                       :azimuth 180.0
                                       :elevation 10.0
                                       :linger 1.5
                                       :index 3
                                       :dayzoomtablevalue 3
                                       :heatzoomtablevalue 3}
                                      {:speed 1.0
                                       :azimuth 270.0
                                       :elevation 0.0
                                       :linger 1.0
                                       :index 4
                                       :dayzoomtablevalue 4
                                       :heatzoomtablevalue 4}]}}}})

;; ============================================================================
;; POSITIVE TESTS - VALID DATA ROUND-TRIPS
;; ============================================================================

(deftest test-generated-state-round-trips
  (testing "Generated State messages round-trip with deep equality"
    (dotimes [n 20]
      (let [generated (mg/generate :state/root)
            result (state-round-trip generated)]
        
        (is (:equal? result)
            (str "Round-trip " n " should preserve deep equality. "
                 "Differences: " 
                 (explain-difference (:normalized_original result)
                                   (:normalized_result result) [])))
        
        (is (m/validate :state/root (:result result))
            "Result should still validate against spec")))))

(deftest test-generated-cmd-round-trips
  (testing "Generated Command messages round-trip with deep equality"
    (let [cmd-types [[:ping {:ping {}}]
                    [:noop {:noop {}}]
                    [:frozen {:frozen {:flag true}}]
                    [:gps {:gps {:set_manual_position {:latitude 45.5 
                                                 :longitude -122.6 
                                                 :altitude 100.0}}}]
                    [:rotary {:rotary {:stop {}}}]
                    [:system {:system {:set_recording {:enable true}}}]]]
      
      (doseq [[cmd-type payload] cmd-types]
        (testing (str "Command type: " cmd-type)
          (let [cmd {:protocol_version 1
                    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                    :cmd payload}
                result (cmd-round-trip cmd)]
            
            (is (:equal? result)
                (str cmd-type " should round-trip with deep equality. "
                     "Differences: "
                     (explain-difference (:normalized_original result)
                                       (:normalized_result result) [])))
            
            (is (m/validate :cmd/root (:result result))
                "Result should still validate against spec")))))))

(deftest test-manual-nested-payloads
  (testing "Manually created deeply nested State payload"
    (let [result (state-round-trip manual-nested-state)]
      
      (is (:equal? result)
          (str "Manual nested state should round-trip with deep equality. "
               "Binary size: " (:binary_size result) " bytes. "
               "Differences: "
               (explain-difference (:normalized_original result)
                                 (:normalized_result result) [])))
      
      (is (m/validate :state/root (:result result))
          "Result should validate against spec")
      
      ;; Check specific nested values
      (is (= 0.5 (get-in (:result result) [:rotary :current_scan_node :speed]))
          "Nested scan node speed should be preserved")
      
      (is (= :JON_GUI_DATA_GPS_FIX_TYPE-3d (get-in (:result result) [:gps :fix_type]))
          "Enum values should be preserved")))
  
  (testing "Manually created nested Command payload"
    (let [result (cmd-round-trip manual-nested-cmd)]
      
      (is (:equal? result)
          (str "Manual nested command should round-trip with deep equality. "
               "Differences: "
               (explain-difference (:normalized_original result)
                                 (:normalized_result result) [])))
      
      (is (= 4 (count (get-in (:result result) [:cmd :rotary :start_scan :nodes])))
          "All scan nodes should be preserved")
      
      (is (= 0.25 (get-in (:result result) [:cmd :rotary :start_scan :nodes 0 :speed]))
          "First node speed should be preserved"))))

;; ============================================================================
;; NEGATIVE TESTS - INVALID DATA SHOULD FAIL
;; ============================================================================

(deftest test-negative-cases
  (testing "Invalid data should not validate"
    
    (testing "State with invalid GPS coordinates"
      (let [invalid-state (assoc-in manual-nested-state [:gps :latitude] 91.0)]
        (is (not (m/validate :state/root invalid-state))
            "State with latitude > 90 should not validate")))
    
    (testing "State with protocol version 0"
      (let [invalid-state (assoc manual-nested-state :protocol_version 0)]
        (is (not (m/validate :state/root invalid-state))
            "State with protocol-version 0 should not validate")))
    
    (testing "Command with invalid rotary speed"
      (let [invalid-cmd {:protocol_version 1
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :cmd {:rotary {:rotate_azimuth {:direction :JON_GUI_ROTARY_DIRECTION_LEFT
                                                         :speed 0.0}}}}]
        (is (not (m/validate :cmd/root invalid-cmd))
            "Command with speed 0 should not validate")))
    
    (testing "Command with UNSPECIFIED client type"
      (let [invalid-cmd {:protocol_version 1
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
                         :cmd {:ping {}}}]
        (is (not (m/validate :cmd/root invalid-cmd))
            "Command with UNSPECIFIED client type should not validate")))
    
    (testing "State missing required field"
      (let [invalid-state (dissoc manual-nested-state :gps)]
        (is (not (m/validate :state/root invalid-state))
            "State missing GPS should not validate")))
    
    (testing "Command with multiple payloads (violates oneof)"
      (let [invalid-cmd {:protocol_version 1
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :cmd {:ping {}
                               :noop {}}}]
        (is (not (m/validate :cmd/root invalid-cmd))
            "Command with multiple payloads should not validate")))))

;; ============================================================================
;; SANITY CHECKS - EDGE CASES
;; ============================================================================

(deftest test-edge-cases
  (testing "Edge case values"
    
    (testing "Boundary values for GPS"
      (let [boundary-state (-> manual-nested-state
                              (assoc-in [:gps :latitude] 90.0)
                              (assoc-in [:gps :longitude] 180.0)
                              (assoc-in [:gps :altitude] 8848.86))
            result (state-round-trip boundary-state)]
        
        (is (:equal? result)
            "GPS boundary values should round-trip correctly")
        
        (is (= 90.0 (get-in (:result result) [:gps :latitude]))
            "Max latitude should be preserved")
        
        (is (= 180.0 (get-in (:result result) [:gps :longitude]))
            "Max longitude should be preserved")))
    
    (testing "Minimum valid rotary speed"
      (let [min-speed-cmd {:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :cmd {:rotary {:rotate_azimuth {:direction :JON_GUI_ROTARY_DIRECTION_LEFT
                                                           :speed 0.001}}}}
            result (cmd-round-trip min-speed-cmd)]
        
        (is (m/validate :cmd/root (:result result))
            "Minimum valid speed should validate")
        
        (is (> (get-in (:result result) [:cmd :rotary :rotate_azimuth :speed]) 0)
            "Speed should be > 0")))
    
    (testing "Empty sub-messages"
      (let [empty-meteo-state manual-nested-state
            result (state-round-trip empty-meteo-state)]
        
        (is (:equal? result)
            "State with empty meteo should round-trip")
        
        (is (= {} (get (:result result) :meteo_internal))
            "Empty meteo should remain empty")))
    
    (testing "Large protocol version"
      (let [large-version-cmd {:protocol_version 999999
                               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                               :cmd {:ping {}}}
            result (cmd-round-trip large-version-cmd)]
        
        (is (:equal? result)
            "Large protocol version should round-trip")
        
        (is (= 999999 (:protocol_version (:result result)))
            "Large version should be preserved")))))

;; ============================================================================
;; BUF.VALIDATE INTEGRATION
;; ============================================================================

(deftest test-buf-validate-round-trips
  (testing "Round-tripped data passes buf.validate"
    
    (testing "Generated state validates after round-trip"
      (let [generated (mg/generate :state/root)
            result (state-round-trip generated)
            ;; Use the already created proto-map from the round-trip
            base-state (h/valid-state)
            proto-map (reduce-kv
                       (fn [proto k v]
                         (p/p-> proto (assoc k v)))
                       base-state
                       (:result result))
            binary (p/proto-map->bytes proto-map)
            validation (v/validate-binary binary :type :state)]
        
        (is (:valid? validation)
            (str "Round-tripped state should pass buf.validate. "
                 "Violations: " (:violations validation)))))
    
    (testing "Manual state validates after round-trip"
      (let [result (state-round-trip manual-nested-state)
            base-state (h/valid-state)
            proto-map (reduce-kv
                       (fn [proto k v]
                         (p/p-> proto (assoc k v)))
                       base-state
                       (:result result))
            binary (p/proto-map->bytes proto-map)
            validation (v/validate-binary binary :type :state)]
        
        (is (:valid? validation)
            (str "Round-tripped manual state should pass buf.validate. "
                 "Violations: " (:violations validation)))))
    
    (testing "Commands validate after round-trip"
      (let [cmd {:protocol_version 1
                :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                :cmd {:gps {:set_manual_position {:latitude 45.5
                                            :longitude -122.6
                                            :altitude 100.0}}}}
            result (cmd-round-trip cmd)
            proto-map (p/proto-map h/cmd-mapper JonSharedCmd$Root
                                  :protocol_version (:protocol_version (:result result))
                                  :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                  :gps (get-in (:result result) [:cmd :gps]))
            binary (p/proto-map->bytes proto-map)
            validation (v/validate-binary binary :type :cmd)]
        
        (is (:valid? validation)
            (str "Round-tripped command should pass buf.validate. "
                 "Violations: " (:violations validation)))))))

;; ============================================================================
;; PERFORMANCE SANITY CHECK
;; ============================================================================

(deftest test-round-trip-performance
  (testing "Round-trip performance is reasonable"
    
    (testing "State round-trip performance"
      (let [state (mg/generate :state/root)
            start (System/currentTimeMillis)
            _ (dotimes [_ 10]
                (state-round-trip state))
            elapsed (- (System/currentTimeMillis) start)]
        
        (is (< elapsed 1000)
            (str "10 state round-trips should complete within 1 second. "
                 "Actual: " elapsed "ms"))))
    
    (testing "Command round-trip performance"
      (let [cmd {:protocol_version 1
                :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                :cmd {:ping {}}}
            start (System/currentTimeMillis)
            _ (dotimes [_ 100]
                (cmd-round-trip cmd))
            elapsed (- (System/currentTimeMillis) start)]
        
        (is (< elapsed 500)
            (str "100 simple command round-trips should complete within 500ms. "
                 "Actual: " elapsed "ms"))))))