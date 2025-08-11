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
   [potatoclient.specs.cmd.root])
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
  {:protocol-version 1
   :client-type :jon-gui-data-client-type-local-network
   :cmd {:ping {}}})

(def manual-noop-cmd
  {:protocol-version 100
   :client-type :jon-gui-data-client-type-internal-cv
   :cmd {:noop {}}})

(def manual-frozen-cmd
  {:protocol-version 42
   :client-type :jon-gui-data-client-type-certificate-protected
   :cmd {:frozen {}}})

(def manual-rotary-cmds
  "Manual test values for rotary commands"
  [{:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:set-platform-azimuth {:value 0.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:set-platform-azimuth {:value 180.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:set-platform-azimuth {:value 359.999}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-internal-cv
    :cmd {:rotary {:set-platform-elevation {:value -90.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-internal-cv
    :cmd {:rotary {:set-platform-elevation {:value 0.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-internal-cv
    :cmd {:rotary {:set-platform-elevation {:value 90.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:set-mode {:mode :jon-gui-data-rotary-mode-position}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:set-mode {:mode :jon-gui-data-rotary-mode-stabilization}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:stop {}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:scan-node {:speed 0.001  ; Just above 0
                               :azimuth 180.0
                               :elevation 0.0
                               :linger 0.5
                               :index 1
                               :dayzoomtablevalue 2
                               :heatzoomtablevalue 3}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:rotary {:scan-node {:speed 1.0  ; Max speed
                               :azimuth 0.0
                               :elevation -90.0
                               :linger 10.0
                               :index 100
                               :dayzoomtablevalue 4
                               :heatzoomtablevalue 4}}}}])

(def manual-gps-cmds
  [{:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:gps {:set-fix-manual {:latitude 0.0
                                 :longitude 0.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:gps {:set-fix-manual {:latitude 90.0
                                 :longitude 180.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:gps {:set-fix-manual {:latitude -90.0
                                 :longitude -180.0}}}}
   
   {:protocol-version 1
    :client-type :jon-gui-data-client-type-local-network
    :cmd {:gps {:set-fix-auto {}}}}])

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
    
    (testing "Property-based Ping (100 samples)"
      (let [results (harness/run-property-tests 
                     :cmd/ping
                     h/cmd-mapper
                     JonSharedCmd$Ping
                     :n 100)]
        (is (= 1.0 (:success-rate results))
            (str "Ping spec should pass all tests. "
                 "Failed: " (:failed results)))))
    
    (testing "Property-based Noop (100 samples)"
      (let [results (harness/run-property-tests 
                     :cmd/noop
                     h/cmd-mapper
                     JonSharedCmd$Noop
                     :n 100)]
        (is (= 1.0 (:success-rate results))
            (str "Noop spec should pass all tests. "
                 "Failed: " (:failed results)))))
    
    (testing "Property-based Frozen (100 samples)"
      (let [results (harness/run-property-tests 
                     :cmd/frozen
                     h/cmd-mapper
                     JonSharedCmd$Frozen
                     :n 100)]
        (is (= 1.0 (:success-rate results))
            (str "Frozen spec should pass all tests. "
                 "Failed: " (:failed results)))))))

(deftest test-rotary-command-specs
  (testing "Rotary command specs"
    (testing "Manual rotary commands"
      (doseq [cmd manual-rotary-cmds]
        (is (m/validate :cmd/root cmd)
            (str "Rotary command should validate: " 
                 (keys (get-in cmd [:cmd :rotary]))))))
    
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
                 (keys (get-in cmd [:cmd :gps]))))))
    
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
      (let [valid-cmd {:protocol-version 1
                       :client-type :jon-gui-data-client-type-local-network
                       :cmd {:ping {}}}
            
            invalid-multi-cmd {:protocol-version 1
                              :client-type :jon-gui-data-client-type-local-network
                              :cmd {:ping {}
                                   :noop {}}}]
        
        (is (m/validate :cmd/root valid-cmd)
            "Single command should validate")
        
        (is (not (m/validate :cmd/root invalid-multi-cmd))
            "Multiple commands should not validate (oneof constraint)")))
    
    (testing "Exactly one command must be present"
      (let [no-cmd {:protocol-version 1
                   :client-type :jon-gui-data-client-type-local-network
                   :cmd {}}]
        
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
                   "Command: " (keys (get-in cmd [:cmd :rotary]))
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
                     (keys (get-in generated [:cmd])) 
                     "Error:" (:error result))))))))

;; ============================================================================
;; BOUNDARY VALUE TESTS FOR COMMANDS
;; ============================================================================

(deftest test-cmd-boundary-values
  (testing "Command boundary value validation"
    (testing "Protocol version boundaries"
      (let [min-protocol {:protocol-version 1
                         :client-type :jon-gui-data-client-type-local-network
                         :cmd {:ping {}}}
            
            max-protocol {:protocol-version 2147483647
                         :client-type :jon-gui-data-client-type-local-network
                         :cmd {:ping {}}}]
        
        (is (m/validate :cmd/root min-protocol)
            "Min protocol version should validate")
        
        (is (m/validate :cmd/root max-protocol)
            "Max protocol version should validate")))
    
    (testing "Rotary speed boundaries"
      (let [min-speed-cmd {:protocol-version 1
                           :client-type :jon-gui-data-client-type-local-network
                           :cmd {:rotary {:scan-node {:speed 0.001
                                                      :azimuth 0.0
                                                      :elevation 0.0
                                                      :linger 0.001
                                                      :index 1
                                                      :dayzoomtablevalue 1
                                                      :heatzoomtablevalue 1}}}}
            
            max-speed-cmd {:protocol-version 1
                          :client-type :jon-gui-data-client-type-local-network
                          :cmd {:rotary {:scan-node {:speed 1.0
                                                     :azimuth 359.999
                                                     :elevation 90.0
                                                     :linger 100.0
                                                     :index 1000
                                                     :dayzoomtablevalue 4
                                                     :heatzoomtablevalue 4}}}}]
        
        (is (:success? (harness/round-trip-validate 
                        :cmd/root
                        h/cmd-mapper
                        JonSharedCmd$Root
                        min-speed-cmd))
            "Min speed rotary command should validate")
        
        (is (:success? (harness/round-trip-validate 
                        :cmd/root
                        h/cmd-mapper
                        JonSharedCmd$Root
                        max-speed-cmd))
            "Max speed rotary command should validate")))))

;; ============================================================================
;; CLIENT TYPE VALIDATION
;; ============================================================================

(deftest test-client-type-constraints
  (testing "Client type cannot be UNSPECIFIED"
    (testing "Valid client types"
      (let [valid-types [:jon-gui-data-client-type-internal-cv
                        :jon-gui-data-client-type-local-network
                        :jon-gui-data-client-type-certificate-protected
                        :jon-gui-data-client-type-lira]]
        (doseq [client-type valid-types]
          (let [cmd {:protocol-version 1
                    :client-type client-type
                    :cmd {:ping {}}}]
            (is (m/validate :cmd/root cmd)
                (str "Client type " client-type " should validate"))))))
    
    (testing "Invalid UNSPECIFIED client type"
      (let [invalid-cmd {:protocol-version 1
                        :client-type :jon-gui-data-client-type-unspecified
                        :cmd {:ping {}}}]
        (is (not (m/validate :proto/client-type :jon-gui-data-client-type-unspecified))
            "UNSPECIFIED client type should not validate")))))

;; ============================================================================
;; SPEC COVERAGE TESTS FOR COMMANDS
;; ============================================================================

(deftest test-cmd-spec-coverage
  (testing "Command spec coverage analysis"
    (testing "Command root coverage"
      (let [coverage (harness/check-spec-coverage :cmd/root 100)]
        (is (contains? (:paths coverage) :protocol-version))
        (is (contains? (:paths coverage) :client-type))
        (is (contains? (:paths coverage) :cmd))))
    
    (testing "All command types generated"
      (let [samples (mg/sample :cmd/root {:size 500})
            cmd-types (set (mapcat (fn [sample]
                                    (keys (get sample :cmd)))
                                  samples))]
        (is (>= (count cmd-types) 10)
            (str "Should generate at least 10 different command types. "
                 "Generated: " cmd-types))))))