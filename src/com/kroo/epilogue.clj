(ns com.kroo.epilogue
  (:import [org.slf4j Logger LoggerFactory]
           [org.slf4j.event Level]
           [org.slf4j.spi LoggingEventBuilder]))

(defmacro logger []
  `(LoggerFactory/getLogger (str ~*ns*)))

(defmacro log
  ""
  [msg data & {:keys [level throwable logger-factory logger-ns]
               :or   {level :info}}]
  `())

;; TODO: different arity taking an exception?
(defmacro ^:private deflevel [level]
  `(defmacro ~(symbol level)
     ""
     {:arglists '~'([msg data & {:as opts}])}
     [msg# data# & {:as opts#}]
     `(log ~msg# ~data# (assoc opts# :level ~~(keyword level)))))

(declare fatal error warn info debug trace)
(deflevel :fatal)
(deflevel :error)
(deflevel :warn)
(deflevel :info)
(deflevel :debug)
(deflevel :trace)

(defmacro spy [])

(defmacro raise [])
