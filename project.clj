(defproject com.kroo/epilogue "0.1"
  :url "https://github.com/b-social/epilogue"
  :description "Simple Clojure logging facade for logging structured data via SLF4J 2."
  :license {:name "MIT"
            :url "https://github.com/b-social/epilogue/blob/master/LICENCE"
            :distribution :repo}
  :pedantic? true
  :dependencies [[org.clojure/clojure "1.11.1" :scope "provided"]
                 [org.slf4j/slf4j-api "2.0.7"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-simple "2.0.7"]]}})
