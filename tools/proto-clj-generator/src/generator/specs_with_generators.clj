(ns generator.specs-with-generators
  "Malli specifications with integrated generators for all data structures.
  This combines specs and generators in one place for better maintainability."
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.registry :as mr]
            [malli.util :as mu]
            [clojure.test.check.generators :as gen]
            [lambdaisland.regal :as regal]
            [lambdaisland.regal.generator :as regal-gen]))

;; =============================================================================
;; Generator Helpers
;; =============================================================================

(defn enum-gen
  "Create a generator from a fixed set of values"
  [values]
  (gen/elements values))

(defn regex-gen
  "Create a generator from a Regal pattern"
  [pattern]
  (regal-gen/gen pattern))

;; =============================================================================
;; Basic Patterns with Regal
;; =============================================================================

;; Package name pattern: com.example.service
(def package-pattern
  [:cat
   [:class [\a \z]]
   [:* [:class [\a \z \0 \9 \_]]]
   [:* [:cat "." 
        [:class [\a \z]]
        [:* [:class [\a \z \0 \9 \_]]]]]])

;; Proto filename pattern: service.proto
(def proto-filename-pattern
  [:cat
   [:+ [:class [\a \z \_]]]
   ".proto"])

;; PascalCase pattern for types: UserRequest
(def pascal-case-pattern
  [:cat
   [:class [\A \Z]]
   [:* [:class [\a \z \A \Z \0 \9]]]])

;; snake_case pattern for fields: user_id
(def snake-case-pattern
  [:cat
   [:class [\a \z]]
   [:* [:alt
        [:class [\a \z \0 \9]]
        [:cat "_" [:class [\a \z \0 \9]]]]]])

;; UPPER_SNAKE pattern for enum values: USER_ACTIVE
(def upper-snake-pattern
  [:cat
   [:class [\A \Z]]
   [:* [:alt
        [:class [\A \Z \0 \9]]
        [:cat "_" [:class [\A \Z \0 \9]]]]]])

;; =============================================================================
;; Basic Types with Generators
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
;; Type References with Generators
;; =============================================================================

(def TypeReference
  "A protobuf type reference (e.g., '.com.example.Message')"
  [:and
   {:gen/gen (gen/fmap (fn [[pkg type-name]]
                         (str "." pkg "." type-name))
                       (gen/tuple 
                        (regex-gen package-pattern)
                        (regex-gen pascal-case-pattern)))}
   :string
   [:fn {:error/message "must start with dot"} 
    #(clojure.string/starts-with? % ".")]])

(def ScalarType
  "Scalar type representation"
  [:map
   {:gen/gen (gen/fmap (fn [s] {:scalar s})
                       (enum-gen [:double :float :int32 :int64 :uint32 :uint64 
                                  :sint32 :sint64 :bool :string :bytes]))}
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
;; Basic IR Structures with Generators
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
                        (regex-gen upper-snake-pattern)
                        (gen/choose 0 100)))}
   [:name :keyword]
   [:proto-name :string]
   [:number :int]])

(def EnumDef
  "Enum definition"
  [:map
   {:gen/gen (gen/fmap 
              (fn [[proto-name pkg values]]
                {:type :enum
                 :name (keyword (clojure.string/lower-case proto-name))
                 :proto-name proto-name
                 :package pkg
                 :values (vec (map-indexed 
                               (fn [i v] (assoc v :number i))
                               values))})
              (gen/tuple
               (regex-gen pascal-case-pattern)
               (regex-gen package-pattern)
               (gen/vector (:gen/gen (m/properties EnumValue)) 1 5)))}
   [:type [:= :enum]]
   [:name :keyword]
   [:proto-name :string]
   [:package :string]
   [:java-class {:optional true} :string]
   [:values [:vector EnumValue]]])

(def FieldConstraints
  "Field validation constraints"
  [:map
   {:gen/gen (gen/return {})}  ; Simple empty constraints for now
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
               (regex-gen snake-case-pattern)
               (gen/choose 1 1000)
               (enum-gen [:label-optional :label-required :label-repeated])
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
               (regex-gen snake-case-pattern)
               (gen/choose 0 10)
               (gen/vector (:gen/gen (m/properties Field)) 2 5)))}
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
                {:type :message
                 :name (keyword (clojure.string/lower-case proto-name))
                 :proto-name proto-name
                 :package pkg
                 :fields (vec (map-indexed 
                               (fn [i f] (assoc f :number (inc i)))
                               fields))})
              (gen/tuple
               (regex-gen pascal-case-pattern)
               (regex-gen package-pattern)
               (gen/vector (:gen/gen (m/properties Field)) 1 5)))}
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
   {:gen/gen (gen/let [filename (regex-gen proto-filename-pattern)
                       package (regex-gen package-pattern)
                       num-deps (gen/choose 0 3)
                       deps (gen/vector (regex-gen proto-filename-pattern) num-deps)
                       enums (gen/vector (:gen/gen (m/properties EnumDef)) 0 3)
                       messages (gen/vector (:gen/gen (m/properties MessageDef)) 0 5)]
               (let [;; Ensure consistent package
                     enums' (mapv #(assoc % :package package) enums)
                     messages' (mapv #(assoc % :package package) messages)
                     ;; Remove self-dependencies
                     deps' (vec (remove #(= % filename) deps))]
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
;; Registry Setup
;; =============================================================================

;; Define all specs in a map for registration
(def all-specs
  {;; Type references
   ::TypeReference TypeReference
   ::ScalarType ScalarType
   ::MessageType MessageType
   ::EnumType EnumType
   ::UnknownType UnknownType
   ::FieldType FieldType
   
   ;; Basic structures
   ::EnumValue EnumValue
   ::EnumDef EnumDef
   ::FieldConstraints FieldConstraints
   ::Field Field
   ::OneofDef OneofDef
   ::MessageDef MessageDef
   ::FileDef FileDef
   ::DescriptorSet DescriptorSet})

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