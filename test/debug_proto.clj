(ns debug-proto
  (:require [potatoclient.proto :as proto])
  (:import (ser JonSharedData$JonGUIState)))

;; Test empty protobuf
(let [builder (JonSharedData$JonGUIState/newBuilder)
      proto-msg (.build builder)
      binary (.toByteArray proto-msg)]
  (println "Empty proto binary length:" (count binary))
  (println "Binary bytes:" (vec binary))

  ;; Try to parse it back
  (try
    (let [parsed (JonSharedData$JonGUIState/parseFrom binary)
          edn-map (proto/proto-map->clj-map parsed)]
      (println "Parsed EDN:" edn-map)
      (println "Protocol version:" (:protocol-version edn-map)))
    (catch Exception e
      (println "Error:" (.getMessage e)))))