(ns othello.ui.host
  (:require [othello.board :as board]
            [othello.ui.events :as events]))

(defn initial-state [play-ai]
  (events/fresh-ui board/black play-ai))

(defn update-state [state {:keys [job-done? job-result launch-ai]}]
  (let [s (events/on-frame state {:job-done? job-done? :job-result job-result})]
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
