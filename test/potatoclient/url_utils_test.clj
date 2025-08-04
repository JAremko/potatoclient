(ns potatoclient.url-utils-test
  "Tests for URL validation and extraction utilities"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.url-utils :as url-utils]))

(deftest test-valid-domain-or-ip?
  (testing "Valid domains"
    (is (url-utils/valid-domain-or-ip? "example.com"))
    (is (url-utils/valid-domain-or-ip? "sub.example.com"))
    (is (url-utils/valid-domain-or-ip? "sub.sub.example.com"))
    (is (url-utils/valid-domain-or-ip? "a.b"))
    (is (url-utils/valid-domain-or-ip? "localhost"))
    (is (url-utils/valid-domain-or-ip? "test-domain.com"))
    (is (url-utils/valid-domain-or-ip? "123.example.com")))
  
  (testing "Valid IPv4 addresses"
    (is (url-utils/valid-domain-or-ip? "192.168.1.1"))
    (is (url-utils/valid-domain-or-ip? "10.0.0.1"))
    (is (url-utils/valid-domain-or-ip? "127.0.0.1"))
    (is (url-utils/valid-domain-or-ip? "255.255.255.255"))
    (is (url-utils/valid-domain-or-ip? "0.0.0.0")))
  
  (testing "Valid IPv6 addresses"
    (is (url-utils/valid-domain-or-ip? "2001:db8::1"))
    (is (url-utils/valid-domain-or-ip? "::1"))
    (is (url-utils/valid-domain-or-ip? "fe80::1"))
    (is (url-utils/valid-domain-or-ip? "2001:0db8:0000:0000:0000:0000:0000:0001")))
  
  (testing "Invalid domains"
    (is (not (url-utils/valid-domain-or-ip? "")))
    (is (not (url-utils/valid-domain-or-ip? " ")))
    (is (not (url-utils/valid-domain-or-ip? "-example.com")))
    (is (not (url-utils/valid-domain-or-ip? "example.com-")))
    (is (not (url-utils/valid-domain-or-ip? "example..com")))
    (is (not (url-utils/valid-domain-or-ip? ".example.com")))
    (is (not (url-utils/valid-domain-or-ip? "example.com.")))
    (is (not (url-utils/valid-domain-or-ip? "exam ple.com")))
    (is (not (url-utils/valid-domain-or-ip? "example,com"))))
  
  (testing "Invalid IPv4 addresses"
    (is (not (url-utils/valid-domain-or-ip? "256.1.1.1")))
    (is (not (url-utils/valid-domain-or-ip? "192.168.1")))
    (is (not (url-utils/valid-domain-or-ip? "192.168.1.1.1")))
    (is (not (url-utils/valid-domain-or-ip? "192.168.-1.1")))
    (is (not (url-utils/valid-domain-or-ip? "192.168.1.256")))))

(deftest test-extract-domain
  (testing "Plain domains"
    (is (= "example.com" (url-utils/extract-domain "example.com")))
    (is (= "sub.example.com" (url-utils/extract-domain "sub.example.com")))
    (is (= "localhost" (url-utils/extract-domain "localhost"))))
  
  (testing "IP addresses"
    (is (= "192.168.1.1" (url-utils/extract-domain "192.168.1.1")))
    (is (= "127.0.0.1" (url-utils/extract-domain "127.0.0.1"))))
  
  (testing "URLs with protocols"
    (is (= "example.com" (url-utils/extract-domain "http://example.com")))
    (is (= "example.com" (url-utils/extract-domain "https://example.com")))
    (is (= "example.com" (url-utils/extract-domain "ws://example.com")))
    (is (= "example.com" (url-utils/extract-domain "wss://example.com"))))
  
  (testing "URLs with ports"
    (is (= "example.com" (url-utils/extract-domain "http://example.com:8080")))
    (is (= "example.com" (url-utils/extract-domain "https://example.com:443")))
    (is (= "192.168.1.1" (url-utils/extract-domain "http://192.168.1.1:8080"))))
  
  (testing "URLs with paths"
    (is (= "example.com" (url-utils/extract-domain "http://example.com/path")))
    (is (= "example.com" (url-utils/extract-domain "https://example.com/path/to/resource")))
    (is (= "example.com" (url-utils/extract-domain "wss://example.com/ws"))))
  
  (testing "URLs with query parameters"
    (is (= "example.com" (url-utils/extract-domain "http://example.com?param=value")))
    (is (= "example.com" (url-utils/extract-domain "https://example.com/path?foo=bar&baz=qux"))))
  
  (testing "URLs with fragments"
    (is (= "example.com" (url-utils/extract-domain "http://example.com#section")))
    (is (= "example.com" (url-utils/extract-domain "https://example.com/path#fragment"))))
  
  (testing "Complex URLs"
    (is (= "example.com" (url-utils/extract-domain "https://example.com:8080/path?query=value#fragment")))
    (is (= "sub.example.com" (url-utils/extract-domain "wss://sub.example.com:9000/ws/endpoint?token=abc123"))))
  
  (testing "Edge cases"
    (is (= "example.com" (url-utils/extract-domain "   example.com   ")))
    (is (= "example.com" (url-utils/extract-domain "example.com/")))
    (is (nil? (url-utils/extract-domain "")))
    (is (nil? (url-utils/extract-domain "   ")))
    (is (nil? (url-utils/extract-domain "not a domain!")))))

(deftest test-validate-url-input
  (testing "Valid inputs"
    (let [result (url-utils/validate-url-input "example.com")]
      (is (:valid result))
      (is (= "example.com" (:domain result))))
    
    (let [result (url-utils/validate-url-input "https://example.com:8080/path")]
      (is (:valid result))
      (is (= "example.com" (:domain result))))
    
    (let [result (url-utils/validate-url-input "192.168.1.1")]
      (is (:valid result))
      (is (= "192.168.1.1" (:domain result))))
    
    (let [result (url-utils/validate-url-input "wss://localhost/ws")]
      (is (:valid result))
      (is (= "localhost" (:domain result)))))
  
  (testing "Invalid inputs"
    (let [result (url-utils/validate-url-input "")]
      (is (not (:valid result)))
      (is (string? (:error result))))
    
    (let [result (url-utils/validate-url-input "   ")]
      (is (not (:valid result)))
      (is (string? (:error result))))
    
    (let [result (url-utils/validate-url-input "not a valid domain!")]
      (is (not (:valid result)))
      (is (string? (:error result))))
    
    (let [result (url-utils/validate-url-input "http://")]
      (is (not (:valid result)))
      (is (string? (:error result))))
    
    (let [result (url-utils/validate-url-input "256.256.256.256")]
      (is (not (:valid result)))
      (is (string? (:error result)))))
  
  (testing "Real-world examples"
    ;; Common user inputs
    (is (:valid (url-utils/validate-url-input "sych.local")))
    (is (:valid (url-utils/validate-url-input "https://sych.local")))
    (is (:valid (url-utils/validate-url-input "https://sych.local/?loc=ua&palette=main")))
    (is (:valid (url-utils/validate-url-input "wss://example.com:8080/ws")))
    (is (:valid (url-utils/validate-url-input "192.168.0.100")))
    (is (:valid (url-utils/validate-url-input "10.0.0.1")))
    
    ;; Common mistakes
    (is (not (:valid (url-utils/validate-url-input "example com"))))
    (is (not (:valid (url-utils/validate-url-input "http://"))))
    (is (not (:valid (url-utils/validate-url-input "://example.com"))))))

(deftest test-get-example-formats
  (testing "Returns vector of example strings"
    (let [examples (url-utils/get-example-formats)]
      (is (vector? examples))
      (is (every? string? examples))
      (is (> (count examples) 0)))))

;; Run this test to verify localization is working
(deftest test-localized-error-messages
  (testing "Error messages are localized strings"
    (let [empty-result (url-utils/validate-url-input "")
          invalid-result (url-utils/validate-url-input "!!!")]
      (is (string? (:error empty-result)))
      (is (string? (:error invalid-result)))
      (is (not= (:error empty-result) (:error invalid-result))))))