(ns potatoclient.pronto-simple-test
  "Simple test to verify pronto works with our protobuf classes"
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io])
  (:import [cmd JonSharedCmd]
           [cmd.RotaryPlatform JonSharedCmdRotary JonSharedCmdRotary$Halt 
            JonSharedCmdRotary$GotoAzEl JonSharedCmdRotary$Root JonSharedCmdRotary$Root$RootCase]
           [ser JonSharedData JonSharedDataGps$GetGpsNavData 
            JonSharedDataCompass$GetCompassData]))

(deftest protobuf-classes-exist-test
  (testing "Generated protobuf classes are available"
    (is (class? JonSharedCmd))
    (is (class? JonSharedCmdRotary$Halt))
    (is (class? JonSharedCmdRotary$GotoAzEl))
    (is (class? JonSharedDataGps$GetGpsNavData))
    (is (class? JonSharedDataCompass$GetCompassData))))

(deftest protobuf-basic-creation-test
  (testing "Can create protobuf instances using builders"
    (let [;; Create a GPS data message using the builder pattern
          gps-builder (JonSharedDataGps$GetGpsNavData/newBuilder)
          gps-data (-> gps-builder
                      (.setLatitude 37.7749)
                      (.setLongitude -122.4194)
                      (.setAltitude 52.0)
                      (.build))]
      (is (instance? JonSharedDataGps$GetGpsNavData gps-data))
      (is (= 37.7749 (.getLatitude gps-data)))
      (is (= -122.4194 (.getLongitude gps-data)))
      (is (= 52.0 (.getAltitude gps-data))))))

(deftest protobuf-serialization-test
  (testing "Can serialize and deserialize protobuf messages"
    (let [;; Create a compass data message
          compass-data (-> (JonSharedDataCompass$GetCompassData/newBuilder)
                          (.setAzimuth 180.0)
                          (.setPitch 15.0)
                          (.setRoll -5.0)
                          (.build))
          ;; Serialize to bytes
          bytes (.toByteArray compass-data)
          ;; Deserialize back
          restored (JonSharedDataCompass$GetCompassData/parseFrom bytes)]
      
      (testing "Serialization produces bytes"
        (is (bytes? bytes))
        (is (> (alength bytes) 0)))
      
      (testing "Deserialization restores values"
        (is (= 180.0 (.getAzimuth restored)))
        (is (= 15.0 (.getPitch restored)))
        (is (= -5.0 (.getRoll restored)))))))

(deftest rotary-command-creation-test
  (testing "Can create rotary platform commands"
    (let [;; Create a Halt command
          halt-cmd (.build (JonSharedCmdRotary$Halt/newBuilder))
          ;; Create a GotoAzEl command
          goto-cmd (-> (JonSharedCmdRotary$GotoAzEl/newBuilder)
                      (.setAz 45.0)
                      (.setEl 30.0)
                      (.build))]
      
      (testing "Halt command creation"
        (is (instance? JonSharedCmdRotary$Halt halt-cmd)))
      
      (testing "GotoAzEl command with values"
        (is (instance? JonSharedCmdRotary$GotoAzEl goto-cmd))
        (is (= 45.0 (.getAz goto-cmd)))
        (is (= 30.0 (.getEl goto-cmd)))))))

(deftest nested-message-test
  (testing "Can work with nested/oneof messages"
    (let [;; Create a halt command
          halt-cmd (.build (JonSharedCmdRotary$Halt/newBuilder))
          ;; Create the root message with halt as the oneof choice
          root-msg (-> (JonSharedCmdRotary$Root/newBuilder)
                      (.setHalt halt-cmd)
                      (.build))]
      
      (testing "Root message contains halt"
        (is (instance? JonSharedCmdRotary$Root root-msg))
        (is (.hasHalt root-msg))
        (is (instance? JonSharedCmdRotary$Halt (.getHalt root-msg))))
      
      (testing "Oneof case is set correctly"
        (is (= JonSharedCmdRotary$Root$RootCase/HALT 
               (.getRootCase root-msg)))))))

(deftest descriptor-inspection-test
  (testing "Can inspect protobuf descriptors"
    (let [descriptor (.getDescriptor JonSharedDataGps$GetGpsNavData)
          fields (.getFields descriptor)]
      (is (not (nil? descriptor)))
      (is (> (.size fields) 0))
      ;; Check some expected fields exist
      (let [field-names (map #(.getName %) fields)]
        (is (some #{"latitude"} field-names))
        (is (some #{"longitude"} field-names))
        (is (some #{"altitude"} field-names))))))

;; Run a simple validation
(defn validate-setup []
  (try
    (println "\n=== Protobuf Setup Validation ===")
    (println "✓ JonSharedDataGps available:" (class? JonSharedDataGps))
    (println "✓ JonSharedCmdRotary available:" (class? JonSharedCmdRotary))
    (let [test-msg (-> (JonSharedDataGps$GetGpsNavData/newBuilder)
                       (.setLatitude 1.0)
                       (.build))]
      (println "✓ Can create protobuf instances:" (instance? JonSharedDataGps$GetGpsNavData test-msg))
      (println "✓ Can serialize to bytes:" (> (alength (.toByteArray test-msg)) 0)))
    (println "=================================\n")
    true
    (catch Exception e
      (println "✗ Setup validation failed:" (.getMessage e))
      false)))

;; Run validation
(validate-setup)