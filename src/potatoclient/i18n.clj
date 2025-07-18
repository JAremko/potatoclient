(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [tongue.core :as tongue]
            [potatoclient.state :as state]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging]
            [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]]))

;; Atom to hold translations and translator instance
(defonce translations-atom (atom {}))
(defonce translator-atom (atom nil))

(>defn load-translation-file
       "Load a translation file from resources"
       [locale]
       [:potatoclient.specs/locale-code => (? [:map-of keyword? string?])]
       (try
         (let [resource-path (str "i18n/" (name locale) ".edn")
               resource (io/resource resource-path)]
           (when resource
             (with-open [rdr (-> resource io/reader java.io.PushbackReader.)]
               (edn/read rdr))))
         (catch Exception e
           (logging/log-error (str "Failed to load translation file for locale: " locale " Error: " (.getMessage e)))
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
       (logging/log-info "Reloading translations...")
       (load-translations!))

;; Initialize translations on namespace load
(load-translations!)

(>defn tr
       "Translate a key using current locale"
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
       (state/set-locale! (state/get-locale)))

(>defn get-current-locale
       "Get the current locale"
       []
       [=> :potatoclient.specs/locale]
       (state/get-locale))

(>defn get-available-locales
       "Get a list of available locales"
       []
       [=> [:sequential :potatoclient.specs/locale]]
       [:english :ukrainian])

(>defn set-locale!
       "Set the current locale"
       [locale]
       [:potatoclient.specs/locale => nil?]
       (state/set-locale! locale))