#!/usr/bin/env bb

(require '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str]
         '[clojure.java.io :as io]
         '[clojure.edn :as edn])

(def reports-dir "reports/lint")

(defn ensure-reports-dir []
  (.mkdirs (io/file reports-dir)))

(defn run-linter [cmd & args]
  (apply sh cmd args))

(defn parse-clj-kondo-output [output]
  (let [lines (str/split-lines output)
        issues (for [line lines
                     :when (and (not (str/blank? line))
                                (re-find #":\d+:\d+: " line)  ; Match file:line:col: pattern
                                (not (str/starts-with? line "linting took"))  ; Skip summary line
                                (not (str/starts-with? line ".")))]  ; Skip progress dots
                 (let [match (re-find #"^(.+?):(\d+):(\d+): (warning|error): (.+)$" line)]
                   (when match
                     (let [[_ file line-num col-num level message] match]
                       {:file file
                        :line (Integer/parseInt line-num)
                        :col (Integer/parseInt col-num)
                        :level (keyword level)
                        :message message}))))]
    (remove nil? issues)))

(defn parse-ktlint-output [output]
  (let [lines (str/split-lines output)
        current-file (atom nil)
        issues (atom [])]
    (doseq [line lines]
      (cond
        ;; Skip WARNING lines from JVM
        (str/starts-with? line "WARNING:")
        nil
        
        ;; File header line - absolute or relative path
        (or (re-matches #"^/.+\.kt$" line)
            (re-matches #"^src/.+\.kt$" line))
        (reset! current-file line)
        
        ;; Issue line format: file.kt:line:col: message
        (re-find #"\.kt:\d+:\d+:" line)
        (let [match (re-find #"^(.+\.kt):(\d+):(\d+):\s*(.+)$" line)]
          (when match
            (let [[_ file line-num col-num message] match]
              (swap! issues conj
                     {:file file
                      :line (Integer/parseInt line-num)
                      :col (Integer/parseInt col-num)
                      :level :error  ; ktlint only reports errors
                      :message message}))))))
    @issues))

(defn parse-detekt-output [output]
  ;; Detekt output format: 
  ;; RuleName - extra info - [function/property] at file.kt:line:col - Signature=...
  (let [lines (str/split-lines output)
        pattern #"^(\w+)\s+-\s+.*?\s+-\s+\[.*?\]\s+at\s+(.+?):(\d+):(\d+)\s+-\s+(.+)$"
        issues (for [line lines
                     :when (and (not (str/blank? line))
                                (re-find #"\s+at\s+" line))]
                 (let [parts (str/split line #"\s+-\s+")
                       rule-name (first parts)
                       ;; Find the "at" part
                       at-part (some #(when (re-find #"\s+at\s+" %) %) parts)
                       location-match (when at-part
                                         (re-find #"at\s+(.+?):(\d+):(\d+)" at-part))]
                   (when location-match
                     (let [[_ file line col] location-match]
                       {:file file
                        :line (Integer/parseInt line)
                        :col (Integer/parseInt col)
                        :level :warning  ; detekt issues are warnings
                        :message rule-name}))))]
    (remove nil? issues)))

(defn group-by-severity [issues]
  (group-by :level issues))

(defn group-by-file [issues]
  (group-by :file issues))

(defn write-markdown-report [file-path title issues]
  (with-open [w (io/writer file-path)]
    (.write w (str "# " title "\n\n"))
    (.write w (str "Generated: " (java.time.LocalDateTime/now) "\n\n"))
    (.write w (str "Total issues: " (count issues) "\n\n"))
    
    (when (seq issues)
      ;; Group by file
      (let [by-file (group-by-file issues)]
        (doseq [[file file-issues] (sort-by key by-file)]
          (.write w (str "## " file "\n\n"))
          (.write w (str "Issues: " (count file-issues) "\n\n"))
          
          ;; Sort issues by line number
          (doseq [issue (sort-by :line file-issues)]
            (.write w (str "- **Line " (:line issue)))
            (when (:col issue)
              (.write w (str ":" (:col issue))))
            (.write w (str "** [" (name (:level issue)) "] " (:message issue) "\n")))
          (.write w "\n"))))))

(defn write-summary-report [file-path clj-issues kotlin-issues]
  (with-open [w (io/writer file-path)]
    (.write w "# Lint Report Summary\n\n")
    (.write w (str "Generated: " (java.time.LocalDateTime/now) "\n\n"))
    
    ;; Overall statistics
    (let [clj-count (count clj-issues)
          kotlin-count (count kotlin-issues)
          total-count (+ clj-count kotlin-count)
          clj-by-severity (group-by-severity clj-issues)
          kotlin-by-severity (group-by-severity kotlin-issues)]
      
      (.write w "## Overall Statistics\n\n")
      (.write w (str "- **Total Issues**: " total-count "\n"))
      (.write w (str "- **Clojure Issues**: " clj-count "\n"))
      (.write w (str "- **Kotlin Issues**: " kotlin-count "\n\n"))
      
      (.write w "## By Severity\n\n")
      (.write w "### Clojure\n")
      (doseq [[level issues] (sort-by key clj-by-severity)]
        (.write w (str "- **" (name level) "**: " (count issues) "\n")))
      
      (.write w "\n### Kotlin\n")
      (doseq [[level issues] (sort-by key kotlin-by-severity)]
        (.write w (str "- **" (name level) "**: " (count issues) "\n")))
      
      (.write w "\n## Quick Links\n\n")
      (.write w "### Clojure Reports\n")
      (.write w "- [All Issues](clojure-all.md)\n")
      (doseq [[level _] (sort-by key clj-by-severity)]
        (.write w (str "- [" (str/capitalize (name level)) "s](clojure-" (name level) ".md)\n")))
      
      (.write w "\n### Kotlin Reports\n")
      (.write w "- [All Issues](kotlin-all.md)\n")
      (.write w "- [ktlint Issues](kotlin-ktlint.md)\n")
      (.write w "- [detekt Issues](kotlin-detekt.md)\n")
      (doseq [[level _] (sort-by key kotlin-by-severity)]
        (.write w (str "- [" (str/capitalize (name level)) "s](kotlin-" (name level) ".md)\n"))))))

(defn main []
  (println "Generating organized lint reports...")
  (ensure-reports-dir)
  
  ;; Run linters
  (println "Running clj-kondo...")
  (let [clj-result (run-linter "clj-kondo" "--lint" "src" "--config" ".clj-kondo/config.edn")
        ;; clj-kondo outputs to stdout, not stderr
        clj-issues (parse-clj-kondo-output (:out clj-result))]
    
    (println "Running ktlint...")
    (let [ktlint-result (run-linter "java" "-jar" ".ktlint/ktlint" "src/**/*.kt" "--relative")
          ;; ktlint outputs to both stdout and stderr, combine them
          ktlint-output (str (:out ktlint-result) "\n" (:err ktlint-result))
          ktlint-issues (parse-ktlint-output ktlint-output)]
      
      (println "Running detekt...")
      (let [detekt-jar (if (.exists (io/file ".detekt/detekt-cli.jar"))
                         ".detekt/detekt-cli.jar"
                         (do
                           (println "Detekt not found. Please run 'make lint-kotlin-detekt' first to install it.")
                           nil))
            detekt-issues (if detekt-jar
                            (let [detekt-result (run-linter "java" "-jar" detekt-jar
                                                           "--config" "detekt.yml" 
                                                           "--input" "src/potatoclient/kotlin"
                                                           "--report" "txt:reports/lint/detekt-raw.txt")
                                  detekt-output (when (.exists (io/file "reports/lint/detekt-raw.txt"))
                                                  (slurp "reports/lint/detekt-raw.txt"))]
                              (if detekt-output
                                (parse-detekt-output detekt-output)
                                []))
                            [])]
        
        ;; Combine Kotlin issues
        (let [all-kotlin-issues (concat ktlint-issues detekt-issues)]
          
          ;; Write Clojure reports
          (println "Writing Clojure reports...")
          (write-markdown-report (str reports-dir "/clojure-all.md") 
                                 "Clojure Lint Report (All Issues)" 
                                 clj-issues)
          
          (doseq [[level issues] (group-by-severity clj-issues)]
            (write-markdown-report (str reports-dir "/clojure-" (name level) ".md")
                                   (str "Clojure Lint Report (" (str/capitalize (name level)) "s)")
                                   issues))
          
          ;; Write Kotlin reports
          (println "Writing Kotlin reports...")
          (write-markdown-report (str reports-dir "/kotlin-all.md")
                                 "Kotlin Lint Report (All Issues)"
                                 all-kotlin-issues)
          
          (write-markdown-report (str reports-dir "/kotlin-ktlint.md")
                                 "Kotlin ktlint Report"
                                 ktlint-issues)
          
          (write-markdown-report (str reports-dir "/kotlin-detekt.md")
                                 "Kotlin detekt Report"
                                 detekt-issues)
          
          (doseq [[level issues] (group-by-severity all-kotlin-issues)]
            (write-markdown-report (str reports-dir "/kotlin-" (name level) ".md")
                                   (str "Kotlin Lint Report (" (str/capitalize (name level)) "s)")
                                   issues))
          
          ;; Write summary
          (println "Writing summary report...")
          (write-summary-report (str reports-dir "/summary.md") clj-issues all-kotlin-issues)
          
          ;; Print summary
          (println "\n✓ Reports generated in" reports-dir)
          (println (str "  Clojure issues: " (count clj-issues)))
          (println (str "  Kotlin issues: " (count all-kotlin-issues)))
          (println (str "  Total issues: " (+ (count clj-issues) (count all-kotlin-issues)))))))))

(main)