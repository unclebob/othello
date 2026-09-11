(ns othello.game-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]
            [othello.game :as game]
            [othello.rules :as rules]
            [othello.spec-helper :refer [parse-board]]))

(describe "game"
  (it "starts with black to move and the human playing black"
    (let [g (game/new-game)]
      (should= board/black (:to-move g))
      (should= board/black (:human g))
      (should= :in-play (:status g))
      (should (game/human? g))
      (should= board/white (game/computer-color g))
      (should-not (:passed? g))
      (should= {:black 2 :white 2} (game/score g))
      (should= [[2 3] [3 2] [4 5] [5 4]] (game/legal-positions g))))

  (it "can start with the human playing white"
    (let [g (game/new-game board/white)]
      (should= board/white (:human g))
      (should= board/black (game/computer-color g))
      (should-not (game/human? g))))

  (it "plays a legal move and switches to white"
    (let [g (game/play (game/new-game) 2 3)]
      (should= board/white (:to-move g))
      (should= [2 3] (:last-move g))
      (should= [[3 3]] (:last-flips g))
      (should-not (:passed? g))
      (should= board/black (board/cell (:board g) 2 3))
      (should= board/black (board/cell (:board g) 3 3))
      (should= {:black 4 :white 1} (game/score g))
      (should= 1 (count (:moves g)))
      (should= {:player board/black :row 2 :col 3} (first (:moves g)))))

  (it "ignores an illegal move"
    (let [g (game/new-game)
          next (game/play g 0 0)]
      (should= g next)
      (should= board/black (:to-move next))))

  (it "ignores a move after the game is over"
    (let [g (assoc (game/new-game)
              :status :over
              :to-move nil
              :winner :draw)
          next (game/play g 2 3)]
      (should= :over (:status next))
      (should= [] (game/legal-positions g))))

  (it "undoes a single ply"
    (let [g (game/play (game/new-game) 2 3)
          undone (game/undo g)]
      (should= board/black (:to-move undone))
      (should= [] (:moves undone))
      (should= (board/initial-board) (:board undone))))

  (it "undo on a new game is a no-op"
    (let [g (game/new-game)]
      (should= g (game/undo g))
      (should= g (game/undo-turn g))))

  (it "undo-turn rewinds until the human is to move"
    (let [g (-> (game/new-game)
                (game/play 2 3)
                (game/play 2 2))
          undone (game/undo-turn g)]
      (should= board/black (:to-move undone))
      (should (game/human? undone))
      (should= (board/initial-board) (:board undone))))

  (context "passing"
    (it "lets the same player move again when the opponent has no move"
      (let [board (parse-board
                    "bw.bbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbb.wb")
            g (assoc (game/new-game) :board board :to-move board/black)
            next (game/play g 0 2)]
        (should (rules/legal? board board/black 0 2))
        (should (rules/legal? board board/black 7 5))
        (should-not (rules/has-move? board board/white))
        (should-not (rules/has-move? (:board next) board/white))
        (should (rules/has-move? (:board next) board/black))
        (should= board/black (:to-move next))
        (should (:passed? next))))

    (it "ends the game when neither player can move after a play"
      (let [board (parse-board
                    "bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbb.wbbb
                     bbbbbbbb
                     bbbbbbbb
                     bbbbbbbb")
            g (assoc (game/new-game) :board board :to-move board/black)
            next (game/play g 4 3)]
        (should= :over (:status next))
        (should= board/black (:winner next))
        (should= nil (:to-move next))
        (should-not (:passed? next))))))
