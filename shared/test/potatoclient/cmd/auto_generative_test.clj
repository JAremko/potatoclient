(ns potatoclient.cmd.auto-generative-test
  "Automatic generative testing for all functions with Malli specs.
   Discovers functions, generates inputs from their specs, and validates outputs.
   Also validates that generated commands pass protobuf serialization (buf validate)."
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   [potatoclient.test-harness :as harness]
   [potatoclient.validation-harness :as val-harness]
   [potatoclient.cmd.validation :as validation]
   [clojure.string :as str]))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!" 
                  {:initialized? harness/initialized?})))

;; Initialize registry
(registry/setup-global-registry!)

;; Ensure validation systems are working before we test
(val-harness/ensure-validation-working!)

;; ============================================================================
;; Auto-discovery and Testing
;; ============================================================================

(def cmd-namespaces
  "List of all cmd namespaces to test"
  ['potatoclient.cmd.root
   'potatoclient.cmd.system
   'potatoclient.cmd.compass
   'potatoclient.cmd.gps
   'potatoclient.cmd.lrf
   'potatoclient.cmd.rotary
   'potatoclient.cmd.cv
   'potatoclient.cmd.osd
   'potatoclient.cmd.day-camera
   'potatoclient.cmd.heat-camera
   'potatoclient.cmd.cam-day-glass-heater
   'potatoclient.cmd.lrf-alignment])

(def num-tests 10)
(def generator-opts {:size 10})

(defn extract-arg-schemas
  "Extract argument schemas from a :cat or :catn schema."
  [[tag & args]]
  (case tag
    :cat args
    :catn (mapv second (partition 2 args))
    []))

(defn test-function-with-spec
  "Test a single function by generating inputs from its spec."
  [f-var f-name schema]
  (try
    (let [f (deref f-var)]
      (when (fn? f)
        (let [[arrow & parts] schema]
          (when (= arrow :=>)
            (let [[input-schema output-schema] parts
                  arg-schemas (extract-arg-schemas input-schema)]
              ;; Generate and test multiple times
              (doseq [i (range num-tests)]
                (let [seed (+ 42 i (* 1000 (hash f-name)))
                      args (try
                             (mapv #(mg/generate % (assoc generator-opts :seed seed))
                                   arg-schemas)
                             (catch Exception e
                               (throw (ex-info (str "Failed to generate args for " f-name)
                                               {:schema arg-schemas
                                                :error (.getMessage e)}))))
                      result (try
                               (apply f args)
                               (catch Exception e
                                 (throw (ex-info (str "Function " f-name " threw exception")
                                                 {:args args
                                                  :error (.getMessage e)}))))]
                  ;; Validate output against Malli schema
                  (when-not (m/validate output-schema result)
                    (throw (ex-info (str "Output validation failed for " f-name)
                                    {:args args
                                     :result result
                                     :explain (me/humanize (m/explain output-schema result))})))
                  ;; Validate protobuf serialization (buf validate equivalent)
                  ;; This ensures the command is valid according to protobuf constraints
                  (when (= output-schema :cmd/root)
                    (let [roundtrip-result (validation/validate-roundtrip-with-report result)]
                      (when-not (:valid? roundtrip-result)
                        (throw (ex-info (str "Protobuf validation failed for " f-name)
                                        {:args args
                                         :result result
                                         :error "Command failed protobuf roundtrip validation"
                                         :diff (:pretty-diff roundtrip-result)}))))))))
            {:function f-name
             :tested true
             :num-tests num-tests}))))
    (catch Exception e
      {:function f-name
       :tested false
       :error (.getMessage e)
       :data (ex-data e)})))

(defn discover-and-test-namespace
  "Discover all functions with Malli specs in a namespace and test them."
  [ns-sym]
  ;; Load the namespace
  (require ns-sym)
  (let [ns-obj (find-ns ns-sym)
        public-vars (ns-publics ns-obj)
        results (for [[var-name var-obj] public-vars
                      :let [metadata (meta var-obj)
                            schema (:malli/schema metadata)]
                      :when schema]
                  (let [full-name (symbol (str ns-sym) (str var-name))]
                    (test-function-with-spec var-obj full-name schema)))]
    (filter some? results)))

(deftest auto-discover-and-test-all-cmd-functions
  (testing "Automatically discover and test all cmd functions with Malli specs"
    (let [all-results (mapcat discover-and-test-namespace cmd-namespaces)
          successful (filter :tested all-results)
          failed (remove :tested all-results)]
      
      ;; Report summary
      (println "\n=== Generative Testing Summary ===")
      (println (str "Tested " (count successful) " functions successfully"))
      (println (str "Each function tested " num-tests " times with:"))
      (println "  - Malli schema validation")
      (println "  - Protobuf serialization validation (buf validate)")
      (println (str "Total test cases: " (* (count successful) num-tests)))
      
      ;; Report successful namespaces
      (let [by-ns (group-by #(namespace (:function %)) successful)]
        (doseq [[ns funcs] (sort-by key by-ns)]
          (println (str "\n" ns ": " (count funcs) " functions tested"))))
      
      ;; Check for failures
      (when (seq failed)
        (println "\n=== FAILURES ===")
        (doseq [{:keys [function error data]} failed]
          (println (str "\n" function ":"))
          (println (str "  Error: " error))
          (when data
            (println (str "  Data: " (pr-str data))))))
      
      ;; Assert all passed
      (is (empty? failed)
          (str "All functions should pass generative testing. "
               (count failed) " failures out of " (count all-results) " functions")))))

;; ============================================================================
;; Focused testing for specific patterns
;; ============================================================================

(deftest parameterless-functions-test
  (testing "All parameterless command functions produce valid cmd/root"
    (let [results (for [ns-sym cmd-namespaces]
                   (do
                     (require ns-sym)
                     (let [ns-obj (find-ns ns-sym)]
                       (for [[var-name var-obj] (ns-publics ns-obj)
                             :let [metadata (meta var-obj)
                                   schema (:malli/schema metadata)]
                             :when (and schema
                                       (= :=> (first schema))
                                       (= [:cat] (second schema)))]
                         (let [f (deref var-obj)
                               full-name (symbol (str ns-sym) (str var-name))
                               [_ _ output-schema] schema]
                           (try
                             (let [result (f)]
                               (if (m/validate output-schema result)
                                 {:function full-name :valid true :type output-schema}
                                 {:function full-name :valid false
                                  :error (str "Invalid output. Expected: " output-schema 
                                             ", Got type: " (type result))}))
                             (catch Exception e
                               {:function full-name :valid false
                                :error (.getMessage e)})))))))]
      (let [all-results (flatten results)
            failures (remove :valid all-results)]
        (when (seq failures)
          (doseq [{:keys [function error]} failures]
            (is false (str function " failed: " error))))
        (is (empty? failures)
            (str "All parameterless functions should produce valid cmd/root. "
                 (count failures) " failures out of " (count all-results)))))))

(deftest functions-with-simple-args-test
  (testing "Functions with simple arguments work with generated inputs"
    (let [simple-arg-functions
          ;; Find functions that take only primitive arguments
          (for [ns-sym cmd-namespaces
                :let [_ (require ns-sym)
                      ns-obj (find-ns ns-sym)]
                [var-name var-obj] (ns-publics ns-obj)
                :let [metadata (meta var-obj)
                      schema (:malli/schema metadata)]
                :when (and schema
                          (= :=> (first schema))
                          (let [[_ input-schema _] schema
                                [cat-type & args] input-schema]
                            (and (= :cat cat-type)
                                 (seq args)
                                 ;; Only simple schemas
                                 (every? #(or (keyword? %)
                                             (and (vector? %)
                                                  (= :enum (first %))))
                                        args))))]
            [var-obj (symbol (str ns-sym) (str var-name)) schema])]
      
      (doseq [[var-obj full-name schema] simple-arg-functions
              :when var-obj]
        (testing (str "Function: " full-name)
          (let [result (test-function-with-spec var-obj full-name schema)]
            (is (:tested result)
                (str full-name " should be testable with generated inputs. "
                     "Error: " (:error result)))))))))