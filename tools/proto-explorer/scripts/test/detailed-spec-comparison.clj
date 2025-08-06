(ns detailed-spec-comparison
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn parse-proto-messages
  "Extract message definitions from proto file content."
  [proto-content]
  (let [;; Remove comments
        content (str/replace proto-content #"//.*$" "")
        ;; Find all message definitions
        message-pattern #"message\s+(\w+)\s*\{([^{}]*(?:\{[^{}]*\}[^{}]*)*)\}"
        messages (re-seq message-pattern content)]
    (reduce (fn [acc [_ msg-name msg-body]]
              (let [;; Extract fields
                    field-pattern #"(?:optional\s+|repeated\s+)?(\w+(?:\.\w+)*)\s+(\w+)\s*=\s*(\d+)"
                    fields (re-seq field-pattern msg-body)
                    ;; Extract oneofs
                    oneof-pattern #"oneof\s+(\w+)\s*\{([^}]+)\}"
                    oneofs (re-seq oneof-pattern msg-body)]
                (assoc acc msg-name
                       {:fields (map (fn [[_ type-name field-name _]]
                                      {:name field-name
                                       :type type-name})
                                    fields)
                        :oneofs (map (fn [[_ oneof-name oneof-body]]
                                      {:name oneof-name
                                       :fields (map (fn [[_ type-name field-name _]]
                                                     {:name field-name
                                                      :type type-name})
                                                   (re-seq field-pattern oneof-body))})
                                    oneofs)})))
            {}
            messages)))

(defn parse-spec-file
  "Parse a generated spec file and extract message definitions."
  [spec-file]
  (let [content (slurp spec-file)
        ;; Extract namespace
        ns-match (re-find #"^\(ns\s+([\w.]+)" content)
        namespace (when ns-match (second ns-match))
        ;; Extract def forms - improved regex
        def-pattern #"\(def\s+(\S+)\s+\"[^\"]*\"\s+((?:\[|\(|\{)[\s\S]*?)(?=\n\(def|\n\z)"
        defs (re-seq def-pattern content)]
    {:namespace namespace
     :specs (reduce (fn [acc [_ spec-name spec-form]]
                     (try
                       (let [spec (edn/read-string spec-form)]
                         (assoc acc spec-name spec))
                       (catch Exception e
                         (println "Error parsing spec" spec-name ":" (.getMessage e))
                         acc)))
                   {}
                   defs)}))

(defn extract-spec-fields
  "Extract field information from a Malli spec."
  [spec]
  (when (and (vector? spec) (= :map (first spec)))
    (map (fn [[field-key field-spec]]
           {:name (name field-key)
            :proto-name (str/replace (name field-key) #"-" "_")
            :type (cond
                    (keyword? field-spec) field-spec
                    (and (vector? field-spec) (= :maybe (first field-spec)))
                    (second field-spec)
                    (vector? field-spec) (first field-spec)
                    :else :unknown)})
         (rest spec))))

(defn compare-message
  "Compare a proto message with its spec."
  [proto-msg spec msg-name]
  (let [proto-fields (:fields proto-msg)
        spec-fields (extract-spec-fields spec)
        proto-field-names (set (map :name proto-fields))
        spec-field-names (set (map :proto-name spec-fields))]
    
    {:message msg-name
     :proto-fields (count proto-fields)
     :spec-fields (count spec-fields)
     :missing-in-spec (str/join ", " (sort (clojure.set/difference proto-field-names spec-field-names)))
     :extra-in-spec (str/join ", " (sort (clojure.set/difference spec-field-names proto-field-names)))
     :oneofs-in-proto (count (:oneofs proto-msg))
     :match? (and (= proto-field-names spec-field-names)
                 (= (count proto-fields) (count spec-fields)))}))

(defn compare-proto-with-spec
  "Compare a proto file with its corresponding spec file."
  [proto-file spec-file]
  (println "\n" (str/join "" (repeat 60 "=")) "")
  (println "COMPARING:" (.getName proto-file))
  (println (str/join "" (repeat 60 "=")) "")
  
  (if (and (.exists proto-file) (.exists spec-file))
    (let [proto-content (slurp proto-file)
          proto-messages (parse-proto-messages proto-content)
          spec-data (parse-spec-file spec-file)
          spec-namespace (:namespace spec-data)
          specs (:specs spec-data)]
      
      (println "\nProto file:" (.getPath proto-file))
      (println "Spec file:" (.getPath spec-file))
      (println "Spec namespace:" spec-namespace)
      (println "\nMessages found in proto:" (count proto-messages))
      (println "Specs found in file:" (count specs))
      
      ;; Compare each message
      (doseq [[msg-name proto-msg] proto-messages]
        (let [;; Try different naming conventions
              possible-spec-names [(str/replace msg-name #"_" "-")
                                  msg-name
                                  (str/replace msg-name #"([a-z])([A-Z])" "$1-$2")]
              spec-entry (first (filter (fn [name] (get specs name)) possible-spec-names))
              spec (get specs spec-entry)]
          
          (if spec
            (let [comparison (compare-message proto-msg spec msg-name)]
              (println (str "\n--- " msg-name " ---"))
              (println "  Proto fields:" (:proto-fields comparison))
              (println "  Spec fields:" (:spec-fields comparison))
              (when (seq (:missing-in-spec comparison))
                (println "  ⚠️  Missing in spec:" (:missing-in-spec comparison)))
              (when (seq (:extra-in-spec comparison))
                (println "  ⚠️  Extra in spec:" (:extra-in-spec comparison)))
              (when (pos? (:oneofs-in-proto comparison))
                (println "  Oneofs:" (:oneofs-in-proto comparison)))
              (println "  ✓ Match:" (if (:match? comparison) "YES" "NO")))
            (println (str "\n❌ No spec found for message: " msg-name))))))
    
    (println "\n⚠️  Files not found:"
            (when-not (.exists proto-file) (str "\n  Proto: " (.getPath proto-file)))
            (when-not (.exists spec-file) (str "\n  Spec: " (.getPath spec-file))))))

(defn find-corresponding-spec-file
  "Find the spec file for a given proto file name."
  [proto-name spec-dir]
  (let [;; Extract package/module name from proto filename
        base-name (str/replace proto-name #"\.proto$" "")
        ;; Map proto names to spec file names
        spec-mappings {"jon_shared_cmd" "cmd-specs.clj"
                      "jon_shared_data" "ser-specs.clj"
                      "jon_shared_data_types" "ser-specs.clj"
                      "jon_shared_cmd_rotary" "cmd.RotaryPlatform-specs.clj"
                      "jon_shared_cmd_heat_camera" "cmd.HeatCamera-specs.clj"
                      "jon_shared_cmd_day_camera" "cmd.DayCamera-specs.clj"
                      "jon_shared_cmd_compass" "cmd.Compass-specs.clj"
                      "jon_shared_cmd_cv" "cmd.CV-specs.clj"
                      "jon_shared_cmd_gps" "cmd.Gps-specs.clj"
                      "jon_shared_cmd_lrf" "cmd.Lrf-specs.clj"
                      "jon_shared_cmd_lira" "cmd.Lira-specs.clj"
                      "jon_shared_cmd_osd" "cmd.OSD-specs.clj"
                      "jon_shared_cmd_system" "cmd.System-specs.clj"
                      "jon_shared_cmd_lrf_align" "cmd.Lrf-calib-specs.clj"
                      "jon_shared_cmd_day_cam_glass_heater" "cmd.DayCamGlassHeater-specs.clj"}
        spec-name (get spec-mappings base-name)]
    (when spec-name
      (io/file spec-dir spec-name))))

(defn check-all-protos
  "Check all proto files against their generated specs."
  []
  (let [proto-dir (io/file "proto")
        spec-dir (io/file "../../shared/specs/protobuf")
        proto-files (filter #(str/ends-with? (.getName %) ".proto")
                           (.listFiles proto-dir))]
    
    (println "Proto Explorer - Spec Accuracy Check")
    (println "====================================")
    (println "Proto directory:" (.getPath proto-dir))
    (println "Spec directory:" (.getPath spec-dir))
    (println "Found" (count proto-files) "proto files")
    
    ;; Check a subset of important proto files
    (let [important-protos ["jon_shared_cmd.proto"
                           "jon_shared_cmd_rotary.proto"
                           "jon_shared_cmd_heat_camera.proto"
                           "jon_shared_cmd_day_camera.proto"
                           "jon_shared_data.proto"
                           "jon_shared_data_types.proto"]]
      
      (doseq [proto-name important-protos]
        (let [proto-file (io/file proto-dir proto-name)
              spec-file (find-corresponding-spec-file proto-name spec-dir)]
          (if (and proto-file spec-file)
            (compare-proto-with-spec proto-file spec-file)
            (println "\n❌ Cannot find files for:" proto-name)))))))

;; Run the check
(check-all-protos)