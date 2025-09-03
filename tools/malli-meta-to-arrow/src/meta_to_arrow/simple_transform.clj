(ns meta-to-arrow.simple-transform
  "Simplified transformation using string manipulation for reliability."
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(defn extract-defn-parts
  "Extract parts of a defn form from string."
  [defn-str]
  (try
    (let [form (edn/read-string defn-str)
          [defn-sym name & rest] form
          ;; Check for docstring
          [docstring rest] (if (string? (first rest))
                              [(first rest) (rest rest)]
                              [nil rest])
          ;; Check for metadata
          [metadata rest] (if (map? (first rest))
                            [(first rest) (rest rest)]
                            [nil rest])]
      {:defn-sym defn-sym
       :name name
       :docstring docstring
       :metadata metadata
       :body rest
       :schema (when metadata (:malli/schema metadata))})
    (catch Exception _ nil)))

(defn rebuild-defn
  "Rebuild defn string without malli/schema in metadata."
  [{:keys [defn-sym name docstring metadata body schema]}]
  (let [cleaned-meta (when metadata
                       (let [m (dissoc metadata :malli/schema)]
                         (when (seq m) m)))
        parts (cond-> [(str "(" defn-sym) (str name)]
                docstring (conj (pr-str docstring))
                cleaned-meta (conj (pr-str cleaned-meta)))
        body-str (str/join " " (map pr-str body))]
    (str (str/join " " parts) " " body-str ")")))

(defn create-arrow-str
  "Create m/=> declaration string."
  [name schema require-alias]
  (str "(" require-alias "/=> " name " " (pr-str schema) ")"))

(defn process-defn-string
  "Process a single defn form string."
  [defn-str require-alias]
  (if-let [parts (extract-defn-parts defn-str)]
    (if (:schema parts)
      {:transformed (rebuild-defn parts)
       :arrow (create-arrow-str (:name parts) (:schema parts) require-alias)
       :name (:name parts)}
      {:original defn-str})
    {:original defn-str}))

(defn find-defns-in-string
  "Find all defn forms in a string using regex."
  [content]
  (let [;; Match (defn or (defn- followed by content until balanced parens
        pattern #"(?s)\(defn-?\s+[^)]*\)[^(]*(?:\([^)]*\)[^(]*)*"
        matches (re-seq pattern content)]
    (map first matches)))

(defn transform-content
  "Transform content by finding and replacing defns."
  [content {:keys [require-alias] :or {require-alias "m"}}]
  (let [;; Split content into forms
        lines (str/split-lines content)
        ns-end-idx (if-let [idx (first (keep-indexed 
                                         #(when (and (str/includes? %2 "(ns ")
                                                     (or (str/includes? %2 ")")
                                                         (str/includes? (get lines (inc %1) "") ")")))
                                            %1)
                                         lines))]
                     (if (str/includes? (get lines idx) ")")
                       idx
                       (inc idx))
                     -1)
        
        ;; Process content line by line, collecting defns
        result (loop [idx 0
                      output []
                      in-defn? false
                      defn-lines []
                      transformed-count 0]
                 (if (>= idx (count lines))
                   {:output output
                    :count transformed-count
                    :needs-require? (> transformed-count 0)}
                   (let [line (get lines idx)]
                     (cond
                       ;; Start of defn
                       (and (not in-defn?)
                            (or (str/starts-with? (str/trim line) "(defn ")
                                (str/starts-with? (str/trim line) "(defn-")))
                       (recur (inc idx) output true [line] transformed-count)
                       
                       ;; In defn, collect lines
                       in-defn?
                       (let [new-defn-lines (conj defn-lines line)
                             defn-str (str/join "\n" new-defn-lines)
                             ;; Check if defn is complete (balanced parens)
                             open-parens (count (re-seq #"\(" defn-str))
                             close-parens (count (re-seq #"\)" defn-str))]
                         (if (= open-parens close-parens)
                           ;; Defn complete, process it
                           (let [result (process-defn-string defn-str require-alias)]
                             (if (:arrow result)
                               (recur (inc idx)
                                      (conj output (:transformed result)
                                            (:arrow result))
                                      false
                                      []
                                      (inc transformed-count))
                               (recur (inc idx)
                                      (conj output defn-str)
                                      false
                                      []
                                      transformed-count)))
                           ;; Continue collecting
                           (recur (inc idx) output true new-defn-lines transformed-count)))
                       
                       ;; Regular line
                       :else
                       (recur (inc idx) (conj output line) false [] transformed-count)))))
        
        ;; Add require if needed
        final-lines (if (:needs-require? result)
                      (let [lines (:output result)]
                        (if (>= ns-end-idx 0)
                          ;; Insert require after ns form
                          (let [[before after] (split-at (inc ns-end-idx) lines)
                                ns-line (last before)]
                            (if (str/includes? ns-line ":require")
                              ;; Add to existing require
                              (let [new-ns (str/replace ns-line
                                                        #"\)"
                                                        (str "\n            [malli.core :as " 
                                                             require-alias "])"))]
                                (concat (butlast before) [new-ns] after))
                              ;; Add new require clause
                              (let [new-require (str "  (:require [malli.core :as " 
                                                     require-alias "])")]
                                (concat (butlast before)
                                        [(str/replace ns-line #"\)$" "")]
                                        [new-require ")"]
                                        after))))
                          lines))
                      (:output result))]
    
    {:transformed (str/join "\n" final-lines)
     :count (:count result)}))

(defn transform-file
  "Main entry point matching the expected interface."
  [content options]
  (transform-content content options))