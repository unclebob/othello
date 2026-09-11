(ns othello.ui.sketch
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [othello.board :as board]
            [othello.ai :as ai]
            [othello.ui.layout :as layout]
            [othello.ui.events :as events]
            [othello.ui.draw :as draw]))

(defn- play-ai [board player]
  (ai/move board player {:time-ms 1200 :endgame 12 :max-depth 6}))

(defn setup []
  (q/frame-rate 60)
  (q/color-mode :rgb)
  (q/smooth)
  (q/text-font (q/create-font "SansSerif" 16 true))
  (events/fresh-ui board/black play-ai))

(defn- job-done? [state]
  (let [job (:ai-job state)]
    (boolean (and job (realized? job)))))

(defn- job-result [state]
  (when (job-done? state)
    @(:ai-job state)))

(defn- launch-ai [state]
  (let [game (:game state)]
    (assoc state :ai-job (future (play-ai (:board game) (:to-move game))))))

(defn update-state [state]
  (let [s (events/on-frame state {:job-done? (job-done? state)
                                  :job-result (job-result state)})]
    (if (events/needs-ai-job? s)
      (launch-ai s)
      s)))

(defn event-xy [event]
  [(int (:x event)) (int (:y event))])

(defn on-press [state event]
  (if (= :right (:button event))
    state
    (let [[x y] (event-xy event)]
      (events/on-click state x y))))

(defn on-move [state event]
  (let [[x y] (event-xy event)]
    (events/on-mouse-move state x y)))

(defn on-key [state event]
  (events/on-key state (:key event)))

(defn start! []
  (q/sketch
    :title "Othello"
    :size [layout/window-width layout/window-height]
    :setup setup
    :update update-state
    :draw draw/draw-state
    :mouse-pressed on-press
    :mouse-moved on-move
    :key-pressed on-key
    :middleware [m/fun-mode]
    :features [:keep-on-top]))
