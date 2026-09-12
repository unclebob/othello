(ns othello.ui.view
  (:require [othello.board :as board]
            [othello.game :as game]
            [othello.ui.layout :as layout]))

(def place-frames 8)
(def flip-stagger 4)
(def hold-frames 10)
(def min-think-frames 15)
(def pass-display-frames 90)

(defn flips-shown [anim]
  (let [raw (quot (- (:frame anim) place-frames) flip-stagger)]
    (max 0 raw)))

(defn animation-done? [anim]
  (>= (:frame anim)
      (+ place-frames (* flip-stagger (count (:flips anim))) hold-frames)))

(defn animated-board [board anim]
  (let [player (:player anim)
        placed (board/place board (:row anim) (:col anim) player)
        shown (take (flips-shown anim) (:flips anim))]
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

(defn square-model [state row col]
  (let [board (displayed-board state)
        disc (board/cell board row col)
        pos [row col]
        hover (hover-square state)
        last (last-move-of state)
        legal (contains? (legal-set state) pos)
        flash? (= pos (:flash-pos state))
        [cx cy] (layout/square-center row col)
        show-hint (and (:hints? state)
                       legal
                       (= :awaiting-human (:phase state)))]
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
     :ghost (and (hovered-legal? state) (= pos hover))
     :ghost-fill (disc-fill (:to-move (:game state)))
     :hint show-hint
     :last-move? (= pos last)
     :flash? flash?}))

(defn squares [state]
  (map (fn [[row col]] (square-model state row col))
       (board/squares)))

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
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:30:50.634327-05:00", :module-hash "ded204b85ae966ada531af79ad43ba5da0a98049cc8dd9a247e26efbd8a8a9ff", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "4a118879430717b1bd26e1af198359854cfcb88dcd8f5ce51f8f471a0d516e8e"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 4, :hash "137b63f6c0503d27e964db160e2eb4953b0372e739d1100f930e08b0c3b07829"} {:id "def/place-frames", :kind "def", :line 6, :end-line 6, :hash "f96e3c60a2925b8cee9b418ddec3f49bc608607779d8c207eb5cc74941e78d20"} {:id "def/flip-stagger", :kind "def", :line 7, :end-line 7, :hash "21335fba39896815f59e2e51d7358de1cb63b7b6386667997e132b0f4509f0a7"} {:id "def/hold-frames", :kind "def", :line 8, :end-line 8, :hash "ebcf69ff871defff5b39ac25a6ec7535d55bbbea89ce3867db488a5b5516c0d8"} {:id "def/min-think-frames", :kind "def", :line 9, :end-line 9, :hash "a7a6bb949dc92e912c4e1657b330a451a87291989a13fcf92fddd72a8f31ab97"} {:id "def/pass-display-frames", :kind "def", :line 10, :end-line 10, :hash "78b356eb6973a67d7ea4047cf76b5ad9c0e52eaf5071f0e021a43cb523debc4d"} {:id "defn/flips-shown", :kind "defn", :line 12, :end-line 14, :hash "8753168826d333dded1368248121915c5eaf93c583b5359619225ed6a76059ff"} {:id "defn/animation-done?", :kind "defn", :line 16, :end-line 18, :hash "88bf5755c74bb38be0fe0e4ba9c830c8df1b7c51bb2b68b328c1feefa8c18b90"} {:id "defn/animated-board", :kind "defn", :line 20, :end-line 26, :hash "b2e8c1e3bac3c372f7e49e560702cdc9d590d7cbbed8899e5955884379540e58"} {:id "defn/displayed-board", :kind "defn", :line 28, :end-line 31, :hash "3408ce1f64fe4328eae024b2005899ba54dde52ffb44208289f1cae0cfe5b74e"} {:id "defn/winner-text", :kind "defn", :line 33, :end-line 39, :hash "cfed17a28be7182bee4c43a2707f9f10d1e01553166524d47464d4501c25d1b4"} {:id "defn/passer-name", :kind "defn", :line 41, :end-line 44, :hash "b085ac91964a589bbbce48160d7460ce78eafead60337e1209a3a54d74ca1d58"} {:id "defn/status-text", :kind "defn", :line 46, :end-line 55, :hash "d820249380f0ae52fd83273188cf0c0db1d4516c9968a1110aa4b405b6206bcb"} {:id "defn/score-view", :kind "defn", :line 57, :end-line 60, :hash "550932c423b0fce4c642211530fc9d3497d374a01493146277854551ca9f5eb8"} {:id "defn/hover-square", :kind "defn", :line 62, :end-line 65, :hash "a40af8caa95f5f153c6ee3a349136abc57fe7b8ff279798affcde8c0a564f298"} {:id "defn/legal-set", :kind "defn", :line 67, :end-line 68, :hash "08927010ceb4a0e269427424d849307b983ae1908638b338684c68a77e7ec510"} {:id "defn/hovered-legal?", :kind "defn", :line 70, :end-line 74, :hash "ce1929f5e825abac824d59518910466b3cb40d88320752fc8dd4c72dcdb10cd9"} {:id "defn/last-move-of", :kind "defn", :line 76, :end-line 79, :hash "64794726968714ac48a9418c145180b24e4f28a9cdd8a4de0c62c22cee9e4343"} {:id "defn/square-fill", :kind "defn", :line 81, :end-line 86, :hash "d3fd06e8e9489ed19e4ec1d281b8dffa308de31a871f7ddd3a057baffbc3fe98"} {:id "defn/disc-fill", :kind "defn", :line 88, :end-line 91, :hash "72cc0a24e560c79b5616d391e277751d708e0cfbae5249bfeb083ab8ccbe2808"} {:id "defn/disc-paint", :kind "defn", :line 93, :end-line 96, :hash "e2c257576267285e5fcf8f5290656ea7e93566af73ccfca905a23a56c78b3e4b"} {:id "defn/square-model", :kind "defn", :line 98, :end-line 124, :hash "779c97cada593b68b27ce1f6221053840a445978e8dcc252189c4682ae05065f"} {:id "defn/squares", :kind "defn", :line 126, :end-line 128, :hash "6d7e97bdf65ed700f23cb0889170dab33db81ed1849abf8bfc2234066224c2b4"} {:id "defn/move-list", :kind "defn", :line 130, :end-line 132, :hash "ee15d4b0f7bd06e89e4a41cb54734a0c4112045906bf15dc9105155d65f79519"} {:id "defn/button-model", :kind "defn", :line 134, :end-line 144, :hash "d10e85e11477d9246e6761739a59a16c7d0db213ad9cc72f64aac637e0a8c344"} {:id "defn/sidebar", :kind "defn", :line 146, :end-line 159, :hash "51cb933d9488955141ff0bc02210a9346126cb48f5ab416cf856446885989396"} {:id "defn/view-model", :kind "defn", :line 161, :end-line 165, :hash "b465e6e2c5bce1d6df7fa8dcfdda2a54165d92a9e41595a5725e7e8a8c4597c1"}]}
;; clj-mutate-manifest-end
