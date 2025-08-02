(ns potatoclient.specs.buf.validate "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

;; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed
;; These will be replaced with actual implementations when loaded

(def timestamp-rules "Schema for timestamp-rules" [:map [:const [:maybe :google/timestamp]] [:within [:maybe :google/duration]] [:example [:vector :google/timestamp]] [:less-than [:oneof {:lt [:map [:lt [:maybe :google/timestamp]]], :lte [:map [:lte [:maybe :google/timestamp]]], :lt-now [:map [:lt-now [:maybe :boolean]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :google/timestamp]]], :gte [:map [:gte [:maybe :google/timestamp]]], :gt-now [:map [:gt-now [:maybe :boolean]]]}]]])

(def float-rules "Schema for float-rules" [:map [:const [:maybe :double]] [:in [:vector :double]] [:not-in [:vector :double]] [:finite [:maybe :boolean]] [:example [:vector :double]] [:less-than [:oneof {:lt [:map [:lt [:maybe :double]]], :lte [:map [:lte [:maybe :double]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :double]]], :gte [:map [:gte [:maybe :double]]]}]]])

(def field-rules "Schema for field-rules" [:map [:cel [:vector :buf/rule]] [:required [:maybe :boolean]] [:ignore [:maybe :buf/ignore]] [:type [:oneof {:int32 [:map [:int32 [:maybe :buf/int32-rules]]], :enum [:map [:enum [:maybe :buf/enum-rules]]], :double [:map [:double [:maybe :buf/double-rules]]], :int64 [:map [:int64 [:maybe :buf/int64-rules]]], :uint64 [:map [:uint64 [:maybe :buf/u-int64-rules]]], :float [:map [:float [:maybe :buf/float-rules]]], :duration [:map [:duration [:maybe :buf/duration-rules]]], :fixed64 [:map [:fixed64 [:maybe :buf/fixed64-rules]]], :string [:map [:string [:maybe :buf/string-rules]]], :uint32 [:map [:uint32 [:maybe :buf/u-int32-rules]]], :sfixed64 [:map [:sfixed64 [:maybe :buf/s-fixed64-rules]]], :fixed32 [:map [:fixed32 [:maybe :buf/fixed32-rules]]], :bytes [:map [:bytes [:maybe :buf/bytes-rules]]], :sint32 [:map [:sint32 [:maybe :buf/s-int32-rules]]], :sint64 [:map [:sint64 [:maybe :buf/s-int64-rules]]], :bool [:map [:bool [:maybe :buf/bool-rules]]], :timestamp [:map [:timestamp [:maybe :buf/timestamp-rules]]], :sfixed32 [:map [:sfixed32 [:maybe :buf/s-fixed32-rules]]], :map [:map [:map [:maybe :buf/map-rules]]], :any [:map [:any [:maybe :buf/any-rules]]], :repeated [:map [:repeated [:maybe :buf/repeated-rules]]]}]]])

(def int64-rules "Schema for int64-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def any-rules "Schema for any-rules" [:map [:in [:vector :string]] [:not-in [:vector :string]]])

(def field-path "Schema for field-path" [:map [:elements [:vector :buf/field-path-element]]])

(def u-int64-rules "Schema for u-int64-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def s-int64-rules "Schema for s-int64-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def rule "Schema for rule" [:map [:id [:maybe :string]] [:message [:maybe :string]] [:expression [:maybe :string]]])

(def s-int32-rules "Schema for s-int32-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def fixed64-rules "Schema for fixed64-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def repeated-rules "Schema for repeated-rules" [:map [:min-items [:maybe :int]] [:max-items [:maybe :int]] [:unique [:maybe :boolean]] [:items [:maybe :buf/field-rules]]])

(def s-fixed64-rules "Schema for s-fixed64-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def message-rules "Schema for message-rules" [:map [:cel [:vector :buf/rule]] [:oneof [:vector :buf/message-oneof-rule]]])

(def enum-rules "Schema for enum-rules" [:map [:const [:maybe :int]] [:defined-only [:maybe :boolean]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]]])

(def fixed32-rules "Schema for fixed32-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def predefined-rules "Schema for predefined-rules" [:map [:cel [:vector :buf/rule]]])

(def ignore "Schema for ignore" [:enum :ignore-unspecified :ignore-if-zero-value :ignore-always])

(def message-oneof-rule "Schema for message-oneof-rule" [:map [:fields [:vector :string]] [:required [:maybe :boolean]]])

(def int32-rules "Schema for int32-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def known-regex "Schema for known-regex" [:enum :known-regex-unspecified :known-regex-http-header-name :known-regex-http-header-value])

(def oneof-rules "Schema for oneof-rules" [:map [:required [:maybe :boolean]]])

(def u-int32-rules "Schema for u-int32-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def bool-rules "Schema for bool-rules" [:map [:const [:maybe :boolean]] [:example [:vector :boolean]]])

(def double-rules "Schema for double-rules" [:map [:const [:maybe :double]] [:in [:vector :double]] [:not-in [:vector :double]] [:finite [:maybe :boolean]] [:example [:vector :double]] [:less-than [:oneof {:lt [:map [:lt [:maybe :double]]], :lte [:map [:lte [:maybe :double]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :double]]], :gte [:map [:gte [:maybe :double]]]}]]])

(def s-fixed32-rules "Schema for s-fixed32-rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def string-rules "Schema for string-rules" [:map [:const [:maybe :string]] [:len [:maybe :int]] [:min-len [:maybe :int]] [:max-len [:maybe :int]] [:len-bytes [:maybe :int]] [:min-bytes [:maybe :int]] [:max-bytes [:maybe :int]] [:pattern [:maybe :string]] [:prefix [:maybe :string]] [:suffix [:maybe :string]] [:contains [:maybe :string]] [:not-contains [:maybe :string]] [:in [:vector :string]] [:not-in [:vector :string]] [:strict [:maybe :boolean]] [:example [:vector :string]] [:well-known [:oneof {:host-and-port [:map [:host-and-port [:maybe :boolean]]], :ip-with-prefixlen [:map [:ip-with-prefixlen [:maybe :boolean]]], :address [:map [:address [:maybe :boolean]]], :email [:map [:email [:maybe :boolean]]], :ip [:map [:ip [:maybe :boolean]]], :hostname [:map [:hostname [:maybe :boolean]]], :ipv4-prefix [:map [:ipv4-prefix [:maybe :boolean]]], :uri-ref [:map [:uri-ref [:maybe :boolean]]], :ipv6-prefix [:map [:ipv6-prefix [:maybe :boolean]]], :tuuid [:map [:tuuid [:maybe :boolean]]], :ipv6 [:map [:ipv6 [:maybe :boolean]]], :ipv4-with-prefixlen [:map [:ipv4-with-prefixlen [:maybe :boolean]]], :ipv4 [:map [:ipv4 [:maybe :boolean]]], :well-known-regex [:map [:well-known-regex [:maybe :buf/known-regex]]], :uri [:map [:uri [:maybe :boolean]]], :ipv6-with-prefixlen [:map [:ipv6-with-prefixlen [:maybe :boolean]]], :uuid [:map [:uuid [:maybe :boolean]]], :ip-prefix [:map [:ip-prefix [:maybe :boolean]]]}]]])

(def field-path-element "Schema for field-path-element" [:map [:field-number [:maybe :int]] [:field-name [:maybe :string]] [:field-type [:maybe :google/type]] [:key-type [:maybe :google/type]] [:value-type [:maybe :google/type]] [:subscript [:oneof {:index [:map [:index [:maybe :int]]], :bool-key [:map [:bool-key [:maybe :boolean]]], :int-key [:map [:int-key [:maybe :int]]], :uint-key [:map [:uint-key [:maybe :int]]], :string-key [:map [:string-key [:maybe :string]]]}]]])

(def violations "Schema for violations" [:map [:violations [:vector :buf/violation]]])

(def duration-rules "Schema for duration-rules" [:map [:const [:maybe :google/duration]] [:in [:vector :google/duration]] [:not-in [:vector :google/duration]] [:example [:vector :google/duration]] [:less-than [:oneof {:lt [:map [:lt [:maybe :google/duration]]], :lte [:map [:lte [:maybe :google/duration]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :google/duration]]], :gte [:map [:gte [:maybe :google/duration]]]}]]])

(def bytes-rules "Schema for bytes-rules" [:map [:const [:maybe :bytes]] [:len [:maybe :int]] [:min-len [:maybe :int]] [:max-len [:maybe :int]] [:pattern [:maybe :string]] [:prefix [:maybe :bytes]] [:suffix [:maybe :bytes]] [:contains [:maybe :bytes]] [:in [:vector :bytes]] [:not-in [:vector :bytes]] [:example [:vector :bytes]] [:well-known [:oneof {:ip [:map [:ip [:maybe :boolean]]], :ipv4 [:map [:ipv4 [:maybe :boolean]]], :ipv6 [:map [:ipv6 [:maybe :boolean]]]}]]])

(def map-rules "Schema for map-rules" [:map [:min-pairs [:maybe :int]] [:max-pairs [:maybe :int]] [:keys [:maybe :buf/field-rules]] [:values [:maybe :buf/field-rules]]])

(def violation "Schema for violation" [:map [:field [:maybe :buf/field-path]] [:rule [:maybe :buf/field-path]] [:rule-id [:maybe :string]] [:message [:maybe :string]] [:for-key [:maybe :boolean]]])