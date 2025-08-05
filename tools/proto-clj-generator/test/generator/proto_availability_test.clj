(ns generator.proto-availability-test
  "Test to check if protobuf Java classes are available"
  (:require [clojure.test :refer :all]))

(deftest check-proto-classes
  (testing "Protobuf Java classes availability"
    (testing "Can import cmd.JonSharedCmd$Root"
      (is (try
            (Class/forName "cmd.JonSharedCmd$Root")
            true
            (catch ClassNotFoundException e
              false))
          "cmd.JonSharedCmd$Root class should be available. You may need to compile the protobuf files first."))
    
    (testing "Can import ser.JonSharedData$JonGUIState"
      (is (try
            (Class/forName "ser.JonSharedData$JonGUIState")
            true
            (catch ClassNotFoundException e
              false))
          "ser.JonSharedData$JonGUIState class should be available. You may need to compile the protobuf files first."))))