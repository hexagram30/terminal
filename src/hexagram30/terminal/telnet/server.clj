(ns hexagram30.terminal.telnet.server
  (:require
    [clojusc.twig :as logger]
    [hexagram30.terminal.config :as config]
    [taoensso.timbre :as log])
  (:import
    (hexagram30.terminal.telnet.initializer TelnetServerInitializer)
    (io.netty.bootstrap ServerBootstrap)
    (io.netty.channel EventLoopGroup)
    (io.netty.channel.nio NioEventLoopGroup)
    (io.netty.channel.socket.nio NioServerSocketChannel)
    (io.netty.handler.logging LogLevel LoggingHandler)
    (io.netty.handler.ssl SslContextBuilder)
    (io.netty.handler.ssl.util SelfSignedCertificate)
    (java.net InetAddress)
    (java.util Date))
  (:gen-class
    :name hexagram30.terminal.telnet.server.TelnetServer))

(defn build-ssl-context
  [ssl?]
  (let [ssc (new SelfSignedCertificate)
        cert (.certificate ssc)
        private-key (.privateKey ssc)]
    (when ssl?
      (.build (SslContextBuilder/forServer cert private-key)))))

(defn init
  []
  (log/debug "Initializing telnet event loops ...")
  {:boss-group (new NioEventLoopGroup 1)
   :worker-group (new NioEventLoopGroup)})

(defn boot
  [event-loops port ssl?]
  (log/debug "Booting telnet server ...")
  (let [ssl-context (build-ssl-context ssl?)
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
  [event-loops port ssl?]
  (future (boot event-loops port ssl?)))

(defn stop
  [{:keys [boss-group worker-group]}]
  (log/debug "Attempting graceful shutdown of telnet server ....")
  (.shutdownGracefully boss-group)
  (.shutdownGracefully worker-group))

(defn -main
  []
  (let [cfg (config/data)
        port (get-in cfg [:telnet :port])
        ssl? (:ssl? cfg)
        event-loops (init)]
    (logger/set-level! (get-in cfg [:logging :nss])
                       (get-in cfg [:logging :level]))
    (try
      (start event-loops port ssl?)
      (.join (Thread/currentThread))
      (finally
        (stop event-loops)))))
