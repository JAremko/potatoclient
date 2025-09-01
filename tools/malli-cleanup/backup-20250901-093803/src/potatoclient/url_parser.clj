(ns potatoclient.url-parser
  "Unified URL parsing and validation using Instaparse grammar.
   Single source of truth for all URL/domain/IP validation in the application."
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [instaparse.core :as insta]))

;; -----------------------------------------------------------------------------
;; Pre-processing to help parser
;; -----------------------------------------------------------------------------

(defn- looks-like-ipv4?
  "Quick check if string looks like an IPv4 address" {:malli/schema [:=> [:cat :string] :boolean]}
  [s]
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

(defn- valid-octet?
  "Check if string represents a valid octet (0-255)" {:malli/schema [:=> [:cat :string] :boolean]}
  [s]
  (try
    (<= 0 (Integer/parseInt s) 255)
    (catch NumberFormatException _
      false)))

(defn- valid-port?
  "Check if string represents a valid port (1-65535)" {:malli/schema [:=> [:cat :string] :boolean]}
  [s]
  (try
    (let [port (Integer/parseInt s)]
      (<= 1 port 65535))
    (catch NumberFormatException _
      false)))

(defn- valid-ipv4?
  "Validate IPv4 from parsed components" {:malli/schema [:=> [:cat :sequential] :boolean]}
  [octets]
  (and (= 4 (count octets))
       (every? valid-octet? octets)))

(defn- valid-ipv6?
  "Basic IPv6 validation" {:malli/schema [:=> [:cat :string] :boolean]}
  [addr]
  ;; Basic check - proper IPv6 validation is complex
  (boolean (and (str/includes? addr ":")
                (re-matches #"^[0-9a-fA-F:]+$" addr))))

;; -----------------------------------------------------------------------------
;; AST Processing with core.match
;; -----------------------------------------------------------------------------

(defn- extract-text
  "Extract text content from parse tree node" {:malli/schema [:=> [:cat :any] :string]}
  [node]
  (cond
    (string? node) node
    (and (vector? node) (= :OCTET (first node))) (second node)
    (and (vector? node) (= :LABEL (first node))) (second node)
    :else (apply str (filter string? node))))

(defn- process-ipv4
  "Process IPV4 node" {:malli/schema [:=> [:cat :vector] [:maybe :string]]}
  [ipv4-node]
  (let [octets (map extract-text (filter #(and (vector? %) (= :OCTET (first %))) ipv4-node))]
    (when (valid-ipv4? octets)
      (str/join "." octets))))

(defn- process-domain
  "Process DOMAIN node" {:malli/schema [:=> [:cat :vector] :string]}
  [domain-node]
  (let [labels (map extract-text (filter #(and (vector? %) (= :LABEL (first %))) domain-node))]
    (str/join "." labels)))

(defn- extract-host
  "Extract host from various node types" {:malli/schema [:=> [:cat :any] [:maybe :string]]}
  [node]
  (match node
    [:IPV4 & _] (process-ipv4 node)
    [:IPV6 addr] (when (valid-ipv6? addr) addr)
    [:IPV6_BRACKETED _ [:IPV6 addr] _] (when (valid-ipv6? addr) addr)
    [:DOMAIN & _] (process-domain node)
    _ nil))

(defn- extract-host-from-ast
  "Extract host (domain or IP) from parsed AST" {:malli/schema [:=> [:cat :any] [:maybe :string]]}
  [ast]
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

(defn parse-url
  "Parse URL and return parse tree or failure" {:malli/schema [:=> [:cat :string] :any]}
  [input]
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

(defn extract-domain
  "Extract domain/IP from various URL formats using Instaparse.
   Returns extracted domain or nil if invalid." {:malli/schema [:=> [:cat :string] [:maybe :string]]}
  [input]
  (let [parsed (parse-url input)]
    (if (insta/failure? parsed)
      nil
      (extract-host-from-ast parsed))))

(defn validate-url-input
  "Validate user input and return either {:valid true :domain domain} 
   or {:valid false :error error-keyword}" {:malli/schema [:=> [:cat :string] :map]}
  [input]
  (if (str/blank? input)
    {:valid false
     :error :url-error-empty}
    (if-let [domain (extract-domain input)]
      {:valid true
       :domain domain}
      {:valid false
       :error :url-error-invalid-format})))

(defn get-example-format-keys
  "Get example URL format keys for user help" {:malli/schema [:=> [:cat] [:vector :keyword]]}
  []
  [:url-example-domain      ; "example.com"
   :url-example-subdomain   ; "app.example.com"
   :url-example-ip          ; "192.168.1.100"
   :url-example-ipv6        ; "[2001:db8::1]"
   :url-example-https       ; "https://example.com"
   :url-example-wss         ; "wss://example.com:8080/ws"
   :url-example-localhost]) ; "localhost"

(defn parse-tree-node-type
  "Get the type of node in the parse tree (the first element after :INPUT)" {:malli/schema [:=> [:cat :string] [:maybe :keyword]]}
  [input]
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

;; -----------------------------------------------------------------------------
;; Validation API using Grammar
;; -----------------------------------------------------------------------------

(defn valid-domain-or-ip?
  "Check if string is a valid domain name or IP address using the grammar.
   This is the single source of truth for domain/IP validation." {:malli/schema [:=> [:cat :string] :boolean]}
  [s]
  (let [parsed (parse-url s)]
    (and (not (insta/failure? parsed))
         (not= (parse-tree-node-type s) :EMPTY)
         (some? (extract-host-from-ast parsed)))))

(defn valid-url?
  "Check if string is a valid URL (has protocol)" {:malli/schema [:=> [:cat :string] :boolean]}
  [s]
  (= (parse-tree-node-type s) :URL))