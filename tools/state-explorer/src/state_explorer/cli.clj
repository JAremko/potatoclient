(ns state-explorer.cli
  "Command-line interface for state-explorer"
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as str]))

(def cli-options
  [["-d" "--domain DOMAIN" "WebSocket server domain"
    :default "sych.local"
    :validate [#(not (str/blank? %)) "Domain cannot be empty"]]
   
   ["-c" "--count COUNT" "Number of payloads to capture"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Count must be positive"]]
   
   ["-o" "--output-dir DIR" "Output directory for captured files"
    :default "./output"
    :validate [#(not (str/blank? %)) "Output directory cannot be empty"]]
   
   ["-p" "--port PORT" "WebSocket port (if not using default HTTPS port)"
    :parse-fn #(Integer/parseInt %)
    :validate [#(and (> % 0) (<= % 65535)) "Port must be between 1 and 65535"]]
   
   ["-e" "--endpoint ENDPOINT" "WebSocket endpoint path"
    :default "/ws/ws_state"]
   
   ["-s" "--no-ssl" "Use ws:// instead of wss://"
    :default false]
   
   ["-v" "--verbose" "Enable verbose logging"
    :default false]
   
   ["-h" "--help" "Show this help message"]])

(defn usage [options-summary]
  (str/join
   \newline
   ["State-Explorer - Capture and analyze WebSocket state messages"
    ""
    "Usage: state-explorer [options]"
    ""
    "Options:"
    options-summary
    ""
    "Examples:"
    "  state-explorer                    # Capture 1 payload from sych.local"
    "  state-explorer -c 10              # Capture 10 payloads"
    "  state-explorer -d myserver.com    # Use different domain"
    "  state-explorer -o /tmp/capture    # Save to different directory"
    "  state-explorer --no-ssl           # Use unencrypted WebSocket"
    ""]))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Returns a map with :options, :errors, or :exit-message"
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}
      
      errors
      {:exit-message (error-msg errors)}
      
      :else
      {:options options})))

(defn build-ws-url
  "Build WebSocket URL from options"
  [{:keys [domain port endpoint no-ssl]}]
  (let [protocol (if no-ssl "ws" "wss")
        port-str (when port (str ":" port))]
    (str protocol "://" domain port-str endpoint)))

(defn format-capture-summary
  "Format a summary of the capture session"
  [captures stats]
  (str/join
   \newline
   [(str "=" (apply str (repeat 50 "=")))
    "Capture Summary"
    (str "=" (apply str (repeat 50 "=")))
    (format "Total payloads captured: %d" (:total-count stats))
    (format "Total data size: %d bytes" (:total-size stats))
    (format "Average payload size: %.2f bytes" (double (:avg-size stats)))
    (when (:duration-ms stats)
      (format "Capture duration: %.2f seconds" (/ (:duration-ms stats) 1000.0)))
    ""
    "Files saved:"
    (str/join \newline
              (map (fn [c]
                     (format "  %s%s"
                             (.getName (:binary-file c))
                             (if (:edn-file c)
                               (str " + " (.getName (:edn-file c)))
                               " (EDN conversion failed)")))
                   captures))
    (str "=" (apply str (repeat 50 "=")))]))

(defn print-progress
  "Print progress indicator"
  [current total]
  (let [percentage (int (* 100 (/ current total)))
        bar-width 30
        filled (int (* bar-width (/ current total)))
        empty (- bar-width filled)]
    (print (str "\r["
                (apply str (repeat filled "="))
                (apply str (repeat empty " "))
                "] "
                percentage "% "
                (format "(%d/%d)" current total)))
    (flush)))