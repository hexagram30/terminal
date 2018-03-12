(ns hxgm30.terminal.config
  (:require
   [hxgm30.common.file :as common]))

(def config-file "hexagram30-config/terminal.edn")

(defn data
  ([]
    (data config-file))
  ([filename]
    (common/read-edn-resource filename)))
