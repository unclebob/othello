(ns othello.board-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]))

(describe "board"
  (it "has eight rows and columns"
    (should= 8 board/size)
    (should= 64 (count (board/empty-board)))
    (should= 64 (count (board/squares))))

  (it "maps row and column onto a unique index"
    (should= 0 (board/index 0 0))
    (should= 7 (board/index 0 7))
    (should= 8 (board/index 1 0))
    (should= 9 (board/index 1 1))
    (should= 63 (board/index 7 7)))

  (context "bounds"
    (it "includes the four corners"
      (should (board/in-bounds? 0 0))
      (should (board/in-bounds? 0 7))
      (should (board/in-bounds? 7 0))
      (should (board/in-bounds? 7 7)))

    (it "excludes squares just outside each edge"
      (should-not (board/in-bounds? -1 0))
      (should-not (board/in-bounds? 0 -1))
      (should-not (board/in-bounds? 8 0))
      (should-not (board/in-bounds? 0 8))
      (should-not (board/in-bounds? 8 8))
      (should-not (board/in-bounds? -1 -1))))

  (context "players"
    (it "treats white as black's opponent"
      (should= board/white (board/opponent board/black)))

    (it "treats black as white's opponent"
      (should= board/black (board/opponent board/white))))

  (context "initial position"
    (it "places four discs in the center"
      (let [b (board/initial-board)]
        (should= board/white (board/cell b 3 3))
        (should= board/black (board/cell b 3 4))
        (should= board/black (board/cell b 4 3))
        (should= board/white (board/cell b 4 4))
        (should= 2 (board/count-player b board/black))
        (should= 2 (board/count-player b board/white))
        (should= 60 (board/count-player b board/empty-cell))))

    (it "leaves every other square empty"
      (let [b (board/initial-board)]
        (should (board/empty-square? b 0 0))
        (should (board/empty-square? b 2 3))
        (should (board/occupied? b 3 3))
        (should-not (board/empty-square? b 3 3)))))

  (context "place"
    (it "puts a disc on an empty square without moving others"
      (let [b (board/place (board/empty-board) 2 5 board/black)]
        (should= board/black (board/cell b 2 5))
        (should (board/empty-square? b 2 4))
        (should (board/empty-square? b 2 6)))))

  (it "lists eight compass directions"
    (should= 8 (count board/directions))
    (should-contain [0 1] board/directions)
    (should-contain [0 -1] board/directions)
    (should-contain [1 0] board/directions)
    (should-contain [-1 0] board/directions)
    (should-contain [1 1] board/directions)
    (should-contain [1 -1] board/directions)
    (should-contain [-1 1] board/directions)
    (should-contain [-1 -1] board/directions)))
