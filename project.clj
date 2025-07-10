(defproject potatoclient "1.1.1"
  :description "Potato Client - Video Stream Control Center with Protocol Buffer Support"
  :license {:name "LGPL-3.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [seesaw "1.5.0"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/core.async "1.6.681"]
                 ;; Java dependencies for the stream handler
                 [com.fasterxml.jackson.core/jackson-databind "2.15.2"]
                 [org.java-websocket/Java-WebSocket "1.5.4"]
                 [org.slf4j/slf4j-nop "2.0.9" :exclusions [org.slf4j/slf4j-api]]
                 ;; GStreamer for video playback
                 [org.freedesktop.gstreamer/gst1-java-core "1.4.0" :exclusions [net.java.dev.jna/jna]]
                 ;; Add specific JNA version to avoid version range warning
                 [net.java.dev.jna/jna "5.13.0"]
                 ;; Protocol Buffer dependencies
                 [com.google.protobuf/protobuf-java "3.25.1"]
                 [com.appsflyer/pronto "3.0.0"]]
  
  :source-paths ["src"]
  :java-source-paths ["src/java"]
  :resource-paths ["resources"]
  
  :main ^:skip-aot video-control-center
  
  :jvm-opts ["-Djna.library.path=/usr/lib"
             "-Dgstreamer.library.path=/usr/lib"
             "-Dgstreamer.plugin.path=/usr/lib/gstreamer-1.0"
             "--enable-native-access=ALL-UNNAMED"]
  
  :javac-options ["--release" "17"]
  
  :profiles {:uberjar {:aot :all
                       :pedantic? :abort
                       :jvm-opts ["-Dclojure.compiler.elide-meta=[:doc :file :line :added]"
                                  "-Dclojure.compiler.direct-linking=true"]
                       :global-vars {*warn-on-reflection* false
                                     *assert* false}
                       :uberjar-name "potatoclient.jar"}
             :dev {:source-paths ["dev"]
                   :global-vars {*warn-on-reflection* true
                                 *assert* true}
                   :plugins [[com.appsflyer/lein-protodeps "1.0.5"]]}}
  
  :lein-protodeps {:output-path "src/java"
                   :proto-version "3.25.1"
                   :compile-grpc? false
                   :repos {:local {:repo-type :filesystem
                                   :config {:path "."}
                                   :proto-paths ["proto"]
                                   :dependencies []}}}
  
)
