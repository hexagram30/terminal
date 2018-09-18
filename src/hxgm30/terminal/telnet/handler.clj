(ns hxgm30.terminal.telnet.handler
  (:require
    [clojure.string :as string]
    [hxgm30.terminal.components.config :as config]
    [hxgm30.shell.components.registry :as shell-registry]
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
    :constructors {[Object] []})

(defn get-shell
  [this]
  (:shell (.state this)))

(defn get-system
  [this]
  (:system (.state this)))

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
  [{:keys [ssl? system]}]
  (log/debug "Default shell: " (config/default-shell system))
  (log/debug "Loading shell from registry ...")
  [[] {:shell (shell-registry/get-shell system (config/default-shell system))
       :ssl? ssl?
       :system system}])

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
