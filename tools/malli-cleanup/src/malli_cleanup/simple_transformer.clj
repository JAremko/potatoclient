(ns malli-cleanup.simple-transformer
  "Simple transformer using string manipulation for Malli metadata"
  (:require [clojure.string :as str]))

;; ============================================================================
;; Lambda transformation patterns
;; ============================================================================

(def lambda-patterns
  "Patterns for lambda transformation"
  [;; (fn* [x] (instance? Type x)) -> (partial instance? Type)
   {:pattern #"\(fn\* \[([a-zA-Z0-9#_]+)\] \(instance\? ([a-zA-Z0-9./]+) \1\)\)"
    :replacement "(partial instance? $2)"}
   
   ;; (fn* [x] (pred? x)) -> pred?
   {:pattern #"\(fn\* \[([a-zA-Z0-9#_]+)\] \(([a-zA-Z0-9?-]+\?) \1\)\)"
    :replacement "$2"}])

(defn transform-lambdas
  "Transform lambdas in a string"
  [s]
  (reduce (fn [text {:keys [pattern replacement]}]
            (str/replace text pattern replacement))
          s
          lambda-patterns))

;; ============================================================================
;; Metadata positioning
;; ============================================================================

(defn ensure-metadata-on-new-line
  "Ensure metadata is on its own line after docstring"
  [s]
  ;; Pattern: docstring followed by metadata on same line
  (let [pattern #"(\n\s+\"[^\"]*\")\s*(\{[^}]*:malli/schema[^}]*\})"
        replacement "$1\n  $2"]
    (str/replace s pattern replacement)))

;; ============================================================================
;; File processing
;; ============================================================================

(defn process-defn-block
  "Process a single defn block"
  [defn-block]
  (-> defn-block
      transform-lambdas
      ensure-metadata-on-new-line))

(defn split-into-forms
  "Split file content into top-level forms"
  [content]
  (let [lines (str/split-lines content)
        forms (atom [])
        current-form (atom [])]
    
    (doseq [line lines]
      (cond
        ;; Start of new top-level form
        (and (re-matches #"^\(defn.*" line)
             (seq @current-form))
        (do (swap! forms conj (str/join "\n" @current-form))
            (reset! current-form [line]))
        
        ;; Add to current form
        :else
        (swap! current-form conj line)))
    
    ;; Add last form
    (when (seq @current-form)
      (swap! forms conj (str/join "\n" @current-form)))
    
    @forms))

(defn transform-file-content
  "Transform entire file content"
  [content]
  (-> content
      transform-lambdas
      ensure-metadata-on-new-line))

(defn transform-file
  "Transform a file"
  [file-path output-path]
  (let [content (slurp file-path)
        transformed (transform-file-content content)]
    (spit output-path transformed)
    transformed))