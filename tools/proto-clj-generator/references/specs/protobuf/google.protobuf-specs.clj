(ns potatoclient.specs.google.protobuf
  "Generated Malli specs from protobuf descriptors"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def extension-range-options
  "Schema for extension-range-options"
  [:map [:uninterpreted-option [:vector :google/uninterpreted-option]]
   [:declaration [:vector :google/declaration]] [:features [:maybe :google/feature-set]]
   [:verification [:maybe :google/verification-state]]])


(def field-descriptor-proto
  "Schema for field-descriptor-proto"
  [:map [:name [:maybe :string]] [:number [:maybe :int]] [:label [:maybe :google/label]]
   [:type [:maybe :google/type]] [:type-name [:maybe :string]] [:extendee [:maybe :string]]
   [:default-value [:maybe :string]] [:oneof-index [:maybe :int]] [:json-name [:maybe :string]]
   [:options [:maybe :google/field-options]] [:proto3-optional [:maybe :boolean]]])


(def enum-options
  "Schema for enum-options"
  [:map [:allow-alias [:maybe :boolean]] [:deprecated [:maybe :boolean]]
   [:deprecated-legacy-json-field-conflicts [:maybe :boolean]]
   [:features [:maybe :google/feature-set]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def generated-code-info
  "Schema for generated-code-info"
  [:map [:annotation [:vector :google/annotation]]])


(def field-options
  "Schema for field-options"
  [:map [:ctype [:maybe :google/c-type]] [:packed [:maybe :boolean]]
   [:jstype [:maybe :google/js-type]] [:lazy [:maybe :boolean]] [:unverified-lazy [:maybe :boolean]]
   [:deprecated [:maybe :boolean]] [:weak [:maybe :boolean]] [:debug-redact [:maybe :boolean]]
   [:retention [:maybe :google/option-retention]] [:targets [:vector :google/option-target-type]]
   [:edition-defaults [:vector :google/edition-default]] [:features [:maybe :google/feature-set]]
   [:feature-support [:maybe :google/feature-support]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def method-descriptor-proto
  "Schema for method-descriptor-proto"
  [:map [:name [:maybe :string]] [:input-type [:maybe :string]] [:output-type [:maybe :string]]
   [:options [:maybe :google/method-options]] [:client-streaming [:maybe :boolean]]
   [:server-streaming [:maybe :boolean]]])


(def service-descriptor-proto
  "Schema for service-descriptor-proto"
  [:map [:name [:maybe :string]] [:method [:vector :google/method-descriptor-proto]]
   [:options [:maybe :google/service-options]]])


(def duration "Schema for duration" [:map [:seconds [:maybe :int]] [:nanos [:maybe :int]]])


(def descriptor-proto
  "Schema for descriptor-proto"
  [:map [:name [:maybe :string]] [:field [:vector :google/field-descriptor-proto]]
   [:extension [:vector :google/field-descriptor-proto]]
   [:nested-type [:vector :google/descriptor-proto]]
   [:enum-type [:vector :google/enum-descriptor-proto]]
   [:extension-range [:vector :google/extension-range]]
   [:oneof-decl [:vector :google/oneof-descriptor-proto]]
   [:options [:maybe :google/message-options]] [:reserved-range [:vector :google/reserved-range]]
   [:reserved-name [:vector :string]] [:visibility [:maybe :google/symbol-visibility]]])


(def file-options
  "Schema for file-options"
  [:map [:java-package [:maybe :string]] [:java-outer-classname [:maybe :string]]
   [:java-multiple-files [:maybe :boolean]] [:java-generate-equals-and-hash [:maybe :boolean]]
   [:java-string-check-utf8 [:maybe :boolean]] [:optimize-for [:maybe :google/optimize-mode]]
   [:go-package [:maybe :string]] [:cc-generic-services [:maybe :boolean]]
   [:java-generic-services [:maybe :boolean]] [:py-generic-services [:maybe :boolean]]
   [:deprecated [:maybe :boolean]] [:cc-enable-arenas [:maybe :boolean]]
   [:objc-class-prefix [:maybe :string]] [:csharp-namespace [:maybe :string]]
   [:swift-prefix [:maybe :string]] [:php-class-prefix [:maybe :string]]
   [:php-namespace [:maybe :string]] [:php-metadata-namespace [:maybe :string]]
   [:ruby-package [:maybe :string]] [:features [:maybe :google/feature-set]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def method-options
  "Schema for method-options"
  [:map [:deprecated [:maybe :boolean]] [:idempotency-level [:maybe :google/idempotency-level]]
   [:features [:maybe :google/feature-set]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def file-descriptor-set
  "Schema for file-descriptor-set"
  [:map [:file [:vector :google/file-descriptor-proto]]])


(def service-options
  "Schema for service-options"
  [:map [:features [:maybe :google/feature-set]] [:deprecated [:maybe :boolean]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def enum-descriptor-proto
  "Schema for enum-descriptor-proto"
  [:map [:name [:maybe :string]] [:value [:vector :google/enum-value-descriptor-proto]]
   [:options [:maybe :google/enum-options]] [:reserved-range [:vector :google/enum-reserved-range]]
   [:reserved-name [:vector :string]] [:visibility [:maybe :google/symbol-visibility]]])


(def uninterpreted-option
  "Schema for uninterpreted-option"
  [:map [:name [:vector :google/name-part]] [:identifier-value [:maybe :string]]
   [:positive-int-value [:maybe :int]] [:negative-int-value [:maybe :int]]
   [:double-value [:maybe :double]] [:string-value [:maybe :bytes]]
   [:aggregate-value [:maybe :string]]])


(def oneof-options
  "Schema for oneof-options"
  [:map [:features [:maybe :google/feature-set]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def file-descriptor-proto
  "Schema for file-descriptor-proto"
  [:map [:name [:maybe :string]] [:package [:maybe :string]] [:dependency [:vector :string]]
   [:public-dependency [:vector :int]] [:weak-dependency [:vector :int]]
   [:option-dependency [:vector :string]] [:message-type [:vector :google/descriptor-proto]]
   [:enum-type [:vector :google/enum-descriptor-proto]]
   [:service [:vector :google/service-descriptor-proto]]
   [:extension [:vector :google/field-descriptor-proto]] [:options [:maybe :google/file-options]]
   [:source-code-info [:maybe :google/source-code-info]] [:syntax [:maybe :string]]
   [:edition [:maybe :google/edition]]])


(def feature-set
  "Schema for feature-set"
  [:map [:field-presence [:maybe :google/field-presence]] [:enum-type [:maybe :google/enum-type]]
   [:repeated-field-encoding [:maybe :google/repeated-field-encoding]]
   [:utf8-validation [:maybe :google/utf8-validation]]
   [:message-encoding [:maybe :google/message-encoding]] [:json-format [:maybe :google/json-format]]
   [:enforce-naming-style [:maybe :google/enforce-naming-style]]
   [:default-symbol-visibility [:maybe :google/default-symbol-visibility]]])


(def enum-value-options
  "Schema for enum-value-options"
  [:map [:deprecated [:maybe :boolean]] [:features [:maybe :google/feature-set]]
   [:debug-redact [:maybe :boolean]] [:feature-support [:maybe :google/feature-support]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def edition
  "Schema for edition"
  [:enum :edition-unknown :edition-legacy :edition-proto2 :edition-proto3 :edition-2023
   :edition-2024 :edition-1-test-only :edition-2-test-only :edition-99997-test-only
   :edition-99998-test-only :edition-99999-test-only :edition-max])


(def timestamp "Schema for timestamp" [:map [:seconds [:maybe :int]] [:nanos [:maybe :int]]])


(def symbol-visibility
  "Schema for symbol-visibility"
  [:enum :visibility-unset :visibility-local :visibility-export])


(def source-code-info "Schema for source-code-info" [:map [:location [:vector :google/location]]])


(def message-options
  "Schema for message-options"
  [:map [:message-set-wire-format [:maybe :boolean]]
   [:no-standard-descriptor-accessor [:maybe :boolean]] [:deprecated [:maybe :boolean]]
   [:map-entry [:maybe :boolean]] [:deprecated-legacy-json-field-conflicts [:maybe :boolean]]
   [:features [:maybe :google/feature-set]]
   [:uninterpreted-option [:vector :google/uninterpreted-option]]])


(def oneof-descriptor-proto
  "Schema for oneof-descriptor-proto"
  [:map [:name [:maybe :string]] [:options [:maybe :google/oneof-options]]])


(def enum-value-descriptor-proto
  "Schema for enum-value-descriptor-proto"
  [:map [:name [:maybe :string]] [:number [:maybe :int]]
   [:options [:maybe :google/enum-value-options]]])


(def feature-set-defaults
  "Schema for feature-set-defaults"
  [:map [:defaults [:vector :google/feature-set-edition-default]]
   [:minimum-edition [:maybe :google/edition]] [:maximum-edition [:maybe :google/edition]]])
