#!/usr/bin/env bb

(ns generate-kotlin-handlers
  "Generate static Kotlin Transit handlers from keyword trees using simple templates"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

;; Simple template system without external dependencies
(defn render [template vars]
  (reduce (fn [s [k v]]
            (str/replace s (str "{{" (name k) "}}") (str v)))
          template
          vars))

(defn render-list [template items]
  (str/join "\n" (map #(render template %) items)))

;; Conversion helpers
(defn kebab->pascal [s]
  (->> (str/split (name s) #"-")
       (map str/capitalize)
       (apply str)))

(defn kebab->camel [s]
  (let [parts (str/split (name s) #"-")]
    (apply str (first parts) (map str/capitalize (rest parts)))))

(defn kebab->screaming [s]
  (-> (name s)
      (str/upper-case)
      (str/replace #"-" "_")))

(defn proto-type->kotlin [proto-type]
  (case proto-type
    "TYPE_DOUBLE" "Double"
    "TYPE_FLOAT" "Float"
    "TYPE_INT64" "Long"
    "TYPE_UINT64" "Long"
    "TYPE_INT32" "Int"
    "TYPE_UINT32" "Int"
    "TYPE_BOOL" "Boolean"
    "TYPE_STRING" "String"
    "TYPE_ENUM" "String"
    "TYPE_MESSAGE" "Message"
    "Any"))

;; Escape dollar signs for Kotlin string templates
(defn escape-kotlin-string [s]
  (str/replace s "$" "\\$"))

;; Convert Java inner class notation to Kotlin notation
;; e.g., "cmd.OSD.JonSharedCmdOsd$Root" -> "JonSharedCmdOsd.Root"
(defn java-class->kotlin-ref [java-class]
  (if (.contains java-class "$")
    (let [parts (str/split java-class #"\$")
          outer-parts (str/split (first parts) #"\.")
          class-name (last outer-parts)
          inner-name (second parts)]
      (str class-name "." inner-name))
    ;; For non-inner classes, just use the simple name
    (last (str/split java-class #"\."))))

;; Get the import statement for a java class
;; e.g., "cmd.OSD.JonSharedCmdOsd$Root" -> "cmd.OSD.JonSharedCmdOsd"
(defn java-class->import [java-class]
  (if (.contains java-class "$")
    (first (str/split java-class #"\$"))
    java-class))

;; Load keyword tree from file
(defn load-keyword-tree [file]
  (binding [*read-eval* false]
    (let [content (slurp file)
          start (+ (.indexOf content "(def keyword-tree") 17)
          tree-str (subs content start (- (count content) 1))]
      (edn/read-string tree-str))))

;; Generate field setter code
(defn generate-field-setter [field-name field-info parent-path]
  (let [{:keys [proto-field setter type repeated java-class]} field-info
        kt-type (proto-type->kotlin type)
        field-str (name field-name)
        ;; Add full path prefix for nested message builders
        parent-prefix (when (seq parent-path)
                        (str/join "" (map kebab->pascal parent-path)))
        builder-name (str "build" parent-prefix (kebab->pascal field-name))
        ;; Fix setter names for camelCase fields - the keyword tree has incorrect setters
        ;; For camelCase fields like DayZoomTableValue, the setter should preserve casing
        actual-setter (if (re-find #"[A-Z]" proto-field)
                        (str "set" (-> proto-field (subs 0 1) str/upper-case) (subs proto-field 1))
                        setter)]
    (cond
      ;; Repeated message fields
      (and repeated (= type "TYPE_MESSAGE"))
      (str "                \"" field-str "\" -> {\n"
           "                    val list = value as? List<*> ?: listOf(value)\n"
           "                    list.forEach { item ->\n"
           "                        if (item != null) {\n"
           "                            builder.add" (kebab->pascal (str/replace setter #"^set" "")) 
           "(" builder-name "(item as Map<*, *>))\n"
           "                        }\n"
           "                    }\n"
           "                }")
      
      ;; Repeated simple fields
      repeated
      (str "                \"" field-str "\" -> {\n"
           "                    val list = value as? List<*> ?: listOf(value)\n"
           "                    list.forEach { item ->\n"
           "                        if (item != null) {\n"
           "                            builder.add" (kebab->pascal (str/replace setter #"^set" ""))
           "(convert" kt-type "(item))\n"
           "                        }\n"
           "                    }\n"
           "                }")
      
      ;; Message fields
      (= type "TYPE_MESSAGE")
      (str "                \"" field-str "\" -> builder." actual-setter 
           "(" builder-name "(value as Map<*, *>))")
      
      ;; Enum fields
      (= type "TYPE_ENUM")
      (let [;; For enums, we need to determine the enum class from context
            ;; The type-ref field has the enum type path
            enum-class (when (:type-ref field-info)
                        ;; Convert type-ref like ".ser.JonGuiDataTypes.JonGuiDataSystemLocalizations"
                        ;; to "ser.JonSharedDataTypes.JonGuiDataSystemLocalizations"
                        (let [ref (:type-ref field-info)
                              parts (str/split ref #"\.")
                              ;; Skip empty first element from leading dot
                              path-parts (rest parts)]
                          (if (seq path-parts)
                            (str/join "." path-parts)
                            ;; Fallback - try to guess from field name and parent context
                            nil)))]
        (if enum-class
          (str "                \"" field-str "\" -> {\n"
               "                    val enumValue = when (value) {\n"
               "                        is Keyword -> value.name\n"
               "                        else -> value.toString()\n"
               "                    }.uppercase().replace(\"-\", \"_\")\n"
               "                    try {\n"
               "                        builder." actual-setter "(" enum-class ".valueOf(enumValue))\n"
               "                    } catch (e: IllegalArgumentException) {\n"
               "                        LoggingUtils.log(\"ERROR\", \"Invalid enum value " 
               (escape-kotlin-string "$enumValue") " for " enum-class "\")\n"
               "                    }\n"
               "                }")
          ;; Fallback for when we can't determine enum class
          (str "                \"" field-str "\" -> {\n"
               "                    // WARNING: Could not determine enum class from type-ref\n"
               "                    val enumValue = when (value) {\n"
               "                        is Keyword -> value.name\n"
               "                        else -> value.toString()\n"
               "                    }.uppercase().replace(\"-\", \"_\")\n"
               "                    // TODO: Manually specify enum type\n"
               "                    // builder." actual-setter "(SomeEnumType.valueOf(enumValue))\n"
               "                }")))
      
      ;; Simple fields
      :else
      (str "                \"" field-str "\" -> builder." actual-setter 
           "(convert" kt-type "(value))"))))

;; Generate field getter code
(defn generate-field-getter [field-name field-info parent-path]
  (let [{:keys [proto-field type repeated setter]} field-info
        ;; The setter names in the keyword tree are incorrect for camelCase fields
        ;; We need to generate the correct getter based on the proto field
        getter-name (cond
                      ;; For camelCase fields, preserve exact casing
                      (re-find #"[A-Z]" proto-field)
                      (str "get" (-> proto-field (subs 0 1) str/upper-case) (subs proto-field 1))
                      
                      ;; For snake_case fields with numbers, handle specially
                      ;; e.g., distance_3b -> getDistance3B, distance_2d -> getDistance2D
                      (re-find #"_\d[a-z]$" proto-field)
                      (let [parts (str/split proto-field #"_")
                            main-part (str/join "_" (butlast parts))
                            last-part (last parts)]
                        (str "get" (kebab->pascal (str/replace main-part #"_" "-"))
                             (str/upper-case last-part)))
                      
                      ;; For snake_case fields, convert to PascalCase
                      (re-find #"_" proto-field)
                      (str "get" (kebab->pascal (str/replace proto-field #"_" "-")))
                      
                      ;; For simple lowercase fields
                      :else
                      (str "get" (kebab->pascal proto-field)))
        field-str (name field-name)
        ;; Add full path prefix for nested message extractors
        parent-prefix (when (seq parent-path)
                        (str/join "" (map kebab->pascal parent-path)))
        extractor-name (str "extract" parent-prefix (kebab->pascal field-name))]
    (cond
      ;; Repeated enum fields
      (and repeated (= type "TYPE_ENUM"))
      (let [camel-field (kebab->camel (str/replace proto-field #"_" "-"))]
        (str "        if (msg." camel-field "Count > 0) {\n"
             "            result[\"" field-str "\"] = msg." camel-field 
             "List.map { it.name.lowercase().replace(\"_\", \"-\") }\n"
             "        }"))
      
      ;; Repeated message fields
      (and repeated (= type "TYPE_MESSAGE"))
      (let [camel-field (kebab->camel (str/replace proto-field #"_" "-"))]
        (str "        if (msg." camel-field "Count > 0) {\n"
             "            result[\"" field-str "\"] = msg." camel-field
             "List.map { " extractor-name "(it) }\n"
             "        }"))
      
      ;; Repeated simple fields
      repeated
      (let [camel-field (kebab->camel (str/replace proto-field #"_" "-"))]
        (str "        if (msg." camel-field "Count > 0) {\n"
             "            result[\"" field-str "\"] = msg." camel-field "List\n"
             "        }"))
      
      ;; Message fields
      (= type "TYPE_MESSAGE")
      (str "        if (msg.has" (kebab->pascal (str/replace proto-field #"_" "-")) "()) {\n"
           "            result[\"" field-str "\"] = " extractor-name 
           "(msg." getter-name "())\n"
           "        }")
      
      ;; Enum fields
      (= type "TYPE_ENUM")
      (str "        result[\"" field-str "\"] = msg." getter-name 
           "().name.lowercase().replace(\"_\", \"-\")")
      
      ;; Simple fields
      :else
      (str "        result[\"" field-str "\"] = msg." getter-name "()"))))

;; Generate builder function
(defn generate-builder-function [key node path]
  (let [{:keys [java-class fields children]} node
        class-name (last (str/split java-class #"\$"))
        pascal-name (kebab->pascal key)
        kotlin-class (java-class->kotlin-ref java-class)
        ;; Add parent prefix for nested functions to avoid conflicts
        ;; Use full path for deeply nested structures
        parent-prefix (when (seq path)
                        (str/join "" (map kebab->pascal path)))]
    (str "\n    /**\n"
         "     * Build " class-name " from Transit data\n"
         "     */\n"
         "    private fun build" (or parent-prefix "") pascal-name "(data: Map<*, *>): " kotlin-class " {\n"
         "        val builder = " kotlin-class ".newBuilder()\n"
         "        \n"
         "        for ((key, value) in data) {\n"
         "            if (value == null) continue\n"
         "            \n"
         "            when (keyToString(key)) {\n"
         (str/join "\n" (map (fn [[k v]] (generate-field-setter k v (conj path key))) fields))
         "\n"
         "                else -> LoggingUtils.log(\"WARN\", \"Unknown field " (escape-kotlin-string "$key") " for " class-name "\")\n"
         "            }\n"
         "        }\n"
         "        \n"
         "        return builder.build()\n"
         "    }\n"
         ;; Generate builders for child messages
         (str/join "\n" (map (fn [[k v]] 
                               (generate-builder-function k v (conj path key))) 
                             children)))))

;; Generate extractor function
(defn generate-extractor-function [key node path]
  (let [{:keys [java-class fields children]} node
        class-name (last (str/split java-class #"\$"))
        pascal-name (kebab->pascal key)
        kotlin-class (java-class->kotlin-ref java-class)
        ;; Add parent prefix for nested functions to avoid conflicts
        ;; Use full path for deeply nested structures
        parent-prefix (when (seq path)
                        (str/join "" (map kebab->pascal path)))]
    (str "\n    /**\n"
         "     * Extract Transit data from " class-name "\n"
         "     */\n"
         "    private fun extract" (or parent-prefix "") pascal-name "(msg: " kotlin-class "): Map<String, Any?> {\n"
         "        val result = mutableMapOf<String, Any?>()\n"
         "        \n"
         (str/join "\n" (map (fn [[k v]] (generate-field-getter k v (conj path key))) fields))
         "\n        \n"
         "        return result\n"
         "    }\n"
         ;; Generate extractors for child messages
         (str/join "\n" (map (fn [[k v]]
                               (generate-extractor-function k v (conj path key)))
                             children)))))

;; Collect all java classes used in a tree
(defn collect-java-classes [tree]
  (distinct
   (flatten
    (map (fn collect-from-node [[k node]]
           (cons (:java-class node)
                 (concat
                  ;; Get java classes from fields that are messages
                  (keep (fn [[field-k field-v]]
                          (when (= (:type field-v) "TYPE_MESSAGE")
                            (:java-class field-v)))
                        (:fields node))
                  ;; Recursively collect from children
                  (collect-java-classes (:children node)))))
         tree))))

;; Generate the main command handlers file
(defn generate-command-handlers [tree]
  (let [timestamp (str (java.util.Date.))
        commands (map (fn [[k _]]
                        {:name (name k)
                         :pascal (kebab->pascal k)
                         :camel (kebab->camel k)
                         :screaming (kebab->screaming k)})
                      tree)
        ;; Collect all imports needed
        all-java-classes (collect-java-classes tree)
        imports (distinct (map java-class->import all-java-classes))
        ;; Sort imports and filter out the base cmd package
        sorted-imports (sort (remove #(= % "cmd") imports))
        builders (str/join "\n" (map (fn [[k v]] 
                                       (generate-builder-function k v [])) 
                                     tree))
        extractors (str/join "\n" (map (fn [[k v]] 
                                         (generate-extractor-function k v [])) 
                                       tree))]
    (str "package potatoclient.kotlin.transit.generated\n\n"
         "import clojure.lang.Keyword\n"
         "import com.cognitect.transit.ReadHandler\n"
         "import com.cognitect.transit.WriteHandler\n"
         "import com.google.protobuf.Message\n"
         "import potatoclient.kotlin.transit.LoggingUtils\n"
         (str/join "\n" (map #(str "import " %) sorted-imports))
         "\n\n"
         "/**\n"
         " * Generated Transit handlers for command messages.\n"
         " * \n"
         " * This file is auto-generated from protobuf definitions.\n"
         " * DO NOT EDIT - regenerate with: bb generate-kotlin-handlers.clj\n"
         " * \n"
         " * Generated on: " timestamp "\n"
         " */\n"
         "object GeneratedCommandHandlers {\n"
         "    \n"
         "    /**\n"
         "     * Build command root from Transit data\n"
         "     */\n"
         "    fun buildCommand(data: Map<*, *>): JonSharedCmd.Root {\n"
         "        val builder = JonSharedCmd.Root.newBuilder()\n"
         "        \n"
         "        // Set protocol version if present\n"
         "        val version = data[\"protocol-version\"] ?: data[\"protocolVersion\"]\n"
         "        if (version != null) {\n"
         "            builder.protocolVersion = convertInt(version)\n"
         "        }\n"
         "        \n"
         "        // Find and build the command\n"
         "        for ((key, value) in data) {\n"
         "            if (value == null || key == \"protocol-version\" || key == \"protocolVersion\") continue\n"
         "            \n"
         "            when (keyToString(key)) {\n"
         (str/join "\n" (map (fn [{:keys [name pascal camel]}]
                               (str "                \"" name "\" -> builder.set" pascal "(build" pascal "(value as Map<*, *>))"))
                             commands))
         "\n                else -> LoggingUtils.log(\"WARN\", \"Unknown command type: " (escape-kotlin-string "$key") "\")\n"
         "            }\n"
         "        }\n"
         "        \n"
         "        return builder.build()\n"
         "    }\n"
         "    \n"
         "    /**\n"
         "     * Extract Transit data from command message\n"
         "     */\n"
         "    fun extractCommand(root: JonSharedCmd.Root): Map<String, Any?> {\n"
         "        val result = mutableMapOf<String, Any?>()\n"
         "        result[\"protocol-version\"] = root.protocolVersion\n"
         "        \n"
         "        when (root.payloadCase) {\n"
         (str/join "\n" (map (fn [{:keys [name pascal camel screaming]}]
                               (str "            JonSharedCmd.Root.PayloadCase." screaming " -> \n"
                                    "                result[\"" name "\"] = extract" pascal "(root." camel ")"))
                             commands))
         "\n            JonSharedCmd.Root.PayloadCase.PAYLOAD_NOT_SET -> \n"
         "                LoggingUtils.log(\"WARN\", \"Command not set\")\n"
         "            else -> \n"
         "                LoggingUtils.log(\"WARN\", \"Unknown payload case: " (escape-kotlin-string "${root.payloadCase}") "\")\n"
         "        }\n"
         "        \n"
         "        return result\n"
         "    }\n"
         builders
         "\n"
         extractors
         "\n\n"
         "    // Utility conversion functions\n"
         "    private fun keyToString(key: Any?): String = when (key) {\n"
         "        is String -> key\n"
         "        is com.cognitect.transit.Keyword -> key.name\n"
         "        null -> \"null\"\n"
         "        else -> key.toString()\n"
         "    }\n"
         "    \n"
         "    private fun convertInt(value: Any): Int = when (value) {\n"
         "        is Int -> value\n"
         "        is Number -> value.toInt()\n"
         "        is String -> value.toInt()\n"
         "        else -> throw IllegalArgumentException(\"Cannot convert to int: " (escape-kotlin-string "$value") "\")\n"
         "    }\n"
         "    \n"
         "    private fun convertLong(value: Any): Long = when (value) {\n"
         "        is Long -> value\n"
         "        is Number -> value.toLong()\n"
         "        is String -> value.toLong()\n"
         "        else -> throw IllegalArgumentException(\"Cannot convert to long: " (escape-kotlin-string "$value") "\")\n"
         "    }\n"
         "    \n"
         "    private fun convertFloat(value: Any): Float = when (value) {\n"
         "        is Float -> value\n"
         "        is Number -> value.toFloat()\n"
         "        is String -> value.toFloat()\n"
         "        else -> throw IllegalArgumentException(\"Cannot convert to float: " (escape-kotlin-string "$value") "\")\n"
         "    }\n"
         "    \n"
         "    private fun convertDouble(value: Any): Double = when (value) {\n"
         "        is Double -> value\n"
         "        is Number -> value.toDouble()\n"
         "        is String -> value.toDouble()\n"
         "        else -> throw IllegalArgumentException(\"Cannot convert to double: " (escape-kotlin-string "$value") "\")\n"
         "    }\n"
         "    \n"
         "    private fun convertBoolean(value: Any): Boolean = when (value) {\n"
         "        is Boolean -> value\n"
         "        is String -> value.toBoolean()\n"
         "        else -> throw IllegalArgumentException(\"Cannot convert to boolean: " (escape-kotlin-string "$value") "\")\n"
         "    }\n"
         "    \n"
         "    private fun convertString(value: Any): String = value.toString()\n"
         "}\n\n"
         "/**\n"
         " * Transit read handler for command messages\n"
         " */\n"
         "class GeneratedCommandReadHandler : ReadHandler<Message, Map<*, *>> {\n"
         "    override fun fromRep(rep: Map<*, *>): Message {\n"
         "        return GeneratedCommandHandlers.buildCommand(rep)\n"
         "    }\n"
         "}\n\n"
         "/**\n"
         " * Transit write handler for command messages\n"
         " */\n"
         "class GeneratedCommandWriteHandler : WriteHandler<JonSharedCmd.Root, Any> {\n"
         "    override fun tag(o: JonSharedCmd.Root): String = \"cmd\"\n"
         "    \n"
         "    override fun rep(o: JonSharedCmd.Root): Any {\n"
         "        return GeneratedCommandHandlers.extractCommand(o)\n"
         "    }\n"
         "    \n"
         "    override fun stringRep(o: JonSharedCmd.Root): String? = null\n"
         "    \n"
         "    override fun <V : Any> getVerboseHandler(): WriteHandler<JonSharedCmd.Root, V>? = null\n"
         "}\n")))

;; Generate the state handlers file
(defn generate-state-handlers [tree]
  (let [timestamp (str (java.util.Date.))
        state-fields (map (fn [[k _]]
                            {:name (name k)
                             :pascal (kebab->pascal k)
                             :getter (kebab->camel k)
                             :has-check (kebab->pascal k)})
                          tree)
        ;; Collect all imports needed
        all-java-classes (collect-java-classes tree)
        imports (distinct (map java-class->import all-java-classes))
        ;; Sort imports and filter out the base ser package  
        sorted-imports (sort (remove #(= % "ser") imports))
        extractors (str/join "\n" (map (fn [[k v]]
                                         (generate-extractor-function k v []))
                                       tree))]
    (str "package potatoclient.kotlin.transit.generated\n\n"
         "import com.cognitect.transit.WriteHandler\n"
         "import ser.JonSharedData\n"
         (str/join "\n" (map #(str "import " %) sorted-imports))
         "\n\n"
         "/**\n"
         " * Generated Transit handlers for state messages.\n"
         " * \n"
         " * This file is auto-generated from protobuf definitions.\n"
         " * DO NOT EDIT - regenerate with: bb generate-kotlin-handlers.clj\n"
         " * \n"
         " * Generated on: " timestamp "\n"
         " */\n"
         "object GeneratedStateHandlers {\n"
         "    \n"
         "    /**\n"
         "     * Extract Transit data from state message\n"
         "     */\n"
         "    fun extractState(state: JonSharedData.JonGUIState): Map<String, Any?> {\n"
         "        val result = mutableMapOf<String, Any?>()\n"
         "        \n"
         (str/join "\n" (map (fn [{:keys [name pascal getter has-check]}]
                               (str "        if (state.has" has-check "()) {\n"
                                    "            result[\"" name "\"] = extract" pascal 
                                    "(state." getter ")\n"
                                    "        }"))
                             state-fields))
         "\n        \n"
         "        return result\n"
         "    }\n"
         extractors
         "\n}\n\n"
         "/**\n"
         " * Transit write handler for state messages\n"
         " */\n"
         "class GeneratedStateWriteHandler : WriteHandler<JonSharedData.JonGUIState, Any> {\n"
         "    override fun tag(o: JonSharedData.JonGUIState): String = \"state\"\n"
         "    \n"
         "    override fun rep(o: JonSharedData.JonGUIState): Any {\n"
         "        return GeneratedStateHandlers.extractState(o)\n"
         "    }\n"
         "    \n"
         "    override fun stringRep(o: JonSharedData.JonGUIState): String? = null\n"
         "    \n"
         "    override fun <V : Any> getVerboseHandler(): WriteHandler<JonSharedData.JonGUIState, V>? = null\n"
         "}\n")))

;; Main function
(defn -main [& args]
  (println "Generating Kotlin Transit handlers from keyword trees...")
  
  (let [cmd-tree (load-keyword-tree "../../shared/specs/protobuf/proto_keyword_tree_cmd.clj")
        state-tree (load-keyword-tree "../../shared/specs/protobuf/proto_keyword_tree_state.clj")
        output-dir "../../src/potatoclient/kotlin/transit/generated"]
    
    ;; Create output directory
    (io/make-parents (str output-dir "/dummy"))
    
    ;; Generate command handlers
    (let [cmd-content (generate-command-handlers cmd-tree)
          cmd-file (str output-dir "/GeneratedCommandHandlers.kt")]
      (spit cmd-file cmd-content)
      (println "✓ Generated:" cmd-file))
    
    ;; Generate state handlers
    (let [state-content (generate-state-handlers state-tree)
          state-file (str output-dir "/GeneratedStateHandlers.kt")]
      (spit state-file state-content)
      (println "✓ Generated:" state-file))
    
    (println "\nDone! Files generated successfully.")
    (println "\nNext steps:")
    (println "1. Format the files: cd ../.. && make fmt-kotlin")
    (println "2. Compile and test: make compile")))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))