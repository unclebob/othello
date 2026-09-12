(ns othello.ui.web
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [othello.ai :as ai]
            [othello.ui.layout :as layout]
            [othello.ui.host :as host]
            [othello.ui.draw :as draw]))

(defn- play-ai [board player]
  (ai/move board player {:time-ms 400 :endgame 8 :max-depth 4}))

(defn setup []
  (q/frame-rate 60)
  (q/color-mode :rgb)
  (q/smooth)
  (host/initial-state play-ai))

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
  (host/update-state state {:job-done? (job-done? state)
                            :job-result (job-result state)
                            :launch-ai launch-ai}))

(q/defsketch othello
  :host "othello"
  :size [layout/window-width layout/window-height]
  :setup setup
  :update update-state
  :draw draw/draw-state
  :mouse-pressed host/on-press
  :mouse-moved host/on-move
  :key-pressed host/on-key
  :middleware [m/fun-mode])

(defn init []
  othello)
