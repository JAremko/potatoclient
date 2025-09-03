(ns potatoclient.url-parser
  "Unified URL parsing and validation using Instaparse grammar.
   Single source of truth for all URL/domain/IP validation in the application."
  (:require
            [malli.core :as m] [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [instaparse.core :as insta]))

;; -----------------------------------------------------------------------------
;; Pre-processing to help parser
;; -----------------------------------------------------------------------------

(defn- looks-like-ipv4?
  "Quick check if string looks like an IPv4 address"
  [s]
  (boolean (re-matches #"^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$" s))) 
 (m/=> looks-like-ipv4? [:=> [:cat :string] :boolean])

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
  "Check if string represents a valid octet (0-255)"
  [s]
  (try
    (<= 0 (Integer/parseInt s) 255)
    (catch NumberFormatException _
      false))) 
 (m/=> valid-octet? [:=> [:cat :string] :boolean])

(defn- valid-port?
  "Check if string represents a valid port (1-65535)"
  [s]
  (try
    (let [port (Integer/parseInt s)]
      (<= 1 port 65535))
    (catch NumberFormatException _
      false))) 
 (m/=> valid-port? [:=> [:cat :string] :boolean])

(defn- valid-ipv4?
  "Validate IPv4 from parsed components"
  [octets]
  (and (= 4 (count octets))
       (every? valid-octet? octets))) 
 (m/=> valid-ipv4? [:=> [:cat :sequential] :boolean])

(defn- valid-ipv6?
  "Basic IPv6 validation"
  [addr]
  ;; Basic check - proper IPv6 validation is complex
  (boolean (and (str/includes? addr ":")
                (re-matches #"^[0-9a-fA-F:]+$" addr)))) 
 (m/=> valid-ipv6? [:=> [:cat :string] :boolean])

;; -----------------------------------------------------------------------------
;; AST Processing with core.match
;; -----------------------------------------------------------------------------

(defn- extract-text
  "Extract text content from parse tree node"
  [node]
  (cond
    (string? node) node
    (and (vector? node) (= :OCTET (first node))) (second node)
    (and (vector? node) (= :LABEL (first node))) (second node)
    :else (apply str (filter string? node)))) 
 (m/=> extract-text [:=> [:cat :any] :string])

(defn- process-ipv4
  "Process IPV4 node"
  [ipv4-node]
  (let [octets (map extract-text (filter #(and (vector? %) (= :OCTET (first %))) ipv4-node))]
    (when (valid-ipv4? octets)
      (str/join "." octets)))) 
 (m/=> process-ipv4 [:=> [:cat :vector] [:maybe :string]])

(defn- process-domain
  "Process DOMAIN node"
  [domain-node]
  (let [labels (map extract-text (filter #(and (vector? %) (= :LABEL (first %))) domain-node))]
    (str/join "." labels))) 
 (m/=> process-domain [:=> [:cat :vector] :string])

(defn- extract-host
  "Extract host from various node types"
  [node]
  (match node
    [:IPV4 & _] (process-ipv4 node)
    [:IPV6 addr] (when (valid-ipv6? addr) addr)
    [:IPV6_BRACKETED _ [:IPV6 addr] _] (when (valid-ipv6? addr) addr)
    [:DOMAIN & _] (process-domain node)
    _ nil)) 
 (m/=> extract-host [:=> [:cat :any] [:maybe :string]])

(defn- extract-host-from-ast
  "Extract host (domain or IP) from parsed AST"
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
 (m/=> extract-host-from-ast [:=> [:cat :any] [:maybe :string]])

;; -----------------------------------------------------------------------------
;; Public API
;; -----------------------------------------------------------------------------

(defn parse-url
  "Parse URL and return parse tree or failure"
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
 (m/=> parse-url [:=> [:cat :string] :any])

(defn extract-domain
  "Extract domain/IP from various URL formats using Instaparse.
   Returns extracted domain or nil if invalid."
  [input]
  (let [parsed (parse-url input)]
    (if (insta/failure? parsed)
      nil
      (extract-host-from-ast parsed)))) 
 (m/=> extract-domain [:=> [:cat :string] [:maybe :string]])

(defn validate-url-input
  "Validate user input and return either {:valid true :domain domain} 
   or {:valid false :error error-keyword}"
  [input]
  (if (str/blank? input)
    {:valid false
     :error :url-error-empty}
    (if-let [domain (extract-domain input)]
      {:valid true
       :domain domain}
      {:valid false
       :error :url-error-invalid-format}))) 
 (m/=> validate-url-input [:=> [:cat :string] :map])

(defn get-example-format-keys
  "Get example URL format keys for user help"
  []
  [:url-example-domain      ; "example.com"
   :url-example-subdomain   ; "app.example.com"
   :url-example-ip          ; "192.168.1.100"
   :url-example-ipv6        ; "[2001:db8::1]"
   :url-example-https       ; "https://example.com"
   :url-example-wss         ; "wss://example.com:8080/ws"
   :url-example-localhost]) 
 (m/=> get-example-format-keys [:=> [:cat] [:vector :keyword]]) ; "localhost"

(defn parse-tree-node-type
  "Get the type of node in the parse tree (the first element after :INPUT)"
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
 (m/=> parse-tree-node-type [:=> [:cat :string] [:maybe :keyword]])

;; -----------------------------------------------------------------------------
;; Validation API using Grammar
;; -----------------------------------------------------------------------------

(defn valid-domain-or-ip?
  "Check if string is a valid domain name or IP address using the grammar.
   This is the single source of truth for domain/IP validation."
  [s]
  (let [parsed (parse-url s)]
    (and (not (insta/failure? parsed))
         (not= (parse-tree-node-type s) :EMPTY)
         (some? (extract-host-from-ast parsed))))) 
 (m/=> valid-domain-or-ip? [:=> [:cat :string] :boolean])

(defn valid-url?
  "Check if string is a valid URL (has protocol)"
  [s]
  (= (parse-tree-node-type s) :URL)) 
 (m/=> valid-url? [:=> [:cat :string] :boolean])