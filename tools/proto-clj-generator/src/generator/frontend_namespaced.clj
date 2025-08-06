(ns generator.frontend-namespaced
  "Frontend that generates separate namespaces for each protobuf package.
  Instead of putting everything in command.clj and state.clj, this creates
  files like cmd/compass.clj, cmd/gps.clj, etc."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [generator.frontend :as frontend]
            [generator.naming :as naming]))

;; =============================================================================
;; Package Organization
;; =============================================================================

;; Use centralized naming functions instead of local definitions

(defn package->namespace-suffix
  "Get just the namespace suffix part for a package.
  e.g. 'cmd.DayCamera' -> 'cmd.daycamera'"
  [proto-package]
  (let [full-ns (naming/proto-package->clj-namespace proto-package)
        prefix "potatoclient.proto."]
    (if (str/starts-with? full-ns prefix)
      (subs full-ns (count prefix))
      full-ns)))

(defn file->namespace-suffix
  "Convert a proto filename to a namespace suffix.
  e.g. 'jon_shared_data_types.proto' in package 'ser' -> 'ser.types'"
  [filename package]
  (let [;; For packages like "cmd.RotaryPlatform", convert to "cmd.rotaryplatform"
        ns-package (if (str/includes? package ".")
                    (let [parts (str/split package #"\.")]
                      (str (first parts) "." (csk/->kebab-case (last parts))))
                    package)
        ;; Remove .proto extension and jon_shared_ prefix
        base-name (-> filename
                     (str/replace #"\.proto$" "")
                     (str/replace #"^jon_shared_" ""))
        ;; Extract the meaningful part after package prefix
        ;; e.g. "cmd_rotary" -> "rotary", "data_types" -> "types"
        package-prefix (first (str/split package #"\."))
        meaningful-part (-> base-name
                           (str/replace (re-pattern (str "^" package-prefix "_")) "")
                           (str/replace (re-pattern (str "^data_")) ""))
        ;; Convert to kebab case
        ns-part (csk/->kebab-case meaningful-part)]
    ;; For complex packages like cmd.RotaryPlatform, return the converted package
    (if (str/includes? package ".")
      ns-package
      ;; For simple packages, combine with meaningful part if different
      (if (and (not= ns-part package) (not (str/blank? ns-part)))
        (str package "." ns-part)
        package))))

(defn group-by-file
  "Group messages and enums by their file (for separated namespace mode)."
  [files]
  (reduce
   (fn [acc file]
     (let [package (:package file)
           filename (:name file)
           ns-key (file->namespace-suffix filename package)]
       (-> acc
           (update-in [ns-key :messages] concat (:messages file))
           (update-in [ns-key :enums] concat (:enums file))
           (assoc-in [ns-key :package] package)
           (assoc-in [ns-key :java-package] (:java-package file))
           (assoc-in [ns-key :java-outer-classname] (:java-outer-classname file))
           (assoc-in [ns-key :filename] filename))))
   {}
   files))

(defn resolve-type-refs
  "Resolve type references across namespaces.
  Returns a map of {:imports [...] :type-name ...}"
  [type-ref type-lookup current-ns-key ns-prefix]
  (if-let [type-info (get type-lookup (keyword type-ref))]
    (let [;; Get the namespace based on filename
          target-filename (:filename type-info)
          target-package (:package type-info)
          target-ns (if target-filename
                     (file->namespace-suffix target-filename target-package)
                     (package->namespace-suffix target-package))]
      (if (= current-ns-key target-ns)
        ;; Same namespace, no import needed
        {:type-name (name (:name type-info))}
        ;; Different namespace, need import
        {:imports [(str ns-prefix "." target-ns)]
         :type-name (str target-ns "/" (name (:name type-info)))}))
    ;; Fallback for built-in types
    {:type-name type-ref}))

(defn collect-imports-for-message
  "Collect all imports needed for a message."
  [message type-lookup current-ns-key ns-prefix]
  (let [imports (atom #{})]
    ;; Check all field types
    (doseq [field (:fields message)]
      (when-let [type-ref (get-in field [:type :message :type-ref])]
        (let [{:keys [imports type-name]} (resolve-type-refs type-ref type-lookup current-ns-key ns-prefix)]
          (when imports
            (swap! imports into imports)))))
    ;; Check oneof field types
    (doseq [oneof (:oneofs message)
            field (:fields oneof)]
      (when-let [type-ref (get-in field [:type :message :type-ref])]
        (let [{:keys [imports type-name]} (resolve-type-refs type-ref type-lookup current-ns-key ns-prefix)]
          (when imports
            (swap! imports into imports)))))
    @imports))

(defn collect-all-imports
  "Collect all imports needed for a namespace."
  [messages enums type-lookup current-ns-key ns-prefix]
  (reduce
   (fn [imports message]
     (into imports (collect-imports-for-message message type-lookup current-ns-key ns-prefix)))
   #{}
   messages))

(defn generate-namespace-file
  "Generate a single namespace file for a package."
  [package-data ns-prefix type-lookup dependencies ns-key]
  (let [{:keys [messages enums package]} package-data
        ns-name (str ns-prefix "." ns-key)
        imports (collect-all-imports messages enums type-lookup ns-key ns-prefix)
        ;; Add Java imports
        java-imports (distinct 
                      (concat
                       (map :java-class messages)
                       (map :java-class enums)))
        ;; Get dependency-based requires from our analysis using ns-key
        require-specs (get dependencies ns-key [])]
    (frontend/generate-namespace ns-name java-imports enums messages type-lookup require-specs true package)))

;; =============================================================================
;; Main API
;; =============================================================================

(declare generate-index-file)

(defn generate-from-backend
  "Generate Clojure code from backend EDN output with namespace separation."
  [{:keys [command state type-lookup dependency-graph] :as backend-output} ns-prefix]
  (let [;; Group all files by package
        all-files (concat (:files command) (:files state))
        grouped (group-by-file all-files)
        
        ;; Build a map from package to file-based namespace
        package-to-ns (reduce (fn [acc file]
                               (let [ns-key (file->namespace-suffix (:name file) (:package file))]
                                 (assoc acc (:package file) ns-key)))
                             {}
                             all-files)
        
        ;; Build dependencies map using file-based namespaces
        dependencies (reduce (fn [acc ns-key]
                              (let [;; Find which files contributed to this namespace
                                    files-for-ns (filter #(= (file->namespace-suffix (:name %) (:package %)) ns-key) all-files)
                                    ;; Collect requires from these files
                                    all-requires (mapcat #(get % :clj-requires []) files-for-ns)
                                    ;; Remove duplicates and self-references
                                    unique-requires (->> all-requires
                                                        distinct
                                                        (remove #(= (str (first %)) 
                                                                   (str ns-prefix "." ns-key)))
                                                        vec)]
                                (assoc acc ns-key unique-requires)))
                            {}
                            (keys grouped))
        
        ;; Generate a file for each package
        generated-files
        (reduce-kv
         (fn [acc ns-key package-data]
           (let [file-path (if (str/includes? ns-key ".")
                          ;; For file-based namespaces like "ser.types"
                          (let [parts (str/split ns-key #"\.")
                                ;; Convert dashes to underscores for filesystem compatibility
                                parts-with-underscores (map #(str/replace % #"-" "_") parts)]
                            (str (str/join "/" parts-with-underscores) ".clj"))
                          ;; For simple packages - also need to handle dashes
                          (let [path (naming/proto-package->file-path (:package package-data))]
                            ;; Convert dashes to underscores in the filename part
                            (str/replace path #"-" "_")))
                 content (generate-namespace-file package-data ns-prefix type-lookup dependencies ns-key)]
             (assoc acc file-path content)))
         {}
         grouped)]
    
    ;; Also generate index files that re-export everything for compatibility
    ;; We need to use the actual namespace keys from grouped, not the original packages
    (let [all-ns-keys (keys grouped)
          ;; For command index, include namespaces that start with "cmd." but not "cmd" itself
          cmd-namespaces (filter #(and (str/starts-with? % "cmd.")
                                      (not= % "cmd"))
                                all-ns-keys)
          ;; For state index, include all namespaces that start with "ser"
          state-namespaces (filter #(str/starts-with? % "ser")
                                  all-ns-keys)
          
          ;; Generate index for commands
          cmd-index (generate-index-file 
                     (str ns-prefix ".command")
                     cmd-namespaces
                     ns-prefix
                     "Commands index - re-exports all command namespaces"
                     grouped)
          
          ;; Generate index for state
          state-index (generate-index-file
                       (str ns-prefix ".state") 
                       state-namespaces
                       ns-prefix
                       "State index - re-exports all state namespaces"
                       grouped)]
      
      (-> generated-files
          (assoc "command.clj" cmd-index)
          (assoc "state.clj" state-index)))))

(defn generate-index-file
  "Generate an index file that re-exports functions from multiple namespaces."
  [ns-name namespace-keys ns-prefix description grouped]
  (let [;; Build requires with proper alias handling to avoid duplicates
        requires-with-aliases
        (loop [remaining namespace-keys
               seen-aliases #{}
               result []]
          (if (empty? remaining)
            result
            (let [ns-key (first remaining)
                  ;; For index files, we're already given the namespace key
                  ns-suffix ns-key
                  ;; Extract package from grouped data to get the alias
                  package-data (get grouped ns-key)
                  pkg (:package package-data)
                  base-alias (if pkg
                              (naming/proto-package->clojure-alias pkg)
                              ;; Fallback: use last part of namespace key
                              (last (str/split ns-key #"\.")))
                  ;; Handle duplicate aliases by appending numbers
                  final-alias (loop [n 1
                                     candidate base-alias]
                                (if (contains? seen-aliases candidate)
                                  (recur (inc n) (str base-alias n))
                                  candidate))]
              (recur (rest remaining)
                     (conj seen-aliases final-alias)
                     (conj result [(symbol (str ns-prefix "." ns-suffix)) 
                                  :as (symbol final-alias)])))))
        
        ;; Create a namespace-key->alias map from requires
        ns-key-to-alias (into {} (map (fn [[ns-sym _ alias-sym] ns-key]
                                        [ns-key (name alias-sym)])
                                      requires-with-aliases
                                      namespace-keys))
        
        ;; Generate re-exports for all messages in the namespaces
        re-exports (when (seq requires-with-aliases)
                    (mapcat (fn [ns-key]
                             (let [ns-alias (get ns-key-to-alias ns-key)
                                   ;; Get the package data directly using ns-key
                                   package-data (get grouped ns-key)]
                               (when package-data
                                 (mapcat (fn [msg]
                                          (let [msg-name (naming/proto-name->clojure-fn-name (:proto-name msg))
                                                ;; Use ns-alias as prefix to avoid conflicts
                                                prefixed-build-fn (str ns-alias "-build-" msg-name)
                                                prefixed-parse-fn (str ns-alias "-parse-" msg-name)
                                                build-fn (str "build-" msg-name)
                                                parse-fn (str "parse-" msg-name)]
                                            [(str "(def " prefixed-build-fn " " ns-alias "/" build-fn ")")
                                             (str "(def " prefixed-parse-fn " " ns-alias "/" parse-fn ")")]))
                                        (:messages package-data)))))
                           namespace-keys))
        
        template (if (seq requires-with-aliases)
                   (str "(ns " ns-name "\n"
                        "  \"" description "\"\n"
                        "  (:require\n"
                        (str/join "\n" (map #(str "   " (pr-str %)) requires-with-aliases))
                        "))\n\n"
                        ";; Re-export all public functions from sub-namespaces\n"
                        ";; This supports testing without needing to know the internal namespace structure\n\n"
                        (str/join "\n" re-exports) "\n")
                   (str "(ns " ns-name "\n"
                        "  \"" description "\")\n\n"
                        ";; No sub-namespaces to re-export\n"))]
    template))