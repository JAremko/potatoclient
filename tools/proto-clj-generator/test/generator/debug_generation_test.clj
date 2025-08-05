(ns generator.debug-generation-test
  "Debug actual generation"
  (:require [clojure.test :refer :all]
            [generator.frontend-namespaced :as frontend-ns]
            [clojure.string :as str]))

(deftest debug-actual-generation
  (let [test-backend-output
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
            :enums []}]}
         
         :state {:type :descriptor-set :files []}
         
         :type-lookup
         {:.cmd.Compass.Root {:name :root :package "cmd.Compass"}
          :.cmd.Compass.Start {:name :start :package "cmd.Compass"}}}
        
        generated (frontend-ns/generate-from-backend 
                   test-backend-output 
                   "potatoclient.proto")
        
        compass-content (get generated "cmd/compass.clj")]
    
    (println "\nGenerated files:" (keys generated))
    (println "\nCompass content length:" (count compass-content))
    
    ;; Count occurrences
    (let [build-root-matches (re-seq #"defn build-root" compass-content)
          parse-root-matches (re-seq #"defn parse-root" compass-content)
          build-root-exact (re-seq #"defn build-root\b" compass-content)
          parse-root-exact (re-seq #"defn parse-root\b" compass-content)]
      (println "\nbuild-root occurrences:" (count build-root-matches))
      (println "parse-root occurrences:" (count parse-root-matches))
      (println "\nbuild-root exact (with \\b):" (count build-root-exact))
      (println "parse-root exact (with \\b):" (count parse-root-exact))
      
      ;; Show all defn lines
      (println "\nAll defn lines:")
      (let [lines (str/split compass-content #"\n")]
        (doseq [[idx line] (map-indexed vector lines)]
          (when (str/includes? line "defn ")
            (println (format "Line %d: %s" (inc idx) (str/trim line)))))))))