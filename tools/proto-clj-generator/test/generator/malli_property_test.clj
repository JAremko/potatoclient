(ns generator.malli-property-test
  "Property-based tests using Malli's built-in generators with constraints"
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.spec-gen :as spec-gen]
            [generator.constraints.compiler :as compiler]))

;; =============================================================================
;; Using Malli's Built-in Generators
;; =============================================================================

(deftest test-malli-generators-respect-constraints
  (testing "Malli generates values respecting constraints"
    ;; RGB color spec with constraints
    (let [rgb-spec [:and :int [:>= 0] [:<= 255]]
          generated-values (mg/sample rgb-spec {:size 100})]
      (is (every? #(and (>= % 0) (<= % 255)) generated-values))
      (is (= 100 (count generated-values))))
    
    ;; Protocol version spec
    (let [version-spec [:and :int [:> 0]]
          generated-values (mg/sample version-spec {:size 100})]
      (is (every? #(> % 0) generated-values)))
    
    ;; Latitude spec
    (let [lat-spec [:and :double [:>= -90.0] [:<= 90.0]]
          generated-values (mg/sample lat-spec {:size 100})]
      (is (every? #(and (>= % -90.0) (<= % 90.0)) generated-values)))
    
    ;; String with length constraints
    (let [string-spec [:and :string [:fn #(>= (count %) 5)] [:fn #(<= (count %) 10)]]
          generated-values (mg/sample string-spec {:size 50})]
      (is (every? #(and (>= (count %) 5) (<= (count %) 10)) generated-values)))))

(defspec malli-spec-roundtrip-property
  100
  ;; Test that values generated from specs validate against those specs
  (let [specs [[:and :int [:> 0]]                           ; protocol version
               [:and :int [:>= 0] [:<= 255]]               ; RGB
               [:and :double [:>= -90.0] [:<= 90.0]]       ; latitude
               [:and :double [:>= -180.0] [:<= 180.0]]     ; longitude
               [:and :string [:fn #(>= (count %) 3)]]]]    ; min length string
    (prop/for-all [spec (gen/elements specs)]
      (let [value (mg/generate spec)]
        (m/validate spec value)))))

;; =============================================================================
;; Test Actual Generated Specs
;; =============================================================================

(deftest test-generated-message-specs
  (testing "Generated message specs with constraints"
    ;; Simulate what our generated specs look like
    (let [root-spec [:map 
                     [:protocol-version [:and :int [:> 0]]]
                     [:session-id [:maybe :int]]
                     [:important [:maybe :boolean]]]
          
          rgb-color-spec [:map
                          [:red [:and [:maybe :int] [:>= 0] [:<= 255]]]
                          [:green [:and [:maybe :int] [:>= 0] [:<= 255]]]
                          [:blue [:and [:maybe :int] [:>= 0] [:<= 255]]]]
          
          gps-position-spec [:map
                             [:latitude [:and [:maybe :double] [:>= -90.0] [:<= 90.0]]]
                             [:longitude [:and [:maybe :double] [:>= -180.0] [:<= 180.0]]]
                             [:altitude [:maybe :double]]]]
      
      ;; Test generation and validation
      (testing "Root message generation"
        (let [generated (mg/generate root-spec)]
          (is (m/validate root-spec generated))
          (is (> (:protocol-version generated) 0))))
      
      (testing "RGB color generation"
        (let [generated (mg/generate rgb-color-spec)]
          (is (m/validate rgb-color-spec generated))
          (when (:red generated)
            (is (and (>= (:red generated) 0) (<= (:red generated) 255))))
          (when (:green generated)
            (is (and (>= (:green generated) 0) (<= (:green generated) 255))))
          (when (:blue generated)
            (is (and (>= (:blue generated) 0) (<= (:blue generated) 255))))))
      
      (testing "GPS position generation"
        (let [generated (mg/generate gps-position-spec)]
          (is (m/validate gps-position-spec generated))
          (when (:latitude generated)
            (is (and (>= (:latitude generated) -90.0) 
                     (<= (:latitude generated) 90.0))))
          (when (:longitude generated)
            (is (and (>= (:longitude generated) -180.0) 
                     (<= (:longitude generated) 180.0)))))))))

;; =============================================================================
;; Property Tests for Constraint Compilation
;; =============================================================================

(defspec compiled-constraints-generate-valid-values
  100
  (prop/for-all [gt (gen/choose 0 100)
                 lt (gen/choose 101 200)]
    (let [field {:type :type-int32
                 :constraints {:gt gt :lt lt}}
          compiled (compiler/apply-constraints :int field)
          value (mg/generate compiled)]
      (and (> value gt)
           (< value lt)))))

;; =============================================================================
;; Test Edge Cases
;; =============================================================================

(deftest test-constraint-edge-cases
  (testing "Empty constraints"
    (let [spec [:int]
          values (mg/sample spec {:size 10})]
      (is (every? int? values))))
  
  (testing "Conflicting constraints should fail"
    ;; This should ideally throw or return nil
    (let [impossible-spec [:and :int [:> 10] [:< 5]]]
      (is (thrown? Exception (mg/generate impossible-spec)))))
  
  (testing "Exact value constraint"
    (let [const-spec [:and :int [:>= 42] [:<= 42]]
          values (mg/sample const-spec {:size 10})]
      (is (every? #(= 42 %) values))))
  
  (testing "Complex string constraints"
    (let [email-like [:and :string 
                      [:fn #(re-matches #"[^@]+@[^@]+" %)]]
          value (mg/generate email-like {:size 10})]
      (is (re-matches #"[^@]+@[^@]+" value)))))

;; =============================================================================
;; Integration with Generated Code
;; =============================================================================

(defn test-generated-builder-with-constraints
  "Template for testing generated builder functions"
  [builder-fn parser-fn valid-data invalid-data]
  (testing "Valid data passes through"
    (let [proto (builder-fn valid-data)
          parsed (parser-fn proto)]
      (is (= valid-data parsed))))
  
  (testing "Invalid data rejected by guardrails"
    ;; When guardrails is enabled, this should throw
    (is (thrown? Exception (builder-fn invalid-data)))))

;; Example test that would work with generated code:
(comment
  (deftest test-rgb-color-builder
    (test-generated-builder-with-constraints
     build-rgb-color
     parse-rgb-color
     {:red 128 :green 64 :blue 255}    ; valid
     {:red 256 :green -1 :blue 300}))) ; invalid