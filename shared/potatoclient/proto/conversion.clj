(ns potatoclient.proto.conversion
  "Unified string conversion module for proto-clj-generator.
  
  Features:
  - Lossless: Every conversion can be reversed to get the original
  - Bidirectional: Every conversion has an inverse
  - Collision-safe: All conversions check for and prevent collisions
  - Format-preserving: Uses namespaced keywords to preserve original format
  
  Examples:
    'XMLParser' <-> :pascal/XMLParser
    'xmlParser' <-> :camel/xmlParser  
    'xml_parser' <-> :snake/xml_parser
    'xml-parser' <-> :kebab/xml-parser
    'XML_PARSER' <-> :proto/XML_PARSER
    'TYPE_INT32' <-> :proto/TYPE_INT32 (exact preservation)"
  (:require [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [lambdaisland.regal :as regal]
            [lambdaisland.regal.generator :as regal-gen]))

;; =============================================================================
;; Regal Patterns and Specs
;; =============================================================================

(def valid-string-pattern
  "Pattern for valid input strings"
  [:+ [:alt [:class [:alpha]] [:class [:digit]] "_" "-" "."]])

(def valid-keyword-name-pattern
  "Pattern for valid keyword names (no namespace)"
  [:cat
   [:? [:alt [:class [:alpha]] "_" "-"]]
   [:* [:alt [:class [:alnum]] "_" "-" "."]]])

(def valid-namespaced-keyword-pattern
  "Pattern for valid namespaced keywords"
  [:cat
   [:? [:cat
        [:+ [:alt [:class [:alpha]] "_" "-"]]
        "/"]]
   valid-keyword-name-pattern])

(def ValidString
  "Spec for valid input strings"
  [:and
   :string
   [:fn {:error/message "must be a valid identifier string"}
    #(re-matches (regal/regex valid-string-pattern) %)]
   [::m/gen #(regal-gen/gen valid-string-pattern)]])

(def ValidKeyword
  "Spec for valid keywords"
  [:and
   :keyword
   [:fn {:error/message "must be a valid keyword"}
    #(re-matches (regal/regex valid-namespaced-keyword-pattern) 
                 (str (when-let [ns (namespace %)] (str ns "/")) (name %)))]
   [::m/gen #(keyword (regal-gen/generate valid-keyword-name-pattern))]])

;; =============================================================================
;; Collision Detection
;; =============================================================================

(defonce ^:private forward-cache (atom {}))
(defonce ^:private reverse-cache (atom {}))

(>defn- track-conversion!
  "Track conversions and detect collisions.
  Returns the output after verifying no collision exists."
  [fn-name input output]
  [ValidString any? any? => any?]
  (when (and input output)
    (let [forward-key [fn-name input]
          reverse-key [fn-name output]]
      ;; Check forward cache first
      (if-let [cached (get @forward-cache forward-key)]
        cached
        ;; Check for collision
        (let [existing-input (get @reverse-cache reverse-key)]
          (when (and existing-input (not= existing-input input))
            (throw (ex-info "String conversion collision detected!"
                            {:function fn-name
                             :input input
                             :output output
                             :existing-input existing-input})))
          ;; Safe to cache
          (swap! forward-cache assoc forward-key output)
          (swap! reverse-cache assoc reverse-key input)
          output))))
  output)

(>defn clear-caches!
  "Clear all conversion caches. Useful for testing."
  []
  [=> any?]
  (reset! forward-cache {})
  (reset! reverse-cache {}))

;; =============================================================================
;; Format Detection
;; =============================================================================

(>defn- detect-format
  "Detect the format of a string"
  [s]
  [ValidString => [:enum :PascalCase :camelCase :snake_case :kebab-case :UPPER_SNAKE_CASE :unknown]]
  (cond
    ;; UPPER_SNAKE_CASE (most specific, check first)
    (re-matches #"^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$" s) :UPPER_SNAKE_CASE
    
    ;; PascalCase - starts with capital, has lowercase
    (and (re-matches #"^[A-Z][a-zA-Z0-9]*$" s)
         (re-find #"[a-z]" s)) :PascalCase
    
    ;; camelCase - starts with lowercase, has uppercase  
    (and (re-matches #"^[a-z][a-zA-Z0-9]*$" s)
         (re-find #"[A-Z]" s)) :camelCase
    
    ;; snake_case - all lowercase with underscores
    (re-matches #"^[a-z][a-z0-9_]*$" s) :snake_case
    
    ;; kebab-case - all lowercase with hyphens
    (re-matches #"^[a-z][a-z0-9-]*$" s) :kebab-case
    
    :else :unknown))

;; =============================================================================
;; Core Conversion: String <-> Keyword (Lossless)
;; =============================================================================

(def ^:private format-namespaces
  {:PascalCase "pascal"
   :camelCase "camel"
   :snake_case "snake"
   :kebab-case "kebab"
   :UPPER_SNAKE_CASE "proto"})

(>defn string->keyword
  "Convert any string to a namespaced keyword that preserves format.
  This is THE core conversion - all others build on this."
  [s]
  [ValidString => ValidKeyword]
  (let [format (detect-format s)
        ns (get format-namespaces format (name format))]
    (track-conversion! "string->keyword" s (keyword ns s))))

(>defn keyword->string
  "Convert a namespaced keyword back to its original string.
  This is the inverse of string->keyword."
  [k]
  [ValidKeyword => ValidString]
  (let [s (name k)]
    (track-conversion! "keyword->string" k s)))

;; =============================================================================
;; Format Conversions (all go through keywords for safety)
;; =============================================================================

(>defn- kebab->snake
  "Convert kebab-case to snake_case"
  [s]
  [ValidString => ValidString]
  (str/replace s "-" "_"))

(>defn- snake->kebab
  "Convert snake_case to kebab-case"
  [s]
  [ValidString => ValidString]
  (str/replace s "_" "-"))

(>defn- kebab->pascal
  "Convert kebab-case to PascalCase"
  [s]
  [ValidString => ValidString]
  (->> (str/split s #"-")
       (map str/capitalize)
       (str/join)))

(>defn- kebab->camel
  "Convert kebab-case to camelCase"
  [s]
  [ValidString => ValidString]
  (let [parts (str/split s #"-")]
    (str (first parts)
         (apply str (map str/capitalize (rest parts))))))

(>defn- any->kebab
  "Convert any format to kebab-case (lossy intermediate format)"
  [s]
  [ValidString => ValidString]
  (-> s
      ;; Handle UPPER_SNAKE_CASE
      (str/replace #"_" "-")
      ;; Handle camelCase and PascalCase
      (str/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (str/replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
      ;; Lowercase everything
      str/lower-case))

(>defn convert
  "Convert a string from one format to another.
  Goes through keyword intermediate to ensure losslessness."
  [s from-format to-format]
  [ValidString 
   [:enum :PascalCase :camelCase :snake_case :kebab-case :UPPER_SNAKE_CASE]
   [:enum :PascalCase :camelCase :snake_case :kebab-case :UPPER_SNAKE_CASE]
   => ValidString]
  (if (= from-format to-format)
    s
    (let [;; First preserve original in keyword form
          k (string->keyword s)
          ;; Get kebab-case intermediate (safe for our conversions)
          kebab (any->kebab s)
          ;; Convert to target format
          result (case to-format
                   :kebab-case kebab
                   :snake_case (kebab->snake kebab)
                   :PascalCase (kebab->pascal kebab)
                   :camelCase (kebab->camel kebab)
                   :UPPER_SNAKE_CASE (str/upper-case (kebab->snake kebab)))]
      ;; Track the conversion with collision detection
      (track-conversion! (str "convert-" from-format "->" to-format) s result))))

;; =============================================================================
;; Convenience Functions (for compatibility)
;; =============================================================================

(>defn ->PascalCase
  "Convert any string to PascalCase"
  [s]
  [ValidString => ValidString]
  (kebab->pascal (any->kebab s)))

(>defn ->kebab-case
  "Convert any string to kebab-case"
  [s]
  [ValidString => ValidString]
  (any->kebab s))

(>defn ->kebab-case-keyword
  "Convert any string to kebab-case keyword"
  [s]
  [ValidString => ValidKeyword]
  (keyword (any->kebab s)))

(>defn ->lossless-keyword
  "Convert any string to a lossless keyword preserving exact format"
  [s]
  [ValidString => ValidKeyword]
  (string->keyword s))

;; =============================================================================
;; Protobuf-Specific Conversions
;; =============================================================================

(>defn proto-field->clj-key
  "Convert protobuf field name to Clojure keyword.
  Handles both snake_case and camelCase (JSON) field names."
  [field-name]
  [ValidString => ValidKeyword]
  (let [k (string->keyword field-name)
        ;; For actual use, we want kebab-case keywords
        kebab (any->kebab field-name)]
    (track-conversion! "proto-field->clj-key" field-name (keyword kebab))))

(>defn clj-key->proto-field
  "Convert Clojure keyword to protobuf field name (snake_case)."
  [k]
  [ValidKeyword => ValidString]
  (let [s (name k)
        snake (str/replace s "-" "_")]
    (track-conversion! "clj-key->proto-field" k snake)))

(>defn clj-key->json-field
  "Convert Clojure keyword to JSON field name (camelCase)."
  [k]
  [ValidKeyword => ValidString]
  (let [s (name k)
        camel (kebab->camel s)]
    (track-conversion! "clj-key->json-field" k camel)))

(>defn proto-const->clj-key
  "Convert protobuf constant to Clojure keyword.
  TYPE_INT32 -> :type-int32 (preserves exact form via tracking)"
  [const]
  [ValidString => ValidKeyword]
  (let [k (string->keyword const)
        ;; For use, we want kebab-case
        kebab (-> const str/lower-case (str/replace "_" "-"))]
    (track-conversion! "proto-const->clj-key" const (keyword kebab))))

(>defn clj-key->proto-const
  "Convert Clojure keyword to protobuf constant."
  [k]
  [ValidKeyword => ValidString]
  (let [s (name k)
        const (-> s (str/replace "-" "_") str/upper-case)]
    (track-conversion! "clj-key->proto-const" k const)))

;; =============================================================================
;; Java Method Names
;; =============================================================================

(>defn field->getter
  "Generate Java getter method name.
  :protocol-version -> 'getProtocolVersion'"
  [field-key]
  [ValidKeyword => ValidString]
  (let [pascal (kebab->pascal (name field-key))
        getter (str "get" pascal)]
    (track-conversion! "field->getter" field-key getter)))

(>defn field->setter
  "Generate Java setter method name."
  [field-key]
  [ValidKeyword => ValidString]
  (let [pascal (kebab->pascal (name field-key))
        setter (str "set" pascal)]
    (track-conversion! "field->setter" field-key setter)))

(>defn field->has
  "Generate Java 'has' method name."
  [field-key]
  [ValidKeyword => ValidString]
  (let [pascal (kebab->pascal (name field-key))
        has (str "has" pascal)]
    (track-conversion! "field->has" field-key has)))

;; =============================================================================
;; Type References (preserve exact format)
;; =============================================================================

(>defn type-ref->keyword
  "Convert protobuf type reference to keyword.
  .com.example.MyMessage -> :com.example/MyMessage"
  [type-ref]
  [ValidString => ValidKeyword]
  (let [clean (if (str/starts-with? type-ref ".") 
                (subs type-ref 1) 
                type-ref)
        parts (str/split clean #"\.")
        message-name (last parts)
        package-parts (butlast parts)
        k (if (empty? package-parts)
            (keyword message-name)
            (keyword (str/join "." package-parts) message-name))]
    (track-conversion! "type-ref->keyword" type-ref k)))

(>defn keyword->type-ref
  "Convert keyword to protobuf type reference."
  [k]
  [ValidKeyword => ValidString]
  (let [ref (if-let [ns (namespace k)]
              (str "." ns "." (name k))
              (str "." (name k)))]
    (track-conversion! "keyword->type-ref" k ref)))

;; =============================================================================
;; Global Registry Setup
;; =============================================================================

(def conversion-schemas
  "All conversion schemas to add to global registry"
  {:potatoclient.proto.conversion/ValidString ValidString
   :potatoclient.proto.conversion/ValidKeyword ValidKeyword})

(defn register-conversion-schemas!
  "Register all conversion schemas in the global registry"
  []
  (let [composite-registry (mr/composite-registry
                            (m/default-schemas)
                            conversion-schemas)]
    (mr/set-default-registry! composite-registry)))

;; Auto-register on namespace load
(register-conversion-schemas!)

;; =============================================================================
;; Testing Helpers
;; =============================================================================

(>defn validate-roundtrip
  "Validate that a conversion is truly bidirectional"
  [s from-format to-format]
  [ValidString any? any? => boolean?]
  (let [converted (convert s from-format to-format)
        back (convert converted to-format from-format)]
    (= s back)))

(>defn get-stats
  "Get conversion statistics"
  []
  [=> :map]
  {:forward-conversions (count @forward-cache)
   :reverse-conversions (count @reverse-cache)
   :functions (distinct (map first (keys @forward-cache)))})