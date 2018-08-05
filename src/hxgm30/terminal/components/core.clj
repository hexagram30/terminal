(ns hxgm30.terminal.components.core
  (:require
    [com.stuartsierra.component :as component]
    [hxgm30.terminal.components.config :as config]
    [hxgm30.terminal.components.logging :as logging]
    [hxgm30.terminal.components.telnet :as telnet]
    [hxgm30.terminal.components.telnet-ssl :as telnet-ssl]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Common Configuration Components   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn cfg
  [cfg-data]
  {:config (config/create-component cfg-data)})

(def log
  {:logging (component/using
             (logging/create-component)
             [:config])})

(def telnet
  {:telnet (component/using
            (telnet/create-component)
            [:config :logging])})

(def telnet-ssl
  {:telnet-ssl (component/using
                (telnet-ssl/create-component)
                [:config :logging])})

(defn terminal
  [cfg-data]
  (merge (cfg cfg-data)
         log
         telnet
         telnet-ssl))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Initializations   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn initialize-bare-bones
  []
  (component/map->SystemMap (config/build-config)))

(defn initialize-with-terminal
  []
  (-> (config/build-config)
      terminal
      component/map->SystemMap))

(def init-lookup
  {:basic #'initialize-bare-bones
   :terminal initialize-with-terminal})

(defn init
  ([]
    (init :terminal))
  ([mode]
    ((mode init-lookup))))
