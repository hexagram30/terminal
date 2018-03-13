(ns hxgm30.terminal.telnet.handler
  (:require
    [clojure.string :as string]
    [hxgm30.shell.parser :as parser]
    [hxgm30.shell.core :as shell]
    [taoensso.timbre :as log])
  (:import
    (io.netty.channel ChannelFuture
                      ChannelFutureListener
                      ChannelHandler$Sharable
                      ChannelHandlerContext
                      ChannelHandler$Sharable)))

(gen-class
    :name #^{io.netty.channel.ChannelHandler$Sharable true}
          hxgm30.terminal.telnet.handler.TelnetServerHandler
    :extends io.netty.channel.SimpleChannelInboundHandler
    :state state
    :init init
    :constructors {[Boolean] []})

(defn get-shell
  [this]
  (:shell (.state this)))

(defn ssl?
  [this]
  (:ssl? (.state this)))

(defn disconnect
  [future]
  (log/debug "Connection closing ...")
  (.addListener future ChannelFutureListener/CLOSE))

(defn -init
  [ssl?]
  [[] {:shell (shell/create-shell :demo {:disconnect-handler disconnect})
       :ssl? ssl?}])

(defn -channelActive
  [this ^ChannelHandlerContext ctx]
  (log/debug "Connection opening ...")
  (when (ssl? this)
    (.write ctx "This channel is SSL-encrypted.\r\n\r\n"))
  (.write ctx (shell/banner (get-shell this)))
  (.flush ctx))

(defn -channelRead0
  ;; XXX Once we move to netty 5.0, we will need to rename this function
  ;;     to messageReceived.
  [this ^ChannelHandlerContext ctx request]
  (let [shell (get-shell this)
        {:keys [response message]} (shell/handle-request shell request)
        future (.write ctx message)]
    (shell/handle-disconnect shell response future)))

(defn -channelReadComplete
  [this ^ChannelHandlerContext ctx]
  (.flush ctx))

(defn -exceptionCaught
  [this ctx cause]
  (.printStackTrace cause)
  (.close ctx))
