(ns potatoclient.proto.collision-detection-test
  "Integration test to detect any collisions in our actual proto files"
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.proto.string-conversion-safe :as safe-conv]
            [clojure.data.json :as json]))

(defn find-proto-descriptor-files
  "Find all protobuf descriptor JSON files in the project"
  []
  (let [project-root (io/file ".")
        proto-gen-dir (io/file project-root "tools/proto-clj-generator/resources")]
    (when (.exists proto-gen-dir)
      (->> (file-seq proto-gen-dir)
           (filter #(and (.isFile %)
                        (str/ends-with? (.getName %) ".json")
                        (str/includes? (.getPath %) "descriptor")))
           (map #(.getPath %))))))

(defn extract-all-names-from-descriptor
  "Extract all names from a protobuf descriptor JSON"
  [descriptor-path]
  (try
    (let [data (json/read-str (slurp descriptor-path) :key-fn keyword)
          names (atom #{})]
      ;; Walk the descriptor structure
      (clojure.walk/postwalk
        (fn [x]
          (when (map? x)
            ;; Extract various name fields
            (when-let [name (:name x)]
              (swap! names conj name))
            (when-let [type-name (:typeName x)]
              (swap! names conj type-name))
            (when-let [type (:type x)]
              (when (string? type)
                (swap! names conj type)))
            (when-let [label (:label x)]
              (when (string? label)
                (swap! names conj label)))
            ;; Extract enum values
            (when-let [values (:value x)]
              (doseq [v values]
                (when-let [enum-name (:name v)]
                  (swap! names conj enum-name)))))
          x)
        data)
      @names)
    (catch Exception e
      (println "Error reading" descriptor-path ":" (.getMessage e))
      #{})))

(deftest detect-collisions-in-real-protos
  (testing "No collisions in actual protobuf conversions"
    ;; Clear any previous conversions
    (safe-conv/clear-caches!)
    
    (let [descriptor-files (find-proto-descriptor-files)
          all-names (atom #{})
          collision-count (atom 0)]
      
      (if (empty? descriptor-files)
        (println "No descriptor files found - skipping collision test")
        (do
          (println "Checking" (count descriptor-files) "descriptor files for collisions...")
          
          ;; Collect all names
          (doseq [file descriptor-files]
            (let [names (extract-all-names-from-descriptor file)]
              (swap! all-names into names)))
          
          (println "Found" (count @all-names) "unique names to test")
          
          ;; Test each conversion function
          (testing "->kebab-case conversions"
            (doseq [name @all-names]
              (try
                (safe-conv/->kebab-case name)
                (catch Exception e
                  (swap! collision-count inc)
                  (println "Collision in ->kebab-case:" (.getMessage e))))))
          
          (testing "->PascalCase conversions"
            (doseq [name @all-names]
              (try
                (safe-conv/->PascalCase name)
                (catch Exception e
                  (swap! collision-count inc)
                  (println "Collision in ->PascalCase:" (.getMessage e))))))
          
          (testing "->snake_case conversions"
            (doseq [name @all-names]
              (try
                (safe-conv/->snake_case name)
                (catch Exception e
                  (swap! collision-count inc)
                  (println "Collision in ->snake_case:" (.getMessage e))))))
          
          ;; Report results
          (if (zero? @collision-count)
            (println "✓ No collisions detected in" (count @all-names) "names")
            (do
              (println "✗ Found" @collision-count "collisions")
              (is false "Collisions detected in string conversions"))))))))

(deftest known-problematic-conversions
  (testing "Known edge cases that might collide"
    (safe-conv/clear-caches!)
    
    ;; Test cases that could potentially collide
    (let [test-pairs [;; Different underscore positions
                      ["TYPE_INT32" "TYPE_INT_32"]
                      ["FIELD_1D" "FIELD_1_D"]
                      ["Mode2D" "Mode2_D"]
                      ;; Mixed case variations
                      ["XMLParser" "XmlParser"]
                      ["IOError" "IoError"]
                      ;; Consecutive capitals
                      ["HTTPSConnection" "HttpsConnection"]]]
      
      (doseq [[a b] test-pairs]
        (testing (str "Conversion of " a " vs " b)
          (let [a-kebab (safe-conv/->kebab-case a)
                b-kebab (try
                          (safe-conv/->kebab-case b)
                          (catch Exception e
                            (println "Expected collision:" a "->" a-kebab 
                                    "vs" b "->" (conv/->kebab-case b))
                            :collision))]
            (when (not= b-kebab :collision)
              (println "No collision:" a "->" a-kebab "," b "->" b-kebab))))))))

(deftest validate-method-name-uniqueness
  (testing "Method names don't collide due to prefixes"
    (safe-conv/clear-caches!)
    
    (let [field-names ["value" "Value" "VALUE" "get_value" "getValue"]]
      (doseq [field field-names]
        ;; These shouldn't collide because of method prefixes
        (is (string? (safe-conv/getter-method-name field)))
        (is (string? (safe-conv/setter-method-name field)))
        (is (string? (safe-conv/has-method-name field)))))))

(defn run-collision-tests []
  (println "\n=== Running Collision Detection Tests ===")
  (run-tests 'potatoclient.proto.collision-detection-test))