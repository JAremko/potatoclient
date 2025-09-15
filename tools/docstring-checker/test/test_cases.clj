(ns test-cases
  "Test file with various docstring formats")

;; String docstrings
(defn with-string-doc
  "This has a string docstring"
  [x]
  x)

(defn- private-with-doc
  "Private function with docstring"
  []
  nil)

;; Metadata docstrings
(def ^{:doc "Definition with metadata doc"} with-meta-doc 42)

(defonce ^{:doc "Defonce with metadata doc"} once-with-doc (atom nil))

;; Missing docstrings
(defn no-doc-function
  [x y]
  (+ x y))

(defn- private-no-doc
  []
  :private)

(def no-doc-def 123)

(defonce no-doc-once (atom {}))

;; Complex metadata
(defn ^{:private true :doc "Has both private and doc metadata"} complex-meta
  [data]
  data)

;; Edge case - empty doc
(def ^{:doc ""} empty-doc-string "should be considered missing")

;; Multi-arity without doc
(defn multi-arity
  ([x] x)
  ([x y] (+ x y)))

;; Multi-arity with doc
(defn multi-arity-doc
  "This multi-arity has a doc"
  ([x] x)
  ([x y] (+ x y)))