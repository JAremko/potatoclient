(ns generator.spec-generators
  "Custom generators for the real specs used in production.
  These generators create valid test data that matches our IR structure."
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [clojure.test.check.generators :as gen]
            [generator.specs :as specs]))

;; =============================================================================
;; Basic Generators
;; =============================================================================

(def package-name-gen
  "Generator for valid package names"
  (gen/elements ["com.test" "com.api" "com.example" "org.service" "io.proto"]))

(def proto-filename-gen
  "Generator for valid proto filenames"
  (gen/fmap #(str % ".proto")
            (gen/elements ["test" "api" "types" "service" "common"])))

(def proto-name-gen
  "Generator for valid proto names (PascalCase)"
  (gen/elements ["Request" "Response" "Status" "Config" "Data" "Info" "Error"]))

(def field-name-gen
  "Generator for valid field names (snake_case)"
  (gen/elements ["id" "name" "value" "status" "data" "info" "type"]))

(def enum-value-name-gen
  "Generator for valid enum value names (UPPER_SNAKE)"
  (gen/elements ["UNKNOWN" "SUCCESS" "FAILURE" "PENDING" "ACTIVE" "INACTIVE"]))

;; =============================================================================
;; Type Reference Generators
;; =============================================================================

(def type-reference-gen
  "Generator for valid TypeReference (starts with dot)"
  (gen/fmap (fn [[pkg name]]
              (str "." pkg "." name))
            (gen/tuple package-name-gen proto-name-gen)))

;; =============================================================================
;; Field Type Generators
;; =============================================================================

(def scalar-type-gen
  "Generator for ScalarType"
  (gen/fmap (fn [scalar] {:scalar scalar})
            (gen/elements [:double :float :int32 :int64 :uint32 :uint64 
                           :sint32 :sint64 :fixed32 :fixed64 :sfixed32 :sfixed64 
                           :bool :string :bytes])))

(def message-type-gen
  "Generator for MessageType"
  (gen/fmap (fn [ref] {:message {:type-ref ref}})
            type-reference-gen))

(def enum-type-gen
  "Generator for EnumType"
  (gen/fmap (fn [ref] {:enum {:type-ref ref}})
            type-reference-gen))

(def field-type-gen
  "Generator for FieldType - prefer scalars for simpler tests"
  (gen/frequency [[7 scalar-type-gen]
                  [2 message-type-gen]
                  [1 enum-type-gen]]))

;; =============================================================================
;; Structure Generators
;; =============================================================================

(def enum-value-gen
  "Generator for EnumValue"
  (gen/fmap (fn [[name num]]
              {:name (keyword (clojure.string/lower-case name))
               :proto-name name
               :number num})
            (gen/tuple enum-value-name-gen
                       (gen/choose 0 100))))

(def enum-def-gen
  "Generator for EnumDef"
  (gen/fmap (fn [[name pkg values]]
              {:type :enum
               :name (keyword (clojure.string/lower-case name))
               :proto-name name
               :package pkg
               :values (vec (map-indexed (fn [i v]
                                           (assoc v :number i))
                                         values))})
            (gen/tuple proto-name-gen
                       package-name-gen
                       (gen/vector enum-value-gen 1 5))))

(def field-gen
  "Generator for Field"
  (gen/fmap (fn [[name num type label]]
              {:name (keyword name)
               :proto-name name
               :number num
               :label label
               :type type})
            (gen/tuple field-name-gen
                       (gen/choose 1 1000)
                       field-type-gen
                       (gen/elements [:label-optional :label-required :label-repeated]))))

(def message-def-gen
  "Generator for MessageDef"
  (gen/fmap (fn [[name pkg fields]]
              {:type :message
               :name (keyword (clojure.string/lower-case name))
               :proto-name name
               :package pkg
               :fields (vec (map-indexed (fn [i f]
                                           (assoc f :number (inc i)))
                                         fields))})
            (gen/tuple proto-name-gen
                       package-name-gen
                       (gen/vector field-gen 1 5))))

(def file-def-gen
  "Generator for FileDef"
  (gen/let [filename proto-filename-gen
            package package-name-gen
            num-deps (gen/choose 0 3)
            deps (gen/vector proto-filename-gen num-deps)
            enums (gen/vector enum-def-gen 0 3)
            messages (gen/vector message-def-gen 0 5)]
    (let [;; Ensure consistent package for all definitions
          enums' (mapv #(assoc % :package package) enums)
          messages' (mapv #(assoc % :package package) messages)
          ;; Remove self-dependencies
          deps' (vec (remove #(= % filename) deps))]
      {:type :file
       :name filename
       :package package
       :dependencies deps'
       :messages messages'
       :enums enums'})))

(def descriptor-set-gen
  "Generator for DescriptorSet"
  (gen/fmap (fn [files]
              ;; Ensure unique filenames
              (let [unique-files (reduce (fn [acc file]
                                           (if (some #(= (:name %) (:name file)) acc)
                                             acc
                                             (conj acc file)))
                                         []
                                         files)
                    ;; Get all filenames in the set
                    filenames (set (map :name unique-files))
                    ;; Filter dependencies to only reference existing files
                    filtered-files (mapv (fn [file]
                                           (update file :dependencies
                                                   (fn [deps]
                                                     (vec (filter filenames deps)))))
                                         unique-files)]
                {:type :descriptor-set
                 :files filtered-files}))
            (gen/vector file-def-gen 1 5)))

;; =============================================================================
;; Custom Registry with Generators
;; =============================================================================

(def custom-generators
  "Map of spec keys to their custom generators"
  {::specs/TypeReference type-reference-gen
   ::specs/ScalarType scalar-type-gen
   ::specs/MessageType message-type-gen
   ::specs/EnumType enum-type-gen
   ::specs/FieldType field-type-gen
   ::specs/EnumValue enum-value-gen
   ::specs/EnumDef enum-def-gen
   ::specs/Field field-gen
   ::specs/MessageDef message-def-gen
   ::specs/FileDef file-def-gen
   ::specs/DescriptorSet descriptor-set-gen})

(defn with-generator
  "Attach a custom generator to a spec"
  [spec gen]
  (m/schema spec {:registry specs/registry
                  :gen/gen gen}))

(def enriched-registry
  "Registry with specs that have custom generators attached"
  specs/registry)