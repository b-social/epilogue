(ns com.example.logging.core
  (:require [com.kroo.epilogue :as log])
  (:import [org.slf4j.bridge SLF4JBridgeHandler]))

;; Install the `java.util.logging` SLF4J bridge.
(SLF4JBridgeHandler/removeHandlersForRootLogger)
(SLF4JBridgeHandler/install)

(log/info "Log!" {:foo "bar"})

(defn run [& args]
  (log/info "Log!" {:foo "bar"})
  (log/info "Log no data!"))

