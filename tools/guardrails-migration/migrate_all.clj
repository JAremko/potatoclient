(require '[guardrails-migration.zipper-clean :as m])
(require '[clojure.java.io :as io])

(def files-to-migrate
  ["../../src/potatoclient/ui/control_panel.clj"
   "../../src/potatoclient/ui/menu_bar.clj"
   "../../src/potatoclient/ui/utils.clj"
   "../../src/potatoclient/ui/log_viewer.clj"
   "../../src/potatoclient/reports.clj"
   "../../src/potatoclient/url_parser.clj"
   "../../src/potatoclient/state.clj"
   "../../src/potatoclient/theme.clj"
   "../../src/potatoclient/main.clj"
   "../../src/potatoclient/main_frame.clj"
   "../../src/potatoclient/runtime.clj"
   "../../src/potatoclient/logging.clj"
   "../../src/potatoclient/config.clj"
   "../../src/potatoclient/startup_dialog.clj"
   "../../src/potatoclient/dev.clj"])

(doseq [file files-to-migrate]
  (when (.exists (io/file file))
    (print (str "Migrating: " file " ... "))
    (flush)
    (try
      (m/migrate-file file file)
      (println "SUCCESS")
      (catch Exception e
        (println (str "FAILED: " (.getMessage e)))))))