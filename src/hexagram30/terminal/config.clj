(ns hexagram30.terminal.config
  (:require
   [hexagram30.common.file :as common]))

(def config-file "hexagram30-config/terminal.edn")

(defn data
  ([]
    (data config-file))
  ([filename]
    (common/read-edn-resource filename)))
