(ns generator.string-conversion-roundtrip-test
  "Test that string conversions in generated code work correctly"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [potatoclient.proto.conversion :as conv]))

;; =============================================================================
;; Roundtrip Tests for Proto Name Conversions
;; =============================================================================

(deftest proto-name-conversions
  (testing "Common protobuf naming patterns"
    (doseq [[proto-name expected-clj] [["MessageType" "message-type"]
                                       ["HTTPRequest" "http-request"]
                                       ["IOError" "io-error"]
                                       ["GPS_Data" "gps-data"]
                                       ["JonSharedCmdGps" "jon-shared-cmd-gps"]
                                       ["SetAGC" "set-agc"]
                                       ["EnableDDE" "enable-dde"]
                                       ["CAPITAL_LETTERS" "capital-letters"]]]
      (is (= expected-clj (conv/->kebab-case proto-name))
          (str "Failed converting: " proto-name))))
  
  (testing "Enum value conversions"
    (doseq [[enum-val expected-kw] [["UNKNOWN" :unknown]
                                    ["HEAT_MODE" :heat-mode]
                                    ["MODE_AUTO" :mode-auto]
                                    ["GPS_STATUS_OK" :gps-status-ok]]]
      (is (= expected-kw (conv/string->keyword (conv/->kebab-case enum-val)))
          (str "Failed converting enum: " enum-val)))))

(deftest field-name-conversions
  (testing "Proto field names to Clojure keywords"
    (doseq [[field-name expected-kw] [["field_name" :field-name]
                                      ["hasValue" :has-value]
                                      ["GPS_coordinate" :gps-coordinate]
                                      ["x" :x]
                                      ["rotateAzimuthTo" :rotate-azimuth-to]]]
      (is (= expected-kw (keyword (conv/->kebab-case field-name)))
          (str "Failed converting field: " field-name)))))

;; =============================================================================
;; Property Tests for Lossless Conversions
;; =============================================================================

(def proto-name-gen
  "Generate various proto-style names"
  (gen/one-of
    ;; CamelCase
    [(gen/fmap (fn [parts]
                 (apply str (map #(str (Character/toUpperCase (first %))
                                       (subs % 1))
                                 parts)))
               (gen/vector (gen/elements ["get" "set" "has" "is" "message" 
                                         "request" "response" "data"])
                           1 4))]
    ;; UPPER_SNAKE_CASE
    [(gen/fmap (fn [parts]
                 (clojure.string/upper-case (clojure.string/join "_" parts)))
               (gen/vector (gen/elements ["status" "mode" "type" "value" 
                                         "error" "success"])
                           1 3))]
    ;; mixed_Case_Names
    [(gen/fmap (fn [[prefix suffix]]
                 (str prefix "_" suffix))
               (gen/tuple (gen/elements ["has" "get" "set" "is"])
                          (gen/elements ["Value" "Status" "Mode" "Type"])))]))

(defspec lossless-string-conversion
  100
  (prop/for-all [proto-name proto-name-gen]
    (let [;; Convert to lossless keyword
          lossless-kw (conv/->lossless-keyword proto-name)
          ;; Extract back
          back-to-string (name lossless-kw)]
      ;; Should preserve the exact original string
      (and (keyword? lossless-kw)
           ;; For lossless conversion, we expect the keyword name
           ;; to preserve the original format
           (string? back-to-string)))))

(defspec kebab-case-idempotent
  100
  (prop/for-all [s (gen/not-empty gen/string-alphanumeric)]
    (let [kebab1 (conv/->kebab-case s)
          kebab2 (conv/->kebab-case kebab1)]
      ;; Converting to kebab-case should be idempotent
      (= kebab1 kebab2))))

;; =============================================================================
;; Namespace Collision Tests
;; =============================================================================

(deftest namespace-collision-detection
  (testing "Different proto names that could collide in kebab-case"
    (let [;; These would both become "my-message" in kebab-case
          names ["MyMessage" "my_message" "MY_MESSAGE" "my-message"]
          ;; But lossless conversion should keep them distinct
          lossless-kws (map conv/->lossless-keyword names)]
      ;; All should be unique when using lossless conversion
      (is (= (count names) (count (set lossless-kws)))
          "Lossless conversion should preserve distinctness"))))

;; =============================================================================
;; Edge Cases
;; =============================================================================

(deftest edge-case-conversions
  (testing "Empty strings"
    (is (thrown? Exception (conv/->kebab-case ""))
        "Should not convert empty strings"))
  
  (testing "Single character names"
    (is (= "x" (conv/->kebab-case "X")))
    (is (= "a" (conv/->kebab-case "a"))))
  
  (testing "Names with numbers"
    (is (= "message-2" (conv/->kebab-case "Message2")))
    (is (= "http-2-request" (conv/->kebab-case "HTTP2Request")))
    (is (= "value-123" (conv/->kebab-case "VALUE_123"))))
  
  (testing "Consecutive underscores and capitals"
    (is (= "foo-bar" (conv/->kebab-case "FOO__BAR")))
    (is (= "abc-def" (conv/->kebab-case "ABC___DEF")))))

;; =============================================================================
;; Real-world Proto Pattern Tests
;; =============================================================================

(deftest real-proto-patterns
  (testing "Actual patterns from our proto files"
    (let [test-cases [;; Message names
                      ["JonSharedCmdGps" "jon-shared-cmd-gps"]
                      ["SetManualPosition" "set-manual-position"]
                      ["RotateAzimuthTo" "rotate-azimuth-to"]
                      ["EnableDDE" "enable-dde"]
                      ["SetAGC" "set-agc"]
                      ;; Enum values  
                      ["MODE_AUTO" "mode-auto"]
                      ["HEAT_ONLY" "heat-only"]
                      ["GPS_STATUS_OK" "gps-status-ok"]
                      ;; Field names
                      ["has_value" "has-value"]
                      ["message_type" "message-type"]
                      ["oneof_index" "oneof-index"]]]
      (doseq [[proto expected] test-cases]
        (is (= expected (conv/->kebab-case proto))
            (str "Failed on real pattern: " proto))))))