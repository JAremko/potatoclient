(ns potatoclient.transit-handlers-working-test
  "Verify Transit handlers work for state subprocess"
  (:require [clojure.test :refer [deftest testing is]])
  (:import [potatoclient.kotlin.transit SimpleProtobufHandlers SimpleCommandHandlers]
           [ser JonSharedData$JonGUIState 
                JonSharedDataSystem$JonGuiDataSystem 
                JonSharedDataTypes$JonGuiDataSystemLocalizations]
           [com.cognitect.transit TransitFactory TransitFactory$Format]
           [java.io ByteArrayOutputStream ByteArrayInputStream]))

(deftest test-transit-handlers-integration
  (testing "Transit handlers serialize protobuf state correctly"
    ;; This simulates what StateSubprocess does
    (let [;; Create protobuf state like we'd receive from WebSocket
          system-data (-> (JonSharedDataSystem$JonGuiDataSystem/newBuilder)
                          (.setCpuTemperature 65.5)
                          (.setGpuTemperature 72.3)
                          (.setRecEnabled true)
                          (.setLowDiskSpace false)
                          (.setLoc JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN)
                          (.build))
          
          proto-state (-> (JonSharedData$JonGUIState/newBuilder)
                          (.setProtocolVersion 1)
                          (.setSystem system-data)
                          (.build))
          
          ;; Get handlers from StateSubprocess
          handlers (.createWriteHandlers SimpleProtobufHandlers/INSTANCE)
          
          ;; Serialize like StateSubprocess does
          out (ByteArrayOutputStream.)
          writer (TransitFactory/writer TransitFactory$Format/MSGPACK out handlers)
          _ (.write writer proto-state)
          
          ;; Read back to verify
          in (ByteArrayInputStream. (.toByteArray out))
          reader (TransitFactory/reader TransitFactory$Format/MSGPACK in)
          result (.read reader)]
      
      ;; The result is a TaggedValue "jon-gui-state"
      (is (instance? com.cognitect.transit.TaggedValue result))
      (is (= "jon-gui-state" (.getTag result)))
      
      ;; The representation contains our data
      (let [state-map (.getRep result)]
        (is (map? state-map))
        (is (= 1 (get state-map "protocol-version")))
        (is (not (nil? (get state-map "system"))))
        
        ;; System is also tagged
        (let [system (get state-map "system")]
          (is (instance? com.cognitect.transit.TaggedValue system))
          (is (= "system-data" (.getTag system))))))))

(deftest test-command-building-works
  (testing "Command building from Transit data works"
    ;; This simulates CommandSubprocess receiving commands
    (let [;; Create Transit-style parameters (with keywords)
          params {(TransitFactory/keyword "channel") "heat"
                  (TransitFactory/keyword "x") 0.5
                  (TransitFactory/keyword "y") -0.5}
          
          ;; Build command
          cmd (.buildCommand SimpleCommandHandlers/INSTANCE "rotary-goto-ndc" params)]
      
      (is (not (nil? cmd)))
      (is (.hasRotary cmd))
      (is (= 1 (.getProtocolVersion cmd)))
      
      ;; Verify the command was built correctly
      (let [rotary (.getRotary cmd)
            rotate (.getRotateToNdc rotary)]
        (is (= "heat" (if (= (.getChannel rotate) ser.JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT)
                        "heat"
                        "day")))
        (is (= 0.5 (.getX rotate)))
        (is (= -0.5 (.getY rotate)))))))