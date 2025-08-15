(ns potatoclient.specs.cmd-root-test
  "Tests for cmd.Root buf.validate constraint validation using generated samples.
   Generates samples from Malli specs, converts to protobuf,
   validates with buf.validate, and reports failures."
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.core.reducers :as r]
   [clojure.java.data :as java-data]
   [malli.core :as m]
   [malli.error :as me]
   [malli.generator :as mg]
   [pronto.core :as pronto]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root]))

;; Test fixture to ensure environment is ready
(defn ensure-test-env [f]
  ;; Ensure proto classes are available
  (try
    (Class/forName "cmd.JonSharedCmd$Root")
    (Class/forName "build.buf.protovalidate.Validator")
    (f)
    (catch ClassNotFoundException e
      (println "\n=== Test Environment Not Ready ===")
      (println "Proto classes or buf.validate not available.")
      (println "Please run: clojure -T:build compile-all")
      (println "==================================\n")
      (throw e))))

(use-fixtures :once ensure-test-env)

;; Initialize registry with all specs
(registry/setup-global-registry!)

;; These will be initialized in tests when classes are available
(def validator (atom nil))

(defn init-validator! []
  (when-not @validator
    (let [validator-class (Class/forName "build.buf.protovalidate.Validator")]
      (reset! validator (.newInstance validator-class)))))

;; Import test harness to ensure proto classes are compiled
(require '[potatoclient.test-harness :as harness])

;; Force harness initialization - this MUST succeed or tests fail
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize! Proto classes not available." 
                  {:initialized? harness/initialized?})))

;; Create mapper dynamically using eval after classes are loaded
;; This allows us to work with runtime-loaded proto classes
(def ^:dynamic *cmd-mapper* nil)

;; Initialize the mapper at runtime
(defn init-mapper!
  "Initialize the pronto mapper at runtime."
  []
  (when (nil? *cmd-mapper*)
    (alter-var-root #'*cmd-mapper*
                    (constantly 
                     (eval '(do 
                             (pronto.core/defmapper cmd-mapper-internal 
                                                   [cmd.JonSharedCmd$Root])
                             cmd-mapper-internal))))))

(defn edn->proto
  "Convert EDN map to protobuf message via Pronto."
  [edn-data]
  (try
    ;; Ensure mapper is initialized
    (init-mapper!)
    
    ;; Use eval to work with the runtime-loaded classes and mapper
    (let [proto-class (Class/forName "cmd.JonSharedCmd$Root")
          proto-map (eval `(pronto.core/clj-map->proto-map ~*cmd-mapper* 
                                                           cmd.JonSharedCmd$Root 
                                                           ~edn-data))]
      (pronto/proto-map->proto proto-map))
    (catch Exception e
      {:error :conversion-failed
       :message (.getMessage e)
       :data edn-data})))

(defn validate-proto
  "Validate a protobuf message with buf.validate.
   Returns a validation report as EDN."
  [proto-msg]
  (cond
    ;; If we got an error from conversion, return it
    (and (map? proto-msg) (:error proto-msg))
    proto-msg
    
    ;; If proto-msg is nil, return an error
    (nil? proto-msg)
    {:valid? false
     :error :null-proto
     :message "Proto message is null"}
    
    ;; Otherwise validate the proto
    :else
    (try
      (init-validator!)
      (let [result (.validate @validator proto-msg)]
        (if (.isSuccess result)
          {:valid? true}
          {:valid? false
           :violations (mapv (fn [violation]
                              {:field (.getFieldPath violation)
                               :constraint (.getConstraintId violation)
                               :message (.getMessage violation)})
                            (.getViolations result))}))
      (catch Exception e
        (cond
          (= (.getSimpleName (.getClass e)) "ValidationException")
          {:valid? false
           :error :validation-exception
           :message (.getMessage e)}

          :else
          {:valid? false
           :error :unexpected-error
           :message (.getMessage e)})))))

(defn collect-failures
  "Collect only failed validation reports."
  [reports]
  (into []
        (comp
         ;; Keep only failures
         (filter #(or (contains? % :error)
                     (false? (:valid? %))))
         ;; Take at most 5 to avoid spam
         (take 5))
        reports))

(deftest buf-validate-cmd-root-samples
  (testing "Generated cmd root samples should pass buf.validate constraints"
    ;; Debug the schema first
    (println "Testing cmd/root schema:")
    (let [schema (m/schema :cmd/root)]
      (println "Schema form:" (m/form schema))
      (println "First generated sample:" (mg/generate schema))
      (println "\nStarting validation tests..."))
    
    (let [;; Generate 5000 samples from the cmd root spec
          samples (try
                   (mg/sample (m/schema :cmd/root) {:size 5000})
                   (catch Exception e
                     (throw (ex-info "Failed to generate samples"
                                     {:error (.getMessage e) 
                                      :schema (m/form (m/schema :cmd/root))}))))

          ;; Process samples through validation pipeline
          validation-reports (mapv (fn [sample]
                                     (let [proto (edn->proto sample)
                                           report (validate-proto proto)]
                                       (assoc report :sample sample)))
                                   samples)

          ;; Collect failures
          failures (collect-failures validation-reports)
          failure-count (count (filter #(false? (:valid? %)) validation-reports))]

      ;; Print failure reports (max 5)
      (when (seq failures)
        (println "\n=== buf.validate Validation Failures ===")
        (println (format "Found %d failures out of 5000 samples (showing first %d):"
                        failure-count
                        (min 5 failure-count)))
        (doseq [[idx failure] (map-indexed vector failures)]
          (println (format "\nFailure %d:" (inc idx)))
          (when (:error failure)
            (println "  Error:" (:error failure))
            (println "  Message:" (:message failure)))
          (when (:violations failure)
            (println "  Violations:")
            (doseq [v (:violations failure)]
              (println (format "    - Field: %s" (:field v)))
              (println (format "      Constraint: %s" (:constraint v)))
              (println (format "      Message: %s" (:message v)))))
          (when (and (< idx 2) (:sample failure)) ; Show sample data for first 2
            (println "  Sample data (truncated):")
            (println (pr-str (select-keys (:sample failure)
                                         [:protocol_version :session_id :client_type :payload])))))
        (println "\n========================================="))

      ;; Test assertion
      (is (empty? failures)
          (format "Expected all samples to pass buf.validate, but %d failed"
                 failure-count)))))

(deftest buf-validate-negative-tests
  (testing "Hand-crafted invalid cmd messages"
    (let [cmd-spec (m/schema :cmd/root)
          ;; Generate a valid base sample
          base-sample (mg/generate cmd-spec)]
      
      (testing "Invalid protocol_version (0 - must be > 0)"
        (let [invalid-sample (assoc base-sample :protocol_version 0)]
          (is (not (m/validate cmd-spec invalid-sample))
              "Message with protocol_version=0 should fail Malli validation")
          
          ;; Try proto conversion and buf.validate
          (let [proto (edn->proto invalid-sample)
                result (validate-proto proto)]
            (is (false? (:valid? result))
                "Should fail buf.validate for protocol_version=0")
            (is (some #(re-find #"protocol_version" (:field %)) (:violations result))
                "Should have violation for protocol_version field"))))
      
      (testing "Invalid GPS coordinates in GPS command"
        (when (:gps base-sample)
          (let [invalid-sample (assoc base-sample
                                      :gps {:set_manual_position
                                            {:latitude 91.0     ; Invalid: > 90
                                             :longitude 181.0   ; Invalid: > 180  
                                             :altitude -500.0}})] ; Invalid: < -430
            (is (not (m/validate cmd-spec invalid-sample))
                "Message with invalid GPS coordinates should fail")
            
            (when-let [explanation (m/explain cmd-spec invalid-sample)]
              (let [errors (:errors explanation)]
                (is (some #(re-find #"latitude" (str %)) errors)
                    "Should have error for invalid latitude"))))))
      
      (testing "Invalid compass angles"
        (when (:compass base-sample)
          (let [invalid-sample (assoc base-sample
                                      :compass {:set_offset_angle_azimuth
                                               {:value 180.0}})]  ; Invalid: must be < 180
            (is (not (m/validate cmd-spec invalid-sample))
                "Message with invalid compass offset should fail"))))
      
      (testing "Invalid rotary relative angles"
        (when (:rotary base-sample)
          (let [invalid-sample (assoc base-sample
                                      :rotary {:axis {:azimuth {:relative
                                                                {:value 181.0  ; Invalid: > 180
                                                                 :speed 0.5
                                                                 :direction :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE}}
                                                     :elevation {:relative
                                                                {:value 91.0  ; Invalid: > 90
                                                                 :speed 0.5
                                                                 :direction :JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE}}}})]
            (is (not (m/validate cmd-spec invalid-sample))
                "Message with invalid rotary relative angles should fail"))))
      
      (testing "Multiple oneof fields present (invalid)"
        (let [invalid-sample (-> base-sample
                                 (assoc :ping {})
                                 (assoc :noop {}))]
          (is (not (m/validate cmd-spec invalid-sample))
              "Message with multiple oneof fields should fail")))
      
      (testing "No oneof fields present (invalid)"  
        (let [invalid-sample (-> base-sample
                                 (dissoc :ping :noop :system :gps :compass :lrf 
                                        :lrf_calib :rotary :osd :cv :lira :frozen
                                        :day_camera :heat_camera :day_cam_glass_heater))]
          (is (not (m/validate cmd-spec invalid-sample))
              "Message with no oneof fields should fail")))))
  
  (testing "Sanity checks for valid messages"
    (let [cmd-spec (m/schema :cmd/root)]
      
      (testing "Valid ping command"
        (let [valid-sample {:protocol_version 1
                            :session_id 123
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                            :ping {}}]
          (is (m/validate cmd-spec valid-sample)
              "Valid ping command should pass")
          
          (let [proto (edn->proto valid-sample)
                result (validate-proto proto)]
            (is (:valid? result)
                "Should pass buf.validate"))))
      
      (testing "Valid system command"
        (let [valid-sample {:protocol_version 1
                            :session_id 456
                            :important true
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                            :system {:localization {:loc :JON_GUI_DATA_SYSTEM_LOCALIZATION_EN}}}]
          (is (m/validate cmd-spec valid-sample)
              "Valid system command should pass")))))
  
  (testing "Edge cases"
    (let [cmd-spec (m/schema :cmd/root)]
      
      (testing "Maximum valid protocol_version"
        (let [valid-sample {:protocol_version 2147483647
                            :session_id 0
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LIRA
                            :noop {}}]
          (is (m/validate cmd-spec valid-sample)
              "Max protocol_version should be valid")))
      
      (testing "Minimum valid session_id (0 is valid)"
        (let [valid-sample {:protocol_version 1
                            :session_id 0
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LIRA
                            :frozen {}}]
          (is (m/validate cmd-spec valid-sample)
              "session_id=0 should be valid"))))))

