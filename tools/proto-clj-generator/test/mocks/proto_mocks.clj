(ns mocks.proto-mocks
  "Mock protobuf classes for testing without external dependencies"
  (:import [com.google.protobuf GeneratedMessage$Builder]))

;; Mock the cmd.Root protobuf class
(gen-class
  :name cmd.Root
  :extends com.google.protobuf.GeneratedMessage
  :prefix "root-"
  :methods [[getProtocolVersion [] int]
            [getSessionId [] String]
            [getImportant [] boolean]
            [getFromCvSubsystem [] boolean]
            [getClientType [] cmd.Root$ClientType]
            [getPayloadCase [] cmd.Root$PayloadCase]
            [getPing [] cmd.Ping]
            [getRotary [] cmd.Rotary]
            [getSystem [] cmd.System]
            [toByteArray [] bytes]
            ^{:static true} [newBuilder [] cmd.Root$Builder]
            ^{:static true} [parseFrom [bytes] cmd.Root]])

;; Mock the PayloadCase enum
(gen-class
  :name cmd.Root$PayloadCase
  :prefix "payload-case-"
  :methods [^{:static true} [valueOf [String] cmd.Root$PayloadCase]])

;; Define enum constants
(def Root$PayloadCase/PING 
  (proxy [cmd.Root$PayloadCase] []))

(def Root$PayloadCase/ROTARY
  (proxy [cmd.Root$PayloadCase] []))

(def Root$PayloadCase/SYSTEM
  (proxy [cmd.Root$PayloadCase] []))

;; Mock builder
(gen-class
  :name cmd.Root$Builder
  :extends com.google.protobuf.GeneratedMessage$Builder
  :prefix "root-builder-"
  :methods [[setProtocolVersion [int] cmd.Root$Builder]
            [setSessionId [String] cmd.Root$Builder]
            [setImportant [boolean] cmd.Root$Builder]
            [setFromCvSubsystem [boolean] cmd.Root$Builder]
            [setClientType [cmd.Root$ClientType] cmd.Root$Builder]
            [setPing [cmd.Ping] cmd.Root$Builder]
            [setRotary [cmd.Rotary] cmd.Root$Builder]
            [setSystem [cmd.System] cmd.Root$Builder]
            [build [] cmd.Root]])

;; Mock implementations
(defn root-getProtocolVersion [this] 1)
(defn root-getSessionId [this] "test-session")
(defn root-getImportant [this] false)
(defn root-getFromCvSubsystem [this] false)
(defn root-getClientType [this] nil)
(defn root-getPayloadCase [this] Root$PayloadCase/PING)
(defn root-getPing [this] (proxy [cmd.Ping] []))
(defn root-getRotary [this] nil)
(defn root-getSystem [this] nil)
(defn root-toByteArray [this] (.getBytes "mock-proto-bytes"))
(defn root-newBuilder [] (proxy [cmd.Root$Builder] []))
(defn root-parseFrom [bytes] (proxy [cmd.Root] []))

;; Mock builder implementations
(defn root-builder-setProtocolVersion [this v] this)
(defn root-builder-setSessionId [this v] this)
(defn root-builder-setImportant [this v] this)
(defn root-builder-setFromCvSubsystem [this v] this)
(defn root-builder-setClientType [this v] this)
(defn root-builder-setPing [this v] this)
(defn root-builder-setRotary [this v] this)
(defn root-builder-setSystem [this v] this)
(defn root-builder-build [this] (proxy [cmd.Root] []))

;; Mock ser.JonGUIState
(gen-class
  :name ser.JonGUIState
  :extends com.google.protobuf.GeneratedMessage
  :prefix "state-"
  :methods [[getProtocolVersion [] int]
            [getSystem [] String]
            [getMeteoInternal [] String]
            [toByteArray [] bytes]
            ^{:static true} [newBuilder [] ser.JonGUIState$Builder]
            ^{:static true} [parseFrom [bytes] ser.JonGUIState]])

(gen-class
  :name ser.JonGUIState$Builder
  :extends com.google.protobuf.GeneratedMessage$Builder
  :prefix "state-builder-"
  :methods [[setProtocolVersion [int] ser.JonGUIState$Builder]
            [setSystem [String] ser.JonGUIState$Builder]
            [setMeteoInternal [String] ser.JonGUIState$Builder]
            [build [] ser.JonGUIState]])

;; State implementations
(defn state-getProtocolVersion [this] 1)
(defn state-getSystem [this] "system-data")
(defn state-getMeteoInternal [this] "meteo-data")
(defn state-toByteArray [this] (.getBytes "mock-state-bytes"))
(defn state-newBuilder [] (proxy [ser.JonGUIState$Builder] []))
(defn state-parseFrom [bytes] (proxy [ser.JonGUIState] []))

(defn state-builder-setProtocolVersion [this v] this)
(defn state-builder-setSystem [this v] this)
(defn state-builder-setMeteoInternal [this v] this)
(defn state-builder-build [this] (proxy [ser.JonGUIState] []))