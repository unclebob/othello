(ns othello.ai
  (:require [othello.ai.search :as search]))

(defn move
  ([board player]
   (move board player {:depth search/default-depth}))
  ([board player opts]
   (search/choose board player opts)))
