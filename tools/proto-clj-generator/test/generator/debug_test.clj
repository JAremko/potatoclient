(ns generator.debug-test
  "Debug test to understand proto behavior"
  (:require [clojure.test :refer :all]
            [potatoclient.proto.cmd :as cmd-gen])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]))

(deftest debug-ping-command
  (testing "Debug ping command"
    (let [original {:ping {}}
          proto (cmd-gen/build-root original)]
      
      (println "Proto class:" (class proto))
      (println "Payload case:" (.getPayloadCase proto))
      (println "Has ping:" (.hasPing proto))
      (println "Ping value:" (.getPing proto))
      
      (is (instance? JonSharedCmd$Root proto)))))