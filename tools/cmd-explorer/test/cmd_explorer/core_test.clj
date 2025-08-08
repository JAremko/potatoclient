(ns cmd-explorer.core-test
  (:require [clojure.test :refer :all]
            [cmd-explorer.test-harness :as harness]))

(deftest proto-classes-available
  (testing "Proto classes are compiled and available"
    (is (harness/proto-classes-compiled?) "Proto classes should be compiled")
    (is (harness/pronto-classes-available?) "Pronto classes should be available")))

(deftest cmd-proto-loading
  (testing "Can load command proto classes"
    ;; Test that we can load the main command proto class
    (is (class? (Class/forName "cmd.JonSharedCmd")) "JonSharedCmd class should be loadable")
    (is (class? (Class/forName "cmd.Compass.JonSharedCmdCompass")) "JonSharedCmdCompass class should be loadable")))