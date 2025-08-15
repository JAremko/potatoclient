(ns state-explorer.core-test
  (:require [clojure.test :refer :all]
            [state-explorer.test-harness :as harness]
            [state-explorer.pronto-handler :as proto]))

(deftest proto-classes-available
  (testing "Proto classes are compiled and available"
    (is (harness/proto-classes-compiled?) "Proto classes should be compiled")
    (is (harness/pronto-classes-available?) "Pronto classes should be available")))

(deftest pronto-conversion
  (testing "Can load proto classes"
    ;; Just test that we can load the main proto class
    (is (class? (Class/forName "ser.JonSharedData")) "JonSharedData class should be loadable")
    (is (class? (Class/forName "ser.JonSharedDataGps")) "JonSharedDataGps class should be loadable"))
  
  (testing "Pronto handler can parse messages"
    ;; Create a simple test with empty message
    (let [empty-msg (.toByteArray (.build (ser.JonSharedData$JonGUIState/newBuilder)))
          parsed (proto/parse-state-message empty-msg)]
      (is (not (nil? parsed)) "Should parse empty message")
      (when parsed
        (let [edn (proto/proto->edn parsed)]
          (is (map? edn) "Should convert to EDN map")
          (is (every? keyword? (keys edn)) "All keys should be keywords"))))))