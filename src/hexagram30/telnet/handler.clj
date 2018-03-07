(ns hexagram30.telnet.handler
  (:require
    [clojure.string :as string])
  (:import
    (io.netty.channel ChannelFuture
                      ChannelFutureListener
                      ChannelHandler$Sharable
                      ChannelHandlerContext
                      ChannelHandler$Sharable)
    (java.net InetAddress)
    (java.util Date)))

(gen-class
    :name #^{io.netty.channel.ChannelHandler$Sharable true}
          hexagram30.telnet.handler.TelnetServerHandler
    :extends io.netty.channel.SimpleChannelInboundHandler
    ; :methods [[^Override
    ;            channelActive
    ;            [io.netty.channel.ChannelHandlerContext] void]
    ;           [^Override
    ;            channelRead0
    ;            [io.netty.channel.ChannelHandlerContext String] void]
    ;           [^Override
    ;            channelReadComplete
    ;            [io.netty.channel.ChannelHandlerContext] void]
    ;           [^Override
    ;            exceptionCaught
    ;            [io.netty.channel.ChannelHandlerContext Throwable] void]]
               )

(defn -channelActive
  [this ctx]
  (.write ctx (str "Welcome to " (.getHostName (InetAddress/getLocalHost))  "!\r\n"))
  (.write ctx (str "It is "  (new Date) " now.\r\n"))
  (.flush ctx))


(defn -channelRead0
  [this ctx request]
  (let [[msg close?] (cond (.isEmpty request)
                           ["Please type something.\r\n" false]
                     (= "bye" (string/lower-case (str request)))
                           ["Have a good day!\r\n" true]
                     :else [(str "Did you say '" request "'?\r\n") false])
        future (.write ctx msg)]
    (when close?
      (.addListener future ChannelFutureListener/CLOSE))))

(defn -channelReadComplete
  [this ctx]
  (.flush ctx))

(defn -exceptionCaught
  [this ctx cause]
  (.printStackTrace cause)
  (.close ctx))
