(ns com.kroo.epilogue
  "Simple Clojure logging facade for logging structured data via SLF4J 2+."
  (:import (org.slf4j Logger LoggerFactory Marker MarkerFactory)
           (org.slf4j.event Level)
           (org.slf4j.spi LoggingEventBuilder NOPLoggingEventBuilder)))

(def ^:private levels
  "Map of level names to SLF4J Level objects."
  {:error Level/ERROR
   :warn  Level/WARN
   :info  Level/INFO
   :debug Level/DEBUG
   :trace Level/TRACE})

(def level?
  "Hashset of accepted levels."
  (set (keys levels)))

(def ^:dynamic *context*
  "Logging context.  A structured alternative to the [MDC][] that supports
  default values and nested contexts.  (You can still use the MDC if you like.)

  [MDC]: https://logback.qos.ch/manual/mdc.html

  Everything in here in scope of the log statement will be included in the log.
  Try to use fully qualified keywords to avoid naming conflicts with the core
  log data."
  {})

(defmacro with-context
  "Merge `extra-context` onto the current logging `*context*`, creating a new
  scope around `body`."
  [extra-context & body]
  `(binding [*context* (into *context* ~extra-context)]
     ~@body))

(defn set-default-context!
  "Set the default value of the logging context."
  [value]
  {:pre [(map? value)]}
  (alter-var-root #'*context* (fn [_] value)))

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

(defn- add-marker
  "Add a marker to an SLF4J log event.  If `marker` is a string or keyword, it
  will build a marker."
  ^LoggingEventBuilder [^LoggingEventBuilder builder marker]
  (.addMarker
   builder
   ^Marker (if (instance? Marker marker)
             marker
             (MarkerFactory/getMarker (->str marker)))))

(defn log*
  "Primitive logging function for Epilogue.

  Do not use this function directly!  Use the provided macros instead.
  Backwards compatibility is not guaranteed for this function."
  [level msg data ^Throwable cause markers logger-ns src]
  (let [^String logger-ns (if logger-ns (str logger-ns) (:namespace src))
        ^Logger logger    (LoggerFactory/getLogger logger-ns)
        ^LoggingEventBuilder builder (.atLevel logger (levels level))]
    ;; Check the logging level is enabled and continue if so.
    (when-not (identical? builder nop)
      (as-> builder $
        (.setMessage $ (->str msg))
        (add-kv $ "logger.source" src)
        (reduce-kv add-kv $ *context*)
        (reduce-kv add-kv $ data)
        (reduce add-marker $ markers)
        (cond-> $
          cause (as-> $$ (.setCause ^LoggingEventBuilder $$ cause)))
        (.log ^LoggingEventBuilder $)))))

(defmacro log
  "Logs a message and accompanying data at the specified logging level.

  The log will include anything in `com.kroo.epilogue/*context*` within the
  current dynamic scope.

  `data` can be anything that implements the `clojure.core.protocols/IKVReduce`
  protocol, but it is recommended to log only maps to avoid confusion.

  Options:
    - a throwable object as the `cause`,
    - a sequence of SLF4J `markers` (or strings/keywords), and
    - an override logger namespace (`logger-ns`)."
  [level msg data & {:keys [cause markers logger-ns]}]
  (let [src (-> (meta &form)
                (update :file #(or % (str *file*)))
                (update :namespace #(or % (str *ns*))))]
    `(log* ~level ~msg ~data ~cause ~markers ~logger-ns ~src)))

(defn- single-arity?
  "Returns the index of the final body value if it looks like a single-arity
  macro definition, else returns `nil`."
  [forms]
  (let [last-form-idx (dec (count forms))
        [idx]         (keep-indexed #(when (vector? %2) %1) forms)]
    (when (and idx (not= idx last-form-idx))
      last-form-idx)))

(defn- multi-arity?
  "Returns a list of index paths to the final body values in a multi-arity
  macro definition, else returns `nil`."
  [forms]
  (let [idxs (keep-indexed
              #(when (and (list? %2)
                          (vector? (first %2))
                          (second %2))
                 [%1 (dec (count %2))])
              forms)]
    (when (seq idxs) idxs)))

(defn- preserve-form-meta [body]
  `(with-meta ~body
     (assoc (meta ~'&form) :file (str *file*), :namespace (str *ns*))))

(defmacro defloggingmacro
  "Defines a macro that preserves the original line number and column making it
  suitable for logging.  Otherwise behaves identically to `defmacro`."
  {:arglists (:arglists (meta #'defmacro))}
  [& rst]
  (cons
   `defmacro
   (let [rst (vec rst)]
     (if-let [idx (single-arity? rst)]
       (update rst idx preserve-form-meta)
       (if-let [idxs (multi-arity? rst)]
         (reduce
          (fn [acc idx-path]
            (-> acc
                (update (first idx-path) vec)
                (update-in idx-path preserve-form-meta)
                (update (first idx-path) (partial apply list))))
          rst
          idxs)
         rst)))))

(defmacro ^:private deflevel
  "Construct a convenience macro for a specific logging level."
  [level]
  `(defloggingmacro ~level
     {:doc (str "Log a message and accompanying data at the `" ~(keyword level) "` logging level.\n"
                "\n"
                "  The log will include anything in `com.kroo.epilogue/*context*` within the\n"
                "  current dynamic scope.\n"
                "\n"
                "  `data` can be anything that implements the `clojure.core.protocols/IKVReduce`\n"
                "  protocol, but it is recommended to log only maps to avoid confusion.\n"
                "\n"
                "  Options:\n"
                "    - a throwable object as the `cause`,\n"
                "    - a sequence of SLF4J `markers` (or strings/keywords), and\n"
                "    - an override logger namespace (`logger-ns`).")
      :arglists '~'([msg data & {:keys [cause markers logger-ns]}])}
     ([msg#]
      `(log ~~(keyword level) ~msg# {}))
     ([msg# data# & opts#]
      `(log ~~(keyword level) ~msg# ~data# ~@opts#))))

;; Generate convenience macros for the supported logging levels.
(declare error warn info debug trace)
(deflevel error)
(deflevel warn)
(deflevel info)
(deflevel debug)
(deflevel trace)

(declare raise)
(defloggingmacro raise
  "Log and throw.  Logs at `:error` level by default.  Constructs and throws a
  `clojure.lang.ExceptionInfo` object."
  {:arglists '([msg data & {:keys [level cause markers logger-ns]}])}
  [msg data & {:as opts}]
  `(let [msg#  ~msg
         data# ~data
         opts# ~opts]
     (log (get level? (:level opts#) :error) msg# data# opts#)
     (throw (ex-info msg# data# (:cause opts#)))))
