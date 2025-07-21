(ns potatoclient.cmd.debug
  "Debug utilities for inspecting protobuf command messages"
  (:require [potatoclient.cmd.core :as cmd-core]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]])
  (:import [com.google.protobuf.util JsonFormat]
           [cmd JonSharedCmd$Root]))

;; ============================================================================
;; Protobuf to JSON Conversion
;; ============================================================================

(>defn proto-to-json
  "Convert a protobuf message to JSON string"
  [proto-msg]
  [:potatoclient.specs/protobuf-message => string?]
  (-> (JsonFormat/printer)
      (.includingDefaultValueFields)
      (.print proto-msg)))

(>defn proto-to-json-pretty
  "Convert a protobuf message to pretty-printed JSON string"
  [proto-msg]
  [:potatoclient.specs/protobuf-message => string?]
  (-> (JsonFormat/printer)
      (.includingDefaultValueFields)
      (.print proto-msg)))

(>defn decode-command-bytes
  "Decode command bytes to JSON representation"
  [cmd-bytes]
  [bytes? => string?]
  (try
    (let [root-msg (JonSharedCmd$Root/parseFrom cmd-bytes)]
      (proto-to-json root-msg))
    (catch Exception e
      (str "Error decoding: " (.getMessage e)))))

;; ============================================================================
;; Command Inspection Functions
;; ============================================================================

(>defn inspect-command
  "Create a command and show both Base64 and JSON representations"
  [create-fn description]
  [fn? string? => nil?]
  (println (str "\n=== " description " ==="))
  (let [;; Temporarily capture the command
        commands (atom [])
        ;; Override the send function temporarily
        original-send cmd-core/send-cmd-message]
    (with-redefs [cmd-core/send-cmd-message
                  (fn [root-msg]
                    (swap! commands conj root-msg))]
      ;; Create the command
      (create-fn))

    ;; Now inspect what was captured
    (doseq [root-msg @commands]
      (let [cmd-bytes (cmd-core/encode-cmd-message root-msg)
            base64 (.encodeToString (java.util.Base64/getEncoder) cmd-bytes)
            json (proto-to-json (.build root-msg))]
        (println "Base64 payload:" base64)
        (println "Size:" (count cmd-bytes) "bytes")
        (println "JSON structure:")
        (println json))))
  nil)

;; ============================================================================
;; Demo Functions
;; ============================================================================

(>defn demo-basic-commands
  "Demonstrate basic command structures"
  []
  [=> nil?]
  (println "\n==== Protobuf Command Structure Demo ====")

  ;; Ping command
  (inspect-command
    #(cmd-core/send-cmd-ping)
    "Ping Command")

  ;; Frozen command
  (inspect-command
    #(cmd-core/send-cmd-frozen)
    "Frozen Command")

  ;; Noop command
  (inspect-command
    #(cmd-core/send-cmd-noop)
    "Noop Command")

  nil)

(>defn demo-rotary-commands
  "Demonstrate rotary command structures"
  []
  [=> nil?]
  (require '[potatoclient.cmd.rotary :as rotary])

  (println "\n==== Rotary Command Structures ====")

  ;; Start command
  (inspect-command
    #(rotary/rotary-start)
    "Rotary Start")

  ;; Set azimuth
  (inspect-command
    #(rotary/set-platform-azimuth 45.0)
    "Set Platform Azimuth to 45°")

  ;; Complex rotation
  (inspect-command
    #(rotary/rotate-both-to
       90.0 5.0 (rotary/string->direction "clockwise")
       45.0 3.0)
    "Rotate Both Axes")

  nil)

(>defn demo-camera-commands
  "Demonstrate day camera command structures"
  []
  [=> nil?]
  (require '[potatoclient.cmd.day-camera :as camera])

  (println "\n==== Day Camera Command Structures ====")

  ;; Power on
  (inspect-command
    #(camera/power-on)
    "Camera Power On")

  ;; Zoom direct value
  (inspect-command
    #(camera/zoom-direct-value 2.5)
    "Zoom to 2.5x")

  ;; Change palette
  (inspect-command
    #(camera/change-palette (camera/string->palette "bw"))
    "Change Palette to B&W")

  nil)

(>defn run-all-demos
  "Run all command structure demos"
  []
  [=> nil?]
  (demo-basic-commands)
  (demo-rotary-commands)
  (demo-camera-commands)
  (println "\n==== Demo Complete ====")
  nil)

;; ============================================================================
;; Utility Functions
;; ============================================================================

(>defn decode-base64-command
  "Decode a Base64-encoded command to see its JSON structure"
  [base64-str]
  [string? => nil?]
  (try
    (let [cmd-bytes (.decode (java.util.Base64/getDecoder) base64-str)
          json-str (decode-command-bytes cmd-bytes)]
      (println "Decoded command structure:")
      (println json-str))
    (catch Exception e
      (println "Error decoding:" (.getMessage e))))
  nil)

(>defn compare-commands
  "Create two commands and compare their structures"
  [create-fn1 desc1 create-fn2 desc2]
  [fn? string? fn? string? => nil?]
  (println (str "\n=== Comparing: " desc1 " vs " desc2 " ==="))
  (inspect-command create-fn1 desc1)
  (inspect-command create-fn2 desc2)
  nil)