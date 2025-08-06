(ns hooks.potatoclient
  (:require [clj-kondo.hooks-api :as api]))

(defn defmsg [{:keys [node]}]
  (let [[name-node & body-nodes] (rest (:children node))
        new-node (api/list-node
                  (list*
                   (api/token-node 'def)
                   name-node
                   body-nodes))]
    {:node new-node}))