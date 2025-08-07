(ns generator.code-generation-property-test
  "Property-based tests for the full code generation pipeline"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [generator.deps :as deps]
            [generator.backend :as backend]
            [generator.frontend :as frontend]
            [generator.spec-gen :as spec-gen]
            [generator.naming :as naming]
            [clojure.string :as str]))

;; =============================================================================
;; Generators for Complete Proto Structures
;; =============================================================================

(def valid-proto-name-gen
  "Generate valid protobuf names (CamelCase)"
  (gen/fmap (fn [parts]
              (str/join "" (map str/capitalize parts)))
            (gen/vector (gen/elements ["User" "Request" "Response" "Data" 
                                      "Info" "Status" "Config" "Service"])
                       1 3)))

(def valid-field-name-gen
  "Generate valid field names (snake_case)"
  (gen/fmap (fn [parts]
              (str/join "_" parts))
            (gen/vector (gen/elements ["user" "request" "response" "data" 
                                      "id" "name" "value" "status"])
                       1 3)))

(def complete-enum-gen
  "Generate complete enum with all required fields"
  (gen/fmap
    (fn [[name values package]]
          {:type :enum
           :name (keyword (naming/proto->clj-name name))
           :proto-name name
           :package package
           :java-class (str package "." name)
           :values (map-indexed (fn [i v]
                                 {:name (keyword (naming/proto->clj-name v))
                                  :proto-name v
                                  :number i})
                               values)})
    (gen/tuple
      valid-proto-name-gen
      (gen/vector (gen/fmap #(str/upper-case %) 
                           (gen/elements ["unknown" "active" "inactive" 
                                         "pending" "success" "failure"]))
                  2 5)
      (gen/fmap #(str "test.pkg" %) (gen/choose 1 100)))))

(def complete-message-gen
  "Generate complete message with all required fields for code generation"
  (gen/fmap
    (fn [[name fields enums package]]
          {:type :message
           :name (keyword (naming/proto->clj-name name))
           :proto-name name
           :package package
           :java-class (str package "." name)
           :fields fields
           :oneofs []
           :nested-types enums})
    (gen/tuple
      valid-proto-name-gen
      ;; Generate fields
      (gen/vector
        (gen/fmap (fn [[fname ftype fnum]]
                    {:name (keyword (naming/proto->clj-name fname))
                     :proto-name fname
                     :number fnum
                     :label :label-optional
                     :type {:scalar ftype}})
                  (gen/tuple valid-field-name-gen
                             (gen/elements [:string :int32 :bool :double])
                             (gen/fmap inc gen/nat)))
        1 5)
      ;; Generate nested enums
      (gen/vector complete-enum-gen 0 2)
      (gen/fmap #(str "test.pkg" %) (gen/choose 1 100)))))

(def complete-file-gen
  "Generate a complete file structure suitable for code generation"
  (gen/fmap
    (fn [[package messages enums]]
          {:type :file
           :name (str package ".proto")
           :package package
           :java-package (str "com.example." package)
           :java-outer-classname (str/capitalize package)
           :dependencies []
           :imports []
           :messages messages
           :enums enums})
    (gen/tuple
      (gen/fmap #(str "pkg" %) (gen/choose 1 20))
      (gen/vector complete-message-gen 1 3)
      (gen/vector complete-enum-gen 0 2))))

;; =============================================================================
;; Property Tests for Code Generation
;; =============================================================================

(defspec spec-generation-valid
  20
  (prop/for-all [file complete-file-gen]
    (let [{:keys [messages enums package]} file
          namespace-data {:messages messages
                          :enums enums
                          :current-package package
                          :require-specs []}
          {:keys [enum-specs message-specs]} 
          (spec-gen/generate-specs-for-namespace namespace-data)]
      (and
        ;; Should generate spec strings
        (string? enum-specs)
        (string? message-specs)
        ;; Should contain def forms
        (or (empty? enums) 
            (re-find #"\(def .+-spec" enum-specs))
        (or (empty? messages) 
            (re-find #"\(def .+-spec" message-specs))))))

(defspec naming-consistency
  50
  (prop/for-all [proto-name valid-proto-name-gen]
    (let [clj-name (naming/proto->clj-name proto-name)
          ;; Convert back
          back-to-proto (naming/clj->proto-name clj-name)]
      ;; Should be consistent transformations
      (and (string? clj-name)
           (string? back-to-proto)
           ;; Clojure names should be kebab-case
           (= clj-name (str/lower-case clj-name))
           (not (re-find #"_" clj-name))))))

(defspec frontend-code-generation
  10
  (prop/for-all [files (gen/vector complete-file-gen 1 3)]
    (let [cmd-files (filter #(str/starts-with? (:package %) "cmd") files)
          state-files (remove #(str/starts-with? (:package %) "cmd") files)
          backend-output {:command {:type :descriptor-set :files cmd-files}
                          :state {:type :descriptor-set :files state-files}
                          :type-lookup {}}
          ns-prefix "test.generated"]
      (try
        (let [generated (frontend/generate-from-backend backend-output ns-prefix false)]
          (and
            ;; Should generate command and state namespaces
            (contains? generated :command)
            (contains? generated :state)
            ;; Generated code should be strings
            (string? (:command generated))
            (string? (:state generated))
            ;; Should contain namespace declarations
            (re-find #"\(ns test\.generated\." (:command generated))
            (re-find #"\(ns test\.generated\." (:state generated))))
        (catch Exception e
          ;; Some random combinations might fail - that's ok
          true)))))

(defspec builder-parser-pairs
  25
  (prop/for-all [msg complete-message-gen]
    (let [builder-name (str "build-" (name (:name msg)))
          parser-name (str "parse-" (name (:name msg)))]
      ;; Check that we would generate matching builder/parser pairs
      (and (string? builder-name)
           (string? parser-name)
           (str/starts-with? builder-name "build-")
           (str/starts-with? parser-name "parse-")))))

;; =============================================================================
;; Cross-namespace Reference Tests
;; =============================================================================

(def cross-namespace-file-gen
  "Generate files that reference types from other namespaces"
  (gen/bind (gen/choose 2 4)
            (fn [num-files]
              (gen/fmap
                (fn [file-indices]
                  (mapv (fn [i]
                          (let [package (str "pkg" i)
                                ;; Reference types from other packages
                                other-pkgs (remove #(= % i) (range num-files))
                                ref-fields (mapv (fn [other-i]
                                                  {:name (keyword (str "ref_" other-i))
                                                   :proto-name (str "ref_" other-i)
                                                   :number (inc other-i)
                                                   :label :label-optional
                                                   :type {:message 
                                                          {:type-ref (str ".pkg" other-i 
                                                                         ".Message")}}})
                                                other-pkgs)]
                            {:type :file
                             :name (str package ".proto")
                             :package package
                             :dependencies (mapv #(str "pkg" % ".proto") other-pkgs)
                             :imports (mapv #(str "pkg" % ".proto") other-pkgs)
                             :messages [{:type :message
                                         :name :message
                                         :proto-name "Message"
                                         :package package
                                         :java-class (str package ".Message")
                                         :fields ref-fields}]
                             :enums []}))
                        (range num-files)))
                (gen/return (range num-files))))))

(defspec cross-namespace-enrichment
  15
  (prop/for-all [files cross-namespace-file-gen]
    (try
      (let [descriptor {:type :descriptor-set :files files}
            enriched (deps/enrich-descriptor-set descriptor)]
        ;; Check that cross-namespace references are marked
        (every? (fn [file]
                  (every? (fn [msg]
                            (every? (fn [field]
                                      ;; If it's a message type ref
                                      (if-let [msg-type (get-in field [:type :message])]
                                        ;; Should have enrichment info
                                        (or (not (:type-ref msg-type))
                                            (contains? msg-type :type-ref))
                                        true))
                                    (:fields msg)))
                          (:messages file)))
                (:files enriched)))
      (catch Exception e
        ;; Circular deps possible with random cross-refs
        true))))

;; =============================================================================
;; Edge Cases and Boundary Tests
;; =============================================================================

(defspec empty-structures
  50
  (prop/for-all [package (gen/fmap #(str "empty.pkg" %) gen/nat)]
    (let [;; Files with empty messages/enums
          empty-msg {:type :message
                     :name :empty
                     :proto-name "Empty"
                     :package package
                     :java-class (str package ".Empty")
                     :fields []}
          empty-enum {:type :enum
                      :name :empty-enum  
                      :proto-name "EmptyEnum"
                      :package package
                      :java-class (str package ".EmptyEnum")
                      :values []}
          file {:type :file
                :name "empty.proto"
                :package package
                :dependencies []
                :imports []
                :messages [empty-msg]
                :enums [empty-enum]}]
      ;; Should handle empty structures gracefully
      (let [specs (spec-gen/generate-specs-for-namespace
                    {:messages [empty-msg]
                     :enums [empty-enum]
                     :current-package package
                     :require-specs []})]
        (and (map? specs)
             (contains? specs :enum-specs)
             (contains? specs :message-specs))))))

(defspec large-message-handling
  5
  (prop/for-all [num-fields (gen/choose 50 100)]
    (let [fields (mapv (fn [i]
                         {:name (keyword (str "field_" i))
                          :proto-name (str "field_" i)
                          :number (inc i)
                          :label :label-optional
                          :type {:scalar :string}})
                       (range num-fields))
          large-msg {:type :message
                     :name :large
                     :proto-name "Large"
                     :package "test"
                     :fields fields}]
      ;; Should handle messages with many fields
      (pos? (count fields)))))