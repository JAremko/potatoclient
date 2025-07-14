(ns potatoclient.i18n
  "Internationalization support for PotatoClient"
  (:require [tongue.core :as tongue]
            [potatoclient.state :as state]))

;; Define translations
(def translations
  {:en {:app-title "PotatoClient"
        :app-version "Version"
        
        ;; Menu items
        :menu-file "File"
        :menu-file-export "Export Log"
        :menu-file-exit "Exit"
        :menu-view "View"
        :menu-view-theme "Theme"
        :menu-view-language "Language"
        :menu-help "Help"
        :menu-help-about "About"
        
        ;; Connection status
        :status-disconnected "Disconnected"
        :status-connecting "Connecting..."
        :status-connected "Connected"
        :status-failed "Connection Failed"
        
        ;; Control panel
        :control-label-system "System Control"
        :control-button-connect "Connect"
        :control-button-disconnect "Disconnect"
        :control-label-heat "Heat Stream"
        :control-label-day "Day Stream"
        :control-checkbox-enable "Enable"
        :control-checkbox-autoconnect "Auto-connect"
        
        ;; Log table
        :log-column-time "Time"
        :log-column-type "Type"
        :log-column-source "Source"
        :log-column-message "Message"
        
        ;; Export dialog
        :export-title "Export Log"
        :export-button-export "Export"
        :export-button-cancel "Cancel"
        :export-success "Log exported successfully"
        :export-error "Error exporting log"
        
        ;; About dialog
        :about-title "About PotatoClient"
        :about-text "Video streaming client with dual H.264 WebSocket streams.\n\nMain process handles UI, subprocesses handle video streams."
        :about-button-close "Close"
        
        ;; Configuration
        :config-server-address "Server Address"
        :config-server-port "Server Port"
        :config-reconnect-delay "Reconnect Delay (ms)"
        :config-max-reconnect-attempts "Max Reconnect Attempts"
        
        ;; Stream configuration
        :config-heat-stream "Heat Stream Configuration"
        :config-day-stream "Day Stream Configuration"
        :config-stream-width "Width"
        :config-stream-height "Height"
        :config-stream-framerate "Framerate"
        :config-stream-decoder "Decoder"
        
        ;; Decoder options
        :decoder-auto "Auto-detect"
        :decoder-nvidia "NVIDIA (nvh264dec)"
        :decoder-intel "Intel QSV (msdkh264dec)"
        :decoder-vaapi "VA-API (vaapih264dec)"
        :decoder-software "Software (avdec_h264)"
        
        ;; Error messages
        :error-connection-failed "Failed to connect to server"
        :error-stream-failed "Stream failed to start"
        :error-subprocess-died "Subprocess terminated unexpectedly"
        :error-export-failed "Failed to export log file"}
   
   :uk {:app-title "PotatoClient"
        :app-version "Версія"
        
        ;; Menu items
        :menu-file "Файл"
        :menu-file-export "Експортувати журнал"
        :menu-file-exit "Вихід"
        :menu-view "Вигляд"
        :menu-view-theme "Тема"
        :menu-view-language "Мова"
        :menu-help "Допомога"
        :menu-help-about "Про програму"
        
        ;; Connection status
        :status-disconnected "Від'єднано"
        :status-connecting "Підключення..."
        :status-connected "Підключено"
        :status-failed "Помилка підключення"
        
        ;; Control panel
        :control-label-system "Керування системою"
        :control-button-connect "Підключити"
        :control-button-disconnect "Від'єднати"
        :control-label-heat "Тепловий потік"
        :control-label-day "Денний потік"
        :control-checkbox-enable "Увімкнути"
        :control-checkbox-autoconnect "Автопідключення"
        
        ;; Log table
        :log-column-time "Час"
        :log-column-type "Тип"
        :log-column-source "Джерело"
        :log-column-message "Повідомлення"
        
        ;; Export dialog
        :export-title "Експорт журналу"
        :export-button-export "Експортувати"
        :export-button-cancel "Скасувати"
        :export-success "Журнал успішно експортовано"
        :export-error "Помилка експорту журналу"
        
        ;; About dialog
        :about-title "Про PotatoClient"
        :about-text "Клієнт відеопотоку з подвійними H.264 WebSocket потоками.\n\nГоловний процес керує інтерфейсом, підпроцеси обробляють відеопотоки."
        :about-button-close "Закрити"
        
        ;; Configuration
        :config-server-address "Адреса сервера"
        :config-server-port "Порт сервера"
        :config-reconnect-delay "Затримка перепідключення (мс)"
        :config-max-reconnect-attempts "Макс. спроб перепідключення"
        
        ;; Stream configuration
        :config-heat-stream "Налаштування теплового потоку"
        :config-day-stream "Налаштування денного потоку"
        :config-stream-width "Ширина"
        :config-stream-height "Висота"
        :config-stream-framerate "Частота кадрів"
        :config-stream-decoder "Декодер"
        
        ;; Decoder options
        :decoder-auto "Автовизначення"
        :decoder-nvidia "NVIDIA (nvh264dec)"
        :decoder-intel "Intel QSV (msdkh264dec)"
        :decoder-vaapi "VA-API (vaapih264dec)"
        :decoder-software "Програмний (avdec_h264)"
        
        ;; Error messages
        :error-connection-failed "Не вдалося підключитися до сервера"
        :error-stream-failed "Не вдалося запустити потік"
        :error-subprocess-died "Підпроцес несподівано завершився"
        :error-export-failed "Не вдалося експортувати файл журналу"}})

;; Create translator instance
(def translate (tongue/build-translate translations))

(defn tr
  "Translate a key using current locale"
  ([key]
   (tr key []))
  ([key args]
   (let [locale (case (state/get-locale)
                  :english :en
                  :ukrainian :uk
                  :en)]
     (apply translate locale key args))))

(defn init!
  "Initialize localization system"
  []
  ;; Set initial locale
  (state/set-locale! (state/get-locale)))