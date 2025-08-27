(ns hooks.seesaw-comprehensive-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-kondo.hooks-api :as api]
            [hooks.seesaw-comprehensive :as sut]))

(defn parse-string
  "Helper to parse a string into a rewrite-clj node"
  [s]
  (api/parse-string s))

(defn node->str
  "Helper to convert a node back to string for inspection"
  [node]
  (api/sexpr node))

(deftest test-deep-walk-node
  (testing "deep-walk-node applies transformation to all nodes"
    (let [node (parse-string "(+ 1 (* 2 3))")
          counter (atom 0)
          transform-fn (fn [n]
                        (swap! counter inc)
                        n)
          result (sut/deep-walk-node node transform-fn)]
      ;; Should visit all nodes in the tree
      (is (> @counter 5)) ;; At least the list, +, 1, inner list, *, 2, 3
      (is (= '(+ 1 (* 2 3)) (node->str result)))))
  
  (testing "deep-walk-node can filter out nodes"
    (let [node (parse-string "(list 1 2 3)")
          ;; Remove all number nodes
          transform-fn (fn [n]
                        (if (number? (api/sexpr n))
                          nil
                          n))
          result (sut/deep-walk-node node transform-fn)]
      ;; Numbers should be filtered out
      (is (= 'list (first (node->str result)))))))

(deftest test-analyze-keyword-args
  (testing "analyze-keyword-args separates positional and keyword arguments"
    (let [args (rest (:children (parse-string "(fn target :text \"hello\" :enabled? true)")))
          result (sut/analyze-keyword-args args)]
      (is (= 1 (count (:positional result)))) ;; target
      (is (= :text (first (keys (:keyword-map result)))))
      (is (= :enabled? (second (keys (:keyword-map result))))))
  
  (testing "handles pure positional arguments"
    (let [args (rest (:children (parse-string "(fn a b c)")))
          result (sut/analyze-keyword-args args)]
      (is (= 3 (count (:positional result))))
      (is (empty? (:keyword-map result)))))
  
  (testing "handles pure keyword arguments"
    (let [args (rest (:children (parse-string "(fn :a 1 :b 2 :c 3)")))
          result (sut/analyze-keyword-args args)]
      (is (empty? (:positional result)))
      (is (= #{:a :b :c} (set (keys (:keyword-map result)))))))
  
  (testing "handles mixed arguments"
    (let [args (rest (:children (parse-string "(fn target other :text \"hi\" :width 100)")))
          result (sut/analyze-keyword-args args)]
      (is (= 2 (count (:positional result))))
      (is (= #{:text :width} (set (keys (:keyword-map result))))))))

(deftest test-process-nested-forms
  (testing "process-nested-forms handles vectors of widgets"
    (let [node (parse-string "[[button :text \"A\"] [label :text \"B\"]]")
          result (sut/process-nested-forms node)]
      ;; Should preserve structure
      (is (api/vector-node? result))
      (is (= 2 (count (:children result))))))
  
  (testing "handles deeply nested structures"
    (let [node (parse-string "[[(button :text \"A\")] [(label :text \"B\")]]")
          result (sut/process-nested-forms node)]
      (is (api/vector-node? result))
      ;; Inner vectors should be preserved
      (is (api/vector-node? (first (:children result)))))))

(deftest test-seesaw-widget-comprehensive
  (testing "processes button with keyword arguments"
    (let [node (parse-string "(button :text \"Click me\" :enabled? false)")
          {:keys [node]} (sut/seesaw-widget-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should create a do form that includes all code
      (is (= 'do (first sexpr)))
      ;; Should preserve the button call in some form
      (is (some #(and (seq? %) (= 'button (first %))) (tree-seq coll? seq sexpr)))))
  
  (testing "handles :handler functions in action"
    (let [node (parse-string "(action :name \"Test\" :handler (fn [e] (println e)))")
          {:keys [node]} (sut/seesaw-action-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should wrap handler in proper function form
      (is (= 'do (first sexpr)))
      ;; Should contain function definition
      (is (some #(and (seq? %) (= 'fn (first %))) (tree-seq coll? seq sexpr)))))
  
  (testing "processes nested :items in menubar"
    (let [node (parse-string "(menubar :items [(menu :text \"File\" :items [(action :name \"Exit\")])])")
          {:keys [node]} (sut/seesaw-menubar-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should process nested menu and action calls
      (is (= 'do (first sexpr)))
      ;; The nested structures should be analyzed
      (is (some #(and (seq? %) (= 'action (first %))) (tree-seq coll? seq sexpr))))))

(deftest test-seesaw-listen-comprehensive
  (testing "processes event handlers in listen"
    (let [node (parse-string "(listen button :action (fn [e] (println \"clicked\")) :mouse-entered #(println \"hover\"))")
          {:keys [node]} (sut/seesaw-listen-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should preserve listen call with processed handlers
      (is (= 'listen (first sexpr)))
      ;; Should have wrapped handlers as functions
      (let [handlers (drop 2 sexpr)]
        (is (some #(and (seq? %) (= 'fn (first %))) handlers))))))

(deftest test-seesaw-config-comprehensive
  (testing "processes config! with properties"
    (let [node (parse-string "(config! widget :text \"New text\" :enabled? true)")
          {:keys [node]} (sut/seesaw-config-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should create a do form
      (is (= 'do (first sexpr)))
      ;; Should analyze property values
      (is (some string? (tree-seq coll? seq sexpr)))
      (is (some true? (tree-seq coll? seq sexpr))))))

(deftest test-seesaw-bind-comprehensive
  (testing "processes bind transformations"
    (let [node (parse-string "(bind source (transform str) (property target :text))")
          {:keys [node]} (sut/seesaw-bind-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should create do form with threading
      (is (= 'do (first sexpr)))
      ;; Should contain threading form
      (is (some #(and (seq? %) (= '-> (first %))) (tree-seq coll? seq sexpr))))))

(deftest test-seesaw-mig-panel-comprehensive
  (testing "processes mig-panel with :items"
    (let [node (parse-string "(mig-panel :items [[(button :text \"A\") \"wrap\"] [(label :text \"B\")]])")
          {:keys [node]} (sut/seesaw-mig-panel-comprehensive {:node node})
          sexpr (node->str node)]
      ;; Should create do form that analyzes nested widgets
      (is (= 'do (first sexpr)))
      ;; Should process button and label calls
      (is (some #(and (seq? %) (= 'button (first %))) (tree-seq coll? seq sexpr)))
      (is (some #(and (seq? %) (= 'label (first %))) (tree-seq coll? seq sexpr))))))