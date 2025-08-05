(ns generator.edn-specs
  "Malli specifications for the EDN intermediate representation."
  (:require [malli.core :as m]))

;; =============================================================================
;; Basic Types
;; =============================================================================

(def ScalarType
  [:enum :double :float :int64 :uint64 :int32 :fixed64 :fixed32
   :bool :string :bytes :uint32 :sfixed32 :sfixed64 :sint32 :sint64])

(def Label
  [:enum :optional :required :repeated])

;; =============================================================================
;; Type Specifications
;; =============================================================================

(def FieldType
  [:or
   ;; Scalar type
   [:map
    [:scalar ScalarType]]
   
   ;; Enum type
   [:map
    [:enum [:map
            [:type-ref :string]]]]
   
   ;; Message type
   [:map
    [:message [:map
               [:type-ref :string]]]]
   
   ;; Unknown type
   [:map
    [:unknown [:map
               [:proto-type :keyword]]]]])

;; =============================================================================
;; Field Specification
;; =============================================================================

(def Field
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:number :int]
   [:type FieldType]
   [:label {:optional true} Label]
   [:repeated? {:optional true} :boolean]
   [:optional? {:optional true} :boolean]
   [:oneof-index {:optional true} :int]
   [:default-value {:optional true} :any]])

;; =============================================================================
;; Oneof Specification
;; =============================================================================

(def Oneof
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:index :int]
   [:fields [:vector Field]]])

;; =============================================================================
;; Enum Value Specification
;; =============================================================================

(def EnumValue
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:number :int]])

;; =============================================================================
;; Type Definitions
;; =============================================================================

(def EnumDef
  [:map
   [:type [:= :enum]]
   [:name :keyword]
   [:proto-name :string]
   [:java-class :string]
   [:values [:vector EnumValue]]])

(def MessageDef
  [:map
   [:type [:= :message]]
   [:name :keyword]
   [:proto-name :string]
   [:java-class :string]
   [:fields [:vector Field]]
   [:oneofs [:vector Oneof]]
   [:nested-types {:optional true} [:vector :any]]])

;; =============================================================================
;; File Specification
;; =============================================================================

(def FileDef
  [:map
   [:type [:= :file]]
   [:name :string]
   [:package :string]
   [:java-package {:optional true} :string]
   [:java-outer-classname {:optional true} :string]
   [:syntax {:optional true} :string]
   [:dependencies [:vector :string]]
   [:messages [:vector MessageDef]]
   [:enums [:vector EnumDef]]
   [:services [:vector :any]]])

;; =============================================================================
;; Descriptor Set Specification
;; =============================================================================

(def DescriptorSet
  [:map
   [:type [:= :descriptor-set]]
   [:files [:vector FileDef]]])

;; =============================================================================
;; Type Lookup Specification
;; =============================================================================

(def TypeLookup
  [:map-of :keyword [:or MessageDef EnumDef]])

;; =============================================================================
;; Backend Output Specification
;; =============================================================================

(def BackendOutput
  [:map
   [:command DescriptorSet]
   [:state DescriptorSet]
   [:type-lookup TypeLookup]])

;; =============================================================================
;; Registry
;; =============================================================================

(def registry
  {::ScalarType ScalarType
   ::Label Label
   ::FieldType FieldType
   ::Field Field
   ::Oneof Oneof
   ::EnumValue EnumValue
   ::EnumDef EnumDef
   ::MessageDef MessageDef
   ::FileDef FileDef
   ::DescriptorSet DescriptorSet
   ::TypeLookup TypeLookup
   ::BackendOutput BackendOutput})

;; =============================================================================
;; Validation Functions
;; =============================================================================

(defn validate-field
  "Validate a field definition."
  [field]
  (m/validate Field field))

(defn validate-message
  "Validate a message definition."
  [message]
  (m/validate MessageDef message {:registry registry}))

(defn validate-file
  "Validate a file definition."
  [file]
  (m/validate FileDef file {:registry registry}))

(defn validate-descriptor-set
  "Validate a descriptor set."
  [descriptor-set]
  (m/validate DescriptorSet descriptor-set {:registry registry}))

(defn validate-backend-output
  "Validate the complete backend output."
  [output]
  (m/validate BackendOutput output {:registry registry}))

(defn explain-validation-error
  "Get a human-readable explanation of validation errors."
  [spec data]
  (when-not (m/validate spec data {:registry registry})
    (m/explain spec data {:registry registry})))