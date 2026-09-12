(ns othello.ui.host-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]
            [othello.ui.host :as host]
            [othello.ui.layout :as layout]))

(defn ui []
  (host/initial-state (fn [_ _] nil)))

(describe "host"
  (it "starts a black human with the shared UI state"
    (let [state (ui)]
      (should= :awaiting-human (:phase state))
      (should= board/black (:human (:game state)))))

  (it "ignores a right click"
    (let [state (ui)]
      (should= state (host/on-press state {:button :right :x 1 :y 1}))))

  (it "forwards a board press"
    (let [[x y] (layout/square-center 2 3)
          state (host/on-press (ui) {:button :left :x x :y y})]
      (should= :animating (:phase state))))

  (it "tracks the pointer"
    (should= [10 20] (:pointer (host/on-move (ui) {:x 10 :y 20}))))

  (it "forwards keys"
    (should-not (:hints? (host/on-key (ui) {:key :h}))))

  (it "launches an AI job when the computer needs one"
    (let [launched (atom false)
          thinking (assoc (ui)
                     :phase :computer-thinking
                     :ai-job nil
                     :animation nil)
          next (host/update-state thinking
                                  {:job-done? false
                                   :job-result nil
                                   :launch-ai (fn [s]
                                                (reset! launched true)
                                                (assoc s :ai-job :job))})]
      (should @launched)
      (should= :job (:ai-job next))))

  (it "does not launch when a job is already attached"
    (let [thinking (assoc (ui) :phase :computer-thinking :ai-job :pending)
          next (host/update-state thinking
                                  {:job-done? false
                                   :job-result nil
                                   :launch-ai (fn [_] (throw (Exception. "launched")))})]
      (should= :pending (:ai-job next)))))
