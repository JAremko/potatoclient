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

(defn group-by-package
  "Group messages and enums by their package."
  [files]
  (reduce
   (fn [acc file]
     (let [package (:package file)
           ns-key (package->namespace-suffix package)]
       (-> acc
           (update-in [ns-key :messages] concat (:messages file))
           (update-in [ns-key :enums] concat (:enums file))
           (assoc-in [ns-key :package] package)
           (assoc-in [ns-key :java-package] (:java-package file))
           (assoc-in [ns-key :java-outer-classname] (:java-outer-classname file)))))
   {}
   files))

(defn resolve-type-refs
  "Resolve type references across namespaces.
  Returns a map of {:imports [...] :type-name ...}"
  [type-ref type-lookup current-package ns-prefix]
  (if-let [type-info (get type-lookup (keyword type-ref))]
    (let [target-package (:package type-info)
          current-ns (package->namespace-suffix current-package)
          target-ns (package->namespace-suffix target-package)]
      (if (= current-ns target-ns)
        ;; Same namespace, no import needed
        {:type-name (name (:name type-info))}
        ;; Different namespace, need import
        {:imports [(str ns-prefix "." target-ns)]
         :type-name (str target-ns "/" (name (:name type-info)))}))
    ;; Fallback for built-in types
    {:type-name type-ref}))

(defn collect-imports-for-message
  "Collect all imports needed for a message."
  [message type-lookup current-package ns-prefix]
  (let [imports (atom #{})]
    ;; Check all field types
    (doseq [field (:fields message)]
      (when-let [type-ref (get-in field [:type :message :type-ref])]
        (let [{:keys [imports type-name]} (resolve-type-refs type-ref type-lookup current-package ns-prefix)]
          (when imports
            (swap! imports into imports)))))
    ;; Check oneof field types
    (doseq [oneof (:oneofs message)
            field (:fields oneof)]
      (when-let [type-ref (get-in field [:type :message :type-ref])]
        (let [{:keys [imports type-name]} (resolve-type-refs type-ref type-lookup current-package ns-prefix)]
          (when imports
            (swap! imports into imports)))))
    @imports))

(defn collect-all-imports
  "Collect all imports needed for a namespace."
  [messages enums type-lookup current-package ns-prefix]
  (reduce
   (fn [imports message]
     (into imports (collect-imports-for-message message type-lookup current-package ns-prefix)))
   #{}
   messages))

(defn generate-namespace-file
  "Generate a single namespace file for a package."
  [package-data ns-prefix type-lookup dependencies ns-key guardrails?]
  (let [{:keys [messages enums package]} package-data
        ns-name (str ns-prefix "." (package->namespace-suffix package))
        imports (collect-all-imports messages enums type-lookup package ns-prefix)
        ;; Add Java imports
        java-imports (distinct 
                      (concat
                       (map :java-class messages)
                       (map :java-class enums)))
        ;; Get dependency-based requires from our analysis using ns-key
        require-specs (get dependencies ns-key [])]
    (frontend/generate-namespace ns-name java-imports enums messages type-lookup require-specs guardrails? true package)))

;; =============================================================================
;; Main API
;; =============================================================================

(declare generate-index-file)

(defn generate-from-backend
  "Generate Clojure code from backend EDN output with namespace separation."
  [{:keys [command state type-lookup dependency-graph] :as backend-output} ns-prefix guardrails?]
  (let [;; Group all files by package
        all-files (concat (:files command) (:files state))
        grouped (group-by-package all-files)
        
        ;; Build dependencies map from backend output
        ;; Aggregate dependencies by target namespace, not original package
        dependencies (reduce (fn [acc ns-key]
                              (let [files-in-ns (filter #(= (package->namespace-suffix (:package %)) ns-key) all-files)
                                    all-requires (mapcat #(get % :clj-requires []) files-in-ns)
                                    ;; Remove duplicates and filter out self-references
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
           (let [file-path (naming/proto-package->file-path (:package package-data))
                 content (generate-namespace-file package-data ns-prefix type-lookup dependencies ns-key guardrails?)]
             (assoc acc file-path content)))
         {}
         grouped)]
    
    ;; Also generate index files that re-export everything for compatibility
    ;; We need to use original packages, not the ns-keys which are lowercased
    (let [all-original-packages (distinct (map :package (vals grouped)))
          cmd-packages (filter #(and (str/starts-with? % "cmd.") ;; Only sub-packages
                                    (not= % "cmd"))  ;; Exclude base cmd package
                              all-original-packages)
          state-packages (filter #(str/starts-with? % "ser") 
                                all-original-packages)
          
          ;; Generate index for commands
          cmd-index (generate-index-file 
                     (str ns-prefix ".command")
                     cmd-packages
                     ns-prefix
                     "Commands index - re-exports all command namespaces"
                     grouped)
          
          ;; Generate index for state
          state-index (generate-index-file
                       (str ns-prefix ".state") 
                       state-packages
                       ns-prefix
                       "State index - re-exports all state namespaces"
                       grouped)]
      
      (-> generated-files
          (assoc "command.clj" cmd-index)
          (assoc "state.clj" state-index)))))

(defn generate-index-file
  "Generate an index file that re-exports functions from multiple namespaces."
  [ns-name packages ns-prefix description grouped]
  (let [;; Build requires with proper alias handling to avoid duplicates
        requires-with-aliases
        (loop [remaining packages
               seen-aliases #{}
               result []]
          (if (empty? remaining)
            result
            (let [pkg (first remaining)
                  ns-suffix (package->namespace-suffix pkg)
                  base-alias (naming/proto-package->alias pkg)
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
        
        ;; Create a package->alias map from requires
        pkg-to-alias (into {} (map (fn [[ns-sym _ alias-sym] pkg]
                                     [pkg (name alias-sym)])
                                   requires-with-aliases
                                   packages))
        
        ;; Generate re-exports for all messages in the packages
        re-exports (when (seq requires-with-aliases)
                    (mapcat (fn [pkg]
                             (let [ns-alias (get pkg-to-alias pkg)
                                   ;; Find the package data in grouped using ns-key, not original package
                                   ns-key (package->namespace-suffix pkg)
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
                           packages))
        
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