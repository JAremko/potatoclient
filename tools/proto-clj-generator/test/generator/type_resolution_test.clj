(ns generator.type_resolution_test
  "Unit tests for type resolution functions"
  (:require [clojure.test :refer [deftest testing is]]
            [generator.type-resolution :as tr]))

(deftest type-ref->package-test
  (testing "extracts package from type reference"
    (is (= "ser" (tr/type-ref->package ".ser.JonGuiDataRotaryMode")))
    (is (= "cmd.rotary" (tr/type-ref->package ".cmd.rotary.Root")))
    (is (= "state.heat" (tr/type-ref->package ".state.heat.HeatState"))))
  
  (testing "handles edge cases"
    (is (nil? (tr/type-ref->package nil)))
    (is (nil? (tr/type-ref->package "")))
    (is (nil? (tr/type-ref->package "SimpleType")))
    (is (nil? (tr/type-ref->package ".SimpleType")))))

(deftest resolve-enum-reference-test
  (testing "resolves local enum (same package)"
    (let [type-lookup {:".ser.JonGuiDataRotaryMode" {:name :jon-gui-data-rotary-mode
                                                      :package "ser"}}
          result (tr/resolve-enum-reference ".ser.JonGuiDataRotaryMode" "ser" type-lookup)]
      (is (= {:name "jon-gui-data-rotary-mode-values"
              :qualified? false}
             result))))
  
  (testing "resolves cross-package enum"
    (let [type-lookup {:".ser.JonGuiDataRotaryMode" {:name :jon-gui-data-rotary-mode
                                                      :package "ser"}}
          result (tr/resolve-enum-reference ".ser.JonGuiDataRotaryMode" "cmd.rotary" type-lookup)]
      (is (= {:name "jon-gui-data-rotary-mode-values"
              :qualified? true
              :ns-alias "types"}
             result))))
  
  (testing "handles ser.* package special case"
    (let [type-lookup {:".ser.foo.BarEnum" {:name :bar-enum
                                            :package "ser.foo"}}
          result (tr/resolve-enum-reference ".ser.foo.BarEnum" "cmd.rotary" type-lookup)]
      (is (= {:name "bar-enum-values"
              :qualified? true
              :ns-alias "types"}
             result))))
  
  (testing "fallback for missing type info"
    (let [result (tr/resolve-enum-reference ".unknown.MissingEnum" "cmd.rotary" {})]
      (is (= {:name "missing-enum-values"
              :qualified? true
              :ns-alias "unknown"}
             result))))
  
  (testing "fallback correctly converts to keyword"
    ;; This is the bug we fixed - ensure the fallback path works
    (let [result (tr/resolve-enum-reference ".package.SomeEnum" "other.package" {})]
      (is (string? (:name result)))
      (is (= "some-enum-values" (:name result))))))

(deftest resolve-enum-keyword-map-test
  (testing "resolves keyword map reference"
    (let [type-lookup {:".ser.JonGuiDataRotaryMode" {:name :jon-gui-data-rotary-mode
                                                      :package "ser"}}
          result (tr/resolve-enum-keyword-map ".ser.JonGuiDataRotaryMode" "ser" type-lookup)]
      (is (= {:name "jon-gui-data-rotary-mode-keywords"
              :qualified? false}
             result))))
  
  (testing "replaces -values with -keywords"
    (let [type-lookup {:".test.MyEnum" {:name :my-enum
                                        :package "test"}}
          result (tr/resolve-enum-keyword-map ".test.MyEnum" "test" type-lookup)]
      (is (= "my-enum-keywords" (:name result))))))

(deftest qualified-enum-ref-test
  (testing "generates qualified reference"
    (is (= "types/my-enum-values" 
           (tr/qualified-enum-ref {:name "my-enum-values" 
                                   :qualified? true 
                                   :ns-alias "types"}))))
  
  (testing "generates unqualified reference"
    (is (= "my-enum-values" 
           (tr/qualified-enum-ref {:name "my-enum-values" 
                                   :qualified? false}))))
  
  (testing "handles missing ns-alias"
    (is (= "my-enum-values" 
           (tr/qualified-enum-ref {:name "my-enum-values" 
                                   :qualified? false
                                   :ns-alias nil})))))

(deftest integration-test
  (testing "full flow from type ref to qualified string"
    (let [type-lookup {:".ser.JonGuiDataRotaryMode" {:name :jon-gui-data-rotary-mode
                                                      :package "ser"}}
          ;; From a different package
          enum-ref (tr/resolve-enum-reference ".ser.JonGuiDataRotaryMode" "cmd.rotary" type-lookup)
          qualified-ref (tr/qualified-enum-ref enum-ref)]
      (is (= "types/jon-gui-data-rotary-mode-values" qualified-ref)))
    
    ;; Same package
    (let [type-lookup {:".cmd.rotary.SomeEnum" {:name :some-enum
                                                 :package "cmd.rotary"}}
          enum-ref (tr/resolve-enum-reference ".cmd.rotary.SomeEnum" "cmd.rotary" type-lookup)
          qualified-ref (tr/qualified-enum-ref enum-ref)]
      (is (= "some-enum-values" qualified-ref)))))