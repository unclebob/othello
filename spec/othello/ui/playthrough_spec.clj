(ns othello.ui.playthrough-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]
            [othello.game :as game]
            [othello.rules :as rules]
            [othello.ui.events :as events]
            [othello.ui.layout :as layout]
            [othello.ui.view :as view]
            [othello.spec-helper :refer [click-square click-button drain
                                         parse-board play-at let-computer-play
                                         play-turn]]))

(defn first-move [board player]
  (first (rules/legal-moves board player)))

(defn ui []
  (events/fresh-ui board/black first-move))

(defn human-ready? [state]
  (and (= :awaiting-human (:phase state))
       (game/human? (:game state))
       (= :in-play (:status (:game state)))))

(describe "UI playthrough"
  (it "plays a legal opening click into a computer reply"
    (let [after-human (play-at (ui) 2 3)
          vm (view/view-model after-human)
          after-computer (let-computer-play after-human)]
      (should (events/awaiting-computer? after-human))
      (should= 4 (:black-score (:sidebar vm)))
      (should= 1 (:white-score (:sidebar vm)))
      (should= "Computer's turn" (view/status-text after-human))
      (should (human-ready? after-computer))
      (should= 2 (count (:moves (:game after-computer))))
      (should= board/white (board/cell (:board (:game after-computer))
                                       (first (:last-move (:game after-computer)))
                                       (second (:last-move (:game after-computer)))))))

  (it "rejects an illegal click then accepts the highlighted square"
    (let [miss (click-square (ui) 0 0)
          recovered (play-at miss 3 2)]
      (should= [0 0] (:flash-pos miss))
      (should= :awaiting-human (:phase miss))
      (should (events/awaiting-computer? recovered))
      (should= [3 2] (:last-move (:game recovered)))))

  (it "keeps the board inert while discs are flipping"
    (let [animating (click-square (ui) 2 3)
          [x y] (layout/square-center 3 2)
          ignored (events/on-click animating x y)]
      (should= :animating (:phase animating))
      (should= :animating (:phase ignored))
      (should= 2 (:row (:animation ignored)))))

  (it "can start as white so the computer opens"
    (let [state (click-button (ui) :play-white)
          after-open (let-computer-play (drain state))]
      (should= board/white (:human (:game after-open)))
      (should (human-ready? after-open))
      (should= board/black (:player (first (:moves (:game after-open)))))
      (should= "White" (:you-are (view/sidebar after-open)))))

  (it "plays several full turns through the same click path as the sketch"
    (let [state (reduce (fn [s _]
                          (let [ready (drain s)
                                pos (first (game/legal-positions (:game ready)))]
                            (play-turn ready (first pos) (second pos))))
                        (ui)
                        (range 4))
          scores (game/score (:game state))]
      (should (human-ready? state))
      (should= 8 (count (:moves (:game state))))
      (should (< 2 (:black scores)))
      (should (< 2 (:white scores)))))

  (it "new game restores the opening position after play"
    (let [state (-> (ui)
                    (play-turn 2 3)
                    (click-button :new-game))]
      (should= (board/initial-board) (:board (:game state)))
      (should= :awaiting-human (:phase state))
      (should= "Your turn — click a highlighted square"
               (view/status-text state))))

  (it "shows a game-over overlay when the board is finished through the UI"
    (let [board (parse-board
                  "bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbb.wbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb")
          state (assoc-in (ui) [:game :board] board)
          done (play-at state 4 3)
          bar (:sidebar (view/view-model done))]
      (should= :over (:status (:game done)))
      (should= :game-over (:phase done))
      (should= "You win" (:status bar))
      (should (:over? bar)))))
