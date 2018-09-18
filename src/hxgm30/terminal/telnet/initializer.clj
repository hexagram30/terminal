(ns hxgm30.terminal.telnet.initializer
  (:require
    [clojure.string :as string]
    [taoensso.timbre :as log])
  (:import
    (hxgm30.terminal.telnet.handler HexagramTelnetServerHandler)
    (io.netty.channel.socket SocketChannel)
    (io.netty.handler.codec DelimiterBasedFrameDecoder Delimiters)
    (io.netty.handler.codec.string StringDecoder StringEncoder)
    (io.netty.handler.ssl SslContext))
  (:gen-class
    :name hxgm30.terminal.telnet.initializer.HexagramTelnetServerInitializer
    :extends io.netty.channel.ChannelInitializer
    :constructors {[Object] []}
    :init init
    :state state))

(defn get-ssl-context
  [this]
  (log/debug "Getting SSL context ...")
  (:ssl-context (.state this)))

(defn -init
  [init-data]
  (log/debug "Initializing telnet ...")
  (log/debug "init-data: " init-data)
  [[] init-data])

(defn -initChannel
  [this ch]
  (log/debug "Initializing telnet channel ...")
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
              (new HexagramTelnetServerHandler ssl?))))
