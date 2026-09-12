(ns othello.ui.view-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]
            [othello.game :as game]
            [othello.ui.events :as events]
            [othello.ui.anim :as anim]
            [othello.ui.layout :as layout]
            [othello.ui.view :as view]
            [othello.spec-helper :refer [click-square drain]]))

(defn ui []
  (events/fresh-ui board/black (fn [_ _] nil)))

(defn square-at [state row col]
  (first (filter #(and (= row (:row %)) (= col (:col %)))
                 (view/squares state))))

(describe "view model"
  (it "shows four opening hints for black"
    (let [state (ui)
          hints (filter :hint (view/squares state))]
      (should= 4 (count hints))
      (should= [[2 3] [3 2] [4 5] [5 4]]
               (map (juxt :row :col) hints))))

  (it "hides hints when they are toggled off"
    (let [state (events/handle-button (ui) :hints)]
      (should-not (:hints? state))
      (should= [] (filter :hint (view/squares state)))
      (should= "Hints: Off" (->> (view/sidebar state) :buttons
                                 (filter #(= :hints (:id %)))
                                 first :label))))

  (it "ghosts a legal square under the pointer"
    (let [[x y] (layout/square-center 2 3)
          state (events/on-mouse-move (ui) x y)
          sq (square-at state 2 3)]
      (should (:ghost sq))
      (should= (view/disc-fill board/black) (:ghost-fill sq))
      (should= :hand (:cursor (view/view-model state)))))

  (it "does not ghost an illegal square"
    (let [[x y] (layout/square-center 0 0)
          state (events/on-mouse-move (ui) x y)]
      (should-not (:ghost (square-at state 0 0)))
      (should= :arrow (:cursor (view/view-model state)))))

  (it "uses a darker fill on odd squares and a flash fill on a miss"
    (should= [20 122 78] (view/square-fill 0 0 false))
    (should= [16 108 68] (view/square-fill 0 1 false))
    (should= [16 108 68] (view/square-fill 1 0 false))
    (should= [20 122 78] (view/square-fill 1 1 false))
    (should= [168 52 42] (view/square-fill 0 0 true)))

  (it "paints black and white discs differently"
    (should= [22 22 24] (view/disc-fill board/black))
    (should= [236 236 228] (view/disc-fill board/white))
    (should= nil (view/disc-paint board/empty-cell))
    (should= (view/disc-fill board/black) (view/disc-paint board/black)))

  (it "keeps the placed disc visible from frame zero while captures wait"
    (let [state (click-square (ui) 2 3)
          anim (:animation state)]
      (should= :animating (:phase state))
      (should= 0 (anim/flips-shown anim))
      (should= board/black (board/cell (view/displayed-board state) 2 3))
      (should= board/white (board/cell (view/displayed-board state) 3 3))))

  (it "reveals the first flipped disc after the place delay"
    (let [state (click-square (ui) 2 3)
          later (assoc-in state [:animation :frame] anim/place-frames)]
      (should= 0 (anim/flips-shown (:animation later)))
      (let [flipping (assoc-in state [:animation :frame]
                               (+ anim/place-frames anim/flip-stagger))]
        (should= 1 (anim/flips-shown (:animation flipping)))
        (should= board/black (board/cell (view/displayed-board flipping) 3 3)))))

  (it "reports scores from the displayed board"
    (should= {:black 2 :white 2} (view/score-view (ui)))
    (let [state (click-square (ui) 2 3)]
      (should= 3 (:black (view/score-view state)))))

  (it "describes whose turn it is"
    (should= "Your turn — click a highlighted square"
             (view/status-text (ui)))
    (should= "Computer is thinking…"
             (view/status-text (assoc (ui) :phase :computer-thinking)))
    (should (:thinking? (view/sidebar (assoc (ui) :phase :computer-thinking))))
    (should= 1 (:think-frames (view/sidebar (assoc (ui) :think-frames 1))))
    (should= 0 (:think-frames (view/sidebar (dissoc (ui) :think-frames))))
    (should= "Flipping discs…"
             (view/status-text (assoc (ui) :phase :animating)))
    (should= "Computer passed"
             (view/status-text (assoc (ui) :phase :pass-notice)))
    (should= "Computer's turn"
             (view/status-text (assoc (ui) :phase :awaiting-computer))))

  (it "announces the winner from the human's point of view"
    (let [g (fn [winner human]
              (assoc (game/new-game human) :status :over :winner winner))]
      (should= "You win" (view/winner-text (g board/black board/black)))
      (should= "Computer wins" (view/winner-text (g board/white board/black)))
      (should= "Draw" (view/winner-text (g :draw board/black)))
      (should= "You win" (view/status-text
                           {:game (g board/black board/black)
                            :phase :game-over}))))

  (it "names the passer from the human's point of view"
    (should= "Computer" (view/passer-name (game/new-game)))
    (should= "You" (view/passer-name (game/new-game board/white))))

  (it "lists algebraic moves and who the human is"
    (let [state (drain (click-square (ui) 2 3))
          bar (view/sidebar state)]
      (should= ["d3"] (:moves bar))
      (should= "Black" (:you-are bar))
      (should= "White" (:computer-is bar))
      (should= "Othello" (:title bar))
      (should-not (:thinking? bar))
      (should= 0 (:think-frames bar))))

  (it "marks the last move during and after animation"
    (let [animating (click-square (ui) 2 3)]
      (should (:last-move? (square-at animating 2 3)))
      (should (:last-move? (square-at (drain animating) 2 3)))))

  (it "marks a flashed illegal square"
    (let [state (click-square (ui) 0 0)]
      (should (:flash? (square-at state 0 0)))
      (should-not (:flash? (square-at state 2 3))))))
