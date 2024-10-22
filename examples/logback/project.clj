(defproject com.example/logging "1.0"
  :dependencies [[org.clojure/clojure            "1.12.0"]
                 [com.kroo/epilogue              "0.6"]
                 ;; Use Logback-classic as the logging backend.
                 [ch.qos.logback/logback-classic "1.5.11"]
                 [com.kroo/typeset.logback       "0.7"]
                 ;; Add SLF4J logging bridges to make logs from other logging libraries go via SLF4J.
                 [org.slf4j/jul-to-slf4j         "2.0.16"]
                 [org.slf4j/jcl-over-slf4j       "2.0.16"]
                 [org.slf4j/log4j-over-slf4j     "2.0.16"]]
  ;; Make clojure.tools.logging use SLF4J.
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"])
