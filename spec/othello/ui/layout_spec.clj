(ns othello.ui.layout-spec
  (:require [speclj.core :refer :all]
            [othello.ui.layout :as layout]))

(describe "layout"
  (it "maps square centers back to board coordinates"
    (doseq [row (range 8)
            col (range 8)]
      (let [[x y] (layout/square-center row col)]
        (should= [row col] (layout/square-at x y)))))

  (it "maps Processing's floating-point mouse coordinates to integer squares"
    (let [[x y] (layout/square-center 2 3)]
      (should= [2 3] (layout/square-at (+ x 0.4) (+ y 0.6)))
      (should= [2 3] (layout/square-at (double x) (double y)))
      (should= java.lang.Long (type (first (layout/square-at (double x) (double y)))))))

  (it "hits the top-left pixel of a1 and misses the pixel left of the board"
    (should= [0 0] (layout/square-at layout/board-left layout/board-top))
    (should= nil (layout/square-at (dec layout/board-left) layout/board-top))
    (should= nil (layout/square-at layout/board-left (dec layout/board-top))))

  (it "hits the bottom-right square and misses the pixel just outside"
    (let [right (+ layout/board-left layout/board-pixels)
          bottom (+ layout/board-top layout/board-pixels)]
      (should= [7 7] (layout/square-at (dec right) (dec bottom)))
      (should= nil (layout/square-at right (dec bottom)))
      (should= nil (layout/square-at (dec right) bottom))))

  (it "names squares in algebraic notation from the top-left"
    (should= "a1" (layout/algebraic 0 0))
    (should= "h1" (layout/algebraic 0 7))
    (should= "a8" (layout/algebraic 7 0))
    (should= "h8" (layout/algebraic 7 7))
    (should= \d (layout/column-label 3))
    (should= "4" (layout/row-label 3)))

  (it "computes square origins from row and column"
    (should= layout/board-left (layout/square-left 0))
    (should= (+ layout/board-left layout/square-size) (layout/square-left 1))
    (should= layout/board-top (layout/square-top 0))
    (should= (+ layout/board-top layout/square-size) (layout/square-top 1)))

  (context "buttons"
    (it "hits each control at its center and misses just outside"
      (doseq [button (layout/buttons)]
        (let [cx (+ (:x button) (quot (:w button) 2))
              cy (+ (:y button) (quot (:h button) 2))]
          (should= (:id button) (:id (layout/button-at cx cy)))
          (should= (:id button) (:id (layout/button-at (:x button) cy)))
          (should= (:id button) (:id (layout/button-at cx (:y button))))
          (should-be-nil (layout/button-at (dec (:x button)) cy))
          (should-be-nil (layout/button-at cx (dec (:y button))))
          (should-be-nil (layout/button-at (+ (:x button) (:w button)) cy))
          (should-be-nil (layout/button-at cx (+ (:y button) (:h button)))))))

    (it "places the sidebar controls at fixed coordinates"
      (let [by-id (fn [id] (first (filter #(= id (:id %)) (layout/buttons))))]
        (should= layout/sidebar-left (:x (by-id :new-game)))
        (should= (+ layout/sidebar-left 186) (:x (by-id :undo)))
        (should= layout/sidebar-left (:x (by-id :play-black)))
        (should= (+ layout/sidebar-left 176) (:x (by-id :play-white)))
        (should= layout/sidebar-left (:x (by-id :hints)))))

    (it "names the five controls"
      (should= [:new-game :undo :play-black :play-white :hints]
               (map :id (layout/buttons))))))
