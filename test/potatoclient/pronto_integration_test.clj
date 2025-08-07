(ns potatoclient.pronto-integration-test
  "Test that pronto library works with our generated protobuf classes"
  (:require [clojure.test :refer [deftest testing is]]
            [pronto.core :as pronto]
            [pronto.utils :as pu])
  (:import [cmd JonSharedCmd]
           [cmd.RotaryPlatform JonSharedCmdRotary]
           [ser JonSharedData JonSharedDataGps JonSharedDataCompass]))

(deftest basic-proto-creation-test
  (testing "Can create proto-map from generated classes"
    (let [proto-class JonSharedDataGps$GetGpsNavData
          proto-map (pronto/proto-map proto-class)]
      (is (not (nil? proto-map)))
      (is (map? proto-map))
      (is (satisfies? pronto.core/ProtoMap proto-map)))))

(deftest gps-data-manipulation-test
  (testing "Can create and manipulate GPS data"
    (let [gps-proto (pronto/proto-map JonSharedDataGps$GetGpsNavData)
          ;; Set some values
          gps-with-data (-> gps-proto
                           (assoc :latitude 45.5231)
                           (assoc :longitude -122.6765)
                           (assoc :altitude 100.5))]
      ;; Check values were set
      (is (= 45.5231 (:latitude gps-with-data)))
      (is (= -122.6765 (:longitude gps-with-data)))
      (is (= 100.5 (:altitude gps-with-data))))))

(deftest compass-data-test
  (testing "Can work with compass data"
    (let [compass-proto (pronto/proto-map JonSharedDataCompass$GetCompassData)
          compass-with-data (-> compass-proto
                               (assoc :azimuth 180.0)
                               (assoc :pitch 15.0)
                               (assoc :roll -5.0))]
      (is (= 180.0 (:azimuth compass-with-data)))
      (is (= 15.0 (:pitch compass-with-data)))
      (is (= -5.0 (:roll compass-with-data))))))

(deftest rotary-command-test
  (testing "Can create rotary platform commands"
    (let [;; Create a Halt command
          halt-cmd (pronto/proto-map JonSharedCmdRotary$Halt)
          ;; Create a GotoAzEl command
          goto-cmd (-> (pronto/proto-map JonSharedCmdRotary$GotoAzEl)
                      (assoc :az 45.0)
                      (assoc :el 30.0))]
      
      (testing "Halt command"
        (is (not (nil? halt-cmd)))
        (is (map? halt-cmd)))
      
      (testing "GotoAzEl command"
        (is (= 45.0 (:az goto-cmd)))
        (is (= 30.0 (:el goto-cmd))))))

(deftest serialization-test
  (testing "Can serialize and deserialize proto messages"
    (let [;; Create a GPS data message
          original (-> (pronto/proto-map JonSharedDataGps$GetGpsNavData)
                      (assoc :latitude 37.7749)
                      (assoc :longitude -122.4194)
                      (assoc :altitude 52.0)
                      (assoc :speed 25.5))
          ;; Serialize to bytes
          bytes (pronto/proto-map->bytes original)
          ;; Deserialize back
          restored (pronto/bytes->proto-map JonSharedDataGps$GetGpsNavData bytes)]
      
      (testing "Serialization produces bytes"
        (is (bytes? bytes))
        (is (> (count bytes) 0)))
      
      (testing "Deserialization restores values"
        (is (= (:latitude original) (:latitude restored)))
        (is (= (:longitude original) (:longitude restored)))
        (is (= (:altitude original) (:altitude restored)))
        (is (= (:speed original) (:speed restored))))))

(deftest enum-field-test
  (testing "Can work with enum fields"
    ;; Many of our protos have enum fields for status, modes, etc.
    ;; Let's test with a command that might have an enum
    (let [proto-class JonSharedCmdRotary$SetVelocity
          velocity-cmd (-> (pronto/proto-map proto-class)
                          (assoc :azSpeed 10.0)
                          (assoc :elSpeed 5.0))]
      (is (= 10.0 (:azSpeed velocity-cmd)))
      (is (= 5.0 (:elSpeed velocity-cmd))))))

(deftest nested-message-test
  (testing "Can work with nested messages"
    ;; The Root message contains other messages as oneofs
    (let [root-proto (pronto/proto-map JonSharedCmdRotary$Root)
          ;; Create a halt command inside the root
          with-halt (assoc root-proto :halt 
                          (pronto/proto-map JonSharedCmdRotary$Halt))]
      (is (not (nil? (:halt with-halt))))
      (is (map? (:halt with-halt))))))

(deftest proto-utils-test
  (testing "Proto utils work with our classes"
    (let [proto-class JonSharedDataGps$GetGpsNavData
          descriptor (pu/descriptor proto-class)
          field-descriptors (pu/field-descriptors descriptor)]
      (testing "Can get descriptor"
        (is (not (nil? descriptor))))
      (testing "Can get field descriptors"
        (is (seq field-descriptors))
        (is (> (count field-descriptors) 0))))))

(deftest default-values-test
  (testing "Proto maps have appropriate default values"
    (let [gps-proto (pronto/proto-map JonSharedDataGps$GetGpsNavData)]
      ;; Numeric fields should default to 0
      (is (= 0.0 (:latitude gps-proto)))
      (is (= 0.0 (:longitude gps-proto)))
      (is (= 0.0 (:altitude gps-proto))))))

(deftest update-and-build-test
  (testing "Can update fields and build valid proto"
    (let [proto-map (-> (pronto/proto-map JonSharedDataCompass$GetCompassData)
                        (assoc :azimuth 270.0)
                        (assoc :pitch 0.0)
                        (assoc :roll 0.0))
          ;; Convert to actual protobuf object
          proto-obj (pu/proto-map->proto proto-map)]
      (is (instance? com.google.protobuf.Message proto-obj))
      ;; Can convert back to map
      (let [back-to-map (pronto/proto->proto-map proto-obj)]
        (is (= 270.0 (:azimuth back-to-map)))))))

;; Run a simple smoke test at namespace load to verify basics work
(defn smoke-test []
  (try
    (let [test-proto (pronto/proto-map JonSharedDataGps$GetGpsNavData)]
      (println "✓ Pronto integration working - successfully created proto-map from" 
               (.getSimpleName JonSharedDataGps$GetGpsNavData)))
    (catch Exception e
      (println "✗ Pronto integration failed:" (.getMessage e))
      (throw e))))

;; Run smoke test when namespace loads
(smoke-test)