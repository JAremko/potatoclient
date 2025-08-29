(ns potatoclient.cmd.cv-test
  "Tests for CV (Computer Vision) command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.cv :as cv]
   [potatoclient.cmd.validation :as validation]
   [malli.core :as m]
   [malli.instrument :as mi]
   [potatoclient.test-harness :as harness]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; ============================================================================


;; ============================================================================
;; Tracking Commands Tests
;; ============================================================================

(deftest test-start-track-ndc
  (testing "start-track-ndc creates valid command with NDC coordinates"
    (let [result (cv/start-track-ndc :JON_GUI_DATA_VIDEO_CHANNEL_DAY 
                                     0.5 -0.5 123456789)]
      (is (m/validate :cmd/root result))
      (is (match? {:cv {:start_track_ndc {:channel :JON_GUI_DATA_VIDEO_CHANNEL_DAY
                                           :x 0.5
                                           :y -0.5
                                           :frame_time 123456789}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "start-track-ndc works with HEAT channel"
    (let [result (cv/start-track-ndc :JON_GUI_DATA_VIDEO_CHANNEL_HEAT 
                                     -1.0 1.0 987654321)]
      (is (m/validate :cmd/root result))
      (is (match? {:cv {:start_track_ndc {:channel :JON_GUI_DATA_VIDEO_CHANNEL_HEAT
                                           :x -1.0
                                           :y 1.0}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "start-track-ndc works with edge NDC values"
    (let [result (cv/start-track-ndc :JON_GUI_DATA_VIDEO_CHANNEL_DAY 
                                     0.0 0.0 0)]
      (is (m/validate :cmd/root result))
      (is (match? {:cv {:start_track_ndc {:x 0.0
                                           :y 0.0}}}
                  result))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

(deftest test-stop-track
  (testing "stop-track creates valid command"
    (let [result (cv/stop-track)]
      (is (m/validate :cmd/root result))
      (is (match? {:cv {:stop_track {}}}
                  result))
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
      (is (match? {:cv {:set_auto_focus {:channel :JON_GUI_DATA_VIDEO_CHANNEL_DAY
                                          :value true}}}
                  result))
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

