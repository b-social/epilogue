(defproject com.kroo/epilogue "0.1"
  :url "https://github.com/b-social/epilogue"
  :description "Simple Clojure logging facade for logging structured data via SLF4J 2."
  :pedantic? true
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.slf4j/slf4j-api "2.0.7"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-simple "2.0.7"]]}})
