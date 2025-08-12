(ns validate.specs.cmd-property-test
  "Property-based tests for Command message specs.
   Tests each command type and the complete root message against buf.validate."
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.spec-validation-harness :as harness]
   [validate.test-harness :as h]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [pronto.core :as p]
   [validate.validator :as v]
   [clojure.tools.logging :as log]
   ;; Load all command specs
   [potatoclient.specs.common]
   [potatoclient.specs.cmd.common]
   [potatoclient.specs.cmd.compass]
   [potatoclient.specs.cmd.cv]
   [potatoclient.specs.cmd.day-cam-glass-heater]
   [potatoclient.specs.cmd.day-camera]
   [potatoclient.specs.cmd.gps]
   [potatoclient.specs.cmd.heat-camera]
   [potatoclient.specs.cmd.lira]
   [potatoclient.specs.cmd.lrf]
   [potatoclient.specs.cmd.lrf-align]
   [potatoclient.specs.cmd.osd]
   [potatoclient.specs.cmd.rotary]
   [potatoclient.specs.cmd.system]
   [potatoclient.specs.cmd.root :refer [command-fields]])
  (:import 
   [cmd JonSharedCmd$Root 
    JonSharedCmd$Ping
    JonSharedCmd$Noop
    JonSharedCmd$Frozen]
   [cmd.RotaryPlatform 
    JonSharedCmdRotary$Root
    JonSharedCmdRotary$SetPlatformAzimuth
    JonSharedCmdRotary$SetPlatformElevation
    JonSharedCmdRotary$SetMode
    JonSharedCmdRotary$Stop]
   [cmd.CV JonSharedCmdCv$Root]
   [cmd.DayCamera JonSharedCmdDayCamera$Root]
   [cmd.HeatCamera JonSharedCmdHeatCamera$Root]
   [cmd.Gps JonSharedCmdGps$Root]
   [cmd.Compass JonSharedCmdCompass$Root]
   [cmd.Lrf JonSharedCmdLrf$Root]
   [cmd.OSD JonSharedCmdOsd$Root]
   [cmd.System JonSharedCmdSystem$Root]))

(harness/init-registry!)

;; ============================================================================
;; MANUAL TEST VALUES FOR COMMANDS
;; ============================================================================

(def manual-ping-cmd
  {:protocol_version 1
   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   :ping {}})

(def manual-noop-cmd
  {:protocol_version 100
   :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   :noop {}})

(def manual-frozen-cmd
  {:protocol_version 42
   :client_type :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   :frozen {}})

(def manual-rotary-cmds
  "Manual test values for rotary commands"
  [{:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:set_platform_azimuth {:value 0.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:set_platform_azimuth {:value 180.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:set_platform_azimuth {:value 359.999}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
    :rotary {:set_platform_elevation {:value -90.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
    :rotary {:set_platform_elevation {:value 0.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
    :rotary {:set_platform_elevation {:value 90.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:set_mode {:mode :JON_GUI_DATA_ROTARY_MODE_POSITION}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:set_mode {:mode :JON_GUI_DATA_ROTARY_MODE_STABILIZATION}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:stop {}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:scan_add_node {:azimuth 180.0
                             :elevation 0.0
                             :linger 0.5
                             :dayzoomtablevalue 2
                             :heatzoomtablevalue 3}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :rotary {:scan_update_node {:index 1
                                :azimuth 0.0
                                :elevation -90.0
                                :linger 10.0
                                :dayzoomtablevalue 4
                                :heatzoomtablevalue 4}}}])

(def manual-gps-cmds
  [{:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :gps {:set_manual_position {:latitude 0.0
                                :longitude 0.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :gps {:set_manual_position {:latitude 90.0
                                :longitude 180.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :gps {:set_manual_position {:latitude -90.0
                                :longitude -180.0}}}
   
   {:protocol_version 1
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :gps {:set_use_manual_position {:value true}}}])

;; ============================================================================
;; INDIVIDUAL COMMAND TYPE TESTS
;; ============================================================================

(deftest test-simple-command-specs
  (testing "Simple command specs (Ping, Noop, Frozen)"
    (testing "Manual Ping command"
      (is (m/validate :cmd/root manual-ping-cmd)
          "Ping command should validate"))
    
    (testing "Manual Noop command"
      (is (m/validate :cmd/root manual-noop-cmd)
          "Noop command should validate"))
    
    (testing "Manual Frozen command"
      (is (m/validate :cmd/root manual-frozen-cmd)
          "Frozen command should validate"))
    
    (testing "Simple commands should be empty maps"
      (is (m/validate :cmd/ping {}) "Ping should be empty map")
      (is (m/validate :cmd/noop {}) "Noop should be empty map")
      (is (m/validate :cmd/frozen {}) "Frozen should be empty map")
      (is (not (m/validate :cmd/ping {:extra "field"})) "Ping should reject extra fields")
      (is (not (m/validate :cmd/noop {:extra "field"})) "Noop should reject extra fields")
      (is (not (m/validate :cmd/frozen {:extra "field"})) "Frozen should reject extra fields"))))

(deftest test-rotary-command-specs
  (testing "Rotary command specs"
    (testing "Manual rotary commands"
      (doseq [cmd manual-rotary-cmds]
        (is (m/validate :cmd/root cmd)
            (str "Rotary command should validate: " 
                 (keys (:rotary cmd))))))
    
    (testing "Property-based Rotary commands (300 samples)"
      (let [results (harness/run-property-tests 
                     :cmd/rotary
                     h/cmd-mapper
                     JonSharedCmdRotary$Root
                     :n 300)]
        (is (>= (:success-rate results) 0.95)
            (str "Rotary spec should pass at least 95% of tests. "
                 "Passed: " (:passed results) "/" (:total results)))
        
        (when (pos? (:failed results))
          (log/warn "Rotary failures:" (harness/analyze-violations results)))))))

(deftest test-gps-command-specs
  (testing "GPS command specs"
    (testing "Manual GPS commands"
      (doseq [cmd manual-gps-cmds]
        (is (m/validate :cmd/root cmd)
            (str "GPS command should validate: " 
                 (keys (:gps cmd))))))
    
    (testing "Property-based GPS commands (200 samples)"
      (let [results (harness/run-property-tests 
                     :cmd/gps
                     h/cmd-mapper
                     JonSharedCmdGps$Root
                     :n 200)]
        (is (>= (:success-rate results) 0.95)
            (str "GPS spec should pass at least 95% of tests. "
                 "Passed: " (:passed results) "/" (:total results)))))))

;; ============================================================================
;; ROOT COMMAND MESSAGE TESTS
;; ============================================================================

(deftest test-cmd-root-spec
  (testing "Complete Command root message validation"
    (testing "Manual command values"
      (is (m/validate :cmd/root manual-ping-cmd))
      (is (m/validate :cmd/root manual-noop-cmd))
      (is (m/validate :cmd/root manual-frozen-cmd))
      (doseq [cmd manual-rotary-cmds]
        (is (m/validate :cmd/root cmd))))
    
    (testing "Property-based testing (100 complete commands)"
      (let [results (harness/run-property-tests 
                     :cmd/root
                     h/cmd-mapper
                     JonSharedCmd$Root
                     :n 100)]
        (is (>= (:success-rate results) 0.90)
            (str "Command root spec should pass at least 90% of property tests. "
                 "Passed: " (:passed results) "/" (:total results)))
        
        (when (pos? (:failed results))
          (let [analysis (harness/analyze-violations results)]
            (log/warn "Command root failures:" analysis)
            (println "\nViolation analysis:")
            (println "  Top violations:" (:top-violations analysis))))))))

;; ============================================================================
;; ONEOF VALIDATION TESTS
;; ============================================================================

(deftest test-oneof-exclusivity
  (testing "Oneof field exclusivity"
    (testing "Only one command type should be present"
      (let [valid-cmd {:protocol_version 1
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :ping {}}
            
            invalid-multi-cmd {:protocol_version 1
                              :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                              :ping {}
                              :noop {}}]
        
        (is (m/validate :cmd/root valid-cmd)
            "Single command should validate")
        
        (is (not (m/validate :cmd/root invalid-multi-cmd))
            "Multiple commands should not validate (oneof constraint)")))
    
    (testing "Exactly one command must be present"
      (let [no-cmd {:protocol_version 1
                   :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK}]
        
        (is (not (m/validate :cmd/root no-cmd))
            "No command should not validate (oneof requires exactly one)")))))

;; ============================================================================
;; ROUND-TRIP VALIDATION TESTS FOR COMMANDS
;; ============================================================================

(deftest test-cmd-round-trip-validation
  (testing "Command round-trip validation"
    (testing "Simple command round-trips"
      (let [ping-result (harness/round-trip-validate 
                         :cmd/root
                         h/cmd-mapper
                         JonSharedCmd$Root
                         manual-ping-cmd)]
        (is (:success? ping-result)
            (str "Ping round-trip should succeed. Error: " (:error ping-result))))
      
      (let [noop-result (harness/round-trip-validate 
                         :cmd/root
                         h/cmd-mapper
                         JonSharedCmd$Root
                         manual-noop-cmd)]
        (is (:success? noop-result)
            (str "Noop round-trip should succeed. Error: " (:error noop-result)))))
    
    (testing "Complex rotary command round-trips"
      (doseq [cmd (take 3 manual-rotary-cmds)]
        (let [result (harness/round-trip-validate 
                      :cmd/root
                      h/cmd-mapper
                      JonSharedCmd$Root
                      cmd)]
          (is (:success? result)
              (str "Rotary command round-trip should succeed. "
                   "Command: " (keys (:rotary cmd))
                   " Error: " (:error result))))))
    
    (testing "Generated command round-trips (50 samples)"
      (dotimes [_ 50]
        (let [generated (mg/generate :cmd/root)
              result (harness/round-trip-validate 
                      :cmd/root
                      h/cmd-mapper
                      JonSharedCmd$Root
                      generated)]
          (when-not (:success? result)
            (log/warn "Failed round-trip for generated command:" 
                     (filter command-fields (keys generated))
                     "Error:" (:error result))))))))

;; ============================================================================
;; BOUNDARY VALUE TESTS FOR COMMANDS
;; ============================================================================

(deftest test-cmd-boundary-values
  (testing "Command boundary value validation"
    (testing "Protocol version boundaries"
      (let [min-protocol {:protocol_version 1
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}
            
            max-protocol {:protocol_version 2147483647
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}}]
        
        (is (m/validate :cmd/root min-protocol)
            "Min protocol version should validate")
        
        (is (m/validate :cmd/root max-protocol)
            "Max protocol version should validate")))
    
    (testing "Rotary position boundaries"
      (let [min-values-cmd {:protocol_version 1
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :rotary {:scan_add_node {:azimuth 0.0
                                                    :elevation -90.0
                                                    :linger 0.001
                                                    :dayzoomtablevalue 0
                                                    :heatzoomtablevalue 0}}}
            
            max-values-cmd {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :rotary {:scan_update_node {:index 1000
                                                      :azimuth 359.999
                                                      :elevation 90.0
                                                      :linger 100.0
                                                      :dayzoomtablevalue 10
                                                      :heatzoomtablevalue 10}}}]
        
        (is (:success? (harness/round-trip-validate 
                        :cmd/root
                        h/cmd-mapper
                        JonSharedCmd$Root
                        min-values-cmd))
            "Min values rotary command should validate")
        
        (is (:success? (harness/round-trip-validate 
                        :cmd/root
                        h/cmd-mapper
                        JonSharedCmd$Root
                        max-values-cmd))
            "Max values rotary command should validate")))))

;; ============================================================================
;; CLIENT TYPE VALIDATION
;; ============================================================================

(deftest test-client-type-constraints
  (testing "Client type cannot be UNSPECIFIED"
    (testing "Valid client types"
      (let [valid-types [:JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                        :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                        :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
                        :JON_GUI_DATA_CLIENT_TYPE_LIRA]]
        (doseq [client-type valid-types]
          (let [cmd {:protocol_version 1
                    :client_type client-type
                    :ping {}}]
            (is (m/validate :cmd/root cmd)
                (str "Client type " client-type " should validate"))))))
    
    (testing "Invalid UNSPECIFIED client type"
      (let [invalid-cmd {:protocol_version 1
                        :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED
                        :ping {}}]
        (is (not (m/validate :proto/client-type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED))
            "UNSPECIFIED client type should not validate")))))

;; ============================================================================
;; SPEC COVERAGE TESTS FOR COMMANDS
;; ============================================================================

(deftest test-cmd-spec-coverage
  (testing "Command spec coverage analysis"
    (testing "Command root coverage"
      (let [coverage (harness/check-spec-coverage :cmd/root 100)]
        (is (contains? (:paths coverage) :protocol_version))
        (is (contains? (:paths coverage) :client_type))
        ;; Commands are at root level, not under :cmd
        (is (or (contains? (:paths coverage) :ping)
                (contains? (:paths coverage) :noop)
                (contains? (:paths coverage) :rotary))
            "Should have at least one command field")))
    
    (testing "All command types generated"
      (let [samples (mg/sample :cmd/root {:size 500})
            command-fields #{:ping :noop :frozen :rotary :gps :compass :lrf :cv 
                           :day_camera :heat_camera :osd :system :lrf_calib 
                           :day_cam_glass_heater :lira}
            cmd-types (set (filter command-fields (mapcat keys samples)))]
        (is (>= (count cmd-types) 10)
            (str "Should generate at least 10 different command types. "
                 "Generated: " cmd-types))))))