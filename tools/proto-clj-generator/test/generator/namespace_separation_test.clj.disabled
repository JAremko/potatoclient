(ns generator.namespace-separation-test
  "Test namespace separation functionality"
  (:require [clojure.test :refer :all]
            [generator.core-namespaced :as core-ns]
            [generator.frontend-namespaced :as frontend-ns]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def test-backend-output
  "Simplified backend output for testing"
  {:command
   {:type :descriptor-set
    :files
    [{:type :file
      :name "jon_shared_cmd_compass.proto"
      :package "cmd.Compass"
      :java-package nil
      :java-outer-classname "JonSharedCmdCompass"
      :messages
      [{:type :message
        :name :root
        :proto-name "Root"
        :java-class "cmd.Compass.JonSharedCmdCompass$Root"
        :fields []
        :oneofs
        [{:name :cmd
          :proto-name "cmd"
          :fields
          [{:name :start
            :proto-name "start"
            :type {:message {:type-ref ".cmd.Compass.Start"}}}]}]}
       {:type :message
        :name :start
        :proto-name "Start"
        :java-class "cmd.Compass.JonSharedCmdCompass$Start"
        :fields []}]
      :enums []}
     {:type :file
      :name "jon_shared_cmd_gps.proto"
      :package "cmd.Gps"
      :java-package nil
      :java-outer-classname "JonSharedCmdGps"
      :messages
      [{:type :message
        :name :root
        :proto-name "Root"
        :java-class "cmd.Gps.JonSharedCmdGps$Root"
        :fields []
        :oneofs []}]
      :enums []}]}
   
   :state
   {:type :descriptor-set
    :files
    [{:type :file
      :name "jon_shared_data_gps.proto"
      :package "ser.Gps"
      :java-package nil
      :java-outer-classname "JonSharedDataGps"
      :messages
      [{:type :message
        :name :state
        :proto-name "State"
        :java-class "ser.Gps.JonSharedDataGps$State"
        :fields []}]
      :enums []}]}
   
   :type-lookup
   {:.cmd.Compass.Root {:name :root :package "cmd.Compass" :java-class "cmd.Compass.JonSharedCmdCompass$Root"}
    :.cmd.Compass.Start {:name :start :package "cmd.Compass" :java-class "cmd.Compass.JonSharedCmdCompass$Start"}
    :.cmd.Gps.Root {:name :root :package "cmd.Gps" :java-class "cmd.Gps.JonSharedCmdGps$Root"}
    :.ser.Gps.State {:name :state :package "ser.Gps" :java-class "ser.Gps.JonSharedDataGps$State"}}})

(deftest package-namespace-conversion-test
  (testing "Package to namespace conversion"
    (is (= "cmd.compass" (frontend-ns/package->namespace "cmd.Compass")))
    (is (= "cmd.gps" (frontend-ns/package->namespace "cmd.Gps")))
    (is (= "ser.gps" (frontend-ns/package->namespace "ser.Gps")))
    (is (= "cmd.day-camera" (frontend-ns/package->namespace "cmd.Day_Camera")))))

(deftest package-file-path-conversion-test
  (testing "Package to file path conversion"
    (is (= "cmd/compass.clj" (frontend-ns/package->file-path "cmd.Compass")))
    (is (= "cmd/gps.clj" (frontend-ns/package->file-path "cmd.Gps")))
    (is (= "ser/gps.clj" (frontend-ns/package->file-path "ser.Gps")))))

(deftest group-by-package-test
  (testing "Grouping files by package"
    (let [files (concat (get-in test-backend-output [:command :files])
                        (get-in test-backend-output [:state :files]))
          grouped (frontend-ns/group-by-package files)]
      
      (is (= #{"cmd.compass" "cmd.gps" "ser.gps"} (set (keys grouped))))
      
      (is (= 2 (count (get-in grouped ["cmd.compass" :messages]))))
      (is (= 1 (count (get-in grouped ["cmd.gps" :messages]))))
      (is (= 1 (count (get-in grouped ["ser.gps" :messages]))))
      
      (is (= "cmd.Compass" (get-in grouped ["cmd.compass" :package])))
      (is (= "cmd.Gps" (get-in grouped ["cmd.gps" :package]))))))

(deftest type-ref-resolution-test
  (testing "Type reference resolution within same namespace"
    (let [result (frontend-ns/resolve-type-refs 
                  ".cmd.Compass.Start" 
                  (:type-lookup test-backend-output)
                  "cmd.Compass"
                  "potatoclient.proto")]
      (is (= "start" (:type-name result)))
      (is (nil? (:imports result)))))
  
  (testing "Type reference resolution across namespaces"
    (let [result (frontend-ns/resolve-type-refs 
                  ".cmd.Gps.Root" 
                  (:type-lookup test-backend-output)
                  "cmd.Compass"
                  "potatoclient.proto")]
      (is (= "cmd.gps/root" (:type-name result)))
      (is (= ["potatoclient.proto.cmd.gps"] (:imports result))))))

(deftest generate-namespaced-files-test
  (testing "Generate files with namespace separation"
    (let [generated (frontend-ns/generate-from-backend 
                     test-backend-output 
                     "potatoclient.proto")]
      
      ;; Check that we get the expected files
      (is (contains? generated "cmd/compass.clj"))
      (is (contains? generated "cmd/gps.clj"))
      (is (contains? generated "ser/gps.clj"))
      (is (contains? generated "command.clj"))
      (is (contains? generated "state.clj"))
      
      ;; Check that compass namespace contains expected content
      (let [compass-content (get generated "cmd/compass.clj")]
        (is (str/includes? compass-content "ns potatoclient.proto.cmd.compass"))
        (is (str/includes? compass-content "build-root"))
        (is (str/includes? compass-content "build-start"))
        (is (str/includes? compass-content "parse-root"))
        (is (str/includes? compass-content "parse-start")))
      
      ;; Check that there's only one build-root in compass namespace
      (let [compass-content (get generated "cmd/compass.clj")
            build-root-count (count (re-seq #"defn build-root(?!-)" compass-content))]
        (is (= 1 build-root-count) "Should have exactly one build-root in compass namespace"))
      
      ;; Check index files
      (let [cmd-index (get generated "command.clj")]
        (is (str/includes? cmd-index "ns potatoclient.proto.command"))
        (is (str/includes? cmd-index "Commands index"))))))

(deftest no-duplicate-functions-test
  (testing "No duplicate function names across namespaces"
    (let [generated (frontend-ns/generate-from-backend 
                     test-backend-output 
                     "potatoclient.proto")
          ;; Get all namespace files (excluding index files)
          ns-files (filter #(str/includes? % "/") (keys generated))]
      
      ;; Each namespace should have its own build-root without conflicts
      (doseq [file-path ns-files]
        (let [content (get generated file-path)
              build-root-count (count (re-seq #"defn build-root(?!-)" content))
              parse-root-count (count (re-seq #"defn parse-root(?!-)" content))]
          ;; Each namespace with a Root message should have exactly one build-root
          (when (str/includes? content "build-root")
            (is (= 1 build-root-count) 
                (str "Should have exactly one build-root in " file-path)))
          (when (str/includes? content "parse-root")
            (is (= 1 parse-root-count) 
                (str "Should have exactly one parse-root in " file-path))))))))