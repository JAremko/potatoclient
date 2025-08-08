(ns potatoclient.proto-basic-test
  "Basic test to verify protobuf classes are available"
  (:require [clojure.test :refer [deftest testing is]]))

(deftest load-proto-classes-test
  (testing "Can load generated protobuf classes"
    ;; Try loading the classes dynamically
    (let [rotary-class (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary")
          halt-class (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary$Halt")
          gps-class (Class/forName "ser.JonSharedDataGps$JonGuiDataGps")]
      
      (is (not (nil? rotary-class)))
      (is (not (nil? halt-class)))
      (is (not (nil? gps-class)))
      
      ;; Try creating instances
      (let [halt-builder (.getMethod halt-class "newBuilder" (make-array Class 0))
            builder (.invoke halt-builder nil (make-array Object 0))
            build-method (.getMethod (.getClass builder) "build" (make-array Class 0))
            halt-instance (.invoke build-method builder (make-array Object 0))]
        (is (not (nil? halt-instance)))
        (println "Successfully created instance of" (.getName (.getClass halt-instance)))))))

(deftest basic-proto-operations-test
  (testing "Can perform basic protobuf operations"
    (let [;; Load GPS data class
          gps-class (Class/forName "ser.JonSharedDataGps$JonGuiDataGps")
          ;; Get newBuilder method
          builder-method (.getMethod gps-class "newBuilder" (make-array Class 0))
          ;; Create builder
          builder (.invoke builder-method nil (make-array Object 0))
          ;; Set some values using reflection
          _ (.invoke (.getMethod (.getClass builder) "setLatitude" (into-array Class [Double/TYPE]))
                     builder
                     (into-array Object [45.5]))
          _ (.invoke (.getMethod (.getClass builder) "setLongitude" (into-array Class [Double/TYPE]))
                     builder  
                     (into-array Object [-122.6]))
          ;; Build the message
          build-method (.getMethod (.getClass builder) "build" (make-array Class 0))
          gps-msg (.invoke build-method builder (make-array Object 0))
          ;; Get values back
          get-lat (.getMethod (.getClass gps-msg) "getLatitude" (make-array Class 0))
          get-lon (.getMethod (.getClass gps-msg) "getLongitude" (make-array Class 0))
          lat (.invoke get-lat gps-msg (make-array Object 0))
          lon (.invoke get-lon gps-msg (make-array Object 0))]
      
      (is (= 45.5 lat))
      (is (= -122.6 lon))
      (println "GPS message created with lat=" lat "lon=" lon))))

;; Simple validation function
(defn validate-proto-setup []
  (println "\n=== Checking Protobuf Setup ===")
  (try
    (Class/forName "cmd.RotaryPlatform.JonSharedCmdRotary")
    (println "✓ Can load Rotary command class")
    (Class/forName "ser.JonSharedDataGps")
    (println "✓ Can load GPS data class")
    (Class/forName "ser.JonSharedDataCompass")
    (println "✓ Can load Compass data class")
    (println "================================\n")
    true
    (catch ClassNotFoundException e
      (println "✗ Failed to load class:" (.getMessage e))
      (println "Make sure to run: make compile-java-proto")
      false)))

(validate-proto-setup)