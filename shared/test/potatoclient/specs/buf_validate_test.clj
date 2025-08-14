(ns potatoclient.specs.buf-validate-test
  "Tests for buf.validate constraint validation using generated samples.
   Generates 1000 samples from Malli specs, converts to protobuf,
   validates with buf.validate, and reports failures."
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.core.reducers :as r]
   [clojure.java.data :as java-data]
   [malli.core :as m]
   [malli.generator :as mg]
   [pronto.core :as pronto]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.state.root]))

;; Test fixture to ensure environment is ready
(defn ensure-test-env [f]
  ;; Ensure proto classes are available
  (try
    (Class/forName "ser.JonSharedData$JonGUIState")
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

;; Now we can safely create the mapper since classes are guaranteed to exist
;; But we still need to handle the compile-time class resolution issue
;; We'll use a different approach - create mapper inside a function

(defn create-state-mapper
  "Create mapper at runtime after ensuring classes are loaded."
  []
  ;; First verify class is actually available
  (let [proto-class (Class/forName "ser.JonSharedData$JonGUIState")]
    ;; Since defmapper is a macro that needs compile-time class resolution,
    ;; we need a different approach. Let's use the mapper protocol directly.
    ;; Actually, looking at pronto source, we can use proto-map directly
    {:proto-class proto-class}))

(def state-mapper-info (delay (create-state-mapper)))

(defn edn->proto
  "Convert EDN map to protobuf message via Pronto.
   This skips the mapper and uses proto-map directly."
  [edn-data]
  (try
    ;; Get the proto class (force will throw if harness failed)
    (let [proto-class (:proto-class @state-mapper-info)
          ;; Create a proto-map directly without defmapper
          ;; The proto-map function can work without a pre-defined mapper
          proto-map (pronto/proto-map proto-class edn-data)
          ;; Convert proto-map to bytes
          proto-bytes (pronto/proto-map->bytes proto-map)
          ;; Parse bytes to get Java proto object for validation
          parse-method (.getMethod proto-class "parseFrom" (into-array Class [bytes]))]
      ;; Invoke the static parseFrom method
      (.invoke parse-method nil (object-array [proto-bytes])))
    (catch Exception e
      {:error :conversion-failed
       :message (.getMessage e)
       :data edn-data})))

(defn validate-proto
  "Validate a protobuf message with buf.validate.
   Returns a validation report as EDN."
  [proto-msg]
  (if (map? proto-msg) ; Error from conversion
    proto-msg
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

(deftest buf-validate-state-samples
  (testing "Generated state samples should pass buf.validate constraints"
    (let [;; Generate 1000 samples from the state spec
          samples (try
                   (mg/sample (m/schema :state/root) {:size 1000})
                   (catch Exception e
                     (throw (ex-info "Failed to generate samples"
                                     {:error (.getMessage e)}))))

          ;; Process samples through validation pipeline
          ;; Using simple map for now instead of reducers
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
        (println (format "Found %d failures out of 1000 samples (showing first %d):"
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
                                         [:protocol_version :gps :system])))))
        (println "\n========================================="))

      ;; Test assertion
      (is (empty? failures)
          (format "Expected all samples to pass buf.validate, but %d failed"
                 failure-count)))))

(deftest buf-validate-specific-constraints
  (testing "Specific buf.validate constraints"
    (testing "GPS latitude must be within [-90, 90]"
      (let [invalid-sample {:protocol_version 1
                            :gps {:latitude 91.0  ; Invalid
                                  :longitude 0.0
                                  :altitude 0.0
                                  :manual_latitude 0.0
                                  :manual_longitude 0.0
                                  :manual_altitude 0.0
                                  :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                                  :use_manual false}}
            proto (edn->proto invalid-sample)
            result (validate-proto proto)]
        (is (false? (:valid? result))
            "Should fail validation for latitude > 90")
        (is (some #(re-find #"latitude" (:field %)) (:violations result))
            "Should have violation for latitude field")))

    (testing "Azimuth must be within [0, 360)"
      (let [invalid-sample {:protocol_version 1
                            :compass {:azimuth 360.0  ; Invalid (must be < 360)
                                      :elevation 0.0
                                      :bank 0.0
                                      :offsetAzimuth 0.0
                                      :offsetElevation 0.0
                                      :magneticDeclination 0.0
                                      :calibrating false}}
            proto (edn->proto invalid-sample)
            result (validate-proto proto)]
        (is (false? (:valid? result))
            "Should fail validation for azimuth >= 360")
        (is (some #(re-find #"azimuth" (:field %)) (:violations result))
            "Should have violation for azimuth field")))))
