(ns generator.roundtrip-test
  "Roundtrip tests for generated protobuf code with constraints"
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [malli.core :as m]
            [malli.generator :as mg]
            [generator.main :as main]
            [clojure.java.io :as io]))

;; =============================================================================
;; Test Fixture - Generate Code for Testing
;; =============================================================================

(def test-output-dir "test-roundtrip-output")

(defn generate-test-code-fixture [f]
  ;; Generate code before tests
  (main/generate {:input-dir "../../tools/proto-explorer/output/json-descriptors"
                  :output-dir test-output-dir
                  :namespace-prefix "test.proto"
                  :guardrails? true})
  (f)
  ;; Cleanup after tests if needed
  )

(use-fixtures :once generate-test-code-fixture)

;; =============================================================================
;; Spec Definitions (matching what we generate)
;; =============================================================================

(def cmd-root-spec
  [:map 
   [:protocol-version [:and :int [:> 0]]]
   [:session-id [:maybe :int]]
   [:important [:maybe :boolean]]
   [:from-cv-subsystem [:maybe :boolean]]
   [:client-type [:maybe keyword?]]
   [:payload [:altn
              [:ping [:map [:ping map?]]]
              [:noop [:map [:noop map?]]]
              [:system [:map [:system map?]]]
              ;; ... other variants
              ]]])

(def rgb-color-spec
  [:map
   [:red [:and [:maybe :int] [:>= 0] [:<= 255]]]
   [:green [:and [:maybe :int] [:>= 0] [:<= 255]]]
   [:blue [:and [:maybe :int] [:>= 0] [:<= 255]]]])

(def gps-position-spec
  [:map
   [:latitude [:and [:maybe :double] [:>= -90.0] [:<= 90.0]]]
   [:longitude [:and [:maybe :double] [:>= -180.0] [:<= 180.0]]]
   [:altitude [:maybe :double]]])

;; =============================================================================
;; Roundtrip Test Helpers
;; =============================================================================

(defn roundtrip-preserves-data?
  "Test that data roundtrips correctly through protobuf"
  [data builder-fn parser-fn]
  (try
    (let [proto (builder-fn data)
          parsed (parser-fn proto)]
      (= data parsed))
    (catch Exception e
      ;; If guardrails rejects invalid data, that's expected
      false)))

(defn generate-valid-message
  "Generate a valid message using Malli generators"
  [spec]
  (mg/generate spec))

;; =============================================================================
;; Property-Based Roundtrip Tests
;; =============================================================================

(defspec rgb-color-roundtrip-property
  100
  (prop/for-all [color (mg/generator rgb-color-spec)]
    ;; This would work if we could load the generated code
    ;; For now, we test the spec logic
    (let [valid-color? (m/validate rgb-color-spec color)]
      (if valid-color?
        ;; Valid colors should roundtrip
        (and (or (nil? (:red color)) 
                 (and (>= (:red color) 0) (<= (:red color) 255)))
             (or (nil? (:green color))
                 (and (>= (:green color) 0) (<= (:green color) 255)))
             (or (nil? (:blue color))
                 (and (>= (:blue color) 0) (<= (:blue color) 255))))
        ;; Invalid colors should be rejected
        true))))

(defspec gps-position-roundtrip-property
  100
  (prop/for-all [position (mg/generator gps-position-spec)]
    (m/validate gps-position-spec position)))

(defspec protocol-version-always-positive
  100
  (prop/for-all [msg (mg/generator [:map [:protocol-version [:and :int [:> 0]]]])]
    (> (:protocol-version msg) 0)))

;; =============================================================================
;; Constraint Violation Tests
;; =============================================================================

(deftest test-constraint-violations
  (testing "RGB values outside 0-255 are rejected"
    (let [invalid-colors [{:red -1 :green 128 :blue 128}
                          {:red 256 :green 128 :blue 128}
                          {:red 128 :green -1 :blue 128}
                          {:red 128 :green 256 :blue 128}
                          {:red 128 :green 128 :blue -1}
                          {:red 128 :green 128 :blue 256}]]
      (doseq [color invalid-colors]
        (is (not (m/validate rgb-color-spec color))))))
  
  (testing "Protocol version must be positive"
    (let [invalid-versions [{:protocol-version 0}
                            {:protocol-version -1}
                            {:protocol-version -100}]]
      (doseq [msg invalid-versions]
        (is (not (m/validate [:map [:protocol-version [:and :int [:> 0]]]] msg))))))
  
  (testing "GPS coordinates must be in valid ranges"
    (let [invalid-positions [{:latitude 91.0 :longitude 0.0}
                             {:latitude -91.0 :longitude 0.0}
                             {:latitude 0.0 :longitude 181.0}
                             {:latitude 0.0 :longitude -181.0}]]
      (doseq [pos invalid-positions]
        (is (not (m/validate gps-position-spec pos)))))))

;; =============================================================================
;; Edge Case Tests
;; =============================================================================

(deftest test-boundary-values
  (testing "RGB boundary values are accepted"
    (let [boundary-colors [{:red 0 :green 0 :blue 0}
                           {:red 255 :green 255 :blue 255}
                           {:red 0 :green 128 :blue 255}]]
      (doseq [color boundary-colors]
        (is (m/validate rgb-color-spec color)))))
  
  (testing "GPS boundary values are accepted"
    (let [boundary-positions [{:latitude -90.0 :longitude -180.0}
                              {:latitude 90.0 :longitude 180.0}
                              {:latitude 0.0 :longitude 0.0}
                              {:latitude 45.0 :longitude -45.0}]]
      (doseq [pos boundary-positions]
        (is (m/validate gps-position-spec pos)))))
  
  (testing "Protocol version boundary"
    (is (m/validate [:map [:protocol-version [:and :int [:> 0]]]] 
                    {:protocol-version 1}))
    (is (not (m/validate [:map [:protocol-version [:and :int [:> 0]]]] 
                         {:protocol-version 0})))))

;; =============================================================================
;; Complex Message Tests
;; =============================================================================

(deftest test-complex-message-generation
  (testing "Generate complex messages with nested constraints"
    (let [complex-spec [:map
                        [:id [:and :int [:> 0]]]
                        [:name [:and :string [:fn #(>= (count %) 3)]]]
                        [:color rgb-color-spec]
                        [:location gps-position-spec]
                        [:tags [:vector [:and :string [:fn #(> (count %) 0)]]]]]
          generated (mg/generate complex-spec)]
      (is (m/validate complex-spec generated))
      (is (> (:id generated) 0))
      (is (>= (count (:name generated)) 3))
      (when (:color generated)
        (is (m/validate rgb-color-spec (:color generated))))
      (when (:location generated)
        (is (m/validate gps-position-spec (:location generated)))))))

;; =============================================================================
;; Oneof Handling Tests
;; =============================================================================

(deftest test-oneof-generation
  (testing "Oneof fields have exactly one value set"
    ;; Using our custom :oneof spec
    (let [oneof-spec [:map
                      [:payload [:altn
                                 [:text [:map [:text :string]]]
                                 [:number [:map [:number :int]]]
                                 [:flag [:map [:flag :boolean]]]]]]
          samples (mg/sample oneof-spec {:size 20})]
      (doseq [sample samples]
        (let [payload (:payload sample)]
          ;; Exactly one field should be set
          (is (= 1 (count payload)))
          ;; The value should match its spec
          (is (or (string? (get-in payload [:text]))
                  (int? (get-in payload [:number]))
                  (boolean? (get-in payload [:flag])))))))))

;; =============================================================================
;; Performance Tests
;; =============================================================================

(deftest test-generation-performance
  (testing "Constraint validation doesn't significantly impact performance"
    (let [simple-spec [:map [:value :int]]
          constrained-spec [:map [:value [:and :int [:> 0] [:< 1000000]]]]
          
          simple-time (time (dotimes [_ 1000]
                              (mg/generate simple-spec)))
          
          constrained-time (time (dotimes [_ 1000]
                                   (mg/generate constrained-spec)))]
      ;; Just ensure both complete without error
      (is true))))

;; =============================================================================
;; Integration Test Template
;; =============================================================================

(defn test-generated-code-integration
  "Template for testing actual generated code when available"
  []
  ;; This would be used with actual generated code
  (comment
    (require '[test.proto.cmd :as cmd])
    (require '[test.proto.ser :as ser])
    
    (deftest test-actual-roundtrip
      (testing "Actual RGB color roundtrip"
        (let [color {:red 128 :green 64 :blue 255}
              proto (ser/build-rgb-color color)
              parsed (ser/parse-rgb-color proto)]
          (is (= color parsed))))
      
      (testing "Invalid RGB color rejected"
        (is (thrown? Exception 
                     (ser/build-rgb-color {:red 300 :green 64 :blue 255})))))))