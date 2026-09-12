(ns othello.ui.view
  (:require [othello.board :as board]
            [othello.game :as game]
            [othello.ui.anim :as anim]
            [othello.ui.layout :as layout]))

(defn animated-board [board animation]
  (let [player (:player animation)
        placed (board/place board (:row animation) (:col animation) player)
        shown (take (anim/flips-shown animation) (:flips animation))]
    (reduce (fn [b [r c]] (board/place b r c player))
            placed
            shown)))

(defn displayed-board [state]
  (if-let [anim (:animation state)]
    (animated-board (:board (:game state)) anim)
    (:board (:game state))))

(defn winner-text [game]
  (let [winner (:winner game)
        human (:human game)]
    (cond
      (= winner :draw) "Draw"
      (= winner human) "You win"
      :else "Computer wins")))

(defn passer-name [game]
  (if (game/human? game)
    "Computer"
    "You"))

(defn status-text [state]
  (let [phase (:phase state)
        game (:game state)]
    (cond
      (= :over (:status game)) (winner-text game)
      (= :computer-thinking phase) "Computer is thinking…"
      (= :animating phase) "Flipping discs…"
      (= :pass-notice phase) (str (passer-name game) " passed")
      (= :awaiting-computer phase) "Computer's turn"
      :else "Your turn — click a highlighted square")))

(defn score-view [state]
  (let [board (displayed-board state)]
    {:black (board/count-player board board/black)
     :white (board/count-player board board/white)}))

(defn hover-square [state]
  (if-let [[x y] (:pointer state)]
    (layout/square-at x y)
    nil))

(defn legal-set [state]
  (set (game/legal-positions (:game state))))

(defn hovered-legal? [state]
  (let [pos (hover-square state)
        phase (:phase state)]
    (and (= :awaiting-human phase)
         (contains? (legal-set state) pos))))

(defn last-move-of [state]
  (if-let [anim (:animation state)]
    [(:row anim) (:col anim)]
    (:last-move (:game state))))

(defn square-fill [row col flash?]
  (if flash?
    [168 52 42]
    (if (even? (bit-xor row col))
      [20 122 78]
      [16 108 68])))

(defn disc-fill [player]
  (if (= player board/black)
    [22 22 24]
    [236 236 228]))

(defn disc-paint [disc]
  (if (= disc board/empty-cell)
    nil
    (disc-fill disc)))

(defn- square-frame [state]
  (let [hover (hover-square state)
        legal (legal-set state)
        human-turn? (= :awaiting-human (:phase state))]
    {:board (displayed-board state)
     :hover hover
     :last (last-move-of state)
     :legal legal
     :ghost? (and human-turn? (contains? legal hover))
     :hints? (:hints? state)
     :human-turn? human-turn?
     :flash-pos (:flash-pos state)
     :to-move (:to-move (:game state))}))

(defn- square-model [frame row col]
  (let [{:keys [board hover last legal ghost? hints? human-turn?
                flash-pos to-move]} frame
        disc (board/cell board row col)
        pos [row col]
        flash? (= pos flash-pos)
        [cx cy] (layout/square-center row col)]
    {:row row
     :col col
     :x (layout/square-left col)
     :y (layout/square-top row)
     :size layout/square-size
     :cx cx
     :cy cy
     :fill (square-fill row col flash?)
     :disc disc
     :disc-fill (disc-paint disc)
     :ghost (and ghost? (= pos hover))
     :ghost-fill (disc-fill to-move)
     :hint (and hints? human-turn? (contains? legal pos))
     :last-move? (= pos last)
     :flash? flash?}))

(defn squares [state]
  (let [frame (square-frame state)]
    (map (fn [[row col]] (square-model frame row col))
         (board/squares))))

(defn move-list [state]
  (map (fn [m] (layout/algebraic (:row m) (:col m)))
       (:moves (:game state))))

(defn button-model [state button]
  (let [pointer (or (:pointer state) [-1 -1])
        hovered (layout/inside? (first pointer) (second pointer) button)
        id (:id button)
        label (if (= id :hints)
                (if (:hints? state) "Hints: On" "Hints: Off")
                (:label button))]
    (assoc button
      :label label
      :hovered hovered)))

(defn sidebar [state]
  (let [game (:game state)
        scores (score-view state)]
    {:title "Othello"
     :status (status-text state)
     :black-score (:black scores)
     :white-score (:white scores)
     :you-are (if (= board/black (:human game)) "Black" "White")
     :computer-is (if (= board/black (:human game)) "White" "Black")
     :thinking? (= :computer-thinking (:phase state))
     :think-frames (:think-frames state 0)
     :moves (move-list state)
     :buttons (map #(button-model state %) (layout/buttons))
     :over? (= :over (:status game))}))

(defn view-model [state]
  {:squares (squares state)
   :sidebar (sidebar state)
   :hover-square (hover-square state)
   :cursor (if (hovered-legal? state) :hand :arrow)})

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? false, :tested-at "2026-09-12T08:46:24.69556-05:00", :module-hash "0e3c55a694fd785e35551fff1854e02828fbbd868eecefa4766af0357259d49f", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "e484b75f66cdd819ebbd386b124280a03f7445a7a2c6b2346fec4e8f0f72c0d8"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 5, :hash "3aec376175c9a2b23b3968542aea16a4a63f64ebdfe257ff916b5d4cf5c436cd"} {:id "defn/animated-board", :kind "defn", :line 7, :end-line 13, :hash "e8e292fd8fa650ab5e1875aaeca01a2fdd1c6a67a4feaccc2c69e4c22b913f22"} {:id "defn/displayed-board", :kind "defn", :line 15, :end-line 18, :hash "3408ce1f64fe4328eae024b2005899ba54dde52ffb44208289f1cae0cfe5b74e"} {:id "defn/winner-text", :kind "defn", :line 20, :end-line 26, :hash "cfed17a28be7182bee4c43a2707f9f10d1e01553166524d47464d4501c25d1b4"} {:id "defn/passer-name", :kind "defn", :line 28, :end-line 31, :hash "b085ac91964a589bbbce48160d7460ce78eafead60337e1209a3a54d74ca1d58"} {:id "defn/status-text", :kind "defn", :line 33, :end-line 42, :hash "d820249380f0ae52fd83273188cf0c0db1d4516c9968a1110aa4b405b6206bcb"} {:id "defn/score-view", :kind "defn", :line 44, :end-line 47, :hash "550932c423b0fce4c642211530fc9d3497d374a01493146277854551ca9f5eb8"} {:id "defn/hover-square", :kind "defn", :line 49, :end-line 52, :hash "a40af8caa95f5f153c6ee3a349136abc57fe7b8ff279798affcde8c0a564f298"} {:id "defn/legal-set", :kind "defn", :line 54, :end-line 55, :hash "08927010ceb4a0e269427424d849307b983ae1908638b338684c68a77e7ec510"} {:id "defn/hovered-legal?", :kind "defn", :line 57, :end-line 61, :hash "ce1929f5e825abac824d59518910466b3cb40d88320752fc8dd4c72dcdb10cd9"} {:id "defn/last-move-of", :kind "defn", :line 63, :end-line 66, :hash "64794726968714ac48a9418c145180b24e4f28a9cdd8a4de0c62c22cee9e4343"} {:id "defn/square-fill", :kind "defn", :line 68, :end-line 73, :hash "d3fd06e8e9489ed19e4ec1d281b8dffa308de31a871f7ddd3a057baffbc3fe98"} {:id "defn/disc-fill", :kind "defn", :line 75, :end-line 78, :hash "72cc0a24e560c79b5616d391e277751d708e0cfbae5249bfeb083ab8ccbe2808"} {:id "defn/disc-paint", :kind "defn", :line 80, :end-line 83, :hash "e2c257576267285e5fcf8f5290656ea7e93566af73ccfca905a23a56c78b3e4b"} {:id "defn-/square-frame", :kind "defn-", :line 85, :end-line 97, :hash "452588a141565c6cee1f0cebc13e83647324de98a240e4d6e6a45f01712bb485"} {:id "defn-/square-model", :kind "defn-", :line 99, :end-line 120, :hash "f0f7437932b6d540f99e62315b670ea9a1f95333e13f08a02d0e930eda7f0e4f"} {:id "defn/squares", :kind "defn", :line 122, :end-line 125, :hash "43ec9dd1b5ff1efd4a6a799801ea029128af76a42bbf85af5c105e24e6d056e1"} {:id "defn/move-list", :kind "defn", :line 127, :end-line 129, :hash "ee15d4b0f7bd06e89e4a41cb54734a0c4112045906bf15dc9105155d65f79519"} {:id "defn/button-model", :kind "defn", :line 131, :end-line 140, :hash "1b35d72a6ebea8f4a2c58415035bc95ba6584feabcd2205270cf5befea34a760"} {:id "defn/sidebar", :kind "defn", :line 142, :end-line 155, :hash "51cb933d9488955141ff0bc02210a9346126cb48f5ab416cf856446885989396"} {:id "defn/view-model", :kind "defn", :line 157, :end-line 161, :hash "b465e6e2c5bce1d6df7fa8dcfdda2a54165d92a9e41595a5725e7e8a8c4597c1"}]}
;; clj-mutate-manifest-end
