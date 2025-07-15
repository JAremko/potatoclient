(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [tongue.core :as tongue]
            [potatoclient.state :as state]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [malli.core :as m]
            [potatoclient.specs :as specs])


;; Atom to hold translations and translator instance
(defonce translations-atom (atom {}))
(defonce translator-atom (atom nil))

(defn load-translation-file
  "Load a translation file from resources"
  [locale]
  (try
    (let [resource-path (str "i18n/" (name locale) ".edn")
          resource (io/resource resource-path)]
      (when resource
        (with-open [rdr (-> resource io/reader java.io.PushbackReader.)]
          (edn/read rdr))))
    (catch Exception e
      (println "Failed to load translation file for locale:" locale "Error:" (.getMessage e))
      nil)))

(m/=> load-translation-file [:=> [:cat ::specs/locale-code] [:maybe :map]])

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
    translations))

(m/=> load-translations! [:=> [:cat] ::specs/translations-map])

(defn reload-translations!
  "Reload all translations - useful for development"
  []
  (println "Reloading translations...")
  (load-translations!))

(m/=> reload-translations! [:=> [:cat] ::specs/translations-map])

;; Initialize translations on namespace load
(load-translations!)

(defn tr
  "Translate a key using current locale"
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

(m/=> tr [:function
          [:=> [:cat ::specs/translation-key] :string]
          [:=> [:cat ::specs/translation-key ::specs/translation-args] :string]])

(defn init!
  "Initialize localization system"
  []
  ;; Ensure translations are loaded
  (when (empty? @translations-atom)
    (load-translations!))
  ;; Set initial locale
  (state/set-locale! (state/get-locale)))

(m/=> init! [:=> [:cat] :any])