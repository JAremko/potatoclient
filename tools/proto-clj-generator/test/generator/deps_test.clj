(ns generator.deps-test
  (:require [clojure.test :refer [deftest is testing]]
            [generator.deps :as deps]))

(deftest test-topological-sort
  (testing "Simple dependency graph"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "C" :package "pkg.C" :dependencies []}
                              {:type :file :name "B" :package "pkg.B" :dependencies ["C"]}
                              {:type :file :name "A" :package "pkg.A" :dependencies ["B"]}]}
          {:keys [graph]} (deps/build-dependency-graph descriptor)
          sorted (deps/topological-sort graph)]
      (is (= ["C" "B" "A"] sorted))))
  
  (testing "Multiple dependencies"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "D" :package "pkg.D" :dependencies []}
                              {:type :file :name "B" :package "pkg.B" :dependencies ["D"]}
                              {:type :file :name "C" :package "pkg.C" :dependencies ["D"]}
                              {:type :file :name "A" :package "pkg.A" :dependencies ["B" "C"]}]}
          {:keys [graph]} (deps/build-dependency-graph descriptor)
          sorted (deps/topological-sort graph)]
      (is (= "D" (first sorted)))
      (is (= "A" (last sorted)))))
  
  (testing "Circular dependency detection"
    (let [descriptor {:type :descriptor-set
                      :files [{:type :file :name "A" :package "pkg.A" :dependencies ["B"]}
                              {:type :file :name "B" :package "pkg.B" :dependencies ["A"]}]}]
      (is (thrown-with-msg? Exception #"[Cc]ircular"
                            (deps/build-dependency-graph descriptor))))))

(deftest test-symbol-collection
  (testing "Collect symbols from file"
    (let [file {:type :file
                :package "test.pkg"
                :enums [{:type :enum
                         :proto-name "Status"
                         :name :status}]
                :messages [{:type :message
                            :proto-name "Request"
                            :name :request
                            :nested-types [{:type :enum
                                            :proto-name "Type"
                                            :name :type}]}]}
          symbols (deps/collect-file-symbols file)]
      (is (= 3 (count symbols)))
      (is (= #{".test.pkg.Status" 
               ".test.pkg.Request" 
               ".test.pkg.Request.Type"}
             (set (map :fqn symbols)))))))

(deftest test-type-enrichment
  (testing "Enrich cross-namespace enum reference in file"
    (let [file {:type :file
                :package "cmd.camera"
                :messages [{:type :message
                            :proto-name "SetMode"
                            :name :set-mode
                            :fields [{:name :mode
                                      :proto-name "mode"
                                      :number 1
                                      :label :label-optional
                                      :type {:enum {:type-ref ".ser.FxMode"}}}]}]}
          registry {".ser.FxMode" {:fqn ".ser.FxMode"
                                   :type :enum
                                   :definition {:package "ser"
                                                :name :fx-mode}}}
          enriched (deps/enrich-file file registry)]
      (is (get-in enriched [:messages 0 :fields 0 :type :enum :cross-namespace]))
      (is (= "ser" (get-in enriched [:messages 0 :fields 0 :type :enum :target-package]))))))