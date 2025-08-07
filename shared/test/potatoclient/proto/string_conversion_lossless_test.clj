(ns potatoclient.proto.string-conversion-lossless-test
  "Tests for truly lossless string conversions"
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [potatoclient.proto.string-conversion-lossless :as lossless]
            [potatoclient.proto.string-conversion-specs :as specs]
            [malli.generator :as mg]
            [clojure.string :as str]))

;; =============================================================================
;; Format Detection Tests
;; =============================================================================

(deftest format-detection-test
  (testing "Format detection works correctly"
    (are [s expected] (= expected (#'lossless/detect-format s))
      "XMLParser" :PascalCase
      "XmlParser" :PascalCase
      "DayCamera" :PascalCase
      
      "xmlParser" :camelCase
      "dayCamera" :camelCase
      "getValue" :camelCase
      
      "xml_parser" :snake_case
      "day_camera" :snake_case
      "get_value" :snake_case
      
      "xml-parser" :kebab-case
      "day-camera" :kebab-case
      "get-value" :kebab-case
      
      "XML_PARSER" :UPPER_SNAKE_CASE
      "DAY_CAMERA" :UPPER_SNAKE_CASE
      "TYPE_INT32" :UPPER_SNAKE_CASE
      
      ;; Ambiguous cases
      "value" :snake_case  ; Could be snake_case or kebab-case
      "XML" :UPPER_SNAKE_CASE
      "x" :snake_case)))

;; =============================================================================
;; Lossless Encoding Tests
;; =============================================================================

(deftest lossless-encoding-test
  (testing "Basic lossless encoding preserves format"
    (are [s expected] (= expected (lossless/string->lossless-keyword s))
      "XMLParser" :pascal/xmlparser
      "xmlParser" :camel/xml-parser
      "xml_parser" :snake/xml-parser
      "xml-parser" :kebab/xml-parser
      "XML_PARSER" :proto/xml-parser)))

(deftest lossless-decoding-test
  (testing "Lossless decoding recovers original format"
    (are [k expected] (= expected (lossless/lossless-keyword->string k))
      :pascal/xml-parser "XmlParser"
      :camel/xml-parser "xmlParser"
      :snake/xml-parser "xml_parser"
      :kebab/xml-parser "xml-parser"
      :proto/xml-parser "XML_PARSER")))

;; =============================================================================
;; Exact Encoding Tests
;; =============================================================================

(deftest exact-encoding-test
  (testing "Exact encoding preserves everything"
    (let [test-cases ["XMLParser" "XmlParser" "HTTPSConnection" "Mode2D" "mode2d"
                      "TYPE_INT_32" "TYPE_INT32" "_leadingUnderscore" "trailing_"]]
      (doseq [s test-cases]
        (testing (str "Exact encoding of: " s)
          (let [encoded (lossless/string->exact-keyword s)
                decoded (lossless/exact-keyword->string encoded)]
            (is (= :exact (namespace encoded)))
            (is (= s decoded))))))))

(deftest needs-exact-encoding-test
  (testing "Detection of strings needing exact encoding"
    (are [s needs?] (= needs? (lossless/needs-exact-encoding? s))
      "XMLParser" true   ; Consecutive capitals
      "HTTPSConnection" true
      "IOError" true
      
      "XmlParser" false  ; Normal PascalCase
      "xmlParser" false  ; Normal camelCase
      
      "Mode2D" true      ; Number + capital
      "mode2d" false     ; Number + lowercase
      
      "TYPE_INT_32" true ; Could be ambiguous
      "TYPE_INT32" false ; Standard pattern)))

;; =============================================================================
;; Smart Lossless Conversion Tests
;; =============================================================================

(deftest smart-lossless-roundtrip-test
  (testing "Smart lossless conversions always roundtrip"
    (let [test-cases ["XMLParser" "XmlParser" "xmlParser" "xml_parser" 
                      "xml-parser" "XML_PARSER" "Mode2D" "mode2d"
                      "TYPE_INT32" "TYPE_INT_32" "IOError" "getValue"
                      "DayCamera" "day_camera" "DAY_CAMERA"]]
      (doseq [s test-cases]
        (testing (str "Roundtrip: " s)
          (let [encoded (lossless/string->smart-lossless-keyword s)
                decoded (lossless/smart-lossless-keyword->string encoded)]
            (is (= s decoded)
                (str s " -> " encoded " -> " decoded))))))))

;; =============================================================================
;; Property Tests for Losslessness
;; =============================================================================

(defspec all-pascal-case-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/PascalCaseString)]
    (let [encoded (lossless/string->smart-lossless-keyword s)
          decoded (lossless/smart-lossless-keyword->string encoded)]
      (= s decoded))))

(defspec all-camel-case-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/CamelCaseString)]
    (let [encoded (lossless/string->smart-lossless-keyword s)
          decoded (lossless/smart-lossless-keyword->string encoded)]
      (= s decoded))))

(defspec all-snake-case-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/SnakeCaseString)]
    (let [encoded (lossless/string->smart-lossless-keyword s)
          decoded (lossless/smart-lossless-keyword->string encoded)]
      (= s decoded))))

(defspec all-proto-constant-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/ProtoConstantString)]
    (let [encoded (lossless/string->smart-lossless-keyword s)
          decoded (lossless/smart-lossless-keyword->string encoded)]
      (= s decoded))))

(defspec all-kebab-case-lossless 10000
  (prop/for-all [s (mg/generator :potatoclient.proto.string-conversion-specs/KebabCaseString)]
    (let [encoded (lossless/string->smart-lossless-keyword s)
          decoded (lossless/smart-lossless-keyword->string encoded)]
      (= s decoded))))

;; =============================================================================
;; Cross-format Conversion Tests
;; =============================================================================

(deftest cross-format-lossless-conversion
  (testing "Converting between any formats is lossless"
    (let [test-pairs [["XMLParser" :PascalCase :camelCase]
                      ["xmlParser" :camelCase :UPPER_SNAKE_CASE]
                      ["TYPE_INT32" :UPPER_SNAKE_CASE :kebab-case]
                      ["day-camera" :kebab-case :PascalCase]
                      ["mode_2d" :snake_case :camelCase]]]
      (doseq [[original from-format to-format] test-pairs]
        (testing (str original " from " from-format " to " to-format " and back")
          (let [converted (lossless/convert-lossless original from-format to-format)
                back (lossless/convert-lossless converted to-format from-format)]
            (is (= original back)
                (str original " -> " converted " -> " back))))))))

;; =============================================================================
;; Metadata-based Conversion Tests
;; =============================================================================

(deftest metadata-conversion-test
  (testing "Metadata preserves original string exactly"
    (let [test-cases ["XMLParser" "Mode2D" "TYPE_INT_32" "_weird__case___"]]
      (doseq [s test-cases]
        (let [k (lossless/string->metadata-keyword s)
              back (lossless/metadata-keyword->string k)]
          (is (= s back))
          (is (= s (:original-string (meta k))))
          (is (= (#'lossless/detect-format s) (:original-format (meta k)))))))))

;; =============================================================================
;; Example Validation
;; =============================================================================

(deftest validate-documented-examples
  (testing "All documented examples work correctly"
    (lossless/validate-lossless-examples)))

;; =============================================================================
;; Edge Cases
;; =============================================================================

(deftest edge-case-handling
  (testing "Edge cases are handled correctly"
    (are [s encoded] (= s (lossless/smart-lossless-keyword->string 
                           (lossless/string->smart-lossless-keyword s)))
      ;; Empty components
      "_" 
      "__"
      "a__b"
      
      ;; Leading/trailing
      "_leading"
      "trailing_"
      "-leading"
      "trailing-"
      
      ;; Numbers
      "123"
      "a123"
      "123a"
      "a123b"
      
      ;; Mixed weird cases  
      "HTML5Parser"
      "parseHTMLToXML"
      "TYPE__INT___32")))

;; =============================================================================
;; Performance Comparison
;; =============================================================================

(deftest ^:performance encoding-performance
  (testing "Performance of different encoding strategies"
    (let [test-string "XMLParserFactoryConfiguration"
          iterations 100000]
      
      (println "\nPerformance comparison for:" test-string)
      
      ;; Format-preserving encoding
      (let [start (System/nanoTime)]
        (dotimes [_ iterations]
          (lossless/string->lossless-keyword test-string))
        (println "Format-preserving:" (/ (- (System/nanoTime) start) 1e6) "ms"))
      
      ;; Exact encoding
      (let [start (System/nanoTime)]
        (dotimes [_ iterations]
          (lossless/string->exact-keyword test-string))
        (println "Exact encoding:" (/ (- (System/nanoTime) start) 1e6) "ms"))
      
      ;; Smart encoding
      (let [start (System/nanoTime)]
        (dotimes [_ iterations]
          (lossless/string->smart-lossless-keyword test-string))
        (println "Smart encoding:" (/ (- (System/nanoTime) start) 1e6) "ms")))))

(defn run-lossless-tests []
  (println "\n=== Running Lossless String Conversion Tests ===")
  (run-tests 'potatoclient.proto.string-conversion-lossless-test))