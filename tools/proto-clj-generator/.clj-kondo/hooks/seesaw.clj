(ns hooks.seesaw
  (:require [clj-kondo.hooks-api :as api]))

(defn defaction [{:keys [node]}]
  (let [[name-node & body-nodes] (rest (:children node))
        new-node (api/list-node
                  (list*
                   (api/token-node 'def)
                   name-node
                   body-nodes))]
    {:node new-node}))

(defn bind [{:keys [node]}]
  (let [args (rest (:children node))
        new-node (api/list-node
                  (list*
                   (api/token-node 'do)
                   args))]
    {:node new-node}))