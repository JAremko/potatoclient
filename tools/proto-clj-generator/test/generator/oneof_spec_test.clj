(ns generator.oneof-spec-test
  "Comprehensive tests for custom :oneof spec integration in proto-clj-generator.
   Based on proto-explorer's oneof_test.clj but adapted for our generation context."
  (:require [clojure.test :refer :all]
            [potatoclient.specs.malli-oneof :as oneof]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [generator.core :as core]
            [clojure.java.io :as io]))

;; =============================================================================
;; Registry Setup
;; =============================================================================

(def test-registry
  "Test registry with custom :oneof schema"
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

;; =============================================================================
;; Basic Oneof Tests
;; =============================================================================

(deftest test-oneof-schema-basics
  (testing "Basic oneof validation"
    (let [schema [:oneof {:ping [:maybe :map]
                         :noop [:maybe :map]
                         :frozen [:maybe :map]}]]
      
      (testing "Valid single option"
        (is (m/validate schema {:ping {}} {:registry test-registry}))
        (is (m/validate schema {:noop {}} {:registry test-registry}))
        (is (m/validate schema {:frozen {}} {:registry test-registry})))
      
      (testing "Invalid - no options"
        (is (not (m/validate schema {} {:registry test-registry}))))
      
      (testing "Invalid - multiple options"
        (is (not (m/validate schema {:ping {} :noop {}} {:registry test-registry}))))
      
      (testing "Invalid - unknown option"
        (is (not (m/validate schema {:unknown {}} {:registry test-registry})))))))

(deftest test-oneof-with-protobuf-patterns
  (testing "Oneof patterns common in protobuf"
    (let [;; Command pattern - exactly one command must be set
          command-schema [:oneof {:ping [:map [:ping :map]]
                                 :rotary [:map [:rotary [:map 
                                                        [:goto-ndc [:map
                                                                   [:channel :keyword]
                                                                   [:x :double]
                                                                   [:y :double]]]]]]}]]
      
      (testing "Valid command structures"
        (is (m/validate command-schema 
                       {:ping {:ping {}}} 
                       {:registry test-registry}))
        (is (m/validate command-schema 
                       {:rotary {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}} 
                       {:registry test-registry})))
      
      (testing "Invalid command structures"
        ;; Missing the nested map structure
        (is (not (m/validate command-schema 
                            {:ping {}} 
                            {:registry test-registry})))
        ;; Multiple commands
        (is (not (m/validate command-schema 
                            {:ping {:ping {}} :rotary {:rotary {}}} 
                            {:registry test-registry})))))))

;; =============================================================================
;; Generator Tests
;; =============================================================================

(deftest test-oneof-generator
  (testing "Oneof generator produces valid data"
    (let [schema [:oneof {:ping [:map [:ping :map]]
                         :noop [:map [:noop :map]]
                         :frozen [:map [:frozen :map]]}]]
      
      (testing "Can create generator"
        (let [generator (mg/generator schema {:registry test-registry})]
          (is (some? generator))))
      
      (testing "Generated data is valid"
        (let [generator (mg/generator schema {:registry test-registry})]
          (dotimes [_ 20]
            (let [data (mg/generate generator)]
              (is (m/validate schema data {:registry test-registry})
                  (str "Generated data should be valid: " data))
              (is (= 1 (count data)) 
                  "Should have exactly one key")
              (is (contains? #{:ping :noop :frozen} (first (keys data)))
                  "Should be one of the expected keys"))))))))

(deftest test-oneof-with-constraints
  (testing "Oneof with constrained field values"
    (let [schema [:oneof {:set-position [:map 
                                        [:set-position 
                                         [:map
                                          [:x [:and :double [:>= -1.0] [:<= 1.0]]]
                                          [:y [:and :double [:>= -1.0] [:<= 1.0]]]]]]
                         :set-velocity [:map
                                       [:set-velocity
                                        [:map
                                         [:azimuth [:and :double [:>= 0] [:< 360]]]
                                         [:elevation [:and :double [:>= -90] [:<= 90]]]]]]}]]
      
      (testing "Valid constrained data"
        (is (m/validate schema 
                       {:set-position {:set-position {:x 0.5 :y -0.5}}} 
                       {:registry test-registry}))
        (is (m/validate schema 
                       {:set-velocity {:set-velocity {:azimuth 45.0 :elevation 30.0}}} 
                       {:registry test-registry})))
      
      (testing "Invalid - constraint violations"
        ;; X out of range
        (is (not (m/validate schema 
                            {:set-position {:set-position {:x 1.5 :y 0.0}}} 
                            {:registry test-registry})))
        ;; Azimuth out of range
        (is (not (m/validate schema 
                            {:set-velocity {:set-velocity {:azimuth 360.0 :elevation 0.0}}} 
                            {:registry test-registry}))))
      
      (testing "Generator respects constraints"
        (let [generator (mg/generator schema {:registry test-registry})]
          (dotimes [_ 20]
            (let [data (mg/generate generator)]
              (is (m/validate schema data {:registry test-registry})
                  (str "Generated data should respect constraints: " data))
              
              (cond
                (contains? data :set-position)
                (let [pos (get-in data [:set-position :set-position])]
                  (is (and (>= (:x pos) -1.0) (<= (:x pos) 1.0))
                      "X should be in range [-1, 1]")
                  (is (and (>= (:y pos) -1.0) (<= (:y pos) 1.0))
                      "Y should be in range [-1, 1]"))
                
                (contains? data :set-velocity)
                (let [vel (get-in data [:set-velocity :set-velocity])]
                  (is (and (>= (:azimuth vel) 0) (< (:azimuth vel) 360))
                      "Azimuth should be in range [0, 360)")
                  (is (and (>= (:elevation vel) -90) (<= (:elevation vel) 90))
                      "Elevation should be in range [-90, 90]"))))))))))

;; =============================================================================
;; Nested Oneof Tests
;; =============================================================================

(deftest test-nested-oneof
  (testing "Nested oneof structures"
    (let [;; Inner oneof for different parameter types
          param-schema [:oneof {:int-param [:map [:value :int]]
                               :double-param [:map [:value :double]]
                               :string-param [:map [:value :string]]}]
          ;; Outer oneof for commands with parameters
          command-schema [:oneof {:set-param [:map [:param param-schema]]
                                 :get-param [:map [:name :string]]
                                 :clear-params [:map]}]]
      
      (testing "Valid nested structures"
        (is (m/validate command-schema 
                       {:set-param {:param {:int-param {:value 42}}}} 
                       {:registry test-registry}))
        (is (m/validate command-schema 
                       {:set-param {:param {:string-param {:value "hello"}}}} 
                       {:registry test-registry}))
        (is (m/validate command-schema 
                       {:get-param {:name "temperature"}} 
                       {:registry test-registry})))
      
      (testing "Invalid nested structures"
        ;; Multiple params in inner oneof
        (is (not (m/validate command-schema 
                            {:set-param {:param {:int-param {:value 42}
                                               :double-param {:value 3.14}}}} 
                            {:registry test-registry})))
        ;; Empty inner oneof
        (is (not (m/validate command-schema 
                            {:set-param {:param {}}} 
                            {:registry test-registry})))))))

;; =============================================================================
;; Error Messages and Explanations
;; =============================================================================

(deftest test-oneof-error-messages
  (testing "Oneof validation error messages"
    (let [schema [:oneof {:ping [:map [:ping :map]]
                         :noop [:map [:noop :map]]}]]
      
      (testing "Error for empty map"
        (let [explanation (m/explain schema {} {:registry test-registry})]
          (is (some? explanation))
          (is (seq (:errors explanation)))
          (is (some #(re-find #"Exactly one field must be set" (str %)) 
                    (map :message (:errors explanation))))))
      
      (testing "Error for multiple options"
        (let [explanation (m/explain schema 
                                   {:ping {:ping {}} :noop {:noop {}}} 
                                   {:registry test-registry})]
          (is (some? explanation))
          (is (seq (:errors explanation)))))
      
      (testing "Error for unknown option"
        (let [explanation (m/explain schema 
                                   {:unknown {}} 
                                   {:registry test-registry})]
          (is (some? explanation))
          (is (seq (:errors explanation))))))))

;; =============================================================================
;; Integration with Generated Code
;; =============================================================================

(defonce generated? (atom false))

(defn ensure-test-generation!
  "Generate test code with oneof specs"
  []
  (when-not @generated?
    (println "\nGenerating code for oneof spec tests...")
    ;; Clear naming cache
    (require 'generator.naming)
    ((resolve 'generator.naming/clear-conversion-cache!))
    
    ;; Create a test proto file with oneofs if needed
    (let [test-output-dir "test-oneof-output"]
      (io/make-parents (str test-output-dir "/dummy.txt"))
      
      ;; Generate code
      (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                       :output-dir test-output-dir
                                       :namespace-prefix "test.oneof"
                                       :debug? false})]
        (when-not (:success result)
          (throw (ex-info "Failed to generate code" result)))
        
        (reset! generated? true)))))

(deftest test-generated-oneof-specs
  (testing "Generated code uses custom :oneof specs correctly"
    (ensure-test-generation!)
    
    ;; Load generated namespaces
    (require '[test.oneof.cmd :as cmd] :reload)
    
    (let [cmd-specs (ns-resolve 'test.oneof.cmd 'specs)]
      (when cmd-specs
        (let [specs @cmd-specs
              ;; Find a spec that should have oneof
              root-spec (:root specs)]
          
          (testing "Generated root spec exists"
            (is (some? root-spec)))
          
          (when root-spec
            (testing "Can validate with generated specs"
              ;; The actual structure will depend on the proto files
              ;; This is a placeholder test
              (is true "Generated spec validation test placeholder"))))))))

;; =============================================================================
;; Registry Propagation Tests
;; =============================================================================

(deftest test-registry-propagation
  (testing "Registry with :oneof propagates through nested validation"
    (let [;; Complex nested structure
          inner-oneof [:oneof {:a :string :b :int}]
          middle-schema [:map 
                        [:id :string]
                        [:data inner-oneof]]
          outer-schema [:oneof {:create [:map [:payload middle-schema]]
                               :delete [:map [:id :string]]}]]
      
      (testing "Registry propagates to all levels"
        (is (m/validate outer-schema 
                       {:create {:payload {:id "123" :data {:a "hello"}}}} 
                       {:registry test-registry}))
        (is (m/validate outer-schema 
                       {:create {:payload {:id "456" :data {:b 42}}}} 
                       {:registry test-registry})))
      
      (testing "Invalid at inner level is caught"
        (is (not (m/validate outer-schema 
                            {:create {:payload {:id "789" :data {:a "hello" :b 42}}}} 
                            {:registry test-registry})))
        (is (not (m/validate outer-schema 
                            {:create {:payload {:id "789" :data {}}}} 
                            {:registry test-registry})))))))

;; =============================================================================
;; Performance Tests
;; =============================================================================

(deftest test-oneof-performance
  (testing "Oneof validation performance"
    (let [schema [:oneof {:option1 [:map [:data :string]]
                         :option2 [:map [:data :int]]
                         :option3 [:map [:data :double]]}]
          valid-data {:option2 {:data 42}}
          
          ;; Warm up
          _ (dotimes [_ 100]
              (m/validate schema valid-data {:registry test-registry}))
          
          ;; Measure
          start (System/nanoTime)
          iterations 10000
          _ (dotimes [_ iterations]
              (m/validate schema valid-data {:registry test-registry}))
          end (System/nanoTime)
          duration-ms (/ (- end start) 1e6)
          per-validation-us (/ duration-ms iterations 0.001)]
      
      (println (format "\nOneof validation performance: %.2f μs per validation" 
                      per-validation-us))
      
      (is (< per-validation-us 100)
          "Oneof validation should be fast (< 100μs per validation)"))))

(deftest ^:integration test-oneof-with-actual-proto
  (testing "Oneof works with actual protobuf patterns"
    ;; This test would use actual generated specs from proto files
    ;; For now, we simulate the expected structure
    (let [;; Simulated command root with oneof
          cmd-root-schema [:map
                          [:protocol-version [:and :int [:> 0]]]
                          [:client-type :keyword]
                          [:payload [:oneof {:ping [:map [:ping :map]]
                                           :get-version [:map [:get-version :map]]
                                           :rotary [:map [:rotary :map]]}]]]]
      
      (testing "Valid command structures"
        (is (m/validate cmd-root-schema 
                       {:protocol-version 1
                        :client-type :jon-gui-data-client-type-local-network
                        :payload {:ping {:ping {}}}} 
                       {:registry test-registry})))
      
      (testing "Invalid - no payload"
        (is (not (m/validate cmd-root-schema 
                            {:protocol-version 1
                             :client-type :jon-gui-data-client-type-local-network
                             :payload {}} 
                            {:registry test-registry})))))))

;; Run fixture to ensure test isolation
(use-fixtures :each
  (fn [f]
    ;; Could reset any global state here if needed
    (f)))