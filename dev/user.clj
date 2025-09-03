(ns user
  "Entry point that loads the appropriate development namespace.
   For NREPL: loads repl.clj with REPL utilities
   For make dev: does nothing (dev.clj handles initialization)")

;; Only load REPL utilities when running in NREPL mode
(when (or (System/getProperty "nrepl.load")
          (some? (resolve 'nrepl.server/start-server))
          (some? (resolve 'cider.nrepl/start-nrepl)))
  (require 'repl))