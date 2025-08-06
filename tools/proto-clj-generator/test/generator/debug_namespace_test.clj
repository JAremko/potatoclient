(ns generator.debug-namespace-test
  "Debug namespace generation"
  (:require [clojure.test :refer :all]
            [generator.frontend-namespaced :as frontend-ns]
            [clojure.pprint :as pp]))

(deftest debug-generation
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
              :oneofs []}
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
        
        grouped (frontend-ns/group-by-file 
                 (get-in test-backend-output [:command :files]))]
    
    (println "\nGrouped data:")
    (pp/pprint grouped)
    
    ;; Check what messages are in the compass namespace
    (let [compass-messages (get-in grouped ["cmd.compass" :messages])]
      (println "\nCompass messages:")
      (pp/pprint compass-messages)
      (println "\nNumber of messages:" (count compass-messages))
      (println "Message names:" (map :name compass-messages)))))