#!/usr/bin/env clojure

;; Quick test script to verify WebSocket connection

(require '[clojure.java.io :as io])

(println "Testing WebSocket connection to sych.local...")
(println "Checking if server is reachable...")

(try
  (let [url (java.net.URL. "https://sych.local")
        conn (.openConnection url)]
    (.setConnectTimeout conn 5000)
    (.connect conn)
    (println "✓ Server is reachable via HTTPS"))
  (catch Exception e
    (println "✗ Cannot reach server:" (.getMessage e))))

(println "\nTo capture state payloads:")
(println "1. Make sure protobuf classes are built: make build")
(println "2. Run the capture: make run")
(println "\nNote: The tool will trust all SSL certificates for development.")