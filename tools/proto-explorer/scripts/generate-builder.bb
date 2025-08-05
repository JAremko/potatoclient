#!/usr/bin/env bb

(require '[clojure.string :as str]
         '[clojure.java.io :as io]
         '[clojure.pprint :as pp]
         '[babashka.process :as process])

;; Load the generated specs
(def spec-dir "/home/jare/git/potatoclient/shared/specs/protobuf")
(def spec-files (when-let [dir (io/file spec-dir)]
                  (when (.exists dir)
                    (.listFiles dir))))

;; Function to kebab->camelCase
(defn kebab->camel [s]
  (let [parts (str/split s #"-")]
    (str (first parts)
         (str/join "" (map str/capitalize (rest parts))))))

;; Function to kebab->PascalCase
(defn kebab->pascal [s]
  (str/join "" (map str/capitalize (str/split s #"-"))))

;; Read a spec file and extract command info
(defn read-spec-file [file]
  (when (.endsWith (.getName file) ".clj")
    (let [content (slurp file)
          ns-match (re-find #"ns\s+([\w.-]+)" content)]
      (when ns-match
        (let [ns-name (second ns-match)
              ;; Extract package name from namespace
              ;; e.g. potatoclient.specs.cmd.OSD -> cmd.OSD
              pkg-parts (str/split ns-name #"\.")
              pkg-name (when (>= (count pkg-parts) 4)
                         (str (nth pkg-parts 2) "." (nth pkg-parts 3)))
              ;; Find all defs in the file
              defs (re-seq #"def\s+(\S+)\s+\"Schema for (\S+)\"" content)]
          (when pkg-name
            {:package pkg-name
             :namespace ns-name
             :commands (map (fn [[_ var-name schema-name]]
                             {:var-name var-name
                              :schema-name schema-name})
                           defs)}))))))

;; Generate builder for a package
(defn generate-builder [pkg-info]
  (let [pkg-name (:package pkg-info)
        commands (filter #(not= "root" (:schema-name %)) (:commands pkg-info))
        cmd-pkg (if (str/starts-with? pkg-name "cmd.")
                  (subs pkg-name 4)
                  pkg-name)]
    (when (and (str/starts-with? pkg-name "cmd.")
               (seq commands))
      (println (str "\n=== " cmd-pkg " ==="))
      (println "Commands found:")
      (doseq [cmd commands]
        (println (str "  " (:schema-name cmd))))
      
      ;; Generate basic builder structure
      (str "package potatoclient.kotlin.transit.builders\n\n"
           "import cmd.JonSharedCmd\n"
           "import cmd." cmd-pkg ".JonSharedCmd" cmd-pkg "\n"
           "import com.cognitect.transit.TransitFactory\n\n"
           "/**\n"
           " * Builder for " cmd-pkg " commands\n"
           " * Generated from protobuf specs\n"
           " */\n"
           "object " cmd-pkg "CommandBuilder {\n"
           "    \n"
           "    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {\n"
           "        val " (str/lower-case cmd-pkg) "Msg = when (action) {\n"
           (str/join "\n"
                     (map (fn [cmd]
                            (let [action-name (str (str/lower-case cmd-pkg) "-" (:schema-name cmd))
                                  method-name (str "build" (kebab->pascal (:schema-name cmd)))]
                              (str "            \"" action-name "\" -> " method-name "()")))
                          commands))
           "\n            \n"
           "            else -> return Result.failure(\n"
           "                IllegalArgumentException(\"Unknown " cmd-pkg " command: $action\")\n"
           "            )\n"
           "        }\n"
           "        \n"
           "        return " (str/lower-case cmd-pkg) "Msg.map { " (str/lower-case cmd-pkg) " ->\n"
           "            JonSharedCmd.Root.newBuilder()\n"
           "                .setProtocolVersion(1)\n"
           "                .set" cmd-pkg "(" (str/lower-case cmd-pkg) ")\n"
           "                .build()\n"
           "        }\n"
           "    }\n"
           "    \n"
           ;; Generate stub methods
           (str/join "\n"
                     (map (fn [cmd]
                            (let [;; Convert kebab to pascal for method name but handle underscores
                                  method-name (str "build" (kebab->pascal (str/replace (:schema-name cmd) #"_" "-")))
                                  ;; For proto message name, we need to preserve original or convert properly
                                  msg-name (kebab->pascal (:schema-name cmd))]
                              (str "    private fun " method-name "(): Result<JonSharedCmd" cmd-pkg ".Root> = Result.success(\n"
                                   "        JonSharedCmd" cmd-pkg ".Root.newBuilder()\n"
                                   "            .set" msg-name "(JonSharedCmd" cmd-pkg "." msg-name ".newBuilder().build())\n"
                                   "            .build()\n"
                                   "    )\n")))
                          commands))
           "}\n"))))

;; Main
(println "Analyzing spec files...")
(println (str "Found " (count spec-files) " files in " spec-dir))
(def all-specs (keep read-spec-file spec-files))

;; Find command packages (those starting with cmd.)
(def cmd-packages (filter #(str/starts-with? (:package %) "cmd.") all-specs))

(println "\nFound command packages:")
(doseq [pkg cmd-packages]
  (println (str "  " (:package pkg) " - " (count (:commands pkg)) " messages")))

;; Generate builders for all key packages
(def key-packages ["cmd.OSD" "cmd.System" "cmd.HeatCamera" 
                   "cmd.RotaryPlatform" "cmd.CV" "cmd.Gps" 
                   "cmd.Compass" "cmd.Lrf" "cmd.DayCamera"
                   "cmd.Lira" "cmd.DayCamGlassHeater"])

(doseq [pkg-name key-packages]
  (when-let [pkg (first (filter #(= pkg-name (:package %)) cmd-packages))]
    (let [builder-code (generate-builder pkg)]
      (when builder-code
        (let [output-file (str "generated/" 
                              (last (str/split pkg-name #"\."))
                              "CommandBuilder.kt")]
          (io/make-parents output-file)
          (spit output-file builder-code)
          (println (str "\nGenerated: " output-file)))))))