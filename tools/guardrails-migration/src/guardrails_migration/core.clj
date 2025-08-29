(ns guardrails-migration.core
  "Automated migration tool from Guardrails >defn to defn with Malli metadata"
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [rewrite-clj.parser :as p]
            [guardrails-migration.specs :as specs]))

;; ============================================================================
;; Gspec parsing
;; ============================================================================

(defn parse-gspec
  "Parse a Guardrails gspec vector into components"
  [gspec]
  (when (and (vector? gspec) (some #(= '=> %) gspec))
    (let [arrow-idx (.indexOf gspec '=>)
          args-part (subvec gspec 0 arrow-idx)
          ret-part (subvec gspec (inc arrow-idx))
          
          ;; Check for such-that in args
          pipe-idx (when (seq args-part) (.indexOf args-part '|))
          [args arg-preds] (if (and pipe-idx (>= pipe-idx 0))
                              [(subvec args-part 0 pipe-idx)
                               (subvec args-part (inc pipe-idx))]
                              [args-part nil])
          
          ;; Check for such-that in return
          ret-pipe-idx (when (seq ret-part) (.indexOf ret-part '|))
          [ret ret-preds] (if (and ret-pipe-idx (>= ret-pipe-idx 0))
                            [(first (subvec ret-part 0 ret-pipe-idx))
                             (subvec ret-part (inc ret-pipe-idx))]
                            [(first ret-part) nil])
          
          ;; Convert (? spec) to [:maybe spec]
          args (mapv (fn [spec]
                       (if (and (list? spec) (= '? (first spec)))
                         [:maybe (second spec)]
                         spec))
                     args)]
      (cond-> {:args args :ret ret}
        arg-preds (assoc :arg-preds arg-preds)
        ret-preds (assoc :ret-preds ret-preds)))))

(defn spec->malli
  "Convert a Guardrails spec to Malli spec"
  [spec]
  (cond
    ;; Already a Malli spec (keyword or vector)
    (keyword? spec) spec
    (vector? spec) spec
    
    ;; Predicate function - convert to keyword
    (and (symbol? spec) (str/ends-with? (name spec) "?"))
    (keyword (str/replace (name spec) #"\?$" ""))
    
    ;; Qualified keyword spec
    (and (symbol? spec) (namespace spec))
    (keyword (namespace spec) (name spec))
    
    ;; Other symbols stay as-is
    :else spec))

(defn gspec->malli-schema
  "Convert a gspec to a Malli :=> or :function schema"
  [gspecs]
  (if (= 1 (count gspecs))
    ;; Single arity
    (let [{:keys [args ret]} (first gspecs)
          malli-args (mapv spec->malli args)
          malli-ret (spec->malli ret)]
      [:=> (into [:cat] malli-args) malli-ret])
    ;; Multi-arity
    (into [:function]
          (for [{:keys [args ret]} gspecs]
            [:=> (into [:cat] (mapv spec->malli args)) (spec->malli ret)]))))

;; ============================================================================
;; AST manipulation with rewrite-clj
;; ============================================================================

(defn extract-defn-components
  "Extract components from a >defn form"
  [form]
  (let [defn-sym (first form)
        is-private? (= '>defn- defn-sym)
        [name-sym & rest-forms] (rest form)
        
        ;; Extract optional docstring
        [docstring rest-forms] (if (string? (first rest-forms))
                                  [(first rest-forms) (rest rest-forms)]
                                  [nil rest-forms])
        
        ;; Extract optional attr-map
        [attr-map rest-forms] (if (map? (first rest-forms))
                                 [(first rest-forms) (rest rest-forms)]
                                 [nil rest-forms])
        
        ;; Determine if single or multi-arity
        is-multi? (list? (first rest-forms))
        
        ;; Extract gspecs from each body
        gspecs-and-bodies (if is-multi?
                            ;; Multi-arity - each element is a list (args & body)
                            (for [clause rest-forms]
                              (let [[args & rest] clause
                                    [gspec rest] (if (and (vector? (first rest))
                                                         (some #(= '=> %) (first rest)))
                                                   [(first rest) (rest rest)]
                                                   [nil rest])]
                                {:args args
                                 :gspec gspec
                                 :body rest}))
                            ;; Single-arity - rest-forms is [args gspec? & body]
                            (let [[args & rest] rest-forms
                                  [gspec rest] (if (and (vector? (first rest))
                                                       (some #(= '=> %) (first rest)))
                                                 [(first rest) (rest rest)]
                                                 [nil rest])]
                              [{:args args
                                :gspec gspec
                                :body rest}]))]
    
    {:private? is-private?
     :name name-sym
     :docstring docstring
     :attr-map attr-map
     :multi? is-multi?
     :bodies gspecs-and-bodies}))

(defn build-malli-metadata
  "Build metadata map with Malli schema"
  [attr-map gspecs]
  (let [parsed-gspecs (keep #(when % (parse-gspec %)) gspecs)
        malli-schema (when (seq parsed-gspecs)
                       (gspec->malli-schema parsed-gspecs))]
    (cond-> (or attr-map {})
      malli-schema (assoc :malli/schema malli-schema))))

(defn reconstruct-defn
  "Reconstruct a defn form from components"
  [{:keys [private? name docstring attr-map multi? bodies]}]
  (let [defn-sym (if private? 'defn- 'defn)
        gspecs (map :gspec bodies)
        has-gspec? (some identity gspecs)
        metadata (when has-gspec?
                   (build-malli-metadata attr-map gspecs))
        
        ;; Build the form pieces
        form-parts (cond-> [defn-sym name]
                     docstring (conj docstring)
                     metadata (conj metadata)
                     (and attr-map (not has-gspec?)) (conj attr-map))
        
        ;; Build bodies without gspecs
        clean-bodies (if multi?
                       (for [{:keys [args body]} bodies]
                         (cons args body))
                       (let [{:keys [args body]} (first bodies)]
                         (cons args body)))]
    
    (if multi?
      (apply list (concat form-parts clean-bodies))
      (apply list (concat form-parts [clean-bodies])))))

;; ============================================================================
;; Main migration functions
;; ============================================================================

(defn migrate-form
  "Migrate a single >defn form string to defn with Malli metadata"
  [form-str]
  (try
    (let [form (read-string form-str)]
      (if (and (list? form)
               (contains? #{'> '>defn '>defn-} (first form)))
        (let [components (extract-defn-components form)
              new-form (reconstruct-defn components)]
          (pr-str new-form))
        form-str))
    (catch Exception e
      (println "Error migrating form:" (.getMessage e))
      form-str)))

(defn migrate-file
  "Migrate all >defn forms in a file"
  [input-path output-path]
  (try
    (let [content (slurp input-path)
          zloc (z/of-string content)
          
          ;; Process all top-level forms
          migrated (loop [loc zloc
                          result []]
                     (if (z/end? loc)
                       result
                       (let [node (z/node loc)
                             form (when (n/sexpr-able? node) (n/sexpr node))]
                         (if (and (list? form)
                                  (contains? #{'> '>defn '>defn-} (first form)))
                           ;; Migrate this form
                           (let [components (extract-defn-components form)
                                 new-form (reconstruct-defn components)
                                 new-node (n/coerce new-form)]
                             (recur (z/next loc)
                                    (conj result (z/replace loc new-node))))
                           ;; Keep as-is
                           (recur (z/next loc)
                                  (conj result loc))))))
          
          ;; Write output
          output-str (z/root-string (last migrated))]
      
      (io/make-parents output-path)
      (spit output-path output-str)
      {:status :success
       :message (str "Migrated " input-path " to " output-path)})
    
    (catch Exception e
      {:status :error
       :message (.getMessage e)
       :file input-path})))

(defn migrate-directory
  "Recursively migrate all .clj files in a directory"
  [dir-path output-dir]
  (let [dir (io/file dir-path)
        files (filter #(str/ends-with? (.getName %) ".clj")
                      (file-seq dir))
        results (for [file files]
                  (let [rel-path (str/replace (.getPath file)
                                              (str dir-path "/") "")
                        output-path (str output-dir "/" rel-path)]
                    (migrate-file (.getPath file) output-path)))]
    {:total (count files)
     :success (count (filter #(= :success (:status %)) results))
     :errors (filter #(= :error (:status %)) results)}))

;; ============================================================================
;; CLI
;; ============================================================================

(def cli-options
  [["-i" "--input PATH" "Input file or directory"
    :required true]
   ["-o" "--output PATH" "Output file or directory"
    :required true]
   ["-d" "--dry-run" "Show what would be migrated without writing files"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (do (println "Guardrails to Malli Migration Tool")
                          (println summary))
      errors (do (println "Errors:" errors)
                 (System/exit 1))
      :else
      (let [{:keys [input output dry-run]} options
            input-file (io/file input)]
        (cond
          (.isDirectory input-file)
          (let [result (migrate-directory input output)]
            (println "Migration complete!")
            (println "Total files:" (:total result))
            (println "Success:" (:success result))
            (when (seq (:errors result))
              (println "Errors:")
              (doseq [err (:errors result)]
                (println "  -" (:file err) ":" (:message err)))))
          
          (.isFile input-file)
          (let [result (migrate-file input output)]
            (if (= :success (:status result))
              (println (:message result))
              (println "Error:" (:message result))))
          
          :else
          (println "Input path does not exist:" input))))))