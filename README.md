# Epilogue

Simple Clojure logging facade for logging structured data via [SLF4J][] 2+.

[SLF4J]: https://www.slf4j.org/


## Rationale

Logs are the epilogue of program execution.  They provide us valuable insights into how our programs really behaved.  While the world of Java logging is fraught with [complexity and competing solutions][Logging in Clojure], Clojure provides us with an excellent facade for these tools in [clojure.tools.logging][].  Unfortunately though, suffers from a critical limitation.  Logs are strings; no structured data.

While it would be great to use simpler logging solutions like [μ/log][mulog].  Many situations still require full integration with the Java logging mess.  Is there a half-way point?

The recent 2.0.0 release of SLF4J, the de-facto logging facade for Java, added
support for logging data!

This library is a simple Clojure logging facade that wraps SLF4J 2+ (the version that added structured data support) with an interface similar to that of `ex-info`.  Epilogue also provides useful additional functionality.

[Logging in Clojure]: https://lambdaisland.com/blog/2020-06-12-logging-in-clojure-making-sense-of-the-mess
[clojure.tools.logging]: https://github.com/clojure/tools.logging
[mulog]: https://github.com/BrunoBonacci/mulog


## Installation

```clojure
;; tools.deps
com.kroo/epilogue {:mvn/version "0.1"}
;; Leiningen
[com.kroo/epilogue "0.1"]
```

Before you can use Epilogue, you will probably want to first configure all logs to go through SLF4J and then configure a logging backend.  You can find an example Clojure project using Epilogue with [Logback][] in the "[examples](/examples/logback)" folder of this repository, which demonstrates how to set it up.

[Logback]: https://logback.qos.ch


## Usage

Once the tricky part of setting up logging is over, Epilogue is super easy to use!

```clojure
(require '[com.kroo.epilogue :as log])

;; Log at any logging level supported by SLF4J.
(log/error "This will log at ERROR level." {:some "structured data", :to-add-to "the log"})
(log/warn "This will log at WARN level." {:some-other "data"})
(log/info "This will log at INFO level." {:some {:hi [4 5 6]})
(log/debug "This will log at DEBUG level." {:foo "bar", :biz [1 2 3]})
(log/trace "This will log at TRACE level." {:foo "bar"})

;; Maybe log with a throwable as the cause.  (Works on all logging levels.)
(try
  (throw (ex-info "Something broke" {:hello "world"}))
  (catch Exception ex
    (log/error "This will log at ERROR level with a \"cause\""
               {:some "data", :foo "bar"}
               :throwable ex)))

;; You can also use keywords as the log message!
(log/info ::account-created {:email-address "john.doe@example.com"})

;; Need to override the logger namespace?  No problem!
(log/info ::different-namespace? {:foo "bar"}
          :logger-ns "this.is.another-namespace")

;; You can pass SLF4J "markers" to logs.  Either pass an `org.slf4j.Marker`
;; instance, a string or a keyword.
(log/error "Something has gone really wrong!" {:id 123}
           :markers [:alert/critical])
```


## Legal

Copyright © 2023 Kroo Bank Ltd.

This library and source code are available under the terms of the MIT licence.  A full copy of the licence file is provided in the `LICENCE` file of the source code.
