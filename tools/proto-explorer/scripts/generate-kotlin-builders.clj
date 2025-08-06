#!/usr/bin/env bb

(require '[clojure.string :as str]
         '[clojure.pprint :as pp]
         '[clojure.java.io :as io])

;; Load proto-explorer functionality
(load-file "src/proto_explorer/generated_specs.clj")

;; Initialize specs
(proto-explorer.generated-specs/load-all-specs! "../../shared/specs/protobuf")

(defn kebab->camel [s]
  (let [parts (str/split s #"-")]
    (str/join "" (cons (first parts)
                       (map str/capitalize (rest parts))))))

(defn kebab->pascal [s]
  (let [parts (str/split s #"-")]
    (str/join "" (map str/capitalize parts))))

(defn spec-key->proto-path [spec-key]
  "Convert :cmd.OSD/show-default-screen to OSD.ShowDefaultScreen"
  (let [ns-name (namespace spec-key)
        msg-name (name spec-key)
        ;; Extract the proto package part (e.g., "OSD" from "potatoclient.specs.cmd.OSD")
        proto-pkg (when ns-name
                    (last (str/split ns-name #"\.")))]
    (when proto-pkg
      (str proto-pkg "." (kebab->pascal msg-name)))))

(defn generate-builder-method [cmd-name spec]
  "Generate a Kotlin builder method for a command"
  (let [method-name (str "build" (kebab->pascal cmd-name))
        proto-type (spec-key->proto-path spec)]
    (str "    private fun " method-name "(): Result<JonSharedCmd" proto-pkg ".Root> = Result.success(\n"
         "        JonSharedCmd" proto-pkg ".Root.newBuilder()\n"
         "            .set" (kebab->pascal cmd-name) "(JonSharedCmd" proto-pkg "." proto-type ".newBuilder().build())\n"
         "            .build()\n"
         "    )\n")))

(defn find-commands-for-package [pkg-name]
  "Find all command specs for a given package"
  (let [prefix (str "potatoclient.specs.cmd." pkg-name "/")]
    (->> @proto-explorer.generated-specs/spec-registry
         (filter (fn [[k v]]
                   (and (str/starts-with? (str k) prefix)
                        (not= (name k) "root"))))
         (map first))))

(defn generate-kotlin-builder [pkg-name commands]
  "Generate a complete Kotlin builder file for a package"
  (str "package potatoclient.kotlin.transit\n\n"
       "import cmd.JonSharedCmd\n"
       "import cmd." pkg-name ".JonSharedCmd" pkg-name "\n"
       "import com.cognitect.transit.TransitFactory\n\n"
       "/**\n"
       " * Builder for " pkg-name " commands\n"
       " */\n"
       "object " pkg-name "CommandBuilder {\n"
       "    \n"
       "    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {\n"
       "        val " (str/lower-case pkg-name) "Msg = when (action) {\n"
       (str/join "\n" 
                 (map (fn [cmd-key]
                        (let [cmd-name (name cmd-key)
                              action-name (str (str/lower-case pkg-name) "-" cmd-name)]
                          (str "            \"" action-name "\" -> build" (kebab->pascal cmd-name) "()")))
                      commands))
       "\n            \n"
       "            else -> return Result.failure(\n"
       "                IllegalArgumentException(\"Unknown " pkg-name " command: $action\")\n"
       "            )\n"
       "        }\n"
       "        \n"
       "        return " (str/lower-case pkg-name) "Msg.map { " (str/lower-case pkg-name) " ->\n"
       "            JonSharedCmd.Root.newBuilder()\n"
       "                .setProtocolVersion(1)\n"
       "                .set" pkg-name "(" (str/lower-case pkg-name) ")\n"
       "                .build()\n"
       "        }\n"
       "    }\n"
       "    \n"
       (str/join "\n" 
                 (map (fn [cmd-key]
                        (generate-builder-method (name cmd-key) cmd-key))
                      commands))
       "}\n"))

;; Generate builders for OSD and System packages
(let [osd-commands (find-commands-for-package "OSD")
      system-commands (find-commands-for-package "System")]
  
  (println "Found OSD commands:" (count osd-commands))
  (doseq [cmd osd-commands]
    (println "  " (name cmd)))
  
  (println "\nFound System commands:" (count system-commands))
  (doseq [cmd system-commands]
    (println "  " (name cmd)))
  
  ;; Generate OSD builder
  (spit "generated/OSDCommandBuilder.kt" 
        (generate-kotlin-builder "OSD" osd-commands))
  
  ;; Generate System builder
  (spit "generated/SystemCommandBuilder.kt"
        (generate-kotlin-builder "System" system-commands))
  
  (println "\nGenerated builders in generated/ directory"))