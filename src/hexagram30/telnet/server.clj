(ns hexagram30.telnet.server
  (:import
    (hexagram30.telnet.initializer TelnetServerInitializer)
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
    :name hexagram30.telnet.server.TelnetServer))

(defn build-ssl-context
  [ssl?]
  (let [ssc (new SelfSignedCertificate)
        cert (.certificate ssc)
        private-key (.privateKey ssc)]
    (if ssl?
      (.build (SslContextBuilder/forServer cert private-key))
      nil)))

(defn -main
  ([]
   (-main (Integer/parseInt (or (System/getProperty "port") "8023"))))
  ([port]
   (-main port (not (nil? (System/getProperty "ssl")))))
  ([port ssl?]
   (let [ssl-context (build-ssl-context ssl?)
         boss-group (new NioEventLoopGroup 1)
         worker-group (new NioEventLoopGroup)]
     (try
       (let [boot (new ServerBootstrap)]
        (-> boot
            (.group boss-group worker-group)
            (.channel NioServerSocketChannel)
            (.handler (new LoggingHandler LogLevel/INFO))
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
          (.shutdownGracefully worker-group)))))))
