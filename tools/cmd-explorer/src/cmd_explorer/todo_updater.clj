(ns cmd-explorer.todo-updater
  "Update TODO.md with extracted TypeScript functions"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cmd-explorer.ts-parser :as parser]
            [clojure.tools.logging :as log])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn backup-todo
  "Create a backup of the current TODO.md"
  []
  (let [todo-file (io/file "todo.md")
        timestamp (.format (LocalDateTime/now) 
                          (DateTimeFormatter/ofPattern "yyyyMMdd_HHmmss"))
        backup-file (io/file (str "bkp/todo_" timestamp ".md"))]
    (when (.exists todo-file)
      (io/make-parents backup-file)
      (io/copy todo-file backup-file)
      (log/info "Backed up TODO to" (.getPath backup-file))
      backup-file)))

(defn format-function-entry
  "Format a function entry with all its subtasks"
  [{:keys [name ts-name line file tasks]}]
  (let [main-task (format "- [ ] %s (%s:%d)" name file line)
        sub-tasks (map #(format "  - [ ] %s" %) (filter some? tasks))]
    (str/join "\n" (cons main-task sub-tasks))))

(defn group-functions-by-module
  "Group functions by their source module"
  [functions]
  (reduce (fn [acc func]
            (let [module (-> (:file func)
                           (str/replace #"cmdSender/" "")
                           (str/replace #"\.ts$" ""))]
              (update acc module (fnil conj []) func)))
          {}
          functions))

(defn generate-phase-section
  "Generate a complete phase section for a module"
  [phase-num module-name functions]
  (let [header (format "### Phase %d: %s Commands (%d functions)"
                      phase-num
                      (-> module-name
                          (str/replace #"cmd" "")
                          (str/replace #"([a-z])([A-Z])" "$1 $2"))
                      (count functions))
        function-entries (map format-function-entry 
                             (sort-by :line functions))]
    (str/join "\n" (cons header function-entries))))

(defn find-phase-section
  "Find the start and end of a phase section in the TODO"
  [lines phase-pattern]
  (let [start-idx (.indexOf lines 
                           (first (filter #(re-find phase-pattern %) lines)))]
    (when (>= start-idx 0)
      (let [end-idx (or (first (keep-indexed 
                                 (fn [idx line]
                                   (when (and (> idx start-idx)
                                             (re-find #"^### Phase \d+:" line))
                                     (+ start-idx idx)))
                                 (drop start-idx lines)))
                       (count lines))]
        {:start start-idx :end end-idx}))))

(defn update-todo-section
  "Update a specific section in the TODO"
  [todo-content phase-num module-name functions]
  (let [lines (str/split-lines todo-content)
        ;; Try to find exact module match first
        phase-pattern (re-pattern (str "^### Phase \\d+:.*" 
                                      (-> module-name
                                          (str/replace #"cmd" "")
                                          (str/replace #"([a-z])([A-Z])" "$1.*$2"))))
        section (find-phase-section lines phase-pattern)]
    
    (if section
      ;; Replace existing section
      (let [new-section (generate-phase-section phase-num module-name functions)
            before (take (:start section) lines)
            after (drop (:end section) lines)]
        (str/join "\n" (concat before [new-section] after)))
      
      ;; Add new section (find appropriate location based on phase number)
      (let [;; Find where to insert based on phase numbering
            insertion-point (or 
                             ;; Find the first phase with a higher number
                             (first (keep-indexed
                                     (fn [idx line]
                                       (when-let [match (re-find #"^### Phase (\d+):" line)]
                                         (let [found-phase (Integer/parseInt (second match))]
                                           (when (> found-phase phase-num)
                                             idx))))
                                     lines))
                             ;; Otherwise add at the end
                             (count lines))
            new-section (generate-phase-section phase-num module-name functions)
            before (take insertion-point lines)
            after (drop insertion-point lines)]
        (str/join "\n" (concat before ["" new-section] after))))))

(defn update-todo-with-functions
  "Update TODO.md with extracted functions"
  [ts-dir]
  (log/info "Extracting functions from" ts-dir)
  
  ;; Backup current TODO
  (backup-todo)
  
  ;; Extract functions
  (let [functions (parser/extract-all-functions ts-dir)
        grouped (group-functions-by-module functions)
        todo-content (slurp "todo.md")]
    
    (log/info "Found" (count functions) "functions in" (count grouped) "modules")
    
    ;; Update TODO for each module
    (let [module-phases {"cmdDayCamera" 4
                        "cmdHeatCamera" 5
                        "cmdRotary" 6
                        "cmdLRF" 7
                        "cmdCompass" 8
                        "cmdGps" 9
                        "cmdSystem" 10
                        "cmdOSD" 11
                        "cmdCamDayGlassHeater" 12
                        "cmdCV" 13
                        "cmdLRFAlignment" 14}
          
          updated-content
          (reduce (fn [content [module funcs]]
                    (if-let [phase (get module-phases module)]
                      (do
                        (log/info "Updating Phase" phase "for" module 
                                 "with" (count funcs) "functions")
                        (update-todo-section content phase module funcs))
                      (do
                        (log/warn "No phase mapping for module" module)
                        content)))
                  todo-content
                  grouped)]
      
      ;; Write updated TODO
      (spit "todo.md" updated-content)
      (log/info "Updated TODO.md successfully")
      
      ;; Return summary
      {:total-functions (count functions)
       :modules (count grouped)
       :functions-by-module (into {} (map (fn [[k v]] [k (count v)]) grouped))})))

(defn validate-generated-tasks
  "Validate that generated tasks reference valid files and proto messages"
  [ts-dir]
  (let [functions (parser/extract-all-functions ts-dir)
        ts-base-dir (io/file ts-dir)
        proto-explorer (io/file "../../proto-explorer/proto-explorer")]
    
    (log/info "Validating" (count functions) "functions")
    
    (doseq [{:keys [name file line proto-msg]} functions]
      ;; Validate TypeScript file exists
      (let [ts-file (io/file ts-base-dir "cmdSender" file)]
        (when-not (.exists ts-file)
          (log/error "TypeScript file not found:" (.getPath ts-file))))
      
      ;; Validate line number is reasonable
      (when (< line 1)
        (log/error "Invalid line number for" name ":" line))
      
      ;; Log proto message for manual verification
      (log/debug "Function" name "maps to proto" proto-msg))
    
    (log/info "Validation complete")
    true))