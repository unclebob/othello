(ns othello.ai.search
  (:require [othello.board :as board]
            [othello.rules :as rules]
            [othello.ai.eval :as eval]))

(def alpha-min -1000000)
(def alpha-max 1000000)
(def default-depth 5)
(def default-max-depth 6)
(def default-endgame 12)

(defn now-ms []
  (System/currentTimeMillis))

(defn ordered-moves [board player]
  (->> (rules/legal-moves board player)
       (sort-by (fn [[row col]]
                  (- (nth eval/weights (board/index row col)))))
       vec))

(defn search-end? [board depth]
  (or (= depth 0)
      (rules/game-over? board)))

(defn search-depth-limit [board opts]
  (let [empties (board/count-player board board/empty-cell)
        endgame (or (:endgame opts) default-endgame)]
    (if (<= empties endgame)
      empties
      (or (:max-depth opts) default-max-depth))))

(defn stop-deepening? [depth cap now deadline]
  (or (> depth cap)
      (and (> depth 1)
           (>= (now) deadline))))

(declare negamax)

(defn- child-score [board player depth alpha beta row col]
  (let [child (rules/apply-move board player row col)
        opponent (board/opponent player)]
    (- (negamax child opponent (dec depth) (- beta) (- alpha)))))

(defn- best-score [board player depth alpha beta moves]
  (loop [remaining moves
         best alpha-min
         a alpha]
    (if (seq remaining)
      (let [pos (first remaining)
            score (child-score board player depth a beta (first pos) (second pos))
            next-best (max best score)
            next-a (max a score)]
        (if (pos? (- next-a beta))
          next-best
          (recur (rest remaining) next-best next-a)))
      best)))

(defn negamax [board player depth alpha beta]
  (if (search-end? board depth)
    (eval/evaluate board player)
    (let [moves (ordered-moves board player)
          opponent (board/opponent player)]
      (if (seq moves)
        (best-score board player depth alpha beta moves)
        (- (negamax board opponent (dec depth) alpha-min alpha-max))))))

(defn- scored-move [board player depth pos]
  {:pos pos
   :score (child-score board player depth alpha-min alpha-max
                       (first pos) (second pos))})

(defn- pick-better [current candidate]
  (if (> (:score candidate) (:score current))
    candidate
    current))

(defn choose-at-depth [board player depth]
  (let [moves (ordered-moves board player)]
    (if (seq moves)
      (:pos (reduce pick-better (map #(scored-move board player depth %) moves)))
      nil)))

(defn- timed-deepen [board player opts]
  (let [now (or (:now opts) now-ms)
        deadline (+ (now) (:time-ms opts))
        cap (search-depth-limit board opts)]
    (loop [depth 1
           best (first (ordered-moves board player))]
      (if (stop-deepening? depth cap now deadline)
        best
        (recur (inc depth) (choose-at-depth board player depth))))))

(defn choose [board player opts]
  (let [moves (ordered-moves board player)]
    (if (seq moves)
      (if (:time-ms opts)
        (timed-deepen board player opts)
        (choose-at-depth board player (or (:depth opts) default-depth)))
      nil)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:26:57.484443-05:00", :module-hash "5e49c2ab6f4b8c0b8f7b41f3e7c898a5875f506593ac626e6ebf42ef25a73b50", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "7e85d1e0bc53669bd2793e7f773243b8bbb9bc1833d7efa773e02cd14a3a18b5"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 4, :hash "e1a1714b0ee63fc40a6738ec56901c04971fc34146a2880c4f6626187db7a2fa"} {:id "def/alpha-min", :kind "def", :line 6, :end-line 6, :hash "9218383e2cb40f22febb4251bf7e3aa4b6bfd32807ab1a343bd3b841fa697af7"} {:id "def/alpha-max", :kind "def", :line 7, :end-line 7, :hash "8b6d2acc2e156d6648dd8dadc029ed80d93905f44a99edbe9ab0a570a58107ab"} {:id "def/default-depth", :kind "def", :line 8, :end-line 8, :hash "6163c4da5f2aa6ea0554ba8a8da7762be74ea5a539033a3ba0f61e24a97cb84a"} {:id "def/default-max-depth", :kind "def", :line 9, :end-line 9, :hash "b06ef35c4b2de12084ae39905ca946b9c38a827fb8f92d01d3db843aeb37f46b"} {:id "def/default-endgame", :kind "def", :line 10, :end-line 10, :hash "7e7767b3e4a607cc13cfbd88cb76c6a402a195e691b0fb4f5e9153def9fd237f"} {:id "defn/now-ms", :kind "defn", :line 12, :end-line 13, :hash "daccbc32ce84917fc1a65b6b2d71ba9259d48f862a294a3be8ff5b9f7f73eb72"} {:id "defn/ordered-moves", :kind "defn", :line 15, :end-line 19, :hash "d2f79f27e79e0b52208995ca109b4b88e78f8bb914a1379a3298268203366833"} {:id "defn/search-end?", :kind "defn", :line 21, :end-line 23, :hash "2124b5638fc058adfc56591c8c7422bdb20ee6113d5b887f982148f55b998400"} {:id "defn/search-depth-limit", :kind "defn", :line 25, :end-line 30, :hash "99393a7e88a501c86bb450d12caad2abc740c5689f71f7becd84059f065ff190"} {:id "defn/stop-deepening?", :kind "defn", :line 32, :end-line 35, :hash "6f94973a9e239843b3bb8e9f596fbb2521b3aeafdc33270cd2bed97441aa90b1"} {:id "form/11/declare", :kind "declare", :line 37, :end-line 37, :hash "55aa658eadbbc1f2a4eff9798d3772d9aacc494222f6024d2ef2830faac3bb77"} {:id "defn-/child-score", :kind "defn-", :line 39, :end-line 42, :hash "086ab5ad116afc41c9382cb366f9f312891d75fee7400291cb16600e090e0c0c"} {:id "defn-/best-score", :kind "defn-", :line 44, :end-line 56, :hash "f7f0293b09cf6d9e362078e749f9e8e60e15a09781ee2bcd68f6917e7188cdd6"} {:id "defn/negamax", :kind "defn", :line 58, :end-line 65, :hash "2ecc12becf76ac2227c10fe24cfc8ae742957c218f3374a8e7930662ce224a3d"} {:id "defn-/scored-move", :kind "defn-", :line 67, :end-line 70, :hash "a20d0bc9806fb66e983a14f96cf1f8e632c10a639b94708e845ddc44dda3b36a"} {:id "defn-/pick-better", :kind "defn-", :line 72, :end-line 75, :hash "d93419750942ffb88950858a364fb4236abe8344f4438d7fd9fca6283a209fcf"} {:id "defn/choose-at-depth", :kind "defn", :line 77, :end-line 81, :hash "a46daa9447a5f402415a4535042501d592a17f19bfc21d7c91cba1ee1b4f96b9"} {:id "defn-/timed-deepen", :kind "defn-", :line 83, :end-line 91, :hash "ee0c0bceccfb824b7ae478848770b2b0a7480845f1a5fb8c7848d25fabf28895"} {:id "defn/choose", :kind "defn", :line 93, :end-line 99, :hash "367edcd8fa13f7c7eafd126cf9c79ee5aeb2ad7221d334600ec6c803f266076c"}]}
;; clj-mutate-manifest-end
