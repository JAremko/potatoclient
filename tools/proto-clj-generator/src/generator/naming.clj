(ns generator.naming
  "Centralized naming conversions between protobuf and Clojure.
  
  This module ensures LOSSLESS bidirectional transformations:
  - Package names (e.g. cmd.DayCamera <-> :potatoclient.proto/cmd.DayCamera)
  - Type references (e.g. .cmd.DayCamera.Root <-> :potatoclient.proto/cmd.DayCamera.Root)
  - Preserves all original casing and structure
  
  The key insight is to encode the proto structure directly in Clojure keywords
  rather than trying to convert to idiomatic Clojure names.
  
  All conversions are memoized and checked for collisions to guarantee 1-to-1 mappings."
  (:require [clojure.string :as str]
            [potatoclient.proto.conversion :as conv]
            [malli.core :as m]
            [malli.generator :as mg]))

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def proto-package-spec
  "Spec for protobuf package names like 'cmd.DayCamera'"
  [:re #"^[a-z][a-z0-9]*(\.[A-Za-z][A-Za-z0-9_]*)*$"])

(def proto-type-ref-spec
  "Spec for protobuf type references like '.cmd.DayCamera.Root'"
  [:re #"^\.?[a-z][a-z0-9]*(\.[A-Za-z][A-Za-z0-9_]*)+$"])

(def proto-identifier-spec
  "Spec for a single protobuf identifier (PascalCase or snake_case)"
  [:re #"^[A-Za-z_][A-Za-z0-9_]*$"])

(def clojure-namespace-spec
  "Spec for Clojure namespace strings like 'cmd.daycamera'"
  [:re #"^[a-z][a-z0-9_-]*(\.[a-z][a-z0-9_-]*)*$"])

(def clojure-keyword-spec
  "Spec for Clojure namespaced keywords like :cmd.daycamera/root"
  [:and keyword? 
   [:fn (fn [k] (namespace k))]])

(def proto-encoded-keyword-spec
  "Spec for our special proto-encoded keywords like :potatoclient.proto/cmd.DayCamera.Root"
  [:and keyword?
   [:fn (fn [k] (= (namespace k) "potatoclient.proto"))]
   [:fn (fn [k] (re-matches #"^[a-z][a-z0-9]*(\.[A-Za-z][A-Za-z0-9_]*)*$" (name k)))]])

(def file-path-spec
  "Spec for file paths like 'cmd/daycamera.clj'"
  [:re #"^[a-z0-9_]+(/[a-z0-9_]+)*\.clj$"])

;; =============================================================================
;; Strategy for Lossless Conversion
;; =============================================================================
;; 
;; We use a special encoding to preserve proto structure in Clojure:
;; 1. Proto packages become namespaced keywords with special prefix
;; 2. Original casing is preserved in the keyword name part
;; 3. For filesystem compatibility, we still need lossy conversions
;;
;; Examples:
;; .cmd.DayCamera.Root -> :potatoclient.proto/cmd.DayCamera.Root
;; This preserves the exact proto structure and can roundtrip perfectly

;; =============================================================================
;; Collision Detection and Memoization
;; =============================================================================

(defonce conversion-cache
  (atom {}))

(defonce reverse-cache
  (atom {}))

(defn- check-and-cache!
  "Memoize conversion and check for collisions.
  Throws if output already exists with different input."
  [conversion-name input output]
  (let [forward-path [conversion-name input]
        reverse-path [conversion-name output]]
    ;; Check if we've seen this input before
    (if-let [cached (get-in @conversion-cache forward-path)]
      ;; Return cached value
      cached
      ;; New conversion - check for collisions
      (let [existing-input (get-in @reverse-cache reverse-path)]
        (when (and existing-input (not= existing-input input))
          (throw (ex-info "Naming collision detected!"
                          {:conversion conversion-name
                           :input input
                           :output output
                           :existing-input existing-input
                           :message (str "Output '" output "' already mapped to '" 
                                        existing-input "', cannot map to '" input "'")})))
        ;; Safe to cache
        (swap! conversion-cache assoc-in forward-path output)
        (swap! reverse-cache assoc-in reverse-path input)
        output))))

(defn clear-conversion-cache!
  "Clear all conversion caches. Useful for testing."
  []
  (reset! conversion-cache {})
  (reset! reverse-cache {}))

;; =============================================================================
;; Core Conversions (Lossless)
;; =============================================================================

(def ^:const proto-ns-prefix "potatoclient.proto")

(defn proto-type->keyword
  "Convert protobuf type to keyword preserving all information.
  e.g. '.cmd.DayCamera.Root' -> :potatoclient.proto/cmd.DayCamera.Root
       'cmd.Root' -> :potatoclient.proto/cmd.Root
  Always normalizes by removing leading dot."
  {:malli/schema [:=> [:cat proto-type-ref-spec] proto-encoded-keyword-spec]}
  [type-ref]
  ;; Remove leading dot if present for cleaner keywords
  (let [clean-ref (if (str/starts-with? type-ref ".")
                    (subs type-ref 1)
                    type-ref)
        result (keyword proto-ns-prefix clean-ref)]
    (check-and-cache! ::proto-type->keyword type-ref result)))

(defn keyword->proto-type
  "Convert keyword back to protobuf type reference.
  e.g. :potatoclient.proto/cmd.DayCamera.Root -> '.cmd.DayCamera.Root'
  Always returns with leading dot for consistency."
  {:malli/schema [:=> [:cat proto-encoded-keyword-spec] proto-type-ref-spec]}
  [kw]
  (when (= (namespace kw) proto-ns-prefix)
    (let [result (str "." (name kw))]
      (check-and-cache! ::keyword->proto-type kw result))))

(defn proto-package->keyword
  "Convert protobuf package to keyword preserving all information.
  e.g. 'cmd.DayCamera' -> :potatoclient.proto/cmd.DayCamera"
  {:malli/schema [:=> [:cat proto-package-spec] proto-encoded-keyword-spec]}
  [proto-package]
  (let [result (keyword proto-ns-prefix proto-package)]
    (check-and-cache! ::proto-package->keyword proto-package result)))

(defn keyword->proto-package
  "Convert keyword back to protobuf package.
  e.g. :potatoclient.proto/cmd.DayCamera -> 'cmd.DayCamera'"
  {:malli/schema [:=> [:cat proto-encoded-keyword-spec] proto-package-spec]}
  [kw]
  (when (= (namespace kw) proto-ns-prefix)
    (name kw)))

;; =============================================================================
;; Filesystem Conversions (Lossy but necessary)
;; =============================================================================

(defn normalize-for-filesystem
  "Convert a proto name part to filesystem-safe form.
  DayCamera -> daycamera
  Lrf_calib -> lrf_calib (preserves underscores)
  
  Note: This is case-insensitive, so 'Lira' and 'lira' both -> 'lira'.
  The collision detection will catch any actual collisions."
  [part]
  (str/lower-case part))

(defn proto-package->clj-namespace
  "Convert protobuf package to Clojure namespace for filesystem.
  This is lossy but needed for file paths.
  e.g. 'cmd.DayCamera' -> 'potatoclient.proto.cmd.daycamera'"
  {:malli/schema [:=> [:cat proto-package-spec] clojure-namespace-spec]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")
        normalized (map normalize-for-filesystem parts)
        result (str proto-ns-prefix "." (str/join "." normalized))]
    (check-and-cache! ::proto-package->clj-namespace proto-package result)))

(defn proto-package->file-path
  "Convert protobuf package to file path.
  e.g. 'cmd.DayCamera' -> 'cmd/daycamera.clj'"
  {:malli/schema [:=> [:cat proto-package-spec] file-path-spec]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")
        normalized (map normalize-for-filesystem parts)
        result (str (str/join "/" normalized) ".clj")]
    (check-and-cache! ::proto-package->file-path proto-package result)))

(defn proto-package->alias
  "Extract alias from protobuf package.
  e.g. 'cmd.DayCamera' -> 'daycamera'"
  {:malli/schema [:=> [:cat proto-package-spec] :string]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")
        result (normalize-for-filesystem (last parts))]
    (check-and-cache! ::proto-package->alias proto-package result)))

(defn proto-package->clojure-alias
  "Extract Clojure-compatible alias from protobuf package, preserving kebab-case.
  e.g. 'cmd.DayCamera' -> 'day-camera'
       'cmd.DayCamGlassHeater' -> 'day-cam-glass-heater'
  This is used for namespace aliases in require statements."
  {:malli/schema [:=> [:cat proto-package-spec] :string]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")
        ;; Use kebab-case conversion to preserve word boundaries
        result (conv/->kebab-case (last parts))]
    (check-and-cache! ::proto-package->clojure-alias proto-package result)))

;; =============================================================================
;; Name Conversions (for idiomatic Clojure)
;; =============================================================================

(defn proto-name->clojure-fn-name
  "Convert protobuf message/enum name to Clojure function name.
  Used for build-*/parse-* functions.
  e.g. 'SetDDELevel' -> 'set-dde-level'"
  {:malli/schema [:=> [:cat :string] :string]}
  [proto-name]
  (let [base-result (conv/->kebab-case proto-name)
        ;; Handle edge cases where result might be empty, start with hyphen, or start with number
        intermediate (cond
                       (empty? base-result) "underscore"  ; "_" -> "underscore"
                       (str/starts-with? base-result "-") (str "underscore" base-result) ; "_Foo" -> "underscore-foo"
                       :else base-result)
        result (if (re-matches #"^[0-9].*" intermediate) 
                 (str "n" intermediate) ; "0" -> "n0", "9abc" -> "n9abc"
                 intermediate)]
    (check-and-cache! ::proto-name->clojure-fn-name proto-name result)))

(defn proto-field->clojure-key
  "Convert protobuf field name to Clojure keyword.
  e.g. 'protocol_version' -> :protocol-version
       'clientType' -> :client-type"
  {:malli/schema [:=> [:cat :string] keyword?]}
  [field-name]
  (let [result (keyword (conv/->kebab-case field-name))]
    (check-and-cache! ::proto-field->clojure-key field-name result)))

;; =============================================================================
;; Spec References (for Malli)
;; =============================================================================

(defn proto-type->spec-keyword
  "Convert proto type to Malli spec keyword.
  Uses filesystem-safe names for compatibility.
  e.g. '.cmd.DayCamera.Root' -> :cmd.daycamera/root"
  {:malli/schema [:=> [:cat proto-type-ref-spec] keyword?]}
  [type-ref]
  (let [parts (str/split type-ref #"\.")
        ;; Remove empty first part from leading dot
        parts (if (empty? (first parts)) (rest parts) parts)
        result (if (= 1 (count parts))
                 ;; Single part
                 (keyword (normalize-for-filesystem (last parts)))
                 ;; Multiple parts - namespace/name
                 (let [ns-parts (butlast parts)
                       name-part (last parts)
                       ;; Convert namespace parts
                       ns-str (->> ns-parts
                                   (map normalize-for-filesystem)
                                   (str/join "."))]
                   (keyword ns-str (conv/->kebab-case name-part))))]
    (check-and-cache! ::proto-type->spec-keyword type-ref result)))

;; =============================================================================
;; Validation Functions
;; =============================================================================

(defn valid-proto-package? [s]
  (m/validate proto-package-spec s))

(defn valid-proto-type-ref? [s]
  (m/validate proto-type-ref-spec s))

(defn valid-clojure-namespace? [s]
  (m/validate clojure-namespace-spec s))

(defn valid-proto-encoded-keyword? [k]
  (m/validate proto-encoded-keyword-spec k))

(defn valid-file-path? [s]
  (m/validate file-path-spec s))

;; =============================================================================
;; Property Testing Helpers
;; =============================================================================

(defn gen-proto-identifier
  "Generate a valid protobuf identifier"
  []
  (mg/generator proto-identifier-spec))

(defn gen-proto-package
  "Generate a valid protobuf package"
  []
  (mg/generator proto-package-spec))

(defn gen-proto-type-ref
  "Generate a valid protobuf type reference"
  []
  (mg/generator proto-type-ref-spec))