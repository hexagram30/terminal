(ns hxgm30.terminal.core
  (:require
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [hxgm30.terminal.components.core :as components]
    [taoensso.timbre :as log]
    [trifl.java :as java])
  (:gen-class))

(defn shutdown
  [system]
  (component/stop system)
  (logger/set-level! '[hxgm30] :info)
  (log/info "System shutdown complete."))

(defn -main
  [& args]
  (logger/set-level! '[hxgm30] :info)
  (log/info "Starting system ...")
  (let [system (components/init)]
    (component/start system)
    (java/add-shutdown-handler #(shutdown system))
    (java/join-current-thread)))
