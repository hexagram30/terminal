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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constants   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ssl-notice "\r\nThis connection is SSL-encrypted.\r\n")
(def disconnect-notice "\r\nDisconnecting ...\r\n")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Accessors   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-shell
  [this]
  (:shell (.state this)))

(defn get-system
  [this]
  (:system (.state this)))

(defn ssl?
  [this]
  (:ssl? (.state this)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Helper Methods   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn disconnect
  [this ^ChannelHandlerContext ctx]
  (log/debug "Connection closing ...")
  (let [future (.write ctx disconnect-notice)]
    (.flush ctx)
    (.addListener future ChannelFutureListener/CLOSE)))

(defn connected
  [this ^ChannelHandlerContext ctx]
  (log/debug "Connection opening ...")
  (let [sh (get-shell this)]
    (when (ssl? this)
      (.write ctx ssl-notice))
    (.write ctx (shell/on-connect sh))
    (.write ctx (shell/prompt sh))))

(defn message-received
  [this ^ChannelHandlerContext ctx line]
  (let [sh (get-shell this)
        {:keys [cmd] :as parsed} (shell/read sh line)]
    (if (shell/disconnect? sh cmd)
      (disconnect this ctx)
      (let [evaled (shell/evaluate sh parsed)
            result (shell/print sh evaled)]
        (log/debug "result:" result)
        (.write ctx (str result (shell/prompt sh)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   SimpleChannelInboundHandler Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -init
  [{:keys [ssl? system]}]
  (let [shell-key (config/default-shell system)]
    (log/debug "Default shell: " shell-key)
    (log/debug "Loading shell from registry ...")
    [[] {:shell (shell-registry/get-shell system shell-key)
         :ssl? ssl?
         :system system}]))

(defn -channelActive
  [this ^ChannelHandlerContext ctx]
  (connected this ctx)
  (.flush ctx))

(defn -channelRead0
  ;; XXX Once we move to netty 5.0, we will need to rename this function
  ;;     to -messageReceived.
  [this ^ChannelHandlerContext ctx request]
  (message-received this ctx request))

(defn -channelReadComplete
  [this ^ChannelHandlerContext ctx]
  (log/debug "Channel read completed.")
  (.flush ctx))

(defn -exceptionCaught
  [this ctx cause]
  (.printStackTrace cause)
  (log/errorf "Got exception:\n%s" cause)
  (.close ctx))
