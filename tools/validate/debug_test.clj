(ns debug-test
  (:require [pronto.core :as p]
            [clojure.pprint :as pp])
  (:import [cmd JonSharedCmd$Root JonSharedCmd$Ping]))

;; Create mapper
(p/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

;; Create a simple ping command
(def ping-cmd
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
               :protocol_version 1
               :session_id 1000
               :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
               :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))

(println "Created ping command:")
(pp/pprint ping-cmd)

(println "\nConverting to bytes...")
(def ping-bytes (p/proto-map->bytes ping-cmd))
(println "Byte count:" (count ping-bytes))
(println "Bytes:" (vec ping-bytes))

;; Try parsing it back
(println "\nParsing back from bytes...")
(def parsed (p/bytes->proto-map cmd-mapper cmd.JonSharedCmd$Root ping-bytes))
(println "Parsed successfully:" (some? parsed))
(pp/pprint parsed)