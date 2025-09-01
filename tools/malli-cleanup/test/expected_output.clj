(ns sample.input
  "Sample file for testing Malli metadata transformation"
  (:require [clojure.string :as str])
  (:import (java.io File)
           (javax.swing JFrame JPanel)))

;; Case 1: Simple function with inline metadata (needs moving)
(defn simple-inline
  "Simple function with inline malli metadata"
  {:malli/schema [:=> [:cat :string] :string]}
  [x]
  (str/upper-case x))

;; Case 2: Function with attr-map after docstring (already correct)
(defn already-correct
  "Function with metadata in correct position"
  {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))

;; Case 3: Private function with inline metadata (needs moving)
(defn- private-inline
  "Private function with inline metadata"
  {:malli/schema [:=> [:cat :int :int] :int]}
  [x y]
  (+ x y))

;; Case 4: Function without docstring but with metadata (needs moving)
(defn no-docstring
  {:malli/schema [:=> [:cat :string] :keyword]}
  [s]
  (keyword s))

;; Case 5: Multi-arity function
(defn multi-arity
  "Multi-arity function"
  {:malli/schema [:function
                  [:=> [:cat :string] :string]
                  [:=> [:cat :string :string] :string]]}
  ([x]
   (str/upper-case x))
  ([x y]
   (str x " " y)))

;; Case 6: Function with lambda that can be converted to partial
(defn with-simple-lambda
  "Function with simple instance check lambda"
  {:malli/schema [:=> [:cat [:fn (partial instance? File)]] :boolean]}
  [file]
  (.exists file))

;; Case 7: Function with multiple lambdas
(defn with-multiple-lambdas
  "Function with multiple lambdas"
  {:malli/schema [:=> [:cat 
                       [:fn (partial instance? JFrame)]
                       [:fn (partial instance? JPanel)]]
                  :nil]}
  [frame panel]
  (.add frame panel))

;; Case 8: Function with complex lambda (cannot be converted)
(defn with-complex-lambda
  "Function with complex lambda that cannot be converted"
  {:malli/schema [:=> [:cat [:fn (fn* [p1__1237#] (and (string? p1__1237#) 
                                                        (> (count p1__1237#) 5)))]] 
                  :boolean]}
  [s]
  (str/starts-with? s "test"))

;; Case 9: Function with error message in lambda
(defn with-error-message
  "Function with lambda containing error message"
  {:malli/schema [:=> [:cat [:fn {:error/message "must be a File"} 
                              (partial instance? File)]] 
                  :string]}
  [file]
  (.getName file))

;; Case 10: Function with predicate lambda
(defn with-predicate-lambda
  "Function with predicate lambda"
  {:malli/schema [:=> [:cat [:fn string?]] :keyword]}
  [s]
  (keyword s))

;; Case 11: Function with custom predicate lambda
(defn with-custom-predicate
  "Function with custom predicate lambda"
  {:malli/schema [:=> [:cat [:fn instant?]] :int]}
  [inst]
  (.getYear inst))

;; Case 12: Function with nested structure and lambdas
(defn with-nested-schema
  "Function with nested schema structure"
  {:malli/schema [:=> [:cat [:map 
                              [:file [:fn (partial instance? File)]]
                              [:name :string]]]
                  :boolean]}
  [{:keys [file name]}]
  (= (.getName file) name))

;; Case 13: Regular function without malli metadata (should be ignored)
(defn regular-function
  "This should stay unchanged"
  [x]
  (println x))