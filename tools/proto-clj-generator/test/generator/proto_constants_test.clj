(ns generator.proto-constants-test
  "Property-based tests for proto constant conversion to ensure lossless bijection."
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [potatoclient.proto.constants :as pc]
            [clojure.string :as str]))

;; =============================================================================
;; Generators
;; =============================================================================

(def proto-const-segment-gen
  "Generate a segment of a proto constant (e.g., TYPE, INT32, OPTIONAL)"
  (gen/fmap (fn [s] (str/upper-case s))
            (gen/such-that #(and (seq %)
                                 (Character/isLetter (first %)))
                           gen/string-alphanumeric)))

(def proto-const-gen
  "Generate valid protobuf constant strings"
  (gen/fmap (fn [segments]
              (str/join "_" segments))
            (gen/vector proto-const-segment-gen 1 5)))

(def proto-keyword-gen
  "Generate keywords that could come from proto constants"
  (gen/fmap (fn [const]
              (pc/proto-const->keyword const))
            proto-const-gen))

;; Special cases that are common in protobuf
(def common-proto-constants
  ["TYPE_INT32" "TYPE_INT64" "TYPE_UINT32" "TYPE_UINT64"
   "TYPE_SINT32" "TYPE_SINT64" "TYPE_FIXED32" "TYPE_FIXED64"
   "TYPE_SFIXED32" "TYPE_SFIXED64" "TYPE_BOOL" "TYPE_STRING"
   "TYPE_BYTES" "TYPE_DOUBLE" "TYPE_FLOAT" "TYPE_ENUM" "TYPE_MESSAGE"
   "LABEL_OPTIONAL" "LABEL_REQUIRED" "LABEL_REPEATED"
   "TARGET_TYPE_UNKNOWN" "TARGET_TYPE_FILE" "TARGET_TYPE_EXTENSION_RANGE"
   "RETENTION_UNKNOWN" "RETENTION_RUNTIME" "RETENTION_SOURCE"])

(def common-proto-const-gen
  "Generator that includes common protobuf constants"
  (gen/frequency [[7 proto-const-gen]
                  [3 (gen/elements common-proto-constants)]]))

;; =============================================================================
;; Property Tests
;; =============================================================================

(defspec proto-const-keyword-bijection 10000
  ;; Test that conversion is bijective (lossless)
  (prop/for-all [const common-proto-const-gen]
    (and (pc/valid-proto-const? const)
         (let [kw (pc/proto-const->keyword const)
               back (pc/keyword->proto-const kw)]
           (and (keyword? kw)
                (= const back))))))

(defspec keyword-proto-const-bijection 10000
  ;; Test reverse direction
  (prop/for-all [kw proto-keyword-gen]
    (when kw  ; proto-keyword-gen might generate nil
      (let [const (pc/keyword->proto-const kw)
            back (pc/proto-const->keyword const)]
        (and (string? const)
             (pc/valid-proto-const? const)
             (= kw back))))))

(defspec handles-edge-cases 1000
  ;; Test edge cases with numbers and special patterns
  (prop/for-all [segments (gen/vector 
                           (gen/one-of [(gen/return "TYPE")
                                        (gen/return "LABEL")
                                        (gen/return "INT")
                                        (gen/return "32")
                                        (gen/return "64")
                                        (gen/return "UINT")
                                        (gen/return "SINT")
                                        (gen/return "S")
                                        (gen/return "3")
                                        (gen/return "KEY")])
                           1 4)]
    (let [const (str/join "_" segments)]
      (if (pc/valid-proto-const? const)
        (let [kw (pc/proto-const->keyword const)
              back (pc/keyword->proto-const kw)]
          (= const back))
        true))))  ; Skip invalid constants

(defspec preserves-number-boundaries 10000
  ;; Specifically test that TYPE_INT32 doesn't become TYPE_INT_32
  (prop/for-all [prefix (gen/elements ["TYPE" "LABEL" "TARGET" "FIELD"])
                 suffix (gen/elements ["32" "64" "128" "256" "1" "2" "3D" "2D"])]
    (let [const (str prefix "_INT" suffix)
          kw (pc/proto-const->keyword const)
          back (pc/keyword->proto-const kw)]
      (and (= (str ":" (str/lower-case prefix) "-int" (str/lower-case suffix)) 
              (str kw))
           (= const back)))))

;; =============================================================================
;; Unit Tests for Specific Cases
;; =============================================================================

(deftest specific-conversions-test
  (testing "Common protobuf constants convert correctly"
    (are [const expected-kw]
         (= expected-kw (pc/proto-const->keyword const))
      "TYPE_INT32"         :type-int32
      "TYPE_INT_32"        :type-int-32  ; Different!
      "TYPE_SINT64"        :type-sint64
      "LABEL_OPTIONAL"     :label-optional
      "TARGET_TYPE_FILE"   :target-type-file
      "S3_KEY"             :s3-key
      "TYPE_3D"            :type-3d
      "INT32"              :int32))
  
  (testing "Keywords convert back correctly"
    (are [kw expected-const]
         (= expected-const (pc/keyword->proto-const kw))
      :type-int32          "TYPE_INT32"
      :type-int-32         "TYPE_INT_32"  ; Different!
      :label-optional      "LABEL_OPTIONAL"
      :s3-key              "S3_KEY"
      :type-3d             "TYPE_3D")))

(deftest validation-test
  (testing "Valid proto constant detection"
    (are [s expected]
         (= expected (pc/valid-proto-const? s))
      "TYPE_INT32"     true
      "LABEL_OPTIONAL" true
      "A"              true
      "A123_B456"      true
      "type_int32"     false  ; lowercase
      "Type_Int32"     false  ; mixed case
      "123TYPE"        false  ; starts with number
      ""               false
      nil              false
      :keyword         false))
  
  (testing "Valid proto keyword detection"
    (are [k expected]
         (= expected (pc/valid-proto-keyword? k))
      :type-int32      true
      :label-optional  true
      :a               true
      :a123-b456       true
      :TYPE-INT32      false  ; uppercase
      :Type-Int32      false  ; mixed case
      :-type           false  ; starts with hyphen
      :123type         false  ; starts with number
      ""               false
      nil              false
      "string"         false)))

(deftest convert-value-test
  (testing "Convert value handles mixed data"
    (are [v expected]
         (= expected (pc/convert-value v))
      "TYPE_INT32"     :type-int32
      "LABEL_OPTIONAL" :label-optional
      :already-kw      :already-kw
      "regular string" "regular string"
      123              123
      nil              nil
      true             true)))

;; =============================================================================
;; Regression Tests for Known Issues
;; =============================================================================

(deftest camel-snake-kebab-comparison-test
  (testing "Our conversion differs from camel-snake-kebab for numbers"
    ;; camel-snake-kebab would produce :type-int-32 for TYPE_INT32
    ;; Our conversion produces :type-int32 (no extra hyphen)
    (is (= :type-int32 (pc/proto-const->keyword "TYPE_INT32")))
    (is (not= :type-int-32 (pc/proto-const->keyword "TYPE_INT32")))
    
    ;; But TYPE_INT_32 correctly produces :type-int-32
    (is (= :type-int-32 (pc/proto-const->keyword "TYPE_INT_32")))
    
    ;; And both are reversible
    (is (= "TYPE_INT32" (pc/keyword->proto-const :type-int32)))
    (is (= "TYPE_INT_32" (pc/keyword->proto-const :type-int-32)))))