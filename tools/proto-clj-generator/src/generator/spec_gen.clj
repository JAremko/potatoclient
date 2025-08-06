(ns generator.spec-gen
  "Generate Malli specs from protobuf EDN representation.
  Based on proto-explorer's approach but integrated into our generation pipeline."
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [generator.naming :as naming]))

;; =============================================================================
;; Field Processing
;; =============================================================================

(defn type-name->keyword
  "Convert protobuf type name to keyword reference.
  e.g. '.cmd.Compass.Root' -> :cmd.compass/root
  Uses centralized naming module for consistency."
  [type-name]
  (when type-name
    (naming/proto-type->spec-keyword type-name)))

(defmulti process-field-type
  "Process a field's type into a Malli schema"
  (fn [field _context] 
    ;; Handle nested type structure from backend
    (let [type (:type field)]
      (cond
        (:scalar type) :scalar
        (:enum type) :enum
        (:message type) :message
        (:unknown type) :unknown
        :else type))))

;; Scalar types
(defmethod process-field-type :scalar
  [field context]
  (let [scalar-type (get-in field [:type :scalar])]
    (case scalar-type
      :double   :double
      :float    :float
      :int32    :int
      :uint32   :int
      :int64    :int
      :uint64   :int
      :sint32   :int
      :sint64   :int
      :fixed32  :int
      :fixed64  :int
      :sfixed32 :int
      :sfixed64 :int
      :bool     :boolean
      :string   :string
      :bytes    :bytes
      :any)))

;; Message type references
(defmethod process-field-type :message
  [field context]
  (type-name->keyword (get-in field [:type :message :type-ref])))

;; Enum references
(defmethod process-field-type :enum
  [field context]
  (type-name->keyword (get-in field [:type :enum :type-ref])))

;; Unknown types (from backend when it can't resolve)
(defmethod process-field-type :unknown
  [field context]
  (let [proto-type (get-in field [:type :unknown :proto-type])]
    ;; Try to map the proto type directly
    (case proto-type
      :type-int-32  :int
      :type-int-64  :int
      :type-uint-32 :int
      :type-uint-64 :int
      :type-sint-32 :int
      :type-sint-64 :int
      :type-fixed-32 :int
      :type-fixed-64 :int
      :type-sfixed-32 :int
      :type-sfixed-64 :int
      :type-bool :boolean
      :type-string :string
      :type-bytes :bytes
      :type-double :double
      :type-float :float
      ;; If we still can't resolve it
      (do
        (println "WARNING: Unknown proto type" proto-type "for field" (:name field))
        :any))))

;; Default fallback
(defmethod process-field-type :default
  [field context]
  (println "WARNING: Unhandled field type structure" (:type field) "for field" (:name field))
  :any)

(defn apply-field-label
  "Apply field label (repeated, optional) to schema"
  [schema field]
  (case (:label field)
    :label-repeated [:vector schema]
    :label-optional (if (:oneof-index field)
                      schema  ; Oneof fields handle optionality at oneof level
                      [:maybe schema])
    schema))

(defn apply-constraints
  "Apply buf.validate constraints to schema"
  [schema field]
  ;; TODO: Extract and apply constraints from field options
  ;; For now, just return the schema unchanged
  schema)

(defn process-field-schema
  "Process a single field into its schema"
  [field context]
  (-> field
      (process-field-type context)
      (apply-field-label field)
      (apply-constraints field)))

;; =============================================================================
;; Oneof Processing
;; =============================================================================

(defn process-oneof
  "Process a oneof declaration into a schema"
  [oneof-decl fields context]
  (let [oneof-index (:index oneof-decl)
        oneof-fields (:fields oneof-decl)
        oneof-name (:name oneof-decl)]
    ;; Return just the key and schema pair, not wrapped in [:map ...]
    [(keyword oneof-name)
     [:oneof
      (into {}
            (map (fn [field]
                   (let [field-key (keyword (:name field))]
                     [field-key [:map [field-key (process-field-schema field context)]]]))
                 oneof-fields))]]))

;; =============================================================================
;; Message Processing
;; =============================================================================

(defn process-message-fields
  "Process all fields in a message"
  [message context]
  (let [regular-fields (remove :oneof-index (:fields message))
        oneofs (:oneofs message)]
    (concat
     ;; Regular fields
     (map (fn [field]
            [(keyword (:name field))
             (process-field-schema field context)])
          regular-fields)
     ;; Oneof fields - each returns a [key schema] pair
     (map #(process-oneof % (:fields message) context) oneofs))))

(defn generate-message-spec
  "Generate a Malli spec for a message"
  [message context]
  (let [fields (process-message-fields message context)]
    (if (empty? fields)
      [:map]  ; Empty message
      (into [:map] fields))))

;; =============================================================================
;; Enum Processing
;; =============================================================================

(defn generate-enum-spec
  "Generate a Malli spec for an enum"
  [enum-type context]
  (let [values (map (fn [v]
                     (keyword (naming/proto-name->clojure-fn-name (or (:proto-name v) (:name v)))))
                   (:values enum-type))]
    (if (empty? values)
      [:enum]  ; Empty enum (shouldn't happen but be defensive)
      (into [:enum] values))))

;; =============================================================================
;; Spec Name Generation
;; =============================================================================

(defn message->spec-name
  "Generate spec def name for a message"
  [message]
  (-> (or (:proto-name message) (:name message))
      naming/proto-name->clojure-fn-name
      (str "-spec")
      symbol))

(defn enum->spec-name
  "Generate spec def name for an enum"
  [enum-type]
  (-> (or (:proto-name enum-type) (:name enum-type))
      naming/proto-name->clojure-fn-name
      (str "-spec")
      symbol))

;; =============================================================================
;; Code Generation
;; =============================================================================

(defn generate-spec-def
  "Generate a def form for a spec using our template"
  [spec-name description schema]
  (str "(def " spec-name "\n"
       "  \"Malli spec for " description "\"\n"
       "  " (pr-str schema) ")"))

; Removed - enum maps are already generated by frontend/generate-enum-def

;; =============================================================================
;; Public API
;; =============================================================================

(defn generate-specs-for-namespace
  "Generate all Malli specs for messages and enums in a namespace"
  [{:keys [messages enums] :as namespace-data}]
  (let [context {}  ; Can add type lookup, etc. here
        
        ;; Generate specs for enums
        enum-specs (map (fn [enum-type]
                         (let [spec-name (enum->spec-name enum-type)
                               schema (generate-enum-spec enum-type context)]
                           (generate-spec-def spec-name 
                                            (str (name (:name enum-type)) " enum")
                                            schema)))
                       enums)
        
        ;; Generate specs for messages
        message-specs (map (fn [message]
                            (let [spec-name (message->spec-name message)
                                  schema (generate-message-spec message context)]
                              (generate-spec-def spec-name
                                               (str (name (:name message)) " message")
                                               schema)))
                          messages)]
    
    {:enum-specs (str/join "\n\n" enum-specs)
     :message-specs (str/join "\n\n" message-specs)}))