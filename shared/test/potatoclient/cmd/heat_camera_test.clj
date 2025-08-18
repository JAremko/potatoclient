(ns potatoclient.cmd.heat-camera-test
  "Tests for Heat Camera (thermal imaging) command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [potatoclient.cmd.heat-camera :as heat-camera]
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
;; Photo Control Tests
;; ============================================================================

(deftest test-photo-control
  (testing "take-photo creates valid command"
    (let [result (heat-camera/take-photo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :photo])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; AGC and Filter Settings Tests
;; ============================================================================

(deftest test-agc-and-filter
  (testing "set-agc creates valid command"
    (let [result (heat-camera/set-agc :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1 
             (get-in result [:heat_camera :set_agc :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "set-filter creates valid command"
    (let [result (heat-camera/set-filter :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
             (get-in result [:heat_camera :set_filter :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Camera Control Tests
;; ============================================================================

(deftest test-camera-control
  (testing "calibrate creates valid command"
    (let [result (heat-camera/calibrate)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :calibrate])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "start creates valid command"
    (let [result (heat-camera/start)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :start])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "stop creates valid command"
    (let [result (heat-camera/stop)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "set-calib-mode creates valid command"
    (let [result (heat-camera/set-calib-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :set_calib_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Zoom Control Tests
;; ============================================================================

(deftest test-zoom-control
  (testing "zoom-in creates valid command"
    (let [result (heat-camera/zoom-in)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :zoom_in])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "zoom-out creates valid command"
    (let [result (heat-camera/zoom-out)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :zoom_out])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "zoom-stop creates valid command"
    (let [result (heat-camera/zoom-stop)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :zoom_stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "reset-zoom creates valid command"
    (let [result (heat-camera/reset-zoom)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :reset_zoom])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "save-zoom-to-table creates valid command"
    (let [result (heat-camera/save-zoom-to-table)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :save_to_table])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Zoom Table Operations Tests
;; ============================================================================

(deftest test-zoom-table-operations
  (testing "set-zoom-table-value creates valid command"
    (let [result (heat-camera/set-zoom-table-value 25)]
      (is (m/validate :cmd/root result))
      (is (= 25 (get-in result [:heat_camera :zoom :set_zoom_table_value :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "next-zoom-table-pos creates valid command"
    (let [result (heat-camera/next-zoom-table-pos)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :zoom :next_zoom_table_pos])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "prev-zoom-table-pos creates valid command"
    (let [result (heat-camera/prev-zoom-table-pos)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :zoom :prev_zoom_table_pos])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Digital Zoom Tests
;; ============================================================================

(deftest test-digital-zoom
  (testing "set-digital-zoom-level creates valid command"
    (let [result (heat-camera/set-digital-zoom-level 2.5)]
      (is (m/validate :cmd/root result))
      (is (= 2.5 (get-in result [:heat_camera :set_digital_zoom_level :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Focus Control Tests
;; ============================================================================

(deftest test-focus-control
  (testing "set-auto-focus creates valid command"
    (let [result (heat-camera/set-auto-focus true)]
      (is (m/validate :cmd/root result))
      (is (= true (get-in result [:heat_camera :set_auto_focus :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "focus-stop creates valid command"
    (let [result (heat-camera/focus-stop)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :focus_stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "focus-in creates valid command"
    (let [result (heat-camera/focus-in)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :focus_in])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "focus-out creates valid command"
    (let [result (heat-camera/focus-out)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :focus_out])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "focus-step-plus creates valid command"
    (let [result (heat-camera/focus-step-plus)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :focus_step_plus])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "focus-step-minus creates valid command"
    (let [result (heat-camera/focus-step-minus)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :focus_step_minus])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Meteo Data Tests
;; ============================================================================

(deftest test-meteo
  (testing "get-meteo creates valid command"
    (let [result (heat-camera/get-meteo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :get_meteo])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; DDE Control Tests
;; ============================================================================

(deftest test-dde-control
  (testing "enable-dde creates valid command"
    (let [result (heat-camera/enable-dde)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :enable_dde])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "disable-dde creates valid command"
    (let [result (heat-camera/disable-dde)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :disable_dde])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "set-dde-level creates valid command"
    (let [result (heat-camera/set-dde-level 50)]
      (is (m/validate :cmd/root result))
      (is (= 50 (get-in result [:heat_camera :set_dde_level :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "shift-dde creates valid command"
    (let [result (heat-camera/shift-dde -25)]
      (is (m/validate :cmd/root result))
      (is (= -25 (get-in result [:heat_camera :shift_dde :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; FX Mode Control Tests
;; ============================================================================

(deftest test-fx-mode
  (testing "set-fx-mode creates valid command"
    (let [result (heat-camera/set-fx-mode :JON_GUI_DATA_FX_MODE_HEAT_A)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_FX_MODE_HEAT_A (get-in result [:heat_camera :set_fx_mode :mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "next-fx-mode creates valid command"
    (let [result (heat-camera/next-fx-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :next_fx_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "prev-fx-mode creates valid command"
    (let [result (heat-camera/prev-fx-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :prev_fx_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "refresh-fx-mode creates valid command"
    (let [result (heat-camera/refresh-fx-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:heat_camera :refresh_fx_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; CLAHE Control Tests
;; ============================================================================

(deftest test-clahe-control
  (testing "set-clahe-level creates valid command"
    (let [result (heat-camera/set-clahe-level 0.8)]
      (is (m/validate :cmd/root result))
      (is (= 0.8 (get-in result [:heat_camera :set_clahe_level :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "shift-clahe-level creates valid command"
    (let [result (heat-camera/shift-clahe-level -0.2)]
      (is (m/validate :cmd/root result))
      (is (= -0.2 (get-in result [:heat_camera :shift_clahe_level :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

(deftest test-heat-camera-functions-generative
  (testing "All Heat Camera functions pass generative testing with mi/check"
    ;; Functions with parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 20}
                       [#'heat-camera/set-agc
                        #'heat-camera/set-filter
                        #'heat-camera/set-zoom-table-value
                        #'heat-camera/set-digital-zoom-level
                        #'heat-camera/set-auto-focus
                        #'heat-camera/set-dde-level
                        #'heat-camera/shift-dde
                        #'heat-camera/set-fx-mode
                        #'heat-camera/set-clahe-level
                        #'heat-camera/shift-clahe-level])))
    
    ;; Functions without parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 5}
                       [#'heat-camera/take-photo
                        #'heat-camera/calibrate
                        #'heat-camera/start
                        #'heat-camera/stop
                        #'heat-camera/set-calib-mode
                        #'heat-camera/zoom-in
                        #'heat-camera/zoom-out
                        #'heat-camera/zoom-stop
                        #'heat-camera/reset-zoom
                        #'heat-camera/save-zoom-to-table
                        #'heat-camera/next-zoom-table-pos
                        #'heat-camera/prev-zoom-table-pos
                        #'heat-camera/focus-stop
                        #'heat-camera/focus-in
                        #'heat-camera/focus-out
                        #'heat-camera/focus-step-plus
                        #'heat-camera/focus-step-minus
                        #'heat-camera/get-meteo
                        #'heat-camera/enable-dde
                        #'heat-camera/disable-dde
                        #'heat-camera/next-fx-mode
                        #'heat-camera/prev-fx-mode
                        #'heat-camera/refresh-fx-mode])))))