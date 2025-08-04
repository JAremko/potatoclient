# Adding Languages Guide

This guide explains how to add new language support to PotatoClient.

## Overview

PotatoClient uses EDN files for translations with a simple keyword-based system. Currently supports English and Ukrainian.

## Step 1: Create Translation File

Create a new EDN file in `resources/i18n/` named after the locale:

```bash
# For French
touch resources/i18n/fr.edn

# For German  
touch resources/i18n/de.edn
```

## Step 2: Add Translations

Copy an existing translation file as a template:

```clojure
;; resources/i18n/fr.edn
{:app-title "PotatoClient"
 
 ;; File menu
 :menu-file "Fichier"
 :menu-file-settings "Paramètres"
 :menu-file-refresh "Actualiser"
 :menu-file-exit "Quitter"
 
 ;; View menu
 :menu-view "Affichage"
 :menu-view-theme "Thème"
 :menu-view-language "Langue"
 :menu-view-always-on-top "Toujours au premier plan"
 
 ;; Help menu
 :menu-help "Aide"
 :menu-help-about "À propos"
 :menu-help-logs "Afficher les journaux"
 
 ;; Languages
 :lang-english "Anglais"
 :lang-ukrainian "Ukrainien"
 :lang-french "Français"  ; Add your language
 
 ;; Themes
 :theme-sol-dark "Sol Sombre"
 :theme-sol-light "Sol Clair"
 :theme-dark "Sombre"
 :theme-hi-dark "Très Sombre"
 
 ;; About dialog
 :about-title "À propos de PotatoClient"
 :about-version "Version"
 :about-build "Type de build"
 :about-close "Fermer"
 
 ;; Settings dialog
 :settings-title "Paramètres"
 :settings-domain "Domaine"
 :settings-save "Enregistrer"
 :settings-cancel "Annuler"
 
 ;; Stream states
 :stream-connecting "Connexion..."
 :stream-connected "Connecté"
 :stream-disconnected "Déconnecté"
 :stream-error "Erreur"}
```

## Step 3: Update Malli Schema

Add the new locale to the schema in `potatoclient.ui-specs`:

```clojure
(def locale
  "Supported locales"
  [:enum :english :ukrainian :french])  ; Add :french
```

## Step 4: Update i18n Namespace

Modify `src/potatoclient/i18n.clj`:

### 4.1 Add to Translation Loading

```clojure
(>defn- load-translations!
  []
  [=> nil?]
  (reset! translations
          {:en (load-edn-file "i18n/en.edn")
           :uk (load-edn-file "i18n/uk.edn")
           :fr (load-edn-file "i18n/fr.edn")})  ; Add French
  nil)
```

### 4.2 Update Translation Function

```clojure
(>defn tr
  [key & args]
  [keyword? (s/* any?) => (? string?)]
  (let [lang (case @current-language
               :english :en
               :ukrainian :uk
               :french :fr  ; Add mapping
               :en)]
    ;; ... rest of function
```

## Step 5: Add Menu Option

Update the language menu in `src/potatoclient/core.clj`:

```clojure
(defn- create-language-menu
  []
  (let [current-lang @current-language
        lang-group (seesaw/button-group)]
    (seesaw/menu
     :text (tr :menu-view-language)
     :items [(create-radio-item :english current-lang lang-group)
             (create-radio-item :ukrainian current-lang lang-group)
             (create-radio-item :french current-lang lang-group)])))  ; Add French
```

## Step 6: Test Your Translation

```clojure
;; In REPL
(require '[potatoclient.i18n :as i18n])

;; Set language
(i18n/set-language! :french)

;; Test translations
(i18n/tr :menu-file)  ; => "Fichier"
(i18n/tr :about-title) ; => "À propos de PotatoClient"

;; Reload translations during development
(i18n/reload-translations!)
```

## Step 7: Add Tests

Create a test for your translation:

```clojure
(deftest french-translation-test
  (testing "French translations are complete"
    (i18n/set-language! :french)
    (is (string? (i18n/tr :app-title)))
    (is (string? (i18n/tr :menu-file)))
    ;; Test all required keys
    (doseq [k (keys (i18n/get-translations :en))]
      (is (string? (i18n/tr k))
          (str "Missing French translation for " k)))))
```

## Common Patterns

### Date/Time Formatting

For locale-specific formatting, extend the translation system:

```clojure
;; In translation file
{:date-format "d MMMM yyyy"      ; French: "15 janvier 2024"
 :time-format "HH:mm"            ; 24-hour format
 :datetime-format "d MMM à HH:mm"}

;; Usage
(defn format-date [date]
  (let [formatter (DateTimeFormatter/ofPattern (tr :date-format)
                                              (get-locale))]
    (.format date formatter)))
```

### Pluralization

For languages with complex plural rules:

```clojure
;; In translation file
{:items-count-0 "Aucun élément"
 :items-count-1 "1 élément"
 :items-count-n "%d éléments"}

;; Helper function
(defn tr-plural [key n]
  (let [plural-key (cond
                    (= n 0) (keyword (str (name key) "-0"))
                    (= n 1) (keyword (str (name key) "-1"))
                    :else (keyword (str (name key) "-n")))]
    (format (tr plural-key) n)))
```

### Right-to-Left Languages

For RTL languages (Arabic, Hebrew):

```clojure
;; Add metadata to translation file
{:rtl? true  ; Mark as RTL
 :app-title "PotatoClient"
 ;; ... translations
}

;; Apply RTL layout
(when (:rtl? (i18n/get-translations (i18n/current-language)))
  (seesaw/config! frame :component-orientation :right-to-left))
```

## Best Practices

1. **Complete Coverage**: Ensure all keys from English are translated
2. **Consistent Terminology**: Use same terms throughout
3. **Test UI Layout**: Some languages need more space
4. **Format Appropriately**: Use correct date/number formats
5. **Review**: Have native speaker review translations

## Validation

Run validation to ensure completeness:

```bash
# Check translation coverage
bb validate-translations

# This will report:
# - Missing keys in each language
# - Keys in translation but not in English
# - Formatting inconsistencies
```

## Tips

- Keep translations concise for UI elements
- Use comments in EDN files to explain context
- Test with actual UI to ensure text fits
- Consider cultural differences in terminology
- Document any special formatting needs

## See Also

- [i18n.clj](https://github.com/JAremko/potatoclient/blob/main/src/potatoclient/i18n.clj)
- [Localization Best Practices](https://www.w3.org/International/questions/qa-i18n)