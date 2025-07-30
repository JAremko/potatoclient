(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn ?]]
            [potatoclient.logging :as logging]
            [potatoclient.state :as state]
            [tongue.core :as tongue])
  (:import (java.io PushbackReader)))

;; Atom to hold translations and translator instance
;; Atom holding all loaded translations.
(defonce translations-atom (atom {}))

;; Atom holding the tongue translator instance.
(defonce translator-atom (atom nil))

(>defn load-translation-file
  "Load a translation file from resources"
  [locale]
  [:potatoclient.specs/locale-code => (? [:map-of keyword? string?])]
  (try
    (let [resource-path (str "i18n/" (name locale) ".edn")
          resource (io/resource resource-path)]
      (when resource
        (with-open [rdr (-> resource io/reader PushbackReader.)]
          (edn/read rdr))))
    (catch Exception e
      (logging/log-error {:msg (str "Failed to load translation file for locale: " locale " Error: " (.getMessage e))})
      nil)))

(>defn load-translations!
  "Load all translation files from resources"
  []
  [=> :potatoclient.specs/translations-map]
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

(>defn reload-translations!
  "Reload all translations - useful for development"
  []
  [=> :potatoclient.specs/translations-map]
  (logging/log-info {:msg "Reloading translations..."})
  (load-translations!))

;; Initialize translations on namespace load
(load-translations!)

(>defn tr
  "Translate a key using the current locale."
  ([key]
   [:potatoclient.specs/translation-key => string?]
   (tr key []))
  ([key args]
   [:potatoclient.specs/translation-key :potatoclient.specs/translation-args => string?]
   (let [locale (case (state/get-locale)
                  :english :en
                  :ukrainian :uk
                  :en)
         translator @translator-atom]
     (if translator
       (apply translator locale key args)
       (str key)))))

(>defn init!
  "Initialize localization system"
  []
  [=> nil?]
  ;; Ensure translations are loaded
  (when (empty? @translations-atom)
    (load-translations!))
  ;; Set initial locale
  (state/set-locale! (state/get-locale))
  nil)