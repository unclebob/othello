(ns othello.ui.layout
  (:require [othello.board :as board]))

(def window-width 1100)
(def window-height 740)
(def board-left 48)
(def board-top 56)
(def square-size 78)
(def board-pixels (* board/size square-size))
(def sidebar-left 710)
(def disc-radius 30)

(defn square-left [col]
  (+ board-left (* col square-size)))

(defn square-top [row]
  (+ board-top (* row square-size)))

(defn square-center [row col]
  [(+ (square-left col) (quot square-size 2))
   (+ (square-top row) (quot square-size 2))])

(defn square-at [x y]
  (let [dx (- (int x) board-left)
        dy (- (int y) board-top)]
    (if (and (<= 0 dx) (< dx board-pixels)
             (<= 0 dy) (< dy board-pixels))
      [(quot dy square-size) (quot dx square-size)]
      nil)))

(defn buttons []
  [{:id :new-game :label "New Game" :x sidebar-left :y 548 :w 170 :h 40}
   {:id :undo :label "Undo" :x (+ sidebar-left 186) :y 548 :w 140 :h 40}
   {:id :play-black :label "Play Black" :x sidebar-left :y 602 :w 160 :h 36}
   {:id :play-white :label "Play White" :x (+ sidebar-left 176) :y 602 :w 150 :h 36}
   {:id :hints :label "Hints" :x sidebar-left :y 652 :w 170 :h 36}])

(defn inside? [px py {:keys [x y w h]}]
  (and (<= x px) (< px (+ x w))
       (<= y py) (< py (+ y h))))

(defn button-at [px py]
  (first (filter #(inside? px py %) (buttons))))

(defn column-label [col]
  (nth "abcdefgh" col))

(defn row-label [row]
  (str (inc row)))

(defn algebraic [row col]
  (str (column-label col) (row-label row)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:50:35.915075-05:00", :module-hash "0153a0420aa6f65a3d31203dcd53c74975c4876a65ef1ae411c2511ea80141d8", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "343476e3b7bbebacc22aaf91e802b6eaeada22083ab4836b0c89f4f17b1097d2"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 2, :hash "68ea36c44a1b49a2c0a9101429044681da837d50936a031d353404c8808103de"} {:id "def/window-width", :kind "def", :line 4, :end-line 4, :hash "65050f93ec730b91496d8859c612d78a83ccafaedf2faedf3e3af67f1865582b"} {:id "def/window-height", :kind "def", :line 5, :end-line 5, :hash "df87b2051ac481fcffe053a64a18fe1dfd1fbc87d8d663d967ad03a1d585bd2b"} {:id "def/board-left", :kind "def", :line 6, :end-line 6, :hash "dcfd5a08792bb808d226b8aa5ec94bd5a50f765df9044d582ba59a91df4cfd87"} {:id "def/board-top", :kind "def", :line 7, :end-line 7, :hash "a6b096d657ff41261ea49e233b96bd83ed9ebf914e832e6fc284f300bbe2b042"} {:id "def/square-size", :kind "def", :line 8, :end-line 8, :hash "8605ea4b94ba9cdaabede9a90d3a3e801d234b0339ca4f0bf064eee7670c7214"} {:id "def/board-pixels", :kind "def", :line 9, :end-line 9, :hash "1b181c0c4f5d47b1d5ca21f9e1af0499773bc42d064755a2774dbc3731e15027"} {:id "def/sidebar-left", :kind "def", :line 10, :end-line 10, :hash "a1d0d3a0822291e30a4f0251fcfdd15dbb854384f8d053621fc2fe3729598bd9"} {:id "def/disc-radius", :kind "def", :line 11, :end-line 11, :hash "87f5994877288ab9699b26ae9dd2c193a338c92cced19283ee81c459a87c1e2c"} {:id "defn/square-left", :kind "defn", :line 13, :end-line 14, :hash "aa2f2780a42f1d74c2159463c6246c5b9c2ee4cceb711a3d1eb79ba019cbdcf4"} {:id "defn/square-top", :kind "defn", :line 16, :end-line 17, :hash "b680bc1937022b2f930e6bc407eb7a0b61957f222ca20f98de9251aa829ecf4d"} {:id "defn/square-center", :kind "defn", :line 19, :end-line 21, :hash "5ef3e6293f83863eeffb108fee90ad0291c1f5bd08e6268d62fb6daeb03014e2"} {:id "defn/square-at", :kind "defn", :line 23, :end-line 29, :hash "1311baa7afcc14e0550b77a075768fc82e4cc480a7d78ef5e420df5726de3ed7"} {:id "defn/buttons", :kind "defn", :line 31, :end-line 36, :hash "e227ad263176304c85a3d2ac38600530556ec96c80300ea63e93a69797fbf626"} {:id "defn/inside?", :kind "defn", :line 38, :end-line 40, :hash "a7134c688b2837b37d4f291164a8a6acf40c7856067e36e9644dba395b1c9f26"} {:id "defn/button-at", :kind "defn", :line 42, :end-line 43, :hash "4d8e8870c91a876e28a64421a6dd0c197f190b3ebae439085e5926e286e8e213"} {:id "defn/column-label", :kind "defn", :line 45, :end-line 46, :hash "b0e080162c0cbe46daba2ead10b21e2888bd46c357915b4d0b7b6ed15c5e3d44"} {:id "defn/row-label", :kind "defn", :line 48, :end-line 49, :hash "00d312d1f61fbd7cfc9d43d7a0fad5241a1c087f26d91a858643246abeb7c4d3"} {:id "defn/algebraic", :kind "defn", :line 51, :end-line 52, :hash "6f0e7ea5c5706f812b6b0da649a1f03c1c8bdf4f730c2c2cd66094c8b99e4d25"}]}
;; clj-mutate-manifest-end
