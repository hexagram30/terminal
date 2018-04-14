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
    (java.util Date)))

(defn convert-log-level
  [log-level]
  (case
    :trace LogLevel/TRACE
    :debug LogLevel/DEBUG
    :info LogLevel/INFO
    :warn LogLevel/WARN
    :error LogLevel/ERROR
    :fatal LogLevel/ERROR))

(defn build-ssl-context
  [key-gen-cfg]
  (when (seq key-gen-cfg)
    (let [ssc (new SelfSignedCertificate (:fqdn key-gen-cfg)
                                         (new SecureRandom)
                                         (:pkey-bits key-gen-cfg))
          cert (.certificate ssc)
          private-key (.privateKey ssc)]
      (.build (SslContextBuilder/forServer cert private-key)))))

(defn init
  ([]
    (init {}))
  ([_cfg]
    (log/debug "Initializing telnet event loops ...")
    {:boss-group (new NioEventLoopGroup 1)
     :worker-group (new NioEventLoopGroup)}))

(defn bootstrap
  [event-loops port key-gen-cfg log-level]
  (log/debug "Booting telnet server ...")
  (let [ssl-context (build-ssl-context key-gen-cfg)
        {:keys [boss-group worker-group]} event-loops
        boot (new ServerBootstrap)]
    (-> boot
        (.group boss-group worker-group)
        (.channel NioServerSocketChannel)
        (.handler (new LoggingHandler (convert-log-level log-level)))
        (.childHandler (new TelnetServerInitializer ssl-context)))
    (-> boot
        (.bind port)
        (.sync)
        (.channel)
        (.closeFuture)
        (.sync))
    (log/debug "Joining current thread ...")))

(defn start
  ([event-loops port log-level]
    (start event-loops port {} log-level))
  ([event-loops port key-gen-cfg log-level]
    (-> event-loops
        (bootstrap port key-gen-cfg log-level)
        (future))))

(defn stop
  [{:keys [boss-group worker-group]}]
  (log/debug "Attempting graceful shutdown of telnet server ....")
  (.shutdownGracefully boss-group)
  (.shutdownGracefully worker-group))
