{:reporter [kaocha.report/documentation]
 :plugins [:kaocha.plugin/filter]
 :tests [{:id :unit
          :test-paths ["test"]
          :source-paths ["src"]}]}