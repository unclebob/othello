(ns othello.ui.sketch
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [othello.ai :as ai]
            [othello.ui.layout :as layout]
            [othello.ui.host :as host]
            [othello.ui.draw :as draw]))

(defn- play-ai [board player]
  (ai/move board player {:time-ms 1200 :endgame 12 :max-depth 6}))

(defn setup []
  (q/frame-rate 60)
  (q/color-mode :rgb)
  (q/smooth)
  (q/text-font (q/create-font "SansSerif" 16 true))
  (host/initial-state play-ai))

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
  (host/update-state state {:job-done? (job-done? state)
                            :job-result (job-result state)
                            :launch-ai launch-ai}))

(defn start! []
  (q/sketch
    :title "Othello"
    :size [layout/window-width layout/window-height]
    :setup setup
    :update update-state
    :draw draw/draw-state
    :mouse-pressed host/on-press
    :mouse-moved host/on-move
    :key-pressed host/on-key
    :middleware [m/fun-mode]
    :features [:keep-on-top]))
