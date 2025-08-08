(ns proto-explorer.class-resolver
  "Robust class resolution with fallback mechanisms for protobuf Java classes"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clj-fuzzy.metrics :as fuzzy])
  (:import [java.io File]
           [java.net URL URLClassLoader]))

(defn try-load-class
  "Try to load a class by name, returns the Class object or nil"
  [class-name]
  (try
    (Class/forName class-name)
    (catch ClassNotFoundException _ nil)
    (catch Exception _ nil)))

(defn generate-class-variations
  "Generate possible class name variations to try.
   Handles different proto-to-Java naming conventions."
  [original-class-name proto-package proto-file message-name]
  (let [variations (atom [original-class-name])]
    
    ;; If we have proto metadata, generate smart variations
    (when (and proto-package proto-file message-name)
      (let [;; Convert proto file to Java outer class
            file-base (-> proto-file
                         (str/replace ".proto" "")
                         (str/replace "_" ""))
            file-camel (-> proto-file
                           (str/replace ".proto" "")
                           (str/split #"_")
                           (->> (map str/capitalize)
                                (str/join)))]
        
        ;; Try different patterns based on package structure
        (if (str/includes? proto-package ".")
          ;; Nested package like cmd.Gps
          (do
            (swap! variations conj (str proto-package "." file-camel "$" message-name))
            (swap! variations conj (str proto-package "." file-base "$" message-name))
            (swap! variations conj (str proto-package "$" message-name)))
          ;; Simple package like cmd or ser
          (do
            (swap! variations conj (str proto-package "." file-camel "$" message-name))
            (swap! variations conj (str proto-package "." file-base "$" message-name))
            (when (= proto-package "ser")
              (swap! variations conj (str "ser$" message-name)))))))
    
    ;; Also try some generic transformations on the original
    (when original-class-name
      ;; Try replacing single $ with dot patterns
      (when (str/includes? original-class-name "$")
        (let [[pkg cls] (str/split original-class-name #"\$" 2)]
          ;; cmd.Gps$Root -> cmd.Gps.JonSharedCmdGps$Root
          (when (str/includes? pkg ".")
            (let [pkg-parts (str/split pkg #"\.")
                  last-part (last pkg-parts)
                  prefix (str/join "." (butlast pkg-parts))]
              ;; Try adding JonShared prefix variations
              (swap! variations conj (str pkg ".JonShared" (str/capitalize last-part) "$" cls))
              (swap! variations conj (str pkg ".JonSharedCmd" (str/capitalize last-part) "$" cls))
              (swap! variations conj (str pkg ".JonSharedData" (str/capitalize last-part) "$" cls))))
          
          ;; ser$JonGuiDataGps -> ser.JonSharedDataGps$JonGuiDataGps
          (when (= pkg "ser")
            (swap! variations conj (str "ser.JonSharedData" (str/replace cls "JonGuiData" "") "$" cls))
            (swap! variations conj (str "ser.JonSharedData$" cls))))))
    
    ;; Remove duplicates and nils
    (->> @variations
         (filter some?)
         distinct
         vec)))

(defn resolve-class
  "Resolve a Java class with multiple fallback strategies.
   Returns a map with :class if found, or :error with :tried-names."
  ([class-name]
   (resolve-class class-name nil nil nil))
  ([class-name proto-package proto-file message-name]
   (let [variations (generate-class-variations class-name proto-package proto-file message-name)
         results (map (fn [name]
                       {:name name
                        :class (try-load-class name)})
                     variations)
         found (first (filter :class results))]
     (if found
       {:success true
        :class (:class found)
        :resolved-name (:name found)
        :original-name class-name}
       {:success false
        :error :class-not-found
        :original-name class-name
        :tried-names (mapv :name results)}))))

;; Cache for discovered proto classes
(def proto-classes-cache (atom nil))

(defn find-proto-classes-in-dir
  "Find all proto-generated Java classes in a directory"
  [dir-path]
  (let [dir (io/file dir-path)]
    (when (.exists dir)
      (->> (file-seq dir)
           (filter #(.endsWith (.getName %) ".class"))
           (map (fn [f]
                  (-> (.getPath f)
                      (str/replace (.getPath dir) "")
                      (str/replace File/separator ".")
                      (str/replace #"^\.+" "")
                      (str/replace ".class" ""))))
           (filter #(or (str/starts-with? % "cmd.")
                       (str/starts-with? % "ser.")))
           vec))))

(defn discover-all-proto-classes
  "Discover all available proto-generated classes.
   Caches the result for performance."
  []
  (or @proto-classes-cache
      (let [classes (concat
                     (find-proto-classes-in-dir "target/classes")
                     (find-proto-classes-in-dir "build/classes"))]
        (reset! proto-classes-cache classes)
        classes)))

(defn fuzzy-match-class
  "Find the best matching class using fuzzy string matching.
   Returns {:class-name :score :match-type}"
  [target-name message-name]
  (let [all-classes (discover-all-proto-classes)
        target-lower (str/lower-case target-name)
        message-lower (when message-name (str/lower-case message-name))
        
        scored (map (fn [class-name]
                     (let [class-lower (str/lower-case class-name)
                           ;; Check if class contains the message name
                           contains-message? (and message-lower
                                                  (str/includes? class-lower message-lower))
                           ;; Check substring match
                           contains-target? (str/includes? class-lower target-lower)
                           ;; Fuzzy string similarity
                           fuzzy-score (fuzzy/jaro-winkler target-lower class-lower)
                           ;; Message name similarity if available
                           message-score (if message-lower
                                          (fuzzy/jaro-winkler message-lower class-lower)
                                          0)
                           ;; Combined score
                           score (cond
                                  contains-message? (+ 0.7 (* 0.3 fuzzy-score))
                                  contains-target? (+ 0.5 (* 0.5 fuzzy-score))
                                  :else (max fuzzy-score (* 0.8 message-score)))]
                       {:class-name class-name
                        :score score
                        :match-type (cond
                                     contains-message? :message-name-match
                                     contains-target? :substring-match
                                     (> score 0.7) :fuzzy-match
                                     :else :weak-match)}))
                   all-classes)
        
        best-match (->> scored
                       (filter #(> (:score %) 0.5))
                       (sort-by :score >)
                       first)]
    best-match))

(defn resolve-class-with-fuzzy
  "Enhanced resolve with fuzzy matching fallback.
   Returns detailed information about resolution method."
  ([class-name]
   (resolve-class-with-fuzzy class-name nil nil nil))
  ([class-name proto-package proto-file message-name]
   (let [;; First try exact variations
         variations (generate-class-variations class-name proto-package proto-file message-name)
         exact-results (map (fn [name]
                             {:name name
                              :class (try-load-class name)})
                           variations)
         exact-found (first (filter :class exact-results))]
     
     (if exact-found
       ;; Exact match found
       {:success true
        :class (:class exact-found)
        :resolved-name (:name exact-found)
        :original-name class-name
        :resolution-method (if (= (:name exact-found) class-name)
                            :exact
                            :variation)
        :warning nil}
       
       ;; Try fuzzy matching
       (if-let [fuzzy-match (fuzzy-match-class class-name message-name)]
         (if-let [loaded-class (try-load-class (:class-name fuzzy-match))]
           {:success true
            :class loaded-class
            :resolved-name (:class-name fuzzy-match)
            :original-name class-name
            :resolution-method :fuzzy
            :match-score (:score fuzzy-match)
            :match-type (:match-type fuzzy-match)
            :warning (str "Class resolved using fuzzy matching. "
                         "Original: " class-name ", "
                         "Found: " (:class-name fuzzy-match) " "
                         "(score: " (format "%.2f" (:score fuzzy-match)) ", "
                         "type: " (name (:match-type fuzzy-match)) ")")}
           
           ;; Fuzzy match found but couldn't load
           {:success false
            :error :class-load-failed
            :original-name class-name
            :fuzzy-candidate (:class-name fuzzy-match)
            :tried-names (mapv :name exact-results)
            :warning "Found potential match but couldn't load class"})
         
         ;; No match at all
         {:success false
          :error :class-not-found
          :original-name class-name
          :tried-names (mapv :name exact-results)
          :warning "No matching class found even with fuzzy search"})))))

(defn resolve-class
  "Original resolve function, now uses enhanced version internally"
  ([class-name]
   (resolve-class-with-fuzzy class-name))
  ([class-name proto-package proto-file message-name]
   (resolve-class-with-fuzzy class-name proto-package proto-file message-name)))

(defn get-class-or-nil
  "Simple wrapper that returns the Class object or nil"
  ([class-name]
   (:class (resolve-class class-name)))
  ([class-name proto-package proto-file message-name]
   (:class (resolve-class class-name proto-package proto-file message-name))))