(ns validate.spec-validation-harness
  "Comprehensive test harness for validating Malli specs against buf.validate.
   Provides round-trip testing: spec -> proto-map -> binary -> proto-map -> spec
   with buf.validate validation at each step."
  (:require
   [clojure.test :refer [is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [pronto.core :as p]
   [validate.validator :as v]
   [validate.test-harness :as h]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [clojure.tools.logging :as log])
  (:import 
   [cmd JonSharedCmd$Root]
   [ser JonSharedData$JonGUIState]))

;; ============================================================================
;; INITIALIZATION
;; ============================================================================

(defn init-registry!
  "Initialize the global Malli registry with all required schemas."
  []
  (registry/setup-global-registry!
    (oneof-edn/register-oneof-edn-schema!)))

;; ============================================================================
;; SPEC TO PROTO-MAP CONVERSION
;; ============================================================================

(defn edn->proto-map
  "Convert Malli-generated EDN map to Pronto proto-map.
   Handles the conversion of kebab-case keys to snake_case."
  [edn-map mapper proto-class]
  (let [snake-case-map (clojure.walk/postwalk
                         (fn [x]
                           (if (keyword? x)
                             (keyword (clojure.string/replace (name x) "-" "_"))
                             x))
                         edn-map)]
    (p/clj-map->proto-map mapper proto-class snake-case-map)))

(defn proto-map->edn
  "Convert Pronto proto-map back to EDN format with kebab-case keys."
  [proto-map]
  (let [snake-map (p/proto-map->clj-map proto-map)
        kebab-map (clojure.walk/postwalk
                   (fn [x]
                     (if (keyword? x)
                       (keyword (clojure.string/replace (name x) "_" "-"))
                       x))
                   snake-map)]
    kebab-map))

;; ============================================================================
;; VALIDATION FUNCTIONS
;; ============================================================================

(defn validate-with-buff
  "Validate a proto-map using buf.validate.
   Returns validation result with details."
  [proto-map]
  (let [binary (p/proto-map->bytes proto-map)
        ;; Determine type based on the proto-map's class
        message-type (cond
                      ;; Check if it's a state message by trying to access state-specific fields
                      (and (:protocol_version proto-map)
                           (:gps proto-map)
                           (:system proto-map))
                      :state
                      
                      ;; Check if it's a command by the presence of :cmd field
                      (and (:protocol_version proto-map)
                           (:client_type proto-map)
                           (:cmd proto-map))
                      :cmd
                      
                      :else :auto)]
    (v/validate-binary binary :type message-type)))

(defn validate-with-spec
  "Validate an EDN map against a Malli spec.
   Returns validation result with explanations."
  [spec-key edn-map]
  (let [valid? (m/validate spec-key edn-map)]
    (if valid?
      {:valid? true
       :message "Spec validation successful"}
      {:valid? false
       :message "Spec validation failed"
       :explanation (me/humanize (m/explain spec-key edn-map))})))

;; ============================================================================
;; ROUND-TRIP VALIDATION
;; ============================================================================

(defn round-trip-validate
  "Perform complete round-trip validation:
   1. Generate data from Malli spec
   2. Convert to proto-map
   3. Validate with buf.validate
   4. Convert to binary
   5. Parse binary back to proto-map
   6. Validate with buf.validate again
   7. Convert back to EDN
   8. Validate with Malli spec
   9. Check equality with original
   
   Returns detailed results at each step."
  [spec-key mapper proto-class generated-edn]
  (try
    (let [;; Step 1: Validate generated EDN with spec
          spec-result-1 (validate-with-spec spec-key generated-edn)
          _ (when-not (:valid? spec-result-1)
              (throw (ex-info "Initial spec validation failed" spec-result-1)))
          
          ;; Step 2: Convert to proto-map
          proto-map-1 (edn->proto-map generated-edn mapper proto-class)
          
          ;; Step 3: Validate proto-map with buf.validate
          buff-result-1 (validate-with-buff proto-map-1)
          _ (when-not (:valid? buff-result-1)
              (throw (ex-info "Initial buf.validate failed" buff-result-1)))
          
          ;; Step 4: Convert to binary
          binary (p/proto-map->bytes proto-map-1)
          
          ;; Step 5: Parse binary back to proto-map
          proto-map-2 (p/bytes->proto-map mapper proto-class binary)
          
          ;; Step 6: Validate parsed proto-map with buf.validate
          buff-result-2 (validate-with-buff proto-map-2)
          _ (when-not (:valid? buff-result-2)
              (throw (ex-info "Post-binary buf.validate failed" buff-result-2)))
          
          ;; Step 7: Convert back to EDN
          edn-map-2 (proto-map->edn proto-map-2)
          
          ;; Step 8: Validate with spec again
          spec-result-2 (validate-with-spec spec-key edn-map-2)
          _ (when-not (:valid? spec-result-2)
              (throw (ex-info "Final spec validation failed" spec-result-2)))
          
          ;; Step 9: Check equality (ignoring order differences)
          equal? (= (set (flatten (seq generated-edn)))
                    (set (flatten (seq edn-map-2))))]
      
      {:success? true
       :steps {:initial-spec spec-result-1
               :initial-buff buff-result-1
               :post-binary-buff buff-result-2
               :final-spec spec-result-2
               :equality equal?}
       :original generated-edn
       :final edn-map-2})
    
    (catch Exception e
      {:success? false
       :error (.getMessage e)
       :exception e})))

;; ============================================================================
;; PROPERTY-BASED TEST RUNNER
;; ============================================================================

(defn run-property-tests
  "Run property-based tests for a spec.
   Generates n samples and validates each through the round-trip process.
   
   Options:
   - :n - number of samples to generate (default 100)
   - :seed - random seed for reproducibility
   - :verbose? - print detailed results for each test
   
   Returns summary with statistics and any failures."
  [spec-key mapper proto-class & {:keys [n seed verbose?]
                                  :or {n 100
                                       verbose? false}}]
  (init-registry!)
  (let [samples (if seed
                  (mg/sample spec-key {:size n :seed seed})
                  (mg/sample spec-key {:size n}))
        results (mapv (fn [idx sample]
                       (let [result (round-trip-validate spec-key mapper proto-class sample)]
                         (when verbose?
                           (println (str "Test " (inc idx) ": " 
                                       (if (:success? result) "✓" "✗"))))
                         (assoc result :index idx :sample sample)))
                     (range n) samples)
        successes (filter :success? results)
        failures (remove :success? results)]
    
    {:total n
     :passed (count successes)
     :failed (count failures)
     :success-rate (float (/ (count successes) n))
     :failures (vec failures)
     :summary (if (empty? failures)
               "All tests passed!"
               (str (count failures) " tests failed. First failure: "
                    (:error (first failures))))}))

;; ============================================================================
;; TEST UTILITIES FOR SPECIFIC MESSAGE TYPES
;; ============================================================================

(defn test-state-spec
  "Test the state root spec with property-based testing."
  [& opts]
  (apply run-property-tests :state/root h/state-mapper 
         ser.JonSharedData$JonGUIState opts))

(defn test-cmd-spec
  "Test the command root spec with property-based testing."
  [& opts]
  (apply run-property-tests :cmd/root h/cmd-mapper 
         cmd.JonSharedCmd$Root opts))

(defn test-sub-message-spec
  "Test a specific sub-message spec (e.g., :state/gps, :cmd/rotary)."
  [spec-key mapper proto-class & opts]
  (apply run-property-tests spec-key mapper proto-class opts))

;; ============================================================================
;; MANUAL VALUE TESTING
;; ============================================================================

(defn test-manual-values
  "Test specific manual values against specs and buf.validate.
   Useful for testing edge cases and known problematic values."
  [spec-key mapper proto-class test-values]
  (init-registry!)
  (mapv (fn [idx value]
          (let [result (round-trip-validate spec-key mapper proto-class value)]
            (assoc result :index idx :test-value value)))
        (range) test-values))

;; ============================================================================
;; VIOLATION ANALYSIS
;; ============================================================================

(defn analyze-violations
  "Analyze validation failures to identify common patterns.
   Helps improve specs to match buf.validate constraints."
  [test-results]
  (let [failures (:failures test-results)
        buff-violations (mapcat (fn [failure]
                                  (when-let [buff-result (get-in failure [:steps :initial-buff])]
                                    (:violations buff-result)))
                               failures)
        violation-counts (frequencies (map :field buff-violations))]
    {:total-failures (count failures)
     :unique-violations (count violation-counts)
     :top-violations (take 10 (sort-by val > violation-counts))
     :violation-details (group-by :field buff-violations)}))

;; ============================================================================
;; SPEC COVERAGE ANALYSIS
;; ============================================================================

(defn check-spec-coverage
  "Check if generated values cover all expected ranges and enum values."
  [spec-key n]
  (init-registry!)
  (let [samples (mg/sample spec-key {:size n})
        paths (atom #{})]
    
    ;; Collect all paths in generated data
    (doseq [sample samples]
      (clojure.walk/postwalk
       (fn [x]
         (when (map? x)
           (swap! paths clojure.set/union (set (keys x))))
         x)
       sample))
    
    {:total-samples n
     :unique-paths (count @paths)
     :paths @paths}))

;; ============================================================================
;; EXAMPLE USAGE
;; ============================================================================

(comment
  ;; Initialize registry
  (init-registry!)
  
  ;; Test state spec with 100 generated samples
  (test-state-spec :n 100 :verbose? true)
  
  ;; Test command spec with specific seed for reproducibility
  (test-cmd-spec :n 50 :seed 42)
  
  ;; Test GPS sub-message
  (test-sub-message-spec :state/gps h/state-mapper 
                        ser.JonSharedData$JonGUIState$GPS
                        :n 200)
  
  ;; Test with manual edge cases
  (test-manual-values :state/gps h/state-mapper 
                     ser.JonSharedData$JonGUIState$GPS
                     [{:latitude 90.0 :longitude 180.0 :altitude 8848.86
                       :fix-type :jon-gui-data-gps-fix-type-3d
                       :manual-latitude 0.0 :manual-longitude 0.0}])
  
  ;; Analyze failures
  (-> (test-state-spec :n 1000)
      analyze-violations)
  
  ;; Check spec coverage
  (check-spec-coverage :state/root 100)
  )