(ns potatoclient.reports
  "Generate markdown reports for development and documentation.

   This namespace provides utilities to generate reports about the codebase,
   including unspecced functions, code coverage, and other metrics."
  (:require
            [malli.core :as m] [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.runtime :as runtime])
  (:import (java.io File)
           (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

;; -----------------------------------------------------------------------------
;; Markdown utilities
;; -----------------------------------------------------------------------------

(defn- md-header
  "Create a markdown header at the specified level."
  [level text]
  (str (str/join (repeat level "#")) " " text)) 
 (m/=> md-header [:=> [:cat :pos-int :string] :string])

(defn- md-code-block
  "Create a Markdown code block with optional language."
  ([code]
   (md-code-block code nil))
  ([code lang]
   (str "```" (or lang "") "\n" code "\n```"))) 
 (m/=> md-code-block [:function [:=> [:cat :string] :string] [:=> [:cat :string [:maybe :string]] :string]])

(defn- md-list-item
  "Create a Markdown list item."
  [text & {:keys [indent] :or {indent 0}}]
  (str (str/join (repeat indent "  ")) "- " text)) 
 (m/=> md-list-item [:=> [:cat :string [:* :any]] :string])

(defn- md-table-header
  "Create a Markdown table header row."
  [headers]
  (str "| " (str/join " | " headers) " |\n"
       "|" (str/join "|" (repeat (count headers) " --- ")) "|")) 
 (m/=> md-table-header [:=> [:cat [:sequential :string]] :string])

(defn- md-table-row
  "Create a Markdown table row."
  [cells]
  (str "| " (str/join " | " cells) " |")) 
 (m/=> md-table-row [:=> [:cat [:sequential :string]] :string])

(defn- md-bold
  "Create bold text."
  [text]
  (str "**" text "**")) 
 (m/=> md-bold [:=> [:cat :string] :string])

(defn- md-italic
  "Create italic text."
  [text]
  (str "*" text "*")) 
 (m/=> md-italic [:=> [:cat :string] :string])

(defn- md-badge
  "Create a shields.io style badge."
  [label value color]
  (let [encoded-label (str/replace label " " "%20")
        encoded-value (str/replace value " " "%20")]
    (str "![" label "](https://img.shields.io/badge/"
         encoded-label "-" encoded-value "-" color ")"))) 
 (m/=> md-badge [:=> [:cat :string :string :string] :string])

;; -----------------------------------------------------------------------------
;; Report generation
;; -----------------------------------------------------------------------------

(defn- get-timestamp
  "Get current timestamp for reports."
  []
  (.format (LocalDateTime/now)
           (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))) 
 (m/=> get-timestamp [:=> [:cat] :string])

(defn- get-report-dir
  "Get the directory for storing reports."
  []
  (io/file "reports")) 
 (m/=> get-report-dir [:=> [:cat] [:fn (partial instance? File)]])

(defn- ensure-report-dir!
  "Ensure the report directory exists."
  []
  (let [dir (get-report-dir)]
    (when-not (.exists dir)
      (.mkdirs dir))
    dir)) 
 (m/=> ensure-report-dir! [:=> [:cat] [:fn (partial instance? File)]])

(defn- write-report!
  "Write a report to a file."
  [filename content]
  (let [report-dir (ensure-report-dir!)
        file (io/file report-dir filename)]
    (spit file content)
    (.getAbsolutePath file))) 
 (m/=> write-report! [:=> [:cat :string :string] :string])

(defn- format-namespace-section
  "Format a namespace section with its unspecced functions."
  [ns-sym functions]
  (let [sorted-fns (sort functions)]
    (str (md-header 3 (str ns-sym))
         "\n\n"
         (str/join "\n" (map #(md-list-item (str "`" % "`")) sorted-fns))
         "\n"))) 
 (m/=> format-namespace-section [:=> [:cat :symbol [:sequential :symbol]] :string])