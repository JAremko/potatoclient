(ns proto-explorer.keyword-tree-mapper-test
  (:require [clojure.test :refer :all]
            [proto-explorer.keyword-tree-mapper :as mapper]
            [clojure.string :as str]))

(deftest test-field-name-conversion
  (testing "Convert proto field names to keywords"
    (is (= :hello-world (mapper/field-name->keyword "hello_world")))
    (is (= :start-all (mapper/field-name->keyword "start_all")))
    (is (= :get-meteo (mapper/field-name->keyword "get_meteo")))
    (is (= :simple (mapper/field-name->keyword "simple")))))

(deftest test-proto-field-to-setter
  (testing "Convert proto field names to Java setter methods"
    (is (= "setHelloWorld" (mapper/proto-field-name->setter "hello_world")))
    (is (= "setStartAll" (mapper/proto-field-name->setter "start_all")))
    (is (= "setGetMeteo" (mapper/proto-field-name->setter "get_meteo")))
    (is (= "setSimple" (mapper/proto-field-name->setter "simple")))
    (is (= "setProtocolVersion" (mapper/proto-field-name->setter "protocol_version")))))

(deftest test-package-to-outer-class
  (testing "Convert package and proto file to outer class name"
    (is (= "JonSharedCmd" 
           (mapper/package->outer-class "cmd" "jon_shared_cmd.proto")))
    (is (= "JonSharedCmdSystem" 
           (mapper/package->outer-class "cmd.System" "jon_shared_cmd_system.proto")))
    (is (= "JonSharedCmdRotary" 
           (mapper/package->outer-class "cmd.RotaryPlatform" "jon_shared_cmd_rotary.proto")))))

(def test-descriptor
  {:file [{:name "jon_shared_cmd.proto"
           :package "cmd"
           :messageType [{:name "Root"
                          :field [{:name "ping" :type "TYPE_MESSAGE" :typeName ".cmd.Ping" :oneofIndex 0}
                                  {:name "system" :type "TYPE_MESSAGE" :typeName ".cmd.System.Root" :oneofIndex 0}
                                  {:name "protocol_version" :type "TYPE_UINT32"}
                                  {:name "session_id" :type "TYPE_STRING"}]}
                         {:name "Ping" :field []}]}]})

(deftest test-extract-all-nodes
  (testing "Extract nodes from descriptors"
    (let [nodes (mapper/extract-all-nodes [test-descriptor])]
      ;; Should extract Root and Ping
      (is (= 2 (count nodes)))
      
      ;; Check Root node
      (let [root-node (get nodes "cmd.JonSharedCmd$Root")]
        (is (not (nil? root-node)))
        (is (= "cmd.JonSharedCmd$Root" (:java-class root-node)))
        
        ;; Check children (message fields)
        (is (= {:ping ".cmd.Ping" :system ".cmd.System.Root"} 
               (:children root-node)))
        
        ;; Check fields
        (let [fields (:fields root-node)]
          ;; All fields should be present
          (is (= 4 (count fields)))
          
          ;; Check ping field (message type)
          (let [ping-field (:ping fields)]
            (is (= "ping" (:proto-field ping-field)))
            (is (= "setPing" (:setter ping-field)))
            (is (= "TYPE_MESSAGE" (:type ping-field)))
            (is (= 0 (:oneof-index ping-field)))
            (is (= ".cmd.Ping" (:type-ref ping-field))))
          
          ;; Check protocol_version field (primitive type)
          (let [pv-field (:protocol-version fields)]
            (is (= "protocol_version" (:proto-field pv-field)))
            (is (= "setProtocolVersion" (:setter pv-field)))
            (is (= "TYPE_UINT32" (:type pv-field)))
            (is (nil? (:type-ref pv-field))))))
      
      ;; Check Ping node
      (let [ping-node (get nodes "cmd.JonSharedCmd$Ping")]
        (is (not (nil? ping-node)))
        (is (empty? (:children ping-node)))
        (is (empty? (:fields ping-node)))))))

(deftest test-type-resolution
  (testing "Resolve type references to Java classes"
    (let [nodes {"cmd.JonSharedCmd$Root" {:java-class "cmd.JonSharedCmd$Root"
                                           :children {:ping ".cmd.Ping"
                                                      :system ".cmd.System.Root"}
                                           :fields {:ping {:type-ref ".cmd.Ping"}
                                                    :system {:type-ref ".cmd.System.Root"}}}
                 "cmd.JonSharedCmd$Ping" {:java-class "cmd.JonSharedCmd$Ping"
                                          :children {}
                                          :fields {}}
                 "cmd.System.JonSharedCmdSystem$Root" {:java-class "cmd.System.JonSharedCmdSystem$Root"
                                                        :children {}
                                                        :fields {}}}
          resolved (mapper/resolve-type-references nodes)]
      
      ;; Check resolved children
      (let [root (get resolved "cmd.JonSharedCmd$Root")]
        (is (= {:ping "cmd.JonSharedCmd$Ping"
                :system "cmd.System.JonSharedCmdSystem$Root"}
               (:children root)))
        
        ;; Check resolved field type refs
        (is (= "cmd.JonSharedCmd$Ping" 
               (get-in root [:fields :ping :java-class])))
        (is (= "cmd.System.JonSharedCmdSystem$Root" 
               (get-in root [:fields :system :java-class])))))))

(deftest test-build-keyword-tree
  (testing "Build keyword tree from nodes"
    (let [nodes {"cmd.JonSharedCmd$Root" {:java-class "cmd.JonSharedCmd$Root"
                                           :children {:ping "cmd.JonSharedCmd$Ping"
                                                      :noop "cmd.JonSharedCmd$Noop"}
                                           :fields {:ping {:proto-field "ping" :setter "setPing"}
                                                    :noop {:proto-field "noop" :setter "setNoop"}}}
                 "cmd.JonSharedCmd$Ping" {:java-class "cmd.JonSharedCmd$Ping"
                                          :children {}
                                          :fields {}}
                 "cmd.JonSharedCmd$Noop" {:java-class "cmd.JonSharedCmd$Noop"
                                          :children {}
                                          :fields {}}}
          tree (mapper/build-keyword-tree nodes)]
      
      ;; Should have ping and noop at top level
      (is (= #{:ping :noop} (set (keys tree))))
      
      ;; Check ping node
      (let [ping-node (:ping tree)]
        (is (= "cmd.JonSharedCmd$Ping" (:java-class ping-node)))
        (is (empty? (:children ping-node)))
        (is (empty? (:fields ping-node))))
      
      ;; Check noop node  
      (let [noop-node (:noop tree)]
        (is (= "cmd.JonSharedCmd$Noop" (:java-class noop-node)))
        (is (empty? (:children noop-node)))
        (is (empty? (:fields noop-node)))))))

(deftest test-repeated-field-handling
  (testing "Handle repeated fields correctly"
    (let [descriptor {:file [{:name "jon_shared_test.proto"
                               :package "test"
                               :messageType [{:name "Root"
                                              :field [{:name "items" 
                                                       :type "TYPE_MESSAGE" 
                                                       :typeName ".test.Item"
                                                       :label "LABEL_REPEATED"}]}]}]}
          nodes (mapper/extract-all-nodes [descriptor])
          root-node (get nodes "test.JonSharedTest$Root")]
      
      ;; Check that repeated field is marked
      (is (true? (get-in root-node [:fields :items :repeated]))))))

(deftest test-complex-nesting
  (testing "Handle complex nested structures"
    (let [descriptor {:file [{:name "jon_shared_cmd_rotary.proto"
                               :package "cmd.RotaryPlatform"
                               :messageType [{:name "Root"
                                              :field [{:name "axis" :type "TYPE_MESSAGE" 
                                                       :typeName ".cmd.RotaryPlatform.Axis" 
                                                       :oneofIndex 0}]}
                                             {:name "Axis"
                                              :field [{:name "azimuth" :type "TYPE_MESSAGE"
                                                       :typeName ".cmd.RotaryPlatform.Azimuth"
                                                       :oneofIndex 0}
                                                      {:name "elevation" :type "TYPE_MESSAGE"
                                                       :typeName ".cmd.RotaryPlatform.Elevation"
                                                       :oneofIndex 0}]}]}]}
          nodes (mapper/extract-all-nodes [descriptor])
          resolved (mapper/resolve-type-references nodes)
          tree (mapper/build-keyword-tree resolved)]
      
      ;; Check nodes were extracted
      (is (= 2 (count nodes)))
      (is (contains? nodes "cmd.RotaryPlatform.JonSharedCmdRotary$Root"))
      (is (contains? nodes "cmd.RotaryPlatform.JonSharedCmdRotary$Axis"))
      
      ;; Tree should be empty since we start from cmd.JonSharedCmd$Root
      ;; but the descriptor doesn't have that root
      (is (empty? tree)))))

(deftest test-empty-descriptor
  (testing "Handle empty descriptor gracefully"
    (let [nodes (mapper/extract-all-nodes [{:file []}])]
      (is (empty? nodes)))
    
    (let [tree (mapper/build-keyword-tree {})]
      (is (empty? tree)))))