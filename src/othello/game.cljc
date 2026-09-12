(ns othello.game
  (:require [othello.board :as board]
            [othello.rules :as rules]))

(defn new-game
  ([] (new-game board/black))
  ([human]
   {:board (board/initial-board)
    :to-move board/black
    :human human
    :status :in-play
    :last-move nil
    :last-flips []
    :passed? false
    :winner nil
    :moves []
    :history []}))

(defn score [game]
  {:black (board/count-player (:board game) board/black)
   :white (board/count-player (:board game) board/white)})

(defn human? [game]
  (= (:to-move game) (:human game)))

(defn computer-color [game]
  (board/opponent (:human game)))

(defn legal-positions [game]
  (if (= :in-play (:status game))
    (rules/legal-moves (:board game) (:to-move game))
    []))

(defn- snapshot [game]
  (select-keys game [:board :to-move :status :last-move :last-flips
                     :passed? :winner :moves]))

(defn- finish [game]
  (assoc game
    :to-move nil
    :status :over
    :passed? false
    :winner (rules/winner (:board game))))

(defn- after-move [game board player pos flips]
  (let [opponent (board/opponent player)
        opponent-can (rules/has-move? board opponent)
        player-can (rules/has-move? board player)
        played (assoc game
                 :board board
                 :last-move pos
                 :last-flips flips)]
    (cond
      opponent-can (assoc played :to-move opponent :passed? false)
      player-can (assoc played :to-move player :passed? true)
      :else (finish played))))

(defn play [game row col]
  (let [player (:to-move game)
        board (:board game)
        captured (when (= :in-play (:status game))
                   (rules/flips board player row col))]
    (if (seq captured)
      (after-move
        (-> game
            (update :history conj (snapshot game))
            (update :moves conj {:player player :row row :col col}))
        (rules/apply-move board player row col)
        player
        [row col]
        captured)
      game)))

(defn undo [game]
  (if (seq (:history game))
    (merge game (peek (:history game)) {:history (pop (:history game))})
    game))

(defn undo-turn [game]
  (let [undone (undo game)]
    (if (or (= undone game)
            (human? undone))
      undone
      (undo-turn undone))))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? false, :tested-at "2026-09-12T08:46:20.673643-05:00", :module-hash "9cbdb35a8f685789ffd24e6225c8f70a17708f0b66dcbcbca0c3ae9c21277dcb", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "e484b75f66cdd819ebbd386b124280a03f7445a7a2c6b2346fec4e8f0f72c0d8"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "a6f0d287958cf952b5c19786caab2ac859b7966bce1866703ed41ef9bdd8e5b0"} {:id "defn/new-game", :kind "defn", :line 5, :end-line 17, :hash "3bac705976d0048c6608c19158b3ce14859b207402a59f3feecc1d059865a94a"} {:id "defn/score", :kind "defn", :line 19, :end-line 21, :hash "cd96ca0b7a9663961cc412c7ac57d4a9b472844cd8da1b4e99116741ef0d3dc1"} {:id "defn/human?", :kind "defn", :line 23, :end-line 24, :hash "7646dc5def8da11ee4164335a70dfeb195f69814b8365ee2ce629c2fcfcff1a7"} {:id "defn/computer-color", :kind "defn", :line 26, :end-line 27, :hash "7b6565b5f085f6d5dda7da36d173e53bd451ab3f87ba678a81f3d68985b4089a"} {:id "defn/legal-positions", :kind "defn", :line 29, :end-line 32, :hash "1b43161f3eb6870ed36124f7e05a08c56218e36bab056a91b889aeee7f5cda5a"} {:id "defn-/snapshot", :kind "defn-", :line 34, :end-line 36, :hash "6f7d496b0e971c0078301c4a721daa9f277b5133240e5903df05fde94ffc7b4c"} {:id "defn-/finish", :kind "defn-", :line 38, :end-line 43, :hash "d353b2031bba2f6f9b575eb7e16a7ead53ecdfe479b66c01eb79f2cd51845a7c"} {:id "defn-/after-move", :kind "defn-", :line 45, :end-line 56, :hash "435adc3b51294c255849e462a8774bbf31cf67b97e26c484354961ce74457a1d"} {:id "defn/play", :kind "defn", :line 58, :end-line 72, :hash "b4f3c75802de50d6bc4bbd105dbe0841a5ae471a2f90a34d9334dd2ac9c62ecd"} {:id "defn/undo", :kind "defn", :line 74, :end-line 77, :hash "672fce5fb9da31bb9b2abf8349642a197948a8f660afafa16b0557c219616804"} {:id "defn/undo-turn", :kind "defn", :line 79, :end-line 84, :hash "e4bdc0a10e331c00205c8c8eca2fdd1583bee4d6f01db847eaadea8ac40a25c3"}]}
;; clj-mutate-manifest-end
