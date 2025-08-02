(ns potatoclient.specs.google.protobuf "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def EnumValueOptions "Schema for EnumValueOptions" [:map [:deprecated [:maybe :boolean]] [:features [:maybe :google/FeatureSet]] [:debug-redact [:maybe :boolean]] [:feature-support [:maybe :google/FeatureSupport]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def ExtensionRangeOptions "Schema for ExtensionRangeOptions" [:map [:uninterpreted-option [:vector :google/UninterpretedOption]] [:declaration [:vector :google/Declaration]] [:features [:maybe :google/FeatureSet]] [:verification [:maybe :google/VerificationState]]])

(def OneofDescriptorProto "Schema for OneofDescriptorProto" [:map [:name [:maybe :string]] [:options [:maybe :google/OneofOptions]]])

(def OneofOptions "Schema for OneofOptions" [:map [:features [:maybe :google/FeatureSet]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def FeatureSetDefaults "Schema for FeatureSetDefaults" [:map [:defaults [:vector :google/FeatureSetEditionDefault]] [:minimum-edition [:maybe :google/Edition]] [:maximum-edition [:maybe :google/Edition]]])

(def SymbolVisibility "Schema for SymbolVisibility" [:enum :VISIBILITY-UNSET :VISIBILITY-LOCAL :VISIBILITY-EXPORT])

(def DescriptorProto "Schema for DescriptorProto" [:map [:name [:maybe :string]] [:field [:vector :google/FieldDescriptorProto]] [:extension [:vector :google/FieldDescriptorProto]] [:nested-type [:vector :google/DescriptorProto]] [:enum-type [:vector :google/EnumDescriptorProto]] [:extension-range [:vector :google/ExtensionRange]] [:oneof-decl [:vector :google/OneofDescriptorProto]] [:options [:maybe :google/MessageOptions]] [:reserved-range [:vector :google/ReservedRange]] [:reserved-name [:vector :string]] [:visibility [:maybe :google/SymbolVisibility]]])

(def FeatureSet "Schema for FeatureSet" [:map [:field-presence [:maybe :google/FieldPresence]] [:enum-type [:maybe :google/EnumType]] [:repeated-field-encoding [:maybe :google/RepeatedFieldEncoding]] [:utf8-validation [:maybe :google/Utf8Validation]] [:message-encoding [:maybe :google/MessageEncoding]] [:json-format [:maybe :google/JsonFormat]] [:enforce-naming-style [:maybe :google/EnforceNamingStyle]] [:default-symbol-visibility [:maybe :google/DefaultSymbolVisibility]]])

(def GeneratedCodeInfo "Schema for GeneratedCodeInfo" [:map [:annotation [:vector :google/Annotation]]])

(def ServiceOptions "Schema for ServiceOptions" [:map [:features [:maybe :google/FeatureSet]] [:deprecated [:maybe :boolean]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def Edition "Schema for Edition" [:enum :EDITION-UNKNOWN :EDITION-LEGACY :EDITION-PROTO2 :EDITION-PROTO3 :EDITION-2023 :EDITION-2024 :EDITION-1-TEST-ONLY :EDITION-2-TEST-ONLY :EDITION-99997-TEST-ONLY :EDITION-99998-TEST-ONLY :EDITION-99999-TEST-ONLY :EDITION-MAX])

(def MethodDescriptorProto "Schema for MethodDescriptorProto" [:map [:name [:maybe :string]] [:input-type [:maybe :string]] [:output-type [:maybe :string]] [:options [:maybe :google/MethodOptions]] [:client-streaming [:maybe :boolean]] [:server-streaming [:maybe :boolean]]])

(def MethodOptions "Schema for MethodOptions" [:map [:deprecated [:maybe :boolean]] [:idempotency-level [:maybe :google/IdempotencyLevel]] [:features [:maybe :google/FeatureSet]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def EnumOptions "Schema for EnumOptions" [:map [:allow-alias [:maybe :boolean]] [:deprecated [:maybe :boolean]] [:deprecated-legacy-json-field-conflicts [:maybe :boolean]] [:features [:maybe :google/FeatureSet]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def MessageOptions "Schema for MessageOptions" [:map [:message-set-wire-format [:maybe :boolean]] [:no-standard-descriptor-accessor [:maybe :boolean]] [:deprecated [:maybe :boolean]] [:map-entry [:maybe :boolean]] [:deprecated-legacy-json-field-conflicts [:maybe :boolean]] [:features [:maybe :google/FeatureSet]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def EnumDescriptorProto "Schema for EnumDescriptorProto" [:map [:name [:maybe :string]] [:value [:vector :google/EnumValueDescriptorProto]] [:options [:maybe :google/EnumOptions]] [:reserved-range [:vector :google/EnumReservedRange]] [:reserved-name [:vector :string]] [:visibility [:maybe :google/SymbolVisibility]]])

(def FileOptions "Schema for FileOptions" [:map [:java-package [:maybe :string]] [:java-outer-classname [:maybe :string]] [:java-multiple-files [:maybe :boolean]] [:java-generate-equals-and-hash [:maybe :boolean]] [:java-string-check-utf8 [:maybe :boolean]] [:optimize-for [:maybe :google/OptimizeMode]] [:go-package [:maybe :string]] [:cc-generic-services [:maybe :boolean]] [:java-generic-services [:maybe :boolean]] [:py-generic-services [:maybe :boolean]] [:deprecated [:maybe :boolean]] [:cc-enable-arenas [:maybe :boolean]] [:objc-class-prefix [:maybe :string]] [:csharp-namespace [:maybe :string]] [:swift-prefix [:maybe :string]] [:php-class-prefix [:maybe :string]] [:php-namespace [:maybe :string]] [:php-metadata-namespace [:maybe :string]] [:ruby-package [:maybe :string]] [:features [:maybe :google/FeatureSet]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])

(def UninterpretedOption "Schema for UninterpretedOption" [:map [:name [:vector :google/NamePart]] [:identifier-value [:maybe :string]] [:positive-int-value [:maybe :int]] [:negative-int-value [:maybe :int]] [:double-value [:maybe :double]] [:string-value [:maybe :bytes]] [:aggregate-value [:maybe :string]]])

(def SourceCodeInfo "Schema for SourceCodeInfo" [:map [:location [:vector :google/Location]]])

(def FileDescriptorProto "Schema for FileDescriptorProto" [:map [:name [:maybe :string]] [:package [:maybe :string]] [:dependency [:vector :string]] [:public-dependency [:vector :int]] [:weak-dependency [:vector :int]] [:option-dependency [:vector :string]] [:message-type [:vector :google/DescriptorProto]] [:enum-type [:vector :google/EnumDescriptorProto]] [:service [:vector :google/ServiceDescriptorProto]] [:extension [:vector :google/FieldDescriptorProto]] [:options [:maybe :google/FileOptions]] [:source-code-info [:maybe :google/SourceCodeInfo]] [:syntax [:maybe :string]] [:edition [:maybe :google/Edition]]])

(def EnumValueDescriptorProto "Schema for EnumValueDescriptorProto" [:map [:name [:maybe :string]] [:number [:maybe :int]] [:options [:maybe :google/EnumValueOptions]]])

(def ServiceDescriptorProto "Schema for ServiceDescriptorProto" [:map [:name [:maybe :string]] [:method [:vector :google/MethodDescriptorProto]] [:options [:maybe :google/ServiceOptions]]])

(def Duration "Schema for Duration" [:map [:seconds [:maybe :int]] [:nanos [:maybe :int]]])

(def Timestamp "Schema for Timestamp" [:map [:seconds [:maybe :int]] [:nanos [:maybe :int]]])

(def FileDescriptorSet "Schema for FileDescriptorSet" [:map [:file [:vector :google/FileDescriptorProto]]])

(def FieldDescriptorProto "Schema for FieldDescriptorProto" [:map [:name [:maybe :string]] [:number [:maybe :int]] [:label [:maybe :google/Label]] [:type [:maybe :google/Type]] [:type-name [:maybe :string]] [:extendee [:maybe :string]] [:default-value [:maybe :string]] [:oneof-index [:maybe :int]] [:json-name [:maybe :string]] [:options [:maybe :google/FieldOptions]] [:proto3-optional [:maybe :boolean]]])

(def FieldOptions "Schema for FieldOptions" [:map [:ctype [:maybe :google/CType]] [:packed [:maybe :boolean]] [:jstype [:maybe :google/JSType]] [:lazy [:maybe :boolean]] [:unverified-lazy [:maybe :boolean]] [:deprecated [:maybe :boolean]] [:weak [:maybe :boolean]] [:debug-redact [:maybe :boolean]] [:retention [:maybe :google/OptionRetention]] [:targets [:vector :google/OptionTargetType]] [:edition-defaults [:vector :google/EditionDefault]] [:features [:maybe :google/FeatureSet]] [:feature-support [:maybe :google/FeatureSupport]] [:uninterpreted-option [:vector :google/UninterpretedOption]]])