(ns hooks.guardrails
  (:require [clj-kondo.hooks-api :as api]))

(defn defn-hook [{:keys [node]}]
  (let [[name-node & body] (rest (:children node))
        ;; Find the arglist
        arglist-node (first (filter #(and (api/vector-node? %)
                                           (not (#{:=> :| :?} (api/sexpr %))))
                                     body))
        ;; Find where the spec ends (look for => or the next vector for multi-arity)
        spec-end-idx (loop [idx 0
                            nodes body]
                       (cond
                         (empty? nodes) idx
                         (= :=> (api/sexpr (first nodes))) (inc idx)
                         (and (> idx 0) (api/vector-node? (first nodes))) idx
                         :else (recur (inc idx) (rest nodes))))
        ;; Extract the actual body (skip the specs)
        actual-body (drop spec-end-idx body)
        ;; Find docstring if present
        docstring (when (and (api/string-node? (first body))
                             (not= (first body) arglist-node))
                    (first body))
        ;; Build new node
        new-node (api/list-node
                  (list*
                   (api/token-node (if (= '>defn- (api/sexpr (first (:children node))))
                                     'defn-
                                     'defn))
                   name-node
                   (if docstring
                     (cons docstring (cons arglist-node actual-body))
                     (cons arglist-node actual-body))))]
    {:node new-node}))

(defn def-hook [{:keys [node]}]
  (let [[name-node spec-node value-node] (rest (:children node))
        new-node (api/list-node
                  (list
                   (api/token-node 'def)
                   name-node
                   value-node))]
    {:node new-node}))