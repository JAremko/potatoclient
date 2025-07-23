(ns potatoclient.cmd.day-camera
  "Day camera command functions for PotatoClient - TEMPORARY STUB"
  (:require [potatoclient.cmd.core :as cmd-core]
            [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]]))

;; Temporary stub functions until protobuf enums are fixed

(>defn power-on []
  [=> nil?]
  (println "Day camera power-on - NOT IMPLEMENTED (missing protobuf enums)")
  nil)

(>defn power-off []
  [=> nil?]
  (println "Day camera power-off - NOT IMPLEMENTED (missing protobuf enums)")
  nil)

(>defn zoom-in []
  [=> nil?]
  (println "Day camera zoom-in - NOT IMPLEMENTED (missing protobuf enums)")
  nil)

(>defn zoom-out []
  [=> nil?]
  (println "Day camera zoom-out - NOT IMPLEMENTED (missing protobuf enums)")
  nil)

(>defn focus-auto []
  [=> nil?]
  (println "Day camera focus-auto - NOT IMPLEMENTED (missing protobuf enums)")
  nil)

(>defn focus-manual []
  [=> nil?]
  (println "Day camera focus-manual - NOT IMPLEMENTED (missing protobuf enums)")
  nil)

;; String conversion stubs
(>defn string->palette [value]
  [string? => any?]
  nil)

(>defn string->agc-mode [value]
  [string? => any?]
  nil)

(>defn string->exposure-mode [value]
  [string? => any?]
  nil)

(>defn string->wdr-mode [value]
  [string? => any?]
  nil)

(>defn string->boolean-enum [value]
  [string? => any?]
  nil)

(>defn string->defog-status [value]
  [string? => any?]
  nil)