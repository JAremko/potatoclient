(ns potatoclient.transit.keyword-handlers
  "Transit handlers for automatic string to keyword conversion.
  
  This implements the keyword-based type system where all enum values
  are automatically converted to keywords, except for designated text fields."
  (:require [cognitect.transit :as transit]
            [clojure.string :as str]))

;; Message types that can contain preserved text
(def text-preserving-message-types
  "Message types where :text fields should remain as strings"
  #{:log :error :debug :info :warn :trace})

(defn enum-string?
  "Check if a string looks like an enum value (lowercase with dashes)"
  [s]
  (and (string? s)
       (re-matches #"^[a-z][a-z0-9-]*$" s)))

(defn should-keywordize?
  "Determine if a value should be converted to a keyword.
  
  Rules:
  1. Strings that look like enums (lowercase-with-dashes) → keywords
  2. Special case: :text fields in log messages stay as strings
  3. UUIDs stay as strings
  4. Everything else passes through"
  [context value]
  (cond
    ;; Not a string - pass through
    (not (string? value)) false
    
    ;; UUID pattern - keep as string
    (re-matches #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$" value) false
    
    ;; In a text-preserving context and this is the text field
    (and (= (:key context) :text)
         (contains? text-preserving-message-types (:msg-type context))) false
    
    ;; Looks like an enum value
    (enum-string? value) true
    
    ;; Default: keep as string
    :else false))

(defn create-keywordizing-read-handler
  "Creates a Transit read handler that intelligently converts strings to keywords"
  []
  (let [context (atom {})]
    (transit/read-handler
      (fn [tag rep]
        (cond
          ;; Handle the string conversion
          (and (= tag "?") (string? rep))
          (if (should-keywordize? @context rep)
            (keyword rep)
            rep)
          
          ;; Track context for nested structures
          (map? rep)
          (do
            ;; Update context with message type if present
            (when-let [msg-type (:msg-type rep)]
              (swap! context assoc :msg-type msg-type))
            rep)
          
          ;; Default passthrough
          :else rep)))))

(defn create-keyword-writer
  "Creates a Transit writer for the Kotlin side that handles enums properly"
  [format]
  (transit/writer format
    {:handlers
     {;; Java enums write as their string representation
      java.lang.Enum
      (transit/write-handler
        "enum"
        (fn [e] (.name e))
        str)
      
      ;; Clojure keywords write as strings for Kotlin
      clojure.lang.Keyword
      (transit/write-handler
        "kw"
        name
        str)}}))

(defn create-keyword-reader
  "Creates a Transit reader that automatically converts appropriate strings to keywords"
  [format]
  (transit/reader format
    {:handlers
     {"?" (create-keywordizing-read-handler)
      "enum" (transit/read-handler keyword)
      "kw" (transit/read-handler keyword)}}))

;; Example usage functions
(defn write-for-kotlin
  "Write data for consumption by Kotlin (keywords → strings)"
  [data]
  (let [out (java.io.ByteArrayOutputStream.)
        writer (create-keyword-writer :msgpack)]
    (transit/write writer data out)
    (.toByteArray out)))

(defn read-from-kotlin
  "Read data from Kotlin (strings → keywords where appropriate)"
  [bytes]
  (let [in (java.io.ByteArrayInputStream. bytes)
        reader (create-keyword-reader :msgpack)]
    (transit/read reader in)))

;; Testing the conversion logic
(comment
  ;; These should become keywords
  (should-keywordize? {} "heat") ;=> true
  (should-keywordize? {} "window-close") ;=> true
  (should-keywordize? {} "double-tap") ;=> true
  
  ;; These should stay as strings
  (should-keywordize? {} "Hello World") ;=> false
  (should-keywordize? {} "123e4567-e89b-12d3-a456-426614174000") ;=> false
  (should-keywordize? {:key :text :msg-type :log} "This is log text") ;=> false
  
  ;; Round trip test
  (let [data {:msg-type "command"
              :action "rotary-goto-ndc"
              :params {:channel "heat"
                       :x 0.5
                       :y -0.5}}
        encoded (write-for-kotlin data)
        decoded (read-from-kotlin encoded)]
    decoded)
  ;; => {:msg-type :command
  ;;     :action :rotary-goto-ndc  
  ;;     :params {:channel :heat
  ;;              :x 0.5
  ;;              :y -0.5}}
  )