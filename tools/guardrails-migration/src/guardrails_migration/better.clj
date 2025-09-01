(ns guardrails-migration.better
  "Better migration using rewrite-clj to preserve formatting"
  (:require [clojure.string :as str]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]))

;; ============================================================================
;; Spec conversion helpers
;; ============================================================================

(defn convert-spec
  "Convert a Guardrails spec to Malli spec"
  [spec-str]
  (cond
    ;; (? spec) -> [:maybe spec]
    (str/starts-with? spec-str "(? ")
    (str "[:maybe " (subs spec-str 3 (dec (count spec-str))) "]")
    
    ;; Predicate function int? -> :int
    (and (str/ends-with? spec-str "?")
         (not (str/includes? spec-str " "))
         (not (str/starts-with? spec-str ":")))
    (let [base-name (str/replace spec-str #"\?$" "")]
      (case base-name
        "nil" ":nil"
        "any" ":any"
        "fn" ":fn"
        "ifn" ":ifn"
        (str ":" base-name)))
    
    ;; Already looks like keyword or complex spec
    :else spec-str))

(defn parse-gspec
  "Parse gspec string into args and return value"
  [gspec-str]
  (when (and gspec-str (str/includes? gspec-str "=>"))
    (let [parts (str/split gspec-str #"\s*=>\s*")
          args-str (first parts)
          ret-str (second parts)
          ;; Remove brackets if present
          args-str (if (str/starts-with? args-str "[")
                     (subs args-str 1 (dec (count args-str)))
                     args-str)
          ;; Split args and convert each
          args (if (str/blank? args-str)
                 []
                 (mapv convert-spec (str/split args-str #"\s+")))]
      {:args args
       :ret (convert-spec ret-str)})))

(defn gspec->malli-schema
  "Convert gspec string to Malli schema string"
  [gspec-str]
  (when-let [{:keys [args ret]} (parse-gspec gspec-str)]
    (str "[:=> [:cat" 
         (when (seq args) 
           (str " " (str/join " " args)))
         "] " ret "]")))

;; ============================================================================
;; AST manipulation
;; ============================================================================

(defn find-guardrails-require
  "Find and remove Guardrails imports from ns form"
  [zloc]
  (loop [loc zloc]
    (if-let [node (z/node loc)]
      (cond
        ;; Found a require vector with Guardrails
        (and (n/vector-node? node)
             (let [first-elem (first (n/children node))]
               (and first-elem
                    (or (= (str first-elem) "com.fulcrologic.guardrails.malli.core")
                        (= (str first-elem) "com.fulcrologic.guardrails.core")))))
        (z/remove loc)
        
        ;; Continue searching
        (z/right loc)
        (recur (z/right loc))
        
        ;; Go deeper if possible
        (and (z/down loc) (not (z/end? (z/down loc))))
        (recur (z/down loc))
        
        ;; Try to go up and right
        (z/up loc)
        (if-let [up (z/up loc)]
          (if-let [right (z/right up)]
            (recur right)
            loc)
          loc)
        
        :else loc)
      loc)))

(defn process-defn-form
  "Process a single >defn form, converting it to defn with Malli metadata"
  [zloc]
  (let [defn-node (z/node zloc)
        children (n/children defn-node)]
    (when (and (>= (count children) 3)
               (contains? #{'> '>defn '>defn-} (n/sexpr (first children))))
      (let [;; Extract parts
            defn-sym (if (= '>defn- (n/sexpr (first children))) 'defn- 'defn)
            name-sym (second children)
            rest-nodes (drop 2 children)
            
            ;; Check for docstring
            has-doc? (and (seq rest-nodes)
                          (n/string-node? (first rest-nodes)))
            docstring (when has-doc? (first rest-nodes))
            rest-nodes (if has-doc? (rest rest-nodes) rest-nodes)
            
            ;; Check for attr-map
            has-attrs? (and (seq rest-nodes)
                            (n/map-node? (first rest-nodes)))
            attr-map (when has-attrs? (first rest-nodes))
            rest-nodes (if has-attrs? (rest rest-nodes) rest-nodes)
            
            ;; Check for gspec (vector after args)
            args-node (first rest-nodes)
            potential-gspec (second rest-nodes)
            has-gspec? (and potential-gspec
                            (n/vector-node? potential-gspec)
                            (str/includes? (str potential-gspec) "=>"))
            gspec-str (when has-gspec?
                        (str/trim (str/replace (str potential-gspec) #"[\[\]]" "")))
            malli-schema (when gspec-str (gspec->malli-schema gspec-str))
            
            ;; Build new metadata map
            metadata-node (when (or malli-schema attr-map)
                            (if attr-map
                              (if malli-schema
                                ;; Add :malli/schema to existing map
                                (n/map-node
                                 (concat (n/children attr-map)
                                         [(n/keyword-node :malli/schema)
                                          (p/parse-string malli-schema)]))
                                attr-map)
                              ;; Create new map with just :malli/schema
                              (n/map-node
                               [(n/keyword-node :malli/schema)
                                (p/parse-string malli-schema)])))
            
            ;; Build body (skip gspec if present)
            body-nodes (if has-gspec?
                         (drop 2 rest-nodes)
                         (rest rest-nodes))
            
            ;; Build new form
            new-children (concat
                          [(n/token-node defn-sym)
                           name-sym]
                          (when docstring [docstring])
                          (when metadata-node [metadata-node])
                          [args-node]
                          body-nodes)]
        
        ;; Return new form
        (n/list-node new-children)))))

(defn migrate-string
  "Migrate a string of Clojure code"
  [code-str]
  (let [zloc (z/of-string code-str)]
    (loop [loc zloc
           processed []]
      (if (z/end? loc)
        (str/join "\n\n" processed)
        (let [node (z/node loc)]
          (cond
            ;; Process ns form to remove Guardrails
            (and (n/list-node? node)
                 (= 'ns (n/sexpr (first (n/children node)))))
            (let [cleaned (find-guardrails-require loc)]
              (recur (z/right loc)
                     (conj processed (z/string cleaned))))
            
            ;; Process >defn forms
            (and (n/list-node? node)
                 (contains? #{'> '>defn '>defn-} 
                           (n/sexpr (first (n/children node)))))
            (if-let [new-form (process-defn-form loc)]
              (recur (z/right loc)
                     (conj processed (str new-form)))
              (recur (z/right loc)
                     (conj processed (z/string loc))))
            
            ;; Keep other forms as-is
            :else
            (recur (z/right loc)
                   (conj processed (z/string loc)))))))))

(defn migrate-file
  "Migrate a file from Guardrails to Malli"
  [input-path output-path]
  (let [content (slurp input-path)
        migrated (migrate-string content)]
    (spit output-path migrated)
    {:status :success
     :file output-path}))