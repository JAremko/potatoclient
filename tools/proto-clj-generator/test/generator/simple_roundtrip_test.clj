(ns generator.simple-roundtrip-test
  "Simple roundtrip test without conditional loading"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.proto.cmd :as cmd-gen]
            [potatoclient.proto.ser :as state-gen]
            [potatoclient.specs.malli-oneof :as oneof])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [ser JonSharedData JonSharedData$JonGUIState]))

;; Simple schemas for testing
(def ping-command-schema
  [:map {:closed true}
   [:ping [:map {:closed true}]]])

(def simple-state-schema
  [:map {:closed true}
   [:protocol-version {:optional true} :int]
   [:system {:optional true} :string]])

(deftest ping-command-roundtrip
  (testing "Simple ping command roundtrip"
    (let [original {:ping {}}
          proto (cmd-gen/build-root original)
          binary (.toByteArray proto)
          parsed (JonSharedCmd$Root/parseFrom binary)
          roundtripped (cmd-gen/parse-root parsed)]
      
      (is (instance? JonSharedCmd$Root proto))
      (is (.equals proto parsed) "Protos should be equal")
      ;; The roundtripped data includes default values
      (is (= (:ping roundtripped) {}) "Ping field should roundtrip"))))

(deftest command-with-fields-roundtrip
  (testing "Command with metadata fields"
    (let [original {:protocol-version 42
                    :session-id 123
                    :important true
                    :ping {}}
          proto (cmd-gen/build-root original)
          binary (.toByteArray proto)
          parsed (JonSharedCmd$Root/parseFrom binary)
          roundtripped (cmd-gen/parse-root parsed)]
      
      (is (= 42 (:protocol-version roundtripped)))
      (is (= 123 (:session-id roundtripped)))
      (is (= true (:important roundtripped)))
      (is (= {} (:ping roundtripped))))))

(deftest state-roundtrip
  (testing "Simple state roundtrip"
    (let [original {:protocol-version 1}
          proto (state-gen/build-jon-gui-state original)
          binary (.toByteArray proto)
          parsed (JonSharedData$JonGUIState/parseFrom binary)
          roundtripped (state-gen/parse-jon-gui-state parsed)]
      
      (is (instance? JonSharedData$JonGUIState proto))
      (is (.equals proto parsed) "Protos should be equal")
      (is (= 1 (:protocol-version roundtripped))))))