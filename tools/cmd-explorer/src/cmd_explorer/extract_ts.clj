(ns cmd-explorer.extract-ts
  "Main entry point for TypeScript function extraction and TODO update"
  (:require [cmd-explorer.todo-updater :as updater]
            [cmd-explorer.todo-generator :as generator]
            [cmd-explorer.ts-parser :as parser]
            [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:gen-class))

(def cli-options
  [["-t" "--ts-dir DIR" "TypeScript source directory"
    :default "../../examples/web/frontend/ts/cmd"
    :validate [#(.exists (clojure.java.io/file %)) "Directory must exist"]]
   ["-v" "--validate" "Validate generated tasks only"]
   ["-d" "--dry-run" "Show what would be updated without modifying TODO"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["TypeScript Function Extractor for CMD-Explorer"
        ""
        "Usage: clojure -M:extract [options]"
        ""
        "Options:"
        options-summary
        ""
        "This tool:"
        "  1. Parses TypeScript cmd files"
        "  2. Extracts function signatures"
        "  3. Generates TODO tasks with proto-explorer commands"
        "  4. Updates TODO.md with proper sub-tasks"
        "  5. Creates backups before modifications"]
       (str/join \newline)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (do (println (usage summary))
          (System/exit 0))
      
      errors
      (do (println "Errors:" (str/join \newline errors))
          (println (usage summary))
          (System/exit 1))
      
      (:validate options)
      (do (log/info "Validating generated tasks...")
          (updater/validate-generated-tasks (:ts-dir options))
          (System/exit 0))
      
      (:dry-run options)
      (do (log/info "Dry run - showing extracted functions...")
          (let [functions (parser/extract-all-functions (:ts-dir options))
                grouped (updater/group-functions-by-module functions)]
            (doseq [[module funcs] grouped]
              (println "\n" module ":" (count funcs) "functions")
              (doseq [{:keys [name line]} funcs]
                (println "  -" name "(line" line ")"))))
          (System/exit 0))
      
      :else
      (do (log/info "Regenerating TODO with TypeScript functions...")
          (let [result (generator/regenerate-todo (:ts-dir options))]
            (println "\nUpdate complete!")
            (println "Total functions:" (:total-functions result))
            (println "Phases:" (:phases result))
            (println "\nFunctions by module:")
            (doseq [[module count] (:functions-by-module result)]
              (println "  " module ":" count))
            (System/exit 0))))))