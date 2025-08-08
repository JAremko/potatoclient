(ns proto-explorer.main
  "Main entry point for proto-explorer - protobuf message inspection tool."
  (:require [proto-explorer.cli-jvm :as cli-jvm]
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
          (println "  java-class <message>    Get Java class info for a protobuf message")
          (println "  java-fields <message>   Get proto field to Java method mapping")
          (println "  java-builder <message>  Get Java builder info")
          (println "  java-summary <message>  Get human-readable Java class summary with Pronto EDN")
          (println "  pronto-edn <message>    Get Pronto EDN representation of a message")
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
      
      ;; Java class info commands
      (#{"java-class" "java-fields" "java-builder" "java-summary" "pronto-edn"} (first arguments))
      (cli-jvm/dispatch-command (first arguments) (rest arguments))
      
      :else
      (do (println "Usage: proto-explorer COMMAND [options]")
          (println "\nCommands:")
          (println "  java-class <message>    Get Java class info for a protobuf message")
          (println "  java-fields <message>   Get proto field to Java method mapping")
          (println "  java-builder <message>  Get Java builder info")
          (println "  java-summary <message>  Get human-readable Java class summary with Pronto EDN")
          (println "  pronto-edn <message>    Get Pronto EDN representation of a message")
          (println "\nRun 'proto-explorer --help' for more information")))))