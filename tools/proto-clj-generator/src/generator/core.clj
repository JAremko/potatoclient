(ns generator.core
  "Core generator that orchestrates backend and frontend."
  (:require [generator.backend :as backend]
            [generator.frontend :as frontend]
            [generator.frontend-namespaced :as frontend-ns]
            [generator.dependency-graph :as dep-graph]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [clojure.pprint :as pp]
            [cljfmt.core :as cljfmt]
            [zprint.core :as zp]))

(defn write-edn-debug
  "Write EDN intermediate representation for debugging."
  [edn-data output-dir filename]
  (let [debug-dir (io/file output-dir "debug")
        file (io/file debug-dir filename)]
    (.mkdirs debug-dir)
    (spit file (with-out-str (pp/pprint edn-data)))
    (log/info "Wrote EDN debug to" (.getPath file))))

(defn generate-all
  "Generate Clojure code using Specter backend and template-based frontend.
  Options:
  - :debug? - Write debug EDN files (default true)
  - :line-width - Line width for formatting (default 80)"
  [{:keys [input-dir output-dir namespace-prefix debug? line-width]
    :or {debug? true
         line-width 80}}]
  (try
    (log/info "Starting code generation")
    (log/info "Input directory:" input-dir)
    (log/info "Output directory:" output-dir)
    (log/info "Namespace prefix:" namespace-prefix)
    
    ;; Step 1: Parse descriptors to EDN using Backend
    (log/info "Backend: Parsing descriptors to EDN...")
    (let [backend-output (backend/parse-all-descriptors input-dir)]
      
      ;; Step 2: Debug output
      (when debug?
        (log/info "Writing EDN representation for debugging...")
        (write-edn-debug (:command backend-output) output-dir "command-edn.edn")
        (write-edn-debug (:state backend-output) output-dir "state-edn.edn")
        (write-edn-debug (:type-lookup backend-output) output-dir "type-lookup.edn")
        ;; Also write dependency graph
        (let [enriched (dep-graph/analyze-dependencies backend-output namespace-prefix)]
          (write-edn-debug (:dependency-graph enriched) output-dir "dependency-graph.edn")))
      
      ;; Step 3: Generate Clojure code using Frontend
      (log/info "Frontend: Generating Clojure code from EDN...")
      (log/info "Using separated namespace mode")
      
      ;; Always use namespaced frontend
      (let [;; Analyze dependencies and enrich backend output
              enriched-backend-output (dep-graph/analyze-dependencies backend-output namespace-prefix)
              generated (frontend-ns/generate-from-backend enriched-backend-output namespace-prefix)]
          (log/info "Formatting and writing generated code...")
          (let [format-opts {:indents cljfmt/default-indents
                           :alias-map {}}
                zprint-opts {:width line-width}
                ns-path (str/replace namespace-prefix #"\." "/")
                written-files (atom [])]
            
            ;; Write each generated file
            (doseq [[file-path content] generated]
              (let [full-path (io/file output-dir ns-path file-path)
                    ;; Format with cljfmt first
                    cljfmt-formatted (cljfmt/reformat-string content format-opts)
                    ;; Then format with zprint for line width
                    formatted (zp/zprint-file-str cljfmt-formatted (.getName full-path) zprint-opts)]
                (.mkdirs (.getParentFile full-path))
                (spit full-path formatted)
                (log/info "Generated" (.getPath full-path))
                (swap! written-files conj (str full-path))))
            
        {:success true
         :files @written-files
         :backend-output backend-output
         :mode :separated})))
    
    (catch Exception e
      (log/error e "Code generation failed")
      {:success false
       :error (.getMessage e)})))