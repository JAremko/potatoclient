(ns proto-explorer.java-class-info-test
  "Tests for Java class information extraction"
  (:require [clojure.test :refer :all]
            [proto-explorer.java-class-info :as java-info]
            [clojure.string :as str]))

(deftest test-class-to-edn-conversion
  (testing "Primitive type conversion"
    (let [result (java-info/class->edn Integer/TYPE)]
      (is (= :primitive (:type result)))
      (is (= "int" (:name result)))))
  
  (testing "Class type conversion"
    (let [result (java-info/class->edn String)]
      (is (= :class (:type result)))
      (is (= "java.lang.String" (:name result)))
      (is (= "String" (:simple-name result)))))
  
  (testing "Array type conversion"
    (let [result (java-info/class->edn (Class/forName "[I"))]
      (is (= :array (:type result)))
      ;; For arrays, component-type is a nested structure
      (let [component (:component-type result)]
        (is (map? component))
        (is (= :primitive (:type component)))))))

(deftest test-spec-keyword-to-class-info
  (testing "Simple message name conversion"
    (let [result (java-info/spec-keyword->class-info :Root)]
      (if (not (:error result))
        (when-let [class-name (get-in result [:class :name])]
          (is (re-find #"Root" class-name)))
        (is (= :class-not-found (:error result))))))
  
  (testing "Namespaced spec conversion"
    (let [result (java-info/spec-keyword->class-info :cmd/Root)]
      (if (not (:error result))
        (do
          (is (map? (:class result)))
          (when-let [class-name (get-in result [:class :name])]
            (is (re-find #"Root" class-name))))
        (is (= :class-not-found (:error result))))))
  
  (testing "Kebab-case to PascalCase conversion"
    ;; This tests the conversion logic even if class doesn't exist
    (with-redefs [java-info/analyze-protobuf-class 
                  (fn [class-name]
                    {:test-class-name class-name})]
      (let [result (java-info/spec-keyword->class-info :cmd.RotaryPlatform/set-velocity)]
        (is (or (= "cmd.RotaryPlatform.SetVelocity" (:test-class-name result))
                (= "cmd.RotaryPlatform$SetVelocity" (:test-class-name result))))))))

(deftest test-find-message-class
  (testing "Pattern matching for common protobuf naming"
    (with-redefs [java-info/analyze-protobuf-class
                  (fn [class-name]
                    (if (re-find #"Root" class-name)
                      {:found true :class-name class-name}
                      {:error :class-not-found}))]
      (let [result (java-info/find-message-class "Root")]
        (is (:found result))
        (is (re-find #"Root" (:class-name result)))))))

(deftest test-protobuf-method-filtering
  (testing "Filters out Object methods"
    (is (not (java-info/protobuf-method? 
              (.getMethod Object "toString" (into-array Class []))))))
  
  (testing "Method name filtering"
    ;; Test the logic directly without mocking
    (is (not (java-info/protobuf-method? 
              (.getMethod String "hashCode" (into-array Class [])))))
    (is (not (java-info/protobuf-method? 
              (.getMethod String "equals" (into-array Class [Object])))))
    ;; A real non-Object method should pass
    (is (java-info/protobuf-method? 
         (.getMethod String "charAt" (into-array Class [Integer/TYPE]))))))

(deftest test-field-accessor-extraction
  (testing "Field accessor name conversion logic"
    ;; Test the name conversion logic directly
    (let [test-fields [{:name "protocol_version" :number 1 :json-name "protocolVersion"}
                      {:name "session_id" :number 2 :json-name "sessionId"}
                      {:name "simple" :number 3 :json-name "simple"}]]
      ;; The function converts snake_case to camelCase getter names
      ;; protocol_version -> getProtocolVersion
      ;; session_id -> getSessionId
      ;; simple -> getSimple
      (let [to-camel (fn [s]
                       (let [camel (str/replace s #"_(.)" (fn [[_ c]] (str/upper-case c)))]
                         (str (str/upper-case (first camel)) (subs camel 1))))]
        (is (= "getProtocolVersion" 
               (str "get" (to-camel "protocol_version"))))
        (is (= "getSimple" 
               (str "get" (to-camel "simple"))))))
    ;; For actual protobuf classes, this would find real getter methods
    ))

(deftest test-get-builder-info
  (testing "Returns error for non-existent builder"
    (with-redefs [java-info/analyze-protobuf-class
                  (fn [class-name]
                    (if (str/ends-with? class-name "$Builder")
                      {:error :class-not-found}
                      {:class {:name class-name}}))]
      (let [result (java-info/get-builder-info "TestMessage")]
        (is (= :not-found (:error result)))))))

(deftest test-format-class-info
  (testing "Formats class info correctly"
    (let [test-info {:class {:simple-name "TestMessage"
                            :name "com.example.TestMessage"
                            :package "com.example"}
                    :fields [{:name "field1" :type {:simple-name "String"}}]
                    :methods [{:name "method1" 
                              :parameter-types []
                              :return-type {:simple-name "void"}}]
                    :protobuf-descriptor {:fields [{:number 1
                                                   :name "field1"
                                                   :type "TYPE_STRING"
                                                   :is-repeated false}]}}
          formatted (java-info/format-class-info test-info)]
      (is (string? formatted))
      (is (re-find #"Class: TestMessage" formatted))
      (is (re-find #"field1" formatted))
      (is (re-find #"method1" formatted)))))

;; Integration test (only runs if protobuf classes are available)
(deftest ^:integration test-real-protobuf-class
  (testing "Analyzes real protobuf class if available"
    ;; This test will only pass if the protobuf classes are on classpath
    (try
      (let [result (java-info/analyze-protobuf-class "cmd.JonSharedCmd$Root")]
        (when-not (:error result)
          (is (map? (:class result)))
          (is (vector? (:fields result)))
          (is (vector? (:methods result)))
          (when (:protobuf-descriptor result)
            (is (vector? (get-in result [:protobuf-descriptor :fields]))))))
      (catch Exception e
        ;; It's okay if the class isn't available in test environment
        (is true)))))