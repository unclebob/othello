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
    :passed? (boolean false)
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

(defn- finish [game board pos flips]
  (assoc game
    :board board
    :to-move nil
    :status :over
    :last-move pos
    :last-flips flips
    :passed? (boolean false)
    :winner (rules/winner board)))

(defn- after-move [game board player pos flips]
  (let [opponent (board/opponent player)
        opponent-can (rules/has-move? board opponent)
        player-can (rules/has-move? board player)]
    (cond
      opponent-can (assoc game
                     :board board
                     :to-move opponent
                     :last-move pos
                     :last-flips flips
                     :passed? (boolean false))
      player-can (assoc game
                   :board board
                   :to-move player
                   :last-move pos
                   :last-flips flips
                   :passed? (boolean true))
      :else (finish game board pos flips))))

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
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:19:01.441191-05:00", :module-hash "5658f22ad5cb0dceee0b4bf09a9f681a77ab157276bf0240d324913700bd95d0", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "9670c68e9bb7a7f42b22717c734382c09424b649b7ac4d64ebfa805fe0c0e627"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "a6f0d287958cf952b5c19786caab2ac859b7966bce1866703ed41ef9bdd8e5b0"} {:id "defn/new-game", :kind "defn", :line 5, :end-line 17, :hash "4a10e756099a10a3883eff321da9fae22568b7cc772d8bbff506af8faa7af63b"} {:id "defn/score", :kind "defn", :line 19, :end-line 21, :hash "cd96ca0b7a9663961cc412c7ac57d4a9b472844cd8da1b4e99116741ef0d3dc1"} {:id "defn/human?", :kind "defn", :line 23, :end-line 24, :hash "7646dc5def8da11ee4164335a70dfeb195f69814b8365ee2ce629c2fcfcff1a7"} {:id "defn/computer-color", :kind "defn", :line 26, :end-line 27, :hash "7b6565b5f085f6d5dda7da36d173e53bd451ab3f87ba678a81f3d68985b4089a"} {:id "defn/legal-positions", :kind "defn", :line 29, :end-line 32, :hash "1b43161f3eb6870ed36124f7e05a08c56218e36bab056a91b889aeee7f5cda5a"} {:id "defn-/snapshot", :kind "defn-", :line 34, :end-line 36, :hash "6f7d496b0e971c0078301c4a721daa9f277b5133240e5903df05fde94ffc7b4c"} {:id "defn-/finish", :kind "defn-", :line 38, :end-line 46, :hash "27449a35d604883ec433ac97b8f9f49f8d71a7942de506391f5181a4acb6033b"} {:id "defn-/after-move", :kind "defn-", :line 48, :end-line 65, :hash "d67deab195988a6b6f00a14d14a75deb81b3c9d4d3785bc5285a41fb59bba1fe"} {:id "defn/play", :kind "defn", :line 67, :end-line 81, :hash "b4f3c75802de50d6bc4bbd105dbe0841a5ae471a2f90a34d9334dd2ac9c62ecd"} {:id "defn/undo", :kind "defn", :line 83, :end-line 86, :hash "672fce5fb9da31bb9b2abf8349642a197948a8f660afafa16b0557c219616804"} {:id "defn/undo-turn", :kind "defn", :line 88, :end-line 93, :hash "e4bdc0a10e331c00205c8c8eca2fdd1583bee4d6f01db847eaadea8ac40a25c3"}]}
;; clj-mutate-manifest-end
