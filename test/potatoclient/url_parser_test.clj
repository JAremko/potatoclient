(ns potatoclient.url-parser-test
  "Tests for Instaparse-based URL parser"
  (:require [clojure.test :refer [deftest testing is]]
            [instaparse.core :as insta]
            [potatoclient.url-parser :as parser]))

(deftest test-parse-url
  (testing "Basic parsing"
    (is (not (insta/failure? (parser/parse-url "example.com"))))
    (is (not (insta/failure? (parser/parse-url "192.168.1.1"))))
    (is (not (insta/failure? (parser/parse-url "http://example.com"))))
    (is (not (insta/failure? (parser/parse-url "wss://example.com:8080/path")))))

  (testing "Parse failures"
    (is (insta/failure? (parser/parse-url "not a valid domain!")))
    (is (insta/failure? (parser/parse-url "http://")))
    (is (insta/failure? (parser/parse-url "://example.com")))))

(deftest test-extract-domain
  (testing "Plain domains"
    (is (= "example.com" (parser/extract-domain "example.com")))
    (is (= "sub.example.com" (parser/extract-domain "sub.example.com")))
    (is (= "sub.sub.example.com" (parser/extract-domain "sub.sub.example.com")))
    (is (= "localhost" (parser/extract-domain "localhost")))
    (is (= "test-domain.com" (parser/extract-domain "test-domain.com")))
    (is (= "123.example.com" (parser/extract-domain "123.example.com"))))

  (testing "IPv4 addresses"
    (is (= "192.168.1.1" (parser/extract-domain "192.168.1.1")))
    (is (= "10.0.0.1" (parser/extract-domain "10.0.0.1")))
    (is (= "127.0.0.1" (parser/extract-domain "127.0.0.1")))
    (is (= "255.255.255.255" (parser/extract-domain "255.255.255.255")))
    (is (= "0.0.0.0" (parser/extract-domain "0.0.0.0"))))

  (testing "Invalid IPv4 addresses"
    (is (nil? (parser/extract-domain "256.1.1.1")))
    (is (nil? (parser/extract-domain "192.168.1.256")))
    (is (nil? (parser/extract-domain "192.168.1")))
    (is (nil? (parser/extract-domain "192.168.1.1.1"))))

  (testing "IPv6 addresses"
    (is (= "2001:db8::1" (parser/extract-domain "[2001:db8::1]")))
    (is (= "::1" (parser/extract-domain "[::1]")))
    (is (= "fe80::1" (parser/extract-domain "[fe80::1]"))))

  (testing "URLs with protocols"
    (is (= "example.com" (parser/extract-domain "http://example.com")))
    (is (= "example.com" (parser/extract-domain "https://example.com")))
    (is (= "example.com" (parser/extract-domain "ws://example.com")))
    (is (= "example.com" (parser/extract-domain "wss://example.com"))))

  (testing "URLs with ports"
    (is (= "example.com" (parser/extract-domain "http://example.com:8080")))
    (is (= "example.com" (parser/extract-domain "https://example.com:443")))
    (is (= "192.168.1.1" (parser/extract-domain "http://192.168.1.1:8080")))
    (is (= "localhost" (parser/extract-domain "localhost:3000"))))

  (testing "URLs with paths"
    (is (= "example.com" (parser/extract-domain "http://example.com/path")))
    (is (= "example.com" (parser/extract-domain "https://example.com/path/to/resource")))
    (is (= "example.com" (parser/extract-domain "wss://example.com/ws"))))

  (testing "URLs with query parameters"
    (is (= "example.com" (parser/extract-domain "http://example.com?param=value")))
    (is (= "example.com" (parser/extract-domain "https://example.com/path?foo=bar&baz=qux"))))

  (testing "URLs with fragments"
    (is (= "example.com" (parser/extract-domain "http://example.com#section")))
    (is (= "example.com" (parser/extract-domain "https://example.com/path#fragment"))))

  (testing "Complex URLs"
    (is (= "example.com" (parser/extract-domain "https://example.com:8080/path?query=value#fragment")))
    (is (= "sub.example.com" (parser/extract-domain "wss://sub.example.com:9000/ws/endpoint?token=abc123"))))

  (testing "Edge cases"
    (is (= "example.com" (parser/extract-domain "   example.com   ")))
    (is (= "example.com" (parser/extract-domain "example.com/")))
    (is (nil? (parser/extract-domain "")))
    (is (nil? (parser/extract-domain "   ")))
    (is (nil? (parser/extract-domain "not a valid domain!")))
    (is (nil? (parser/extract-domain "example com")))
    (is (nil? (parser/extract-domain "http://")))
    (is (nil? (parser/extract-domain "://example.com")))))

(deftest test-validate-url-input
  (testing "Valid inputs"
    (let [result (parser/validate-url-input "example.com")]
      (is (:valid result))
      (is (= "example.com" (:domain result))))

    (let [result (parser/validate-url-input "https://example.com:8080/path")]
      (is (:valid result))
      (is (= "example.com" (:domain result))))

    (let [result (parser/validate-url-input "192.168.1.1")]
      (is (:valid result))
      (is (= "192.168.1.1" (:domain result))))

    (let [result (parser/validate-url-input "wss://localhost/ws")]
      (is (:valid result))
      (is (= "localhost" (:domain result)))))

  (testing "Invalid inputs"
    (let [result (parser/validate-url-input "")]
      (is (not (:valid result)))
      (is (string? (:error result))))

    (let [result (parser/validate-url-input "   ")]
      (is (not (:valid result)))
      (is (string? (:error result))))

    (let [result (parser/validate-url-input "not a valid domain!")]
      (is (not (:valid result)))
      (is (string? (:error result))))

    (let [result (parser/validate-url-input "http://")]
      (is (not (:valid result)))
      (is (string? (:error result))))

    (let [result (parser/validate-url-input "256.256.256.256")]
      (is (not (:valid result)))
      (is (string? (:error result)))))

  (testing "Real-world examples"
    ;; Common user inputs
    (is (:valid (parser/validate-url-input "sych.local")))
    (is (:valid (parser/validate-url-input "https://sych.local")))
    (is (:valid (parser/validate-url-input "https://sych.local/?loc=ua&palette=main")))
    (is (:valid (parser/validate-url-input "wss://example.com:8080/ws")))
    (is (:valid (parser/validate-url-input "192.168.0.100")))
    (is (:valid (parser/validate-url-input "10.0.0.1")))

    ;; Common mistakes that should fail
    (is (not (:valid (parser/validate-url-input "example com"))))
    (is (not (:valid (parser/validate-url-input "http://"))))
    (is (not (:valid (parser/validate-url-input "://example.com"))))))

(deftest test-parse-tree-inspection
  (testing "Inspect parse tree structure"
    (is (= :URL (parser/parse-tree-node-type "https://example.com:8080/path?query=value#fragment")))
    (is (= :IPV4 (parser/parse-tree-node-type "192.168.1.1")))
    (is (= :DOMAIN (parser/parse-tree-node-type "example.com")))))

(deftest test-ipv6-parsing
  (testing "IPv6 with brackets in URLs"
    (is (= "2001:db8::1" (parser/extract-domain "http://[2001:db8::1]:8080")))
    (is (= "::1" (parser/extract-domain "wss://[::1]/ws")))
    (is (= "fe80::1" (parser/extract-domain "https://[fe80::1]:443/path"))))

  (testing "Plain IPv6 addresses"
    (is (= "2001:db8::1" (parser/extract-domain "2001:db8::1")))
    (is (= "::1" (parser/extract-domain "::1")))
    (is (= "fe80::1" (parser/extract-domain "fe80::1")))))

(deftest test-special-cases
  (testing "Domains starting with numbers"
    (is (= "123.com" (parser/extract-domain "123.com")))
    (is (= "9gag.com" (parser/extract-domain "9gag.com"))))

  (testing "Single-letter domains"
    (is (= "x.com" (parser/extract-domain "x.com")))
    (is (= "a.b.c" (parser/extract-domain "a.b.c"))))

  (testing "Domains with hyphens"
    (is (= "my-domain.com" (parser/extract-domain "my-domain.com")))
    (is (= "test-123.example.com" (parser/extract-domain "test-123.example.com"))))

  (testing "URLs with authentication (should extract domain only)"
    ;; Note: Our grammar doesn't handle auth, so these will fail parsing
    ;; This is intentional - we don't want to accept URLs with credentials
    (is (nil? (parser/extract-domain "http://user:pass@example.com")))
    (is (nil? (parser/extract-domain "https://user@example.com")))))