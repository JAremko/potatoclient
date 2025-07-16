(ns potatoclient.reports
  "Generate markdown reports for development and documentation.
   
   This namespace provides utilities to generate reports about the codebase,
   including unspecced functions, code coverage, and other metrics."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.instrumentation :as instrumentation]
            [potatoclient.runtime :as runtime])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

;; -----------------------------------------------------------------------------
;; Markdown utilities
;; -----------------------------------------------------------------------------

(defn- md-header
  "Create a markdown header at the specified level."
  [level text]
  (str (str/join (repeat level "#")) " " text))

(defn- md-code-block
  "Create a markdown code block with optional language."
  ([code] (md-code-block code nil))
  ([code lang]
   (str "```" (or lang "") "\n" code "\n```")))

(defn- md-list-item
  "Create a markdown list item."
  [text & {:keys [indent] :or {indent 0}}]
  (str (str/join (repeat indent "  ")) "- " text))

(defn- md-table-header
  "Create a markdown table header row."
  [headers]
  (str "| " (str/join " | " headers) " |\n"
       "|" (str/join "|" (repeat (count headers) " --- ")) "|"))

(defn- md-table-row
  "Create a markdown table row."
  [cells]
  (str "| " (str/join " | " cells) " |"))

(defn- md-link
  "Create a markdown link."
  [text url]
  (str "[" text "](" url ")"))

(defn- md-bold
  "Create bold text."
  [text]
  (str "**" text "**"))

(defn- md-italic
  "Create italic text."
  [text]
  (str "*" text "*"))

(defn- md-badge
  "Create a shields.io style badge."
  [label value color]
  (let [encoded-label (str/replace label " " "%20")
        encoded-value (str/replace value " " "%20")]
    (str "![" label "](https://img.shields.io/badge/"
         encoded-label "-" encoded-value "-" color ")")))

;; -----------------------------------------------------------------------------
;; Report generation
;; -----------------------------------------------------------------------------

(defn- get-timestamp
  "Get current timestamp for reports."
  []
  (.format (LocalDateTime/now) 
           (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")))

(defn- get-report-dir
  "Get the directory for storing reports."
  []
  (io/file "reports"))

(defn- ensure-report-dir!
  "Ensure the report directory exists."
  []
  (let [dir (get-report-dir)]
    (when-not (.exists dir)
      (.mkdirs dir))
    dir))

(defn- write-report!
  "Write a report to a file."
  [filename content]
  (let [report-dir (ensure-report-dir!)
        file (io/file report-dir filename)]
    (spit file content)
    (.getAbsolutePath file)))

(defn- format-namespace-section
  "Format a namespace section with its unspecced functions."
  [ns-sym functions]
  (let [sorted-fns (sort functions)]
    (str (md-header 3 (str ns-sym))
         "\n\n"
         (str/join "\n" (map #(md-list-item (str "`" % "`")) sorted-fns))
         "\n")))

(defn generate-unspecced-functions-report!
  "Generate a markdown report of functions without Malli specs.
   Returns the path to the generated report file.
   Can optionally take pre-computed report data to avoid duplicate computation."
  ([]
   (generate-unspecced-functions-report! nil))
  ([report-data]
   (if (runtime/release-build?)
     (do
       (println "Reports are not available in release builds")
       nil)
     (let [report-data (or report-data (instrumentation/report-unspecced-functions))]
       (if (= :error (:status report-data))
         (do
           (println (:message report-data))
           nil)
         (let [timestamp (get-timestamp)
               unspecced-data (:data report-data)
               total (:total report-data)
               filename "unspecced-functions.md"
              content (str (md-header 1 "Unspecced Functions Report")
                          "\n\n"
                          (md-italic (str "Generated: " timestamp))
                          "\n\n"
                          (md-header 2 "Summary")
                          "\n\n"
                          (if (zero? total)
                            (str (md-badge "Coverage" "100%" "brightgreen") "\n\n"
                                 "✅ " (md-bold "All functions have Malli specs!"))
                            (str (md-badge "Unspecced Functions" (str total) "orange") "\n\n"
                                 "⚠️ Found " (md-bold (str total)) " functions without Malli specs across "
                                 (md-bold (str (count unspecced-data))) " namespaces.\n"))
                          "\n"
                          (when (pos? total)
                            (str (md-header 2 "Unspecced Functions by Namespace")
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
                            "Great job! All functions are properly instrumented with Malli specs."
                            (str "1. Add Malli specs for the unspecced functions to `src/potatoclient/instrumentation.clj`\n"
                                 "2. Follow the pattern: `(m/=> namespace/function [:=> [:cat ...args...] return-type])`\n"
                                 "3. Run `(potatoclient.reports/generate-unspecced-functions-report!)` again to update this report\n"))
                          "\n")]
          (let [path (write-report! filename content)]
            (println (str "Report generated: " path))
            path)))))))

(defn generate-all-reports!
  "Generate all available reports.
   Returns a map of report type to file path."
  []
  (if (runtime/release-build?)
    (do
      (println "Reports are not available in release builds")
      {})
    {:unspecced-functions (generate-unspecced-functions-report!)}))