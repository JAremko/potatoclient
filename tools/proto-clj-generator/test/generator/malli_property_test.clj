(ns generator.malli-property-test
  "Property-based tests using Malli's built-in generators with constraints"
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
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
;; Property Tests for Generated Specs
;; =============================================================================

(deftest test-generated-spec-constraints
  (testing "Generated specs include proper constraints"
    ;; Test RGB spec generation
    (let [rgb-field {:name "red"
                     :type {:scalar :uint32}
                     :constraints {:field-constraints {:gte 0 :lte 255}}}
          spec (spec-gen/process-field-schema rgb-field {})]
      (is (= [:and :int [:>= 0] [:<= 255]] spec)))
    
    ;; Test protocol version
    (let [version-field {:name "protocol_version"
                         :type {:scalar :uint32}
                         :constraints {:field-constraints {:gt 0}}}
          spec (spec-gen/process-field-schema version-field {})]
      (is (= [:and :int [:> 0]] spec)))
    
    ;; Test latitude
    (let [lat-field {:name "latitude"
                     :type {:scalar :double}
                     :constraints {:field-constraints {:gte -90.0 :lte 90.0}}}
          spec (spec-gen/process-field-schema lat-field {})]
      (is (= [:and :double [:>= -90.0] [:<= 90.0]] spec)))))

;; =============================================================================
;; Advanced Property Tests
;; =============================================================================

(defspec complex-message-generation
  50
  ;; Test that complex messages with multiple constraints generate valid data
  (let [message-spec [:map
                      [:protocol-version [:and :int [:> 0]]]
                      [:color [:map
                               [:red [:and :int [:>= 0] [:<= 255]]]
                               [:green [:and :int [:>= 0] [:<= 255]]]
                               [:blue [:and :int [:>= 0] [:<= 255]]]]]
                      [:location [:map
                                  [:latitude [:and :double [:>= -90.0] [:<= 90.0]]]
                                  [:longitude [:and :double [:>= -180.0] [:<= 180.0]]]]]]
        gen-opts {:size 50 :seed 42}]
    (prop/for-all [message (mg/generator message-spec gen-opts)]
      (and (m/validate message-spec message)
           (> (:protocol-version message) 0)
           (every? #(and (>= % 0) (<= % 255)) 
                   (vals (:color message)))
           (let [{:keys [latitude longitude]} (:location message)]
             (and (>= latitude -90.0) (<= latitude 90.0)
                  (>= longitude -180.0) (<= longitude 180.0)))))))

(deftest test-enum-constraints
  (testing "Enum constraints (not_in) work correctly"
    ;; Simulate an enum with not_in: [0] constraint
    (let [enum-spec [:and [:enum :unspecified :value1 :value2 :value3]
                     [:fn #(not= % :unspecified)]]
          generated-values (mg/sample enum-spec {:size 100})]
      (is (every? #(not= % :unspecified) generated-values))
      (is (every? #{:value1 :value2 :value3} generated-values)))))

;; =============================================================================
;; Performance Tests
;; =============================================================================

(deftest test-generator-performance
  (testing "Generators with constraints perform acceptably"
    (let [complex-spec [:map
                        [:id [:and :string [:fn #(>= (count %) 10)] [:fn #(<= (count %) 20)]]]
                        [:score [:and :int [:>= 0] [:<= 100]]]
                        [:timestamp [:and :int [:> 0]]]
                        [:tags [:vector [:and :string [:fn #(>= (count %) 1)] [:fn #(<= (count %) 10)]]]]]
          start (System/nanoTime)
          _ (mg/sample complex-spec {:size 1000})
          elapsed (/ (- (System/nanoTime) start) 1e9)]
      (is (< elapsed 5.0) ; Should generate 1000 samples in under 5 seconds
          (str "Generation took " elapsed " seconds")))))
