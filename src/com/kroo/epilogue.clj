(ns com.kroo.epilogue
  "Simple Clojure logging facade for logging structured data via SLF4J 2+."
  (:import [org.slf4j Logger LoggerFactory]
           [org.slf4j.event Level]
           [org.slf4j.spi LoggingEventBuilder NOPLoggingEventBuilder]))

(set! *warn-on-reflection* true)

(def ^:private levels
  "Map of level names to SLF4J Level objects."
  {:error Level/ERROR
   :warn  Level/WARN
   :info  Level/INFO
   :debug Level/DEBUG
   :trace Level/TRACE})

(def ^:dynamic *context*
  "Logging context.  A structured alternative to the [MDC][] that is thread safe
   and nicer to use from Clojure.  (You can still use the MDC if you like.)

   [MDC]: https://logback.qos.ch/manual/mdc.html

   Everything in here in scope of the log statement will be included in the log.
   Try to use fully qualified keywords to avoid naming conflicts with the core
   log data.

   ---

   Why an atom AND a dynamic var?  The dynamic var allows this value to
   differentiate between threads and dynamic scope, while the atom provides safe
   alteration and the ability to set global context values."
  (atom {}))

(defmacro with-context
  "Merge `context` onto the current logging `*context*`, creating a new scope
   around `body`."
  [context & body]
  `(binding [*context* (atom (merge @*context* ~context))]
     ~@body))

(def ^:private nop
  "The singleton NOPLoggingEventBuilder, used for checking if logging is enabled
   at a particular level."
  ^LoggingEventBuilder (NOPLoggingEventBuilder/singleton))

(defn- add-kv
  "Add a key value pair to an SLF4J log event."
  ^LoggingEventBuilder [^LoggingEventBuilder builder k ^Object v]
  (.addKeyValue builder
                (if (keyword? k)
                  (subs (str k) 1)
                  (str k))
                v))

(defn log*
  "Primitive logging function for Epilogue.

   Do not use this function directly!  Use the provided macros instead.
   Backwards compatibility is not guaranteed for this function."
  [^Logger logger level ^String msg data ^Throwable throwable src]
  (let [^LoggingEventBuilder builder (.atLevel logger (levels level))]
    ;; Check the logging level is enabled then continue if so.
    (when-not (identical? builder nop)
      (let [^LoggingEventBuilder builder
            (as-> builder $
              (.setMessage ^LoggingEventBuilder $ msg)
              (add-kv ^LoggingEventBuilder $ ::source src)
              (reduce-kv add-kv $ @*context*)
              (reduce-kv add-kv $ data))]
        (.log ^LoggingEventBuilder
         (if throwable
           (.setCause builder throwable)
           builder))))))

(defmacro log
  ""
  [level msg data & {:keys [throwable logger-ns]}]
  `(let [ns# (or ~logger-ns (str *ns*))]
     (log* (LoggerFactory/getLogger ns#)
           ~level
           ~msg
           ~data
           ~throwable
           (assoc ~(meta &form), :file *file*, :namespace ns#))))

(defmacro ^:private deflevel
  "Construct a convenience macro for a specific logging level."
  [level]
  `(defmacro ~(symbol level)
     ""
     {:arglists '~'([msg data & {:keys [throwable logger-ns]}])}
     [msg# data# & {:as opts#}]
     (with-meta
       `(log ~~level ~msg# ~data# ~(:throwable opts#))
       ~'(meta &form))))

(declare error warn info debug trace)
(deflevel :error)
(deflevel :warn)
(deflevel :info)
(deflevel :debug)
(deflevel :trace)

(defmacro spy [])

(defmacro raise [])
