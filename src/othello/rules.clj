(ns othello.rules
  (:require [othello.board :as board]))

(defn- gather-from [board player row col dr dc captured]
  (if (board/in-bounds? row col)
    (let [here (board/cell board row col)]
      (cond
        (= here (board/opponent player))
        (gather-from board player (+ row dr) (+ col dc) dr dc (conj captured [row col]))

        (= here player)
        captured

        :else
        []))
    []))

(defn flips-in-direction [board player row col dr dc]
  (gather-from board player (+ row dr) (+ col dc) dr dc []))

(defn flips [board player row col]
  (if (board/empty-square? board row col)
    (mapcat (fn [[dr dc]]
              (flips-in-direction board player row col dr dc))
            board/directions)
    []))

(defn legal? [board player row col]
  (boolean (seq (flips board player row col))))

(defn legal-moves [board player]
  (vec (for [[row col] (board/squares)
             :when (legal? board player row col)]
         [row col])))

(defn apply-move [board player row col]
  (reduce (fn [next-board [r c]]
            (board/place next-board r c player))
          (board/place board row col player)
          (flips board player row col)))

(defn has-move? [board player]
  (boolean (seq (legal-moves board player))))

(defn game-over? [board]
  (and (not (has-move? board board/black))
       (not (has-move? board board/white))))

(defn winner [board]
  (let [black-count (board/count-player board board/black)
        white-count (board/count-player board board/white)]
    (cond
      (> black-count white-count) board/black
      (> white-count black-count) board/white
      :else :draw)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:17:25.121147-05:00", :module-hash "890b636a39743ebd7b1cad50aaa9eaabb0da0de31cfe7f1e4bc0cdf0dd887ec2", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "99d5cf609b1d7a1d1475daed41a5583b503665829a7643e34c2267adec1d63ad"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 2, :hash "5fb292ca326abb0164e9c87efd6ecd6c4d17ab2acc17f83c8c576e93b06f4bef"} {:id "defn-/gather-from", :kind "defn-", :line 4, :end-line 16, :hash "3fd5d0a4c8059ce5e2472eee8168177b6013d355d8959798952c5bd8831a257e"} {:id "defn/flips-in-direction", :kind "defn", :line 18, :end-line 19, :hash "7aa110acfafef2c0a9d8ef6e4155a1c40bc535b56608bb016d05703798ff6e7d"} {:id "defn/flips", :kind "defn", :line 21, :end-line 26, :hash "decc5fabaf3b23e8a569af1474f670628f42426f9142509f6c9075c6b50702b7"} {:id "defn/legal?", :kind "defn", :line 28, :end-line 29, :hash "bd72035ce3ff5599ca114d0a69d22197fef4065af592e696963050c9024f455f"} {:id "defn/legal-moves", :kind "defn", :line 31, :end-line 34, :hash "38ee2e363b6bcb98d20b93b365ecadf62143c797bc64366231ffa2467519c838"} {:id "defn/apply-move", :kind "defn", :line 36, :end-line 40, :hash "aeaadb9b6b6de715e618066b3a81a298cb14508d0c1213dd06bc3b199f0ae98d"} {:id "defn/has-move?", :kind "defn", :line 42, :end-line 43, :hash "4ea935f2e7f13ee2f08e433495e25f295b68f24623b20b4df5b30aee42cd48c8"} {:id "defn/game-over?", :kind "defn", :line 45, :end-line 47, :hash "0b2ea11770823d54b9e5dd0f0d2a03a0522987c905f140be25a72ba7a62e5213"} {:id "defn/winner", :kind "defn", :line 49, :end-line 55, :hash "12f89021a8ba04aa785dcb91106c692c44d3aacc9b6cab141e70c205917d84ae"}]}
;; clj-mutate-manifest-end
