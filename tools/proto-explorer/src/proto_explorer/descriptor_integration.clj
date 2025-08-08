(ns proto-explorer.descriptor-integration
  "Integration module for JSON descriptors - provides buf.validate constraints and full descriptor info"
  (:require [proto-explorer.json-to-edn :as json-edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

(def descriptor-base-path "output/json-descriptors/")

(defn java-class-to-descriptor-info
  "Map a Java class name to its descriptor file and proto package.
  Examples:
    cmd.JonSharedCmd$Root -> {:file 'jon_shared_cmd.json' :package 'cmd' :message 'Root'}
    cmd.Compass$Root -> {:file 'jon_shared_cmd_compass.json' :package 'cmd.Compass' :message 'Root'}"
  [java-class-name]
  (cond
    ;; JonSharedCmd$Message pattern -> main cmd file
    (re-matches #"cmd\.JonSharedCmd\$(.+)" java-class-name)
    (let [[_ msg] (re-matches #"cmd\.JonSharedCmd\$(.+)" java-class-name)]
      {:file "jon_shared_cmd.json" :package "cmd" :message msg})
    
    ;; JonSharedData$Message pattern -> main data file  
    (re-matches #"ser\.JonSharedData\$(.+)" java-class-name)
    (let [[_ msg] (re-matches #"ser\.JonSharedData\$(.+)" java-class-name)]
      {:file "jon_shared_data.json" :package "ser" :message msg})
    
    ;; cmd.SubPackage$Message pattern
    (re-matches #"cmd\.(\w+)\$(.+)" java-class-name)
    (let [[_ sub-pkg msg] (re-matches #"cmd\.(\w+)\$(.+)" java-class-name)]
      {:file (str "jon_shared_cmd_" (str/lower-case sub-pkg) ".json")
       :package (str "cmd." sub-pkg)
       :message msg})
    
    ;; Default: try to match by simple message name
    :else
    nil))

(defn message-name->descriptor-file
  "Convert a message name to its JSON descriptor file path.
  Examples: 
    Root -> jon_shared_cmd.json
    JonGUIState -> jon_shared_data.json
    SetAzimuthValue -> jon_shared_cmd_rotary.json"
  [message-name]
  (let [;; Map of known message prefixes to their descriptor files
        ;; Note: These are for simple message names, not full Java class names
        known-mappings {"Root" "jon_shared_cmd"  ; Default Root -> cmd.Root
                       "JonGUIState" "jon_shared_data"
                       "SetAzimuthValue" "jon_shared_cmd_rotary"
                       "GotoAzEl" "jon_shared_cmd_rotary"
                       "Halt" "jon_shared_cmd_rotary"
                       "GetGpsNavData" "jon_shared_data_gps"
                       "GetCompassData" "jon_shared_data_compass"}
        ;; Try to find a direct mapping
        base-name (or (get known-mappings message-name)
                     ;; Otherwise try to infer from the message name
                     (-> message-name
                         (str/replace #"([A-Z])" "-$1")
                         str/lower-case
                         (str/replace #"^-" "")
                         (str/split #"-")
                         first
                         (#(if (contains? #{"cmd" "ser" "data"} %)
                             (str "jon_shared_" %)
                             (str "jon_shared_cmd_" %)))))]
    (str descriptor-base-path base-name ".json")))

(defn load-descriptor-for-message
  "Load the JSON descriptor for a given message name"
  [message-name]
  (let [descriptor-file (message-name->descriptor-file message-name)
        ;; Try local file system first (since we're in tools/proto-explorer)
        local-path (io/file descriptor-file)]
    (if (.exists local-path)
      (json-edn/load-json-descriptor (.getPath local-path))
      ;; Try as resource
      (if-let [resource (io/resource descriptor-file)]
        (json-edn/load-json-descriptor (.getPath (io/file resource)))
        {:error (str "Descriptor file not found: " descriptor-file)
         :message-name message-name}))))

(defn find-message-in-descriptor
  "Find a specific message definition within a descriptor"
  [descriptor message-name & [expected-package]]
  (let [files (:file descriptor)]
    (some (fn [file]
            ;; If expected-package provided, only check files with that package
            (when (or (nil? expected-package)
                      (= (:package file) expected-package))
              (some (fn [msg-type]
                      (when (= (:name msg-type) message-name)
                        msg-type))
                    (:message-type file))))
          files)))

(defn extract-field-constraints
  "Extract buf.validate constraints from field options"
  [field]
  (let [options (:options field)]
    (when options
      ;; Look for buf.validate extensions in options
      (let [constraints (select-keys options 
                                    [:buf.validate.field
                                     :buf.validate.priv.field
                                     ;; Common constraint fields
                                     :required :min-length :max-length
                                     :min-value :max-value :pattern
                                     :in :not-in :unique :ignore-empty])]
        (when (not-empty constraints)
          constraints)))))

(defn get-message-descriptor-info
  "Get comprehensive descriptor information for a message"
  [message-name-or-class]
  ;; First check if this looks like a full Java class name
  (let [descriptor-info (when (str/includes? message-name-or-class ".")
                          (java-class-to-descriptor-info message-name-or-class))
        ;; If we have descriptor info from Java class mapping, use it
        [descriptor-file message-name expected-package] 
        (if descriptor-info
          [(str descriptor-base-path (:file descriptor-info))
           (:message descriptor-info)
           (:package descriptor-info)]
          ;; Otherwise use the simple name approach
          [(message-name->descriptor-file message-name-or-class)
           message-name-or-class
           nil])
        ;; Load the descriptor
        descriptor (if (.exists (io/file descriptor-file))
                    (json-edn/load-json-descriptor descriptor-file)
                    {:error (str "Descriptor file not found: " descriptor-file)})]
    (if (:error descriptor)
      descriptor
      (if-let [message-def (find-message-in-descriptor descriptor message-name expected-package)]
        {:success true
         :message-name message-name
         :descriptor-info {:name (:name message-def)
                          :fields (mapv (fn [field]
                                        {:name (:name field)
                                         :number (:number field)
                                         :type (:type field)
                                         :type-name (:type-name field)
                                         :label (:label field)
                                         :json-name (:json-name field)
                                         :constraints (extract-field-constraints field)})
                                      (:field message-def))
                          :nested-types (:nested-type message-def)
                          :enum-types (:enum-type message-def)
                          :oneof-decl (:oneof-decl message-def)}}
        {:error (str "Message " message-name " not found in descriptor"
                    (when expected-package (str " (package: " expected-package ")")))
         :message-name message-name}))))

(defn format-descriptor-edn
  "Format descriptor info as pretty-printed EDN"
  [descriptor-info]
  (with-out-str
    (pprint/pprint descriptor-info)))

(defn get-full-descriptor
  "Get the full JSON descriptor as EDN for a message"
  [message-name]
  (let [descriptor (load-descriptor-for-message message-name)]
    (if (:error descriptor)
      descriptor
      {:success true
       :message-name message-name
       :full-descriptor descriptor})))

(defn list-available-descriptors
  "List all available JSON descriptor files"
  []
  (let [descriptor-dir (io/file descriptor-base-path)]
    (if (.exists descriptor-dir)
      (let [json-files (filter #(str/ends-with? (.getName %) ".json")
                               (.listFiles descriptor-dir))]
        {:success true
         :descriptors (mapv #(.getName %) json-files)})
      {:error "Descriptor directory not found"
       :path descriptor-base-path})))

(comment
  ;; Test loading descriptors
  (get-message-descriptor-info "Root")
  (get-message-descriptor-info "SetAzimuthValue")
  (get-message-descriptor-info "JonGUIState")
  
  ;; Get full descriptor
  (get-full-descriptor "Root")
  
  ;; List available descriptors
  (list-available-descriptors)
  )