(ns com.kroo.epilogue
  "Simple Clojure logging facade for logging structured data via SLF4J 2+."
  (:import (org.slf4j Logger LoggerFactory)
           (org.slf4j.event Level)
           (org.slf4j.spi LoggingEventBuilder NOPLoggingEventBuilder)))

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

(defn- ->str
  "Cast `s` to a string while removing the leading `:` if `s` was a keyword."
  ^String [s]
  (if (keyword? s)
    (subs (str s) 1)
    (str s)))

(defn- add-kv
  "Add a key value pair to an SLF4J log event."
  ^LoggingEventBuilder [^LoggingEventBuilder builder k ^Object v]
  (.addKeyValue builder (->str k) v))

(defn log*
  "Primitive logging function for Epilogue.

   Do not use this function directly!  Use the provided macros instead.
   Backwards compatibility is not guaranteed for this function."
  [^Logger logger level msg data ^Throwable throwable src]
  (let [^LoggingEventBuilder builder (.atLevel logger (levels level))]
    ;; Check the logging level is enabled and continue if so.
    (when-not (identical? builder nop)
      (as-> builder $
        (.setMessage $ (->str msg))
        (add-kv $ ::source src)
        (reduce-kv add-kv $ @*context*)
        (reduce-kv add-kv $ data)
        (cond-> $
          throwable (as-> $$ (.setCause ^LoggingEventBuilder $$ throwable)))
        (.log ^LoggingEventBuilder $)))))

(defmacro log
  "Logs a message (`msg`) and `data` at the specified logging `level`.  The log
   will also include anything in `*context*` within the current dynamic scope.

   `data` can be anything that implements the `clojure.core.protocols/IKVReduce`
   protocol, but it is recommended to log only maps to avoid confusion.

   Options:
     - a `throwable` object to set as the \"cause\", and
     - an override logger namespace (`logger-ns`)."
  [level msg data & {:keys [throwable logger-ns]}]
  `(let [ns#  (str *ns*)
         lns# (if-let [l# ~logger-ns] (str l#) ns#)]
     (log* (LoggerFactory/getLogger ^String lns#)
           ~level
           ~msg
           ~data
           ~throwable
           (assoc ~(meta &form), :file *file*, :namespace ns#))))

(defn- single-arity?
  "Returns the index of the final body value if it looks like a single arity
   macro definition, else returns `nil`."
  [forms]
  (when-let [[idx] (keep-indexed #(when (vector? %2) %1) forms)]
    (let [last-form-idx (dec (count forms))]
      (when (not= idx last-form-idx)
        last-form-idx))))

(defn- multiple-arity?
  "Returns the indicies of the body values."
  [forms]
  (let [idxs (keep-indexed
               #(when (and (list? %2)
                           (vector? (first %2))
                           (second %2))
                  [%1 (dec (count %2))])
               forms)]
    (when (seq idxs) idxs)))

(defn- persist-form-meta [body]
  `(with-meta ~body (meta ~'&form)))

(defmacro defloggingmacro
  "Defines a macro that persists the original line number and column making it
   suitable for logging.  Otherwise behaves identically to `defmacro`."
  {:arglists (:arglists (meta #'defmacro))}
  [& rst]
  (cons
    `defmacro
    (let [rst (vec rst)]
      (if-let [idx (single-arity? rst)]
        (update rst idx persist-form-meta)
        (if-let [idxs (multiple-arity? rst)]
          (mapv #(update-in rst % persist-form-meta) idxs)
          rst)))))

(defmacro ^:private deflevel
  "Construct a convenience macro for a specific logging level."
  [level]
  `(defloggingmacro ~(symbol level)
     {:doc (str "Log a message (`msg`) and `data` at the `"
                ~level
                "` logging level.\n\n  "
                "See `com.kroo.epilogue/log` for more details.")
      :arglists '~'([msg data & {:keys [throwable logger-ns]}])}
     [msg# data# & {:as opts#}]
     `(log ~~level ~msg# ~data# opts#)))

;; Generate convenience macros for the supported logging levels.
(declare error warn info debug trace)
(deflevel :error)
(deflevel :warn)
(deflevel :info)
(deflevel :debug)
(deflevel :trace)

;; TODO
;; (declare spy)
;; (defloggingmacro spy
;;   "Log then return `data`.  Logs at `:debug` level by default."
;;   ;; Should `msg` be an opt?
;;   [msg data & {:as opts}])

;; TODO
;; (declare raise)
;; (defloggingmacro raise
;;   "Log an error then throw."
;;   [msg data & {:as opts}]
;;   `(error ~msg ~data))
