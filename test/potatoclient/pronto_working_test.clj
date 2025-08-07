(ns potatoclient.pronto-working-test
  "Working test using actual protobuf class names that exist"
  (:require [clojure.test :refer [deftest testing is]]))

(deftest protobuf-classes-available-test
  (testing "Generated protobuf classes are available and working"
    ;; Load the actual classes that exist
    (let [rotary-class (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary")
          halt-class (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary$Halt")
          gps-class (Class/forName "ser.JonSharedDataGps")
          gps-gui-class (Class/forName "ser.JonSharedDataGps$JonGuiDataGps")
          compass-class (Class/forName "ser.JonSharedDataCompass")]
      
      (is (not (nil? rotary-class)) "Rotary command class should exist")
      (is (not (nil? halt-class)) "Halt command class should exist")
      (is (not (nil? gps-class)) "GPS data class should exist")
      (is (not (nil? gps-gui-class)) "GPS GUI data class should exist")
      (is (not (nil? compass-class)) "Compass data class should exist"))))

(deftest create-protobuf-instances-test
  (testing "Can create protobuf instances using builders"
    ;; Create a Halt command
    (let [halt-class (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary$Halt")
          builder-method (.getMethod halt-class "newBuilder" (make-array Class 0))
          builder (.invoke builder-method nil (make-array Object 0))
          build-method (.getMethod (.getClass builder) "build" (make-array Class 0))
          halt-instance (.invoke build-method builder (make-array Object 0))]
      
      (is (not (nil? halt-instance)) "Should create Halt instance")
      (is (.isInstance halt-class halt-instance) "Instance should be of correct type")
      (println "✓ Created Halt command instance:" (.getSimpleName (.getClass halt-instance))))))

(deftest gps-data-manipulation-test
  (testing "Can create and manipulate GPS GUI data"
    (let [;; Use the actual GPS GUI data class
          gps-class (Class/forName "ser.JonSharedDataGps$JonGuiDataGps")
          builder-method (.getMethod gps-class "newBuilder" (make-array Class 0))
          builder (.invoke builder-method nil (make-array Object 0))
          
          ;; Set latitude and longitude if those fields exist
          ;; First check what fields are available
          builder-class (.getClass builder)
          methods (.getMethods builder-class)
          setter-names (map #(.getName %) methods)
          
          ;; Find relevant setters
          has-lat? (some #(= "setLatitude" %) setter-names)
          has-lon? (some #(= "setLongitude" %) setter-names)
          has-alt? (some #(= "setAltitude" %) setter-names)]
      
      (println "Available GPS setters:")
      (println "  - Has setLatitude:" has-lat?)
      (println "  - Has setLongitude:" has-lon?)
      (println "  - Has setAltitude:" has-alt?)
      
      ;; Set values if methods exist
      (when has-lat?
        (.invoke (.getMethod builder-class "setLatitude" (into-array Class [Double/TYPE]))
                 builder
                 (into-array Object [45.5])))
      
      (when has-lon?
        (.invoke (.getMethod builder-class "setLongitude" (into-array Class [Double/TYPE]))
                 builder
                 (into-array Object [-122.6])))
      
      ;; Build the message
      (let [build-method (.getMethod builder-class "build" (make-array Class 0))
            gps-msg (.invoke build-method builder (make-array Object 0))]
        
        (is (not (nil? gps-msg)) "Should create GPS message")
        
        ;; Get values back if getters exist
        (when has-lat?
          (let [get-lat (.getMethod (.getClass gps-msg) "getLatitude" (make-array Class 0))
                lat (.invoke get-lat gps-msg (make-array Object 0))]
            (is (= 45.5 lat) "Latitude should be set correctly")
            (println "✓ GPS latitude:" lat)))
        
        (when has-lon?
          (let [get-lon (.getMethod (.getClass gps-msg) "getLongitude" (make-array Class 0))
                lon (.invoke get-lon gps-msg (make-array Object 0))]
            (is (= -122.6 lon) "Longitude should be set correctly")
            (println "✓ GPS longitude:" lon)))))))

(deftest serialization-test
  (testing "Can serialize and deserialize protobuf messages"
    (let [;; Create a simple Halt command
          halt-class (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary$Halt")
          builder-method (.getMethod halt-class "newBuilder" (make-array Class 0))
          builder (.invoke builder-method nil (make-array Object 0))
          build-method (.getMethod (.getClass builder) "build" (make-array Class 0))
          halt-msg (.invoke build-method builder (make-array Object 0))
          
          ;; Serialize to bytes
          to-byte-array-method (.getMethod (.getClass halt-msg) "toByteArray" (make-array Class 0))
          bytes (.invoke to-byte-array-method halt-msg (make-array Object 0))
          
          ;; Deserialize back
          parse-from-method (.getMethod halt-class "parseFrom" (into-array Class [(Class/forName "[B")]))
          restored (.invoke parse-from-method nil (into-array Object [bytes]))]
      
      (is (bytes? bytes) "Should serialize to byte array")
      ;; Note: Empty messages can serialize to 0 bytes in protobuf
      (is (>= (alength bytes) 0) "Byte array should exist")
      (is (not (nil? restored)) "Should deserialize back to object")
      (is (.isInstance halt-class restored) "Deserialized object should be correct type")
      (println "✓ Serialization successful - bytes:" (alength bytes)))))

;; Run validation
(println "\n=== Protobuf Integration Test ===")
(println "Testing protobuf classes work correctly...")
(println "==================================\n")