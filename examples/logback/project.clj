(defproject com.example/logging "1.0"
  :dependencies [[org.clojure/clojure            "1.11.1"]
                 [com.kroo/epilogue              "0.2"]
                 ;; Use Logback-classic as the logging backend.
                 [ch.qos.logback/logback-classic "1.4.8"]
                 [com.kroo/typeset.logback       "0.2"]
                 ;; Add SLF4J logging bridges to make logs from other logging libraries go via SLF4J.
                 [org.slf4j/jul-to-slf4j         "2.0.7"]
                 [org.slf4j/jcl-over-slf4j       "2.0.7"]
                 [org.slf4j/log4j-over-slf4j     "2.0.7"]]
  ;; Make clojure.tools.logging use SLF4J.
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"])
