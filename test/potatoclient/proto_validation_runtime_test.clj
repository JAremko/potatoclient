(ns potatoclient.proto-validation-runtime-test
  "Tests for runtime buf.validate validation in dev/test modes"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.proto :as proto]
            [potatoclient.runtime :as runtime])
  (:import (build.buf.protovalidate Validator ValidatorFactory)))

(deftest test-validation-initialization
  (testing "Validator initialization in non-release builds"
    (when-not (runtime/release-build?)
      (testing "Validator can be created"
        (is (some? (.build (ValidatorFactory/newBuilder)))
            "Should be able to create a Validator instance"))

      (testing "Proto validation is available"
        (is (resolve 'build.buf.protovalidate.Validator)
            "Validator class should be available")))))

(deftest test-validation-behavior
  (testing "Validation behavior based on build mode"
    (let [release-mode? (runtime/release-build?)]
      (testing (str "Current mode: " (if release-mode? "RELEASE" "DEV/TEST"))
        (is (boolean? release-mode?)
            "Should be able to detect build mode")))))

(deftest test-proto-serialization-with-validation
  (testing "Proto serialization works with validation system"
    (testing "Valid command serializes successfully"
      (let [cmd {:protocol-version 1  ; Valid: >= 1
                 :session-id 12345
                 :important false
                 :from-cv-subsystem false
                 :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
                 :ping {}}
            serialized (proto/serialize-cmd cmd)]
        (is (bytes? serialized)
            "Should serialize valid command")))

    (testing "Command with constraint-violating values"
      ;; With buf.validate-enabled bindings, validation is enforced
      (let [cmd {:protocol-version 0  ; Violates: must be >= 1
                 :session-id 12345
                 :important false
                 :from-cv-subsystem false
                 :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
                 :ping {}}]
        (is (thrown? Exception
                     (proto/serialize-cmd cmd))
            "Buf.validate rejects constraint-violating values")

        ;; Check exception structure
        (try
          (proto/serialize-cmd cmd)
          (is false "Should have thrown an exception")
          (catch Exception e
            ;; The :violations key is in the nested cause's data
            (let [inner-cause (some-> e ex-data :cause)
                  inner-data (when (instance? clojure.lang.ExceptionInfo inner-cause)
                               (ex-data inner-cause))]
              (is (contains? inner-data :violations)
                  "Validation error should include violations in cause data")
              (is (seq (:violations inner-data))
                  "Should have at least one violation"))))))))

(deftest test-validation-performance
  (testing "Validation has minimal performance impact"
    (let [cmd {:protocol-version 1
               :session-id 12345
               :important false
               :from-cv-subsystem false
               :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
               :ping {}}
          iterations 1000]

      (testing "Serialization performance"
        (let [start (System/nanoTime)]
          (dotimes [_ iterations]
            (proto/serialize-cmd cmd))
          (let [elapsed (/ (- (System/nanoTime) start) 1000000.0)]
            (is (< elapsed 100)  ; Should complete 1000 iterations in < 100ms
                (str "1000 serializations took " elapsed "ms"))
            (println "Serialization performance:"
                     (format "%.3f ms for %d iterations (%.3f μs/op)"
                             elapsed iterations (/ (* elapsed 1000) iterations)))))))))