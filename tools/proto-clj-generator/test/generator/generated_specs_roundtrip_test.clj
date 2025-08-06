(ns generator.generated-specs-roundtrip-test
  "Roundtrip test using the generated Malli specs from proto files"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.proto.cmd :as cmd]
            [potatoclient.proto.ser :as ser]
            [potatoclient.proto.cmd.daycamera :as daycamera]
            [potatoclient.proto.cmd.heatcamera :as heatcamera]
            [potatoclient.proto.cmd.system :as system])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [cmd.System JonSharedCmdSystem$Root]
           [cmd.DayCamera JonSharedCmdDayCamera$Root]
           [ser JonSharedData JonSharedData$JonGUIState]))

(defn approx=
  "Check if two doubles are approximately equal"
  ([a b] (approx= a b 1e-6))
  ([a b epsilon]
   (< (Math/abs (- (double a) (double b))) epsilon)))

(deftest basic-message-roundtrip
  (testing "Simple message roundtrip - Ping"
    (let [original {:ping {}}
          proto (cmd/build-ping original)
          roundtrip (cmd/parse-ping proto)]
      (is (= original roundtrip))))
  
  (testing "Simple message roundtrip - Noop"
    (let [original {:noop {}}
          proto (cmd/build-noop original)
          roundtrip (cmd/parse-noop proto)]
      (is (= {} roundtrip)))))

(deftest message-with-fields-roundtrip
  (testing "Message with scalar fields - DayCamera SetValue"
    (let [original {:value 42.5}
          proto (daycamera/build-set-value original)
          roundtrip (daycamera/parse-set-value proto)]
      (is (approx= (:value original) (:value roundtrip) 1e-5))))
  
  (testing "Message with enum field - DayCamera SetFxMode"
    (let [original {:mode :jon-gui-data-fx-mode-day-a}
          proto (daycamera/build-set-fx-mode original)
          roundtrip (daycamera/parse-set-fx-mode proto)]
      (is (= original roundtrip)))))

(deftest message-with-oneof-roundtrip
  (testing "Message with oneof - System Root"
    (let [original {:start-rec {}}
          proto (system/build-root original)
          roundtrip (system/parse-root proto)]
      (is (= original roundtrip)))
    
    (let [original {:stop-rec {}}
          proto (system/build-root original)
          roundtrip (system/parse-root proto)]
      (is (= original roundtrip)))))

(deftest spec-validation-roundtrip
  (testing "Validate against generated Malli spec before roundtrip"
    (let [valid-data {:value 123.45}]
      (is (m/validate daycamera/set-value-spec valid-data)
          "Data should be valid according to spec")
      
      (let [proto (daycamera/build-set-value valid-data)
            roundtrip (daycamera/parse-set-value proto)]
        ;; Use approx= for float comparison
        (is (approx= (:value valid-data) (:value roundtrip) 1e-5))
        (is (m/validate daycamera/set-value-spec roundtrip)
            "Roundtrip data should still be valid"))))
  
  (testing "Invalid data should fail spec validation"
    (let [invalid-data {:value "not-a-number"}]
      (is (not (m/validate daycamera/set-value-spec invalid-data))
          "Invalid data should fail spec validation"))))

(deftest nested-message-roundtrip
  (testing "Nested message with oneof - DayCamera Root with Focus command"
    (let [original {:focus {:set-value {:value 1.5}}}
          proto (daycamera/build-root original)
          roundtrip (daycamera/parse-root proto)]
      (is (approx= (get-in original [:focus :set-value :value])
                   (get-in roundtrip [:focus :set-value :value]) 1e-5))))
  
  (testing "Nested message with different oneof choice"
    (let [original {:focus {:move {:target-value 2.0 :speed 0.5}}}
          proto (daycamera/build-root original)
          roundtrip (daycamera/parse-root proto)]
      (is (approx= (get-in original [:focus :move :target-value])
                   (get-in roundtrip [:focus :move :target-value]) 1e-5))
      (is (approx= (get-in original [:focus :move :speed])
                   (get-in roundtrip [:focus :move :speed]) 1e-5)))))

(deftest cmd-root-roundtrip
  (testing "Top-level command Root message"
    (let [original {:protocol-version 1
                   :session-id 12345
                   :important true
                   :from-cv-subsystem false
                   :client-type :jon-gui-data-client-type-local-network
                   :ping {}}
          proto (cmd/build-root original)
          roundtrip (cmd/parse-root proto)]
      (is (= original roundtrip))
      (is (m/validate cmd/root-spec roundtrip)
          "Roundtrip data should validate against spec")))
  
  (testing "Command Root with nested payload"
    (let [original {:protocol-version 1
                   :session-id 54321
                   :client-type :jon-gui-data-client-type-internal-cv
                   :day-camera {:focus {:set-value {:value 3.14}}}}
          proto (cmd/build-root original)
          roundtrip (cmd/parse-root proto)]
      ;; Check non-float fields
      (is (= (:protocol-version original) (:protocol-version roundtrip)))
      (is (= (:session-id original) (:session-id roundtrip)))
      (is (= (:client-type original) (:client-type roundtrip)))
      ;; Check float field with approx=
      (is (approx= (get-in original [:day-camera :focus :set-value :value])
                   (get-in roundtrip [:day-camera :focus :set-value :value]) 1e-5)))))

(deftest spec-generation-test
  (testing "Can generate valid data from specs"
    (let [gen (mg/generator daycamera/set-value-spec)
          sample (mg/generate gen)]
      (is (m/validate daycamera/set-value-spec sample)
          "Generated data should be valid")
      
      ;; Test roundtrip with generated data
      (let [proto (daycamera/build-set-value sample)
            roundtrip (daycamera/parse-set-value proto)]
        (is (approx= (:value sample) (:value roundtrip) 1e-5)
            "Generated data should roundtrip correctly"))))
  
  (testing "Generate and test multiple samples"
    (let [gen (mg/generator daycamera/move-spec)
          samples (mg/sample gen 10)]
      (doseq [sample samples]
        (is (m/validate daycamera/move-spec sample))
        (let [proto (daycamera/build-move sample)
              roundtrip (daycamera/parse-move proto)]
          ;; Compare float fields with approx=
          (is (approx= (:target-value sample) (:target-value roundtrip) 1e-5))
          (is (approx= (:speed sample) (:speed roundtrip) 1e-5)))))))

(deftest enum-roundtrip
  (testing "Enum values roundtrip correctly"
    (let [original {:mode :jon-gui-data-fx-mode-day-a}
          proto (daycamera/build-set-fx-mode original)
          roundtrip (daycamera/parse-set-fx-mode proto)]
      (is (= original roundtrip)))
    
    (let [original {:mode :jon-gui-data-fx-mode-day-b}
          proto (daycamera/build-set-fx-mode original)
          roundtrip (daycamera/parse-set-fx-mode proto)]
      (is (= original roundtrip)))))

(deftest state-message-roundtrip
  (testing "State message - JonGUIState"
    (let [original {:protocol-version 1
                   :frame-time 1000000
                   :time {}}
          proto (ser/build-jon-gui-state original)
          roundtrip (ser/parse-jon-gui-state proto)]
      ;; Note: Some fields might have defaults
      (is (= (:protocol-version original) (:protocol-version roundtrip)))
      (is (= (:frame-time original) (:frame-time roundtrip))))))