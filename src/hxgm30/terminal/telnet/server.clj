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
  [fqdn pkey-bits]
  (when (and fqdn pkey-bits)
    (let [ssc (new SelfSignedCertificate fqdn
                                         (new SecureRandom)
                                         pkey-bits)
          cert (.certificate ssc)
          private-key (.privateKey ssc)]
      (.build (SslContextBuilder/forServer cert private-key)))))

(defn init
  ([]
    (init {}))
  ([_opts]
    (log/debug "Initializing telnet event loops ...")
    {:boss-group (new NioEventLoopGroup 1)
     :worker-group (new NioEventLoopGroup)}))

(defn bootstrap
  [event-loops {:keys [port fqdn pkey-bits log-level]}]
  (log/debug "Booting telnet server ...")
  (let [ssl-context (build-ssl-context fqdn pkey-bits)
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
  ([opts]
    (start (init) opts))
  ([event-loops opts]
    (-> event-loops
        (bootstrap opts)
        (future))
    event-loops))

(defn stop
  [{:keys [boss-group worker-group]}]
  (log/debug "Attempting graceful shutdown of telnet server ....")
  (.shutdownGracefully boss-group)
  (.shutdownGracefully worker-group))
