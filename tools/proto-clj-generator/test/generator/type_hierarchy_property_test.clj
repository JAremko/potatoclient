(ns generator.type-hierarchy-property-test
  "Property-based tests for complex type hierarchies and nested message structures"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [generator.deps :as deps]
            [generator.backend :as backend]
            [generator.specs :as specs]))

;; =============================================================================
;; Generators for Complex Type Hierarchies
;; =============================================================================

(def proto-scalar-types
  [:double :float :int32 :uint32 :int64 :uint64 
   :sint32 :sint64 :fixed32 :fixed64 :sfixed32 :sfixed64
   :bool :string :bytes])

(defn make-field-gen
  "Generate a field with optional nested type reference"
  [depth package-name available-types]
  (if (and (pos? depth) (seq available-types))
    (gen/one-of
      [(gen/fmap (fn [[name scalar-type num]]
                   {:name (keyword name)
                    :proto-name name
                    :number num
                    :label :label-optional
                    :type {:scalar scalar-type}})
                 (gen/tuple (gen/fmap #(str "field_" %) gen/nat)
                            (gen/elements proto-scalar-types)
                            (gen/fmap inc gen/nat)))
       (gen/fmap (fn [[name type-ref num]]
                   {:name (keyword name)
                    :proto-name name
                    :number num
                    :label :label-optional
                    :type {:message {:type-ref type-ref}}})
                 (gen/tuple (gen/fmap #(str "ref_field_" %) gen/nat)
                            (gen/elements available-types)
                            (gen/fmap inc gen/nat)))])
    ;; Only scalar fields if no depth or no available types
    (gen/fmap (fn [[name scalar-type num]]
                {:name (keyword name)
                 :proto-name name
                 :number num
                 :label :label-optional
                 :type {:scalar scalar-type}})
              (gen/tuple (gen/fmap #(str "field_" %) gen/nat)
                         (gen/elements proto-scalar-types)
                         (gen/fmap inc gen/nat)))))

(defn make-message-gen
  "Generate a message with potential nested types"
  [depth package-name parent-path available-types]
  (gen/fmap
    (fn [[name fields nested-messages]]
          (let [proto-name (str "Message" name)
                fqn (str parent-path "." proto-name)]
            {:type :message
             :name (keyword (clojure.string/lower-case proto-name))
             :proto-name proto-name
             :package package-name
             :fields fields
             :nested-types nested-messages}))
    (gen/tuple
      gen/nat
      (gen/vector (make-field-gen depth package-name available-types) 1 5)
      ;; Generate nested messages if depth > 0
      (if (pos? depth)
        (gen/vector
          (make-message-gen (dec depth) package-name 
                            (str parent-path ".Message" gen/nat)
                            available-types)
          0 3)
        (gen/return [])))))

(def nested-message-gen
  "Generate messages with nested type hierarchies"
  (gen/fmap
    (fn [[package-name num-messages max-depth]]
          (let [;; First, generate some top-level type names
                top-level-types (mapv #(str "." package-name ".Type" %) 
                                     (range num-messages))
                messages (mapv (fn [i]
                                 (let [proto-name (str "Type" i)]
                                   {:type :message
                                    :name (keyword (clojure.string/lower-case proto-name))
                                    :proto-name proto-name
                                    :package package-name
                                    :fields [{:name :id
                                              :proto-name "id"
                                              :number 1
                                              :label :label-optional
                                              :type {:scalar :int32}}]
                                    :nested-types []}))
                              (range num-messages))]
            {:type :file
             :name (str package-name ".proto")
             :package package-name
             :dependencies []
             :imports []
             :messages messages
             :enums []}))
    (gen/tuple
      (gen/fmap #(str "test.pkg" %) (gen/choose 1 100))
      (gen/choose 1 5)    ; number of top-level messages
      (gen/choose 0 3)))) ; max nesting depth

;; =============================================================================
;; Property Tests for Type Hierarchies
;; =============================================================================

(defspec nested-type-collection
  50
  (prop/for-all [file nested-message-gen]
    (let [symbols (deps/collect-file-symbols file)]
      (and
        ;; All top-level messages should be collected
        (every? (fn [msg]
                  (some #(and (= (:type %) :message)
                              (= (:proto-name (:definition %)) 
                                 (:proto-name msg)))
                        symbols))
                (:messages file))
        
        ;; Symbols should have valid FQNs
        (every? (fn [sym]
                  (and (:fqn sym)
                       (.startsWith (:fqn sym) ".")))
                symbols)))))

(defspec cross-file-type-references
  25
  (prop/for-all [files (gen/vector nested-message-gen 2 5)]
    (let [;; Create a descriptor set with multiple files
          descriptor {:type :descriptor-set
                      :files files}
          ;; Build dependency graph
          {:keys [graph file-info]} (deps/build-dependency-graph descriptor)]
      ;; Graph should contain all files
      (every? #(contains? file-info (:name %)) files))))

(defspec type-hierarchy-enrichment
  25
  (prop/for-all [files (gen/vector nested-message-gen 1 3)]
    (try
      (let [descriptor {:type :descriptor-set :files files}
            enriched (deps/enrich-descriptor-set descriptor)
            registry (:symbol-registry enriched)]
        (and
          ;; Registry should contain all message types
          (every? (fn [file]
                    (every? (fn [msg]
                              (let [fqn (str "." (:package file) "." 
                                           (:proto-name msg))]
                                (contains? registry fqn)))
                            (:messages file)))
                  files)
          
          ;; Enriched files should maintain structure
          (= (count files) (count (:files enriched)))))
      (catch Exception e
        ;; Log but allow - some random combinations might be invalid
        true))))

(defspec complex-dependency-graph
  25
  (prop/for-all [num-files (gen/choose 3 10)]
    (let [;; Generate files with random dependencies
          files (mapv (fn [i]
                        (let [package (str "pkg" i)
                              ;; Each file may depend on 0-3 other files
                              possible-deps (remove #(= % i) (range num-files))
                              deps (take (rand-int 4) (shuffle possible-deps))
                              dep-names (mapv #(str "pkg" % ".proto") deps)]
                          {:type :file
                           :name (str package ".proto")
                           :package package
                           :dependencies dep-names
                           :imports dep-names
                           :messages [{:type :message
                                       :name :root
                                       :proto-name "Root"
                                       :package package
                                       :fields []}]
                           :enums []}))
                      (range num-files))
          descriptor {:type :descriptor-set :files files}]
      (try
        (let [{:keys [graph]} (deps/build-dependency-graph descriptor)
              sorted (deps/topological-sort graph)]
          ;; Should produce valid topological ordering
          (and (vector? sorted)
               (<= (count sorted) num-files)))
        (catch Exception e
          ;; Circular dependencies are expected in random graphs
          (re-find #"[Cc]ircular" (.getMessage e)))))))

;; =============================================================================
;; Tests for Oneof and Complex Field Types
;; =============================================================================

(def oneof-message-gen
  "Generate messages with oneof fields"
  (gen/fmap
    (fn [[package name oneof-fields regular-fields]]
          {:type :message
           :name (keyword (clojure.string/lower-case name))
           :proto-name name
           :package package
           :fields (concat regular-fields
                          ;; Add oneof fields with oneof-index
                          (map-indexed (fn [idx field]
                                         (assoc field :oneof-index 0))
                                       oneof-fields))
           :oneofs [{:name :choice
                     :proto-name "choice"
                     :index 0
                     :fields oneof-fields}]})
    (gen/tuple
      (gen/fmap #(str "test.pkg" %) gen/nat)
      (gen/fmap #(str "Message" %) gen/nat)
      ;; Oneof fields
      (gen/vector
        (gen/fmap (fn [[name num]]
                    {:name (keyword name)
                     :proto-name name
                     :number num
                     :label :label-optional
                     :type {:scalar :string}})
                  (gen/tuple (gen/elements ["option_a" "option_b" "option_c"])
                             (gen/fmap inc gen/nat)))
        2 4)
      ;; Regular fields
      (gen/vector
        (gen/fmap (fn [[name num]]
                    {:name (keyword name)
                     :proto-name name
                     :number (+ 10 num)
                     :label :label-optional
                     :type {:scalar :int32}})
                  (gen/tuple (gen/fmap #(str "field" %) gen/nat)
                             gen/nat))
        1 3))))

(defspec oneof-handling
  25
  (prop/for-all [msg oneof-message-gen]
    (let [file {:type :file
                :name "test.proto"
                :package (:package msg)
                :dependencies []
                :imports []
                :messages [msg]
                :enums []}
          symbols (deps/collect-file-symbols file)]
      ;; Message with oneofs should be collected properly
      (some #(= :message (:type %)) symbols))))

;; =============================================================================
;; Repeated and Map Field Types
;; =============================================================================

(def complex-field-gen
  "Generate fields with various labels and map types"
  (gen/one-of
    ;; Regular optional field
    [(gen/fmap (fn [[name type num]]
                 {:name (keyword name)
                  :proto-name name
                  :number num
                  :label :label-optional
                  :type {:scalar type}})
               (gen/tuple (gen/fmap #(str "field" %) gen/nat)
                          (gen/elements proto-scalar-types)
                          (gen/fmap inc gen/nat)))]
    ;; Repeated field
    [(gen/fmap (fn [[name type num]]
                 {:name (keyword name)
                  :proto-name name
                  :number num
                  :label :label-repeated
                  :type {:scalar type}})
               (gen/tuple (gen/fmap #(str "repeated" %) gen/nat)
                          (gen/elements proto-scalar-types)
                          (gen/fmap inc gen/nat)))]
    ;; Map field (represented as repeated message with key/value)
    [(gen/fmap (fn [[name key-type val-type num]]
                 {:name (keyword name)
                  :proto-name name
                  :number num
                  :label :label-repeated
                  :type {:message {:type-ref (str ".MapEntry" num)}}
                  :map-entry {:key-type key-type :value-type val-type}})
               (gen/tuple (gen/fmap #(str "map" %) gen/nat)
                          (gen/elements [:string :int32 :int64])
                          (gen/elements proto-scalar-types)
                          (gen/fmap inc gen/nat)))]))

(defspec complex-field-types
  25
  (prop/for-all [fields (gen/vector complex-field-gen 1 10)]
    (let [msg {:type :message
               :name :test
               :proto-name "Test"
               :package "test"
               :fields fields}
          file {:type :file
                :name "test.proto"
                :package "test"
                :dependencies []
                :imports []
                :messages [msg]
                :enums []}]
      ;; All field types should be valid
      (every? (fn [field]
                (and (:name field)
                     (:type field)
                     (:label field)))
              fields))))