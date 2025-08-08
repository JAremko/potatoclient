(ns proto-explorer.cli-jvm
  "JVM-based CLI commands for proto-explorer (includes Java reflection features)"
  (:require [proto-explorer.java-class-info :as java-info]
            [proto-explorer.pronto-integration :as pronto-int]
            [proto-explorer.descriptor-integration :as desc-int]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]))

(defn- init-proto-explorer!
  "Initialize proto-explorer if not already initialized"
  []
  ;; No longer need to initialize specs
  nil)

(defn- output-edn
  "Output data as EDN to stdout"
  [data]
  (pp/pprint data))

(defn- parse-message-spec
  "Parse message name or spec keyword from command line"
  [spec-str]
  (cond
    (str/starts-with? spec-str ":") (edn/read-string spec-str)
    (str/includes? spec-str "/") (keyword spec-str)
    :else spec-str))

(defn java-class-info
  "Get Java class info for a protobuf message and output as EDN"
  [args]
  (init-proto-explorer!)
  (if-let [spec-name (first args)]
    (let [message-or-spec (parse-message-spec spec-name)
          result (if (keyword? message-or-spec)
                  (java-info/spec-keyword->class-info message-or-spec)
                  (java-info/find-message-class message-or-spec))]
      (if (:error result)
        (output-edn {:error (:message result)
                     :class-name (:class-name result)})
        (output-edn (select-keys result [:class :fields :methods :inner-classes 
                                        :enum-constants :protobuf-descriptor 
                                        :field-accessors]))))
    (output-edn {:error "Usage: clojure -M:run java-class <message-name-or-spec>"})))

(defn java-field-mapping
  "Get proto field to Java method mapping and output as EDN"
  [args]
  (init-proto-explorer!)
  (if-let [spec-name (first args)]
    (let [message-or-spec (parse-message-spec spec-name)
          class-info (if (keyword? message-or-spec)
                      (java-info/spec-keyword->class-info message-or-spec)
                      (java-info/find-message-class message-or-spec))]
      (if (:error class-info)
        (output-edn class-info)
        (if-let [accessors (:field-accessors class-info)]
          (output-edn {:message (get-in class-info [:class :simple-name])
                      :fields (mapv (fn [{:keys [field-name field-number json-name getter has-method]}]
                                     {:proto-name field-name
                                      :number field-number
                                      :json-name json-name
                                      :getter (:name getter)
                                      :has-method (:name has-method)})
                                   accessors)})
          (output-edn {:error :no-field-accessors
                      :message "No field accessor information available"}))))
    (output-edn {:error "Usage: clojure -M:run java-fields <message-name-or-spec>"})))

(defn java-builder-info
  "Get Java builder info for a protobuf message and output as EDN"
  [args]
  (init-proto-explorer!)
  (if-let [spec-name (first args)]
    (let [message-or-spec (parse-message-spec spec-name)
          class-info (if (keyword? message-or-spec)
                      (java-info/spec-keyword->class-info message-or-spec)
                      (java-info/find-message-class message-or-spec))]
      (if (:error class-info)
        (output-edn class-info)
        (let [result (java-info/get-builder-info (get-in class-info [:class :name]))]
          (if (:error result)
            (output-edn {:error (:message result)})
            (output-edn {:message-class (get-in result [:message-class :class :simple-name])
                        :builder-class (get-in result [:builder-class :class :simple-name])
                        :builder-methods (mapv (fn [m]
                                                {:name (:name m)
                                                 :parameters (mapv :simple-name (:parameter-types m))
                                                 :return-type (get-in m [:return-type :simple-name])})
                                              (:builder-methods result))})))))
    (output-edn {:error "Usage: clojure -M:run java-builder <message-name-or-spec>"})))

(defn java-class-summary
  "Get human-readable Java class summary with Pronto EDN and descriptor info"
  [args]
  (init-proto-explorer!)
  (if-let [spec-name (first args)]
    (let [message-or-spec (parse-message-spec spec-name)
          class-info (if (keyword? message-or-spec)
                      (java-info/spec-keyword->class-info message-or-spec)
                      (java-info/find-message-class message-or-spec))]
      (if (:error class-info)
        (println "Error:" (:message class-info))
        (do
          (println (java-info/format-class-info class-info))
          ;; Add Pronto EDN representation
          (println "\n=== PRONTO EDN REPRESENTATION ===\n")
          (let [pronto-info (pronto-int/get-pronto-info 
                             (or (get-in class-info [:class :simple-name])
                                 message-or-spec))]
            (if (:error pronto-info)
              (println "Error getting Pronto EDN:" (:error pronto-info))
              (pp/pprint (:edn-structure pronto-info))))
          ;; Add Pronto schema
          (println "\n=== PRONTO SCHEMA ===\n")
          (let [schema-info (pronto-int/find-and-get-schema
                             (or (get-in class-info [:class :simple-name])
                                 message-or-spec))]
            (if (:error schema-info)
              (println "Error getting Pronto schema:" (:error schema-info))
              (pp/pprint (:schema schema-info))))
          ;; Add descriptor info with constraints
          (println "\n=== DESCRIPTOR INFO (with buf.validate constraints) ===\n")
          (let [desc-info (desc-int/get-message-descriptor-info
                           (or (get-in class-info [:class :simple-name])
                               message-or-spec))]
            (if (:error desc-info)
              (println "Error getting descriptor:" (:error desc-info))
              (pp/pprint (:descriptor-info desc-info)))))))
    (println "Usage: clojure -M:run java-summary <message-name-or-spec>")))

(defn pronto-edn
  "Get Pronto EDN representation of a protobuf message"
  [args]
  (init-proto-explorer!)
  (if-let [message-name (first args)]
    (let [pronto-info (pronto-int/get-pronto-info message-name)]
      (if (:error pronto-info)
        (output-edn {:error (:error pronto-info)
                     :message-name message-name})
        (output-edn (:edn-structure pronto-info))))
    (output-edn {:error "Usage: clojure -M:run pronto-edn <message-name>"})))

(defn pronto-schema
  "Get Pronto schema for a protobuf message"
  [args]
  (init-proto-explorer!)
  (if-let [message-name (first args)]
    (let [schema-info (pronto-int/find-and-get-schema message-name)]
      (if (:error schema-info)
        (output-edn {:error (:error schema-info)
                     :message-name message-name})
        (output-edn (:schema schema-info))))
    (output-edn {:error "Usage: clojure -M:run pronto-schema <message-name>"})))

(defn descriptor-info
  "Get JSON descriptor info with buf.validate constraints as EDN"
  [args]
  (init-proto-explorer!)
  (if-let [message-name (first args)]
    (let [desc-info (desc-int/get-message-descriptor-info message-name)]
      (if (:error desc-info)
        (output-edn {:error (:error desc-info)
                     :message-name message-name})
        (output-edn (:descriptor-info desc-info))))
    (output-edn {:error "Usage: clojure -M:run descriptor-info <message-name>"})))

;; Dispatch function for CLI commands
(defn dispatch-command
  "Dispatch to appropriate command handler"
  [command args]
  (case command
    "java-class" (java-class-info args)
    "java-fields" (java-field-mapping args)
    "java-builder" (java-builder-info args)
    "java-summary" (java-class-summary args)
    "pronto-edn" (pronto-edn args)
    "pronto-schema" (pronto-schema args)
    "descriptor-info" (descriptor-info args)
    (output-edn {:error (str "Unknown command: " command)
                 :available-commands ["java-class" "java-fields" "java-builder" "java-summary" "pronto-edn" "pronto-schema" "descriptor-info"]})))

(comment
  ;; Test CLI commands
  (java-class-info ["Root"])
  (java-class-info [":cmd/Root"])
  (java-field-mapping ["SetAzimuthValue"])
  (java-builder-info ["Root"])
  )