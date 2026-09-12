(ns othello.ui.anim)

(def place-frames 8)
(def flip-stagger 4)
(def hold-frames 10)
(def min-think-frames 15)
(def pass-display-frames 90)
(def settle-frames 18)
(def flash-frames 12)

(defn flips-shown [anim]
  (let [raw (quot (- (:frame anim) place-frames) flip-stagger)]
    (max 0 raw)))

(defn animation-done? [anim]
  (>= (:frame anim)
      (+ place-frames (* flip-stagger (count (:flips anim))) hold-frames)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? false, :tested-at "2026-09-12T08:46:26.731068-05:00", :module-hash "c5f0b0247063024e0f7ee19650a4ae5f6a2bab2b40487451ece952be3991116b", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "e484b75f66cdd819ebbd386b124280a03f7445a7a2c6b2346fec4e8f0f72c0d8"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 1, :hash "c682d0fadf357c55e2d84680428ae6b16ed3133cce39bab0b0afd01d2397f2a6"} {:id "def/place-frames", :kind "def", :line 3, :end-line 3, :hash "f96e3c60a2925b8cee9b418ddec3f49bc608607779d8c207eb5cc74941e78d20"} {:id "def/flip-stagger", :kind "def", :line 4, :end-line 4, :hash "21335fba39896815f59e2e51d7358de1cb63b7b6386667997e132b0f4509f0a7"} {:id "def/hold-frames", :kind "def", :line 5, :end-line 5, :hash "ebcf69ff871defff5b39ac25a6ec7535d55bbbea89ce3867db488a5b5516c0d8"} {:id "def/min-think-frames", :kind "def", :line 6, :end-line 6, :hash "a7a6bb949dc92e912c4e1657b330a451a87291989a13fcf92fddd72a8f31ab97"} {:id "def/pass-display-frames", :kind "def", :line 7, :end-line 7, :hash "78b356eb6973a67d7ea4047cf76b5ad9c0e52eaf5071f0e021a43cb523debc4d"} {:id "def/settle-frames", :kind "def", :line 8, :end-line 8, :hash "6785e0256896a16827e929331d6ec224630aa30450b6d586fb889f9465fe82fe"} {:id "def/flash-frames", :kind "def", :line 9, :end-line 9, :hash "0e5651d71dd9a7c25baa0ac1f2ee57ca5c7a523ee4b04f74f902c045d763dd43"} {:id "defn/flips-shown", :kind "defn", :line 11, :end-line 13, :hash "8753168826d333dded1368248121915c5eaf93c583b5359619225ed6a76059ff"} {:id "defn/animation-done?", :kind "defn", :line 15, :end-line 17, :hash "88bf5755c74bb38be0fe0e4ba9c830c8df1b7c51bb2b68b328c1feefa8c18b90"}]}
;; clj-mutate-manifest-end
