# Epilogue

Simple Clojure logging facade for logging structured data via [SLF4J][] 2+.

[SLF4J]: https://www.slf4j.org/


## Rationale

Logs are the epilogue of program execution.  They provide us valuable insights
into how our programs really behaved.  Unfortunately, the world of Java logging
is fraught with [complexity and competing solutions][Logging in Clojure].
While Clojure provides us with an excellent facade for these tools in
[clojure.tools.logging][] it unfortunately suffers from a critical limitation.
Logs are strings; no structured data.

While it would be great to use better, simpler logging solutions like
[μ/log][mulog].  Many situations still require full integration with the Java
logging mess.  Is there a half-way point?  Can we add more data to our logs in
a semi-structured way?

This library is a simple Clojure logging facade that wraps SLF4J 2+ (the
version that added structured data support) with an interface similar to that
of `ex-info`.  Epilogue also provides useful additional functionality.

> **Note**<br>
> If tools.logging ever gets structured data support, a mostly backwards
> compatible v2 of this library **may** be published that wraps that instead of
> SLF4J.

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


## Usage

[logback]: https://logback.qos.ch


## Legal

Copyright © 2023 Kroo Bank Ltd.

This library and source code are available under the terms of the MIT licence.
A full copy of the licence file is provided in the `LICENCE` file of the source
code.
