(ns potatoclient.specs.cmd.root-validate-test
  "Tests for cmd.Root buf.validate constraint validation using generated samples.
   Generates samples from Malli specs, converts to protobuf,
   validates with buf.validate, and reports failures."
  (:require
    [clojure.test :refer [deftest is testing use-fixtures]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
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
    (Class/forName "build.buf.protovalidate.ValidatorFactory")
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

;; Proto classes must be compiled before running tests
;; Run 'make compile' or 'clojure -T:build compile-all' first

;; Import test harness to ensure proto classes are compiled
(require '[potatoclient.test-harness :as harness])

;; Force harness initialization - this MUST succeed or tests fail
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize! Proto classes not available."
                  {:initialized? harness/initialized?})))

;; Define Pronto mapper at compile time (proto classes must be available)
(pronto/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

;; Initialize validator
(def validator (delay (.build (build.buf.protovalidate.ValidatorFactory/newBuilder))))

(defn edn->proto
  "Convert EDN map to protobuf message via Pronto."
  [edn-data]
  (try
    (let [proto-map (pronto/clj-map->proto-map cmd-mapper
                                               cmd.JonSharedCmd$Root
                                               edn-data)]
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
      (let [result (.validate @validator proto-msg)]
        (if (.isSuccess result)
          {:valid? true}
          {:valid? false
           :violations (mapv (fn [violation]
                               (let [proto-violation (.toProto violation)]
                                 {:field (str (.getField proto-violation))
                                  :constraint (.getRuleId proto-violation)
                                  :message (.getMessage proto-violation)}))
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