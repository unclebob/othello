(ns othello.board)

(def size 8)
(def empty-cell :empty)
(def black :black)
(def white :white)

(def directions
  [[-1 -1] [-1 0] [-1 1]
   [0 -1] [0 1]
   [1 -1] [1 0] [1 1]])

(defn opponent [player]
  (if (= player black)
    white
    black))

(defn in-bounds? [row col]
  (and (<= 0 row)
       (< row size)
       (<= 0 col)
       (< col size)))

(defn index [row col]
  (+ (* row size) col))

(defn empty-board []
  (vec (repeat (* size size) empty-cell)))

(defn cell [board row col]
  (nth board (index row col)))

(defn place [board row col player]
  (assoc board (index row col) player))

(defn empty-square? [board row col]
  (= empty-cell (cell board row col)))

(defn occupied? [board row col]
  (not (empty-square? board row col)))

(defn squares []
  (for [row (range size)
        col (range size)]
    [row col]))

(defn count-player [board player]
  (count (filter #(= player %) board)))

(defn initial-board []
  (-> (empty-board)
      (place 3 3 white)
      (place 3 4 black)
      (place 4 3 black)
      (place 4 4 white)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T15:17:00.374353-05:00", :module-hash "10f408c932b7c9f7c7dbd39159255bdc2164e9132066f149030c257faf8c8fac", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "99d5cf609b1d7a1d1475daed41a5583b503665829a7643e34c2267adec1d63ad"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 1, :hash "4b3de266211e028aee997bb9049e980877ec3a6be04947b83e7689c371f25cfe"} {:id "def/size", :kind "def", :line 3, :end-line 3, :hash "591196749d11540e73944017a3b0963f579906a07af0d8e66d6f7bd9ea6a6e38"} {:id "def/empty-cell", :kind "def", :line 4, :end-line 4, :hash "5a9945f8873cf677d453321cd9c2c3a5e15583d82504d9cb5b3a6688d404da30"} {:id "def/black", :kind "def", :line 5, :end-line 5, :hash "27e02718b4e54644b690eeea26dbe494d1196f7c956514460cb1ccdfb4c1dda3"} {:id "def/white", :kind "def", :line 6, :end-line 6, :hash "3dbf6e770c82e15a2b19cc3123fd6c5bd387555fd725a2e1e78f0abca85e2162"} {:id "def/directions", :kind "def", :line 8, :end-line 11, :hash "523a5411415b0df6ae015024f50be747affdd446e8fa6a98b6decf3307a827d2"} {:id "defn/opponent", :kind "defn", :line 13, :end-line 16, :hash "e39f109b59e0ca2a7e458e8d2972864822f6cf083e14692c5853b3c7f4f10d8d"} {:id "defn/in-bounds?", :kind "defn", :line 18, :end-line 22, :hash "a2171305ed58a0f5e2e0d6f21248445f4e6109cee3229e5c7179f9e711db4774"} {:id "defn/index", :kind "defn", :line 24, :end-line 25, :hash "d480b3ad2366c8400f339e88608cf0db194dff5c424821143c93622366d4e589"} {:id "defn/empty-board", :kind "defn", :line 27, :end-line 28, :hash "b2a8088adee48433c7d5783ef0dd4b838ddd53114393df8a12a5a0a8f00a4c00"} {:id "defn/cell", :kind "defn", :line 30, :end-line 31, :hash "5bba05b842a68d8d1a40aa8cb21f0ac3a29dbf3441be44cbc605e01f5afffba2"} {:id "defn/place", :kind "defn", :line 33, :end-line 34, :hash "0bcdffafd8481e1ab70164c0cbf4f1333353e4dd2da0d56fbd2832196fe91a2c"} {:id "defn/empty-square?", :kind "defn", :line 36, :end-line 37, :hash "1cd47291d0ecef0b7aa0dc1a1bb6802791e22daea89bb60c8cd97a032d573dda"} {:id "defn/occupied?", :kind "defn", :line 39, :end-line 40, :hash "3017d95661858c74cc0599ce8cfea542be91163ccd7bf4487ecdc32c57f78f05"} {:id "defn/squares", :kind "defn", :line 42, :end-line 45, :hash "be902234ad073551019fdb685bccefe952991921fac0e780771fc79d82d0e1e6"} {:id "defn/count-player", :kind "defn", :line 47, :end-line 48, :hash "17402543af39bcff12acd3cc128502c63014c2f2bacacabb16f4e853f97714e0"} {:id "defn/initial-board", :kind "defn", :line 50, :end-line 55, :hash "fdaa3d604a207e1b1f58526c1780bb76b474479984a0587272c5ce9f0ffdad20"}]}
;; clj-mutate-manifest-end
