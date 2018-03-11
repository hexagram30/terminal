(ns hexagram30.terminal.components.core
  (:require
    [hexagram30.terminal.components.config :as config]
    [hexagram30.terminal.components.database :as database]
    [hexagram30.terminal.components.httpd :as httpd]
    [hexagram30.terminal.components.logging :as logging]
    [com.stuartsierra.component :as component]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Common Configuration Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cfg
  {:config (config/create-component)})

(def log
  {:logging (component/using
             (logging/create-component)
             [:config])})

(def neo
  {:neo (component/using
         (neo/create-component)
         [:config :logging])})

(def elastic
  {:elastic (component/using
             (elastic/create-component)
             [:config :logging])})

(def httpd
  {:httpd (component/using
           (httpd/create-component)
           [:config :logging :neo :elastic])})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Initializations   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn initialize-bare-bones
  []
  (component/map->SystemMap
    (merge cfg
           log
           neo)))

(defn initialize-with-web
  []
  (component/map->SystemMap
    (merge cfg
           log
           neo
           elastic
           httpd)))

(def init-lookup
  {:basic #'initialize-bare-bones
   :web #'initialize-with-web})

(defn init
  ([]
    (init :web))
  ([mode]
    ((mode init-lookup))))
