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
  :description "Terminal communications for use by hexagram30 projects"
  :url "https://github.com/hexagram30/terminal"
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [clojusc/dev-system "0.1.0"]
    [clojusc/twig "0.3.2"]
    [hexagram30/common "0.1.0-SNAPSHOT"]
    [hexagram30/shell "0.1.0-SNAPSHOT"]
    [io.netty/netty-handler "4.1.23.Final"]
    [io.netty/netty-tcnative "2.0.8.Final"]
    [io.netty/netty-tcnative-boringssl-static "2.0.8.Final"]
    [io.netty/netty-transport "4.1.23.Final"]
    [org.clojure/clojure "1.8.0"]]
  :plugins [
    [venantius/ultra "0.5.2"]]
  :main hxgm30.terminal.core
  :profiles {
    :precompile {
      :aot [clojure.tools.logging.impl
            hxgm30.terminal.telnet.handler
            hxgm30.terminal.telnet.initializer
            hxgm30.terminal.telnet.server]}
    :ubercompile {
      :aot :all}
    :dev {
      :dependencies [
        [clojusc/trifl "0.2.0"]
        [org.clojure/tools.namespace "0.2.11"]]
      :plugins [
        [venantius/ultra "0.5.2"]]
      :source-paths ["dev-resources/src"]
      :repl-options {
        :init-ns hxgm30.terminal.dev
        :prompt ~get-prompt
        :init ~(println (get-banner))}}
    :lint {
      :exclusions [
        org.clojure/tools.namespace]
      :dependencies [
        [org.clojure/tools.namespace "0.2.11"]]
      :source-paths ^:replace ["src"]
      :test-paths ^:replace []
      :plugins [
        [jonase/eastwood "0.2.5"]
        [lein-ancient "0.6.15"]
        [lein-bikeshed "0.5.1"]
        [lein-kibit "0.1.6"]
        [venantius/yagni "0.1.4"]]}
    :test {
      :plugins [[lein-ltest "0.3.0"]]}}
  :aliases {
    "precompile"
      ["with-profile" "+precompile" "compile"]
    "repl" ["do"
      ["clean"]
      ["precompile"]
      ["repl"]]
    "check-vers" ["with-profile" "+lint" "ancient" "check" ":all"]
    "check-jars" ["with-profile" "+lint" "do"
      ["deps" ":tree"]
      ["deps" ":plugin-tree"]]
    "check-deps" ["do"
      ["check-jars"]
      ["check-vers"]]
    "kibit" ["with-profile" "+lint" "kibit"]
    "eastwood" ["with-profile" "+lint" "eastwood" "{:namespaces [:source-paths]}"]
    "lint" ["do"
      ["kibit"]
      ;["eastwood"]
      ]
    "ltest" ["with-profile" "+test" "ltest"]
    "ubercompile" ["do"
      ["clean"]
      ["precompile"]
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
    "start" ["do"
      ["clean"]
      ["precompile"]
      ["trampoline" "run"]]
    "start-safe" ["do"
      ["clean"]
      ["precompile"]
      ["run"]]})
