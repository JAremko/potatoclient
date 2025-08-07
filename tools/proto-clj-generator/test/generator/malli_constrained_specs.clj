(ns generator.malli-constrained-specs
  "Constrained specs for better test data generation"
  (:require [generator.specs :as specs]
            [malli.core :as m]))

;; Define more constrained specs that generate better test data

(def proto-filename
  "Valid protobuf filename"
  [:enum "test.proto" "api.proto" "types.proto" "common.proto" "service.proto"])

(def package-name
  "Valid package name"
  [:enum "com.test" "com.api" "com.types" "org.example" "io.service"])

(def TestEnumDef
  "Enum with sensible test constraints"
  [:map
   [:type [:= :enum]]
   [:name :keyword]
   [:proto-name [:enum "Status" "Type" "Action" "Mode"]]
   [:package package-name]
   [:values [:and 
             [:vector [:map
                       [:name :keyword]
                       [:proto-name [:enum "UNKNOWN" "SUCCESS" "FAILURE" "PENDING" "ACTIVE"]]
                       [:number [:int {:min 0 :max 100}]]]]
             [:fn {:error/message "must have at least one value"} 
              #(pos? (count %))]]]])

(def SimpleFieldType
  "Simple field type for testing"
  [:map [:scalar specs/proto-scalar-types]])

(def TestMessageDef  
  "Message with sensible test constraints"
  [:map
   [:type [:= :message]]
   [:name :keyword]
   [:proto-name [:enum "Request" "Response" "Data" "Info" "Config"]]
   [:package package-name]
   [:fields [:and
             [:vector [:map
                       [:name :keyword]
                       [:proto-name [:enum "id" "name" "value" "status" "data"]]
                       [:number [:int {:min 1 :max 1000}]]
                       [:label specs/proto-labels]
                       [:type SimpleFieldType]]]
             [:fn {:error/message "must have at least one field"}
              #(pos? (count %))]]]])

(def TestFileDef
  "File with sensible test constraints"
  [:map
   [:type [:= :file]]
   [:name proto-filename]
   [:package package-name]
   [:dependencies [:vector proto-filename]]
   [:messages [:vector TestMessageDef]]
   [:enums [:vector TestEnumDef]]])

(def TestDescriptorSet
  "Descriptor set with sensible test constraints"
  [:map
   [:type [:= :descriptor-set]]
   [:files [:and 
            [:vector TestFileDef]
            [:fn {:error/message "must have at least one file"}
             #(pos? (count %))]]]])

;; Create a registry that includes both original and test specs
(def test-registry
  (merge (m/default-schemas)
         {:generator.specs/EnumDef specs/EnumDef
          :generator.specs/MessageDef specs/MessageDef
          :generator.specs/FileDef specs/FileDef
          :generator.specs/DescriptorSet specs/DescriptorSet
          :generator.specs/FieldType specs/FieldType
          :generator.specs/proto-labels specs/proto-labels
          :generator.malli-constrained-specs/TestEnumDef TestEnumDef
          :generator.malli-constrained-specs/TestMessageDef TestMessageDef
          :generator.malli-constrained-specs/TestFileDef TestFileDef
          :generator.malli-constrained-specs/TestDescriptorSet TestDescriptorSet}))