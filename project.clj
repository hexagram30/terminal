(defn get-banner
  []
  (try
    (str
      (slurp "resources/text/banner.txt")
      (slurp "resources/text/loading.txt"))
    ;; If another project can't find the banner, just skip it;
    ;; this function is really only meant to be used by Dragon itself.
    (catch Exception _ "")))

(defn get-prompt
  [ns]
  (str "\u001B[35m[\u001B[34m"
       ns
       "\u001B[35m]\u001B[33m λ\u001B[m=> "))

(defproject hexagram30/terminal "0.1.0-SNAPSHOT"
  :description "Telnet, SSL Telnet, SSH, and secure REPLs for use by hexagram30 projects"
  :url "https://github.com/hexagram30/terminal"
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [clojusc/twig "0.3.2"]
    [hexagram30/common "0.1.0-SNAPSHOT"]
    [hexagram30/shell "0.1.0-SNAPSHOT"]
    [io.netty/netty-handler "4.1.22.Final"]
    [io.netty/netty-transport "4.1.22.Final"]
    [org.clojure/clojure "1.8.0"]]
  :plugins [
    [venantius/ultra "0.5.2"]]
  :profiles {
    :ubercompile {
      :aot [hxgm30.terminal.telnet.handler
            hxgm30.terminal.telnet.initializer
            hxgm30.terminal.telnet.server]}
    :test {
      :plugins [[lein-ltest "0.3.0"]]}
    :telnet {
      :main hxgm30.terminal.telnet.server}}
  :aliases {
    "ubercompile" ["do"
      ["clean"]
      ["with-profile" "+ubercompile" "compile"]]
    "install" ["do"
      ["clean"]
      ["with-profile" "+ubercompile" "install"]]
    "jar" ["do"
      ["clean"]
      ["with-profile" "+ubercompile" "jar"]]
    "uberjar" ["do"
      ["clean"]
      ["with-profile" "+ubercompile" "uberjar"]]
    "telnet-server" ["do"
      ["ubercompile"]
      ["with-profile" "+telnet" "run"]]})
