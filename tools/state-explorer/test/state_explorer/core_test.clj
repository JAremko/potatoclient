(ns state-explorer.core-test
  (:require [clojure.test :refer :all]
            [state-explorer.test-harness :as harness]
            [state-explorer.proto-to-edn :as proto-to-edn]))

(deftest proto-classes-available
  (testing "Proto classes are compiled and available"
    (is (harness/proto-classes-compiled?) "Proto classes should be compiled")
    (is (harness/pronto-classes-available?) "Pronto classes should be available")))

(deftest proto-to-edn-conversion
  (testing "Can load proto classes"
    ;; Just test that we can load the main proto class
    (is (class? (Class/forName "ser.JonSharedData")) "JonSharedData class should be loadable")
    (is (class? (Class/forName "ser.JonSharedDataGps")) "JonSharedDataGps class should be loadable")))