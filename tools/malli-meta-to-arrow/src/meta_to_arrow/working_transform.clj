(ns meta-to-arrow.working-transform
  "Working transformation using rewrite-clj properly."
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

(defn has-malli-schema?
  "Check if a metadata map has :malli/schema key."
  [metadata]
  (and (map? metadata)
       (contains? metadata :malli/schema)))

(defn process-defn-at-loc
  "Process a defn form at the given location."
  [loc require-alias]
  (let [fn-name-loc (-> loc z/down z/right)
        fn-name (z/sexpr fn-name-loc)
        after-name (z/right fn-name-loc)]
    
    ;; Check for metadata in various positions
    (cond
      ;; Direct metadata after name
      (and after-name 
           (map? (z/sexpr after-name))
           (has-malli-schema? (z/sexpr after-name)))
      (let [metadata (z/sexpr after-name)
            schema (:malli/schema metadata)
            cleaned-meta (dissoc metadata :malli/schema)
            ;; Update or remove metadata
            loc-updated (if (seq cleaned-meta)
                          (z/replace after-name (n/coerce cleaned-meta))
                          (z/remove after-name))
            ;; Navigate back up to defn form
            defn-loc (loop [l loc-updated]
                       (if (and (z/list? l)
                                (let [first (z/down l)]
                                  (and first (#{'defn 'defn-} (z/sexpr first)))))
                         l
                         (z/up l)))
            ;; Create arrow form
            arrow-node (n/list-node
                        [(n/token-node (symbol require-alias "=>"))
                         (n/spaces 1)
                         (n/token-node fn-name)
                         (n/spaces 1)
                         (n/coerce schema)])]
        {:loc (-> defn-loc
                  (z/insert-right arrow-node)
                  (z/insert-right (n/newlines 1)))
         :transformed? true
         :name fn-name})
      
      ;; Docstring then metadata
      (and after-name
           (string? (z/sexpr after-name)))
      (let [after-doc (z/right after-name)]
        (if (and after-doc
                 (map? (z/sexpr after-doc))
                 (has-malli-schema? (z/sexpr after-doc)))
          (let [metadata (z/sexpr after-doc)
                schema (:malli/schema metadata)
                cleaned-meta (dissoc metadata :malli/schema)
                ;; Update or remove metadata
                loc-updated (if (seq cleaned-meta)
                              (z/replace after-doc (n/coerce cleaned-meta))
                              (z/remove after-doc))
                ;; Navigate back up to defn form
                defn-loc (loop [l loc-updated]
                           (if (and (z/list? l)
                                    (let [first (z/down l)]
                                      (and first (#{'defn 'defn-} (z/sexpr first)))))
                             l
                             (z/up l)))
                ;; Create arrow form
                arrow-node (n/list-node
                            [(n/token-node (symbol require-alias "=>"))
                             (n/spaces 1)
                             (n/token-node fn-name)
                             (n/spaces 1)
                             (n/coerce schema)])]
            {:loc (-> defn-loc
                      (z/insert-right arrow-node)
                      (z/insert-right (n/newlines 1)))
             :transformed? true
             :name fn-name})
          {:loc loc :transformed? false}))
      
      :else
      {:loc loc :transformed? false})))

(defn add-malli-require
  "Add malli.core require if not present."
  [content require-alias]
  ;; Parse content to find ns form
  (let [lines (str/split-lines content)]
    (if-let [ns-idx (first (keep-indexed 
                            #(when (str/starts-with? (str/trim %2) "(ns ") %1)
                            lines))]
      ;; Find end of ns form
      (let [ns-end-idx (loop [idx ns-idx
                              paren-count 0]
                         (if (>= idx (count lines))
                           idx
                           (let [line (get lines idx)
                                 new-count (+ paren-count
                                             (count (re-seq #"\(" line))
                                             (- (count (re-seq #"\)" line))))]
                             (if (and (> idx ns-idx) (zero? new-count))
                               idx
                               (recur (inc idx) new-count)))))
            ;; Check if malli.core already required
            ns-content (str/join "\n" (take (inc ns-end-idx) lines))]
        (if (str/includes? ns-content "malli.core")
          content  ; Already has malli.core
          ;; Add require
          (let [require-line (str "            [malli.core :as " require-alias "]")
                updated-lines (if (str/includes? ns-content ":require")
                                ;; Add to existing require
                                (vec (map-indexed
                                      (fn [idx line]
                                        (if (and (>= idx ns-idx)
                                                 (<= idx ns-end-idx)
                                                 (str/includes? line ":require"))
                                          (str/replace line ":require" 
                                                       (str ":require\n" require-line))
                                          line))
                                      lines))
                                ;; Add new require clause
                                (let [[before after] (split-at ns-end-idx lines)
                                      ns-last (last before)
                                      updated-ns (str/replace ns-last #"\)$" 
                                                              (str "\n  (:require\n" 
                                                                   require-line "))"))]
                                  (vec (concat (butlast before) [updated-ns] after))))]
            (str/join "\n" updated-lines))))
      content)))

(defn transform-file
  "Main transformation entry point."
  [content {:keys [require-alias] :or {require-alias "m"}}]
  (let [zloc (z/of-string content)
        transformed-count (atom 0)
        transformed-names (atom [])
        
        ;; Process all defns
        result-zloc (loop [loc zloc]
                      (if (z/end? loc)
                        loc
                        (if (and (z/list? loc)
                                 (let [first-elem (z/down loc)]
                                   (and first-elem
                                        (#{'defn 'defn-} (z/sexpr first-elem)))))
                          ;; Process this defn
                          (let [result (process-defn-at-loc loc require-alias)]
                            (if (:transformed? result)
                              (do
                                (swap! transformed-count inc)
                                (swap! transformed-names conj (:name result))
                                (recur (z/next (:loc result))))
                              (recur (z/next loc))))
                          ;; Not a defn, continue
                          (recur (z/next loc)))))
        
        ;; Get transformed content
        transformed-content (z/root-string result-zloc)
        
        ;; Add require if needed
        final-content (if (pos? @transformed-count)
                        (add-malli-require transformed-content require-alias)
                        transformed-content)]
    
    {:transformed final-content
     :count @transformed-count
     :additions (map (fn [n] {:name n}) @transformed-names)}))