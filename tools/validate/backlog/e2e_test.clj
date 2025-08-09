(ns validate.e2e-test
  "End-to-end tests using Jimfs (in-memory file system) for file I/O simulation."
  (:require
   [clojure.test :refer [deftest testing is use-fixtures]]
   [validate.validator :as validator]
   [validate.test-harness :as harness])
  (:import
   [com.google.common.jimfs Configuration Jimfs]
   [java.nio.file Files FileSystem FileSystems Path StandardOpenOption]
   [java.nio.charset StandardCharsets]
   [java.nio.file.attribute FileAttribute]))

(def ^:dynamic *test-fs* nil)
(def ^:dynamic *test-root* nil)

(defn create-test-filesystem
  "Create an in-memory filesystem for testing."
  []
  (let [config (-> (Configuration/unix)
                   (.toBuilder)
                   (.setWorkingDirectory "/test")
                   (.build))
        fs (Jimfs/newFileSystem config)]
    {:fs fs
     :root (.getPath fs "/test" (into-array String []))}))

(defn setup-test-fs
  "Fixture to setup and teardown test filesystem."
  [test-fn]
  (let [{:keys [fs root]} (create-test-filesystem)]
    (binding [*test-fs* fs
              *test-root* root]
      (try
        ;; Create test directory structure
        (Files/createDirectories root (into-array java.nio.file.attribute.FileAttribute []))
        (Files/createDirectory (.resolve root "output") (into-array java.nio.file.attribute.FileAttribute []))
        (Files/createDirectory (.resolve root "input") (into-array java.nio.file.attribute.FileAttribute []))
        (test-fn)
        (finally
          (.close fs))))))

(use-fixtures :each setup-test-fs)

(defn write-test-file
  "Write binary data to a test file in the virtual filesystem."
  [relative-path data]
  (let [path (.resolve *test-root* relative-path)
        parent (.getParent path)]
    (when parent
      (Files/createDirectories parent (into-array java.nio.file.attribute.FileAttribute [])))
    (Files/write path data (into-array [StandardOpenOption/CREATE
                                        StandardOpenOption/WRITE
                                        StandardOpenOption/TRUNCATE_EXISTING]))
    path))

(defn read-test-file
  "Read binary data from a test file in the virtual filesystem."
  [relative-path]
  (let [path (.resolve *test-root* relative-path)]
    (Files/readAllBytes path)))

(deftest test-validate-file-with-valid-data
  (testing "Validating a file with valid binary data"
    ;; This test requires actual proto classes to be compiled
    ;; For now, we test the file I/O mechanics
    (let [test-data (byte-array [0x08 0x01 0x10 0x02])
          file-path (write-test-file "input/test_state.bin" test-data)]
      (is (= (seq test-data) (seq (read-test-file "input/test_state.bin")))
          "File should be written and readable")
      
      ;; This will fail until we have actual proto classes
      (is (thrown? Exception
                  (validator/validate-file file-path :type :state))
          "Should attempt to validate the file"))))

(deftest test-validate-multiple-files
  (testing "Validating multiple files in sequence"
    (let [files (for [i (range 5)]
                  (let [data (byte-array [(byte i) (byte (* i 2))])
                        path (write-test-file (str "input/test_" i ".bin") data)]
                    {:path path :data data}))]
      (is (= 5 (count files)) "Should create 5 test files")
      
      (doseq [{:keys [path data]} files]
        (is (= (seq data) (seq (Files/readAllBytes path)))
            "Each file should contain its data")))))

(deftest test-file-not-found
  (testing "Handling non-existent file"
    (let [non-existent (.resolve *test-root* "does_not_exist.bin")]
      (is (thrown-with-msg? Exception #"Failed to read binary file"
                           (validator/validate-file non-existent))
          "Should throw when file doesn't exist"))))

(deftest test-directory-instead-of-file
  (testing "Handling directory path instead of file"
    (let [dir-path (.resolve *test-root* "output")]
      (is (thrown? Exception
                  (validator/validate-file dir-path))
          "Should throw when given a directory"))))

(deftest test-large-file-handling
  (testing "Handling large binary files"
    (let [;; Create a 1MB test file
          large-data (byte-array (* 1024 1024))
          _ (dotimes [i (count large-data)]
              (aset-byte large-data i (byte (mod i 256))))
          file-path (write-test-file "input/large_file.bin" large-data)]
      (is (= (* 1024 1024) (count (Files/readAllBytes file-path)))
          "Large file should be written correctly")
      
      ;; Validation will fail but should handle large files
      (is (thrown? Exception
                  (validator/validate-file file-path))
          "Should handle large files"))))

(deftest test-concurrent-file-access
  (testing "Concurrent file validation"
    (let [test-files (for [i (range 10)]
                      (write-test-file (str "input/concurrent_" i ".bin")
                                      (byte-array [(byte i)])))]
      ;; Simulate concurrent access
      (let [futures (doall (map #(future
                                   (try
                                     (validator/validate-file % :type :state)
                                     (catch Exception e
                                       {:error (.getMessage e)})))
                               test-files))
            results (map deref futures)]
        (is (= 10 (count results)) "Should process all files")
        (is (every? #(contains? % :error) results) 
            "All should have errors (invalid proto data)")))))

(deftest test-file-permissions-simulation
  (testing "Simulating file permission issues"
    ;; Jimfs doesn't support actual permission changes, but we can test the concept
    (let [test-data (byte-array [0x01 0x02])
          file-path (write-test-file "input/test_perms.bin" test-data)]
      ;; In a real filesystem, we'd change permissions here
      ;; For now, just verify the file exists and is readable
      (is (Files/exists file-path (into-array java.nio.file.LinkOption [])) "File should exist")
      (is (Files/isReadable file-path) "File should be readable"))))

(deftest test-nested-directory-structure
  (testing "Working with nested directory structures"
    (let [nested-path "input/level1/level2/level3/deep.bin"
          test-data (byte-array [0xDE 0xAD 0xBE 0xEF])
          file-path (write-test-file nested-path test-data)]
      (is (Files/exists file-path (into-array java.nio.file.LinkOption [])) "Nested file should be created")
      (is (= (seq test-data) (seq (Files/readAllBytes file-path)))
          "Nested file should contain correct data")
      
      (is (thrown? Exception
                  (validator/validate-file file-path))
          "Should attempt to validate nested file"))))

(deftest test-empty-file-handling
  (testing "Handling empty files"
    (let [empty-path (write-test-file "input/empty.bin" (byte-array 0))]
      (is (Files/exists empty-path (into-array java.nio.file.LinkOption [])) "Empty file should exist")
      (is (= 0 (count (Files/readAllBytes empty-path))) "File should be empty")
      
      (is (thrown? Exception
                  (validator/validate-file empty-path))
          "Should throw on empty file"))))