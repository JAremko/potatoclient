(ns comprehensive-samples
  "Comprehensive test samples for Guardrails migration"
  (:require [clojure.string :as str]))

;; ============================================================================
;; Simple cases
;; ============================================================================

(defn simple-no-args
  "A function with no arguments" {:malli/schema [:=> [:cat] :string]}
  []
  "hello")

(defn simple-one-arg {:malli/schema [:=> [:cat :int] :string]}
  [x]
  (str x))

(defn simple-two-args {:malli/schema [:=> [:cat :int :string] :string]}
  [x y]
  (str x y))

;; ============================================================================
;; Private functions
;; ============================================================================

(defn- private-fn {:malli/schema [:=> [:cat :any] :nil]}
  [x]
  nil)

;; ============================================================================
;; Docstrings and metadata
;; ============================================================================

(defn with-docstring
  "This function has a docstring" {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (* x 2))

(defn with-metadata
  {:added "1.0", :author "test", :malli/schema [:=> [:cat :any] :any]}
  [x]
  x)

(defn with-both
  "Has docstring"
  {:private true, :malli/schema [:=> [:cat :any] :any]}
  [x]
  x)

;; ============================================================================
;; Maybe/optional specs
;; ============================================================================

(defn with-maybe {:malli/schema [:=> [:cat :string [:maybe :int]] :string]}
  [required optional]
  (if optional
    (str required "-" optional)
    required))

(defn maybe-return {:malli/schema [:=> [:cat :int] [:maybe :string]]}
  [x]
  (when (pos? x)
    (str x)))

;; ============================================================================
;; Qualified keywords and namespaces
;; ============================================================================

(defn qualified-keyword-spec {:malli/schema [:=> [:cat :potatoclient.ui-specs/theme-key] :any]}
  [theme]
  (println theme))

(defn qualified-return {:malli/schema [:=> [:cat :any] :potatoclient.ui-specs/config]}
  [x]
  {:theme :dark})

;; ============================================================================
;; Collection specs
;; ============================================================================

(defn vector-spec {:malli/schema [:=> [:cat [:vector :string]] :int]}
  [items]
  (count items))

(defn sequential-spec {:malli/schema [:=> [:cat [:sequential :ifn]] :nil]}
  [items]
  nil)

(defn map-spec {:malli/schema [:=> [:cat [:map [:version :string] [:timestamp :string]]] :any]}
  [config]
  config)

;; ============================================================================
;; Complex Malli specs (already in Malli format)
;; ============================================================================

(defn with-fn-spec {:malli/schema [:=> [:cat [:fn {:error/message "must be a validator"} (fn* [p1__3756#] (ifn? p1__3756#))]] :boolean]}
  [validator]
  (validator 42))

(defn with-maybe-complex {:malli/schema [:=> [:cat [:maybe [:fn {:error/message "positive"} :pos]]] :any]}
  [x]
  x)

;; ============================================================================
;; Variadic functions
;; ============================================================================

(defn variadic {:malli/schema [:=> [:cat :string [:* :any]] :string]}
  [x & xs]
  (apply str x xs))

(defn variadic-with-types {:malli/schema [:=> [:cat :int :string [:* :keyword]] :any]}
  [x y & zs]
  [x y zs])

;; ============================================================================
;; Multi-arity functions
;; ============================================================================

(defn multi-arity {:malli/schema [:function [:=> [:cat :int] :int] [:=> [:cat :int :int] :int] [:=> [:cat :int :int :int] :int]]}
  ([x]
   (* x 2))
  ([x y]
   (+ x y))
  ([x y z]
   (+ x y z)))

(defn multi-with-docstring
  "Multi-arity with docstring" {:malli/schema [:function [:=> [:cat :string] :string] [:=> [:cat :string :string] :string]]}
  ([x]
   (str/upper-case x))
  ([x y]
   (str x " " y)))

(defn multi-with-metadata
  "Multi-arity with metadata"
  {:author "test", :malli/schema [:function [:=> [:cat :int] :int] [:=> [:cat :int :int] :int]]}
  ([x]
   x)
  ([x y]
   (+ x y)))

;; ============================================================================
;; Edge cases
;; ============================================================================

(defn empty-gspec
  "Function with empty gspec (no validation)"
  [x]
  []
  x)

(defn only-return-spec {:malli/schema [:=> [:cat] :any]}
  [x y]
  (+ x y))

(defn with-such-that
  "Function with such-that constraint (| clause)" {:malli/schema [:=> [:cat :int :int] :pos-int]}
  [x y]
  (+ x y))

;; ============================================================================
;; Nested collections
;; ============================================================================

(defn nested-maps {:malli/schema [:=> [:cat [:map-of :keyword [:vector :string]]] :any]}
  [x]
  x)

(defn nested-vectors {:malli/schema [:=> [:cat [:vector [:vector :number]]] :number]}
  [matrix]
  (reduce + (map #(reduce + %) matrix)))

;; ============================================================================
;; Special predicates
;; ============================================================================

(defn special-preds {:malli/schema [:=> [:cat :nil :boolean :fn :ifn] :any]}
  [a b c d]
  [a b c d])

;; ============================================================================
;; Short form (using just >)
;; ============================================================================

(defn short-form {:malli/schema [:=> [:cat :int] :int]} [x] (* x 2))

;; ============================================================================
;; Instance checks
;; ============================================================================

(defn instance-check {:malli/schema [:=> [:cat [:fn (fn* [p1__3760#] (instance? java.io.File p1__3760#))]] :nil]}
  [file]
  nil)

;; ============================================================================
;; Regular functions for comparison
;; ============================================================================

(defn regular-function
  "This should remain unchanged"
  [x]
  (println x))

(defn- regular-private
  [x y]
  (+ x y))