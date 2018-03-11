(ns hexagram30.terminal.config
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(def config-file "hexagram30-config/terminal.edn")

(defn data
  ([]
    (data config-file))
  ([filename]
    (with-open [rdr (io/reader (io/resource filename))]
      (edn/read (new java.io.PushbackReader rdr)))))
