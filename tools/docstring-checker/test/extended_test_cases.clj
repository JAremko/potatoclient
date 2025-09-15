(ns extended-test-cases
  "Test file with macro, multimethod, protocol, and record definitions")

;; =============================================================================
;; Macros - Should be checked
;; =============================================================================

(defmacro with-doc-macro
  "This macro has a docstring"
  [& body]
  `(do ~@body))

(defmacro no-doc-macro
  [x]
  `(println ~x))

(defmacro ^{:doc "Macro with metadata doc"} meta-doc-macro
  [& forms]
  `(do ~@forms))

;; =============================================================================
;; Multimethods - Should be checked (defmulti only, not defmethod)
;; =============================================================================

(defmulti process-event
  "Multimethod with docstring for processing events"
  :type)

(defmulti no-doc-multi
  :category)

(defmulti ^{:doc "Multimethod with metadata doc"} meta-multi
  (fn [x] (:kind x)))

;; defmethod implementations typically don't have docstrings
(defmethod process-event :click
  [event]
  (println "Click:" event))

(defmethod process-event :hover
  [event]
  (println "Hover:" event))

;; =============================================================================
;; Protocols - Should be checked
;; =============================================================================

(defprotocol Lifecycle
  "Protocol with docstring defining lifecycle methods"
  (start [this] "Start the component")
  (stop [this] "Stop the component"))

(defprotocol NoDocProtocol
  (method1 [this])
  (method2 [this x]))

;; =============================================================================
;; Records - Should be checked
;; =============================================================================

(defrecord DocumentedRecord
  "Record with a docstring"
  [field1 field2])

(defrecord UndocumentedRecord
  [x y z])

;; =============================================================================
;; Types - Should be checked
;; =============================================================================

(deftype DocumentedType
  "Type with a docstring"
  [^:volatile-mutable state]
  Object
  (toString [this] (str @state)))

(deftype UndocumentedType
  [value]
  Object
  (toString [this] (str value)))

;; =============================================================================
;; Specs (if using spec) - Often self-documenting, might skip
;; =============================================================================

(def some-spec
  "A spec with docstring"
  [:map [:id :int]])

(def no-doc-spec
  [:vector :string])

;; =============================================================================
;; Edge cases
;; =============================================================================

;; Empty docstring should count as missing
(defmacro ^{:doc ""} empty-doc-macro
  [x]
  x)

(defmulti empty-string-multi
  ""  ; Empty docstring
  :type)

;; Complex metadata
(defmacro ^{:private true :doc "Private macro with doc"} private-doc-macro
  [& args]
  `(vector ~@args))

;; Namespace-qualified definitions (less common but valid)
(defn test-cases/qualified-name
  "Function with qualified name"
  []
  :qualified)