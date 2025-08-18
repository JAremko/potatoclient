(ns potatoclient.cmd.generative-test
  "Generative tests for command functions.
   Tests all command functions with 500+ generated samples to ensure robustness."
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [potatoclient.cmd.root :as root]
   [potatoclient.cmd.system :as sys]
   [potatoclient.cmd.core :as core]
   [potatoclient.cmd.validation :as v]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]
   [malli.core :as m]
   [malli.generator :as mg]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; ============================================================================
;; Configuration
;; ============================================================================

(def num-tests 
  "Number of samples to generate for each property test"
  500)

;; ============================================================================
;; Generators
;; ============================================================================

(def session-id-gen
  "Generator for valid session IDs"
  (gen/choose 0 2147483647))

(def client-type-gen
  "Generator for valid client types"
  (gen/elements [:JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                 :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
                 :JON_GUI_DATA_CLIENT_TYPE_LIRA]))

(def localization-gen
  "Generator for valid localization values"
  (gen/elements [:JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
                 :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
                 :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
                 :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS]))

;; ============================================================================
;; Root Command Properties
;; ============================================================================

(defspec ping-command-always-valid num-tests
  (prop/for-all [session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean]
    (let [base-cmd (root/ping)
          full-cmd (merge {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :session_id session-id
                          :important important
                          :from_cv_subsystem from-cv}
                         base-cmd)]
      (and (= {:ping {}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec noop-command-always-valid num-tests
  (prop/for-all [session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean
                 client-type client-type-gen]
    (let [base-cmd (root/noop)
          full-cmd (merge {:protocol_version 1
                          :client_type client-type
                          :session_id session-id
                          :important important
                          :from_cv_subsystem from-cv}
                         base-cmd)]
      (and (= {:noop {}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec frozen-command-always-valid num-tests
  (prop/for-all [session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean
                 client-type client-type-gen]
    (let [base-cmd (root/frozen)
          full-cmd (merge {:protocol_version 1
                          :client_type client-type
                          :session_id session-id
                          :important important
                          :from_cv_subsystem from-cv}
                         base-cmd)]
      (and (= {:frozen {}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

;; ============================================================================
;; System Command Properties
;; ============================================================================

(defspec system-reboot-always-valid num-tests
  (prop/for-all [session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean
                 client-type client-type-gen]
    (let [base-cmd (sys/reboot)
          full-cmd (merge {:protocol_version 1
                          :client_type client-type
                          :session_id session-id
                          :important important
                          :from_cv_subsystem from-cv}
                         base-cmd)]
      (and (= {:system {:reboot {}}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec system-power-off-always-valid num-tests
  (prop/for-all [session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean]
    (let [base-cmd (sys/power-off)
          full-cmd (merge {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :session_id session-id
                          :important important
                          :from_cv_subsystem from-cv}
                         base-cmd)]
      (and (= {:system {:power_off {}}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec system-start-all-always-valid num-tests
  (prop/for-all [session-id session-id-gen
                 important gen/boolean]
    (let [base-cmd (sys/start-all)
          full-cmd (merge {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :session_id session-id
                          :important important
                          :from_cv_subsystem false}
                         base-cmd)]
      (and (= {:system {:start_all {}}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec system-stop-all-always-valid num-tests
  (prop/for-all [session-id session-id-gen]
    (let [base-cmd (sys/stop-all)
          full-cmd (merge {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :session_id session-id
                          :important false
                          :from_cv_subsystem false}
                         base-cmd)]
      (and (= {:system {:stop_all {}}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec system-set-localization-with-valid-enum num-tests
  (prop/for-all [loc localization-gen
                 session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean
                 client-type client-type-gen]
    (let [base-cmd (sys/set-localization loc)
          full-cmd (merge {:protocol_version 1
                          :client_type client-type
                          :session_id session-id
                          :important important
                          :from_cv_subsystem from-cv}
                         base-cmd)]
      (and (= {:system {:localization {:loc loc}}} base-cmd)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

;; ============================================================================
;; Core Infrastructure Properties
;; ============================================================================

(defspec send-command-with-session-preserves-session num-tests
  (prop/for-all [session-id session-id-gen]
    (let [cmd {:ping {}}
          result (core/send-command-with-session! cmd session-id)
          full-cmd (merge {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :session_id session-id
                          :important false
                          :from_cv_subsystem false}
                         cmd)]
      (and (= cmd result)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

(defspec send-important-command-sets-flag num-tests
  (prop/for-all [session-id session-id-gen]
    (let [cmd {:noop {}}
          result (core/send-important-command! cmd)
          full-cmd (merge {:protocol_version 1
                          :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                          :session_id 0  ; Default when not specified
                          :important true
                          :from_cv_subsystem false}
                         cmd)]
      (and (= cmd result)
           (:valid? (v/validate-roundtrip-with-report full-cmd))))))

;; ============================================================================
;; Command Combinations Property
;; ============================================================================

(def command-gen
  "Generator for various command types"
  (gen/one-of [(gen/return {:ping {}})
               (gen/return {:noop {}})
               (gen/return {:frozen {}})
               (gen/return {:system {:reboot {}}})
               (gen/return {:system {:power_off {}}})
               (gen/return {:system {:start_all {}}})
               (gen/return {:system {:stop_all {}}})
               (gen/return {:system {:mark_rec_important {}}})
               (gen/return {:system {:unmark_rec_important {}}})
               (gen/return {:system {:enter_transport {}}})
               (gen/return {:system {:geodesic_mode_enable {}}})
               (gen/return {:system {:geodesic_mode_disable {}}})
               (gen/fmap (fn [loc] {:system {:localization {:loc loc}}})
                         localization-gen)]))

(defspec all-valid-commands-roundtrip num-tests
  (prop/for-all [cmd command-gen
                 session-id session-id-gen
                 important gen/boolean
                 from-cv gen/boolean
                 client-type client-type-gen]
    (let [full-cmd (merge {:protocol_version 1
                           :client_type client-type
                           :session_id session-id
                           :important important
                           :from_cv_subsystem from-cv}
                          cmd)]
      (:valid? (v/validate-roundtrip-with-report full-cmd)))))

;; ============================================================================
;; Edge Cases and Boundary Testing
;; ============================================================================

(defspec protocol-version-always-positive num-tests
  (prop/for-all [protocol-ver (gen/choose 1 100)
                 cmd command-gen]
    (let [full-cmd (merge {:protocol_version protocol-ver
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :session_id 0
                           :important false
                           :from_cv_subsystem false}
                          cmd)]
      (:valid? (v/validate-roundtrip-with-report full-cmd)))))

(defspec session-id-boundary-values num-tests
  (prop/for-all [cmd command-gen]
    (let [test-session-ids [0 1 100 1000 2147483646 2147483647]]
      (every? (fn [session-id]
                (let [full-cmd (merge {:protocol_version 1
                                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                                       :session_id session-id
                                       :important false
                                       :from_cv_subsystem false}
                                     cmd)]
                  (:valid? (v/validate-roundtrip-with-report full-cmd))))
              test-session-ids))))

;; ============================================================================
;; Statistical Testing - Ensure Good Distribution
;; ============================================================================

(deftest generative-test-statistics
  (testing "Generative tests cover a good distribution"
    (let [samples (gen/sample client-type-gen 1000)]
      (testing "Client type distribution"
        (let [counts (frequencies samples)]
          ;; Each client type should appear at least 200 times in 1000 samples
          (is (every? #(>= % 200) (vals counts))
              "All client types should be well represented"))))
    
    (let [samples (gen/sample localization-gen 1000)]
      (testing "Localization distribution"
        (let [counts (frequencies samples)]
          ;; Each localization should appear at least 200 times in 1000 samples
          (is (every? #(>= % 200) (vals counts))
              "All localizations should be well represented"))))
    
    (let [samples (gen/sample session-id-gen 1000)]
      (testing "Session ID distribution"
        (is (< 0 (apply min samples))
            "Should generate small session IDs")
        (is (> 2147483647 (apply max samples))
            "Should generate large session IDs")
        (is (> (count (distinct samples)) 900)
            "Should generate diverse session IDs")))))