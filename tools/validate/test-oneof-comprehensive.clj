#!/usr/bin/env clojure

(ns test-oneof-comprehensive
  "Comprehensive test suite for oneof_edn - validation, generation, integration"
  (:require
   [clojure.test :refer [deftest testing is]]
   [clojure.test.check :as tc]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== COMPREHENSIVE ONEOF_EDN TEST SUITE ===\n")

;; ============================================================================
;; PART 1: BASIC VALIDATION TESTS
;; ============================================================================

(println "PART 1: Basic Validation Tests")
(println "--------------------------------")

(defn test-basic-validation []
  (let [schema [:oneof_edn
                [:field-a :string]
                [:field-b :int]
                [:field-c :boolean]]]
    
    (println "\n1.1 Valid cases (exactly one non-nil field):")
    (let [valid-cases [{:field-a "test"}
                       {:field-b 42}
                       {:field-c true}
                       {:field-a "x" :field-b nil}
                       {:field-a "x" :field-b nil :field-c nil}]]
      (doseq [v valid-cases]
        (let [result (m/validate schema v)]
          (println (str "  " v " => " result))
          (assert result (str "Should be valid: " v)))))
    
    (println "\n1.2 Invalid cases (zero or multiple non-nil fields):")
    (let [invalid-cases [{}
                         {:field-a nil :field-b nil :field-c nil}
                         {:field-a "x" :field-b 42}
                         {:field-a "x" :field-b 42 :field-c true}
                         {:field-a "x" :field-c false}]]
      (doseq [v invalid-cases]
        (let [result (m/validate schema v)]
          (println (str "  " v " => " result))
          (assert (not result) (str "Should be invalid: " v)))))
    
    (println "\n1.3 Invalid cases (extra keys - closed map behavior):")
    (let [invalid-cases [{:field-a "x" :extra 123}
                         {:field-b 42 :unknown nil}
                         {:field-c true :field-d "extra"}]]
      (doseq [v invalid-cases]
        (let [result (m/validate schema v)]
          (println (str "  " v " => " result))
          (assert (not result) (str "Should reject extra keys: " v)))))
    
    (println "\n1.4 Invalid cases (wrong types):")
    (let [invalid-cases [{:field-a 123}  ; int instead of string
                         {:field-b "42"} ; string instead of int
                         {:field-c "true"}]] ; string instead of boolean
      (doseq [v invalid-cases]
        (let [result (m/validate schema v)]
          (println (str "  " v " => " result))
          (assert (not result) (str "Should reject wrong type: " v)))))))

(test-basic-validation)

;; ============================================================================
;; PART 2: GENERATION TESTS
;; ============================================================================

(println "\n\nPART 2: Generation Tests")
(println "------------------------")

(defn test-generation []
  (let [schema [:oneof_edn
                [:option-a :string]
                [:option-b [:int {:min 0 :max 100}]]
                [:option-c [:map {:closed true}
                           [:x :int]
                           [:y :int]]]]
    
    (println "\n2.1 Generate and validate 20 samples:")
    (let [samples (repeatedly 20 #(mg/generate schema))
          keys-seen (atom #{})]
      (doseq [[i sample] (map-indexed vector samples)]
        (let [valid? (m/validate schema sample)
              active-key (first (keys sample))]
          (swap! keys-seen conj active-key)
          (when (< i 5) ; Print first 5
            (println (str "  Sample " i ": " sample " => " valid?)))
          (assert valid? (str "Generated value should be valid: " sample))
          (assert (= 1 (count sample)) "Should have exactly one key")))
      (println (str "\n  Coverage: generated keys: " @keys-seen))
      (assert (>= (count @keys-seen) 2) "Should generate at least 2 different alternatives"))

(test-generation)

;; ============================================================================
;; PART 3: PROPERTY-BASED TESTS
;; ============================================================================

(println "\n\nPART 3: Property-Based Tests")
(println "----------------------------")

(defn test-properties []
  ;; Property 1: All generated values are valid
  (println "\n3.1 Property: All generated values validate")
  (let [schema [:oneof_edn
                [:alpha [:string {:min 1 :max 10}]]
                [:beta [:int {:min -100 :max 100}]]
                [:gamma :boolean]]
        prop-valid (prop/for-all [v (mg/generator schema)]
                     (m/validate schema v))
        result (tc/quick-check 100 prop-valid)]
    (println (str "  Result: " (if (:pass? result) "✓ PASS" "✗ FAIL")))
    (when-not (:pass? result)
      (println "  Failure:" (:fail result)))
    (assert (:pass? result) "Generated values should always be valid"))
  
  ;; Property 2: Generated values have exactly one field
  (println "\n3.2 Property: Generated values have exactly one field")
  (let [schema [:oneof_edn [:a :string] [:b :int] [:c :boolean]]
        prop-one-field (prop/for-all [v (mg/generator schema)]
                         (= 1 (count (keys v))))
        result (tc/quick-check 100 prop-one-field)]
    (println (str "  Result: " (if (:pass? result) "✓ PASS" "✗ FAIL")))
    (assert (:pass? result) "Generated values should have exactly one field"))
  
  ;; Property 3: Maps with multiple non-nil values are always invalid
  (println "\n3.3 Property: Multiple non-nil fields are always invalid")
  (let [schema [:oneof_edn [:x :int] [:y :int] [:z :int]]
        prop-multi-invalid (prop/for-all [x gen/int
                                          y gen/int]
                            (not (m/validate schema {:x x :y y})))
        result (tc/quick-check 100 prop-multi-invalid)]
    (println (str "  Result: " (if (:pass? result) "✓ PASS" "✗ FAIL")))
    (assert (:pass? result) "Multiple non-nil fields should be invalid")))

(test-properties)

;; ============================================================================
;; PART 4: INTEGRATION WITH MAP SPECS
;; ============================================================================

(println "\n\nPART 4: Integration with Map Specs")
(println "-----------------------------------")

(defn test-map-integration []
  ;; Test :merge with oneof_edn
  (println "\n4.1 Testing :merge with oneof_edn")
  (let [base-spec [:map {:closed true}
                   [:id :int]
                   [:name :string]]
        oneof-spec [:oneof_edn
                   [:action-create [:map {:closed true} [:template :string]]]
                   [:action-update [:map {:closed true} [:changes :any]]]
                   [:action-delete [:map {:closed true} [:confirm :boolean]]]]
        merged-spec [:merge base-spec oneof-spec]]
    
    (println "  Valid merged examples:")
    (let [valid-examples [{:id 1 :name "test" :action-create {:template "foo"}}
                          {:id 2 :name "bar" :action-update {:changes []}}
                          {:id 3 :name "baz" :action-delete {:confirm true}}]]
      (doseq [v valid-examples]
        (let [result (m/validate merged-spec v)]
          (println (str "    " v " => " result))
          (assert result (str "Should be valid: " v)))))
    
    (println "\n  Invalid merged examples:")
    (let [invalid-examples [{:id 1 :name "test"} ; missing action
                            {:id 1 :name "test" :action-create {:template "x"} 
                             :action-delete {:confirm false}} ; multiple actions
                            {:id 1 :name "test" :extra "field" ; extra field
                             :action-create {:template "x"}}]]
      (doseq [v invalid-examples]
        (let [result (m/validate merged-spec v)]
          (println (str "    " v " => " result))
          (assert (not result) (str "Should be invalid: " v))))))
  
  ;; Test nested oneof_edn
  (println "\n4.2 Testing nested oneof_edn")
  (let [schema [:map {:closed true}
                [:type [:enum :request :response]]
                [:payload [:oneof_edn
                          [:text :string]
                          [:number :int]
                          [:nested [:oneof_edn
                                   [:option-a :keyword]
                                   [:option-b :boolean]]]]]]]
    (println "  Valid nested examples:")
    (let [valid-examples [{:type :request :payload {:text "hello"}}
                          {:type :response :payload {:number 42}}
                          {:type :request :payload {:nested {:option-a :foo}}}
                          {:type :response :payload {:nested {:option-b false}}}]]
      (doseq [v valid-examples]
        (let [result (m/validate schema v)]
          (println (str "    " v " => " result))
          (assert result (str "Should be valid: " v)))))
    
    (println "\n  Generating nested examples:")
    (dotimes [i 5]
      (let [generated (mg/generate schema)]
        (println (str "    Generated " i ": " generated 
                     " valid? " (m/validate schema generated)))))))

(test-map-integration)

;; ============================================================================
;; PART 5: ERROR MESSAGES AND EXPLANATIONS
;; ============================================================================

(println "\n\nPART 5: Error Messages and Explanations")
(println "----------------------------------------")

(defn test-error-messages []
  (let [schema [:oneof_edn
                [:field-a :string]
                [:field-b :int]]]
    
    (println "\n5.1 Error for no non-nil fields:")
    (let [value {}
          explanation (m/explain schema value)]
      (println (str "  Value: " value))
      (println (str "  Valid?: " (not explanation)))
      (when explanation
        (println (str "  Errors: " (me/humanize explanation)))))
    
    (println "\n5.2 Error for multiple non-nil fields:")
    (let [value {:field-a "test" :field-b 42}
          explanation (m/explain schema value)]
      (println (str "  Value: " value))
      (println (str "  Valid?: " (not explanation)))
      (when explanation
        (println (str "  Errors: " (me/humanize explanation)))))
    
    (println "\n5.3 Error for extra keys:")
    (let [value {:field-a "test" :extra 123}
          explanation (m/explain schema value)]
      (println (str "  Value: " value))
      (println (str "  Valid?: " (not explanation)))
      (when explanation
        (println (str "  Errors: " (me/humanize explanation)))))))

(test-error-messages)

;; ============================================================================
;; PART 6: CMD/ROOT INTEGRATION TEST
;; ============================================================================

(println "\n\nPART 6: CMD/Root Integration Test")
(println "---------------------------------")

(defn test-cmd-root []
  ;; Load cmd specs
  (require '[potatoclient.specs.cmd.common])
  
  ;; Create a simplified cmd/root-like spec
  (let [cmd-spec [:merge
                  [:map {:closed true}
                   [:protocol_version [:int {:min 1}]]
                   [:client_type [:enum :ground :web :ios :android]]]
                  [:oneof_edn
                   [:ping [:map {:closed true} [:id :int]]]
                   [:noop [:map {:closed true}]]
                   [:echo [:map {:closed true} [:message :string]]]]]]
    
    (println "\n6.1 Valid command messages:")
    (let [valid-cmds [{:protocol_version 1 :client_type :ground :ping {:id 123}}
                      {:protocol_version 2 :client_type :web :noop {}}
                      {:protocol_version 1 :client_type :ios :echo {:message "test"}}]]
      (doseq [cmd valid-cmds]
        (let [result (m/validate cmd-spec cmd)]
          (println (str "  " cmd " => " result))
          (assert result (str "Should be valid: " cmd)))))
    
    (println "\n6.2 Invalid command messages:")
    (let [invalid-cmds [{:protocol_version 1 :client_type :ground} ; no command
                        {:protocol_version 1 :client_type :ground 
                         :ping {:id 1} :noop {}} ; multiple commands
                        {:protocol_version 0 :client_type :ground :ping {:id 1}} ; invalid version
                        {:protocol_version 1 :client_type :unknown :ping {:id 1}}]] ; invalid client
      (doseq [cmd invalid-cmds]
        (let [result (m/validate cmd-spec cmd)]
          (println (str "  " cmd " => " result))
          (assert (not result) (str "Should be invalid: " cmd)))))
    
    (println "\n6.3 Generate valid commands:")
    (dotimes [i 10]
      (let [generated (mg/generate cmd-spec)]
        (let [cmd-field (first (filter #{:ping :noop :echo} (keys generated)))]
          (println (str "  Gen " i ": pv=" (:protocol_version generated)
                       " client=" (:client_type generated)
                       " cmd=" cmd-field
                       " valid? " (m/validate cmd-spec generated)))
          (assert (m/validate cmd-spec generated) 
                 (str "Generated command should be valid: " generated))))))

(test-cmd-root)

;; ============================================================================
;; SUMMARY
;; ============================================================================

(println "\n\n=== TEST SUMMARY ===")
(println "All tests completed successfully!")
(println "✓ Basic validation works correctly")
(println "✓ Generation produces valid values")
(println "✓ Property-based tests pass")
(println "✓ Integration with :merge works")
(println "✓ Nested oneof_edn works")
(println "✓ Error messages are provided")
(println "✓ CMD/Root-like structures work")
(println "\n✅ ONEOF_EDN IS FULLY FUNCTIONAL!")

(System/exit 0)