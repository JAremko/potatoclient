(ns potatoclient.theme "Theme management for PotatoClient using DarkLaf" (:require [clojure.java.io :as io] [malli.core :as m] [potatoclient.logging :as logging] [potatoclient.runtime :as runtime] [potatoclient.ui-specs :as specs] [seesaw.core :as seesaw]) (:import com.github.weisj.darklaf.LafManager (com.github.weisj.darklaf.theme HighContrastDarkTheme OneDarkTheme SolarizedDarkTheme SolarizedLightTheme) (java.text SimpleDateFormat) (java.util Date)))

(def theme-config (atom {:current-theme :sol-dark}))

(defn get-current-theme "Get the current theme key" #:malli{:schema [:=> [:cat] :potatoclient.ui-specs/theme-key]} [] (:current-theme (clojure.core/deref theme-config)))

(defn- apply-theme! "Apply the given theme using DarkLaf" #:malli{:schema [:=> [:cat :potatoclient.ui-specs/theme-key] :nil]} [theme-key] (case theme-key :sol-light (LafManager/setTheme (SolarizedLightTheme.)) :sol-dark (LafManager/setTheme (SolarizedDarkTheme.)) :dark (LafManager/setTheme (OneDarkTheme.)) :hi-dark (LafManager/setTheme (HighContrastDarkTheme.)) (LafManager/setTheme (SolarizedDarkTheme.))) (LafManager/install))