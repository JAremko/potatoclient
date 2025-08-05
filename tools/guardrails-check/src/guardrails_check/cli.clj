(ns guardrails-check.cli
  "Babashka CLI interface for guardrails-check"
  (:require [guardrails-check.core :as core]
            [clojure.string :as str]
            [clojure.pprint :as pp]))

(defn- output-edn
  "Output data as EDN to stdout"
  [data]
  (pp/pprint data))

(defn check
  "Check for functions without Guardrails in the given source directory"
  [args]
  (let [src-dir (or (first args) "src/potatoclient")
        report (core/generate-report src-dir)]
    (output-edn report)))

(defn report
  "Generate a markdown report of functions without Guardrails"
  [args]
  (let [src-dir (or (first args) "src/potatoclient")
        report (core/generate-report src-dir)
        formatted (core/format-report report)]
    (println (str/join "\n" formatted))))

(defn stats
  "Show statistics about Guardrails usage"
  [args]
  (let [src-dir (or (first args) "src/potatoclient")
        report (core/generate-report src-dir)
        summary (:summary report)]
    (output-edn {:stats {:unspecced-functions (:total-unspecced-functions summary)
                         :affected-namespaces (:total-namespaces-with-issues summary)
                         :percentage (if (zero? (:total-unspecced-functions summary))
                                      100.0
                                      0.0)}})))

(defn list-namespaces
  "List namespaces with functions not using Guardrails"
  [args]
  (let [src-dir (or (first args) "src/potatoclient")
        report (core/generate-report src-dir)
        namespaces (-> report :summary :namespaces)]
    (output-edn {:namespaces (vec namespaces)})))

(defn find-function
  "Find specific functions not using Guardrails"
  [args]
  (if-let [pattern (first args)]
    (let [src-dir (or (second args) "src/potatoclient")
          report (core/generate-report src-dir)
          details (:details report)
          matches (for [[ns funcs] details
                        func funcs
                        :when (str/includes? (str func) pattern)]
                    {:namespace ns :function func})]
      (output-edn {:pattern pattern
                   :matches matches
                   :count (count matches)}))
    (output-edn {:error "Usage: bb find <pattern> [src-dir]"})))