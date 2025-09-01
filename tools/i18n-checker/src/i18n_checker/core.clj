(ns i18n-checker.core
  "I18n checker tool using rewrite-clj for accurate parsing"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [rewrite-clj.zip :as z]))

;; ============================================================================
;; Translation file loading
;; ============================================================================

(defn load-translation-file
  "Load a translation file from resources"
  [path]
  (try
    (with-open [rdr (-> path io/reader java.io.PushbackReader.)]
      (edn/read rdr))
    (catch Exception e
      (println "Error loading translation file" path ":" (.getMessage e))
      nil)))

(defn load-all-translations
  "Load all translation files"
  []
  (let [base-path "../../resources/i18n/"
        files {"en" (str base-path "en.edn")
               "uk" (str base-path "uk.edn")}]
    (reduce-kv (fn [acc locale path]
                 (if-let [content (load-translation-file path)]
                   (assoc acc locale content)
                   acc))
               {}
               files)))

;; ============================================================================
;; Code analysis using rewrite-clj
;; ============================================================================

(defn find-tr-calls-in-zloc
  "Find all i18n/tr calls in a zipper location"
  [zloc]
  (let [keys (atom #{})]
    (loop [loc zloc]
      (cond
        (z/end? loc) @keys
        
        ;; Check if this is a tr call  
        (and (z/list? loc)
             (let [first-elem (z/down loc)]
               (when first-elem
                 (let [sexpr (try (z/sexpr first-elem) 
                                 (catch Exception _ nil))]
                   (or (= 'i18n/tr sexpr)
                       (= 'tr sexpr))))))
        (let [;; Get the second element (the key)
              key-loc (-> loc z/down z/right)]
          (when key-loc
            (let [tag (z/tag key-loc)
                  sexpr (try (z/sexpr key-loc)
                            (catch Exception _ nil))]
              (when (and (= :token tag)
                        (keyword? sexpr))
                (swap! keys conj sexpr))))
          (recur (z/next loc)))
        
        :else
        (recur (z/next loc))))))

(defn analyze-file
  "Analyze a single file for i18n keys"
  [file-path]
  (try
    (let [content (slurp file-path)
          zloc (z/of-string content)]
      (find-tr-calls-in-zloc zloc))
    (catch Exception e
      (println "Error analyzing file" file-path ":" (.getMessage e))
      #{})))

(defn find-clj-files
  "Recursively find all .clj and .cljc files"
  [dir]
  (let [dir-file (io/file dir)]
    (->> (file-seq dir-file)
         (filter #(.isFile %))
         (filter #(or (str/ends-with? (.getName %) ".clj")
                     (str/ends-with? (.getName %) ".cljc")))
         (map #(.getPath %)))))

(defn analyze-codebase
  "Analyze all Clojure files for i18n keys"
  []
  (let [src-files (find-clj-files "../../src")
        _ (println "  Found" (count src-files) "source files")
        all-keys (reduce (fn [keys file]
                          (let [file-keys (analyze-file file)]
                            (when (seq file-keys)
                              (println "  " file ":" (count file-keys) "keys"))
                            (into keys file-keys)))
                        #{}
                        src-files)]
    all-keys))

;; ============================================================================
;; Analysis and reporting
;; ============================================================================

(defn check-consistency
  "Check consistency between locales"
  [translations]
  (let [all-keys (apply set/union (map (comp set keys val) translations))
        missing-by-locale (reduce (fn [acc [locale trans]]
                                   (let [locale-keys (set (keys trans))
                                         missing (set/difference all-keys locale-keys)]
                                     (if (seq missing)
                                       (assoc acc locale missing)
                                       acc)))
                                 {}
                                 translations)]
    missing-by-locale))

(defn analyze-usage
  "Analyze key usage in code vs translations"
  [used-keys translations]
  (let [all-defined-keys (apply set/union (map (comp set keys val) translations))
        missing-in-i18n (set/difference used-keys all-defined-keys)
        unused-keys (set/difference all-defined-keys used-keys)]
    {:used-in-code (count used-keys)
     :defined-in-i18n (count all-defined-keys)
     :missing-in-i18n missing-in-i18n
     :unused-keys unused-keys}))

(defn generate-report
  "Generate a comprehensive i18n report"
  []
  (println "\n" (str/join "" (repeat 60 "=")) "\n")
  (println "         I18N TRANSLATION CHECKER REPORT")
  (println "\n" (str/join "" (repeat 60 "=")) "\n")
  
  (println "Analyzing codebase...")
  (let [used-keys (analyze-codebase)
        _ (println "Found" (count used-keys) "i18n keys in code")
        
        _ (println "\nLoading translation files...")
        translations (load-all-translations)
        _ (println "Loaded" (count translations) "translation files:"
                  (str/join ", " (keys translations)))
        
        usage (analyze-usage used-keys translations)
        consistency (check-consistency translations)]
    
    (println "\n## SUMMARY")
    (println (format "  Keys used in code:    %d" (:used-in-code usage)))
    (println (format "  Keys in i18n files:   %d" (:defined-in-i18n usage)))
    
    ;; Missing keys in i18n
    (when (seq (:missing-in-i18n usage))
      (println (format "\n## ❌ MISSING KEYS (%d keys used but not defined)"
                      (count (:missing-in-i18n usage))))
      (doseq [key (sort (:missing-in-i18n usage))]
        (println "  " key)))
    
    ;; Unused keys
    (when (seq (:unused-keys usage))
      (println (format "\n## ⚠️  UNUSED KEYS (%d keys defined but not used)"
                      (count (:unused-keys usage))))
      (if (> (count (:unused-keys usage)) 20)
        (do
          (doseq [key (take 20 (sort (:unused-keys usage)))]
            (println "  " key))
          (println (format "  ... and %d more"
                          (- (count (:unused-keys usage)) 20))))
        (doseq [key (sort (:unused-keys usage))]
          (println "  " key))))
    
    ;; Consistency issues
    (when (seq consistency)
      (println "\n## ❌ CONSISTENCY ISSUES")
      (doseq [[locale missing] consistency]
        (println (format "\n  %s is missing %d keys:" locale (count missing)))
        (if (> (count missing) 10)
          (do
            (doseq [key (take 10 (sort missing))]
              (println "    -" key))
            (println (format "    ... and %d more" (- (count missing) 10))))
          (doseq [key (sort missing)]
            (println "    -" key)))))
    
    ;; Success message
    (when (and (empty? (:missing-in-i18n usage))
               (empty? consistency))
      (println "\n## ✅ ALL TRANSLATIONS ARE COMPLETE!"))
    
    (println "\n" (str/join "" (repeat 60 "=")) "\n")
    
    ;; Return data for programmatic use
    {:used-keys used-keys
     :translations translations
     :usage usage
     :consistency consistency}))

(defn generate-stubs
  "Generate stub entries for missing keys"
  [locale]
  (let [data (generate-report)
        missing (:missing-in-i18n (:usage data))]
    (when (seq missing)
      (println (format "\n;; Add these to resources/i18n/%s.edn:\n" locale))
      (doseq [key (sort missing)]
        (println (format " %s \"TODO: translate\"" key)))
      (println))))

(defn -main
  "Main entry point"
  [& args]
  (cond
    (some #{"--help"} args)
    (do
      (println "I18n Translation Checker")
      (println "\nUsage: clojure -M:run [options]")
      (println "\nOptions:")
      (println "  --help        Show this help")
      (println "  --stubs LANG  Generate stub entries for missing keys (en or uk)")
      (println "\nExample:")
      (println "  clojure -M:run           # Generate report")
      (println "  clojure -M:run --stubs en # Generate English stubs"))
    
    (some #{"--stubs"} args)
    (let [idx (.indexOf (vec args) "--stubs")
          locale (get args (inc idx))]
      (if locale
        (generate-stubs locale)
        (println "Error: --stubs requires a locale (en or uk)")))
    
    :else
    (generate-report))
  
  (System/exit 0))