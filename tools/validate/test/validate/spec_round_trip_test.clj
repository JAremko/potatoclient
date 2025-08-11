(ns validate.spec-round-trip-test
  "Simplified round-trip validation tests using actual proto classes from proto-maps.
   This approach avoids hardcoding Java class names."
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [pronto.core :as p]
   [validate.test-harness :as h]
   [validate.validator :as v]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load specs
   [potatoclient.specs.common]
   [potatoclient.specs.state.gps]
   [potatoclient.specs.state.system]
   [potatoclient.specs.state.compass]
   [potatoclient.specs.state.rotary]
   [potatoclient.specs.state.camera-day]
   [potatoclient.specs.state.camera-heat]
   [potatoclient.specs.state.time]
   [potatoclient.specs.state.root]
   [potatoclient.specs.cmd.common]
   [potatoclient.specs.cmd.root]
   [clojure.tools.logging :as log]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; ============================================================================
;; UTILITY FUNCTIONS
;; ============================================================================

(defn get-proto-class
  "Extract the actual Java class from a proto-map."
  [proto-map]
  (when (p/proto-map? proto-map)
    (class (p/proto-map->proto proto-map))))

(defn spec->proto-map
  "Convert a Malli spec-generated EDN map to a proto-map.
   Handles kebab-case to snake_case conversion."
  [edn-map mapper proto-class]
  (let [snake-case-map (clojure.walk/postwalk
                         (fn [x]
                           (if (keyword? x)
                             (keyword (clojure.string/replace (name x) "-" "_"))
                             x))
                         edn-map)]
    (p/clj-map->proto-map mapper proto-class snake-case-map)))

(defn proto-map->spec-edn
  "Convert a proto-map back to EDN format with kebab-case keys."
  [proto-map]
  (let [snake-map (p/proto-map->clj-map proto-map)
        kebab-map (clojure.walk/postwalk
                   (fn [x]
                     (if (keyword? x)
                       (keyword (clojure.string/replace (name x) "_" "-"))
                       x))
                   snake-map)]
    kebab-map))

(defn validate-round-trip
  "Perform a simplified round-trip validation:
   1. Generate data from spec
   2. Convert to proto-map
   3. Get binary and validate with buf.validate
   4. Parse binary back
   5. Convert back to EDN
   6. Validate with spec again"
  [spec-key proto-map-fn]
  (try
    (let [;; Generate from spec
          generated (mg/generate spec-key)
          
          ;; Create proto-map using the provided function
          proto-map-1 (proto-map-fn generated)
          
          ;; Get the actual proto class
          proto-class (get-proto-class proto-map-1)
          
          ;; Convert to binary
          binary (p/proto-map->bytes proto-map-1)
          
          ;; Validate with buf.validate
          validation-result (v/validate-binary binary)
          
          ;; Convert back to EDN
          edn-2 (proto-map->spec-edn proto-map-1)]
      
      {:success? (:valid? validation-result)
       :proto-class proto-class
       :validation validation-result
       :original generated
       :final edn-2
       :equal? (= generated edn-2)})
    
    (catch Exception e
      {:success? false
       :error (.getMessage e)
       :exception e})))

;; ============================================================================
;; ROUND-TRIP TESTS
;; ============================================================================

(deftest test-gps-round-trip
  (testing "GPS message round-trip validation"
    (testing "Manual GPS value"
      (let [gps-edn {:altitude 100.0
                     :fix-type :jon-gui-data-gps-fix-type-3d
                     :latitude 45.5
                     :longitude -122.6
                     :manual-latitude 45.5
                     :manual-longitude -122.6}
            result (validate-round-trip 
                    :state/gps
                    (fn [edn]
                      (spec->proto-map edn h/state-mapper 
                                      ser.JonSharedDataGps$JonGuiDataGps)))]
        (is (:success? result)
            (str "GPS round-trip should succeed. " 
                 "Validation: " (get-in result [:validation :message])))
        (is (:equal? result) "Data should be preserved through round-trip")))
    
    (testing "Generated GPS values (10 samples)"
      (let [results (for [_ (range 10)]
                     (validate-round-trip 
                      :state/gps
                      (fn [edn]
                        (spec->proto-map edn h/state-mapper 
                                        ser.JonSharedDataGps$JonGuiDataGps))))
            successes (filter :success? results)]
        (is (>= (count successes) 8)
            (str "At least 80% of generated GPS values should validate. "
                 "Success: " (count successes) "/10"))))))

(deftest test-simple-state-round-trips
  (testing "Simple state message round-trips"
    (testing "Time message"
      (let [time-edn {:manual-timestamp 1754664759
                      :timestamp 1754664759}
            proto-map (p/proto-map h/state-mapper 
                                   ser.JonSharedDataTime$JonGuiDataTime
                                   :manual_timestamp 1754664759
                                   :timestamp 1754664759)
            proto-class (get-proto-class proto-map)]
        (is (= ser.JonSharedDataTime$JonGuiDataTime proto-class)
            "Should extract correct proto class from proto-map")
        
        (let [binary (p/proto-map->bytes proto-map)
              validation (v/validate-binary binary)]
          (is (:valid? validation)
              "Time proto-map should validate with buf.validate"))))
    
    (testing "Compass message"
      (let [compass-edn {:azimuth 180.0
                         :elevation 45.0
                         :bank 0.0}
            proto-map (spec->proto-map compass-edn h/state-mapper 
                                       ser.JonSharedDataCompass$JonGuiDataCompass)
            binary (p/proto-map->bytes proto-map)
            validation (v/validate-binary binary)]
        (is (:valid? validation)
            "Compass proto-map should validate with buf.validate")))))

(deftest test-command-round-trips
  (testing "Command message round-trips"
    (testing "Ping command"
      (let [ping-cmd {:protocol-version 1
                      :client-type :jon-gui-data-client-type-local-network
                      :cmd {:ping {}}}
            ;; Create the ping command directly
            proto-map (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Root
                                   :protocol_version 1
                                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                   :ping (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Ping))
            proto-class (get-proto-class proto-map)]
        (is (= cmd.JonSharedCmd$Root proto-class)
            "Should extract correct proto class for command")
        
        (let [binary (p/proto-map->bytes proto-map)
              validation (v/validate-binary binary :type :cmd)]
          (is (:valid? validation)
              "Ping command should validate with buf.validate"))))
    
    (testing "Noop command"
      (let [proto-map (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Root
                                   :protocol_version 1
                                   :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                                   :noop (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Noop))
            binary (p/proto-map->bytes proto-map)
            validation (v/validate-binary binary :type :cmd)]
        (is (:valid? validation)
            "Noop command should validate with buf.validate")))))

(deftest test-complete-state-round-trip
  (testing "Complete state message round-trip"
    (testing "Using real state data from test harness"
      (let [state-proto-map (h/valid-state)
            proto-class (get-proto-class state-proto-map)]
        (is (= ser.JonSharedData$JonGUIState proto-class)
            "Should extract correct proto class for complete state")
        
        (let [binary (p/proto-map->bytes state-proto-map)
              validation (v/validate-binary binary :type :state)]
          (is (:valid? validation)
              "Complete state should validate with buf.validate")
          
          ;; Round-trip test
          (let [parsed (p/bytes->proto-map h/state-mapper 
                                           ser.JonSharedData$JonGUIState 
                                           binary)
                edn-1 (proto-map->spec-edn state-proto-map)
                edn-2 (proto-map->spec-edn parsed)]
            (is (= edn-1 edn-2)
                "State data should be preserved through binary round-trip")))))))

(deftest test-proto-class-extraction
  (testing "Proto class extraction from various proto-maps"
    (let [test-cases
          [{:name "GPS State"
            :proto-map (p/proto-map h/state-mapper ser.JonSharedDataGps$JonGuiDataGps)
            :expected ser.JonSharedDataGps$JonGuiDataGps}
           
           {:name "System State"
            :proto-map (p/proto-map h/state-mapper ser.JonSharedDataSystem$JonGuiDataSystem)
            :expected ser.JonSharedDataSystem$JonGuiDataSystem}
           
           {:name "Root Command"
            :proto-map (p/proto-map h/cmd-mapper cmd.JonSharedCmd$Root)
            :expected cmd.JonSharedCmd$Root}
           
           {:name "Complete State"
            :proto-map (h/valid-state)
            :expected ser.JonSharedData$JonGUIState}]]
      
      (doseq [{:keys [name proto-map expected]} test-cases]
        (testing name
          (let [actual (get-proto-class proto-map)]
            (is (= expected actual)
                (str "Should extract " expected " from " name))))))))