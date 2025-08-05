#!/usr/bin/env bb

(ns generate-transit-handlers
  "Generate static Kotlin Transit handlers from keyword trees"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]))

(defn load-keyword-tree [file]
  (binding [*read-eval* false]
    (let [content (slurp file)
          ;; Extract the def form
          start (+ (.indexOf content "(def keyword-tree") 17)
          tree-str (subs content start (- (count content) 1))]
      (edn/read-string tree-str))))

(defn kebab->pascal [s]
  (->> (str/split s #"-")
       (map str/capitalize)
       (apply str)))

(defn kebab->camel [s]
  (let [parts (str/split s #"-")]
    (apply str (first parts) (map str/capitalize (rest parts)))))

(defn kotlin-type [proto-type]
  (case proto-type
    "TYPE_DOUBLE" "Double"
    "TYPE_FLOAT" "Float"
    "TYPE_INT64" "Long"
    "TYPE_UINT64" "Long"
    "TYPE_INT32" "Int"
    "TYPE_UINT32" "Int"
    "TYPE_BOOL" "Boolean"
    "TYPE_STRING" "String"
    "TYPE_ENUM" "String" ; Will be converted to enum
    "TYPE_MESSAGE" "Map<*, *>"
    "Any"))

(defn field->kotlin-setter [field-info key parent-class]
  (let [{:keys [proto-field setter type repeated java-class type-ref]} field-info
        kt-type (kotlin-type type)
        field-name (name key)]
    (cond
      ;; Repeated fields
      repeated
      (format "                \"%s\" -> {
                    val list = value as? List<*> ?: listOf(value)
                    list.forEach { item ->
                        if (item != null) {
                            builder.%s(%s)
                        }
                    }
                }"
              field-name
              (subs setter 3) ; Remove "set" prefix and make it addXXX
              (if (= type "TYPE_MESSAGE")
                (format "build%s(item as Map<*, *>)" (kebab->pascal field-name))
                (format "convert%s(item)" (kotlin-type type))))
      
      ;; Message fields
      (= type "TYPE_MESSAGE")
      (format "                \"%s\" -> builder.%s(build%s(value as Map<*, *>))"
              field-name setter (kebab->pascal field-name))
      
      ;; Enum fields
      (= type "TYPE_ENUM")
      (format "                \"%s\" -> {
                    val enumValue = when (value) {
                        is Keyword -> value.name
                        else -> value.toString()
                    }.toUpperCase().replace(\"-\", \"_\")
                    // TODO: Convert to actual enum type
                    builder.%s(enumValue)
                }"
              field-name setter)
      
      ;; Simple fields
      :else
      (format "                \"%s\" -> builder.%s(convert%s(value))"
              field-name setter kt-type))))

(defn generate-builder-function [key node path]
  (let [{:keys [java-class fields children]} node
        class-name (last (str/split java-class #"\$"))
        full-path (conj path key)]
    (str
     (format "\n    private fun build%s(data: Map<*, *>): %s {\n"
             (kebab->pascal (name key)) java-class)
     (format "        val builder = %s.newBuilder()\n" java-class)
     "        \n"
     "        for ((key, value) in data) {\n"
     "            if (value == null) continue\n"
     "            \n"
     "            when (key.toString()) {\n"
     (str/join "\n" (map (fn [[k v]] (field->kotlin-setter v k java-class)) fields))
     "\n"
     "                else -> logger.warn { \"Unknown field $key for " class-name "\" }\n"
     "            }\n"
     "        }\n"
     "        \n"
     "        return builder.build()\n"
     "    }\n"
     ;; Generate builders for child messages
     (str/join "\n" (map (fn [[k v]] (generate-builder-function k v full-path)) children)))))

(defn generate-extractor-function [key node path]
  (let [{:keys [java-class fields children]} node
        class-name (last (str/split java-class #"\$"))]
    (str
     (format "\n    private fun extract%s(msg: %s): Map<String, Any?> {\n"
             (kebab->pascal (name key)) java-class)
     "        val result = mutableMapOf<String, Any?>()\n"
     "        \n"
     (str/join "\n" 
               (map (fn [[k {:keys [proto-field type repeated]}]]
                      (let [getter (str "get" (kebab->pascal proto-field))
                            field-name (name k)]
                        (cond
                          repeated
                          (format "        if (msg.%sCount > 0) {
            result[\"%s\"] = msg.%sList.map { item ->
                %s
            }
        }"
                                  (kebab->camel proto-field)
                                  field-name
                                  (kebab->camel proto-field)
                                  (if (= type "TYPE_MESSAGE")
                                    (format "extract%s(item)" (kebab->pascal field-name))
                                    (if (= type "TYPE_ENUM")
                                      "item.name.toLowerCase().replace(\"_\", \"-\")"
                                      "item")))
                          
                          (= type "TYPE_MESSAGE")
                          (format "        if (msg.has%s()) {
            result[\"%s\"] = extract%s(msg.%s)
        }"
                                  (kebab->pascal proto-field)
                                  field-name
                                  (kebab->pascal field-name)
                                  getter)
                          
                          (= type "TYPE_ENUM")
                          (format "        result[\"%s\"] = msg.%s.name.toLowerCase().replace(\"_\", \"-\")"
                                  field-name getter)
                          
                          :else
                          (format "        result[\"%s\"] = msg.%s"
                                  field-name getter))))
                    fields))
     "\n        \n"
     "        return result\n"
     "    }\n"
     ;; Generate extractors for child messages
     (str/join "\n" (map (fn [[k v]] (generate-extractor-function k v full-path)) children)))))

(defn generate-command-handlers [tree]
  (str "package potatoclient.kotlin.transit.generated

import com.cognitect.transit.ReadHandler
import com.cognitect.transit.WriteHandler
import com.google.protobuf.Message
import mu.KotlinLogging
import clojure.lang.Keyword
import cmd.*
import cmd.CV.*
import cmd.Compass.*
import cmd.DayCamera.*
import cmd.DayCamGlassHeater.*
import cmd.Gps.*
import cmd.HeatCamera.*
import cmd.Lira.*
import cmd.Lrf.*
import cmd.OSD.*
import cmd.RotaryPlatform.*
import cmd.System.*

private val logger = KotlinLogging.logger {}

/**
 * Generated Transit handlers for command messages
 * Generated from proto keyword tree - DO NOT EDIT
 */
object GeneratedCommandHandlers {
    
    /**
     * Build command root from Transit data
     */
    fun buildCommand(data: Map<*, *>): JonSharedCmd.Root {
        val builder = JonSharedCmd.Root.newBuilder()
        
        // Set protocol version if present
        val version = data[\"protocol-version\"] ?: data[\"protocolVersion\"]
        if (version != null) {
            builder.protocolVersion = convertInt(version)
        }
        
        // Find and build the command
        for ((key, value) in data) {
            if (value == null || key == \"protocol-version\" || key == \"protocolVersion\") continue
            
            when (key.toString()) {\n"
       (str/join "\n" 
                 (map (fn [[k node]]
                        (format "                \"%s\" -> builder.cmd = JonSharedCmd.Cmd.newBuilder()
                        .set%s(build%s(value as Map<*, *>))
                        .build()"
                                (name k)
                                (kebab->pascal (name k))
                                (kebab->pascal (name k))))
                      tree))
       "\n                else -> logger.warn { \"Unknown command type: $key\" }
            }
        }
        
        return builder.build()
    }
    
    /**
     * Extract Transit data from command message
     */
    fun extractCommand(root: JonSharedCmd.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        result[\"protocol-version\"] = root.protocolVersion
        
        when (root.cmd.cmdCase) {\n"
       (str/join "\n"
                 (map (fn [[k _]]
                        (let [enum-name (str/upper-case (str/replace (name k) #"-" "_"))]
                          (format "            JonSharedCmd.Cmd.CmdCase.%s -> 
                result[\"%s\"] = extract%s(root.cmd.%s)"
                                  enum-name
                                  (name k)
                                  (kebab->pascal (name k))
                                  (kebab->camel (name k)))))
                      tree))
       "\n            else -> logger.warn { \"Unknown command case: ${root.cmd.cmdCase}\" }
        }
        
        return result
    }
"
       ;; Generate all builder functions
       (str/join "\n" (map (fn [[k v]] (generate-builder-function k v [])) tree))
       "\n"
       ;; Generate all extractor functions  
       (str/join "\n" (map (fn [[k v]] (generate-extractor-function k v [])) tree))
       "\n"
       "    // Utility conversion functions
    private fun convertInt(value: Any): Int = when (value) {
        is Int -> value
        is Number -> value.toInt()
        is String -> value.toInt()
        else -> throw IllegalArgumentException(\"Cannot convert to int: $value\")
    }
    
    private fun convertLong(value: Any): Long = when (value) {
        is Long -> value
        is Number -> value.toLong()
        is String -> value.toLong()
        else -> throw IllegalArgumentException(\"Cannot convert to long: $value\")
    }
    
    private fun convertFloat(value: Any): Float = when (value) {
        is Float -> value
        is Number -> value.toFloat()
        is String -> value.toFloat()
        else -> throw IllegalArgumentException(\"Cannot convert to float: $value\")
    }
    
    private fun convertDouble(value: Any): Double = when (value) {
        is Double -> value
        is Number -> value.toDouble()
        is String -> value.toDouble()
        else -> throw IllegalArgumentException(\"Cannot convert to double: $value\")
    }
    
    private fun convertBoolean(value: Any): Boolean = when (value) {
        is Boolean -> value
        is String -> value.toBoolean()
        else -> throw IllegalArgumentException(\"Cannot convert to boolean: $value\")
    }
    
    private fun convertString(value: Any): String = value.toString()
}

/**
 * Transit handlers that use the generated functions
 */
class GeneratedCommandReadHandler : ReadHandler<Map<*, *>, Message> {
    override fun fromRep(rep: Map<*, *>): Message {
        return GeneratedCommandHandlers.buildCommand(rep)
    }
}

class GeneratedCommandWriteHandler : WriteHandler<JonSharedCmd.Root> {
    override fun tag(o: JonSharedCmd.Root): String = \"cmd\"
    
    override fun rep(o: JonSharedCmd.Root): Any {
        return GeneratedCommandHandlers.extractCommand(o)
    }
}
"))

(defn generate-state-handlers [tree]
  ;; Similar structure for state messages
  (str "package potatoclient.kotlin.transit.generated

import com.cognitect.transit.ReadHandler
import com.cognitect.transit.WriteHandler
import mu.KotlinLogging
import clojure.lang.Keyword
import ser.*

private val logger = KotlinLogging.logger {}

/**
 * Generated Transit handlers for state messages
 * Generated from proto keyword tree - DO NOT EDIT
 */
object GeneratedStateHandlers {
    
    /**
     * Extract Transit data from state message
     */
    fun extractState(state: JonSharedData.JonGUIState): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        \n"
       (str/join "\n"
                 (map (fn [[k _]]
                        (let [getter (str "get" (kebab->pascal (name k)))]
                          (format "        if (state.has%s()) {
            result[\"%s\"] = extract%s(state.%s)
        }"
                                  (kebab->pascal (name k))
                                  (name k)
                                  (kebab->pascal (name k))
                                  getter)))
                      tree))
       "\n        
        return result
    }
"
       ;; Generate all extractor functions
       (str/join "\n" (map (fn [[k v]] (generate-extractor-function k v [])) tree))
       "\n"
       "    // Note: State messages are read-only from server, no builders needed
}

/**
 * Transit write handler for state messages
 */
class GeneratedStateWriteHandler : WriteHandler<JonSharedData.JonGUIState> {
    override fun tag(o: JonSharedData.JonGUIState): String = \"state\"
    
    override fun rep(o: JonSharedData.JonGUIState): Any {
        return GeneratedStateHandlers.extractState(o)
    }
}
"))

(defn -main [& args]
  (println "Generating Transit handlers from keyword trees...")
  
  ;; Load keyword trees
  (let [cmd-tree (load-keyword-tree "../../shared/specs/protobuf/proto_keyword_tree_cmd.clj")
        state-tree (load-keyword-tree "../../shared/specs/protobuf/proto_keyword_tree_state.clj")
        
        output-dir "../../src/potatoclient/kotlin/transit/generated"]
    
    ;; Create output directory
    (io/make-parents (str output-dir "/dummy"))
    
    ;; Generate command handlers
    (let [cmd-content (generate-command-handlers cmd-tree)
          cmd-file (str output-dir "/GeneratedCommandHandlers.kt")]
      (spit cmd-file cmd-content)
      (println "Generated:" cmd-file))
    
    ;; Generate state handlers
    (let [state-content (generate-state-handlers state-tree)
          state-file (str output-dir "/GeneratedStateHandlers.kt")]
      (spit state-file state-content)
      (println "Generated:" state-file))
    
    (println "Done! Now run ktfmt to format the generated files.")
    (println "cd ../.. && make fmt-kotlin")))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))