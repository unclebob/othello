(ns othello.spec-helper
  (:require [clojure.string :as str]
            [othello.board :as board]
            [othello.rules :as rules]
            [othello.ui.events :as events]
            [othello.ui.layout :as layout]))

(defn parse-board [text]
  (let [chars (->> (str/split-lines text)
                   (map str/trim)
                   (remove str/blank?)
                   (mapcat identity))
        ->cell {\. board/empty-cell
                \b board/black
                \B board/black
                \w board/white
                \W board/white}]
    (vec (map ->cell chars))))

(defn first-legal [board player]
  (first (rules/legal-moves board player)))

(defn drain-animation
  ([state] (drain-animation state 4000))
  ([state limit]
   (loop [s state
          n 0]
     (cond
       (>= n limit) s
       (= :animating (:phase s)) (recur (events/tick s) (inc n))
       (= :pass-notice (:phase s)) (recur (events/tick s) (inc n))
       :else s))))

(defn drain
  ([state] (drain state 4000))
  ([state limit]
   (loop [s (drain-animation state limit)
          n 0]
     (cond
       (>= n limit) s
       (> (:settle-frames s) 0) (recur (events/tick s) (inc n))
       :else s))))

(defn click-square [state row col]
  (let [[x y] (layout/square-center row col)]
    (-> state
        (events/on-mouse-move x y)
        (events/on-click x y))))

(defn play-at [state row col]
  (drain (click-square state row col)))

(defn let-computer-play [state]
  (drain (events/tick-ai state)))

(defn play-turn [state row col]
  (-> state
      (play-at row col)
      let-computer-play))

(defn click-button [state id]
  (let [button (first (filter #(= id (:id %)) (layout/buttons)))
        x (+ (:x button) (quot (:w button) 2))
        y (+ (:y button) (quot (:h button) 2))]
    (events/on-click state x y)))
