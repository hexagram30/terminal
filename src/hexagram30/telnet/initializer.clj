(ns hexagram30.telnet.initializer
  (:require
    [clojure.string :as string])
  (:import
    (hexagram30.telnet.handler TelnetServerHandler)
    (io.netty.channel.socket SocketChannel)
    (io.netty.handler.codec DelimiterBasedFrameDecoder Delimiters)
    (io.netty.handler.codec.string StringDecoder StringEncoder)
    (io.netty.handler.ssl SslContext))
  (:gen-class
    :name hexagram30.telnet.initializer.TelnetServerInitializer
    :extends io.netty.channel.ChannelInitializer
    :constructors {[Object] []}
    :init init
    :post-init set-ssl-context
    ; :methods [[^Override
    ;            initChannel
    ;            [io.netty.channel.socket.SocketChannel] void]]
               ))

(defn -set-ssl-context
  [this ssl-context]
  ;(set! (.sslCtx this) ssl-context)
  )

(defn -init
  [_ssl-context]
  [[] nil])

(defn -initChannel
  [this ch]
  (let [pipeline (.pipeline ch)
        ssl-context nil;(.sslCtx this)
        ]
    (when-not (nil? ssl-context)
      (.addLast pipeline
                (.newHandler ssl-context (.alloc ch))))
    (.addLast pipeline
              (new DelimiterBasedFrameDecoder 8192 (Delimiters/lineDelimiter)))
    (.addLast pipeline
              (new StringDecoder))
    (.addLast pipeline
              (new StringEncoder))
    (.addLast pipeline
              (new TelnetServerHandler))))
