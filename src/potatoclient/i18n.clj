(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [tongue.core :as tongue]
            [potatoclient.state :as state]
            [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

;; Specs for i18n namespace
(s/def ::translation-key keyword?)
(s/def ::locale #{:en :uk})
(s/def ::translation-args (s/coll-of any? :kind vector?))
(s/def ::translations-map (s/map-of keyword? (s/map-of keyword? string?)))

;; Atom to hold translations and translator instance
(defonce translations-atom (atom {}))
(defonce translator-atom (atom nil))

(defn-spec load-translation-file (s/nilable map?)
  "Load a translation file from resources"
  [locale ::locale]
  (try
    (let [resource-path (str "i18n/" (name locale) ".edn")
          resource (io/resource resource-path)]
      (when resource
        (with-open [rdr (-> resource io/reader java.io.PushbackReader.)]
          (edn/read rdr))))
    (catch Exception e
      (println "Failed to load translation file for locale:" locale "Error:" (.getMessage e))
      nil)))

(defn-spec load-translations! ::translations-map
  "Load all translation files from resources"
  []
  (let [locales [:en :uk]
        translations (reduce (fn [acc locale]
                              (if-let [translation (load-translation-file locale)]
                                (assoc acc locale translation)
                                acc))
                            {}
                            locales)]
    (reset! translations-atom translations)
    (reset! translator-atom (tongue/build-translate translations))
    translations))

(defn-spec reload-translations! ::translations-map
  "Reload all translations - useful for development"
  []
  (println "Reloading translations...")
  (load-translations!))

;; Initialize translations on namespace load
(load-translations!)

(defn-spec tr string?
  "Translate a key using current locale"
  ([key ::translation-key]
   (tr key []))
  ([key ::translation-key, args ::translation-args]
   (let [locale (case (state/get-locale)
                  :english :en
                  :ukrainian :uk
                  :en)
         translator @translator-atom]
     (if translator
       (apply translator locale key args)
       (str key)))))

(defn-spec init! any?
  "Initialize localization system"
  []
  ;; Ensure translations are loaded
  (when (empty? @translations-atom)
    (load-translations!))
  ;; Set initial locale
  (state/set-locale! (state/get-locale)))