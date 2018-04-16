(ns hxgm30.terminal.util.networkless
  (:require
    [clojure.string :as string]
    [hxgm30.shell.core :as shell]
    [taoensso.timbre :as log]))

(defprotocol HandlerAPI
  (get-shell [this])
  (disconnect [this])
  (connected [this])
  (message-received [this msg]))

(defrecord Handler
  [shell])

(defn -get-shell
  [this]
  (:shell this))

(defn -disconnect
  [this]
  (log/debug "Connection closing ..."))

(defn -connected
  [this]
  (log/debug "Connection opening ...")
  (print "\nYou are connected to a networkless shell server.\n\n")
  (print (shell/banner (get-shell this)))
  (print "\n\n")
  (flush))

(defn -message-received
  [this input]
  (let [shell (get-shell this)
        {:keys [response message]} (shell/handle-request shell input)]
    (log/debug "response:" response)
    (log/debug "message:" message)
    (print message)
    (shell/handle-disconnect shell response this)
    (flush)))

(def behaviour
  {:get-shell -get-shell
   :disconnect -disconnect
   :connected -connected
   :message-received -message-received})

(extend Handler
        HandlerAPI
        behaviour)

(defn create-shell
  []
  (shell/create-shell :login {:disconnect-handler disconnect}))

(defn create-handler
  []
  (map->Handler {:shell (create-shell)}))

(defn start-server
  []
  (let [handler (create-handler)]
    (connected handler)
    (loop [msg nil]
      (when-not (nil? msg)
        (log/debug "Got msg:" msg)
        (message-received handler msg))
      (when-not (= msg "QUIT")
        (recur
          (read-line))))))
