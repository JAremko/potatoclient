(ns sample.guardrails
  "Sample file with various Guardrails patterns"
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]))

;; Simple function
(>defn simple-fn
  [x]
  [int? => string?]
  (str x))

;; Function with docstring
(>defn with-doc
  "Converts integer to string"
  [x]
  [int? => string?]
  (str "Number: " x))

;; Private function
(>defn- private-helper
  [x y]
  [int? int? => int?]
  (+ x y))

;; Multi-arity function
(>defn multi-arity
  ([x]
   [int? => int?]
   (inc x))
  ([x y]
   [int? int? => int?]
   (+ x y))
  ([x y z]
   [int? int? int? => int?]
   (+ x y z)))

;; Variadic function
(>defn variadic-sum
  [x & xs]
  [int? [:* int?] => int?]
  (apply + x xs))

;; With such-that clause
(>defn positive-double
  [x]
  [int? | #(> % 0) => int? | #(> % %1)]
  (* x 2))

;; Nilable arguments
(>defn handle-maybe
  [x]
  [(? int?) => string?]
  (if x
    (str "Value: " x)
    "No value"))

;; Complex spec
(>defn process-map
  [m]
  [[:map [:id int?] [:name string?]] => [:vector keyword?]]
  [(keyword (:name m))])

;; Regular function (should not be touched)
(defn regular-fn
  [x]
  (inc x))

;; Function with metadata
(>defn with-metadata
  {:author "Test"
   :since "1.0"}
  [x]
  [string? => keyword?]
  (keyword x))