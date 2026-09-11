(ns othello.core
  (:require [othello.ui.sketch :as sketch])
  (:gen-class))

(defn -main [& _]
  (sketch/start!))
