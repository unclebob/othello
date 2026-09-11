(ns othello.ui.web
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [othello.board :as board]
            [othello.ai :as ai]
            [othello.ui.layout :as layout]
            [othello.ui.events :as events]
            [othello.ui.draw :as draw]))

(defn- play-ai [board player]
  (ai/move board player {:time-ms 400 :endgame 8 :max-depth 4}))

(defn setup []
  (q/frame-rate 60)
  (q/color-mode :rgb)
  (q/smooth)
  (events/fresh-ui board/black play-ai))

(defn- job-done? [state]
  (let [job (:ai-job state)]
    (boolean (and job (not= :pending @job)))))

(defn- job-result [state]
  (when (job-done? state)
    @(:ai-job state)))

(defn- launch-ai [state]
  (let [game (:game state)
        job (atom :pending)]
    (js/setTimeout
     (fn []
       (reset! job (play-ai (:board game) (:to-move game))))
     20)
    (assoc state :ai-job job)))

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

(q/defsketch othello
  :host "othello"
  :size [layout/window-width layout/window-height]
  :setup setup
  :update update-state
  :draw draw/draw-state
  :mouse-pressed on-press
  :mouse-moved on-move
  :key-pressed on-key
  :middleware [m/fun-mode])

(defn init []
  othello)
