#!/usr/bin/env bb

(ns lint-report
  (:require [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(defn run-clj-kondo []
  (println "Running clj-kondo...")
  (let [result (shell/sh "clojure" "-M:lint" "--lint" "src" "test" "--config" "{:output {:format :edn}}")]
    (if (= 0 (:exit result))
      {:findings [] :summary {:error 0 :warning 0 :info 0}}
      (try
        (edn/read-string (:out result))
        (catch Exception e
          (println "Error parsing clj-kondo output:" (.getMessage e))
          {:findings [] :summary {:error 0 :warning 0 :info 0}})))))

(defn parse-ktlint-line [line]
  (when-let [[_ file line-col message rule] (re-matches #"([^:]+):(\d+:\d+): (.+) \((.+)\)" line)]
    (let [[line col] (str/split line-col #":")]
      {:filename file
       :row (parse-long line)
       :col (parse-long col)
       :message message
       :rule rule
       :level :warning})))

(defn run-ktlint []
  (println "Running ktlint...")
  (if (fs/exists? ".ktlint/ktlint")
    (let [result (shell/sh ".ktlint/ktlint" "src/potatoclient/kotlin/**/*.kt" "--reporter=plain")]
      (let [findings (keep parse-ktlint-line (str/split-lines (:out result)))]
        {:findings findings
         :summary {:error 0
                   :warning (count findings)
                   :info 0}}))
    (do
      (println "ktlint not found. Skipping Kotlin linting.")
      {:findings [] :summary {:error 0 :warning 0 :info 0}})))

(defn format-clojure-section [clj-issues]
  (str/join "\n"
            ["### Clojure Files"
             ""
             (str/join "\n\n"
                       (for [[file issues] (sort (group-by :filename clj-issues))]
                         (str/join "\n"
                                   [(format "#### %s (%d issues)" file (count issues))
                                    ""
                                    (str/join "\n"
                                              (for [issue (sort-by :row issues)]
                                                (format "- **Line %d:%d** [%s] %s"
                                                        (:row issue)
                                                        (:col issue)
                                                        (name (:level issue))
                                                        (:message issue))))])))]))

(defn format-kotlin-section [kotlin-issues]
  (str/join "\n"
            ["### Kotlin Files (ktlint)"
             ""
             (str/join "\n\n"
                       (for [[file issues] (sort (group-by :filename kotlin-issues))]
                         (str/join "\n"
                                   [(format "#### %s (%d issues)" file (count issues))
                                    ""
                                    (str/join "\n"
                                              (for [issue (sort-by :row issues)]
                                                (format "- **Line %d:%d** %s (%s)"
                                                        (:row issue)
                                                        (:col issue)
                                                        (:message issue)
                                                        (:rule issue))))])))]))

(defn format-md-report [clj-findings kotlin-findings no-errors?]
  (let [filter-fn (if no-errors?
                    #(filter (fn [issue] (not= :error (:level issue))) %)
                    identity)
        clj-issues (filter-fn (:findings clj-findings))
        kotlin-issues (filter-fn (:findings kotlin-findings))
        total-errors (get-in clj-findings [:summary :error] 0)
        total-warnings (+ (get-in clj-findings [:summary :warning] 0)
                          (get-in kotlin-findings [:summary :warning] 0))
        sections []]
    (str/join "\n"
              (concat
               ["# Lint Report"
                ""
                (format "Generated: %s" (str (java.time.LocalDateTime/now)))
                ""
                "## Summary"
                ""
                (format "- **Total Errors**: %d" total-errors)
                (format "- **Total Warnings**: %d" total-warnings)
                ""
                "### Clojure (clj-kondo)"
                (format "- Errors: %d" (get-in clj-findings [:summary :error] 0))
                (format "- Warnings: %d" (get-in clj-findings [:summary :warning] 0))
                ""
                "### Kotlin (ktlint)"
                (format "- Style violations: %d" (count kotlin-issues))
                ""
                "## Issues by File"
                ""]
               (when (seq clj-issues)
                 [(format-clojure-section clj-issues)])
               (when (seq kotlin-issues)
                 ["" (format-kotlin-section kotlin-issues)])))))

(defn -main [& args]
  (fs/create-dirs "reports")
  (let [no-errors? (some #{"--no-errors"} args)
        clj-only? (some #{"--clj-only"} args)
        kotlin-only? (some #{"--kotlin-only"} args)
        output (if (some #{"--output"} args)
                 (second (drop-while #(not= "--output" %) args))
                 "reports/lint-report.md")
        clj-findings (if kotlin-only?
                       {:findings [] :summary {:error 0 :warning 0}}
                       (run-clj-kondo))
        kotlin-findings (if clj-only?
                          {:findings [] :summary {:error 0 :warning 0}}
                          (run-ktlint))
        report (format-md-report clj-findings kotlin-findings no-errors?)]
    (spit output report)
    (println (format "Report written to %s" output))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))