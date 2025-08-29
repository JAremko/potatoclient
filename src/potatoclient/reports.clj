(ns potatoclient.reports
  "Generate markdown reports for development and documentation.

   This namespace provides utilities to generate reports about the codebase,
   including unspecced functions, code coverage, and other metrics."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.runtime :as runtime])
  (:import (java.io File)
           (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

;; -----------------------------------------------------------------------------
;; Markdown utilities
;; -----------------------------------------------------------------------------

(defn- md-header
  "Create a markdown header at the specified level." {:malli/schema [:=> [:cat :pos-int :string] :string]}
  [level text]
  (str (str/join (repeat level "#")) " " text))

(defn- md-code-block
  "Create a Markdown code block with optional language." {:malli/schema [:function [:=> [:cat :string] :string] [:=> [:cat :string [:maybe :string]] :string]]}
  ([code]
   (md-code-block code nil))
  ([code lang]
   (str "```" (or lang "") "\n" code "\n```")))

(defn- md-list-item
  "Create a Markdown list item." {:malli/schema [:=> [:cat :string [:* :any]] :string]}
  [text & {:keys [indent] :or {indent 0}}]
  (str (str/join (repeat indent "  ")) "- " text))

(defn- md-table-header
  "Create a Markdown table header row." {:malli/schema [:=> [:cat [:sequential :string]] :string]}
  [headers]
  (str "| " (str/join " | " headers) " |\n"
       "|" (str/join "|" (repeat (count headers) " --- ")) "|"))

(defn- md-table-row
  "Create a Markdown table row." {:malli/schema [:=> [:cat [:sequential :string]] :string]}
  [cells]
  (str "| " (str/join " | " cells) " |"))

(defn- md-bold
  "Create bold text." {:malli/schema [:=> [:cat :string] :string]}
  [text]
  (str "**" text "**"))

(defn- md-italic
  "Create italic text." {:malli/schema [:=> [:cat :string] :string]}
  [text]
  (str "*" text "*"))

(defn- md-badge
  "Create a shields.io style badge." {:malli/schema [:=> [:cat :string :string :string] :string]}
  [label value color]
  (let [encoded-label (str/replace label " " "%20")
        encoded-value (str/replace value " " "%20")]
    (str "![" label "](https://img.shields.io/badge/"
         encoded-label "-" encoded-value "-" color ")")))

;; -----------------------------------------------------------------------------
;; Report generation
;; -----------------------------------------------------------------------------

(defn- get-timestamp
  "Get current timestamp for reports." {:malli/schema [:=> [:cat] :string]}
  []
  (.format (LocalDateTime/now)
           (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")))

(defn- get-report-dir
  "Get the directory for storing reports." {:malli/schema [:=> [:cat] [:fn (fn* [p1__3834#] (instance? File p1__3834#))]]}
  []
  (io/file "reports"))

(defn- ensure-report-dir!
  "Ensure the report directory exists." {:malli/schema [:=> [:cat] [:fn (fn* [p1__3836#] (instance? File p1__3836#))]]}
  []
  (let [dir (get-report-dir)]
    (when-not (.exists dir)
      (.mkdirs dir))
    dir))

(defn- write-report!
  "Write a report to a file." {:malli/schema [:=> [:cat :string :string] :string]}
  [filename content]
  (let [report-dir (ensure-report-dir!)
        file (io/file report-dir filename)]
    (spit file content)
    (.getAbsolutePath file)))

(defn- format-namespace-section
  "Format a namespace section with its unspecced functions." {:malli/schema [:=> [:cat :symbol [:sequential :symbol]] :string]}
  [ns-sym functions]
  (let [sorted-fns (sort functions)]
    (str (md-header 3 (str ns-sym))
         "\n\n"
         (str/join "\n" (map #(md-list-item (str "`" % "`")) sorted-fns))
         "\n")))

(defn generate-unspecced-functions-report!
  "Generate a markdown report of functions not using Guardrails.
   Returns the path to the generated report file.
   Can optionally take pre-computed report data to avoid duplicate computation." {:malli/schema [:function [:=> [:cat] [:maybe :string]] [:=> [:cat [:maybe [:map-of :symbol [:sequential :symbol]]]] [:maybe :string]]]}
  ([]
   (generate-unspecced-functions-report! nil))
  ([report-data]
   (if (runtime/release-build?)
     (do
       (println "Reports are not available in release builds")
       nil)
     (let [unspecced-data (or report-data {})
           total (reduce + (map count (vals unspecced-data)))
           timestamp (get-timestamp)
           filename "unspecced-functions.md"
           content (str (md-header 1 "Functions Not Using Guardrails Report")
                        "\n\n"
                        (md-italic (str "Generated: " timestamp))
                        "\n\n"
                        (md-header 2 "Summary")
                        "\n\n"
                        (if (zero? total)
                          (str (md-badge "Coverage" "100%" "brightgreen") "\n\n"
                               "✅ " (md-bold "All functions are using Guardrails!"))
                          (str (md-badge "Functions Not Using Guardrails" (str total) "orange") "\n\n"
                               "⚠️ Found " (md-bold (str total)) " functions not using Guardrails across "
                               (md-bold (str (count unspecced-data))) " namespaces.\n"))
                        "\n"
                        (when (pos? total)
                          (str (md-header 2 "Functions by Namespace")
                               "\n\n"
                               (str/join "\n"
                                         (for [[ns-sym fns] (sort unspecced-data)]
                                           (format-namespace-section ns-sym fns)))
                               "\n\n"
                               (md-header 2 "Statistics")
                               "\n\n"
                               (md-table-header ["Namespace" "Count"])
                               "\n"
                               (str/join "\n"
                                         (for [[ns-sym fns] (sort-by (comp count second) > unspecced-data)]
                                           (md-table-row [(str "`" ns-sym "`") (str (count fns))])))
                               "\n"))
                        "\n"
                        (md-header 2 "Next Steps")
                        "\n\n"
                        (if (zero? total)
                          "Great job! All functions are properly using Guardrails!"
                          (str "1. Convert the functions to use Guardrails `>defn` syntax\n"
                               "2. Add proper function specs like: `[arg-spec => return-spec]`\n"
                               "3. Run `(potatoclient.reports/generate-unspecced-functions-report!)` again to update this report\n"))
                        "\n")]
       (doto (write-report! filename content)
         (#(println (str "Report generated: " %))))))))
