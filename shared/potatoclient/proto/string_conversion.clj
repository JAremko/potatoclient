(ns potatoclient.proto.string-conversion
  "Centralized string conversion utilities for proto-clj-generator.
  Replaces camel-snake-kebab with our own implementations that are:
  1. Lossless for protobuf constants
  2. Predictable and simple
  3. Tailored to our specific needs"
  (:require [clojure.string :as str]
            [potatoclient.proto.constants :as constants]
            [potatoclient.proto.string-conversion-specs :as specs]
            [malli.core :as m]
            [ghostwheel.core :as g :refer [>defn >defn- => |]]
            [clojure.core :as core])))

;; =============================================================================
;; Collision Detection and Caching
;; =============================================================================

(defonce ^:private forward-cache
  "Maps [function-name input] -> output"
  (atom {}))

(defonce ^:private reverse-cache
  "Maps [function-name output] -> input"
  (atom {}))

(defn clear-conversion-caches!
  "Clear all conversion caches. Useful for testing."
  []
  (reset! forward-cache {})
  (reset! reverse-cache {}))

(defn- track-conversion!
  "Track conversions and detect collisions.
  Returns cached value if exists, otherwise caches and returns new value.
  Throws if collision detected."
  [fn-name input output]
  (when (and input output)
    (let [forward-key [fn-name input]
          reverse-key [fn-name output]]
      ;; Check forward cache first
      (if-let [cached (get @forward-cache forward-key)]
        cached  ; Return cached value
        ;; New conversion - check for reverse collision
        (let [existing-input (get @reverse-cache reverse-key)]
          (when (and existing-input (not= existing-input input))
            (throw (ex-info "String conversion collision detected!"
                            {:function fn-name
                             :input input
                             :output output
                             :existing-input existing-input
                             :message (str "Both '" existing-input "' and '" input 
                                          "' convert to '" output "' in " fn-name)})))
          ;; Safe to cache
          (swap! forward-cache assoc forward-key output)
          (swap! reverse-cache assoc reverse-key input)
          output))))
  output)

(defn get-conversion-stats
  "Get statistics about conversions"
  []
  {:total-conversions (count @forward-cache)
   :unique-outputs (count @reverse-cache)
   :functions (distinct (map first (keys @forward-cache)))})

;; =============================================================================
;; Case Detection
;; =============================================================================

(>defn- camel-case?
  "Check if a string is in camelCase or PascalCase"
  [s]
  (and (string? s)
       (re-matches #"[a-zA-Z][a-zA-Z0-9]*" s)
       (or (re-find #"[a-z][A-Z]" s)      ; camelCase
           (re-matches #"[A-Z][a-z].*" s)))) ; PascalCase

(>defn- kebab-case?
  "Check if a string is in kebab-case"
  [s]
  (and (string? s)
       (re-matches #"[a-z][a-z0-9-]*" s)))

(>defn- snake-case?
  "Check if a string is in snake_case"
  [s]
  (and (string? s)
       (re-matches #"[a-z][a-z0-9_]*" s)))

(>defn- proto-constant?
  "Check if a string is a protobuf constant (UPPER_SNAKE_CASE)"
  [s]
  (constants/valid-proto-const? s))

;; =============================================================================
;; Conversions TO kebab-case
;; =============================================================================

(>defn ->kebab-case
  "Convert a string to kebab-case.
  Handles: camelCase, PascalCase, snake_case, UPPER_SNAKE_CASE"
  [s]
  [[:or :string :nil] => [:or ::specs/KebabCaseString :nil]]
  (when (string? s)
    (let [result (cond
                   ;; Already kebab-case
                   (kebab-case? s) s
                   
                   ;; Proto constant - use our lossless converter
                   (proto-constant? s) (name (constants/proto-const->keyword s))
                   
                   ;; Snake case - just replace underscores
                   (snake-case? s) (str/replace s "_" "-")
                   
                   ;; Camel/Pascal case - insert hyphens before capitals
                   :else (-> s
                            ;; Handle sequences like "XMLParser" -> "XML-Parser"
                            (str/replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
                            ;; Normal case changes "camelCase" -> "camel-Case"
                            (str/replace #"([a-z0-9])([A-Z])" "$1-$2")
                            ;; Lowercase everything
                            str/lower-case))]
      (track-conversion! "->kebab-case" s result))))

(>defn ->kebab-case-keyword
  "Convert a string to a kebab-case keyword"
  [s]
  [[:or :string :nil] => [:or :keyword :nil]]
  (when s
    (let [result (keyword (->kebab-case s))]
      (track-conversion! "->kebab-case-keyword" s result)))))

;; =============================================================================
;; Conversions TO PascalCase
;; =============================================================================

(>defn ->PascalCase
  "Convert a string to PascalCase.
  Used for Java class names and method names."
  [s]
  [[:or :string :nil] => [:or ::specs/PascalCaseString :nil]]
  (when (string? s)
    (let [result (->> (str/split (->kebab-case s) #"-")
                      (map str/capitalize)
                      (str/join))]
      (track-conversion! "->PascalCase" s result)))))

;; =============================================================================
;; Conversions TO snake_case
;; =============================================================================

(>defn ->snake_case
  "Convert a string to snake_case.
  Rarely used, but included for completeness."
  [s]
  [[:or :string :nil] => [:or ::specs/SnakeCaseString :nil]]
  (when (string? s)
    (let [result (-> (->kebab-case s)
                     (str/replace "-" "_"))]
      (track-conversion! "->snake_case" s result)))))

;; =============================================================================
;; Specialized Conversions
;; =============================================================================

(>defn proto-name->clj-name
  "Convert a protobuf name to idiomatic Clojure name.
  Examples:
    MessageType -> message-type
    ENUM_VALUE -> :enum-value
    field_name -> field-name"
  [s]
  [[:or :string :keyword :nil] => [:or :keyword :string :nil]]
  (when s
    (let [result (cond
                   (proto-constant? s) (constants/proto-const->keyword s)
                   (string? s) (->kebab-case-keyword s)
                   (keyword? s) s
                   :else (str s))]
      (track-conversion! "proto-name->clj-name" s result)))))

(>defn clj-name->proto-name
  "Convert a Clojure name back to protobuf name.
  This is the inverse of proto-name->clj-name for keywords."
  [k]
  [[:or :keyword :string :nil] => [:or :string :keyword :nil]]
  (when k
    (let [result (cond
                   (keyword? k) (constants/keyword->proto-const k)
                   (string? k) k
                   :else (str k))]
      (track-conversion! "clj-name->proto-name" k result)))))

;; =============================================================================
;; JSON Key Conversion
;; =============================================================================

(>defn json-key->clj-key
  "Convert a JSON key (string) to a Clojure keyword.
  Used when parsing protobuf descriptor JSON files."
  [k]
  [:string => :keyword]
  (when k
    (let [result (->kebab-case-keyword k)]
      (track-conversion! "json-key->clj-key" k result)))))

;; =============================================================================
;; Method Name Generation
;; =============================================================================

(>defn getter-method-name
  "Generate a Java getter method name for a field"
  [field-name]
  [::specs/FieldNameString => [:re #"^get[A-Z][a-zA-Z0-9]*$"]]
  (when field-name
    (let [result (str "get" (->PascalCase field-name))]
      ;; Method names don't need collision tracking as they're prefixed
      result)))

(>defn setter-method-name
  "Generate a Java setter method name for a field"
  [field-name]
  [::specs/FieldNameString => [:re #"^set[A-Z][a-zA-Z0-9]*$"]]
  (when field-name
    (str "set" (->PascalCase field-name))))

(>defn has-method-name
  "Generate a Java 'has' method name for a field"
  [field-name]
  [::specs/FieldNameString => [:re #"^has[A-Z][a-zA-Z0-9]*$"]]
  (when field-name
    (str "has" (->PascalCase field-name))))

(>defn add-method-name
  "Generate a Java 'add' method name for a repeated field"
  [field-name]
  [::specs/FieldNameString => [:re #"^add[A-Z][a-zA-Z0-9]*$"]]
  (when field-name
    (str "add" (->PascalCase field-name))))

(>defn add-all-method-name
  "Generate a Java 'addAll' method name for a repeated field"
  [field-name]
  [::specs/FieldNameString => [:re #"^addAll[A-Z][a-zA-Z0-9]*$"]]
  (when field-name
    (str "addAll" (->PascalCase field-name))))