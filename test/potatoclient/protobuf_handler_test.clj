(ns potatoclient.protobuf-handler-test
  "Test the protobuf Transit handlers work correctly"
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream PrintStream]
           [potatoclient.kotlin.transit SimpleProtobufHandlers SimpleCommandHandlers]
           [ser JonSharedData$JonGUIState
            JonSharedDataSystem$JonGuiDataSystem
            JonSharedDataTypes$JonGuiDataSystemLocalizations
            JonSharedDataTypes$JonGuiDataVideoChannel]
           [cmd JonSharedCmd$Root]
           [com.cognitect.transit TransitFactory]))

(deftest test-simple-protobuf-handlers
  (testing "SimpleProtobufHandlers serializes protobuf to Transit correctly"
    ;; Capture stdout to prevent any print statements
    (let [original-out System/out
          _ (System/setOut (PrintStream. (ByteArrayOutputStream.)))]
      (try
        ;; Create a simple system data
        (let [system-data (-> (JonSharedDataSystem$JonGuiDataSystem/newBuilder)
                              (.setCpuTemperature 45.5)
                              (.setRecEnabled true)
                              (.setLoc JonSharedDataTypes$JonGuiDataSystemLocalizations/JON_GUI_DATA_SYSTEM_LOCALIZATION_EN)
                              (.build))

              ;; Create state with system data
              state (-> (JonSharedData$JonGUIState/newBuilder)
                        (.setProtocolVersion 1)
                        (.setSystem system-data)
                        (.build))

              ;; Get our handlers
              handlers (.createWriteHandlers SimpleProtobufHandlers/INSTANCE)

              ;; Create Transit writer with our handlers
              out (ByteArrayOutputStream.)
              writer (com.cognitect.transit.TransitFactory/writer (com.cognitect.transit.TransitFactory$Format/MSGPACK) out handlers)

              ;; Write the protobuf state
              _ (.write writer state)

              ;; Read it back as Transit
              in (ByteArrayInputStream. (.toByteArray out))
              reader (com.cognitect.transit.TransitFactory/reader (com.cognitect.transit.TransitFactory$Format/MSGPACK) in)
              result (.read reader)]

          ;; Check it's a tagged value
          (is (instance? com.cognitect.transit.TaggedValue result))
          (is (= "jon-gui-state" (.getTag result)))

          ;; Get the actual map - getRep returns a Java map, not a Clojure map
          (let [state-map (.getRep result)]
            (is (instance? java.util.Map state-map))
            (is (= 1 (get state-map "protocol-version")))

            ;; Check system is also tagged
            (let [system (get state-map "system")]
              (is (instance? com.cognitect.transit.TaggedValue system))
              (is (= "system-data" (.getTag system)))

              ;; Check system data
              (let [system-map (.getRep system)]
                (is (= 45.5 (get system-map "cpu-temperature")))
                (is (= true (get system-map "rec-enabled")))
                ;; Localization should be a keyword
                (is (= (TransitFactory/keyword "en") (get system-map "localization")))))))

        (finally
          ;; Restore stdout
          (System/setOut original-out))))))

(deftest test-command-handlers-build-correct-protobuf
  (testing "SimpleCommandHandlers builds valid protobuf commands"
    (let [;; Test ping command
          ping-cmd (.buildCommand SimpleCommandHandlers/INSTANCE "ping" nil)]

      (is (not (nil? ping-cmd)))
      (is (instance? JonSharedCmd$Root ping-cmd))
      (is (.hasPing ping-cmd)))

    ;; Test rotary command with Transit keywords
    (let [params {(TransitFactory/keyword "channel") "heat"
                  (TransitFactory/keyword "x") 0.5
                  (TransitFactory/keyword "y") -0.3}
          goto-cmd (.buildCommand SimpleCommandHandlers/INSTANCE "rotary-goto-ndc" params)]

      (is (not (nil? goto-cmd)))
      (is (= 1 (.getProtocolVersion goto-cmd)))
      (is (.hasRotary goto-cmd))

      (let [rotary (.getRotary goto-cmd)]
        (is (.hasRotateToNdc rotary))
        (let [rotate (.getRotateToNdc rotary)]
          (is (= JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
                 (.getChannel rotate)))
          (is (< (Math/abs (- 0.5 (.getX rotate))) 0.001))
          (is (< (Math/abs (- -0.3 (.getY rotate))) 0.001)))))))