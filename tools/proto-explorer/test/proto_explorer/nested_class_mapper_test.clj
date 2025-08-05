(ns proto-explorer.nested-class-mapper-test
  (:require [clojure.test :refer :all]
            [proto-explorer.nested-class-mapper :as mapper]
            [cheshire.core :as json]))

(def test-descriptor
  {:file [{:name "jon_shared_cmd.proto"
           :package "cmd"
           :messageType [{:name "Root"
                          :field [{:name "ping" :type "TYPE_MESSAGE" :typeName ".cmd.Ping" :oneofIndex 0}
                                  {:name "noop" :type "TYPE_MESSAGE" :typeName ".cmd.Noop" :oneofIndex 0}
                                  {:name "stop" :type "TYPE_MESSAGE" :typeName ".cmd.Stop" :oneofIndex 0}
                                  {:name "system" :type "TYPE_MESSAGE" :typeName ".cmd.System" :oneofIndex 0}]}
                         {:name "System"
                          :field [{:name "start" :type "TYPE_MESSAGE" :typeName ".cmd.System.Start" :oneofIndex 0}
                                  {:name "stop" :type "TYPE_MESSAGE" :typeName ".cmd.System.Stop" :oneofIndex 0}]
                          :nestedType [{:name "Start" :field []}
                                       {:name "Stop" :field []}]}]}]})

(deftest test-extract-class-hierarchy
  (testing "Extract class hierarchy from descriptors"
    (let [result (mapper/extract-class-hierarchy test-descriptor)]
      (is (= 2 (count result)))
      
      ;; Check Root class
      (let [root-entry (first (filter #(= "cmd.JonSharedCmd$Root" (:parent-class %)) result))]
        (is (not (nil? root-entry)))
        (is (= 4 (count (:children root-entry))))
        (is (= "cmd.JonSharedCmd$Root$Ping" (get (:children root-entry) "ping")))
        (is (= "cmd.JonSharedCmd$Root$Noop" (get (:children root-entry) "noop")))
        (is (= "cmd.JonSharedCmd$Root$Stop" (get (:children root-entry) "stop")))
        (is (= "cmd.JonSharedCmd$Root$System" (get (:children root-entry) "system"))))
      
      ;; Check System class
      (let [system-entry (first (filter #(= "cmd.JonSharedCmd$System" (:parent-class %)) result))]
        (is (not (nil? system-entry)))
        (is (= 2 (count (:children system-entry))))
        (is (= "cmd.JonSharedCmd$System$Start" (get (:children system-entry) "start")))
        (is (= "cmd.JonSharedCmd$System$Stop" (get (:children system-entry) "stop")))))))

(deftest test-build-class-to-children-mapping
  (testing "Build class->children mapping from hierarchy entries"
    (let [entries [{:parent-class "cmd.JonSharedCmd$Root"
                    :children {"ping" "cmd.JonSharedCmd$Root$Ping"
                               "system" "cmd.JonSharedCmd$Root$System"}}
                   {:parent-class "cmd.JonSharedCmd$Root$System"
                    :children {"start" "cmd.JonSharedCmd$Root$System$Start"
                               "stop" "cmd.JonSharedCmd$Root$System$Stop"}}]
          result (mapper/build-class-to-children-mapping entries)]
      (is (= 2 (count result)))
      (is (= {"ping" "cmd.JonSharedCmd$Root$Ping"
              "system" "cmd.JonSharedCmd$Root$System"}
             (get result "cmd.JonSharedCmd$Root")))
      (is (= {"start" "cmd.JonSharedCmd$Root$System$Start"
              "stop" "cmd.JonSharedCmd$Root$System$Stop"}
             (get result "cmd.JonSharedCmd$Root$System"))))))

(deftest test-ambiguous-keyword-detection
  (testing "Detect ambiguous keywords like :start and :stop"
    (let [entries [{:parent-class "cmd.JonSharedCmd$Root"
                    :children {"stop" "cmd.JonSharedCmd$Root$Stop"}}
                   {:parent-class "cmd.JonSharedCmd$Root$System"
                    :children {"start" "cmd.JonSharedCmd$Root$System$Start"
                               "stop" "cmd.JonSharedCmd$Root$System$Stop"}}
                   {:parent-class "cmd.JonSharedCmd$Root$RotaryPlatform"
                    :children {"start" "cmd.JonSharedCmd$Root$RotaryPlatform$Start"
                               "stop" "cmd.JonSharedCmd$Root$RotaryPlatform$Stop"}}]
          mapping (mapper/build-class-to-children-mapping entries)
          ;; Simulate finding ambiguous keywords
          keyword-parents (reduce-kv 
                            (fn [acc parent children]
                              (reduce-kv
                                (fn [acc2 k _]
                                  (update acc2 k (fnil conj #{}) parent))
                                acc
                                children))
                            {}
                            mapping)
          ambiguous (into {}
                          (filter (fn [[k parents]] (> (count parents) 1))
                                  keyword-parents))]
      ;; Both :start and :stop should be ambiguous
      (is (contains? ambiguous "stop"))
      (is (= 3 (count (get ambiguous "stop"))))
      (is (contains? ambiguous "start"))
      (is (= 2 (count (get ambiguous "start")))))))

(deftest test-oneof-field-detection
  (testing "Only process fields with oneofIndex"
    (let [descriptor {:file [{:name "jon_shared_test.proto"
                               :package "test"
                               :messageType [{:name "Root"
                                              :field [{:name "field1" :type "TYPE_MESSAGE" :typeName ".test.Msg1" :oneofIndex 0}
                                                      {:name "field2" :type "TYPE_MESSAGE" :typeName ".test.Msg2"}  ; No oneofIndex
                                                      {:name "field3" :type "TYPE_STRING" :oneofIndex 0}  ; Not a message
                                                      {:name "field4" :type "TYPE_MESSAGE" :typeName ".test.Msg4" :oneofIndex 1}]}]}]}
          result (mapper/extract-class-hierarchy descriptor)]
      (is (= 1 (count result)))
      (let [entry (first result)]
        ;; Check the parent class name is correct
        (is (= "test.JonSharedTest$Root" (:parent-class entry)))
        ;; Should only include field1 and field4 (TYPE_MESSAGE with oneofIndex)
        (is (= 2 (count (:children entry))))
        (is (contains? (:children entry) "field1"))
        (is (contains? (:children entry) "field4"))
        (is (not (contains? (:children entry) "field2")))
        (is (not (contains? (:children entry) "field3")))))))

(deftest test-nested-message-processing
  (testing "Process deeply nested messages"
    (let [descriptor {:file [{:name "jon_shared_nested.proto"
                               :package "nested"
                               :messageType [{:name "Root"
                                              :field [{:name "level1" :type "TYPE_MESSAGE" :typeName ".nested.Level1" :oneofIndex 0}]
                                              :nestedType [{:name "Level1"
                                                            :field [{:name "level2" :type "TYPE_MESSAGE" :typeName ".nested.Level2" :oneofIndex 0}]
                                                            :nestedType [{:name "Level2"
                                                                          :field [{:name "level3" :type "TYPE_MESSAGE" :typeName ".nested.Level3" :oneofIndex 0}]
                                                                          :nestedType [{:name "Level3" :field []}]}]}]}]}]}
          result (mapper/extract-class-hierarchy descriptor)]
      ;; Should have entries for Root, Level1, and Level2 (but not Level3 as it has no children)
      (is (= 3 (count result)))
      (is (some #(= "nested.JonSharedNested$Root" (:parent-class %)) result))
      (is (some #(= "nested.JonSharedNested$Root$Level1" (:parent-class %)) result))
      (is (some #(= "nested.JonSharedNested$Root$Level1$Level2" (:parent-class %)) result)))))

(deftest test-empty-descriptor
  (testing "Handle empty descriptor gracefully"
    (let [result (mapper/extract-class-hierarchy {:file []})]
      (is (empty? result)))))

(deftest test-file-filtering
  (testing "Only process jon_shared files"
    (let [descriptor {:file [{:name "other.proto" :package "other" :messageType [{:name "Test"}]}
                             {:name "jon_shared_test.proto" :package "test" :messageType [{:name "Root"}]}]}
          result (mapper/extract-class-hierarchy descriptor)]
      ;; Should only process jon_shared_test.proto
      (is (= 0 (count result)))  ; No oneofs in our test, so no results
      ;; But we can verify the file was considered by checking a modified version
      (let [descriptor-with-oneof {:file [{:name "other.proto" :package "other" 
                                           :messageType [{:name "Test" :field [{:name "field" :type "TYPE_MESSAGE" :typeName ".other.Field" :oneofIndex 0}]}]}
                                          {:name "jon_shared_test.proto" :package "test" 
                                           :messageType [{:name "Root" :field [{:name "field" :type "TYPE_MESSAGE" :typeName ".test.Field" :oneofIndex 0}]}]}]}
            result2 (mapper/extract-class-hierarchy descriptor-with-oneof)]
        ;; Should only have entry from jon_shared file
        (is (= 1 (count result2)))
        (is (= "test.JonSharedTest$Root" (:parent-class (first result2))))))))