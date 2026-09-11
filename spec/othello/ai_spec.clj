(ns othello.ai-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]
            [othello.rules :as rules]
            [othello.ai :as ai]
            [othello.ai.eval :as eval]
            [othello.ai.search :as search]
            [othello.spec-helper :refer [parse-board]]))

(describe "evaluation"
  (it "scores an empty board as zero"
    (should= 0 (eval/positional (board/empty-board) board/black))
    (should= 0 (eval/evaluate (board/empty-board) board/black)))

  (it "rewards occupying a corner"
    (let [empty (board/empty-board)
          corner (board/place empty 0 0 board/black)]
      (should= 120 (eval/cell-value corner board/black 0))
      (should= -120 (eval/cell-value corner board/white 0))
      (should (> (eval/positional corner board/black)
                 (eval/positional empty board/black)))))

  (it "penalizes the opponent's corner"
    (let [corner (board/place (board/empty-board) 0 0 board/white)]
      (should (< (eval/positional corner board/black) 0))))

  (it "counts empty squares as zero"
    (should= 0 (eval/cell-value (board/empty-board) board/black 1)))

  (it "gives a huge terminal bonus to the winner"
    (let [board (parse-board
                  "bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   wwwwwwww
                   wwwwwwww
                   wwwwwwww")]
      (should= eval/win-score (eval/terminal board board/black))
      (should= (- eval/win-score) (eval/terminal board board/white))
      (should= eval/win-score (eval/evaluate board board/black))))

  (it "treats a one-disc lead as a win"
    (let [board (parse-board
                  "bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbb.bbbb
                   wwwwwwww
                   wwwwwwww
                   wwwwwwww
                   wwwwwwww")]
      (should= 1 (eval/disc-diff board board/white))
      (should= eval/win-score (eval/terminal board board/white))
      (should= (- eval/win-score) (eval/terminal board board/black))))

  (it "scores a terminal draw as zero"
    (let [board (parse-board
                  "bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   wwwwwwww
                   wwwwwwww
                   wwwwwwww
                   wwwwwwww")]
      (should= eval/draw-score (eval/terminal board board/black))
      (should= eval/draw-score (eval/evaluate board board/black))))

  (it "uses mobility in midgame"
    (let [opening (board/initial-board)
          after-black (rules/apply-move opening board/black 2 3)
          pass-board (parse-board
                       "bw.bbbbb
                        bbbbbbbb
                        bbbbbbbb
                        bbbbbbbb
                        bbbbbbbb
                        bbbbbbbb
                        bbbbbbbb
                        bbbbb.wb")]
      (should= 0 (eval/mobility opening board/black))
      (should= 0 (eval/disc-diff opening board/black))
      (should= 3 (eval/disc-diff after-black board/black))
      (should= 27 (eval/midgame after-black board/black))
      (should= 2 (eval/mobility pass-board board/black))
      (should= -2 (eval/mobility pass-board board/white)))))

(describe "search"
  (it "returns nil when the player has no move"
    (let [board (parse-board
                  "bw.bbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbb.wb")]
      (should= nil (search/choose board board/white {:depth 1}))))

  (it "returns a legal opening move"
    (let [move (search/choose (board/initial-board) board/black {:depth 1})]
      (should-contain move [[2 3] [3 2] [4 5] [5 4]])))

  (it "takes a corner when one is available"
    (let [board (parse-board
                  ".wb.....
                   ........
                   ........
                   ...wb...
                   ...bw...
                   ........
                   ........
                   ........")
          move (search/choose board board/black {:depth 2})]
      (should (rules/legal? board board/black 0 0))
      (should= [0 0] move)))

  (it "solves a short winning endgame"
    (let [board (parse-board
                  "bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbb.wbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb")
          move (search/choose board board/black {:depth 4})]
      (should= [4 3] move)))

  (it "is deterministic at a fixed depth"
    (let [board (board/initial-board)]
      (should= [2 3] (search/choose board board/black {:depth 1}))
      (should= [2 3] (search/choose board board/black {:depth 2}))
      (should= (search/choose board board/black {:depth 2})
               (search/choose board board/black {:depth 2}))))

  (it "orders corner moves before x-squares"
    (let [ordered (search/ordered-moves
                    (parse-board
                      ".wb.....
                       .b......
                       ........
                       ...wb...
                       ...bw...
                       ........
                       ........
                       ........")
                    board/black)]
      (should= [0 0] (first ordered))))

  (it "ends the search at depth zero or a terminal board"
    (should (search/search-end? (board/initial-board) 0))
    (should-not (search/search-end? (board/initial-board) 1))
    (let [full (parse-board
                 "bbbbbbbb
                  bbbbbbbb
                  bbbbbbbb
                  bbbbbbbb
                  wwwwwwww
                  wwwwwwww
                  wwwwwwww
                  wwwwwwww")]
      (should (search/search-end? full 3))))

  (it "searches remaining empties in endgame"
    (let [board (parse-board
                  "bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbb.wbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb")]
      (should= 1 (search/search-depth-limit board {:endgame 12 :max-depth 6}))))

  (it "caps midgame search at max-depth"
    (should= 6 (search/search-depth-limit (board/initial-board)
                                          {:endgame 12 :max-depth 6}))
    (should= 4 (search/search-depth-limit (board/initial-board)
                                          {:endgame 12 :max-depth 4})))

  (it "uses endgame depth when empties equal the threshold"
    (let [board (vec (concat (repeat 52 board/black)
                             (repeat 12 board/empty-cell)))]
      (should= 12 (search/search-depth-limit board {:endgame 12 :max-depth 6}))))

  (it "always searches depth 1 even if the clock has expired"
    (should-not (search/stop-deepening? 1 6 (constantly 100) 0))
    (should (search/stop-deepening? 2 6 (constantly 100) 0))
    (should (search/stop-deepening? 7 6 (constantly 0) 100))
    (should-not (search/stop-deepening? 6 6 (constantly 0) 10000))
    (should (search/stop-deepening? 2 6 (constantly 100) 100)))

  (it "returns exact negamax scores for the opening"
    (let [opening (board/initial-board)]
      (should= 0 (search/negamax opening board/black 0 search/alpha-min search/alpha-max))
      (should= 27 (search/negamax opening board/black 1 search/alpha-min search/alpha-max))
      (should= -24 (search/negamax opening board/black 2 search/alpha-min search/alpha-max))))

  (it "looks ahead past the first ordered reply after d3"
    (let [board (rules/apply-move (board/initial-board) board/black 2 3)]
      (should= [2 2] (search/choose-at-depth board board/white 1))
      (should= [2 2] (search/choose-at-depth board board/white 2))
      (should= [2 4] (search/choose-at-depth board board/white 3))
      (should= 24 (search/negamax board board/white 1 search/alpha-min search/alpha-max))
      (should= -63 (search/negamax board board/white 2 search/alpha-min search/alpha-max))
      (should= 6 (search/negamax board board/white 3 search/alpha-min search/alpha-max))
      (should= [2 4] (search/choose board board/white
                                   {:time-ms 10000
                                    :now (constantly 0)
                                    :max-depth 3
                                    :endgame 1}))))

  (it "searches through a forced pass"
    (let [board (parse-board
                  "bw.bbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbbbbb
                   bbbbb.wb")
          move (search/choose board board/black {:depth 2})]
      (should= [0 2] move)
      (should= 774 (search/negamax board board/black 1 search/alpha-min search/alpha-max))
      (should= -788 (search/negamax board board/white 1 search/alpha-min search/alpha-max))
      (should= -774 (search/negamax board board/white 2 search/alpha-min search/alpha-max))))

  (it "picks the higher scoring of two legal moves"
    (let [board (parse-board
                  ".wb.....
                   ........
                   ........
                   ...wb...
                   ...bw...
                   ........
                   ........
                   .......w")
          move (search/choose board board/black {:depth 1})]
      (should= [0 0] move)))

  (it "uses the wall clock when no clock is injected"
    (should-contain (search/choose (board/initial-board) board/black
                                   {:time-ms 1 :max-depth 1 :endgame 1})
                    [[2 3] [3 2] [4 5] [5 4]]))

  (it "iterative deepening returns a legal move"
    (let [t (atom 0)
          now (fn [] (let [v @t] (swap! t inc) v))
          move (search/choose (board/initial-board) board/black
                              {:time-ms 3 :now now :max-depth 3 :endgame 1})]
      (should-contain move (rules/legal-moves (board/initial-board) board/black))))

  (it "exposes the public AI entry point"
    (let [move (ai/move (board/initial-board) board/black {:depth 1})]
      (should (rules/legal? (board/initial-board) board/black
                            (first move) (second move))))
    (should-contain (ai/move (board/initial-board) board/black)
                    (rules/legal-moves (board/initial-board) board/black))))
