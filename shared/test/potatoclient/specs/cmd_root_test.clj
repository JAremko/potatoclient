(ns potatoclient.specs.cmd-root-test
  "Tests for cmd.Root buf.validate constraint validation using generated samples.
   Generates samples from Malli specs, converts to protobuf,
   validates with buf.validate, and reports failures."
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.core.reducers :as r]
   [clojure.java.data :as java-data]
   [malli.core :as m]
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

(deftest buf-validate-cmd-specific-constraints
  (testing "Specific buf.validate constraints for cmd messages"
    (testing "Protocol version must be >= 1"
      (let [invalid-sample {:protocol_version 0  ; Invalid
                            :session_id 1
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                            :ping {}}  ; No :payload wrapper
            _ (println "\nTesting protocol_version constraint:")
            _ (println "Invalid sample:" invalid-sample)
            proto (edn->proto invalid-sample)
            _ (println "Proto result:" proto)
            result (validate-proto proto)
            _ (println "Validation result:" result)]
        (is (false? (:valid? result))
            "Should fail validation for protocol_version < 1")
        (is (some #(re-find #"protocol_version" (:field %)) (:violations result))
            "Should have violation for protocol_version field")))

    (testing "Session ID must be >= 1"
      (let [invalid-sample {:protocol_version 1
                            :session_id 0  ; Invalid
                            :important false
                            :from_cv_subsystem false
                            :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                            :noop {}}
            proto (edn->proto invalid-sample)
            result (validate-proto proto)]
        (is (false? (:valid? result))
            "Should fail validation for session_id < 1")
        (is (some #(re-find #"session_id" (:field %)) (:violations result))
            "Should have violation for session_id field")))

    (testing "GPS coordinates validation in cmd"
      (let [invalid-sample {:protocol_version 1
                            :session_id 1
                            :important false
                            :from_cv_subsystem false
                            :client_type :CLIENT_TYPE_COELACANTH
                            :payload {:gps {:start {:latitude 91.0  ; Invalid (> 90)
                                                   :longitude 0.0
                                                   :altitude 0.0
                                                   :fix_type :JON_CMD_GPS_FIX_TYPE_3D
                                                   :gps_source :JON_CMD_GPS_SOURCE_INTERNAL}}}}
            proto (edn->proto invalid-sample)
            result (validate-proto proto)]
        (is (false? (:valid? result))
            "Should fail validation for GPS latitude > 90")
        (is (some #(re-find #"latitude" (:field %)) (:violations result))
            "Should have violation for latitude field")))

    (testing "Compass azimuth validation in cmd"
      (let [invalid-sample {:protocol_version 1
                            :session_id 1
                            :important false
                            :from_cv_subsystem false
                            :client_type :CLIENT_TYPE_COELACANTH
                            :payload {:compass {:start {:azimuth 360.0  ; Invalid (must be < 360)
                                                       :elevation 0.0
                                                       :bank 0.0
                                                       :magnetic_declination 0.0
                                                       :compass_source :JON_CMD_COMPASS_SOURCE_INTERNAL}}}}
            proto (edn->proto invalid-sample)
            result (validate-proto proto)]
        (is (false? (:valid? result))
            "Should fail validation for compass azimuth >= 360")
        (is (some #(re-find #"azimuth" (:field %)) (:violations result))
            "Should have violation for azimuth field")))

    (testing "Day camera zoom constraints"
      (let [invalid-sample {:protocol_version 1
                            :session_id 1
                            :important false
                            :from_cv_subsystem false
                            :client_type :CLIENT_TYPE_COELACANTH
                            :payload {:day_camera {:zoom {:zoom_type :DAYC_ZOOM_TYPE_CONTINUOUS
                                                          :optical_zoom -1.0  ; Invalid (< 0)
                                                          :digital_zoom 1.0}}}}
            proto (edn->proto invalid-sample)
            result (validate-proto proto)]
        (is (false? (:valid? result))
            "Should fail validation for negative optical zoom")
        (is (some #(re-find #"optical_zoom" (:field %)) (:violations result))
            "Should have violation for optical_zoom field")))))