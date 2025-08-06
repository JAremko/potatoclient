(ns hooks.seesaw-ui
  (:require [clj-kondo.hooks-api :as api]))

(defn seesaw-widget
  "Hook for Seesaw widget creation functions that take keyword arguments"
  [{:keys [node]}]
  (let [args (rest (:children node))
        ;; Convert keyword args to a map
        kw-args (loop [args args
                       result {}]
                  (if (empty? args)
                    result
                    (if (api/keyword-node? (first args))
                      (recur (drop 2 args)
                             (assoc result (api/sexpr (first args)) (second args)))
                      (recur (rest args) result))))]
    {:node (api/list-node
            (list* (first (:children node))
                   (api/map-node (vec (mapcat (fn [[k v]] [k v]) kw-args)))))}))

(defn mig-panel
  "Hook for mig-panel which has special :items syntax"
  [{:keys [node]}]
  (let [args (rest (:children node))
        ;; Find :items keyword and process its value specially
        processed-args (loop [args args
                              result []]
                         (if (empty? args)
                           result
                           (if (and (api/keyword-node? (first args))
                                    (= :items (api/sexpr (first args))))
                             ;; Process :items value as a vector of vectors
                             (recur (drop 2 args)
                                    (conj result (first args) (second args)))
                             (recur (rest args)
                                    (conj result (first args))))))]
    {:node (api/list-node
            (cons (first (:children node)) processed-args))}))

(defn bind
  "Hook for seesaw.bind/bind which has complex transformations"
  [{:keys [node]}]
  (let [args (rest (:children node))
        ;; Transform to (-> source transforms...)
        transformed (api/list-node
                     (cons (api/token-node '->)
                           args))]
    {:node transformed}))