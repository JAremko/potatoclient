(ns generator.specs-with-generators-v2
  "Malli specifications with integrated generators for all data structures.
  Version 2: More realistic patterns while maintaining diversity."
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.registry :as mr]
            [malli.util :as mu]
            [clojure.test.check.generators :as gen]
            [lambdaisland.regal :as regal]
            [lambdaisland.regal.generator :as regal-gen]))

;; =============================================================================
;; Realistic Name Components
;; =============================================================================

;; Common package components
(def package-components
  ["com" "org" "io" "net" "api" "service" "core" "proto" 
   "grpc" "rpc" "model" "domain" "data" "types" "common"
   "client" "server" "internal" "external" "v1" "v2"])

;; Common type name components
(def type-components
  ["User" "Account" "Order" "Product" "Service" "Request" "Response"
   "Status" "Config" "Settings" "Message" "Event" "Command" "Query"
   "Result" "Error" "Info" "Data" "Meta" "Context" "Session"
   "Auth" "Token" "Key" "Value" "Item" "List" "Map" "Set"])

;; Common field name components
(def field-components
  ["id" "name" "email" "user" "account" "created" "updated" "deleted"
   "status" "type" "value" "data" "meta" "config" "settings" "enabled"
   "disabled" "active" "inactive" "timestamp" "date" "time" "duration"
   "count" "total" "size" "length" "index" "offset" "limit" "page"])

;; Common enum value components
(def enum-components
  ["UNKNOWN" "SUCCESS" "FAILURE" "PENDING" "ACTIVE" "INACTIVE"
   "ENABLED" "DISABLED" "CREATED" "UPDATED" "DELETED" "ERROR"
   "WARNING" "INFO" "DEBUG" "TRACE" "OK" "CANCELLED" "INVALID"])

;; =============================================================================
;; Refined Generators
;; =============================================================================

(defn realistic-package-gen
  "Generate realistic package names"
  []
  (gen/fmap (fn [parts]
              (clojure.string/join "." parts))
            (gen/vector (gen/elements package-components) 2 4)))

(defn realistic-filename-gen
  "Generate realistic proto filenames"
  []
  (gen/fmap (fn [name]
              (str (clojure.string/lower-case name) ".proto"))
            (gen/elements ["user" "account" "order" "product" "service"
                           "auth" "common" "types" "api" "rpc" "config"
                           "events" "messages" "errors" "status"])))

(defn realistic-type-gen
  "Generate realistic type names, optionally with suffix"
  [& [suffix]]
  (if suffix
    (gen/fmap (fn [base]
                (str base suffix))
              (gen/elements type-components))
    (gen/elements type-components)))

(defn realistic-field-gen
  "Generate realistic field names"
  []
  (gen/one-of 
   [(gen/elements field-components)
    ;; Compound names like user_id, created_at
    (gen/fmap (fn [[base suffix]]
                (str base "_" suffix))
              (gen/tuple (gen/elements ["user" "account" "order" "product"
                                        "created" "updated" "deleted"])
                         (gen/elements ["id" "name" "at" "by" "date" "time"
                                        "count" "status" "type"])))]))

(defn realistic-enum-value-gen
  "Generate realistic enum values"
  []
  (gen/one-of
   [(gen/elements enum-components)
    ;; Prefixed values like STATUS_OK, ERROR_INVALID
    (gen/fmap (fn [[prefix suffix]]
                (str prefix "_" suffix))
              (gen/tuple (gen/elements ["STATUS" "ERROR" "TYPE" "MODE" "STATE"])
                         (gen/elements ["OK" "INVALID" "UNKNOWN" "ACTIVE" "INACTIVE"])))]))

;; =============================================================================
;; Basic Types with Realistic Generators
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
;; Type References with Realistic Generators
;; =============================================================================

(def TypeReference
  "A protobuf type reference (e.g., '.com.example.Message')"
  [:and
   {:gen/gen (gen/fmap (fn [[pkg type-name]]
                         (str "." pkg "." type-name))
                       (gen/tuple 
                        (realistic-package-gen)
                        (realistic-type-gen)))}
   :string
   [:fn {:error/message "must start with dot"} 
    #(clojure.string/starts-with? % ".")]])

(def ScalarType
  "Scalar type representation"
  [:map
   {:gen/gen (gen/fmap (fn [s] {:scalar s})
                       (gen/frequency 
                        [[5 (gen/return :string)]
                         [4 (gen/return :int32)]
                         [3 (gen/return :int64)]
                         [3 (gen/return :bool)]
                         [2 (gen/return :double)]
                         [2 (gen/return :bytes)]
                         [1 (gen/elements [:float :uint32 :uint64])]]))}
   [:scalar proto-scalar-types]])

(def MessageType
  "Message type representation"
  [:map
   {:gen/gen (gen/fmap (fn [ref] {:message {:type-ref ref}})
                       (:gen/gen (m/properties TypeReference)))}
   [:message [:map
              [:type-ref TypeReference]]]])

(def EnumType
  "Enum type representation"
  [:map
   {:gen/gen (gen/fmap (fn [ref] {:enum {:type-ref ref}})
                       (:gen/gen (m/properties TypeReference)))}
   [:enum [:map
           [:type-ref TypeReference]]]])

(def UnknownType
  "Unknown type (fallback)"
  [:map
   {:gen/gen (gen/fmap (fn [t] {:unknown {:proto-type t}})
                       (gen/elements [:any :unknown :custom]))}
   [:unknown [:map
              [:proto-type :keyword]]]])

(def FieldType
  "Any field type"
  [:or 
   {:gen/gen (gen/frequency [[7 (:gen/gen (m/properties ScalarType))]
                             [2 (:gen/gen (m/properties MessageType))]
                             [1 (:gen/gen (m/properties EnumType))]])}
   ScalarType MessageType EnumType UnknownType])

;; =============================================================================
;; Basic IR Structures with Realistic Generators
;; =============================================================================

(def EnumValue
  "Enum value definition"
  [:map
   {:gen/gen (gen/fmap (fn [[proto-name num]]
                         {:name (keyword (clojure.string/lower-case 
                                          (clojure.string/replace proto-name #"_" "-")))
                          :proto-name proto-name
                          :number num})
                       (gen/tuple
                        (realistic-enum-value-gen)
                        (gen/choose 0 100)))}
   [:name :keyword]
   [:proto-name :string]
   [:number :int]])

(def EnumDef
  "Enum definition"
  [:map
   {:gen/gen (gen/fmap 
              (fn [[proto-name pkg values]]
                (let [unique-values (vec (distinct values))]
                  {:type :enum
                   :name (keyword (clojure.string/lower-case proto-name))
                   :proto-name proto-name
                   :package pkg
                   :values (vec (map-indexed 
                                 (fn [i v] (assoc v :number i))
                                 unique-values))}))
              (gen/tuple
               (gen/one-of [(realistic-type-gen)
                            (realistic-type-gen "Status")
                            (realistic-type-gen "Type")
                            (realistic-type-gen "Mode")])
               (realistic-package-gen)
               (gen/vector (:gen/gen (m/properties EnumValue)) 2 10)))}
   [:type [:= :enum]]
   [:name :keyword]
   [:proto-name :string]
   [:package :string]
   [:java-class {:optional true} :string]
   [:values [:vector EnumValue]]])

(def FieldConstraints
  "Field validation constraints"
  [:map
   {:gen/gen (gen/return {})}
   [:required {:optional true} :boolean]
   [:string {:optional true} [:map-of :keyword :any]]
   [:float {:optional true} [:map-of :keyword :any]]
   [:int32 {:optional true} [:map-of :keyword :any]]
   [:repeated {:optional true} [:map-of :keyword :any]]])

(def Field
  "Message field definition"
  [:map
   {:gen/gen (gen/fmap
              (fn [[proto-name num label type]]
                {:name (keyword (clojure.string/replace proto-name #"_" "-"))
                 :proto-name proto-name
                 :number num
                 :label label
                 :type type})
              (gen/tuple
               (realistic-field-gen)
               (gen/choose 1 1000)
               (gen/frequency [[7 (gen/return :label-optional)]
                               [2 (gen/return :label-required)]
                               [1 (gen/return :label-repeated)]])
               (:gen/gen (m/properties FieldType))))}
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
   {:gen/gen (gen/fmap
              (fn [[proto-name idx fields]]
                {:name (keyword (clojure.string/replace proto-name #"_" "-"))
                 :proto-name proto-name
                 :index idx
                 :fields fields})
              (gen/tuple
               (gen/fmap (fn [base] (str base "_oneof"))
                         (gen/elements ["result" "response" "data" "value"]))
               (gen/choose 0 10)
               (gen/vector (:gen/gen (m/properties Field)) 2 4)))}
   [:name :keyword]
   [:proto-name :string]
   [:index :int]
   [:fields [:vector Field]]
   [:constraints {:optional true} [:map [:required {:optional true} :boolean]]]])

(def MessageDef
  "Message definition"
  [:map
   {:gen/gen (gen/fmap
              (fn [[proto-name pkg fields]]
                (let [unique-fields (reduce (fn [acc field]
                                              (if (some #(= (:proto-name %) (:proto-name field)) acc)
                                                acc
                                                (conj acc field)))
                                            []
                                            fields)]
                  {:type :message
                   :name (keyword (clojure.string/lower-case proto-name))
                   :proto-name proto-name
                   :package pkg
                   :fields (vec (map-indexed 
                                 (fn [i f] (assoc f :number (inc i)))
                                 unique-fields))}))
              (gen/tuple
               (gen/one-of [(realistic-type-gen)
                            (realistic-type-gen "Request")
                            (realistic-type-gen "Response")
                            (realistic-type-gen "Config")])
               (realistic-package-gen)
               (gen/vector (:gen/gen (m/properties Field)) 1 10)))}
   [:type [:= :message]]
   [:name :keyword]
   [:proto-name :string]
   [:package :string]
   [:java-class {:optional true} :string]
   [:fields [:vector Field]]
   [:oneofs {:optional true} [:vector OneofDef]]
   [:nested-types {:optional true} [:vector :any]]])

(def FileDef
  "Proto file definition"
  [:map
   {:gen/gen (gen/let [filename (realistic-filename-gen)
                       package (realistic-package-gen)
                       num-deps (gen/choose 0 3)
                       deps (gen/vector (realistic-filename-gen) num-deps)
                       enums (gen/vector (:gen/gen (m/properties EnumDef)) 0 3)
                       messages (gen/vector (:gen/gen (m/properties MessageDef)) 1 5)]
               (let [;; Ensure consistent package
                     enums' (mapv #(assoc % :package package) enums)
                     messages' (mapv #(assoc % :package package) messages)
                     ;; Remove self-dependencies and duplicates
                     deps' (vec (distinct (remove #(= % filename) deps)))]
                 {:type :file
                  :name filename
                  :package package
                  :dependencies deps'
                  :messages messages'
                  :enums enums'}))}
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
   {:gen/gen (gen/fmap
              (fn [files]
                ;; Ensure unique filenames
                (let [unique-files (reduce (fn [acc file]
                                             (if (some #(= (:name %) (:name file)) acc)
                                               acc
                                               (conj acc file)))
                                           []
                                           files)
                      ;; Get all filenames
                      filenames (set (map :name unique-files))
                      ;; Filter dependencies to only existing files
                      filtered-files (mapv (fn [file]
                                             (update file :dependencies
                                                     (fn [deps]
                                                       (vec (filter filenames deps)))))
                                           unique-files)]
                  {:type :descriptor-set
                   :files filtered-files}))
              (gen/vector (:gen/gen (m/properties FileDef)) 1 5))}
   [:type [:= :descriptor-set]]
   [:files [:vector FileDef]]])

;; =============================================================================
;; Dependency Resolution Structures (no generators needed for enriched types)
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
;; Enriched IR Structures (no generators - these are output only)
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
   [:nested-types {:optional true} [:vector :any]]])

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
;; Registry Setup
;; =============================================================================

(def all-specs
  {;; Input types with generators
   ::TypeReference TypeReference
   ::ScalarType ScalarType
   ::MessageType MessageType
   ::EnumType EnumType
   ::UnknownType UnknownType
   ::FieldType FieldType
   ::EnumValue EnumValue
   ::EnumDef EnumDef
   ::FieldConstraints FieldConstraints
   ::Field Field
   ::OneofDef OneofDef
   ::MessageDef MessageDef
   ::FileDef FileDef
   ::DescriptorSet DescriptorSet
   
   ;; Output types (no generators)
   ::DependencyNode DependencyNode
   ::DependencyGraph DependencyGraph
   ::SymbolDef SymbolDef
   ::SymbolRegistry SymbolRegistry
   ::EnrichedTypeRef EnrichedTypeRef
   ::EnrichedMessageType EnrichedMessageType
   ::EnrichedEnumType EnrichedEnumType
   ::EnrichedFieldType EnrichedFieldType
   ::EnrichedField EnrichedField
   ::EnrichedMessage EnrichedMessage
   ::EnrichedFile EnrichedFile
   ::EnrichedDescriptorSet EnrichedDescriptorSet})

(def registry
  "Custom registry with all specs"
  (mr/composite-registry
   m/default-registry
   all-specs))

;; =============================================================================
;; Validation Functions
;; =============================================================================

(defn validate
  "Validate data against a spec, return nil if valid or error explanation"
  [spec data]
  (if (m/validate spec data {:registry registry})
    nil
    (me/humanize (m/explain spec data {:registry registry}))))

(defn valid?
  "Check if data is valid according to spec"
  [spec data]
  (m/validate spec data {:registry registry}))