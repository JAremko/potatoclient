(ns comprehensive-samples
  "Comprehensive test samples for Guardrails migration"
  )

;; ============================================================================
;; Simple cases
;; ============================================================================

(defn simple-no-args
  "A function with no arguments"
  []
  [=> string?]
  "hello")

(defn simple-one-arg
  [x]
  [int? => string?]
  (str x))

(defn simple-two-args
  [x y]
  [int? string? => string?]
  (str x y))

;; ============================================================================
;; Private functions
;; ============================================================================

(defn- private-fn
  [x]
  [any? => nil?]
  nil)

;; ============================================================================
;; Docstrings and metadata
;; ============================================================================

(defn with-docstring
  "This function has a docstring"
  [x]
  [int? => int?]
  (* x 2))

(defn with-metadata
  {:author "test"
   :added "1.0"}
  [x]
  [any? => any?]
  x)

(defn with-both
  "Has docstring"
  {:private true}
  [x]
  [any? => any?]
  x)

;; ============================================================================
;; Maybe/optional specs
;; ============================================================================

(defn with-maybe
  [required optional]
  [string? (? int?) => string?]
  (if optional
    (str required "-" optional)
    required))

(defn maybe-return
  [x]
  [int? => (? string?)]
  (when (pos? x)
    (str x)))

;; ============================================================================
;; Qualified keywords and namespaces
;; ============================================================================

(defn qualified-keyword-spec
  [theme]
  [:potatoclient.ui-specs/theme-key => any?]
  (println theme))

(defn qualified-return
  [x]
  [any? => :potatoclient.ui-specs/config]
  {:theme :dark})

;; ============================================================================
;; Collection specs
;; ============================================================================

(defn vector-spec
  [items]
  [[:vector string?] => int?]
  (count items))

(defn sequential-spec
  [items]
  [[:sequential ifn?] => nil?]
  nil)

(defn map-spec
  [config]
  [[:map [:version string?] [:timestamp string?]] => any?]
  config)

;; ============================================================================
;; Complex Malli specs (already in Malli format)
;; ============================================================================

(defn with-fn-spec
  [validator]
  [[:fn {:error/message "must be a validator"}
    #(ifn? %)] => boolean?]
  (validator 42))

(defn with-maybe-complex
  [x]
  [[:maybe [:fn {:error/message "positive"} pos?]] => any?]
  x)

;; ============================================================================
;; Variadic functions
;; ============================================================================

(defn variadic
  [x & xs]
  [string? [:* any?] => string?]
  (apply str x xs))

(defn variadic-with-types
  [x y & zs]
  [int? string? [:* keyword?] => any?]
  [x y zs])

;; ============================================================================
;; Multi-arity functions
;; ============================================================================

(defn multi-arity
  ([x]
   [int? => int?]
   (* x 2))
  ([x y]
   [int? int? => int?]
   (+ x y))
  ([x y z]
   [int? int? int? => int?]
   (+ x y z)))

(defn multi-with-docstring
  "Multi-arity with docstring"
  ([x]
   [string? => string?]
   (str/upper-case x))
  ([x y]
   [string? string? => string?]
   (str x " " y)))

(defn multi-with-metadata
  "Multi-arity with metadata"
  {:author "test"}
  ([x]
   [int? => int?]
   x)
  ([x y]
   [int? int? => int?]
   (+ x y)))

;; ============================================================================
;; Edge cases
;; ============================================================================

(defn empty-gspec
  "Function with empty gspec (no validation)"
  [x]
  []
  x)

(defn only-return-spec
  [x y]
  [=> any?]
  (+ x y))

(defn with-such-that
  "Function with such-that constraint (| clause)"
  [x y]
  [int? int? | #(< % 100) => pos-int?]
  (+ x y))

;; ============================================================================
;; Nested collections
;; ============================================================================

(defn nested-maps
  [x]
  [[:map-of keyword? [:vector string?]] => any?]
  x)

(defn nested-vectors
  [matrix]
  [[:vector [:vector number?]] => number?]
  (reduce + (map #(reduce + %) matrix)))

;; ============================================================================
;; Special predicates
;; ============================================================================

(defn special-preds
  [a b c d]
  [nil? boolean? fn? ifn? => any?]
  [a b c d])

;; ============================================================================
;; Short form (using just >)
;; ============================================================================

(defn short-form {:malli/schema [:=> [:cat :int] :int]} [x] (* x 2))

;; ============================================================================
;; Instance checks
;; ============================================================================

(defn instance-check
  [file]
  [[:fn #(instance? java.io.File %)] => nil?]
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