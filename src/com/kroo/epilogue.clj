(ns com.kroo.epilogue
  "Simple Clojure logging facade for logging structured data via SLF4J 2+."
  (:import [org.slf4j Logger LoggerFactory]
           [org.slf4j.event Level]
           [org.slf4j.spi LoggingEventBuilder]))

(set! *warn-on-reflection* true)

(def levels
  "Map of level names to SLF4J Level objects."
  {:error Level/ERROR
   :warn  Level/WARN
   :info  Level/INFO
   :debug Level/DEBUG
   :trace Level/TRACE})

(def ^:dynamic *context*
  ""
  {})

;; TODO: do I need to remove the log statements if that log level is disabled?

(defn- add-kv
  ""
  ^LoggingEventBuilder [^LoggingEventBuilder builder k ^Object v]
  (.addKeyValue builder
                (if (keyword? k)
                  (subs (str k) 1)
                  (str k))
                v))

(defn log*
  "Primitive logging function.  Do not use this function directly!"
  [^Logger logger level ^String msg data ^Throwable throwable src]
  (let [^LoggingEventBuilder logger
        (as-> logger $
          (.atLevel $ (levels level))
          (.setMessage ^LoggingEventBuilder $ msg)
          (add-kv ^LoggingEventBuilder $ ::source src)
          (reduce-kv add-kv $ *context*)
          (reduce-kv add-kv $ data))]
    (.log ^LoggingEventBuilder
     (if throwable
       (.setCause logger throwable)
       logger))))

(defmacro log
  "The main logging macro."
  [msg data & {:keys [level throwable source], :or {level :info}}]
  `(let [ns# (str *ns*)]
     (log* (LoggerFactory/getLogger ns#)
           ~level
           ~msg
           ~data
           ~throwable
           (or ~source
               (assoc ~(meta &form), :file *file*, :namespace ns#)))))

(defmacro ^:private deflevel
  ""
  [level]
  `(defmacro ~(symbol level)
     ""
     {:arglists '~'([msg data & {:as opts, :keys [throwable]}])}
     [msg# data# & {:as opts#}]
     `(log ~msg#
           ~data#
           {:level     ~~(keyword level)
            :throwable (:throwable opts#)
            #_#_:source    (assoc (meta &form), :file *file*, :namespace ns#)})))

(declare error warn info debug trace)
(doseq [level (keys levels)]
  (deflevel level))

(defmacro spy [])

(defmacro raise [])
