(ns hxgm30.terminal.telnet.handler
  (:require
    [clojure.string :as string]
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
          hxgm30.terminal.telnet.handler.HexagramTelnetServerHandler
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

(defn connected
  [this ^ChannelHandlerContext ctx]
  (log/debug "Connection opening ...")
  (let [shell (get-shell this)]
    (when (ssl? this)
      (.write ctx "\r\nThis connection is SSL-encrypted.\r\n"))
    (.write ctx (shell/on-connect shell))
    (.write ctx (shell/prompt shell))
    (.flush ctx)))

(defn message-received
  [this ^ChannelHandlerContext ctx request]
  (let [shell (get-shell this)
        response (shell/handle-request shell request)
        _ (log/debug "response:" response)
        future (.write ctx (str response (shell/prompt shell)))]
    (shell/handle-disconnect shell response future)
    (.flush ctx)))

(defn -init
  [ssl?]
  [[] {:shell (shell/create :entry {:disconnect-handler disconnect})
       :ssl? ssl?}])

(defn -channelActive
  [this ^ChannelHandlerContext ctx]
  (connected this ctx))

(defn -channelRead0
  ;; XXX Once we move to netty 5.0, we will need to rename this function
  ;;     to -messageReceived.
  [this ^ChannelHandlerContext ctx request]
  (message-received this ctx request))

(defn -channelReadComplete
  [this ^ChannelHandlerContext ctx]
  (.flush ctx))

(defn -exceptionCaught
  [this ctx cause]
  (.printStackTrace cause)
  (.close ctx))
