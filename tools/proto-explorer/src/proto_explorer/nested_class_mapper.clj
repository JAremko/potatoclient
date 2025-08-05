(ns proto-explorer.nested-class-mapper
  "Generate mapping from Java protobuf classes to their children.
  
  This creates a top-down mapping where we start from a known parent class
  and can look up child classes by keyword. This eliminates ambiguity."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn extract-class-hierarchy
  "Extract class hierarchy from protobuf descriptors.
  Returns entries with parent-child relationships."
  [descriptor]
  (let [results (atom [])]
    (doseq [file (:file descriptor)]
      (let [package (:package file)
            proto-file (:name file)]
        ;; Skip non-jon_shared files
        (when (str/starts-with? proto-file "jon_shared_")
          (let [outer-class (-> proto-file
                                (str/replace #"\.proto$" "")
                                (str/split #"_")
                                (->> (map str/capitalize)
                                     (str/join "")))]
            ;; Process each message type
            (letfn [(process-message [msg parent-java-class level]
                      (let [msg-name (:name msg)
                            ;; Build Java class name
                            java-class (if parent-java-class
                                         (str parent-java-class "$" msg-name)
                                         (str package "." outer-class "$" msg-name))
                            ;; Process oneofs to find child messages
                            oneof-fields (atom {})]
                        
                        ;; Check fields in oneofs
                        (doseq [field (:field msg)]
                          (when (and (contains? field :oneofIndex)
                                     (= (:type field) "TYPE_MESSAGE"))
                            (let [field-name (:name field)
                                  ;; Convert to keyword format
                                  field-keyword (-> field-name
                                                   (str/replace #"_" "-"))
                                  ;; Extract the message type name
                                  type-name (:typeName field)
                                  ;; Convert to inner class name
                                  inner-class (when type-name
                                                (cond
                                                  ;; Same package, just the message name
                                                  (str/starts-with? type-name (str "." package "."))
                                                  (last (str/split type-name #"\."))
                                                  ;; Full path starting with dot
                                                  (str/starts-with? type-name ".")
                                                  (last (str/split type-name #"\."))
                                                  ;; Just the name
                                                  :else type-name))]
                              (when inner-class
                                (swap! oneof-fields assoc field-keyword 
                                       (str java-class "$" inner-class))))))
                        
                        ;; Record this class and its children
                        (when (pos? (count @oneof-fields))
                          (swap! results conj
                                 {:parent-class java-class
                                  :children @oneof-fields}))
                        
                        ;; Process nested messages
                        (doseq [nested (:nestedType msg)]
                          (process-message nested java-class (inc level)))))]
              
              ;; Start with top-level messages
              (doseq [msg (:messageType file)]
                (process-message msg nil 0)))))))
    @results))

(defn build-class-to-children-mapping
  "Build the primary mapping: Java class -> {keyword -> child-class}"
  [hierarchy-entries]
  (reduce (fn [acc entry]
            (assoc acc (:parent-class entry) (:children entry)))
          {}
          hierarchy-entries))

(defn generate-nested-mapping-file
  "Generate a Clojure namespace with class->children mapping."
  [descriptor-dir output-file]
  (let [files (file-seq (io/file descriptor-dir))
        json-files (filter #(and (str/ends-with? (.getName %) ".json")
                                 (not= "descriptor-set.json" (.getName %))) files)
        descriptors (map #(json/parse-string (slurp %) true) json-files)
        all-entries (mapcat extract-class-hierarchy descriptors)
        ;; Build the main mapping
        class->children-keywords (build-class-to-children-mapping all-entries)
        ;; Build simpler class -> set of direct child classes
        class->direct-children (reduce-kv
                                 (fn [acc parent children]
                                   (assoc acc parent (set (vals children))))
                                 {}
                                 class->children-keywords)
        ;; Find all classes mentioned
        all-classes (set (concat (keys class->children-keywords)
                                 (mapcat vals (vals class->children-keywords))))
        ;; Find leaf classes (mentioned but not parents)
        leaf-classes (set (remove #(contains? class->children-keywords %) all-classes))]
    
    (with-open [w (io/writer output-file)]
      (.write w "(ns potatoclient.specs.proto-type-mapping-nested-data\n")
      (.write w "  \"Auto-generated nested proto type mappings DATA ONLY.\n")
      (.write w "  Maps Java protobuf classes to their child fields and types.\n")
      (.write w "  Generated by proto-explorer.\n")
      (.write w "  \n")
      (.write w "  DO NOT EDIT - This file is auto-generated.\n")
      (.write w "  For the logic that uses this data, see proto-type-mapping-nested.clj\")\n\n")
      
      ;; Write the simple class->direct-children mapping
      (.write w ";; Simple mapping: Java class -> #{set of direct child classes}\n")
      (.write w ";; Use this to check if a class can be a child of another\n")
      (.write w "(def class->direct-children\n")
      (.write w "  ")
      (.write w (pr-str class->direct-children))
      (.write w ")\n\n")
      
      ;; Write the detailed class->children mapping with keywords
      (.write w ";; Detailed mapping: Java class -> {keyword -> child-class}\n")
      (.write w ";; Used to resolve child proto types based on parent class and keyword\n")
      (.write w "(def class->children\n")
      (.write w "  ")
      (.write w (pr-str class->children-keywords))
      (.write w ")\n\n")
      
      ;; Write leaf classes
      (.write w ";; Set of classes with no children (leaf nodes)\n")
      (.write w ";; These are terminal message types\n")
      (.write w "(def leaf-classes\n")
      (.write w "  ")
      (.write w (pr-str leaf-classes))
      (.write w ")\n"))
    
    ;; Return summary
    {:class-count (count class->children-keywords)
     :leaf-count (count leaf-classes)
     :total-mappings (reduce + (map count (vals class->children-keywords)))}))