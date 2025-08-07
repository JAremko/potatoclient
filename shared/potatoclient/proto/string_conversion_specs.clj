(ns potatoclient.proto.string-conversion-specs
  "Malli specifications with attached Regal generators for string conversions.
  These specs are used both for guardrails and property-based testing."
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [lambdaisland.regal :as regal]
            [lambdaisland.regal.generator :as regal-gen]))

;; =============================================================================
;; Regal Patterns for String Types
;; =============================================================================

(def camel-case-pattern
  "Regal pattern for camelCase: starts lowercase, has at least one uppercase"
  [:cat
   [:class [\a \z]]
   [:* [:class [\a \z] [\0 \9]]]
   [:+ [:cat [:class [\A \Z]] [:* [:class [\a \z] [\0 \9]]]]]])

(def pascal-case-pattern
  "Regal pattern for PascalCase: starts uppercase"
  [:cat
   [:class [\A \Z]]
   [:* [:class [\a \z] [\A \Z] [\0 \9]]]])

(def snake-case-pattern
  "Regal pattern for snake_case: lowercase with underscores"
  [:cat
   [:class [\a \z]]
   [:* [:alt [:class [\a \z] [\0 \9]] \_]]])

(def proto-constant-pattern
  "Regal pattern for PROTO_CONSTANT: uppercase with underscores"
  [:cat
   [:+ [:class [\A \Z]]]
   [:* [:cat \_ [:+ [:class [\A \Z] [\0 \9]]]]]])

(def kebab-case-pattern
  "Regal pattern for kebab-case: lowercase with hyphens"
  [:cat
   [:class [\a \z]]
   [:* [:alt [:class [\a \z] [\0 \9]] \-]]])

;; Special patterns for protobuf constants
(def proto-type-constant-pattern
  "Pattern for protobuf type constants like TYPE_INT32"
  [:cat
   "TYPE_"
   [:alt "DOUBLE" "FLOAT" "BOOL" "STRING" "BYTES"
         [:cat [:alt "INT" "UINT" "SINT" "FIXED" "SFIXED"] [:alt "32" "64"]]]])

(def proto-label-constant-pattern
  "Pattern for protobuf label constants"
  [:cat "LABEL_" [:alt "OPTIONAL" "REQUIRED" "REPEATED"]])

;; Patterns with numeric suffixes
(def numeric-suffix-pattern
  "Pattern for identifiers with numbers like Mode2D, TYPE_INT32"
  [:alt
   ;; PascalCase with number: Mode2D, Type3D
   [:cat
    [:+ [:alt [:class [\A \Z]] [:class [\a \z]]]]
    [:repeat [:class [\0 \9]] 1 2]
    [:? [:class [\A \Z]]]]
   ;; Underscore with number: TYPE_INT32, FIELD_1
   [:cat
    [:+ [:class [\A \Z]]]
    "_"
    [:alt
     [:repeat [:class [\0 \9]] 1 2]
     [:cat [:alt "INT" "UINT"] [:alt "32" "64"]]]]])

;; =============================================================================
;; Input Type Specs with Attached Generators
;; =============================================================================

(def CamelCaseString
  "Spec for camelCase strings with attached Regal generator"
  [:and
   :string
   [:fn {:error/message "must be camelCase"}
    #(and (string? %)
          (re-matches (regal/regex camel-case-pattern) %))]
   [::m/gen #(regal-gen/gen camel-case-pattern)]])

(def PascalCaseString
  "Spec for PascalCase strings with attached Regal generator"
  [:and
   :string
   [:fn {:error/message "must be PascalCase"}
    #(re-matches (regal/regex pascal-case-pattern) %)]
   [::m/gen #(regal-gen/gen pascal-case-pattern)]])

(def SnakeCaseString
  "Spec for snake_case strings with attached Regal generator"
  [:and
   :string
   [:fn {:error/message "must be snake_case"}
    #(re-matches (regal/regex snake-case-pattern) %)]
   [::m/gen #(regal-gen/gen snake-case-pattern)]])

(def ProtoConstantString
  "Spec for PROTO_CONSTANT strings with attached Regal generator"
  [:and
   :string
   [:fn {:error/message "must be PROTO_CONSTANT format"}
    #(re-matches (regal/regex proto-constant-pattern) %)]
   [::m/gen #(regal-gen/gen proto-constant-pattern)]])

(def KebabCaseString
  "Spec for kebab-case strings with attached Regal generator"
  [:and
   :string
   [:fn {:error/message "must be kebab-case"}
    #(re-matches (regal/regex kebab-case-pattern) %)]
   [::m/gen #(regal-gen/gen kebab-case-pattern)]])

(def ProtoTypeConstantString
  "Spec for protobuf type constants with attached generator"
  [:and
   :string
   [:fn {:error/message "must be a protobuf type constant"}
    #(re-matches (regal/regex proto-type-constant-pattern) %)]
   [::m/gen #(regal-gen/gen proto-type-constant-pattern)]])

(def NumericSuffixString
  "Spec for strings with numeric suffixes"
  [:and
   :string
   [:fn {:error/message "must have numeric suffix"}
    #(re-matches (regal/regex numeric-suffix-pattern) %)]
   [::m/gen #(regal-gen/gen numeric-suffix-pattern)]])

(def MixedCaseString
  "Spec for any string that needs conversion with attached mixed generator"
  [:or
   CamelCaseString
   PascalCaseString
   SnakeCaseString
   ProtoConstantString
   KebabCaseString
   ProtoTypeConstantString
   NumericSuffixString])

;; =============================================================================
;; Conversion Function Specs with Generators
;; =============================================================================

(def StringToKebabCase
  "Spec for ->kebab-case function"
  [:function
   [:=> [:cat [:or :string :nil]] [:or KebabCaseString :nil]]
   [::m/gen #(mg/generator
              [:function
               [:=> [:cat MixedCaseString] KebabCaseString]])]])

(def StringToKebabCaseKeyword
  "Spec for ->kebab-case-keyword function"
  [:function
   [:=> [:cat [:or :string :nil]] [:or :keyword :nil]]
   [::m/gen #(mg/generator
              [:function
               [:=> [:cat MixedCaseString] :keyword]])]])

(def StringToPascalCase
  "Spec for ->PascalCase function"
  [:function
   [:=> [:cat [:or :string :nil]] [:or PascalCaseString :nil]]
   [::m/gen #(mg/generator
              [:function
               [:=> [:cat MixedCaseString] PascalCaseString]])]])

(def StringToSnakeCase
  "Spec for ->snake_case function"
  [:function
   [:=> [:cat [:or :string :nil]] [:or SnakeCaseString :nil]]
   [::m/gen #(mg/generator
              [:function
               [:=> [:cat MixedCaseString] SnakeCaseString]])]])

;; =============================================================================
;; Method Name Generation Specs
;; =============================================================================

(def field-name-pattern
  "Regal pattern for field names (camelCase or snake_case)"
  [:alt camel-case-pattern snake-case-pattern])

(def FieldNameString
  "Spec for field names with attached generator"
  [:and
   :string
   [:fn {:error/message "must be a valid field name"}
    #(re-matches (regal/regex field-name-pattern) %)]
   [::m/gen #(regal-gen/gen field-name-pattern)]])

;; Method name patterns and specs
(def getter-method-pattern
  [:cat "get" [:class [\A \Z]] [:* [:class [\a \z] [\A \Z] [\0 \9]]]])

(def GetterMethodString
  "Spec for getter method names with attached generator"
  [:and
   :string
   [:fn {:error/message "must be a getter method name"}
    #(re-matches (regal/regex getter-method-pattern) %)]
   [::m/gen #(regal-gen/gen getter-method-pattern)]])

(def setter-method-pattern
  [:cat "set" [:class [\A \Z]] [:* [:class [\a \z] [\A \Z] [\0 \9]]]])

(def SetterMethodString
  "Spec for setter method names with attached generator"
  [:and
   :string
   [:fn {:error/message "must be a setter method name"}
    #(re-matches (regal/regex setter-method-pattern) %)]
   [::m/gen #(regal-gen/gen setter-method-pattern)]])

(def has-method-pattern
  [:cat "has" [:class [\A \Z]] [:* [:class [\a \z] [\A \Z] [\0 \9]]]])

(def HasMethodString
  "Spec for has method names with attached generator"
  [:and
   :string
   [:fn {:error/message "must be a has method name"}
    #(re-matches (regal/regex has-method-pattern) %)]
   [::m/gen #(regal-gen/gen has-method-pattern)]])

;; =============================================================================
;; Function Specs for Guardrails
;; =============================================================================

(def GetterMethodName
  "Spec for getter method name function"
  [:=> [:cat FieldNameString] GetterMethodString])

(def SetterMethodName
  "Spec for setter method name function"
  [:=> [:cat FieldNameString] SetterMethodString])

(def HasMethodName
  "Spec for has method name function"
  [:=> [:cat FieldNameString] HasMethodString])

(def AddMethodName
  "Spec for add method name function"
  [:=> [:cat FieldNameString] GetterMethodString])  ; Reuses getter pattern

(def AddAllMethodName
  "Spec for addAll method name function"
  [:=> [:cat FieldNameString] GetterMethodString])  ; Reuses getter pattern

;; =============================================================================
;; Other Function Specs
;; =============================================================================

(def ProtoNameToClojureName
  "Spec for proto-name->clj-name function"
  [:=> [:cat [:or :string :keyword :nil]] [:or :keyword :string :nil]])

(def ClojureNameToProtoName
  "Spec for clj-name->proto-name function"
  [:=> [:cat [:or :keyword :string :nil]] [:or :string :keyword :nil]])

(def JsonKeyToClojureKey
  "Spec for json-key->clj-key function"
  [:=> [:cat :string] :keyword])

;; =============================================================================
;; Registry Setup
;; =============================================================================

(def registry
  "Registry of all string conversion specs with generators"
  {::CamelCaseString CamelCaseString
   ::PascalCaseString PascalCaseString
   ::SnakeCaseString SnakeCaseString
   ::ProtoConstantString ProtoConstantString
   ::KebabCaseString KebabCaseString
   ::ProtoTypeConstantString ProtoTypeConstantString
   ::NumericSuffixString NumericSuffixString
   ::MixedCaseString MixedCaseString
   ::FieldNameString FieldNameString
   ::GetterMethodString GetterMethodString
   ::SetterMethodString SetterMethodString
   ::HasMethodString HasMethodString
   ::StringToKebabCase StringToKebabCase
   ::StringToKebabCaseKeyword StringToKebabCaseKeyword
   ::StringToPascalCase StringToPascalCase
   ::StringToSnakeCase StringToSnakeCase
   ::ProtoNameToClojureName ProtoNameToClojureName
   ::ClojureNameToProtoName ClojureNameToProtoName
   ::JsonKeyToClojureKey JsonKeyToClojureKey
   ::GetterMethodName GetterMethodName
   ::SetterMethodName SetterMethodName
   ::HasMethodName HasMethodName
   ::AddMethodName AddMethodName
   ::AddAllMethodName AddAllMethodName})