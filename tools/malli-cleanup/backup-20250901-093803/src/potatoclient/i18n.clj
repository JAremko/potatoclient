(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [potatoclient.logging :as logging]
            [potatoclient.state :as state]
            [tongue.core :as tongue])
  (:import (java.io PushbackReader)))

(defonce ^{:doc "Atom to hold translations instance"} translations-atom
  (atom {}))

(defonce ^{:doc "Atom to hold translator instance"} translator-atom
  (atom nil))

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
         translator @translator-atom]
     (if translator
       (apply translator locale key args)
       (str key)))))

(defn init!
  "Initialize localization system"
  {:malli/schema [:=> [:cat] :nil]}
  []
  ;; Ensure translations are loaded
  (when (empty? @translations-atom)
    (load-translations!))
  ;; Set initial locale
  (state/set-locale! (state/get-locale))
  nil)
