(ns hxgm30.terminal.components.telnet-ssl
  (:require
    [com.stuartsierra.component :as component]
    [hxgm30.terminal.components.config :as config]
    [hxgm30.terminal.telnet.server :as telnet]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Telnet Server Component API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Lifecycle Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord TelnetSSL [server])

(defn start
  [this]
  (log/info "Starting telnet-ssl component ...")
  (let [port (config/telnet-ssl-port this)
        opts (assoc (config/telnet-ssl-key-gen this)
                    :port port
                    :log-level (config/log-level this))
        server (telnet/init)]
    (telnet/start server opts)
    (log/debugf "Telnet SSL server is listening on port %s" port)
    (log/debug "Started telnet-ssl component.")
    (assoc this :server server)))

(defn stop
  [this]
  (log/info "Stopping telnet-ssl component ...")
  (if-let [server (:server this)]
    (telnet/stop server))
  (log/debug "Stopped telnet-ssl component.")
  (assoc this :server nil))

(def lifecycle-behaviour
  {:start start
   :stop stop})

(extend TelnetSSL
  component/Lifecycle
  lifecycle-behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Component Constructor   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-component
  ""
  []
  (map->TelnetSSL {}))
