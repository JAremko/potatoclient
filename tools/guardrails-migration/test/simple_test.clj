(ns simple-test
  (:require [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

;; Let's work directly with nodes
(def sample "(>defn foo [x] [int? => string?] (str x))")

(defn transform [code-str]
  (let [form-node (p/parse-string code-str)
        children (n/children form-node)
        [defn-sym ws1 fn-name ws2 args ws3 gspec ws4 body] children
        
        ;; Check if it's a >defn
        is-gdefn? (and (= :token (n/tag defn-sym))
                      (#{'>defn '>defn- '>} (n/sexpr defn-sym)))
        
        ;; Check if there's a gspec
        has-gspec? (and (= :vector (n/tag gspec))
                       (str/includes? (n/string gspec) "=>"))]
    
    (if (and is-gdefn? has-gspec?)
      ;; Transform it
      (let [;; Parse the gspec
            gspec-sexpr (n/sexpr gspec)
            arrow-idx (.indexOf gspec-sexpr '=>)
            args-specs (subvec gspec-sexpr 0 arrow-idx)
            ret-spec (get gspec-sexpr (inc arrow-idx))
            
            ;; Convert specs
            convert (fn [spec]
                     (cond
                       (and (symbol? spec) 
                            (str/ends-with? (str spec) "?"))
                       (keyword (str/replace (str spec) #"\?$" ""))
                       :else spec))
            
            malli-schema [:=> (into [:cat] (map convert args-specs)) 
                         (convert ret-spec)]
            
            ;; Create new defn node
            new-defn-sym (n/token-node 
                         (case (n/sexpr defn-sym)
                           >defn 'defn
                           >defn- 'defn-
                           > 'defn))
            
            ;; Create metadata node
            meta-node (n/map-node 
                      [(n/keyword-node :malli/schema)
                       (n/whitespace-node " ")
                       (n/coerce malli-schema)])
            
            ;; Build new form
            new-children [new-defn-sym ws1 fn-name ws2 
                         meta-node ws3 args ws4 body]]
        
        (n/list-node new-children))
      ;; Return unchanged
      form-node)))

(println "Original:")
(println sample)
(println "\nTransformed:")
(println (n/string (transform sample)))