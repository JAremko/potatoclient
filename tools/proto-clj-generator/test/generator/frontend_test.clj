(ns generator.frontend-test
  "Test frontend code generation, especially repeated field handling."
  (:require [clojure.test :refer [deftest testing is]]
            [generator.frontend :as frontend]
            [clojure.string :as str]))

(def sample-field
  {:name :tags
   :proto-name "tags"
   :number 1
   :type {:unknown {:proto-type :type-string}}
   :label :label-repeated
   :repeated? true})

(def sample-singular-field
  {:name :name
   :proto-name "name"
   :number 2
   :type {:unknown {:proto-type :type-string}}
   :label :label-optional})

(def sample-enum
  {:type :enum
   :name :status
   :proto-name "Status"
   :java-class "com.example.Test$Status"
   :values [{:name :unknown :proto-name "UNKNOWN" :number 0}
            {:name :active :proto-name "ACTIVE" :number 1}
            {:name :inactive :proto-name "INACTIVE" :number 2}]})

(deftest field-setter-generation-test
  (testing "Repeated field setter uses addAll"
    (let [setter (frontend/generate-field-setter sample-field)]
      (is (str/includes? setter "addAllTags"))
      (is (str/includes? setter "(contains? m :tags)"))
      (is (str/includes? setter "(get m :tags)"))))
  
  (testing "Singular field setter uses set"
    (let [setter (frontend/generate-field-setter sample-singular-field)]
      (is (str/includes? setter ".setName"))
      (is (not (str/includes? setter "addAll")))
      (is (str/includes? setter "(contains? m :name)")))))

(deftest field-getter-generation-test
  (testing "Repeated field getter uses getList and vec"
    (let [getter (frontend/generate-field-getter sample-field)]
      (is (str/includes? getter "true"))  ; No has check
      (is (str/includes? getter ".getTagsList"))
      (is (str/includes? getter "(vec"))
      (is (str/includes? getter ":tags"))))
  
  (testing "Singular field getter uses has check"
    (let [getter (frontend/generate-field-getter sample-singular-field)]
      (is (str/includes? getter ".hasName"))
      (is (str/includes? getter ".getName"))
      (is (not (str/includes? getter "vec")))
      (is (str/includes? getter ":name")))))

(deftest enum-generation-test
  (testing "Enum value mapping generation"
    (let [enum-def (frontend/generate-enum-def sample-enum)]
      ;; Check values mapping
      (is (str/includes? enum-def "status-values"))
      (is (str/includes? enum-def ":unknown com.example.Test$Status/UNKNOWN"))
      (is (str/includes? enum-def ":active com.example.Test$Status/ACTIVE"))
      (is (str/includes? enum-def ":inactive com.example.Test$Status/INACTIVE"))
      
      ;; Check keywords mapping
      (is (str/includes? enum-def "status-keywords"))
      (is (str/includes? enum-def "com.example.Test$Status/UNKNOWN :unknown"))
      (is (str/includes? enum-def "com.example.Test$Status/ACTIVE :active"))
      (is (str/includes? enum-def "com.example.Test$Status/INACTIVE :inactive")))))

(def sample-message-with-repeated
  {:type :message
   :name :example
   :proto-name "Example"
   :java-class "com.example.Test$Example"
   :fields [{:name :tags
             :proto-name "tags"
             :number 1
             :type {:unknown {:proto-type :type-string}}
             :label :label-repeated
             :repeated? true}
            {:name :name
             :proto-name "name"
             :number 2
             :type {:unknown {:proto-type :type-string}}
             :label :label-optional}]
   :oneofs []})

(deftest message-generation-test
  (testing "Builder handles repeated fields correctly"
    (let [builder (frontend/generate-builder sample-message-with-repeated {})]
      (is (str/includes? builder "build-example"))
      (is (str/includes? builder ".addAllTags"))
      (is (str/includes? builder ".setName"))
      (is (str/includes? builder "com.example.Test$Example/newBuilder"))))
  
  (testing "Parser handles repeated fields correctly"
    (let [parser (frontend/generate-parser sample-message-with-repeated {})]
      (is (str/includes? parser "parse-example"))
      (is (str/includes? parser "(vec (.getTagsList proto))"))
      (is (str/includes? parser ".hasName proto) (assoc :name (.getName proto))")))))

(deftest template-replacement-test
  (testing "Template replacements work correctly"
    (let [template "Hello FIELD-NAME from METHOD-NAME"
          replacements {"FIELD-NAME" "Tags"
                       "METHOD-NAME" ".addAllTags"}
          result (frontend/replace-in-template template replacements)]
      (is (= "Hello Tags from .addAllTags" result)))))

(deftest import-collection-test
  (testing "Import collection filters protobuf classes"
    (let [edn-data {:files [{:messages [{:java-class "com.example.Test$Message"}
                                       {:java-class "com.google.protobuf.Empty"}]
                            :enums [{:java-class "com.example.Test$Status"}
                                   {:java-class "build.buf.validate.Constraint"}]}]}
          imports (frontend/collect-imports edn-data)]
      (is (= ["com.example.Test"] imports))
      (is (not (some #(str/includes? % "protobuf") imports)))
      (is (not (some #(str/includes? % "validate") imports))))))