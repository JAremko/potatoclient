(require '[rewrite-clj.node :as n])

;; Test the problematic function structure
(defn test-node []
  (n/list-node
   [(n/token-node 'when)
    (n/whitespace-node " ")
    (n/list-node
     [(n/token-node '.hasField)
      (n/whitespace-node " ")
      (n/token-node 'proto)])
    (n/whitespace-node " ")
    (n/list-node
     [(n/token-node 'assoc)
      (n/whitespace-node " ")
      (n/keyword-node :field)
      (n/whitespace-node " ")
      (n/list-node
       [(n/token-node '.getField)
        (n/whitespace-node " ")
        (n/token-node 'proto)])])]))

(println (n/string (test-node)))