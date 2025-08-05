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
            [generator.frontend :as frontend]))

;; =============================================================================
;; Package Organization
;; =============================================================================

(defn package->namespace
  "Convert protobuf package to Clojure namespace.
  e.g. 'cmd.Compass' -> 'cmd.compass'"
  [proto-package]
  (-> proto-package
      (str/lower-case)
      (str/replace "_" "-")))

(defn package->file-path
  "Convert protobuf package to file path.
  e.g. 'cmd.Compass' -> 'cmd/compass.clj'"
  [proto-package]
  (str (str/replace (package->namespace proto-package) "." "/") ".clj"))

(defn group-by-package
  "Group messages and enums by their package."
  [files]
  (reduce
   (fn [acc file]
     (let [package (:package file)
           ns-key (package->namespace package)]
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
          current-ns (package->namespace current-package)
          target-ns (package->namespace target-package)]
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
  [package-data ns-prefix type-lookup dependencies]
  (let [{:keys [messages enums package]} package-data
        ns-name (str ns-prefix "." (package->namespace package))
        imports (collect-all-imports messages enums type-lookup package ns-prefix)
        ;; Add Java imports
        java-imports (distinct 
                      (concat
                       (map :java-class messages)
                       (map :java-class enums)))
        ;; Get dependency-based requires from our analysis
        require-specs (get dependencies package [])]
    (frontend/generate-namespace ns-name java-imports enums messages type-lookup require-specs)))

;; =============================================================================
;; Main API
;; =============================================================================

(declare generate-index-file)

(defn generate-from-backend
  "Generate Clojure code from backend EDN output with namespace separation."
  [{:keys [command state type-lookup dependency-graph] :as backend-output} ns-prefix]
  (let [;; Group all files by package
        all-files (concat (:files command) (:files state))
        grouped (group-by-package all-files)
        
        ;; Build dependencies map from backend output
        ;; Map package -> require specs
        dependencies (reduce (fn [acc file]
                              (assoc acc (:package file) 
                                     (get file :clj-requires [])))
                            {}
                            all-files)
        
        ;; Generate a file for each package
        generated-files
        (reduce-kv
         (fn [acc ns-key package-data]
           (let [file-path (package->file-path (:package package-data))
                 content (generate-namespace-file package-data ns-prefix type-lookup dependencies)]
             (assoc acc file-path content)))
         {}
         grouped)]
    
    ;; Also generate index files that re-export everything for compatibility
    (let [cmd-packages (filter #(str/starts-with? % "cmd.") (keys grouped))
          state-packages (filter #(str/starts-with? % "ser.") (keys grouped))
          
          ;; Generate index for commands
          cmd-index (generate-index-file 
                     (str ns-prefix ".command")
                     cmd-packages
                     ns-prefix
                     "Commands index - re-exports all command namespaces")
          
          ;; Generate index for state
          state-index (generate-index-file
                       (str ns-prefix ".state") 
                       state-packages
                       ns-prefix
                       "State index - re-exports all state namespaces")]
      
      (-> generated-files
          (assoc "command.clj" cmd-index)
          (assoc "state.clj" state-index)))))

(defn generate-index-file
  "Generate an index file that re-exports functions from multiple namespaces."
  [ns-name packages ns-prefix description]
  (let [requires (map (fn [pkg]
                        (let [ns-suffix (package->namespace pkg)]
                          [(symbol (str ns-prefix "." ns-suffix)) :as (symbol ns-suffix)]))
                      packages)
        
        ;; This is a simplified version - in reality we'd need to enumerate
        ;; all public functions to re-export them
        template (str "(ns " ns-name "\n"
                     "  \"" description "\"\n"
                     "  (:require\n"
                     (str/join "\n" (map #(str "   " (pr-str %)) requires))
                     "))\n\n"
                     ";; Re-export all public functions from sub-namespaces\n"
                     ";; This supports testing without needing to know the internal namespace structure\n")]
    template))