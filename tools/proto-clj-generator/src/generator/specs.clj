(ns generator.specs
  "Malli specifications for all data structures in the proto-clj-generator.
  Provides validation at each stage of the IR transformation pipeline."
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.registry :as mr]
            [malli.util :as mu]))

;; =============================================================================
;; Basic Types
;; =============================================================================

(def proto-scalar-types
  "Valid protobuf scalar types"
  [:enum :double :float :int32 :int64 :uint32 :uint64 
   :sint32 :sint64 :fixed32 :fixed64 :sfixed32 :sfixed64 
   :bool :string :bytes])

(def proto-labels
  "Valid protobuf field labels"
  [:enum :label-optional :label-required :label-repeated])

;; =============================================================================
;; Type References
;; =============================================================================

(def TypeReference
  "A protobuf type reference (e.g., '.com.example.Message')"
  [:and
   :string
   [:fn {:error/message "must start with dot"} 
    #(clojure.string/starts-with? % ".")]])

(def ScalarType
  "Scalar type representation"
  [:map
   [:scalar proto-scalar-types]])

(def MessageType
  "Message type representation"
  [:map
   [:message [:map
              [:type-ref TypeReference]]]])

(def EnumType
  "Enum type representation"
  [:map
   [:enum [:map
           [:type-ref TypeReference]]]])

(def UnknownType
  "Unknown type (fallback)"
  [:map
   [:unknown [:map
              [:proto-type :keyword]]]])

(def FieldType
  "Any field type"
  [:or ScalarType MessageType EnumType UnknownType])

;; =============================================================================
;; Basic IR Structures (from backend)
;; =============================================================================

(def EnumValue
  "Enum value definition"
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:number :int]])

(def EnumDef
  "Enum definition"
  [:map
   [:type [:= :enum]]
   [:name :keyword]
   [:proto-name :string]
   [:package :string]
   [:java-class {:optional true} :string]
   [:values [:vector EnumValue]]])

(def FieldConstraints
  "Field validation constraints"
  [:map
   [:required {:optional true} :boolean]
   [:string {:optional true} [:map-of :keyword :any]]
   [:float {:optional true} [:map-of :keyword :any]]
   [:int32 {:optional true} [:map-of :keyword :any]]
   [:repeated {:optional true} [:map-of :keyword :any]]])

(def Field
  "Message field definition"
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:number :int]
   [:label proto-labels]
   [:type FieldType]
   [:repeated? {:optional true} :boolean]
   [:oneof-index {:optional true} :int]
   [:constraints {:optional true} FieldConstraints]])

(def OneofDef
  "Oneof definition"
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:index :int]
   [:fields [:vector Field]]
   [:constraints {:optional true} [:map [:required {:optional true} :boolean]]]])

(def MessageDef
  "Message definition"
  [:map
   [:type [:= :message]]
   [:name :keyword]
   [:proto-name :string]
   [:package :string]
   [:java-class {:optional true} :string]
   [:fields [:vector Field]]
   [:oneofs {:optional true} [:vector OneofDef]]
   [:nested-types {:optional true} [:vector [:or [:ref ::MessageDef] [:ref ::EnumDef]]]]])

(def FileDef
  "Proto file definition"
  [:map
   [:type [:= :file]]
   [:name :string]
   [:package :string]
   [:java-package {:optional true} :string]
   [:java-outer-classname {:optional true} :string]
   [:dependencies [:vector :string]]
   [:messages [:vector MessageDef]]
   [:enums [:vector EnumDef]]])

(def DescriptorSet
  "Descriptor set containing multiple files"
  [:map
   [:type [:= :descriptor-set]]
   [:files [:vector FileDef]]])

;; =============================================================================
;; Dependency Resolution Structures
;; =============================================================================

(def DependencyNode
  "Node in dependency graph"
  [:map
   [:name :string]
   [:package :string]
   [:depends-on [:vector :string]]])


(def DependencyGraph
  "Full dependency graph"
  [:map
   [:nodes [:set :string]]
   [:edges [:map-of :string [:set :string]]]
   [:file->package [:map-of :string :string]]])

(def SymbolDef
  "Symbol registry entry"
  [:map
   [:fqn TypeReference]
   [:type [:enum :enum :message]]
   [:definition [:or EnumDef MessageDef]]])

(def SymbolRegistry
  "Global symbol registry"
  [:map-of TypeReference SymbolDef])

;; =============================================================================
;; Enriched IR Structures
;; =============================================================================

(def EnrichedTypeRef
  "Type reference enriched with resolution information"
  [:map
   [:type-ref TypeReference]
   [:resolved {:optional true} SymbolDef]
   [:cross-namespace {:optional true} :boolean]
   [:target-package {:optional true} :string]])

(def EnrichedMessageType
  "Message type with enriched reference"
  [:map
   [:message EnrichedTypeRef]])

(def EnrichedEnumType
  "Enum type with enriched reference"
  [:map
   [:enum EnrichedTypeRef]])

(def EnrichedFieldType
  "Any enriched field type"
  [:or ScalarType EnrichedMessageType EnrichedEnumType UnknownType])

(def EnrichedField
  "Field with enriched type information"
  [:map
   [:name :keyword]
   [:proto-name :string]
   [:number :int]
   [:label proto-labels]
   [:type EnrichedFieldType]
   [:repeated? {:optional true} :boolean]
   [:oneof-index {:optional true} :int]
   [:constraints {:optional true} FieldConstraints]])

(def EnrichedMessage
  "Message with enriched fields"
  [:map
   [:type [:= :message]]
   [:name :keyword]
   [:proto-name :string]
   [:package :string]
   [:java-class {:optional true} :string]
   [:fields [:vector EnrichedField]]
   [:oneofs {:optional true} [:vector OneofDef]]
   [:nested-types {:optional true} [:vector [:or [:ref ::EnrichedMessage] EnumDef]]]])

(def EnrichedFile
  "File with enriched messages"
  [:map
   [:type [:= :file]]
   [:name :string]
   [:package :string]
   [:java-package {:optional true} :string]
   [:java-outer-classname {:optional true} :string]
   [:dependencies [:vector :string]]
   [:messages [:vector EnrichedMessage]]
   [:enums [:vector EnumDef]]
   [:clj-requires {:optional true} [:vector :any]]])

(def EnrichedDescriptorSet
  "Fully enriched descriptor set"
  [:map
   [:type [:= :combined]]
   [:files [:vector EnrichedFile]]
   [:dependency-graph DependencyGraph]
   [:sorted-files [:vector :string]]
   [:symbol-registry SymbolRegistry]])

;; =============================================================================
;; Backend Output
;; =============================================================================

(def BackendOutput
  "Output from backend parsing"
  [:map
   [:command DescriptorSet]
   [:state DescriptorSet]
   [:type-lookup [:map-of :string [:or EnumDef MessageDef]]]
   [:dependency-graph {:optional true} DependencyGraph]
   [:sorted-files {:optional true} [:vector :string]]])

;; =============================================================================
;; Code Generation Structures
;; =============================================================================

(def GeneratedCode
  "Generated code output"
  [:or
   ;; Single file mode
   [:map
    [:command :string]
    [:state :string]]
   ;; Namespaced mode
   [:map-of :string :string]])

;; =============================================================================
;; Registry Setup
;; =============================================================================

;; Define all specs in a map for registration
(def all-specs
  {;; Basic types
   ::ScalarType ScalarType
   ::MessageType MessageType
   ::EnumType EnumType
   ::UnknownType UnknownType
   ::FieldType FieldType
   ::TypeReference TypeReference
   
   ;; Basic structures
   ::EnumValue EnumValue
   ::EnumDef EnumDef
   ::FieldConstraints FieldConstraints
   ::Field Field
   ::OneofDef OneofDef
   ::MessageDef MessageDef
   ::FileDef FileDef
   ::DescriptorSet DescriptorSet
   
   ;; Dependency structures
   ::DependencyNode DependencyNode
   ::DependencyGraph DependencyGraph
   ::SymbolDef SymbolDef
   ::SymbolRegistry SymbolRegistry
   
   ;; Enriched structures
   ::EnrichedTypeRef EnrichedTypeRef
   ::EnrichedMessageType EnrichedMessageType
   ::EnrichedEnumType EnrichedEnumType
   ::EnrichedFieldType EnrichedFieldType
   ::EnrichedField EnrichedField
   ::EnrichedMessage EnrichedMessage
   ::EnrichedFile EnrichedFile
   ::EnrichedDescriptorSet EnrichedDescriptorSet
   
   ;; Backend output
   ::BackendOutput BackendOutput
   ::GeneratedCode GeneratedCode})

(def registry
  "Custom registry for all specs"
  (mr/composite-registry
   m/default-registry
   all-specs))

;; Set as default registry
;; NOTE: Commented out to avoid conflicts with guardrails malli registry
;; (mr/set-default-registry! registry)

;; =============================================================================
;; Validation Functions
;; =============================================================================

(defn validate
  "Validate data against a spec, return nil if valid or error explanation"
  [spec data]
  (if (m/validate spec data {:registry registry})
    nil
    (me/humanize (m/explain spec data {:registry registry}))))

(defn validate!
  "Validate data against a spec, throw if invalid"
  [spec data context]
  (when-let [errors (validate spec data)]
    (throw (ex-info (str "Validation failed in " context)
                    {:errors errors
                     :spec spec
                     :data data}))))

(defn valid?
  "Check if data is valid according to spec"
  [spec data]
  (m/validate spec data {:registry registry}))

;; =============================================================================
;; Spec Instrumentation
;; =============================================================================

(defn instrument-fn
  "Instrument a function with input/output validation"
  [f input-spec output-spec fn-name]
  (fn [& args]
    (validate! input-spec args (str fn-name " input"))
    (let [result (apply f args)]
      (validate! output-spec result (str fn-name " output"))
      result)))