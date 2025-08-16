(ns cmd-explorer.cmd-root-test
  "Tests for cmd-root spec"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [cmd-explorer.test-harness] ;; Auto-initializes on load
   [potatoclient.specs.cmd.root :as cmd-root]
   [pronto.core :as p])
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]))

(deftest test-cmd-root-validation
  (testing "Basic command validation"
    
    (testing "Ping command validation"
      (let [ping-cmd (cmd-root/create-ping-command)]
        (is (p/proto-map? ping-cmd))
        (is (instance? JonSharedCmd$Root (.pmap_getProto ping-cmd)))
        (is (= :ping (cmd-root/get-command-type ping-cmd)))
        (is (cmd-root/validate-cmd-root ping-cmd))))
    
    (testing "Noop command validation"
      (let [noop-cmd (cmd-root/create-noop-command)]
        (is (p/proto-map? noop-cmd))
        (is (= :noop (cmd-root/get-command-type noop-cmd)))
        (is (cmd-root/validate-cmd-root noop-cmd))))
    
    (testing "Frozen command validation"
      (let [frozen-cmd (cmd-root/create-frozen-command)]
        (is (p/proto-map? frozen-cmd))
        (is (= :frozen (cmd-root/get-command-type frozen-cmd)))
        (is (cmd-root/validate-cmd-root frozen-cmd))))))

(deftest test-cmd-payload-spec
  (testing "Payload oneof spec validation"
    
    (testing "Validates proto-map with exactly one field"
      (let [cmd (cmd-root/create-ping-command)]
        (is (m/validate cmd-root/cmd-payload-spec cmd))))
    
    (testing "Rejects proto-map with no payload field set"
      (let [empty-cmd (p/proto-map cmd-root/cmd-mapper JonSharedCmd$Root
                                  :protocol_version 1)]
        (is (not (m/validate cmd-root/cmd-payload-spec empty-cmd)))
        (is (nil? (p/which-one-of empty-cmd :payload)))))
    
    (testing "Field type validation"
      ;; Create a command with ping field set
      (let [ping-cmd (cmd-root/create-ping-command)
            ping-value (.getPing ping-cmd)]
        (is (instance? JonSharedCmd$Ping ping-value))))))

(deftest test-command-type-detection
  (testing "Command type detection"
    
    (testing "Detects ping command"
      (is (= :ping (cmd-root/get-command-type 
                    (cmd-root/create-ping-command)))))
    
    (testing "Detects noop command"
      (is (= :noop (cmd-root/get-command-type 
                    (cmd-root/create-noop-command)))))
    
    (testing "Detects frozen command"
      (is (= :frozen (cmd-root/get-command-type 
                      (cmd-root/create-frozen-command)))))
    
    (testing "Returns nil for empty payload"
      (let [empty-cmd (p/proto-map cmd-root/cmd-mapper JonSharedCmd$Root)]
        (is (nil? (cmd-root/get-command-type empty-cmd)))))))

(deftest test-serialization
  (testing "Command serialization round-trip"
    
    (testing "Ping command round-trip"
      (let [original (cmd-root/create-ping-command)
            bytes (p/proto-map->bytes original)
            restored (p/bytes->proto-map cmd-root/cmd-mapper JonSharedCmd$Root bytes)]
        (is (bytes? bytes))
        (is (p/proto-map? restored))
        (is (= (p/which-one-of original :payload)
               (p/which-one-of restored :payload)))
        (is (= 1 (:protocol_version restored)))))
    
    (testing "All command types serialize correctly"
      (doseq [create-fn [cmd-root/create-ping-command
                        cmd-root/create-noop-command
                        cmd-root/create-frozen-command]]
        (let [cmd (create-fn)
              bytes (p/proto-map->bytes cmd)
              restored (p/bytes->proto-map cmd-root/cmd-mapper JonSharedCmd$Root bytes)]
          (is (= (p/which-one-of cmd :payload)
                 (p/which-one-of restored :payload))))))))