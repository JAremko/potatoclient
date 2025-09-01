(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state :as state]
            [tongue.core :as tongue])
  (:import (java.io PushbackReader)))

(defonce ^{:doc "Atom to hold translations instance"} translations-atom
  (atom {}))

(defonce ^{:doc "Atom to hold translator instance"} translator-atom
  (atom nil))

(defonce ^{:doc "Set of missing keys reported (to avoid spam)"} reported-missing-keys
  (atom #{}))

(defn load-translation-file
  "Load a translation file from resources"
  {:malli/schema [:=> [:cat :potatoclient.ui-specs/locale-code]
                  [:maybe [:map-of :keyword :string]]]}
  [locale]
  (try
    (let [resource-path (str "i18n/" (name locale) ".edn")
          resource (io/resource resource-path)]
      (when resource
        (with-open [rdr (-> resource io/reader PushbackReader.)]
          (edn/read rdr))))
    (catch Exception e
      (logging/log-error {:msg (str "Failed to load translation file for locale: " locale " Error: " (.getMessage e))})
      nil)))

(defn load-translations!
  "Load all translation files from resources"
  {:malli/schema [:=> [:cat] :potatoclient.ui-specs/translations-map]}
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

(defn reload-translations!
  "Reload all translations - useful for development"
  {:malli/schema [:=> [:cat] :potatoclient.ui-specs/translations-map]}
  []
  (logging/log-info {:msg "Reloading translations..."})
  (load-translations!))

;; Initialize translations on namespace load
(load-translations!)

(defn- check-missing-key!
  "Check if a key exists in translations and log if missing in dev mode."
  {:malli/schema [:=> [:cat :keyword :keyword] :nil]}
  [locale key]
  (when-not (runtime/release-build?)
    (let [translations @translations-atom
          locale-translations (get translations locale)
          key-exists? (contains? locale-translations key)]
      (when-not key-exists?
        ;; Only report each missing key once to avoid spam
        (when-not (contains? @reported-missing-keys [locale key])
          (swap! reported-missing-keys conj [locale key])
          (logging/log-warn {:msg (str "Missing i18n key '" key "' for locale '" locale "'")
                            :key key
                            :locale locale
                            :available-keys (take 5 (sort (keys locale-translations)))})
          ;; Also check if it exists in other locales to help debugging
          (doseq [[other-locale other-translations] translations
                  :when (not= other-locale locale)]
            (when (contains? other-translations key)
              (logging/log-info {:msg (str "  -> Key '" key "' exists in locale '" other-locale "'")})))))))
  nil)

(declare validate-translations)

(defn tr
  "Translate a key using the current locale."
  {:malli/schema [:function
                  [:=> [:cat :potatoclient.ui-specs/translation-key] :string]
                  [:=> [:cat :potatoclient.ui-specs/translation-key
                        :potatoclient.ui-specs/translation-args] :string]]}
  ([key]
   (tr key []))
  ([key args]
   (let [locale (case (state/get-locale)
                  :english :en
                  :ukrainian :uk
                  :en)
         translator @translator-atom
         result (if translator
                  (apply translator locale key args)
                  (str key))]
     ;; Check for missing keys in dev mode
     ;; Tongue returns "{Missing key :key-name}" as a string when key is not found
     (when (and (not (runtime/release-build?))
                (string? result)
                (or (= result (str key))
                    (re-find #"\{Missing key" result)))
       (check-missing-key! locale key))
     ;; If it's a missing key string from tongue, convert to a more readable format
     (if (and (string? result) (re-find #"\{Missing key" result))
       (str "[MISSING: " key "]")
       result))))

(defn get-all-keys
  "Get all available translation keys across all locales (dev helper)."
  {:malli/schema [:=> [:cat] [:set :keyword]]}
  []
  (let [translations @translations-atom]
    (apply set/union (map (comp set keys val) translations))))

(defn validate-translations
  "Validate that all locales have the same keys (dev helper)."
  {:malli/schema [:=> [:cat] [:map-of :keyword [:set :keyword]]]}
  []
  (let [translations @translations-atom
        all-keys (get-all-keys)
        missing-by-locale (reduce (fn [acc [locale trans]]
                                   (let [locale-keys (set (keys trans))
                                         missing (set/difference all-keys locale-keys)]
                                     (if (seq missing)
                                       (assoc acc locale missing)
                                       acc)))
                                 {}
                                 translations)]
    (when (and (seq missing-by-locale)
               (not (runtime/release-build?)))
      (logging/log-warn {:msg "Translation validation found missing keys"
                        :missing-by-locale missing-by-locale}))
    missing-by-locale))

(defn init!
  "Initialize localization system"
  {:malli/schema [:=> [:cat] :nil]}
  []
  ;; Ensure translations are loaded
  (when (empty? @translations-atom)
    (load-translations!))
  ;; Set initial locale
  (state/set-locale! (state/get-locale))
  ;; In dev mode, validate translations on startup
  (when-not (runtime/release-build?)
    (let [missing (validate-translations)]
      (when (seq missing)
        (logging/log-warn {:msg "Translation keys are inconsistent between locales!"
                          :missing missing}))))
  nil)
