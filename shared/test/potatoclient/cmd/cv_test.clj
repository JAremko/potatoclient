(ns potatoclient.cmd.cv-test
  "Tests for CV (Computer Vision) command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [potatoclient.cmd.cv :as cv]
   [potatoclient.cmd.validation :as validation]
   [malli.core :as m]
   [malli.instrument :as mi]
   [com.fulcrologic.guardrails.malli.core :as gm]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; ============================================================================
;; Enable instrumentation for generative testing
;; ============================================================================

(defn test-ns-hook
  "Enable Guardrails checking for this namespace"
  []
  (gm/=> true))

;; ============================================================================
;; Tracking Commands Tests
;; ============================================================================

(deftest test-start-track-ndc
  (testing "start-track-ndc creates valid command with NDC coordinates"
    (let [result (cv/start-track-ndc :JON_GUI_DATA_VIDEO_CHANNEL_DAY 
                                     0.5 -0.5 123456789)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_DAY 
             (get-in result [:cv :start_track_ndc :channel])))
      (is (= 0.5 (get-in result [:cv :start_track_ndc :x])))
      (is (= -0.5 (get-in result [:cv :start_track_ndc :y])))
      (is (= 123456789 (get-in result [:cv :start_track_ndc :frame_time])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "start-track-ndc works with HEAT channel"
    (let [result (cv/start-track-ndc :JON_GUI_DATA_VIDEO_CHANNEL_HEAT 
                                     -1.0 1.0 987654321)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_HEAT
             (get-in result [:cv :start_track_ndc :channel])))
      (is (= -1.0 (get-in result [:cv :start_track_ndc :x])))
      (is (= 1.0 (get-in result [:cv :start_track_ndc :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "start-track-ndc works with edge NDC values"
    (let [result (cv/start-track-ndc :JON_GUI_DATA_VIDEO_CHANNEL_DAY 
                                     0.0 0.0 0)]
      (is (m/validate :cmd/root result))
      (is (= 0.0 (get-in result [:cv :start_track_ndc :x])))
      (is (= 0.0 (get-in result [:cv :start_track_ndc :y])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

(deftest test-stop-track
  (testing "stop-track creates valid command"
    (let [result (cv/stop-track)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :stop_track])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Focus Control Tests
;; ============================================================================

(deftest test-set-auto-focus
  (testing "set-auto-focus creates valid command with DAY channel"
    (let [result (cv/set-auto-focus :JON_GUI_DATA_VIDEO_CHANNEL_DAY true)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_DAY 
             (get-in result [:cv :set_auto_focus :channel])))
      (is (= true (get-in result [:cv :set_auto_focus :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "set-auto-focus creates valid command with HEAT channel"
    (let [result (cv/set-auto-focus :JON_GUI_DATA_VIDEO_CHANNEL_HEAT false)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_HEAT
             (get-in result [:cv :set_auto_focus :channel])))
      (is (= false (get-in result [:cv :set_auto_focus :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Vampire Mode Tests
;; ============================================================================

(deftest test-vampire-mode
  (testing "enable-vampire-mode creates valid command"
    (let [result (cv/enable-vampire-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :vampire_mode_enable])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "disable-vampire-mode creates valid command"
    (let [result (cv/disable-vampire-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :vampire_mode_disable])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Stabilization Mode Tests
;; ============================================================================

(deftest test-stabilization-mode
  (testing "enable-stabilization-mode creates valid command"
    (let [result (cv/enable-stabilization-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :stabilization_mode_enable])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "disable-stabilization-mode creates valid command"
    (let [result (cv/disable-stabilization-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :stabilization_mode_disable])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Data Dump Commands Tests
;; ============================================================================

(deftest test-dump-commands
  (testing "start-dump creates valid command"
    (let [result (cv/start-dump)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :dump_start])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "stop-dump creates valid command"
    (let [result (cv/stop-dump)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :dump_stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Recognition Mode Tests
;; ============================================================================

(deftest test-recognition-mode
  (testing "enable-recognition-mode creates valid command"
    (let [result (cv/enable-recognition-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :recognition_mode_enable])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "disable-recognition-mode creates valid command"
    (let [result (cv/disable-recognition-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:cv :recognition_mode_disable])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

(deftest test-cv-functions-generative
  (testing "All CV functions pass generative testing with mi/check"
    ;; Functions with parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 20}
                       [#'cv/start-track-ndc
                        #'cv/set-auto-focus])))
    
    ;; Functions without parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 5}
                       [#'cv/stop-track
                        #'cv/enable-vampire-mode
                        #'cv/disable-vampire-mode
                        #'cv/enable-stabilization-mode
                        #'cv/disable-stabilization-mode
                        #'cv/start-dump
                        #'cv/stop-dump
                        #'cv/enable-recognition-mode
                        #'cv/disable-recognition-mode])))))