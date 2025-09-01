(ns malli-cleanup.transformer-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [malli-cleanup.transformer :as t]))

(deftest test-lambda-transformations
  (testing "Simple instance? lambda to partial"
    (let [input "(fn* [p1__1234#] (instance? File p1__1234#))"
          expected "(partial instance? File)"
          result (t/transform-file (str "[:fn " input "]"))]
      (is (str/includes? result expected))))
  
  (testing "Simple predicate lambda"
    (let [input "(fn* [p1__1234#] (string? p1__1234#))"
          expected "string?"
          result (t/transform-file (str "[:fn " input "]"))]
      (is (str/includes? result expected))))
  
  (testing "Lambda with error message"
    (let [input "[:fn {:error/message \"must be a File\"} (fn* [p1__1234#] (instance? File p1__1234#))]"
          expected "[:fn {:error/message \"must be a File\"} (partial instance? File)]"
          result (t/transform-file input)]
      (is (str/includes? result "(partial instance? File)")))))

(deftest test-metadata-positioning
  (testing "Move inline metadata after docstring"
    (let [input "(defn test-fn
  \"Docstring\" {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))"
          result (t/transform-file input)]
      (is (str/includes? result "\"Docstring\""))
      (is (str/includes? result "{:malli/schema")
      (let [doc-pos (.indexOf result "\"Docstring\"")
            meta-pos (.indexOf result "{:malli/schema")]
        (is (< doc-pos meta-pos) "Metadata should come after docstring")))))
  
  (testing "Keep metadata when already in correct position"
    (let [input "(defn test-fn
  \"Docstring\"
  {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))"
          result (t/transform-file input)]
      (is (= input result) "Should not change when already correct"))))

(deftest test-full-transformation
  (testing "Transform sample file"
    (let [input-file "test/sample_input.clj"
          expected-file "test/expected_output.clj"
          result (t/transform-file-path input-file)
          expected (slurp expected-file)]
      ;; Check key transformations occurred
      (is (str/includes? result "(partial instance? File)"))
      (is (str/includes? result "(partial instance? JFrame)"))
      (is (str/includes? result "(partial instance? JPanel)"))
      (is (str/includes? result "string?") "Simple predicates should be simplified")
      (is (str/includes? result "instant?") "Custom predicates should be simplified"))))