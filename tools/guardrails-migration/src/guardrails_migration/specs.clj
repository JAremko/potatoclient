(ns guardrails-migration.specs
  "Malli specs for validating Guardrails forms and defn output"
  (:require [malli.core :as m]))

;; ============================================================================
;; Guardrails gspec components
;; ============================================================================

(def gspec-operator
  "Valid gspec operators: =>, |, ?"
  [:enum '=> '| '?])

(def simple-spec
  "Simple spec predicates like int?, string?, keyword?"
  [:or
   symbol?        ; int?, string?, etc.
   keyword?       ; :int, :string, ::my-spec
   [:sequential any?]]) ; Complex specs

(def such-that-clause
  "Such-that clause: | followed by predicate function(s)"
  [:cat
   [:= '|]
   [:+ any?]]) ; One or more predicates

(def gspec-vector
  "Guardrails gspec vector: [arg-specs* (| preds)? => ret-spec (| preds)?]"
  [:and
   vector?
   [:fn {:error/message "Must contain =>"}
    (fn [v] (some #(= '=> %) v))]])

;; ============================================================================
;; Function definition patterns
;; ============================================================================

(def function-name
  "Valid function name"
  [:and symbol? [:fn (complement namespace)]])

(def docstring
  "Optional docstring"
  string?)

(def attr-map
  "Optional attribute map"
  map?)

(def binding-vector
  "Function argument binding vector"
  [:and
   vector?
   [:fn {:error/message "Valid binding vector"}
    (fn [v] (every? #(or (symbol? %) (= '& %)) v))]])

(def single-arity-body
  "Single arity function: [args] gspec? body+"
  [:cat
   binding-vector
   [:? gspec-vector]  ; Optional gspec
   [:* any?]])        ; Body forms

(def multi-arity-clause
  "Multi-arity clause: ([args] gspec? body+)"
  [:and
   list?
   [:cat
    binding-vector
    [:? gspec-vector]  ; Optional gspec
    [:* any?]]])       ; Body forms

(def multi-arity-body
  "Multi-arity function body"
  [:+ multi-arity-clause])

;; ============================================================================
;; Complete Guardrails form specs
;; ============================================================================

(def guardrails-defn
  "Complete >defn or >defn- form"
  [:and
   list?
   [:cat
    [:enum '>defn '>defn-]
    function-name
    [:? docstring]
    [:? attr-map]
    [:alt
     ;; Single arity
     [:cat
      binding-vector
      [:? gspec-vector]
      [:* any?]]
     ;; Multi-arity
     multi-arity-body]]])

;; ============================================================================
;; Output defn specs (with Malli metadata)
;; ============================================================================

(def malli-schema-metadata
  "Malli schema in metadata"
  [:map
   [:malli/schema any?]
   [:* [keyword? any?]]])

(def defn-with-malli
  "Regular defn with :malli/schema metadata"
  [:and
   list?
   [:cat
    [:enum 'defn 'defn-]
    function-name
    [:? docstring]
    [:? [:and map? [:fn #(contains? % :malli/schema)]]]
    [:alt
     ;; Single arity (no gspec in body)
     [:cat
      binding-vector
      [:* any?]]
     ;; Multi-arity (no gspec in bodies)
     [:+ [:and
          list?
          [:cat
           binding-vector
           [:* any?]]]]]]])

;; ============================================================================
;; Validation functions
;; ============================================================================

(defn valid-guardrails-form?
  "Check if a form is a valid Guardrails >defn"
  [form]
  (m/validate guardrails-defn form))

(defn valid-output-form?
  "Check if a form is a valid defn with Malli metadata"
  [form]
  (m/validate defn-with-malli form))

(defn explain-guardrails-form
  "Explain why a Guardrails form is invalid"
  [form]
  (m/explain guardrails-defn form))

(defn explain-output-form
  "Explain why an output form is invalid"
  [form]
  (m/explain defn-with-malli form))