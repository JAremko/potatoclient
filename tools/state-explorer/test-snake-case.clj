(ns test-snake-case
  (:require [clojure.java.io :as io]
            [state-explorer.proto-to-edn :as p2e]
            [clojure.pprint :as pprint])
  (:import [ser JonSharedData$JonGUIState]))

(defn test-conversion []
  (println "Testing snake_case conversion...")
  
  ;; Read the binary file
  (let [binary-data (with-open [is (io/input-stream "output/1754664759800.bin")]
                      (let [baos (java.io.ByteArrayOutputStream.)]
                        (io/copy is baos)
                        (.toByteArray baos)))
        ;; Parse as protobuf
        proto-msg (JonSharedData$JonGUIState/parseFrom binary-data)
        ;; Convert to EDN
        edn-data (p2e/proto-message->map proto-msg)]
    
    (println "\nFirst few keys of converted EDN (should be snake_case):")
    (println (take 5 (keys edn-data)))
    
    (println "\nSample nested structure (check field names and enum values):")
    (when-let [gps (:gps edn-data)]
      (println "GPS data:")
      (pprint/pprint gps))
    
    (println "\nEnum values (should be UPPER_SNAKE_CASE):")
    (when-let [camera-day (or (:camera_day edn-data) (:camera-day edn-data))]
      (println "Camera day fx_mode:" (:fx_mode camera-day))
      (println "Camera day fields:" (keys camera-day)))
    
    edn-data))

(test-conversion)