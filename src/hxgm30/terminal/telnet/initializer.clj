(ns hxgm30.terminal.telnet.initializer
  (:require
    [clojure.string :as string])
  (:import
    (hxgm30.terminal.telnet.handler TelnetServerHandler)
    (io.netty.channel.socket SocketChannel)
    (io.netty.handler.codec DelimiterBasedFrameDecoder Delimiters)
    (io.netty.handler.codec.string StringDecoder StringEncoder)
    (io.netty.handler.ssl SslContext))
  (:gen-class
    :name hxgm30.terminal.telnet.initializer.TelnetServerInitializer
    :extends io.netty.channel.ChannelInitializer
    :constructors {[Object] []}
    :init init
    :state state))

(defn get-ssl-context
  [this]
  (:ssl-context (.state this)))

(defn -init
  [ssl-context]
  [[] {:ssl-context ssl-context}])

(defn -initChannel
  [this ch]
  (let [pipeline (.pipeline ch)
        ssl-context (get-ssl-context this)
        ssl? (not (nil? ssl-context))]
    (when ssl?
      (.addLast pipeline
                (.newHandler ssl-context (.alloc ch))))
    (.addLast pipeline
              (new DelimiterBasedFrameDecoder 8192 (Delimiters/lineDelimiter)))
    (.addLast pipeline
              (new StringDecoder))
    (.addLast pipeline
              (new StringEncoder))
    (.addLast pipeline
              (new TelnetServerHandler ssl?))))
