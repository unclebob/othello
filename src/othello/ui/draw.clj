(ns othello.ui.draw
  (:require [clojure.string :as str]
            [quil.core :as q]
            [othello.board :as board]
            [othello.ui.layout :as layout]
            [othello.ui.view :as view]))

(defn- rgb
  ([color]
   (apply q/fill color))
  ([color alpha]
   (apply q/fill (conj (vec color) alpha))))

(defn draw-background []
  (q/background 22 28 32))

(defn draw-board-frame []
  (q/no-stroke)
  (q/fill 12 64 44)
  (q/rect (- layout/board-left 18)
          (- layout/board-top 18)
          (+ layout/board-pixels 36)
          (+ layout/board-pixels 36)
          12))

(defn draw-square-base [sq]
  (q/no-stroke)
  (rgb (:fill sq))
  (q/rect (:x sq) (:y sq) (:size sq) (:size sq)))

(defn draw-hint [sq]
  (when (:hint sq)
    (q/no-stroke)
    (q/fill 232 232 214 180)
    (q/ellipse (:cx sq) (:cy sq) 14 14)))

(defn draw-disc-shape [cx cy fill-color alpha]
  (q/no-stroke)
  (q/fill 8 12 10 (int (* 0.35 alpha)))
  (q/ellipse (+ cx 3) (+ cy 4) (* 2 layout/disc-radius) (* 2 layout/disc-radius))
  (rgb fill-color alpha)
  (q/ellipse cx cy (* 2 layout/disc-radius) (* 2 layout/disc-radius))
  (q/no-fill)
  (q/stroke 255 255 255 (int (* 0.18 alpha)))
  (q/stroke-weight 2)
  (q/ellipse cx cy (* 2 layout/disc-radius) (* 2 layout/disc-radius))
  (q/no-stroke)
  (q/fill 255 255 255 (int (* 0.14 alpha)))
  (q/ellipse (- cx 8) (- cy 9) 18 12))

(defn draw-disc [sq]
  (when-let [fill (:disc-fill sq)]
    (draw-disc-shape (:cx sq) (:cy sq) fill 255)))

(defn draw-ghost [sq]
  (when (:ghost sq)
    (draw-disc-shape (:cx sq) (:cy sq) (:ghost-fill sq) 110)))

(defn draw-last [sq]
  (when (:last-move? sq)
    (q/no-fill)
    (q/stroke 232 196 72)
    (q/stroke-weight 3)
    (q/ellipse (:cx sq) (:cy sq) 62 62)
    (q/no-stroke)))

(defn draw-square [sq]
  (draw-square-base sq)
  (draw-hint sq)
  (draw-disc sq)
  (draw-ghost sq)
  (draw-last sq))

(defn draw-coordinates []
  (q/fill 198 208 196)
  (q/text-align :center :center)
  (q/text-size 16)
  (doseq [col (range board/size)]
    (q/text (str (layout/column-label col))
            (first (layout/square-center 0 col))
            (- layout/board-top 28)))
  (doseq [row (range board/size)]
    (q/text (layout/row-label row)
            (- layout/board-left 26)
            (second (layout/square-center row 0)))))

(defn- score-block [label count x y disc-player]
  (q/fill 230 230 224)
  (q/text-align :left :center)
  (q/text-size 20)
  (q/text label x y)
  (draw-disc-shape (+ x 132) y (view/disc-fill disc-player) 255)
  (q/fill 230 230 224)
  (q/text-align :left :center)
  (q/text-size 28)
  (q/text (str count) (+ x 168) y))

(defn- draw-button [button]
  (q/no-stroke)
  (if (:hovered button)
    (q/fill 52 118 96)
    (q/fill 38 78 68))
  (q/rect (:x button) (:y button) (:w button) (:h button) 8)
  (q/fill 236 236 228)
  (q/text-align :center :center)
  (q/text-size 16)
  (q/text (:label button)
          (+ (:x button) (quot (:w button) 2))
          (+ (:y button) (quot (:h button) 2))))

(defn- thinking-dot [sidebar]
  (when (:thinking? sidebar)
    (let [pulse (Math/abs (Math/sin (/ (:think-frames sidebar) 8.0)))]
      (q/fill 232 196 72 (int (+ 80 (* 140 pulse))))
      (q/ellipse (+ layout/sidebar-left 320) 118 12 12))))

(defn- draw-moves [moves]
  (q/fill 180 188 178)
  (q/text-align :left :top)
  (q/text-size 14)
  (q/text (str "Moves: " (str/join "  " (take-last 12 moves)))
          layout/sidebar-left
          430))

(defn draw-sidebar [sidebar]
  (q/fill 236 236 228)
  (q/text-align :left :top)
  (q/text-size 36)
  (q/text (:title sidebar) layout/sidebar-left 40)
  (q/text-size 18)
  (q/fill 210 216 204)
  (q/text (:status sidebar) layout/sidebar-left 96)
  (thinking-dot sidebar)
  (score-block "Black" (:black-score sidebar) layout/sidebar-left 170 board/black)
  (score-block "White" (:white-score sidebar) layout/sidebar-left 230 board/white)
  (q/fill 210 216 204)
  (q/text-align :left :center)
  (q/text-size 16)
  (q/text (str "You are " (:you-are sidebar)) layout/sidebar-left 300)
  (q/text (str "Computer is " (:computer-is sidebar)) layout/sidebar-left 330)
  (q/text "N new   U undo   H hints" layout/sidebar-left 380)
  (q/text "1 play black   2 play white" layout/sidebar-left 404)
  (draw-moves (:moves sidebar))
  (doseq [button (:buttons sidebar)]
    (draw-button button)))

(defn apply-cursor [cursor]
  (if (= cursor :hand)
    (q/cursor :hand)
    (q/cursor :arrow)))

(defn draw-state [state]
  (let [vm (view/view-model state)]
    (q/ellipse-mode :center)
    (apply-cursor (:cursor vm))
    (draw-background)
    (draw-board-frame)
    (doseq [sq (:squares vm)]
      (draw-square sq))
    (draw-coordinates)
    (draw-sidebar (:sidebar vm))))
