(ns generator.core
  "Core generator that orchestrates backend and frontend."
  (:require [generator.backend :as backend]
            [generator.frontend :as frontend]
            [generator.frontend-namespaced :as frontend-ns]
            [generator.dependency-graph :as dep-graph]
            [generator.deps :as deps]
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
  - :namespace-mode - :separated (default, one file per package) or :single (one file per domain)
  - :debug? - Write debug EDN files (default true)
  - :line-width - Line width for formatting (default 80)"
  [{:keys [input-dir output-dir namespace-prefix namespace-mode debug? line-width guardrails?]
    :or {namespace-mode :separated  ;; Default to separated namespaces
         debug? true
         line-width 80
         guardrails? false}}]
  (try
    (log/info "Starting code generation")
    (log/info "Input directory:" input-dir)
    (log/info "Output directory:" output-dir)
    (log/info "Namespace prefix:" namespace-prefix)
    
    ;; Step 1: Parse descriptors to EDN using Backend
    (log/info "Backend: Parsing descriptors to EDN...")
    (let [backend-output (backend/parse-all-descriptors input-dir)
          ;; Enrich the descriptor sets with type resolution
          enriched-command (deps/enrich-descriptor-set (:command backend-output))
          enriched-state (deps/enrich-descriptor-set (:state backend-output))
          ;; Replace the original descriptor sets with enriched versions
          enriched-backend-output (assoc backend-output
                                         :command enriched-command
                                         :state enriched-state)]
      
      ;; Step 2: Debug output
      (when debug?
        (log/info "Writing EDN representation for debugging...")
        (write-edn-debug (:command enriched-backend-output) output-dir "command-edn.edn")
        (write-edn-debug (:state enriched-backend-output) output-dir "state-edn.edn")
        (write-edn-debug (:type-lookup enriched-backend-output) output-dir "type-lookup.edn")
        ;; Also write dependency graph if using separated mode
        (when (= namespace-mode :separated)
          (let [enriched (dep-graph/analyze-dependencies enriched-backend-output namespace-prefix)]
            (write-edn-debug (:dependency-graph enriched) output-dir "dependency-graph.edn"))))
      
      ;; Step 3: Generate Clojure code using Frontend
      (log/info "Frontend: Generating Clojure code from EDN...")
      (log/info "Using namespace mode:" namespace-mode)
      
      (if (= namespace-mode :separated)
        ;; Use namespaced frontend for separated mode
        (let [;; Analyze dependencies and add clj-requires
              final-backend-output (dep-graph/analyze-dependencies enriched-backend-output namespace-prefix)
              generated (frontend-ns/generate-from-backend final-backend-output namespace-prefix guardrails?)]
          (log/info "Formatting and writing generated code...")
          (let [format-opts {:indents cljfmt/default-indents
                           :alias-map {}}
                zprint-opts {:width line-width}
                ns-path (str/replace namespace-prefix #"\." "/")
                written-files (atom [])]
            
            ;; Write each generated file
            (doseq [[file-path content] generated]
              (let [full-path (io/file output-dir ns-path file-path)
                    ;; Try to format with cljfmt first, but if it fails, use the content as-is
                    cljfmt-formatted (try
                                       (cljfmt/reformat-string content format-opts)
                                       (catch Exception e
                                         (log/warn "cljfmt failed for" file-path "-" (.getMessage e))
                                         (log/warn "Problematic code snippet:" (subs content 0 (min 500 (count content))))
                                         content))
                    ;; Then format with zprint for line width
                    formatted (try
                                (zp/zprint-file-str cljfmt-formatted (.getName full-path) zprint-opts)
                                (catch Exception e
                                  (log/warn "zprint failed for" file-path "-" (.getMessage e))
                                  cljfmt-formatted))]
                (.mkdirs (.getParentFile full-path))
                (spit full-path formatted)
                (log/info "Generated" (.getPath full-path))
                (swap! written-files conj (str full-path))))
            
            {:success true
             :files @written-files
             :backend-output enriched-backend-output
             :mode namespace-mode}))
        
        ;; Original single-file mode
        (let [generated (frontend/generate-from-backend enriched-backend-output namespace-prefix guardrails?)]
          ;; Step 4: Format generated code
          (log/info "Formatting generated code...")
          (let [format-opts {:indents cljfmt/default-indents
                            :alias-map {}}
                zprint-opts {:width line-width}
                ;; Format with cljfmt first, then zprint for line width
                cljfmt-command (try
                                 (cljfmt/reformat-string (:command generated) format-opts)
                                 (catch Exception e
                                   (log/warn "cljfmt failed for command.clj -" (.getMessage e))
                                   (:command generated)))
                cljfmt-state (try
                               (cljfmt/reformat-string (:state generated) format-opts)
                               (catch Exception e
                                 (log/warn "cljfmt failed for state.clj -" (.getMessage e))
                                 (:state generated)))
                formatted-command (try
                                    (zp/zprint-file-str cljfmt-command "command.clj" zprint-opts)
                                    (catch Exception e
                                      (log/warn "zprint failed for command.clj -" (.getMessage e))
                                      cljfmt-command))
                formatted-state (try
                                  (zp/zprint-file-str cljfmt-state "state.clj" zprint-opts)
                                  (catch Exception e
                                    (log/warn "zprint failed for state.clj -" (.getMessage e))
                                    cljfmt-state))]
            
            ;; Step 5: Write generated code
            (log/info "Writing generated code...")
            (.mkdirs (io/file output-dir namespace-prefix))
            
            (let [ns-path (str/replace namespace-prefix #"\." "/")
                  cmd-file (io/file output-dir 
                                   (str ns-path "/command.clj"))
                  state-file (io/file output-dir 
                                     (str ns-path "/state.clj"))]
              
              (.mkdirs (.getParentFile cmd-file))
              (.mkdirs (.getParentFile state-file))
              
              (spit cmd-file formatted-command)
              (spit state-file formatted-state)
              
              (log/info "Generated" (.getPath cmd-file))
              (log/info "Generated" (.getPath state-file))
              
              {:success true
               :files [(str cmd-file) (str state-file)]
               :backend-output backend-output
               :mode namespace-mode})))))
    
    (catch Exception e
      (log/error e "Code generation failed")
      {:success false
       :error (.getMessage e)})))