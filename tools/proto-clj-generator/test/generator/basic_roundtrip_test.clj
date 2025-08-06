(ns generator.basic-roundtrip-test
  "Basic roundtrip tests without Malli validation"
  (:require [clojure.test :refer :all]
            [potatoclient.proto.cmd :as cmd]
            [potatoclient.proto.ser :as ser]
            [potatoclient.proto.cmd.daycamera :as daycamera]
            [potatoclient.proto.cmd.heatcamera :as heatcamera]
            [potatoclient.proto.cmd.system :as system])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [cmd.System JonSharedCmdSystem$Root]
           [cmd.DayCamera JonSharedCmdDayCamera$Root JonSharedCmdDayCamera$SetValue]
           [ser JonSharedData JonSharedData$JonGUIState]))

(deftest empty-message-roundtrip
  (testing "Empty messages roundtrip correctly"
    ;; Ping is an empty message
    (let [original {}
          proto (cmd/build-ping original)
          roundtrip (cmd/parse-ping proto)]
      (is (= {} roundtrip) "Empty message should roundtrip as empty map"))
    
    ;; Noop is also empty
    (let [original {}
          proto (cmd/build-noop original)
          roundtrip (cmd/parse-noop proto)]
      (is (= {} roundtrip)))))

(defn approx=
  "Check if two doubles are approximately equal"
  ([a b] (approx= a b 1e-6))
  ([a b epsilon]
   (< (Math/abs (- (double a) (double b))) epsilon)))

(deftest scalar-field-roundtrip
  (testing "Scalar fields roundtrip correctly"
    ;; Double field
    (let [original {:value 42.5}
          proto (daycamera/build-set-value original)
          roundtrip (daycamera/parse-set-value proto)]
      (is (approx= (:value original) (:value roundtrip))))
    
    ;; Test with very small double
    (let [original {:value 0.0000001}
          proto (daycamera/build-set-value original)
          roundtrip (daycamera/parse-set-value proto)]
      (is (approx= (:value original) (:value roundtrip))))
    
    ;; Test with negative double (note: protobuf uses float, not double)
    (let [original {:value -123.456}
          proto (daycamera/build-set-value original)
          roundtrip (daycamera/parse-set-value proto)]
      ;; Use approx= for floating point comparison with larger epsilon for float precision
      (is (approx= (:value original) (:value roundtrip) 1e-5) 
          "Float precision requires larger epsilon"))))

(deftest multiple-field-roundtrip
  (testing "Messages with multiple fields"
    (let [original {:target-value 10.0 :speed 2.5}
          proto (daycamera/build-move original)
          roundtrip (daycamera/parse-move proto)]
      (is (approx= (:target-value original) (:target-value roundtrip)))
      (is (approx= (:speed original) (:speed roundtrip))))))

(deftest optional-field-roundtrip
  (testing "Optional fields are preserved when present"
    (let [original {:target-value 5.0 :speed 1.0}
          proto (daycamera/build-move original)
          roundtrip (daycamera/parse-move proto)]
      (is (approx= (:target-value original) (:target-value roundtrip)))
      (is (approx= (:speed original) (:speed roundtrip)))))
  
  (testing "Missing optional fields get default values"
    ;; Only set target-value, not speed
    (let [original {:target-value 5.0}
          proto (daycamera/build-move original)
          ;; Note: protobuf returns default values for missing fields
          roundtrip (daycamera/parse-move proto)]
      ;; Check that target-value is preserved
      (is (approx= (:target-value original) (:target-value roundtrip)))
      ;; speed will be 0.0 (default) since it wasn't set
      (is (= 0.0 (:speed roundtrip))))))

(deftest oneof-roundtrip
  (testing "Oneof fields roundtrip correctly"
    ;; System command with start-rec
    (let [original {:start-rec {}}
          proto (system/build-root original)
          roundtrip (system/parse-root proto)]
      (is (= original roundtrip)))
    
    ;; System command with stop-rec
    (let [original {:stop-rec {}}
          proto (system/build-root original)
          roundtrip (system/parse-root proto)]
      (is (= original roundtrip)))
    
    ;; Only one field of oneof should be set
    (let [proto (system/build-root {:start-rec {}})
          roundtrip (system/parse-root proto)]
      (is (contains? roundtrip :start-rec))
      (is (not (contains? roundtrip :stop-rec))))))

(deftest nested-message-roundtrip
  (testing "Nested messages roundtrip"
    ;; DayCamera with nested focus command
    (let [original {:focus {:set-value {:value 1.5}}}
          proto (daycamera/build-root original)
          roundtrip (daycamera/parse-root proto)]
      (is (approx= (get-in original [:focus :set-value :value])
                   (get-in roundtrip [:focus :set-value :value]))))
    
    ;; Deeper nesting
    (let [original {:zoom {:move {:target-value 2.0 :speed 0.5}}}
          proto (daycamera/build-root original)
          roundtrip (daycamera/parse-root proto)]
      (is (approx= (get-in original [:zoom :move :target-value])
                   (get-in roundtrip [:zoom :move :target-value])))
      (is (approx= (get-in original [:zoom :move :speed])
                   (get-in roundtrip [:zoom :move :speed]))))))

(deftest enum-field-roundtrip
  (testing "Enum fields using keywords"
    ;; Need to check what enum values are actually available
    ;; This test will need the actual enum keyword
    (let [original {:mode :jon-gui-data-fx-mode-day-a}
          proto (daycamera/build-set-fx-mode original)
          roundtrip (daycamera/parse-set-fx-mode proto)]
      (is (= original roundtrip)))))

(deftest protobuf-default-values
  (testing "Protobuf default values behavior"
    ;; When we don't set optional scalar fields, protobuf returns default values
    (let [original {}
          proto (daycamera/build-move original)
          roundtrip (daycamera/parse-move proto)]
      ;; Protobuf will return 0.0 for unset double fields
      (is (= 0.0 (:target-value roundtrip)))
      (is (= 0.0 (:speed roundtrip)))))
  
  (testing "Boolean default is false"
    (let [original {}
          proto (cmd/build-root original)
          roundtrip (cmd/parse-root proto)]
      ;; These boolean fields should default to false
      (is (false? (:important roundtrip)))
      (is (false? (:from-cv-subsystem roundtrip))))))

(deftest complete-cmd-root-roundtrip
  (testing "Complete command root message"
    (let [original {:protocol-version 1
                   :session-id 12345
                   :important true
                   :from-cv-subsystem false
                   :client-type :jon-gui-data-client-type-local-network
                   :ping {}}
          proto (cmd/build-root original)
          roundtrip (cmd/parse-root proto)]
      (is (= original roundtrip))))
  
  (testing "Command root with different payload"
    (let [original {:protocol-version 2
                   :session-id 54321
                   :client-type :jon-gui-data-client-type-internal-cv
                   :noop {}}
          proto (cmd/build-root original)
          roundtrip (cmd/parse-root proto)]
      ;; Protobuf adds default values for unset fields
      (is (= (:protocol-version original) (:protocol-version roundtrip)))
      (is (= (:session-id original) (:session-id roundtrip)))
      (is (= (:client-type original) (:client-type roundtrip)))
      (is (= (:noop original) (:noop roundtrip)))
      ;; Check that defaults were added
      (is (false? (:important roundtrip)))
      (is (false? (:from-cv-subsystem roundtrip))))))