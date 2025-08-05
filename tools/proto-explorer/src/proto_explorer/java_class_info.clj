(ns proto-explorer.java-class-info
  "Extract Java class information from generated protobuf classes using reflection.
  
  Provides EDN representation of:
  - Class name and package
  - Fields with types and modifiers
  - Methods with signatures
  - Inner classes and enums
  - Builder patterns
  - Protobuf field descriptors with field numbers and proto names"
  (:require [clojure.string :as str])
  (:import [java.lang.reflect Field Method Modifier]
           [com.google.protobuf Descriptors$Descriptor Descriptors$FieldDescriptor
            Descriptors$EnumDescriptor Descriptors$EnumValueDescriptor
            GeneratedMessage]))

;; =============================================================================
;; Type conversion helpers
;; =============================================================================

(defn class->edn
  "Convert Java Class to EDN-friendly format"
  [^Class cls]
  (when cls
    (let [base-info {:type (cond
                           (.isArray cls) :array
                           (.isPrimitive cls) :primitive
                           (.isInterface cls) :interface
                           (.isEnum cls) :enum
                           :else :class)
                    :name (.getName cls)
                    :simple-name (.getSimpleName cls)
                    :package (when-let [pkg (.getPackage cls)]
                              (.getName pkg))}]
      (if (.isArray cls)
        (assoc base-info :component-type (class->edn (.getComponentType cls)))
        base-info))))

(defn field->edn
  "Convert Java Field to EDN"
  [^Field field]
  {:name (.getName field)
   :type (class->edn (.getType field))
   :modifiers (Modifier/toString (.getModifiers field))
   :static? (Modifier/isStatic (.getModifiers field))
   :final? (Modifier/isFinal (.getModifiers field))
   :public? (Modifier/isPublic (.getModifiers field))
   :private? (Modifier/isPrivate (.getModifiers field))})

(defn method->edn
  "Convert Java Method to EDN"
  [^Method method]
  {:name (.getName method)
   :return-type (class->edn (.getReturnType method))
   :parameter-types (mapv class->edn (.getParameterTypes method))
   :modifiers (Modifier/toString (.getModifiers method))
   :static? (Modifier/isStatic (.getModifiers method))
   :public? (Modifier/isPublic (.getModifiers method))
   :private? (Modifier/isPrivate (.getModifiers method))})

(defn field-descriptor->edn
  "Convert Protobuf FieldDescriptor to EDN"
  [^Descriptors$FieldDescriptor fd]
  {:name (.getName fd)
   :number (.getNumber fd)
   :json-name (.getJsonName fd)
   :java-type (str (.getJavaType fd))
   :type (str (.getType fd))
   :is-repeated (.isRepeated fd)
   :is-required (.isRequired fd)
   :is-optional (.isOptional fd)
   :has-default-value (.hasDefaultValue fd)
   :default-value (when (.hasDefaultValue fd) 
                   (.getDefaultValue fd))})

(defn enum-value-descriptor->edn
  "Convert Protobuf EnumValueDescriptor to EDN"
  [^Descriptors$EnumValueDescriptor val-desc]
  {:name (.getName val-desc)
   :number (.getNumber val-desc)})

(defn enum-descriptor->edn
  "Convert Protobuf EnumDescriptor to EDN"
  [^Descriptors$EnumDescriptor enum-desc]
  {:name (.getName enum-desc)
   :full-name (.getFullName enum-desc)
   :values (mapv enum-value-descriptor->edn (.getValues enum-desc))})

(defn descriptor->edn
  "Convert Protobuf Descriptor to EDN"
  [^Descriptors$Descriptor desc]
  {:proto-name (.getName desc)
   :full-name (.getFullName desc)
   :fields (->> (.getFields desc)
               (mapv field-descriptor->edn)
               (sort-by :number))
   :nested-types (->> (.getNestedTypes desc)
                     (mapv (fn [^Descriptors$Descriptor nested]
                            {:name (.getName nested)
                             :full-name (.getFullName nested)})))
   :enum-types (->> (.getEnumTypes desc)
                   (mapv enum-descriptor->edn))})

;; =============================================================================
;; Protobuf-specific extraction
;; =============================================================================

(defn get-protobuf-descriptor
  "Get the protobuf descriptor for a message class if available."
  [^Class cls]
  (try
    ;; Try to call getDescriptor static method
    (when-let [method (.getMethod cls "getDescriptor" (into-array Class []))]
      (when (Modifier/isStatic (.getModifiers method))
        (let [descriptor (.invoke method nil (object-array 0))]
          (when (instance? Descriptors$Descriptor descriptor)
            (descriptor->edn descriptor)))))
    (catch Exception e
      nil)))

(defn get-enum-descriptor
  "Get the protobuf descriptor for an enum class if available."
  [^Class cls]
  (try
    (when (.isEnum cls)
      (when-let [method (.getMethod cls "getDescriptor" (into-array Class []))]
        (when (Modifier/isStatic (.getModifiers method))
          (let [descriptor (.invoke method nil (object-array 0))]
            (when (instance? Descriptors$EnumDescriptor descriptor)
              (descriptor->edn descriptor))))))
    (catch Exception e
      nil)))

(defn get-field-accessors
  "Get accessor methods for protobuf fields."
  [^Class cls proto-fields]
  (when proto-fields
    (let [methods (.getMethods cls)]
      (for [{:keys [name number json-name]} proto-fields
            :let [camel-name (str/replace name #"_(.)" (fn [[_ c]] (str/upper-case c)))
                  capitalized-name (str (str/upper-case (first camel-name)) (subs camel-name 1))
                  getter-name (str "get" capitalized-name)
                  has-name (str "has" capitalized-name)
                  getter (some #(when (= (.getName %) getter-name) %) methods)
                  has-method (some #(when (= (.getName %) has-name) %) methods)]
            :when getter]
        {:field-name name
         :field-number number
         :json-name json-name
         :getter {:name (.getName getter)
                  :return-type (class->edn (.getReturnType getter))}
         :has-method (when has-method
                      {:name (.getName has-method)})}))))

;; =============================================================================
;; Class filtering
;; =============================================================================

(defn protobuf-method?
  "Check if a method is a protobuf-specific method."
  [^Method method]
  (let [name (.getName method)]
    (and (not (#{"equals" "hashCode" "toString" "getClass" "notify" "notifyAll" "wait"} name))
         (not (str/starts-with? name "access$"))
         (not (str/starts-with? name "lambda$")))))

(defn protobuf-field?
  "Check if a field is a protobuf-specific field."
  [^Field field]
  (let [name (.getName field)]
    (not (str/starts-with? name "$"))))

;; =============================================================================
;; Main API
;; =============================================================================

(defn analyze-protobuf-class
  "Analyze a protobuf class and return comprehensive EDN representation."
  [class-name]
  (try
    (let [cls (Class/forName class-name)
          fields (->> (.getDeclaredFields cls)
                     (filter protobuf-field?)
                     (map field->edn)
                     (sort-by :name))
          methods (->> (.getDeclaredMethods cls)
                      (filter protobuf-method?)
                      (map method->edn)
                      (sort-by :name))
          inner-classes (->> (.getDeclaredClasses cls)
                           (map (fn [^Class inner]
                                 {:name (.getSimpleName inner)
                                  :full-name (.getName inner)
                                  :type (class->edn inner)}))
                           (sort-by :name))
          enum-constants (when (.isEnum cls)
                          (->> (.getEnumConstants cls)
                              (map (fn [constant]
                                    {:name (.toString constant)
                                     :ordinal (.ordinal constant)}))))
          base-info {:class (class->edn cls)
                     :fields fields
                     :methods methods
                     :inner-classes inner-classes
                     :enum-constants enum-constants}
          proto-desc (if (.isEnum cls)
                      (get-enum-descriptor cls)
                      (get-protobuf-descriptor cls))]
      ;; Add protobuf descriptor and field accessors if available
      (if proto-desc
        (let [field-accessors (when-not (.isEnum cls)
                               (get-field-accessors cls (:fields proto-desc)))]
          (cond-> (assoc base-info :protobuf-descriptor proto-desc)
            field-accessors (assoc :field-accessors field-accessors)))
        base-info))
    (catch ClassNotFoundException e
      {:error :class-not-found
       :class-name class-name
       :message (.getMessage e)})))

(defn find-message-class
  "Find a protobuf message class by name, trying common patterns."
  [message-name & {:keys [packages] :or {packages ["cmd" "ser"]}}]
  (let [patterns (for [pkg packages
                      suffix ["" (str "$" message-name)]]
                  (str pkg "." message-name suffix))
        ;; Add more patterns based on common naming conventions
        patterns (concat patterns
                        [(str "cmd.Jon" message-name)
                         (str "cmd.JonShared" message-name "$" message-name)
                         (str "cmd.JonSharedCmd$" message-name)])]
    (loop [patterns patterns]
      (if-let [pattern (first patterns)]
        (let [result (analyze-protobuf-class pattern)]
          (if (:error result)
            (recur (rest patterns))
            result))
        ;; All patterns failed, return error
        {:error :class-not-found
         :message (str "Could not find protobuf class for: " message-name)
         :tried-patterns patterns}))))

(defn get-builder-info
  "Extract builder pattern information for a message class."
  [class-name]
  (try
    (let [builder-class-name (str class-name "$Builder")
          message-info (analyze-protobuf-class class-name)
          builder-info (analyze-protobuf-class builder-class-name)]
      (if (and (not (:error message-info))
               (not (:error builder-info)))
        {:message-class message-info
         :builder-class builder-info
         :builder-methods (->> (:methods builder-info)
                              (filter #(or (str/starts-with? (:name %) "set")
                                         (str/starts-with? (:name %) "add")
                                         (str/starts-with? (:name %) "clear")
                                         (= (:name %) "build")))
                              (sort-by :name))}
        {:error :not-found
         :message (str "Could not find builder for " class-name)}))
    (catch Exception e
      {:error :exception
       :message (.getMessage e)})))

;; =============================================================================
;; Integration with Proto Explorer specs
;; =============================================================================

(defn spec-keyword->class-info
  "Convert a spec keyword like :cmd/Root to Java class info.
   This expects an exact spec keyword, not a fuzzy match."
  [spec-keyword]
  (let [spec-str (str spec-keyword)
        ;; Remove leading colon
        spec-str (if (str/starts-with? spec-str ":")
                  (subs spec-str 1)
                  spec-str)
        ;; Split namespace and name
        parts (str/split spec-str #"/")]
    (case (count parts)
      1 (find-message-class (first parts))
      2 (let [[ns name] parts
              ;; Convert kebab-case to PascalCase
              pascal-name (->> (str/split name #"-")
                              (map str/capitalize)
                              (str/join))]
          (find-message-class pascal-name :packages [ns]))
      {:error :invalid-spec
       :spec spec-keyword
       :message "Invalid spec format"})))

;; =============================================================================
;; Pretty printing
;; =============================================================================

(defn format-class-info
  "Format class info for human-readable output."
  [{:keys [class fields methods field-accessors protobuf-descriptor] :as info}]
  (when-not (:error info)
    (str "Class: " (get-in class [:simple-name]) "\n"
         "Full name: " (get-in class [:name]) "\n"
         "Package: " (get-in class [:package]) "\n"
         "\nJava Fields (" (count fields) "):\n"
         (str/join "\n" (map #(str "  " (:name %) " : " (get-in % [:type :simple-name])) fields))
         "\n\nMethods (" (count methods) "):\n"
         (str/join "\n" (take 10 (map #(str "  " (:name %) "(" 
                                           (str/join ", " (map (fn [t] (get t :simple-name)) 
                                                              (:parameter-types %))) 
                                           ") : " (get-in % [:return-type :simple-name])) 
                                      methods)))
         (when (> (count methods) 10)
           (str "\n  ... and " (- (count methods) 10) " more"))
         (when protobuf-descriptor
           (str "\n\nProtobuf Fields (" (count (:fields protobuf-descriptor)) "):\n"
                (str/join "\n" (map #(str "  " (:number %) ": " (:name %) 
                                         " (" (:type %) ")"
                                         (when (:is-repeated %) " repeated"))
                                   (:fields protobuf-descriptor)))))
         (when field-accessors
           (str "\n\nField Accessors:\n"
                (str/join "\n" (map #(str "  " (:field-name %) " -> " 
                                         (get-in % [:getter :name]) "()")
                                   field-accessors)))))))

;; =============================================================================
;; Example Usage
;; =============================================================================

(comment
  ;; Analyze a specific class
  (analyze-protobuf-class "cmd.JonSharedCmd$Root")
  
  ;; Find a message class
  (find-message-class "Root")
  
  ;; Get builder info
  (get-builder-info "cmd.JonSharedCmd$Root")
  
  ;; Convert spec keyword to class info
  (spec-keyword->class-info :cmd/Root)
  (spec-keyword->class-info :cmd.RotaryPlatform/set-velocity)
  
  ;; Pretty print
  (println (format-class-info (find-message-class "Root")))
  
  ;; Get detailed protobuf info
  (let [info (find-message-class "SetAzimuthValue")]
    (println "Protobuf descriptor:")
    (clojure.pprint/pprint (:protobuf-descriptor info))
    (println "\nField accessors:")
    (clojure.pprint/pprint (:field-accessors info)))
  )