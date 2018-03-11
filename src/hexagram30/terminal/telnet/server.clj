(ns hexagram30.terminal.telnet.server
  (:require
    [clojusc.twig :as logger]
    [hexagram30.terminal.config :as config])
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

(defn telnet
  [port ssl-context boss-group worker-group]
  (try
   (let [boot (new ServerBootstrap)]
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
        (.sync)))
   (finally
    (do
      (.shutdownGracefully boss-group)
      (.shutdownGracefully worker-group)))))

(defn -main
  ([]
   (-main (get-in (config/data) [:telnet :port])))
  ([port]
   (-main port (:ssl? (config/data))))
  ([port ssl?]
   (logger/set-level! (get-in (config/data) [:logging :nss])
                      (get-in (config/data) [:logging :level]))
   (let [ssl-context (build-ssl-context ssl?)
         boss-group (new NioEventLoopGroup 1)
         worker-group (new NioEventLoopGroup)]
     (telnet port ssl-context boss-group worker-group))))
