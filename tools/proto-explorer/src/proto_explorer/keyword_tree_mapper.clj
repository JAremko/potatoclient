(ns proto-explorer.keyword-tree-mapper
  "Generate keyword-based tree mapping for protobuf commands.
  
  The root is a map from keyword to node.
  Each node has :java-class and :children (also keyword->node map)."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn field-name->keyword
  "Convert protobuf field name to Clojure keyword."
  [field-name]
  (-> field-name
      (str/replace #"_" "-")
      keyword))

(defn package->outer-class
  "Convert package name to outer class name based on proto file naming."
  [package proto-file]
  (-> proto-file
      (str/replace #"\.proto$" "")
      (str/split #"_")
      (->> (map str/capitalize)
           (str/join ""))))

(defn proto-field-name->setter
  "Convert proto field name to Java setter method name."
  [field-name]
  (str "set" (-> field-name
                 (str/split #"_")
                 (->> (map str/capitalize)
                      (str/join "")))))

(defn extract-all-nodes
  "Extract all nodes from all descriptors into a flat map."
  [descriptors]
  (let [nodes (atom {})]
    (doseq [descriptor descriptors
            file (:file descriptor)]
      (when (str/starts-with? (:name file) "jon_shared_")
        (let [package (:package file)
              proto-file (:name file)
              outer-class (package->outer-class package proto-file)]
          ;; Process each message type
          (letfn [(process-message [msg parent-java-class level]
                    (let [msg-name (:name msg)
                          ;; Build Java class name
                          java-class (if parent-java-class
                                       (str parent-java-class "$" msg-name)
                                       (str package "." outer-class "$" msg-name))
                          ;; Collect children and field mappings
                          children (atom {})
                          field-mappings (atom {})]
                      
                      ;; Process all fields
                      (doseq [field (:field msg)]
                        (let [field-name (:name field)
                              field-keyword (field-name->keyword field-name)
                              setter-name (proto-field-name->setter field-name)
                              field-info {:proto-field field-name
                                          :setter setter-name
                                          :type (:type field)
                                          :repeated (= "LABEL_REPEATED" (:label field))
                                          :oneof-index (:oneofIndex field)}]
                          
                          ;; Handle all message type fields
                          (if (= (:type field) "TYPE_MESSAGE")
                            (let [type-name (:typeName field)]
                              ;; All message fields are children (not just oneofs)
                              (swap! children assoc field-keyword type-name)
                              ;; Add type reference to field info
                              (swap! field-mappings assoc field-keyword
                                     (assoc field-info :type-ref type-name)))
                            ;; Add all other fields
                            (swap! field-mappings assoc field-keyword field-info))))
                      
                      ;; Store node - children are just fields with type-ref
                      (swap! nodes assoc java-class
                             {:java-class java-class
                              :children @children  ; Keep for tree building
                              :fields @field-mappings})
                      
                      ;; Process nested messages
                      (doseq [nested (:nestedType msg)]
                        (process-message nested java-class (inc level)))))]
            
            ;; Process all messages in the file
            (doseq [msg (:messageType file)]
              (process-message msg nil 0))))))
    @nodes))

(defn resolve-type-references
  "Resolve type name references to actual Java class names."
  [nodes]
  ;; Build lookup maps for resolution
  (let [;; Map from proto type path to Java class
        type-to-class (reduce-kv
                        (fn [acc java-class node]
                          ;; Extract the proto type path
                          (let [parts (str/split java-class #"\.")
                                package (str/join "." (butlast parts))
                                class-parts (str/split (last parts) #"\$")
                                ;; Reconstruct proto type path - handle nested classes
                                proto-path (str "." package "." (first class-parts))
                                ;; Also add mapping for inner references
                                inner-path (when (> (count class-parts) 1)
                                             (str "." package "." (last class-parts)))]
                            (cond-> acc
                              true (assoc proto-path java-class)
                              inner-path (assoc inner-path java-class))))
                        {}
                        nodes)]
    ;; Resolve type references in both children and fields
    (reduce-kv
      (fn [acc java-class node]
        (let [;; Resolve children references
              resolved-children (if (seq (:children node))
                                  (reduce-kv
                                    (fn [children k type-ref]
                                      (if-let [resolved-class (get type-to-class type-ref)]
                                        (assoc children k resolved-class)
                                        (do (println "Warning: Could not resolve" type-ref "for" k)
                                            children)))
                                    {}
                                    (:children node))
                                  {})
              ;; Resolve field type references
              resolved-fields (reduce-kv
                                (fn [fields k field-info]
                                  (if-let [type-ref (:type-ref field-info)]
                                    (if-let [resolved-class (get type-to-class type-ref)]
                                      (assoc fields k (assoc field-info :java-class resolved-class))
                                      fields)
                                    (assoc fields k field-info)))
                                {}
                                (:fields node))]
          (assoc acc java-class
                 (assoc node 
                        :children resolved-children
                        :fields resolved-fields))))
      {}
      nodes)))

(defn build-keyword-tree
  "Build the keyword-based tree starting from root.
  For commands, uses cmd.JonSharedCmd$Root.
  For state, uses ser.JonSharedData$JonGUIState."
  [all-nodes & {:keys [root-class] :or {root-class "cmd.JonSharedCmd$Root"}}]
  ;; Find the specified root
  (if-let [root-node (get all-nodes root-class)]
    (letfn [(build-subtree [java-class]
              (when-let [node (get all-nodes java-class)]
                {:java-class java-class
                 :fields (:fields node)  ; Include field mappings
                 :children (reduce-kv
                             (fn [acc k child-class]
                               (if-let [child-tree (build-subtree child-class)]
                                 (assoc acc k child-tree)
                                 acc))
                             {}
                             (:children node))}))]
      ;; Build tree for each root child
      (reduce-kv
        (fn [acc k child-class]
          (if-let [child-tree (build-subtree child-class)]
            (assoc acc k child-tree)
            acc))
        {}
        (:children root-node)))
    {}))

(defn generate-keyword-tree-file
  "Generate a Clojure namespace with keyword tree for either commands or state.
  proto-type can be :command or :state."
  [descriptor-dir output-file proto-type]
  (let [files (file-seq (io/file descriptor-dir))
        json-files (filter #(and (str/ends-with? (.getName %) ".json")
                                 (not= "descriptor-set.json" (.getName %))) files)
        descriptors (map #(json/parse-string (slurp %) true) json-files)
        ;; Extract all nodes
        all-nodes (extract-all-nodes descriptors)
        ;; Resolve type references
        resolved-nodes (resolve-type-references all-nodes)
        ;; Build keyword tree with appropriate root
        [root-class ns-suffix desc-type] (case proto-type
                                            :command ["cmd.JonSharedCmd$Root" 
                                                      "proto-keyword-tree-cmd"
                                                      "command"]
                                            :state ["ser.JonSharedData$JonGUIState" 
                                                    "proto-keyword-tree-state"
                                                    "state"])
        keyword-tree (build-keyword-tree resolved-nodes :root-class root-class)]
    
    (with-open [w (io/writer output-file)]
      (.write w (str "(ns potatoclient.specs." ns-suffix "\n"))
      (.write w (str "  \"Auto-generated " desc-type " proto keyword tree structure.\n"))
      (.write w "  Top level is a map from keyword to node.\n")
      (.write w "  Each node has:\n")
      (.write w "  - :java-class - Full Java class name\n")
      (.write w "  - :fields - Map of keyword to field info {:proto-field :setter :type}\n")
      (.write w "  - :children - Map of keyword to child node (recursive structure)\n")
      (.write w "  \n")
      (.write w "  Generated by proto-explorer.\n")
      (.write w "  \n")
      (.write w "  DO NOT EDIT - This file is auto-generated.\n")
      (.write w "  For the logic that uses this data, see proto-type-mapping-nested.clj\")\n\n")
      
      ;; Write the keyword tree
      (.write w (str ";; Keyword-based " desc-type " proto tree for fast lookup\n"))
      (.write w ";; Start with a keyword, navigate through :children\n")
      (.write w "(def keyword-tree\n")
      (.write w "  ")
      (.write w (pr-str keyword-tree))
      (.write w ")\n"))
    
    ;; Return summary
    {:proto-type proto-type
     :root-class root-class
     :root-count (count keyword-tree)
     :total-nodes (count (tree-seq map? #(vals (:children %)) {:children keyword-tree}))}))