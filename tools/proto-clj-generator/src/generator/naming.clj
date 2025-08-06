(ns generator.naming
  "Centralized naming conversions between protobuf and Clojure.
  
  This module ensures LOSSLESS bidirectional transformations:
  - Package names (e.g. cmd.DayCamera <-> :potatoclient.proto/cmd.DayCamera)
  - Type references (e.g. .cmd.DayCamera.Root <-> :potatoclient.proto/cmd.DayCamera.Root)
  - Preserves all original casing and structure
  
  The key insight is to encode the proto structure directly in Clojure keywords
  rather than trying to convert to idiomatic Clojure names."
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [malli.core :as m]
            [malli.generator :as mg]))

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def proto-package-spec
  "Spec for protobuf package names like 'cmd.DayCamera'"
  [:re #"^[a-z]+(\.[A-Za-z_][A-Za-z0-9_]*)*$"])

(def proto-type-ref-spec
  "Spec for protobuf type references like '.cmd.DayCamera.Root'"
  [:re #"^\.?[a-z]+(\.[A-Za-z_][A-Za-z0-9_]*)+$"])

(def proto-identifier-spec
  "Spec for a single protobuf identifier (PascalCase or snake_case)"
  [:re #"^[A-Za-z_][A-Za-z0-9_]*$"])

(def clojure-namespace-spec
  "Spec for Clojure namespace strings like 'cmd.daycamera'"
  [:re #"^[a-z][a-z0-9-]*(\.[a-z][a-z0-9-]*)*$"])

(def clojure-keyword-spec
  "Spec for Clojure namespaced keywords like :cmd.daycamera/root"
  [:and keyword? 
   [:fn (fn [k] (namespace k))]])

(def proto-encoded-keyword-spec
  "Spec for our special proto-encoded keywords like :potatoclient.proto/cmd.DayCamera.Root"
  [:and keyword?
   [:fn (fn [k] (= (namespace k) "potatoclient.proto"))]
   [:fn (fn [k] (re-matches #"^[a-z]+(\.[A-Za-z_][A-Za-z0-9_]*)+$" (name k)))]])

(def file-path-spec
  "Spec for file paths like 'cmd/daycamera.clj'"
  [:re #"^[a-z_]+(/[a-z_]+)*\.clj$"])

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
;; Core Conversions (Lossless)
;; =============================================================================

(def ^:const proto-ns-prefix "potatoclient.proto")

(defn proto-type->keyword
  "Convert protobuf type to keyword preserving all information.
  e.g. '.cmd.DayCamera.Root' -> :potatoclient.proto/cmd.DayCamera.Root
       'cmd.Root' -> :potatoclient.proto/cmd.Root"
  {:malli/schema [:=> [:cat proto-type-ref-spec] proto-encoded-keyword-spec]}
  [type-ref]
  (let [;; Remove leading dot if present
        clean-ref (if (str/starts-with? type-ref ".")
                    (subs type-ref 1)
                    type-ref)]
    (keyword proto-ns-prefix clean-ref)))

(defn keyword->proto-type
  "Convert keyword back to protobuf type reference.
  e.g. :potatoclient.proto/cmd.DayCamera.Root -> '.cmd.DayCamera.Root'"
  {:malli/schema [:=> [:cat proto-encoded-keyword-spec] proto-type-ref-spec]}
  [kw]
  (when (= (namespace kw) proto-ns-prefix)
    (str "." (name kw))))

(defn proto-package->keyword
  "Convert protobuf package to keyword preserving all information.
  e.g. 'cmd.DayCamera' -> :potatoclient.proto/cmd.DayCamera"
  {:malli/schema [:=> [:cat proto-package-spec] proto-encoded-keyword-spec]}
  [proto-package]
  (keyword proto-ns-prefix proto-package))

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
  Lrf_calib -> lrf_calib (preserves underscores)"
  [part]
  (str/lower-case part))

(defn proto-package->clj-namespace
  "Convert protobuf package to Clojure namespace for filesystem.
  This is lossy but needed for file paths.
  e.g. 'cmd.DayCamera' -> 'potatoclient.proto.cmd.daycamera'"
  {:malli/schema [:=> [:cat proto-package-spec] clojure-namespace-spec]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")
        normalized (map normalize-for-filesystem parts)]
    (str proto-ns-prefix "." (str/join "." normalized))))

(defn proto-package->file-path
  "Convert protobuf package to file path.
  e.g. 'cmd.DayCamera' -> 'cmd/daycamera.clj'"
  {:malli/schema [:=> [:cat proto-package-spec] file-path-spec]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")
        normalized (map normalize-for-filesystem parts)]
    (str (str/join "/" normalized) ".clj")))

(defn proto-package->alias
  "Extract alias from protobuf package.
  e.g. 'cmd.DayCamera' -> 'daycamera'"
  {:malli/schema [:=> [:cat proto-package-spec] :string]}
  [proto-package]
  (let [parts (str/split proto-package #"\.")]
    (normalize-for-filesystem (last parts))))

;; =============================================================================
;; Name Conversions (for idiomatic Clojure)
;; =============================================================================

(defn proto-name->clojure-fn-name
  "Convert protobuf message/enum name to Clojure function name.
  Used for build-*/parse-* functions.
  e.g. 'SetDDELevel' -> 'set-dde-level'"
  {:malli/schema [:=> [:cat :string] :string]}
  [proto-name]
  (csk/->kebab-case proto-name))

(defn proto-field->clojure-key
  "Convert protobuf field name to Clojure keyword.
  e.g. 'protocol_version' -> :protocol-version
       'clientType' -> :client-type"
  {:malli/schema [:=> [:cat :string] keyword?]}
  [field-name]
  (keyword (csk/->kebab-case field-name)))

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
        parts (if (empty? (first parts)) (rest parts) parts)]
    (if (= 1 (count parts))
      ;; Single part
      (keyword (normalize-for-filesystem (last parts)))
      ;; Multiple parts - namespace/name
      (let [ns-parts (butlast parts)
            name-part (last parts)
            ;; Convert namespace parts
            ns-str (->> ns-parts
                        (map normalize-for-filesystem)
                        (str/join "."))]
        (keyword ns-str (csk/->kebab-case name-part))))))

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