(ns potatoclient.url-utils
  "URL validation and extraction utilities with localized error messages"
  (:require [clojure.string :as str]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [potatoclient.i18n :as i18n]))

;; -----------------------------------------------------------------------------
;; Domain/IP Validation Patterns
;; -----------------------------------------------------------------------------

(def ^:private ip-pattern
  "Pattern for IPv4 addresses"
  #"^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$")

(def ^:private domain-pattern
  "Pattern for domain names"
  #"^[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*$")

(def ^:private ipv6-pattern
  "Pattern for IPv6 addresses (simplified)"
  #"^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:))$")

;; -----------------------------------------------------------------------------
;; Validation Functions
;; -----------------------------------------------------------------------------

(>defn- valid-ipv4?
  "Check if string is a valid IPv4 address"
  [s]
  [string? => boolean?]
  (boolean
    (when (re-matches ip-pattern s)
      (try
        (let [octets (str/split s #"\.")]
          (and (= 4 (count octets))
               (every? #(<= 0 (Integer/parseInt %) 255) octets)))
        (catch NumberFormatException _
          false)))))

(>defn- valid-ipv6?
  "Check if string is a valid IPv6 address"
  [s]
  [string? => boolean?]
  (boolean (re-matches ipv6-pattern s)))

(>defn- valid-domain?
  "Check if string is a valid domain name"
  [s]
  [string? => boolean?]
  (boolean
    (and (re-matches domain-pattern s)
         (not (str/starts-with? s "-"))
         (not (str/ends-with? s "-"))
         (<= (count s) 253))))

(>defn valid-domain-or-ip?
  "Check if string is a valid domain name or IP address"
  [s]
  [string? => boolean?]
  (or (valid-ipv4? s)
      (valid-ipv6? s)
      ;; Only check domain if it doesn't look like an IP attempt
      (and (not (re-matches #"^\d+(\.\d+)*$" s))
           (valid-domain? s))))

;; -----------------------------------------------------------------------------
;; URL Extraction
;; -----------------------------------------------------------------------------

(>defn extract-domain
  "Extract domain/IP from various URL formats.
   Returns extracted domain or nil if invalid.
   
   Handles:
   - Plain domains: example.com
   - IPs: 192.168.1.1 or [::1]
   - URLs: https://example.com:8080/path?query
   - WebSocket URLs: wss://example.com/ws"
  [input]
  [string? => (? string?)]
  (let [cleaned (str/trim input)]
    (cond
      ;; Empty string
      (str/blank? cleaned)
      nil

      ;; Already just a domain/IP (no protocol, no path)
      (and (not (str/includes? cleaned "://"))
           (not (str/includes? cleaned "/"))
           (valid-domain-or-ip? cleaned))
      cleaned

      ;; Has protocol - extract the host part
      (str/includes? cleaned "://")
      (when-let [without-protocol (second (str/split cleaned #"://" 2))]
        (when (not (str/blank? without-protocol))
          (let [;; Remove query string and fragment first
                without-query (first (str/split without-protocol #"[?#]"))
                ;; Handle IPv6 addresses in brackets like [::1]
                host-part (if (str/starts-with? without-query "[")
                            (let [end-bracket (str/index-of without-query "]")]
                              (when end-bracket
                                (subs without-query 1 end-bracket)))
                            (first (str/split without-query #"[:/]")))]
            (when (and host-part (valid-domain-or-ip? host-part))
              host-part))))

      ;; Has path but no protocol - take everything before first /
      (str/includes? cleaned "/")
      (let [host-part (first (str/split cleaned #"/"))]
        (when (valid-domain-or-ip? host-part)
          host-part))

      ;; Doesn't match any pattern
      :else nil)))

;; -----------------------------------------------------------------------------
;; Validation with Localized Error Messages
;; -----------------------------------------------------------------------------

(>defn validate-url-input
  "Validate user input and return either {:valid true :domain domain} 
   or {:valid false :error localized-error-message}"
  [input]
  [string? => map?]
  (if (str/blank? input)
    {:valid false
     :error (i18n/tr :url-error-empty)}
    (if-let [domain (extract-domain input)]
      {:valid true
       :domain domain}
      {:valid false
       :error (i18n/tr :url-error-invalid-format)})))

;; -----------------------------------------------------------------------------
;; Example URL Formats for User Help
;; -----------------------------------------------------------------------------

(>defn get-example-formats
  "Get localized example URL formats for user help"
  []
  [=> [:vector string?]]
  [(i18n/tr :url-example-domain)      ; "example.com"
   (i18n/tr :url-example-subdomain)   ; "app.example.com"
   (i18n/tr :url-example-ip)          ; "192.168.1.100"
   (i18n/tr :url-example-ipv6)        ; "[2001:db8::1]"
   (i18n/tr :url-example-https)       ; "https://example.com"
   (i18n/tr :url-example-wss)         ; "wss://example.com:8080/ws"
   (i18n/tr :url-example-localhost)]) ; "localhost"