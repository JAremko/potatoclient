(ns com.fulcrologic.guardrails.malli.core
  "Stub definitions for clj-kondo to understand Guardrails macros")

;; These are stub definitions that help clj-kondo understand Guardrails syntax
;; They don't need to actually work, just have the right shape

(defmacro >defn
  "Stub for Guardrails >defn macro"
  [name & args]
  (let [docstring (when (string? (first args)) (first args))
        args (if docstring (rest args) args)
        argvec (first args)
        specs (second args)
        body (drop 2 args)]
    `(defn ~name ~@(when docstring [docstring]) ~argvec ~@body)))

(defmacro >defn-
  "Stub for Guardrails >defn- macro"
  [name & args]
  (let [docstring (when (string? (first args)) (first args))
        args (if docstring (rest args) args)
        argvec (first args)
        specs (second args)
        body (drop 2 args)]
    `(defn ~(with-meta name {:private true}) ~@(when docstring [docstring]) ~argvec ~@body)))

(defmacro >def
  "Stub for Guardrails >def macro"
  [name spec value]
  `(def ~name ~value))

;; Spec operators
(def => ::return-spec)
(def | ::such-that)
(defn ? [spec] [:maybe spec])