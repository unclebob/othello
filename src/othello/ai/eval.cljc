(ns othello.ai.eval
  (:require [othello.board :as board]
            [othello.rules :as rules]))

(def weights
  [120 -20 20 5 5 20 -20 120
   -20 -40 -5 -5 -5 -5 -40 -20
   20 -5 15 3 3 15 -5 20
   5 -5 3 3 3 3 -5 5
   5 -5 3 3 3 3 -5 5
   20 -5 15 3 3 15 -5 20
   -20 -40 -5 -5 -5 -5 -40 -20
   120 -20 20 5 5 20 -20 120])

(def win-score 100000)
(def draw-score 0)

(defn cell-value [board player i]
  (let [here (nth board i)
        weight (nth weights i)]
    (cond
      (= here player) weight
      (= here (board/opponent player)) (- weight)
      :else (- weight weight))))

(defn positional [board player]
  (reduce + 0
          (map (fn [i] (cell-value board player i))
               (range (count board)))))

(defn mobility [board player]
  (let [mine (count (rules/legal-moves board player))
        opp (count (rules/legal-moves board (board/opponent player)))]
    (- mine opp)))

(defn disc-diff [board player]
  (- (board/count-player board player)
     (board/count-player board (board/opponent player))))

(defn terminal [board player]
  (let [diff (disc-diff board player)]
    (cond
      (> diff 0) win-score
      (< diff 0) (- win-score)
      :else draw-score)))

(defn midgame [board player]
  (+ (positional board player)
     (* 12 (mobility board player))
     (* 6 (disc-diff board player))))

(defn evaluate [board player]
  (if (rules/game-over? board)
    (terminal board player)
    (midgame board player)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:20:44.257485-05:00", :module-hash "ccbd7f775a3a78f69376ea73a12a2a321c135680a5dabfe5b42a1e88608a3a13", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "67dd8b0cbe01743f7227b31631f1ee4335e2c80420b91c6cc72946a3b5abca6d"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "39e8fee64bebcd5490ad5401903c1b64505d3c808d297b649371e9988c2ea780"} {:id "def/weights", :kind "def", :line 5, :end-line 13, :hash "0d3569a0be2edeed9fedf87ff57bc569d4a92432df0567ab34c362ece09682a5"} {:id "def/win-score", :kind "def", :line 15, :end-line 15, :hash "42bbe168169ed41473cfc2de72b100ff76cd6723b2ae2475129e0a236abb0c07"} {:id "def/draw-score", :kind "def", :line 16, :end-line 16, :hash "216d1a1c6355bc37d07c9109936fa3f9a56f69549b26414beece8a341d61818d"} {:id "defn/cell-value", :kind "defn", :line 18, :end-line 24, :hash "5d30f04bebe086d6b150b21d2815013298d5712812c785a606cf28bc6c89563f"} {:id "defn/positional", :kind "defn", :line 26, :end-line 29, :hash "9baf41923ff78660c689920d0bc093792f2122a46c5509611fec2db36f462bd5"} {:id "defn/mobility", :kind "defn", :line 31, :end-line 34, :hash "d8c01b4573f4b318e58728b61d45c45e308a0a55313dcdad6b6e28f733b912fe"} {:id "defn/disc-diff", :kind "defn", :line 36, :end-line 38, :hash "384ec6281ffa942f4e120626b4c0da64aae2c10344870d650e4e5ee4238ab3c3"} {:id "defn/terminal", :kind "defn", :line 40, :end-line 45, :hash "6cef31fa03cf21ec61731e2761e56571a782154262bfc3a4210a70051232ee1f"} {:id "defn/midgame", :kind "defn", :line 47, :end-line 50, :hash "d8aadb5f1f680267e7cbeb294b19c5545f2532c350e3f3ff31b0046968452f33"} {:id "defn/evaluate", :kind "defn", :line 52, :end-line 55, :hash "b5056454fdaabba063f390806a4fa1121b8f02fb06457c3e637a97a2f7d5dfe6"}]}
;; clj-mutate-manifest-end
