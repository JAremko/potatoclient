[:children (<list:
  (ns comprehensive-samples
    "Comprehensive test samples for Guardrails migration"
    (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
              [clojure.string :as str]))
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Simple cases\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn simple-no-args
    "A function with no arguments"
    []
    [=> string?]
    "hello")
> <newline: "\n\n"> <list:
  (>defn simple-one-arg
    [x]
    [int? => string?]
    (str x))
> <newline: "\n\n"> <list:
  (>defn simple-two-args
    [x y]
    [int? string? => string?]
    (str x y))
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Private functions\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn- private-fn
    [x]
    [any? => nil?]
    nil)
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Docstrings and metadata\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn with-docstring
    "This function has a docstring"
    [x]
    [int? => int?]
    (* x 2))
> <newline: "\n\n"> <list:
  (>defn with-metadata
    {:author "test"
     :added "1.0"}
    [x]
    [any? => any?]
    x)
> <newline: "\n\n"> <list:
  (>defn with-both
    "Has docstring"
    {:private true}
    [x]
    [any? => any?]
    x)
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Maybe/optional specs\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn with-maybe
    [required optional]
    [string? (? int?) => string?]
    (if optional
      (str required "-" optional)
      required))
> <newline: "\n\n"> <list:
  (>defn maybe-return
    [x]
    [int? => (? string?)]
    (when (pos? x)
      (str x)))
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Qualified keywords and namespaces\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn qualified-keyword-spec
    [theme]
    [:potatoclient.ui-specs/theme-key => any?]
    (println theme))
> <newline: "\n\n"> <list:
  (>defn qualified-return
    [x]
    [any? => :potatoclient.ui-specs/config]
    {:theme :dark})
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Collection specs\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn vector-spec
    [items]
    [[:vector string?] => int?]
    (count items))
> <newline: "\n\n"> <list:
  (>defn sequential-spec
    [items]
    [[:sequential ifn?] => nil?]
    nil)
> <newline: "\n\n"> <list:
  (>defn map-spec
    [config]
    [[:map [:version string?] [:timestamp string?]] => any?]
    config)
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Complex Malli specs (already in Malli format)\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn with-fn-spec
    [validator]
    [[:fn {:error/message "must be a validator"}
      #(ifn? %)] => boolean?]
    (validator 42))
> <newline: "\n\n"> <list:
  (>defn with-maybe-complex
    [x]
    [[:maybe [:fn {:error/message "positive"} pos?]] => any?]
    x)
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Variadic functions\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn variadic
    [x & xs]
    [string? [:* any?] => string?]
    (apply str x xs))
> <newline: "\n\n"> <list:
  (>defn variadic-with-types
    [x y & zs]
    [int? string? [:* keyword?] => any?]
    [x y zs])
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Multi-arity functions\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn multi-arity
    ([x]
     [int? => int?]
     (* x 2))
    ([x y]
     [int? int? => int?]
     (+ x y))
    ([x y z]
     [int? int? int? => int?]
     (+ x y z)))
> <newline: "\n\n"> <list:
  (>defn multi-with-docstring
    "Multi-arity with docstring"
    ([x]
     [string? => string?]
     (str/upper-case x))
    ([x y]
     [string? string? => string?]
     (str x " " y)))
> <newline: "\n\n"> <list:
  (>defn multi-with-metadata
    "Multi-arity with metadata"
    {:author "test"}
    ([x]
     [int? => int?]
     x)
    ([x y]
     [int? int? => int?]
     (+ x y)))
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Edge cases\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn empty-gspec
    "Function with empty gspec (no validation)"
    [x]
    []
    x)
> <newline: "\n\n"> <list:
  (>defn only-return-spec
    [x y]
    [=> any?]
    (+ x y))
> <newline: "\n\n"> <list:
  (>defn with-such-that
    "Function with such-that constraint (| clause)"
    [x y]
    [int? int? | #(< % 100) => pos-int?]
    (+ x y))
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Nested collections\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn nested-maps
    [x]
    [[:map-of keyword? [:vector string?]] => any?]
    x)
> <newline: "\n\n"> <list:
  (>defn nested-vectors
    [matrix]
    [[:vector [:vector number?]] => number?]
    (reduce + (map #(reduce + %) matrix)))
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Special predicates\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn special-preds
    [a b c d]
    [nil? boolean? fn? ifn? => any?]
    [a b c d])
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Short form (using just >)\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list: (> short-form [x] [int? => int?] (* x 2))> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Instance checks\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (>defn instance-check
    [file]
    [[:fn #(instance? java.io.File %)] => nil?]
    nil)
> <newline: "\n\n"> <comment: ";; ============================================================================\n"> <comment: ";; Regular functions for comparison\n"> <comment: ";; ============================================================================\n"> <newline: "\n"> <list:
  (defn regular-function
    "This should remain unchanged"
    [x]
    (println x))
> <newline: "\n\n"> <list:
  (defn- regular-private
    [x y]
    (+ x y))
>)]