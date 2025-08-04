(ns potatoclient.url-parser
  "URL parsing using Instaparse grammar and core.match for robust extraction"
  (:require [clojure.string :as str]
            [clojure.core.match :refer [match]]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [instaparse.core :as insta]
            [potatoclient.i18n :as i18n]))

;; -----------------------------------------------------------------------------
;; Pre-processing to help parser
;; -----------------------------------------------------------------------------

(>defn- looks-like-ipv4?
  "Quick check if string looks like an IPv4 address"
  [s]
  [string? => boolean?]
  (boolean (re-matches #"^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$" s)))

;; -----------------------------------------------------------------------------
;; Grammar Definition
;; -----------------------------------------------------------------------------

(def url-grammar
  "Grammar for parsing URLs and domain/IP inputs"
  (insta/parser
    "
    INPUT = URL | HOST_PORT_MAYBE_PATH | IPV6_BRACKETED | HOST | EMPTY
    
    URL = PROTOCOL AUTHORITY PATH? QUERY? FRAGMENT?
    
    PROTOCOL = ('http' | 'https' | 'ws' | 'wss') '://'
    
    AUTHORITY = (IPV6_BRACKETED | HOST) PORT?
    
    HOST_PORT_MAYBE_PATH = HOST PORT? TRAILING_SLASH?
    
    <HOST> = IPV4 | IPV6 | DOMAIN
    
    IPV6_BRACKETED = '[' IPV6 ']'
    
    IPV4 = OCTET '.' OCTET '.' OCTET '.' OCTET
    OCTET = #'[0-9]{1,3}'
    
    IPV6 = #'[0-9a-fA-F:]+'
    
    DOMAIN = LABEL ('.' LABEL)*
    LABEL = #'[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?'
    
    PORT = ':' #'[0-9]{1,5}'
    
    PATH = '/' #'[^?#]*'
    
    QUERY = '?' #'[^#]*'
    
    FRAGMENT = '#' #'.*'
    
    TRAILING_SLASH = '/'
    
    EMPTY = #'\\s*'
    "))

;; -----------------------------------------------------------------------------
;; Validation Functions
;; -----------------------------------------------------------------------------

(>defn- valid-octet?
  "Check if string represents a valid octet (0-255)"
  [s]
  [string? => boolean?]
  (try
    (<= 0 (Integer/parseInt s) 255)
    (catch NumberFormatException _
      false)))

(>defn- valid-port?
  "Check if string represents a valid port (1-65535)"
  [s]
  [string? => boolean?]
  (try
    (let [port (Integer/parseInt s)]
      (<= 1 port 65535))
    (catch NumberFormatException _
      false)))

(>defn- valid-ipv4?
  "Validate IPv4 from parsed components"
  [octets]
  [sequential? => boolean?]
  (and (= 4 (count octets))
       (every? valid-octet? octets)))

(>defn- valid-ipv6?
  "Basic IPv6 validation"
  [addr]
  [string? => boolean?]
  ;; Basic check - proper IPv6 validation is complex
  (boolean (and (str/includes? addr ":")
                (re-matches #"^[0-9a-fA-F:]+$" addr))))

;; -----------------------------------------------------------------------------
;; AST Processing with core.match
;; -----------------------------------------------------------------------------

(>defn- extract-text
  "Extract text content from parse tree node"
  [node]
  [any? => string?]
  (cond
    (string? node) node
    (and (vector? node) (= :OCTET (first node))) (second node)
    (and (vector? node) (= :LABEL (first node))) (second node)
    :else (apply str (filter string? node))))

(>defn- process-ipv4
  "Process IPV4 node"
  [ipv4-node]
  [vector? => (? string?)]
  (let [octets (map extract-text (filter #(and (vector? %) (= :OCTET (first %))) ipv4-node))]
    (when (valid-ipv4? octets)
      (str/join "." octets))))

(>defn- process-domain
  "Process DOMAIN node"
  [domain-node]
  [vector? => string?]
  (let [labels (map extract-text (filter #(and (vector? %) (= :LABEL (first %))) domain-node))]
    (str/join "." labels)))

(>defn- extract-host
  "Extract host from various node types"
  [node]
  [any? => (? string?)]
  (match node
    [:IPV4 & _] (process-ipv4 node)
    [:IPV6 addr] (when (valid-ipv6? addr) addr)
    [:IPV6_BRACKETED _ [:IPV6 addr] _] (when (valid-ipv6? addr) addr)
    [:DOMAIN & _] (process-domain node)
    _ nil))

(>defn- extract-host-from-ast
  "Extract host (domain or IP) from parsed AST"
  [ast]
  [any? => (? string?)]
  (match ast
    ;; Full URL
    [:INPUT [:URL _ [:AUTHORITY host & _] & _]]
    (extract-host host)

    ;; Host with port and maybe trailing slash
    [:INPUT [:HOST_PORT_MAYBE_PATH host & _]]
    (extract-host host)

    ;; IPv6 bracketed
    [:INPUT [:IPV6_BRACKETED _ [:IPV6 addr] _]]
    (when (valid-ipv6? addr) addr)

    ;; Just host
    [:INPUT host]
    (extract-host host)

    ;; Empty
    [:INPUT [:EMPTY]]
    nil

    :else nil))

;; -----------------------------------------------------------------------------
;; Public API
;; -----------------------------------------------------------------------------

(>defn parse-url
  "Parse URL and return parse tree or failure"
  [input]
  [string? => any?]
  (let [trimmed (str/trim input)
        result (url-grammar trimmed)]
    ;; If we parsed something as a domain, check if it might be an invalid IPv4
    (if (and (not (insta/failure? result))
             (= (first (second result)) :DOMAIN))
      (let [parts (str/split trimmed #"\.")]
        ;; Check if all parts are numeric (potential IPv4)
        (if (every? #(re-matches #"^\d+$" %) parts)
          ;; It's numeric, validate as IPv4
          (if (and (= 4 (count parts))
                   (every? valid-octet? parts))
            ;; Valid IPv4, reconstruct as IPv4 node
            [:INPUT [:IPV4 [:OCTET (nth parts 0)] "."
                     [:OCTET (nth parts 1)] "."
                     [:OCTET (nth parts 2)] "."
                     [:OCTET (nth parts 3)]]]
            ;; Invalid IPv4 format
            (url-grammar "!invalid!"))
          ;; Not numeric, keep as domain
          result))
      result)))

(>defn extract-domain
  "Extract domain/IP from various URL formats using Instaparse.
   Returns extracted domain or nil if invalid."
  [input]
  [string? => (? string?)]
  (let [parsed (parse-url input)]
    (if (insta/failure? parsed)
      nil
      (extract-host-from-ast parsed))))

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

(>defn parse-tree-node-type
  "Get the type of node in the parse tree (the first element after :INPUT)"
  [input]
  [string? => (? keyword?)]
  (let [parsed (parse-url input)]
    (when-not (insta/failure? parsed)
      (match parsed
        [:INPUT [:URL & _]] :URL
        [:INPUT [:HOST_PORT_MAYBE_PATH & _]] :HOST_PORT
        [:INPUT [:IPV6_BRACKETED & _]] :IPV6_BRACKETED
        [:INPUT [:IPV4 & _]] :IPV4
        [:INPUT [:IPV6 & _]] :IPV6
        [:INPUT [:DOMAIN & _]] :DOMAIN
        [:INPUT [:EMPTY]] :EMPTY
        :else nil))))