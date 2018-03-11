(defproject hexagram30/terminal "0.1.0-SNAPSHOT"
  :description "Remote TTY backends for hexagramMUSH"
  :url "https://github.com/hexagram30/terminal"
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [clojusc/twig "0.3.2"]
    [hexagram30/shell "0.1.0-SNAPSHOT"]
    [io.netty/netty-handler "4.1.22.Final"]
    [io.netty/netty-transport "4.1.22.Final"]
    [org.clojure/clojure "1.8.0"]]
  :profiles {
    :test {
      :plugins [[lein-ltest "0.3.0"]]}
    :ubercompile {
      :aot [hexagram30.telnet.handler
            hexagram30.telnet.initializer
            hexagram30.telnet.server]}
    :telnet {
      :main hexagram30.telnet.server.TelnetServer}
    :secure {
      :jvm-opts ["-Dssl=true"]}}
  :aliases {
    "compile" ["do"
      ["clean"]
      ["with-profile" "+ubercompile" "compile"]]
    "telnet-server" ["with-profile" "+telnet" "run"]
    "ssl-telnet-server" ["with-profile" "+telnet,+secure" "run"]})
