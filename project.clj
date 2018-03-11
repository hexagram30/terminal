(defproject hexagram30/terminal "0.1.0-SNAPSHOT"
  :description "Remote TTY backends for hexagramMUSH"
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
  :profiles {
    :test {
      :plugins [[lein-ltest "0.3.0"]]}
    :ubercompile {
      :aot [hexagram30.terminal.telnet.handler
            hexagram30.terminal.telnet.initializer
            hexagram30.terminal.telnet.server]}
    :telnet {
      :main hexagram30.terminal.telnet.server.TelnetServer}}
  :aliases {
    "compile" ["do"
      ["clean"]
      ["with-profile" "+ubercompile" "compile"]]
    "telnet-server" ["with-profile" "+telnet" "run"]})
