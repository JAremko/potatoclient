(ns proto-explorer.main
  "Main entry point for proto-explorer - protobuf message inspection tool."
  (:require [proto-explorer.cli-final :as cli-final]
            [proto-explorer.cli-jvm :as cli-jvm]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-v" "--verbose" "Verbose output"
    :default false]
   ["-h" "--help"]])

(defn -main
  "Main entry point."
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (do (println "Proto Explorer - Protobuf message inspection tool")
          (println "\nCommands:")
          (println "  java-class <message>       Get Java class info for a protobuf message")
          (println "  java-fields <message>      Get proto field to Java method mapping")
          (println "  java-builder <message>     Get Java builder info")
          (println "  java-summary <message>     Get full summary with Java, Pronto EDN, and descriptor info")
          (println "  pronto-edn <message>       Get Pronto EDN representation of a message")
          (println "  descriptor-info <message>  Get JSON descriptor with buf.validate constraints as EDN")
          (println "\nExamples:")
          (println "  proto-explorer java-summary Root")
          (println "  proto-explorer pronto-edn JonGUIState")
          (println "  proto-explorer java-fields SetAzimuthValue")
          (println "\nOptions:")
          (println summary))
      
      errors
      (do (println "Errors:")
          (doseq [e errors]
            (println "  " e))
          (System/exit 1))
      
      ;; Main commands
      (#{"search" "search-java" "list" "info" "info-edn"} (first arguments))
      (cli-final/dispatch-command (first arguments) (rest arguments))
      
      ;; Legacy Java class info commands
      (#{"java-class" "java-fields" "java-builder" "java-summary" "pronto-edn" "descriptor-info"} (first arguments))
      (cli-jvm/dispatch-command (first arguments) (rest arguments))
      
      :else
      (do (println "Usage: proto-explorer COMMAND [options]")
          (println "\n=== MAIN COMMANDS ===")
          (println "  search <query>        Fuzzy search for protobuf message classes")
          (println "  search-java <query>   Search specifically by Java class name")
          (println "  info <class-name>     Get all information about a class (use full name from search)")
          (println "  info-edn <class-name> Get all information as EDN")
          (println "\n=== LEGACY COMMANDS ===")
          (println "  java-class <message>       Get Java class info")
          (println "  java-fields <message>      Get proto field to Java method mapping")
          (println "  java-builder <message>     Get Java builder info")
          (println "  java-summary <message>     Get full summary")
          (println "  pronto-edn <message>       Get Pronto EDN representation")
          (println "  descriptor-info <message>  Get JSON descriptor info")
          (println "\nExamples:")
          (println "  proto-explorer search root")
          (println "  proto-explorer search-java JonSharedCmd")
          (println "  proto-explorer info 'cmd.JonSharedCmd$Root'")
          (println "\nRun 'proto-explorer --help' for more information")))))