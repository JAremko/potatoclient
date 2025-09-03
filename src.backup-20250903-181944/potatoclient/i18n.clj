(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require
            [malli.core :as m] [clojure.edn :as edn]
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
  [locale]
  (try
    (let [resource-path (str "i18n/" (name locale) ".edn")
          resource (io/resource resource-path)]
      (when resource
        (with-open [rdr (-> resource io/reader PushbackReader.)]
          (edn/read rdr))))
    (catch Exception e
      ;; Don't use logging here as it might not be initialized
      (println (str "ERROR: Failed to load translation file for locale: " locale " Error: " (.getMessage e)))
      nil))) 
 (m/=> load-translation-file [:=> [:cat :potatoclient.ui-specs/locale-code] [:maybe [:map-of :keyword :string]]])

(defn load-translations!
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
    ;; Debug: log how many keys were loaded (only if logging is available)
    (when-not (runtime/release-build?)
      (try
        (doseq [[locale trans] translations]
          (logging/log-debug {:msg (str "Loaded " (count trans) " keys for locale " locale)}))
        (catch Exception _ nil)))
    translations)) 
 (m/=> load-translations! [:=> [:cat] :potatoclient.ui-specs/translations-map])

(defn reload-translations!
  "Reload all translations - useful for development"
  []
  (logging/log-info {:msg "Reloading translations..."})
  (load-translations!)) 
 (m/=> reload-translations! [:=> [:cat] :potatoclient.ui-specs/translations-map])

;; Don't initialize translations on namespace load
;; They will be loaded by init! which is called from main

(defn- check-missing-key!
  "Check if a key exists in translations and log if missing in dev mode."
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
 (m/=> check-missing-key! [:=> [:cat :keyword :keyword] :nil])

(declare validate-translations)

(defn tr
  "Translate a key using the current locale."
  ([key]
   (tr key []))
  ([key args]
   ;; Ensure translations are loaded
   (when (empty? @translations-atom)
     (load-translations!))
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
 (m/=> tr [:function [:=> [:cat :potatoclient.ui-specs/translation-key] :string] [:=> [:cat :potatoclient.ui-specs/translation-key :potatoclient.ui-specs/translation-args] :string]])

(defn get-all-keys
  "Get all available translation keys across all locales (dev helper)."
  []
  (let [translations @translations-atom]
    (apply set/union (map (comp set keys val) translations)))) 
 (m/=> get-all-keys [:=> [:cat] [:set :keyword]])

(defn validate-translations
  "Validate that all locales have the same keys (dev helper)."
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
 (m/=> validate-translations [:=> [:cat] [:map-of :keyword [:set :keyword]]])

(defn init!
  "Initialize localization system"
  []
  ;; Always load translations to ensure they're complete
  (load-translations!)
  ;; Set initial locale
  (state/set-locale! (state/get-locale))
  ;; In dev mode, validate translations on startup
  (when-not (runtime/release-build?)
    (let [missing (validate-translations)]
      (when (seq missing)
        (logging/log-warn {:msg "Translation keys are inconsistent between locales!"
                          :missing missing}))))
  nil) 
 (m/=> init! [:=> [:cat] :nil])