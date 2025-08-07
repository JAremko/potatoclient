(ns potatoclient.proto.constants
  "Centralized module for lossless conversion between protobuf constants and keywords.
  
  Examples:
    TYPE_INT32 <-> :type-int32
    LABEL_OPTIONAL <-> :label-optional
    TYPE_ENUM <-> :type-enum
  
  This module ensures that conversions are bijective (lossless) - every constant
  maps to exactly one keyword and vice versa."
  (:require [clojure.string :as str]))

;; =============================================================================
;; Conversion Functions
;; =============================================================================

(defn proto-const->keyword
  "Convert a protobuf constant string to a keyword.
  TYPE_INT32 -> :type-int32
  LABEL_OPTIONAL -> :label-optional
  
  This conversion is lossless - we can recover the original constant
  from the keyword."
  [s]
  (when (string? s)
    (-> s
        (str/lower-case)
        (str/replace "_" "-")
        (keyword))))

(defn keyword->proto-const
  "Convert a keyword back to a protobuf constant string.
  :type-int32 -> TYPE_INT32
  :label-optional -> LABEL_OPTIONAL
  
  This is the inverse of proto-const->keyword."
  [k]
  (when (keyword? k)
    (-> k
        (name)
        (str/upper-case)
        (str/replace "-" "_"))))

;; =============================================================================
;; Validation Functions
;; =============================================================================

(defn valid-proto-const?
  "Check if a string is a valid protobuf constant.
  Must be all uppercase with underscores."
  [s]
  (boolean
    (and (string? s)
         (re-matches #"[A-Z][A-Z0-9_]*" s))))

(defn valid-proto-keyword?
  "Check if a keyword could have come from a protobuf constant.
  Must be all lowercase with hyphens."
  [k]
  (boolean
    (and (keyword? k)
         (re-matches #"[a-z][a-z0-9-]*" (name k)))))

;; =============================================================================
;; Batch Conversion
;; =============================================================================

(defn convert-value
  "Convert a value if it's a protobuf constant, otherwise return as-is.
  This is useful for processing JSON data where only some values are constants."
  [v]
  (cond
    (valid-proto-const? v) (proto-const->keyword v)
    (keyword? v) v
    :else v))