(ns potatoclient.ui.bind-group-test
  (:require [clojure.test :refer :all]
            [potatoclient.ui.bind-group :as bg]
            [seesaw.bind :as bind]
            [seesaw.core :as ssc]))

(deftest test-bind-group-basic
  (testing "Basic grouped binding creation and cleanup"
    (let [source-atom (atom 0)
          target-atom (atom nil)
          ; Create a grouped binding
          binding (bg/bind-group :test-group source-atom target-atom)]
      
      ; Verify binding works
      (reset! source-atom 42)
      (is (= 42 @target-atom) "Binding should propagate value")
      
      ; Clean the group
      (let [cleaned (bg/clean-group :test-group source-atom)]
        (is (= 1 cleaned) "Should have cleaned 1 binding"))
      
      ; Verify binding is disconnected
      (reset! source-atom 100)
      (is (= 42 @target-atom) "Value should not propagate after cleanup"))))

(deftest test-multiple-groups
  (testing "Multiple binding groups on same atom"
    (let [source-atom (atom 0)
          target1 (atom nil)
          target2 (atom nil)
          target3 (atom nil)]
      
      ; Create bindings in different groups
      (bg/bind-group :group-a source-atom target1)
      (bg/bind-group :group-b source-atom target2)
      (bg/bind-group :group-b source-atom target3)
      
      ; Verify all bindings work
      (reset! source-atom 10)
      (is (= 10 @target1))
      (is (= 10 @target2))
      (is (= 10 @target3))
      
      ; Check group listing
      (is (= #{:group-a :group-b} (bg/list-groups source-atom)))
      
      ; Check group counts
      (is (= 1 (bg/group-count :group-a source-atom)))
      (is (= 2 (bg/group-count :group-b source-atom)))
      
      ; Clean group-b only
      (is (= 2 (bg/clean-group :group-b source-atom)))
      
      ; Verify selective cleanup
      (reset! source-atom 20)
      (is (= 20 @target1) "Group-a binding should still work")
      (is (= 10 @target2) "Group-b binding should be disconnected")
      (is (= 10 @target3) "Group-b binding should be disconnected")
      
      ; Clean remaining
      (bg/clean-all-groups source-atom)
      (reset! source-atom 30)
      (is (= 20 @target1) "All bindings should be disconnected"))))

(deftest test-with-binding-group-macro
  (testing "Temporary binding group with automatic cleanup"
    (let [source-atom (atom 0)
          target-atom (atom nil)]
      
      ; Use the macro
      (bg/with-binding-group [:temp-group source-atom]
        (bg/bind-group :temp-group source-atom target-atom)
        (reset! source-atom 50)
        (is (= 50 @target-atom) "Binding should work within macro"))
      
      ; Verify cleanup happened
      (reset! source-atom 60)
      (is (= 50 @target-atom) "Binding should be cleaned up after macro"))))

(deftest test-bind-group-with-transform
  (testing "Grouped binding with transformation"
    (let [source-atom (atom 5)
          target-atom (atom nil)]
      
      (bg/bind-group :math-group 
                     source-atom 
                     (bind/transform #(* % 2))
                     target-atom)
      
      (reset! source-atom 10)
      (is (= 20 @target-atom) "Transform should double the value")
      
      (bg/clean-group :math-group source-atom)
      (reset! source-atom 15)
      (is (= 20 @target-atom) "Should not update after cleanup"))))

(deftest test-replace-group
  (testing "Replacing all bindings in a group"
    (let [source-atom (atom 0)
          target1 (atom nil)
          target2 (atom nil)
          target3 (atom nil)]
      
      ; Initial bindings
      (bg/bind-group :replaceable source-atom target1)
      (bg/bind-group :replaceable source-atom target2)
      
      (reset! source-atom 5)
      (is (= 5 @target1))
      (is (= 5 @target2))
      (is (nil? @target3))
      
      ; Replace with new bindings
      (bg/replace-group :replaceable source-atom
                        [[target3]
                         [(bind/transform #(* % 10)) target2]])
      
      (reset! source-atom 7)
      (is (= 5 @target1) "Old binding should be disconnected")
      (is (= 70 @target2) "New transformed binding should work")
      (is (= 7 @target3) "New direct binding should work")
      
      (bg/clean-all-groups source-atom))))

(deftest test-convenience-functions
  (testing "Convenience binding functions"
    (let [source-atom (atom "hello")
          label (ssc/label :text "")  ; Initialize with empty text
          checkbox (ssc/checkbox)
          result-atom (atom nil)]
      
      ; Test property binding
      (bg/bind-group-property :ui-group source-atom label :text)
      ; Force a change to trigger binding
      (reset! source-atom "world")
      (reset! source-atom "hello")
      (is (= "hello" (ssc/text label)))
      
      (reset! source-atom "world")
      (is (= "world" (ssc/text label)))
      
      ; Test transform binding
      (bg/bind-group-transform :ui-group 
                               source-atom
                               #(.toUpperCase %)
                               result-atom)
      
      (reset! source-atom "test")
      (is (= "test" (ssc/text label)))
      (is (= "TEST" @result-atom))
      
      ; Cleanup
      (bg/clean-group :ui-group source-atom)
      (reset! source-atom "final")
      (is (= "test" (ssc/text label)) "Should not update after cleanup")
      (is (= "TEST" @result-atom) "Should not update after cleanup"))))

(deftest test-debug-groups
  (testing "Debug information about binding groups"
    (let [atom1 (atom 0)
          atom2 (atom 0)
          target (atom nil)]
      
      (bg/bind-group :group-a atom1 target)
      (bg/bind-group :group-a atom1 target)
      (bg/bind-group :group-b atom1 target)
      (bg/bind-group :group-x atom2 target)
      
      (let [debug-info (bg/debug-groups)]
        (is (= 2 (get-in debug-info [atom1 :group-a])))
        (is (= 1 (get-in debug-info [atom1 :group-b])))
        (is (= 1 (get-in debug-info [atom2 :group-x]))))
      
      ; Cleanup
      (bg/clean-all-groups atom1)
      (bg/clean-all-groups atom2))))

(deftest test-edge-cases
  (testing "Edge cases and error handling"
    (let [source-atom (atom 0)
          target-atom (atom nil)]
      
      ; Test cleaning non-existent group
      (is (= 0 (bg/clean-group :non-existent source-atom))
          "Cleaning non-existent group should return 0")
      
      ; Test empty group operations
      (is (= #{} (bg/list-groups source-atom))
          "Empty atom should have no groups")
      
      (is (= 0 (bg/group-count :any-group source-atom))
          "Non-existent group should have count 0")
      
      ; Test multiple cleanups
      (bg/bind-group :test-group source-atom target-atom)
      (is (= 1 (bg/clean-group :test-group source-atom)))
      (is (= 0 (bg/clean-group :test-group source-atom))
          "Second cleanup should return 0"))))

(deftest test-non-atom-sources
  (testing "Bindings with non-atom sources"
    (let [source-ref (ref 0)
          source-agent (agent 0)
          target-atom (atom nil)]
      
      ; Test with ref
      (bg/bind-group :ref-group source-ref target-atom)
      (dosync (ref-set source-ref 42))
      (Thread/sleep 10) ; Allow propagation
      (is (= 42 @target-atom) "Ref binding should work")
      
      (bg/clean-group :ref-group source-ref)
      (dosync (ref-set source-ref 100))
      (Thread/sleep 10)
      (is (= 42 @target-atom) "Ref binding should be disconnected")
      
      ; Test with agent
      (bg/bind-group :agent-group source-agent target-atom)
      (send source-agent (constantly 55))
      (await source-agent)
      (Thread/sleep 10)
      (is (= 55 @target-atom) "Agent binding should work")
      
      (bg/clean-group :agent-group source-agent)
      (send source-agent (constantly 77))
      (await source-agent)
      (Thread/sleep 10)
      (is (= 55 @target-atom) "Agent binding should be disconnected"))))

(deftest test-complex-binding-chains
  (testing "Complex multi-step binding chains"
    (let [source (atom 10)
          intermediate (atom nil)
          final-target (atom nil)]
      
      ; Create a chain: source -> (*2) -> intermediate -> (+100) -> final
      (bg/bind-group :chain-group
                     source
                     (bind/transform #(* % 2))
                     intermediate
                     (bind/transform #(+ % 100))
                     final-target)
      
      ; Force change to trigger propagation
      (reset! source 5)
      (reset! source 10)
      
      (is (= 20 @intermediate) "Intermediate should be source * 2")
      (is (= 120 @final-target) "Final should be intermediate + 100")
      
      (reset! source 5)
      (is (= 10 @intermediate))
      (is (= 110 @final-target))
      
      (bg/clean-group :chain-group source)
      (reset! source 20)
      (is (= 10 @intermediate) "Chain should be broken")
      (is (= 110 @final-target) "Chain should be broken"))))

(deftest test-same-binding-multiple-groups
  (testing "Adding same binding to multiple groups"
    (let [source (atom 0)
          target (atom nil)]
      
      ; Add similar bindings to different groups
      (bg/bind-group :group1 source target)
      (bg/bind-group :group2 source target)
      
      (reset! source 10)
      (is (= 10 @target))
      
      ; Clean one group
      (bg/clean-group :group1 source)
      
      ; Binding from group2 should still work
      (reset! source 20)
      (is (= 20 @target) "Group2 binding should still work")
      
      (bg/clean-group :group2 source)
      (reset! source 30)
      (is (= 20 @target) "All bindings should be cleaned"))))

(deftest test-funnel-with-groups
  (testing "Using funnel with grouped bindings"
    (let [input1 (atom 5)
          input2 (atom 10)
          output (atom nil)]
      
      ; Create a funnel that combines two inputs
      (bg/bind-group :funnel-group
                     (bind/funnel input1 input2)
                     (bind/transform #(when (every? some? %)
                                        (apply + %)))
                     output)
      
      ; Force both inputs to trigger funnel
      (reset! input1 0)
      (reset! input2 0)
      (reset! input1 5)
      (reset! input2 10)
      
      (is (= 15 @output) "Should sum both inputs")
      
      (reset! input1 7)
      (is (= 17 @output) "Should update when input1 changes")
      
      (reset! input2 3)
      (is (= 10 @output) "Should update when input2 changes")
      
      ; Note: We can't clean funnel bindings as the source isn't an atom
      ; This is a limitation of the current implementation
      )))

(deftest test-selection-binding
  (testing "Selection binding with groups"
    (let [source (atom true)
          checkbox (ssc/checkbox)]
      
      (bg/bind-group-selection :ui-group source checkbox)
      
      ; Force change to trigger binding
      (reset! source false)
      (reset! source true)
      
      (is (ssc/selection checkbox) "Checkbox should be selected")
      
      (reset! source false)
      (is (not (ssc/selection checkbox)) "Checkbox should be unselected")
      
      (bg/clean-group :ui-group source)
      (reset! source true)
      (is (not (ssc/selection checkbox)) "Checkbox should not update after cleanup"))))

(deftest test-concurrent-modifications
  (testing "Thread safety of group operations"
    (let [source (atom 0)
          targets (repeatedly 10 #(atom nil))
          groups (map #(keyword (str "group" %)) (range 5))]
      
      ; Add bindings concurrently
      (doall
        (pmap (fn [[group target]]
                (bg/bind-group group source target))
              (map vector (cycle groups) targets)))
      
      ; Verify all groups exist
      (is (= (set groups) (bg/list-groups source)))
      
      ; Clean groups concurrently
      (doall
        (pmap #(bg/clean-group % source) groups))
      
      ; Verify all cleaned
      (is (= #{} (bg/list-groups source))))))