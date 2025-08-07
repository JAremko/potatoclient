(ns generator.spec-gen
  "Generate Malli specs from protobuf EDN representation.
  Based on proto-explorer's approach but integrated into our generation pipeline."
  (:require [clojure.string :as str]
            [potatoclient.proto.conversion :as conv]
            [generator.constraints.compiler :as compiler]))

;; =============================================================================
;; Field Processing
;; =============================================================================

(defn type-name->keyword
  "Convert protobuf type name to keyword reference.
  e.g. '.cmd.Compass.Root' -> :cmd.compass/root"
  [type-name]
  (when type-name
    (let [parts (str/split type-name #"\.")
          ;; Remove empty first part from leading dot
          parts (if (empty? (first parts)) (rest parts) parts)]
      (if (= 1 (count parts))
        ;; Single part - just kebab-case it
        (keyword (conv/->kebab-case (last parts)))
        ;; Multiple parts - namespace/name
        (let [ns-parts (butlast parts)
              name-part (last parts)
              ;; Convert each part to kebab-case
              ns-str (str/join "." (map conv/->kebab-case ns-parts))]
          (keyword ns-str (conv/->kebab-case name-part)))))))

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
  (let [message-type (get-in field [:type :message])
        type-ref (:type-ref message-type)
        type-keyword (type-name->keyword type-ref)]
    ;; Check if enriched IR has cross-namespace info
    (if (:cross-namespace message-type)
      ;; For cross-namespace messages, we typically use :any since we don't
      ;; generate specs for messages from other namespaces
      :any
      ;; Same namespace - use keyword reference
      type-keyword)))

;; Enum references
(defmethod process-field-type :enum
  [field context]
  (let [enum-type (get-in field [:type :enum])
        type-ref (:type-ref enum-type)
        type-keyword (type-name->keyword type-ref)]
    ;; Check if enriched IR has cross-namespace info
    (if (:cross-namespace enum-type)
      ;; Use enriched metadata for cross-namespace handling
      (let [target-package (:target-package enum-type)
            ns-aliases (:ns-aliases context)
            ;; Look up alias for target package
            alias-name (get ns-aliases target-package)]
        (if alias-name
          ;; Cross-namespace reference with alias - use alias
          (let [enum-name (name type-keyword)
                spec-var-name (str (conv/->kebab-case enum-name) "-spec")]
            ;; Return a symbol that references the spec through the alias
            (symbol (str alias-name "/" spec-var-name)))
          ;; Cross-namespace but no alias found - fall back to keyword
          type-keyword))
      ;; Same namespace - use keyword as before
      type-keyword)))

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
    :label-optional schema  ; In proto3, all fields have defaults and are never nil
    schema))

(defn apply-constraints
  "Apply buf.validate constraints to schema"
  [schema field]
  (if (:constraints field)
    (compiler/apply-constraints schema field)
    schema))

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
        oneof-name (:name oneof-decl)
        ;; Use custom :oneof spec for proper protobuf oneof validation
        field-specs (into {}
                         (map (fn [field]
                                (let [field-key (keyword (:name field))]
                                  [field-key [:map [field-key (process-field-schema field context)]]]))
                              oneof-fields))
        ;; Check if this oneof is required based on constraints
        required? (get-in oneof-decl [:constraints :required])]
    ;; Return the key, optional metadata (if not required), and schema
    (if required?
      [(keyword oneof-name)
       ;; Required oneof
       [:oneof (merge field-specs
                     {:error/message "This oneof field is required"})]]
      ;; Optional oneof (default in proto3)
      [(keyword oneof-name)
       {:optional true}
       [:oneof field-specs]])))

;; =============================================================================
;; Message Processing
;; =============================================================================

(defn process-message-fields
  "Process all fields in a message"
  [message context]
  (let [regular-fields (remove :oneof-index (:fields message))
        oneofs (:oneofs message)]
    (concat
     ;; Regular fields - in proto3 all fields are optional
     (map (fn [field]
            [(keyword (:name field))
             {:optional true}
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
                     (keyword (conv/->kebab-case (or (:proto-name v) (:name v)))))
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
      conv/->kebab-case
      (str "-spec")
      symbol))

(defn enum->spec-name
  "Generate spec def name for an enum"
  [enum-type]
  (-> (or (:proto-name enum-type) (:name enum-type))
      conv/->kebab-case
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
  [{:keys [messages enums current-package require-specs] :as namespace-data}]
  (let [;; Build a map of package names to namespace aliases
        ;; If we have enriched data with package mappings, use that
        ;; Otherwise fall back to namespace suffix mapping
        ns-aliases (if-let [package-mappings (:package-mappings namespace-data)]
                     ;; Use enriched package mappings
                     ;; e.g. {"other.package" "other-alias"}
                     package-mappings
                     ;; Fall back to old behavior for non-enriched IR
                     ;; e.g. [[test.roundtrip.ser :as types]] -> {"ser" "types"}
                     (into {}
                           (for [[ns-sym :as alias-sym] require-specs
                                 :let [ns-str (str ns-sym)
                                       ;; Extract the last part of namespace (e.g. "ser" from "test.roundtrip.ser")
                                       ns-suffix (last (str/split ns-str #"\."))]]
                             [ns-suffix (str alias-sym)])))
        context {:current-package current-package
                 :ns-aliases ns-aliases}  ; Pass aliases to context
        
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