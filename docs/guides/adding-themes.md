# Adding Themes Guide

This guide explains how to add new themes to PotatoClient.

## Overview

PotatoClient themes are defined as Clojure maps in the `potatoclient.theme` namespace. Themes control colors, fonts, and UI appearance.

## Theme Structure

A theme is a map with these keys:

```clojure
{:name "My Theme"              ; Display name
 :key :my-theme               ; Unique keyword identifier
 
 ;; Core colors
 :background "#1a1a1a"        ; Main background
 :foreground "#e0e0e0"        ; Main text
 :selection-bg "#3a3a3a"      ; Selected items
 :selection-fg "#ffffff"      ; Selected text
 
 ;; UI elements
 :button-bg "#2a2a2a"         ; Button background
 :button-fg "#e0e0e0"         ; Button text
 :border "#3a3a3a"            ; Borders
 :separator "#2a2a2a"         ; Separators
 
 ;; Text elements
 :text-bg "#222222"           ; Text field background
 :text-fg "#e0e0e0"           ; Text field text
 :caret "#ffffff"             ; Text cursor
 
 ;; Feedback colors
 :error "#ff5555"             ; Error messages
 :warning "#ffaa55"           ; Warnings
 :success "#55ff55"           ; Success
 :info "#5555ff"              ; Information
 
 ;; Video canvas
 :canvas-bg "#000000"         ; Video background
 :canvas-border "#3a3a3a"     ; Video border
 
 ;; Optional extras
 :tooltip-bg "#2a2a2a"        ; Tooltip background
 :tooltip-fg "#e0e0e0"        ; Tooltip text
 :menu-bg "#1a1a1a"           ; Menu background
 :menu-fg "#e0e0e0"}          ; Menu text
```

## Step 1: Define Your Theme

Add your theme to `src/potatoclient/theme.clj`:

```clojure
(def themes
  "Available themes"
  {:sol-dark sol-dark-theme
   :sol-light sol-light-theme
   :dark dark-theme
   :hi-dark hi-dark-theme
   :my-theme my-theme})  ; Add your theme

;; Define your theme
(def my-theme
  {:name "My Theme"
   :key :my-theme
   
   ;; Base colors
   :background "#1e1e2e"      ; Catppuccin Mocha base
   :foreground "#cdd6f4"      ; Catppuccin Mocha text
   :selection-bg "#45475a"    ; Surface0
   :selection-fg "#cdd6f4"
   
   ;; UI elements
   :button-bg "#313244"       ; Surface0
   :button-fg "#cdd6f4"
   :border "#45475a"          ; Surface1
   :separator "#313244"
   
   ;; Text
   :text-bg "#1e1e2e"
   :text-fg "#cdd6f4"
   :caret "#f5e0dc"          ; Rosewater
   
   ;; Semantic colors
   :error "#f38ba8"          ; Red
   :warning "#fab387"        ; Peach
   :success "#a6e3a1"        ; Green
   :info "#89b4fa"           ; Blue
   
   ;; Video
   :canvas-bg "#11111b"      ; Crust
   :canvas-border "#313244"})
```

## Step 2: Update Theme Functions

Update the `get-available-themes` function:

```clojure
(>defn get-available-themes
  "Get list of available themes"
  []
  [=> [:sequential ::specs/theme-key]]
  [:sol-dark :sol-light :dark :hi-dark :my-theme])  ; Add your theme key
```

## Step 3: Add Translation

Add theme name translation to language files:

```clojure
;; In resources/i18n/en.edn
{:theme-my-theme "My Theme"
 ;; ... other translations
}

;; In resources/i18n/uk.edn  
{:theme-my-theme "Моя Тема"
 ;; ... other translations
}
```

## Step 4: Test Your Theme

```clojure
;; In REPL
(require '[potatoclient.theme :as theme])

;; Apply theme
(theme/apply-theme! :my-theme)

;; Get theme colors
(theme/get-color :background)  ; => "#1e1e2e"
(theme/get-color :error)       ; => "#f38ba8"
```

## Advanced Theme Features

### Derived Colors

Calculate colors based on base colors:

```clojure
(defn- lighten [color percent]
  ;; Implement color lightening
  )

(def my-advanced-theme
  (let [base "#1e1e2e"
        text "#cdd6f4"]
    {:name "My Advanced Theme"
     :key :my-advanced
     :background base
     :foreground text
     :button-bg (lighten base 10)     ; Derived
     :button-hover (lighten base 20)  ; Derived
     ;; ... rest of theme
     }))
```

### Platform-Specific Adjustments

```clojure
(defn- adjust-for-platform [theme]
  (cond
    (platform/windows?)
    (assoc theme :border (lighten (:border theme) 5))
    
    (platform/macos?)
    (assoc theme :menu-bg (:background theme))
    
    :else theme))
```

### Font Customization

Extend themes with font settings:

```clojure
(def my-theme-with-fonts
  (merge my-theme
         {:font-family "JetBrains Mono"
          :font-size 12
          :font-bold "JetBrains Mono Bold"
          :font-mono "Consolas"
          :line-height 1.5}))
```

## Creating Theme Variants

### Dark/Light Pairs

```clojure
(def my-theme-light
  (-> my-theme
      (assoc :name "My Theme Light")
      (assoc :key :my-theme-light)
      (assoc :background "#ffffff")
      (assoc :foreground "#1e1e2e")
      ;; Invert other colors
      ))
```

### Contrast Variants

```clojure
(def my-theme-high-contrast
  (-> my-theme
      (assoc :name "My Theme HC")
      (assoc :key :my-theme-hc)
      (assoc :foreground "#ffffff")  ; Pure white
      (assoc :background "#000000")  ; Pure black
      (assoc :border "#ffffff")))
```

## Theme Guidelines

### Color Selection

1. **Contrast**: Ensure sufficient contrast (WCAG AA minimum)
2. **Consistency**: Use consistent color meanings
3. **Accessibility**: Test with color blindness simulators
4. **Harmony**: Use color theory for pleasing combinations

### Testing Checklist

- [ ] All UI elements visible
- [ ] Text readable on all backgrounds
- [ ] Buttons have hover states
- [ ] Selections clearly visible
- [ ] Error/warning colors distinct
- [ ] Video canvas borders visible
- [ ] Menu items readable

### Performance

Themes are applied by updating Swing look-and-feel:

```clojure
;; Avoid frequent theme switches
(debounce theme-switch 500)

;; Cache color lookups if needed
(def color-cache (atom {}))
```

## Example: Complete Theme

Here's a complete example with all options:

```clojure
(def nord-theme
  {:name "Nord"
   :key :nord
   
   ;; Polar Night
   :background "#2e3440"      ; nord0
   :foreground "#d8dee9"      ; nord4
   
   ;; Snow Storm
   :text-fg "#eceff4"         ; nord6
   :text-bg "#3b4252"         ; nord1
   
   ;; Frost
   :selection-bg "#5e81ac"    ; nord10
   :selection-fg "#eceff4"
   :info "#88c0d0"           ; nord8
   :link "#81a1c1"           ; nord9
   
   ;; Aurora
   :error "#bf616a"          ; nord11
   :warning "#d08770"        ; nord12
   :success "#a3be8c"        ; nord14
   
   ;; UI Elements
   :border "#4c566a"         ; nord3
   :separator "#434c5e"      ; nord2
   :button-bg "#4c566a"
   :button-fg "#d8dee9"
   :caret "#d8dee9"
   
   ;; Video
   :canvas-bg "#2e3440"
   :canvas-border "#4c566a"
   
   ;; Menus
   :menu-bg "#2e3440"
   :menu-fg "#d8dee9"
   :menu-selection-bg "#4c566a"
   :menu-selection-fg "#eceff4"
   
   ;; Tooltips
   :tooltip-bg "#3b4252"
   :tooltip-fg "#d8dee9"
   :tooltip-border "#4c566a"})
```

## Publishing Your Theme

1. **Screenshot**: Include theme screenshots
2. **Color Palette**: Document color choices
3. **Accessibility**: Include contrast ratios
4. **Installation**: Provide clear instructions

```markdown
## My Theme

A warm, comfortable theme inspired by coffee.

### Colors
- Background: `#1e1e2e` (Dark roast)
- Foreground: `#cdd6f4` (Cream)
- Accent: `#fab387` (Caramel)

### Installation
1. Add theme definition to `theme.clj`
2. Update `get-available-themes`
3. Add translations
```

## See Also

- [Seesaw Styling Guide](https://github.com/clj-commons/seesaw/wiki/Styling)
- [Color Theory](https://www.colormatters.com/color-and-design/basic-color-theory)
- [WCAG Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)