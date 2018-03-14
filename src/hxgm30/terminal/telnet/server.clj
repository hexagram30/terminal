(ns hxgm30.terminal.telnet.server
  (:require
    [clojusc.twig :as logger]
    [hxgm30.terminal.config :as config]
    [taoensso.timbre :as log])
  (:import
    (hxgm30.terminal.telnet.initializer TelnetServerInitializer)
    (io.netty.bootstrap ServerBootstrap)
    (io.netty.channel EventLoopGroup)
    (io.netty.channel.nio NioEventLoopGroup)
    (io.netty.channel.socket.nio NioServerSocketChannel)
    (io.netty.handler.logging LogLevel LoggingHandler)
    (io.netty.handler.ssl SslContextBuilder)
    (io.netty.handler.ssl.util SelfSignedCertificate)
    (java.net InetAddress)
    (java.security SecureRandom)
    (java.util Date))
  (:gen-class))

(defn build-ssl-context
  [ssl-config]
  (let [ssc (new SelfSignedCertificate (:fqdn ssl-config)
                                       (new SecureRandom)
                                       (:pkey-bits ssl-config))
        cert (.certificate ssc)
        private-key (.privateKey ssc)]
    (when (:enabled? ssl-config)
      (.build (SslContextBuilder/forServer cert private-key)))))

(defn init
  [_cfg]
  (log/debug "Initializing telnet event loops ...")
  {:boss-group (new NioEventLoopGroup 1)
   :worker-group (new NioEventLoopGroup)})

(defn bootstrap
  [event-loops port ssl-config]
  (log/debug "Booting telnet server ...")
  (let [ssl-context (build-ssl-context ssl-config)
        {:keys [boss-group worker-group]} event-loops
        boot (new ServerBootstrap)]
    (-> boot
        (.group boss-group worker-group)
        (.channel NioServerSocketChannel)
        (.handler (new LoggingHandler LogLevel/DEBUG))
        (.childHandler (new TelnetServerInitializer ssl-context)))
    (-> boot
        (.bind port)
        (.sync)
        (.channel)
        (.closeFuture)
        (.sync))
    (log/debug "Joining current thread ...")))

(defn start
  [event-loops port ssl-config]
  (-> event-loops
      (bootstrap port ssl-config)
      (future)))

(defn stop
  [{:keys [boss-group worker-group]}]
  (log/debug "Attempting graceful shutdown of telnet server ....")
  (.shutdownGracefully boss-group)
  (.shutdownGracefully worker-group))

(defn -main
  []
  (let [cfg (config/data)
        port (get-in cfg [:telnet :port])
        ssl-config (get-in cfg [:telnet :ssl])
        event-loops (init cfg)]
    (logger/set-level! (get-in cfg [:logging :nss])
                       (get-in cfg [:logging :level]))
    (try
      (start event-loops port ssl-config)
      (.join (Thread/currentThread))
      (finally
        (stop event-loops)))))
