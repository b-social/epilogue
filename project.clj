(defproject com.kroo/epilogue "0.1"
  :url "https://github.com/b-social/epilogue"
  :description "Simple Clojure logging facade for logging structured data via SLF4J 2+."
  :license {:name "MIT"
            :url "https://github.com/b-social/epilogue/blob/master/LICENCE"
            :distribution :repo}
  :pedantic? true
  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]
  :global-vars {*warn-on-reflection* true
                *assert*             false}
  :dependencies [[org.clojure/clojure "1.11.1" :scope "provided"]
                 [org.slf4j/slf4j-api "2.0.7"]]
  :plugins [[com.github.clj-kondo/lein-clj-kondo "RELEASE"]
            [com.github.liquidz/antq "RELEASE"]
            [dev.weavejester/lein-cljfmt "RELEASE"]]
  :profiles {:dev {:dependencies [[org.slf4j/slf4j-simple "2.0.7"]]}}
  :aliases {"lint" ["clj-kondo" "--lint" "src"]
            "fmt"  ["cljfmt" "fix"]
            "antq" ["with-profile" "+dev" "antq"]})
