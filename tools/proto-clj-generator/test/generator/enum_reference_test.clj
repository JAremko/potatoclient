(ns generator.enum_reference_test
  "Granular tests for enum reference generation bug"
  (:require [clojure.test :refer [deftest testing is]]
            [generator.type-resolution :as tr]
            [generator.frontend :as frontend]
            [potatoclient.proto.conversion :as conv]))

(deftest enum-name-conversion-test
  (testing "enum names are properly converted to kebab-case"
    ;; The bug: JonGuiDataSystemLocalizations should become jon-gui-data-system-localizations
    (is (= "jon-gui-data-system-localizations" 
           (conv/->kebab-case "JonGuiDataSystemLocalizations")))
    (is (= "my-enum-type" 
           (conv/->kebab-case "MyEnumType")))
    (is (= "some-very-long-enum-name" 
           (conv/->kebab-case "SomeVeryLongEnumName")))))

(deftest enum-reference-resolution-test
  (testing "enum references resolve to kebab-case names"
    (let [type-lookup {".ser.JonGuiDataSystemLocalizations" 
                       {:name :jon-gui-data-system-localizations
                        :package "ser"}}]
      
      ;; Test local enum reference (same package)
      (let [result (tr/resolve-enum-reference 
                    ".ser.JonGuiDataSystemLocalizations" 
                    "ser" 
                    type-lookup)]
        (is (= "jon-gui-data-system-localizations-values" (:name result)))
        (is (= false (:qualified? result))))
      
      ;; Test cross-package enum reference
      (let [result (tr/resolve-enum-reference 
                    ".ser.JonGuiDataSystemLocalizations" 
                    "cmd.rotary" 
                    type-lookup)]
        (is (= "jon-gui-data-system-localizations-values" (:name result)))
        (is (= true (:qualified? result)))
        (is (= "types" (:ns-alias result))))))
  
  (testing "enum references without type lookup use fallback conversion"
    ;; This is where the bug occurs - fallback must convert properly
    (let [result (tr/resolve-enum-reference 
                  ".ser.JonGuiDataSystemLocalizations" 
                  "ser" 
                  {})] ; Empty type lookup forces fallback
      (is (= "jon-gui-data-system-localizations-values" (:name result)))
      (is (string? (:name result))))
    
    ;; Another example with different casing
    (let [result (tr/resolve-enum-reference 
                  ".package.SomeEnumType" 
                  "other.package" 
                  {})]
      (is (= "some-enum-type-values" (:name result)))
      (is (= true (:qualified? result))))))

(deftest field-setter-enum-generation-test
  (testing "field setter generates correct enum reference"
    ;; Mock field with enum type
    (let [field {:name :loc
                 :proto-name "loc"
                 :type {:enum {:type-ref ".ser.JonGuiDataSystemLocalizations"}}}
          type-lookup {".ser.JonGuiDataSystemLocalizations" 
                       {:name :jon-gui-data-system-localizations
                        :package "ser"}}
          
          ;; Generate setter code
          setter-code (frontend/generate-field-setter field "ser" type-lookup)]
      
      ;; Should contain the correct kebab-case enum reference
      (is (re-find #"jon-gui-data-system-localizations-values" setter-code))
      ;; Should NOT contain the PascalCase version
      (is (not (re-find #"JonGuiDataSystemLocalizations-values" setter-code))))))

(deftest qualified-enum-ref-generation-test
  (testing "qualified enum references are generated correctly"
    ;; Local reference (unqualified)
    (is (= "my-enum-values" 
           (tr/qualified-enum-ref {:name "my-enum-values" 
                                   :qualified? false})))
    
    ;; Cross-package reference (qualified)
    (is (= "types/my-enum-values" 
           (tr/qualified-enum-ref {:name "my-enum-values" 
                                   :qualified? true
                                   :ns-alias "types"})))
    
    ;; The actual case from the bug
    (let [enum-ref {:name "jon-gui-data-system-localizations-values"
                    :qualified? false}]
      (is (= "jon-gui-data-system-localizations-values" 
             (tr/qualified-enum-ref enum-ref))))))

(deftest integration-enum-field-test
  (testing "full integration test for enum field generation"
    ;; Simulate the actual field that's causing the bug
    (let [field {:name :loc
                 :proto-name "loc"
                 :type {:enum {:type-ref ".ser.JonGuiDataSystemLocalizations"}}
                 :number 16
                 :label :label-optional
                 :optional? true}
          
          ;; Type lookup as it would be in real generation
          type-lookup {"ser.JonGuiDataSystemLocalizations" 
                       {:type :enum
                        :name :jon-gui-data-system-localizations
                        :proto-name "JonGuiDataSystemLocalizations"
                        :package "ser"}}
          
          ;; Generate field setter
          setter-code (frontend/generate-field-setter field "ser" type-lookup)]
      
      ;; Verify the generated code
      (testing "generated setter code"
        (is (string? setter-code))
        (is (re-find #"\(when \(contains\? m :loc\)" setter-code))
        (is (re-find #"\.setLoc builder" setter-code))
        ;; Most importantly: check the enum reference is correct
        (is (re-find #"jon-gui-data-system-localizations-values" setter-code))
        (is (not (re-find #"JonGuiDataSystemLocalizations-values" setter-code)))))))