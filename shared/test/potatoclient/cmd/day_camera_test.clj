(ns potatoclient.cmd.day-camera-test
  "Tests for Day Camera command functions."
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [potatoclient.cmd.day-camera :as day-camera]
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
;; Infra-Red Filter Control Tests
;; ============================================================================

(deftest test-infra-red-filter
  (testing "set-infra-red-filter creates valid command"
    (let [result (day-camera/set-infra-red-filter true)]
      (is (m/validate :cmd/root result))
      (is (= true (get-in result [:day_camera :set_infra_red_filter :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))
    
    (let [result (day-camera/set-infra-red-filter false)]
      (is (m/validate :cmd/root result))
      (is (= false (get-in result [:day_camera :set_infra_red_filter :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Iris Control Tests
;; ============================================================================

(deftest test-iris-control
  (testing "set-iris creates valid command"
    (let [result (day-camera/set-iris 0.5)]
      (is (m/validate :cmd/root result))
      (is (= 0.5 (get-in result [:day_camera :set_iris :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "set-auto-iris creates valid command"
    (let [result (day-camera/set-auto-iris true)]
      (is (m/validate :cmd/root result))
      (is (= true (get-in result [:day_camera :set_auto_iris :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Photo and Camera Control Tests
;; ============================================================================

(deftest test-camera-control
  (testing "take-photo creates valid command"
    (let [result (day-camera/take-photo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :photo])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "start creates valid command"
    (let [result (day-camera/start)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :start])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "stop creates valid command"
    (let [result (day-camera/stop)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :stop])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "halt-all creates valid command"
    (let [result (day-camera/halt-all)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :halt_all])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Focus Control Tests
;; ============================================================================

(deftest test-focus-control
  (testing "set-focus creates valid command"
    (let [result (day-camera/set-focus 0.75)]
      (is (m/validate :cmd/root result))
      (is (= 0.75 (get-in result [:day_camera :focus :set_value :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "move-focus creates valid command"
    (let [result (day-camera/move-focus 0.8 0.5)]
      (is (m/validate :cmd/root result))
      (is (= 0.8 (get-in result [:day_camera :focus :move :target_value])))
      (is (= 0.5 (get-in result [:day_camera :focus :move :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "halt-focus creates valid command"
    (let [result (day-camera/halt-focus)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :focus :halt])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "offset-focus creates valid command"
    (let [result (day-camera/offset-focus -0.2)]
      (is (m/validate :cmd/root result))
      (is (= -0.2 (get-in result [:day_camera :focus :offset :offset_value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "reset-focus creates valid command"
    (let [result (day-camera/reset-focus)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :focus :reset_focus])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "save-focus-to-table creates valid command"
    (let [result (day-camera/save-focus-to-table)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :focus :save_to_table_focus])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Zoom Control Tests
;; ============================================================================

(deftest test-zoom-control
  (testing "set-zoom creates valid command"
    (let [result (day-camera/set-zoom 0.5)]
      (is (m/validate :cmd/root result))
      (is (= 0.5 (get-in result [:day_camera :zoom :set_value :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "move-zoom creates valid command"
    (let [result (day-camera/move-zoom 0.9 0.3)]
      (is (m/validate :cmd/root result))
      (is (= 0.9 (get-in result [:day_camera :zoom :move :target_value])))
      (is (= 0.3 (get-in result [:day_camera :zoom :move :speed])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "halt-zoom creates valid command"
    (let [result (day-camera/halt-zoom)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :zoom :halt])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "offset-zoom creates valid command"
    (let [result (day-camera/offset-zoom 0.1)]
      (is (m/validate :cmd/root result))
      (is (= 0.1 (get-in result [:day_camera :zoom :offset :offset_value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "reset-zoom creates valid command"
    (let [result (day-camera/reset-zoom)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :zoom :reset_zoom])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "save-zoom-to-table creates valid command"
    (let [result (day-camera/save-zoom-to-table)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :zoom :save_to_table])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "set-zoom-table-value creates valid command"
    (let [result (day-camera/set-zoom-table-value 10)]
      (is (m/validate :cmd/root result))
      (is (= 10 (get-in result [:day_camera :zoom :set_zoom_table_value :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "next-zoom-table-pos creates valid command"
    (let [result (day-camera/next-zoom-table-pos)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :zoom :next_zoom_table_pos])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "prev-zoom-table-pos creates valid command"
    (let [result (day-camera/prev-zoom-table-pos)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :zoom :prev_zoom_table_pos])))
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
    (let [result (day-camera/set-digital-zoom-level 2.0)]
      (is (m/validate :cmd/root result))
      (is (= 2.0 (get-in result [:day_camera :set_digital_zoom_level :value])))
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
    (let [result (day-camera/get-meteo)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :get_meteo])))
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
    (let [result (day-camera/set-fx-mode :JON_GUI_DATA_FX_MODE_DAY_A)]
      (is (m/validate :cmd/root result))
      (is (= :JON_GUI_DATA_FX_MODE_DAY_A (get-in result [:day_camera :set_fx_mode :mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "next-fx-mode creates valid command"
    (let [result (day-camera/next-fx-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :next_fx_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "prev-fx-mode creates valid command"
    (let [result (day-camera/prev-fx-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :prev_fx_mode])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "refresh-fx-mode creates valid command"
    (let [result (day-camera/refresh-fx-mode)]
      (is (m/validate :cmd/root result))
      (is (= {} (get-in result [:day_camera :refresh_fx_mode])))
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
    (let [result (day-camera/set-clahe-level 0.7)]
      (is (m/validate :cmd/root result))
      (is (= 0.7 (get-in result [:day_camera :set_clahe_level :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result))))))))
  
  (testing "shift-clahe-level creates valid command"
    (let [result (day-camera/shift-clahe-level -0.3)]
      (is (m/validate :cmd/root result))
      (is (= -0.3 (get-in result [:day_camera :shift_clahe_level :value])))
      (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
        (is (:valid? roundtrip-result) 
            (str "Should pass roundtrip validation" 
                 (when-not (:valid? roundtrip-result) 
                   (str "\nDiff:\n" (:pretty-diff roundtrip-result)))))))))

;; ============================================================================
;; Generative Testing
;; ============================================================================

(deftest test-day-camera-functions-generative
  (testing "All Day Camera functions pass generative testing with mi/check"
    ;; Functions with parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 20}
                       [#'day-camera/set-infra-red-filter
                        #'day-camera/set-iris
                        #'day-camera/set-auto-iris
                        #'day-camera/set-focus
                        #'day-camera/move-focus
                        #'day-camera/offset-focus
                        #'day-camera/set-zoom
                        #'day-camera/move-zoom
                        #'day-camera/offset-zoom
                        #'day-camera/set-zoom-table-value
                        #'day-camera/set-digital-zoom-level
                        #'day-camera/set-fx-mode
                        #'day-camera/set-clahe-level
                        #'day-camera/shift-clahe-level])))
    
    ;; Functions without parameters
    (is (nil? (mi/check {:filters [(gm/=>)]
                        :num-tests 5}
                       [#'day-camera/take-photo
                        #'day-camera/start
                        #'day-camera/stop
                        #'day-camera/halt-all
                        #'day-camera/halt-focus
                        #'day-camera/reset-focus
                        #'day-camera/save-focus-to-table
                        #'day-camera/halt-zoom
                        #'day-camera/reset-zoom
                        #'day-camera/save-zoom-to-table
                        #'day-camera/next-zoom-table-pos
                        #'day-camera/prev-zoom-table-pos
                        #'day-camera/get-meteo
                        #'day-camera/next-fx-mode
                        #'day-camera/prev-fx-mode
                        #'day-camera/refresh-fx-mode])))))