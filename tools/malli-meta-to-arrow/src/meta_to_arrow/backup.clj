(ns meta-to-arrow.backup
  "Backup functionality for safe migrations."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn timestamp []
  "Generate timestamp string for backup directories."
  (.format (java.time.LocalDateTime/now)
           (java.time.format.DateTimeFormatter/ofPattern "yyyyMMdd-HHmmss")))

(defn backup-file
  "Create backup of a single file."
  [file-path]
  (let [file (io/file file-path)
        parent-dir (.getParent file)
        file-name (.getName file)
        backup-name (str file-name ".backup-" (timestamp))
        backup-path (str parent-dir "/" backup-name)]
    (io/copy file (io/file backup-path))
    {:original file-path
     :backup-path backup-path}))

(defn backup-directory
  "Create backup of entire directory structure."
  [dir-path]
  (let [backup-dir (str dir-path ".backup-" (timestamp))
        files-to-backup (->> (file-seq (io/file dir-path))
                             (filter #(.isFile %))
                             (filter #(re-matches #".*\.clj[c]?$" (.getName %))))]
    
    ;; Create backup directory structure
    (doseq [file files-to-backup]
      (let [rel-path (str/replace (.getPath file) (str dir-path "/") "")
            backup-path (str backup-dir "/" rel-path)]
        (io/make-parents backup-path)
        (io/copy file (io/file backup-path))))
    
    {:original dir-path
     :backup-path backup-dir
     :files-count (count files-to-backup)}))

(defn restore-backup
  "Restore from a backup (file or directory)."
  [backup-path original-path]
  (let [backup (io/file backup-path)
        original (io/file original-path)]
    (cond
      ;; Restore single file
      (.isFile backup)
      (do (io/copy backup original)
          {:status :success
           :type :file
           :restored original-path})
      
      ;; Restore directory
      (.isDirectory backup)
      (let [files (->> (file-seq backup)
                       (filter #(.isFile %)))]
        (doseq [file files]
          (let [rel-path (str/replace (.getPath file) 
                                       (str backup-path "/") "")
                target-path (str original-path "/" rel-path)]
            (io/make-parents target-path)
            (io/copy file (io/file target-path))))
        {:status :success
         :type :directory
         :restored original-path
         :files-count (count files)})
      
      :else
      {:status :error
       :message "Backup path does not exist"})))