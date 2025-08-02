(ns potatoclient.specs.buf.validate "Generated Malli specs from protobuf descriptors" (:require [malli.core :as proto-explorer.spec-generator/m] [malli.generator :as proto-explorer.spec-generator/mg]))

(def Rule "Schema for Rule" [:map [:id [:maybe :string]] [:message [:maybe :string]] [:expression [:maybe :string]]])

(def EnumRules "Schema for EnumRules" [:map [:const [:maybe :int]] [:defined-only [:maybe :boolean]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]]])

(def MessageOneofRule "Schema for MessageOneofRule" [:map [:fields [:vector :string]] [:required [:maybe :boolean]]])

(def MapRules "Schema for MapRules" [:map [:min-pairs [:maybe :int]] [:max-pairs [:maybe :int]] [:keys [:maybe :buf/FieldRules]] [:values [:maybe :buf/FieldRules]]])

(def TimestampRules "Schema for TimestampRules" [:map [:const [:maybe :google/Timestamp]] [:within [:maybe :google/Duration]] [:example [:vector :google/Timestamp]] [:less-than [:oneof {:lt [:map [:lt [:maybe :google/Timestamp]]], :lte [:map [:lte [:maybe :google/Timestamp]]], :lt-now [:map [:lt-now [:maybe :boolean]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :google/Timestamp]]], :gte [:map [:gte [:maybe :google/Timestamp]]], :gt-now [:map [:gt-now [:maybe :boolean]]]}]]])

(def UInt64Rules "Schema for UInt64Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def SInt32Rules "Schema for SInt32Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def DoubleRules "Schema for DoubleRules" [:map [:const [:maybe :double]] [:in [:vector :double]] [:not-in [:vector :double]] [:finite [:maybe :boolean]] [:example [:vector :double]] [:less-than [:oneof {:lt [:map [:lt [:maybe :double]]], :lte [:map [:lte [:maybe :double]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :double]]], :gte [:map [:gte [:maybe :double]]]}]]])

(def FieldPathElement "Schema for FieldPathElement" [:map [:field-number [:maybe :int]] [:field-name [:maybe :string]] [:field-type [:maybe :google/Type]] [:key-type [:maybe :google/Type]] [:value-type [:maybe :google/Type]] [:subscript [:oneof {:index [:map [:index [:maybe :int]]], :bool-key [:map [:bool-key [:maybe :boolean]]], :int-key [:map [:int-key [:maybe :int]]], :uint-key [:map [:uint-key [:maybe :int]]], :string-key [:map [:string-key [:maybe :string]]]}]]])

(def BoolRules "Schema for BoolRules" [:map [:const [:maybe :boolean]] [:example [:vector :boolean]]])

(def Int32Rules "Schema for Int32Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def FieldPath "Schema for FieldPath" [:map [:elements [:vector :buf/FieldPathElement]]])

(def Violations "Schema for Violations" [:map [:violations [:vector :buf/Violation]]])

(def OneofRules "Schema for OneofRules" [:map [:required [:maybe :boolean]]])

(def SFixed64Rules "Schema for SFixed64Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def StringRules "Schema for StringRules" [:map [:const [:maybe :string]] [:len [:maybe :int]] [:min-len [:maybe :int]] [:max-len [:maybe :int]] [:len-bytes [:maybe :int]] [:min-bytes [:maybe :int]] [:max-bytes [:maybe :int]] [:pattern [:maybe :string]] [:prefix [:maybe :string]] [:suffix [:maybe :string]] [:contains [:maybe :string]] [:not-contains [:maybe :string]] [:in [:vector :string]] [:not-in [:vector :string]] [:strict [:maybe :boolean]] [:example [:vector :string]] [:well-known [:oneof {:host-and-port [:map [:host-and-port [:maybe :boolean]]], :ip-with-prefixlen [:map [:ip-with-prefixlen [:maybe :boolean]]], :address [:map [:address [:maybe :boolean]]], :email [:map [:email [:maybe :boolean]]], :ip [:map [:ip [:maybe :boolean]]], :hostname [:map [:hostname [:maybe :boolean]]], :ipv4-prefix [:map [:ipv4-prefix [:maybe :boolean]]], :uri-ref [:map [:uri-ref [:maybe :boolean]]], :ipv6-prefix [:map [:ipv6-prefix [:maybe :boolean]]], :tuuid [:map [:tuuid [:maybe :boolean]]], :ipv6 [:map [:ipv6 [:maybe :boolean]]], :ipv4-with-prefixlen [:map [:ipv4-with-prefixlen [:maybe :boolean]]], :ipv4 [:map [:ipv4 [:maybe :boolean]]], :well-known-regex [:map [:well-known-regex [:maybe :buf/KnownRegex]]], :uri [:map [:uri [:maybe :boolean]]], :ipv6-with-prefixlen [:map [:ipv6-with-prefixlen [:maybe :boolean]]], :uuid [:map [:uuid [:maybe :boolean]]], :ip-prefix [:map [:ip-prefix [:maybe :boolean]]]}]]])

(def UInt32Rules "Schema for UInt32Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def DurationRules "Schema for DurationRules" [:map [:const [:maybe :google/Duration]] [:in [:vector :google/Duration]] [:not-in [:vector :google/Duration]] [:example [:vector :google/Duration]] [:less-than [:oneof {:lt [:map [:lt [:maybe :google/Duration]]], :lte [:map [:lte [:maybe :google/Duration]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :google/Duration]]], :gte [:map [:gte [:maybe :google/Duration]]]}]]])

(def Fixed32Rules "Schema for Fixed32Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def BytesRules "Schema for BytesRules" [:map [:const [:maybe :bytes]] [:len [:maybe :int]] [:min-len [:maybe :int]] [:max-len [:maybe :int]] [:pattern [:maybe :string]] [:prefix [:maybe :bytes]] [:suffix [:maybe :bytes]] [:contains [:maybe :bytes]] [:in [:vector :bytes]] [:not-in [:vector :bytes]] [:example [:vector :bytes]] [:well-known [:oneof {:ip [:map [:ip [:maybe :boolean]]], :ipv4 [:map [:ipv4 [:maybe :boolean]]], :ipv6 [:map [:ipv6 [:maybe :boolean]]]}]]])

(def SInt64Rules "Schema for SInt64Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def Violation "Schema for Violation" [:map [:field [:maybe :buf/FieldPath]] [:rule [:maybe :buf/FieldPath]] [:rule-id [:maybe :string]] [:message [:maybe :string]] [:for-key [:maybe :boolean]]])

(def AnyRules "Schema for AnyRules" [:map [:in [:vector :string]] [:not-in [:vector :string]]])

(def SFixed32Rules "Schema for SFixed32Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def MessageRules "Schema for MessageRules" [:map [:cel [:vector :buf/Rule]] [:oneof [:vector :buf/MessageOneofRule]]])

(def FieldRules "Schema for FieldRules" [:map [:cel [:vector :buf/Rule]] [:required [:maybe :boolean]] [:ignore [:maybe :buf/Ignore]] [:type [:oneof {:int32 [:map [:int32 [:maybe :buf/Int32Rules]]], :enum [:map [:enum [:maybe :buf/EnumRules]]], :double [:map [:double [:maybe :buf/DoubleRules]]], :int64 [:map [:int64 [:maybe :buf/Int64Rules]]], :uint64 [:map [:uint64 [:maybe :buf/UInt64Rules]]], :float [:map [:float [:maybe :buf/FloatRules]]], :duration [:map [:duration [:maybe :buf/DurationRules]]], :fixed64 [:map [:fixed64 [:maybe :buf/Fixed64Rules]]], :string [:map [:string [:maybe :buf/StringRules]]], :uint32 [:map [:uint32 [:maybe :buf/UInt32Rules]]], :sfixed64 [:map [:sfixed64 [:maybe :buf/SFixed64Rules]]], :fixed32 [:map [:fixed32 [:maybe :buf/Fixed32Rules]]], :bytes [:map [:bytes [:maybe :buf/BytesRules]]], :sint32 [:map [:sint32 [:maybe :buf/SInt32Rules]]], :sint64 [:map [:sint64 [:maybe :buf/SInt64Rules]]], :bool [:map [:bool [:maybe :buf/BoolRules]]], :timestamp [:map [:timestamp [:maybe :buf/TimestampRules]]], :sfixed32 [:map [:sfixed32 [:maybe :buf/SFixed32Rules]]], :map [:map [:map [:maybe :buf/MapRules]]], :any [:map [:any [:maybe :buf/AnyRules]]], :repeated [:map [:repeated [:maybe :buf/RepeatedRules]]]}]]])

(def Fixed64Rules "Schema for Fixed64Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def PredefinedRules "Schema for PredefinedRules" [:map [:cel [:vector :buf/Rule]]])

(def KnownRegex "Schema for KnownRegex" [:enum :KNOWN-REGEX-UNSPECIFIED :KNOWN-REGEX-HTTP-HEADER-NAME :KNOWN-REGEX-HTTP-HEADER-VALUE])

(def FloatRules "Schema for FloatRules" [:map [:const [:maybe :float]] [:in [:vector :float]] [:not-in [:vector :float]] [:finite [:maybe :boolean]] [:example [:vector :float]] [:less-than [:oneof {:lt [:map [:lt [:maybe :float]]], :lte [:map [:lte [:maybe :float]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :float]]], :gte [:map [:gte [:maybe :float]]]}]]])

(def RepeatedRules "Schema for RepeatedRules" [:map [:min-items [:maybe :int]] [:max-items [:maybe :int]] [:unique [:maybe :boolean]] [:items [:maybe :buf/FieldRules]]])

(def Int64Rules "Schema for Int64Rules" [:map [:const [:maybe :int]] [:in [:vector :int]] [:not-in [:vector :int]] [:example [:vector :int]] [:less-than [:oneof {:lt [:map [:lt [:maybe :int]]], :lte [:map [:lte [:maybe :int]]]}]] [:greater-than [:oneof {:gt [:map [:gt [:maybe :int]]], :gte [:map [:gte [:maybe :int]]]}]]])

(def Ignore "Schema for Ignore" [:enum :IGNORE-UNSPECIFIED :IGNORE-IF-ZERO-VALUE :IGNORE-ALWAYS])