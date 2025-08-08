(ns cmd-explorer.todo-generator
  "Generate complete TODO structure as EDN then render to Markdown"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [cmd-explorer.ts-parser :as parser]
            [clojure.tools.logging :as log])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn generate-todo-edn
  "Generate complete TODO structure as EDN data"
  [ts-dir]
  (let [functions (parser/extract-all-functions ts-dir)
        grouped (parser/group-functions-by-module functions)
        
        ;; Phase mappings
        module-phases {"cmdDayCamera" 4
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
        
        ;; Build phases structure
        phases (for [[module funcs] grouped
                     :let [phase-num (get module-phases module)]
                     :when phase-num]
                 {:phase phase-num
                  :module module
                  :title (-> module
                            (str/replace #"cmd" "")
                            (str/replace #"([a-z])([A-Z])" "$1 $2")
                            (str " Commands"))
                  :functions funcs})]
    
    ;; Return complete TODO structure
    {:generated-at (str (LocalDateTime/now))
     :total-functions (count functions)
     :modules (count grouped)
     :phases (sort-by :phase phases)}))

(defn edn->markdown-task
  "Convert a single task to markdown"
  [{:keys [name ts-name line file tasks]}]
  (let [main-line (format "- [ ] %s (%s:%d)" name file line)
        sub-tasks (map #(format "  - [ ] %s" %) (filter some? tasks))]
    (str/join "\n" (cons main-line sub-tasks))))

(defn edn->markdown-phase
  "Convert a phase to markdown"
  [{:keys [phase title functions]}]
  (let [header (format "### Phase %d: %s (%d functions)" 
                      phase title (count functions))
        function-lines (map edn->markdown-task 
                           (sort-by :line functions))]
    (str/join "\n" (cons header function-lines))))

(defn edn->markdown-todo
  "Convert complete EDN structure to markdown TODO"
  [todo-edn existing-todo-content]
  (let [lines (str/split-lines existing-todo-content)
        
        ;; Find where phases start (after Phase 3)
        phase-start-idx (or (first (keep-indexed
                                    (fn [idx line]
                                      (when (re-find #"^### Phase 3:" line)
                                        (+ idx 10))) ; Skip Phase 3 content
                                    lines))
                           200)
        
        ;; Keep everything before Phase 4
        before-phases (take phase-start-idx lines)
        
        ;; Generate all phase sections
        phase-sections (map edn->markdown-phase (:phases todo-edn))
        
        ;; Combine
        all-lines (concat before-phases
                         [""]
                         (interpose "" phase-sections))]
    
    (str/join "\n" all-lines)))

(defn save-edn-backup
  "Save EDN structure to file for debugging"
  [todo-edn]
  (let [timestamp (.format (LocalDateTime/now) 
                          (DateTimeFormatter/ofPattern "yyyyMMdd_HHmmss"))
        backup-file (io/file (str "bkp/todo_" timestamp ".edn"))]
    (io/make-parents backup-file)
    (spit backup-file (with-out-str (pp/pprint todo-edn)))
    (log/info "Saved EDN backup to" (.getPath backup-file))
    backup-file))

(defn regenerate-todo
  "Regenerate entire TODO from TypeScript sources"
  [ts-dir]
  (log/info "Generating complete TODO structure from" ts-dir)
  
  ;; Generate EDN representation
  (let [todo-edn (generate-todo-edn ts-dir)]
    
    ;; Save EDN backup
    (save-edn-backup todo-edn)
    
    ;; Read current TODO to preserve non-phase content
    (let [current-todo (slurp "todo.md")
          
          ;; Backup current TODO
          timestamp (.format (LocalDateTime/now) 
                           (DateTimeFormatter/ofPattern "yyyyMMdd_HHmmss"))
          backup-file (io/file (str "bkp/todo_" timestamp ".md"))]
      
      (io/make-parents backup-file)
      (io/copy (io/file "todo.md") backup-file)
      (log/info "Backed up TODO to" (.getPath backup-file))
      
      ;; Generate new TODO
      (let [new-todo (edn->markdown-todo todo-edn current-todo)]
        (spit "todo.md" new-todo)
        (log/info "Generated TODO with" (:total-functions todo-edn) "functions in" 
                 (count (:phases todo-edn)) "phases"))
      
      ;; Return summary
      {:total-functions (:total-functions todo-edn)
       :modules (:modules todo-edn)
       :phases (count (:phases todo-edn))
       :functions-by-module (into {} 
                                  (map (fn [p] [(:module p) (count (:functions p))]) 
                                       (:phases todo-edn)))})))