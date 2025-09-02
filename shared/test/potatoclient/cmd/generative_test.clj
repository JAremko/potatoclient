(ns potatoclient.cmd.generative-test
  "Generative tests for command functions using test.check.
   These tests ensure commands are always valid and survive roundtrip serialization."
  (:require
    [clojure.test :refer [deftest is testing]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [clojure.test.check :as tc]
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]
    [potatoclient.cmd.root :as root]
    [potatoclient.cmd.system :as sys]
    [potatoclient.cmd.core :as core]
    [potatoclient.cmd.builder :as builder]
    [potatoclient.cmd.validation :as v]
    [potatoclient.malli.registry :as registry]
    [potatoclient.test-harness :as harness]
    [malli.core :as m]))

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
  "Number of test cases to generate for each property"
  100)

;; ============================================================================
;; Generators
;; ============================================================================

(def session-id-gen
  "Generate valid session IDs (non-negative integers)"
  gen/nat)

(def client-type-gen
  "Generate valid client type enums"
  (gen/elements [:JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                 :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                 :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED]))

(def localization-gen
  "Generate valid localization enums"
  (gen/elements [:JON_GUI_DATA_SYSTEM_LOCALIZATION_EN
                 :JON_GUI_DATA_SYSTEM_LOCALIZATION_UA
                 :JON_GUI_DATA_SYSTEM_LOCALIZATION_AR
                 :JON_GUI_DATA_SYSTEM_LOCALIZATION_CS]))

;; ============================================================================
;; Root Command Properties
;; ============================================================================

(defspec ping-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (root/ping)]
                  (and (= {} (:ping cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec noop-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (root/noop)]
                  (and (= {} (:noop cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec frozen-command-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (root/frozen)]
                  (and (= {} (:frozen cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

;; ============================================================================
;; System Command Properties
;; ============================================================================

(defspec system-reboot-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (sys/reboot)]
                  (and (= {:reboot {}} (:system cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec system-power-off-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (sys/power-off)]
                  (and (= {:power_off {}} (:system cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec system-start-all-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (sys/start-all)]
                  (and (= {:start_all {}} (:system cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec system-stop-all-always-valid num-tests
  (prop/for-all [_ gen/any]
                (let [cmd (sys/stop-all)]
                  (and (= {:stop_all {}} (:system cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

(defspec system-set-localization-with-valid-enum num-tests
  (prop/for-all [loc localization-gen]
                (let [cmd (sys/set-localization loc)]
                  (and (= {:localization {:loc loc}} (:system cmd))
                       (= 1 (:protocol_version cmd))
                       (= :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK (:client_type cmd))
                       (= 0 (:session_id cmd))
                       (= false (:important cmd))
                       (= false (:from_cv_subsystem cmd))
                       (:valid? (v/validate-roundtrip-with-report cmd))))))

;; ============================================================================
;; Core Infrastructure Properties
;; ============================================================================

;; Important flag removed - it's just a default protocol field like others

;; ============================================================================
;; Command Combinations Property
;; ============================================================================

(defspec all-valid-commands-roundtrip num-tests
  (prop/for-all [cmd-fn (gen/elements [#(root/ping)
                                       #(root/noop)
                                       #(root/frozen)
                                       #(sys/reboot)
                                       #(sys/power-off)])]
                (let [cmd (cmd-fn)]
                  (:valid? (v/validate-roundtrip-with-report cmd)))))

;; ============================================================================
;; Protocol Version Properties
;; ============================================================================

(defspec protocol-version-always-positive num-tests
  (prop/for-all [cmd-fn (gen/elements [#(root/ping)
                                       #(root/noop)
                                       #(root/frozen)])]
                (let [cmd (cmd-fn)]
                  (pos? (:protocol_version cmd)))))

;; ============================================================================
;; Session ID Boundary Values
;; ============================================================================

