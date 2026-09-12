(ns othello.ui.events
  (:require [othello.board :as board]
            [othello.game :as game]
            [othello.rules :as rules]
            [othello.ui.anim :as anim]
            [othello.ui.layout :as layout]))

(defn fresh-ui
  ([human ai-fn]
   {:game (game/new-game human)
    :phase (if (= human board/black)
             :awaiting-human
             :awaiting-computer)
    :pointer nil
    :animation nil
    :flash-pos nil
    :flash-frames 0
    :hints? true
    :settle-frames 0
    :pass-frames 0
    :think-frames 0
    :ai ai-fn
    :ai-job nil})
  ([human]
   (fresh-ui human nil)))

(defn awaiting-computer? [state]
  (and (nil? (:animation state))
       (= 0 (:settle-frames state))
       (= :awaiting-computer (:phase state))
       (= :in-play (:status (:game state)))))

(defn needs-ai-job? [state]
  (and (= :computer-thinking (:phase state))
       (nil? (:ai-job state))
       (nil? (:animation state))))

(defn- begin-computer-thinking [state]
  (assoc state
    :phase :computer-thinking
    :think-frames 0
    :ai-job nil))

(defn- order-flips [row col flips]
  (sort-by (fn [[r c]]
             (max (abs (- r row)) (abs (- c col))))
           flips))

(defn- start-animation-at [state row col]
  (let [game (:game state)
        player (:to-move game)
        captured (rules/flips (:board game) player row col)]
    (if (seq captured)
      (assoc state
        :phase :animating
        :ai-job nil
        :animation {:row row
                    :col col
                    :player player
                    :flips (vec (order-flips row col captured))
                    :frame 0}
        :flash-pos nil
        :flash-frames 0)
      state)))

(defn start-animation [state pos]
  (if pos
    (start-animation-at state (first pos) (second pos))
    state))

(defn- phase-after [game]
  (cond
    (= :over (:status game)) :game-over
    (:passed? game) :pass-notice
    (game/human? game) :awaiting-human
    :else :awaiting-computer))

(defn- complete-animation [state]
  (let [anim (:animation state)
        game (game/play (:game state) (:row anim) (:col anim))]
    (assoc state
      :game game
      :animation nil
      :ai-job nil
      :settle-frames anim/settle-frames
      :pass-frames 0
      :phase (phase-after game))))

(defn- tick-animation [state]
  (if-let [anim (:animation state)]
    (let [frame (inc (:frame anim))
          next-anim (assoc anim :frame frame)]
      (if (anim/animation-done? next-anim)
        (complete-animation state)
        (assoc state :animation next-anim)))
    state))

(defn- tick-flash [state]
  (if (> (:flash-frames state) 0)
    (let [remaining (dec (:flash-frames state))]
      (assoc state
        :flash-frames remaining
        :flash-pos (if (zero? remaining) nil (:flash-pos state))))
    state))

(defn- tick-settle [state]
  (if (> (:settle-frames state) 0)
    (update state :settle-frames dec)
    state))

(defn- begin-next-turn [state]
  (assoc state
    :pass-frames 0
    :phase (if (game/human? (:game state))
             :awaiting-human
             :awaiting-computer)))

(defn- tick-pass [state]
  (if (= :pass-notice (:phase state))
    (let [n (inc (:pass-frames state))]
      (if (>= n anim/pass-display-frames)
        (begin-next-turn state)
        (assoc state :pass-frames n)))
    state))

(defn tick [state]
  (-> state
      tick-flash
      tick-settle
      tick-pass
      tick-animation))

(defn- deliver-computer-move [state pos]
  (let [next (start-animation state pos)]
    (if (= :animating (:phase next))
      next
      (begin-computer-thinking state))))

(defn on-frame [state {:keys [job-done? job-result]}]
  (let [s (tick state)]
    (cond
      (awaiting-computer? s)
      (begin-computer-thinking s)

      (and (= :computer-thinking (:phase s))
           job-done?
           (>= (:think-frames s) anim/min-think-frames)
           job-result)
      (deliver-computer-move s job-result)

      (= :computer-thinking (:phase s))
      (update s :think-frames inc)

      :else s)))

(defn tick-ai [state]
  (let [ai (:ai state)
        game (:game state)]
    (if (and (awaiting-computer? state) ai)
      (start-animation state (ai (:board game) (:to-move game)))
      state)))

(defn- flash [state row col]
  (assoc state :flash-pos [row col] :flash-frames anim/flash-frames))

(defn- legal-here? [state row col]
  (contains? (set (game/legal-positions (:game state))) [row col]))

(defn- try-pos [state row col]
  (if (legal-here? state row col)
    (start-animation state [row col])
    (flash state row col)))

(defn- handle-board-click [state x y]
  (let [pos (layout/square-at x y)]
    (if (and (= :awaiting-human (:phase state)) pos)
      (try-pos state (first pos) (second pos))
      state)))

(defn- restart [state human]
  (fresh-ui human (:ai state)))

(defn- cancel-animation [state]
  (assoc state
    :animation nil
    :phase :awaiting-human
    :flash-pos nil
    :flash-frames 0))

(defn- revert-turn [state]
  (let [game (game/undo-turn (:game state))]
    (assoc state
      :game game
      :ai-job nil
      :animation nil
      :flash-pos nil
      :flash-frames 0
      :think-frames 0
      :phase (if (= :over (:status game))
               :game-over
               (if (game/human? game)
                 :awaiting-human
                 :awaiting-computer)))))

(defn undo-ui [state]
  (if (= :animating (:phase state))
    (cancel-animation state)
    (revert-turn state)))

(defn handle-button [state id]
  (cond
    (= id :new-game) (restart state (:human (:game state)))
    (= id :undo) (undo-ui state)
    (= id :play-black) (restart state board/black)
    (= id :play-white) (restart state board/white)
    (= id :hints) (update state :hints? not)
    :else state))

(defn on-click [state x y]
  (if-let [button (layout/button-at x y)]
    (handle-button state (:id button))
    (handle-board-click state (int x) (int y))))

(defn on-mouse-move [state x y]
  (assoc state :pointer [x y]))

(defn on-key [state key]
  (cond
    (= key :n) (restart state (:human (:game state)))
    (= key :u) (undo-ui state)
    (= key :h) (update state :hints? not)
    (= key :1) (restart state board/black)
    (= key :2) (restart state board/white)
    :else state))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? true, :tested-at "2026-09-10T16:05:13.391686-05:00", :module-hash "96e4f99766bcbd911edb35d1a19bc5afec8c3c3afecd34560cb6d4a847ee5f74", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "175994bbe9f354972612f512c10b5f174fcbb53f5b75e0fe80b0bf9ee57ef4a3"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 6, :hash "62fa1d1bbfde626143a5c9f2553b1302a10bc8233dbe2fe55a96f0846016cc5b"} {:id "defn/fresh-ui", :kind "defn", :line 8, :end-line 25, :hash "3ab8abf3d3846128dacc5687fb18d43d496f3db0d7bd0aa1332adba2b1b79982"} {:id "defn/awaiting-computer?", :kind "defn", :line 27, :end-line 31, :hash "c649efc84507956b158630c9722ac14515fc70f2a2dab1c8a753ef2c7f0fbab8"} {:id "defn/needs-ai-job?", :kind "defn", :line 33, :end-line 36, :hash "b79c1e382862e0f92b5884f758665871d86f8beb5073a02a430520d810c5c0d2"} {:id "defn-/begin-computer-thinking", :kind "defn-", :line 38, :end-line 42, :hash "80f1e5867fc7772287ed14f1559def32277aef7a0f03216e96c9695524429649"} {:id "defn-/order-flips", :kind "defn-", :line 44, :end-line 47, :hash "5887870557c1ccbcb3682d35322e28e3e3a80261156299ca77d9bd863cc21984"} {:id "defn-/start-animation-at", :kind "defn-", :line 49, :end-line 64, :hash "c2e4b41fc574ddba352cdfd17aeb72d7191607afae4d66f760034fe553391cc6"} {:id "defn/start-animation", :kind "defn", :line 66, :end-line 69, :hash "80352519d62b38b3e973a550d2b8d19a4390615aae51bbe9c1c91366953c8881"} {:id "defn-/phase-after", :kind "defn-", :line 71, :end-line 76, :hash "21b931ed679d6bcfd5dd844d67c4edb6f2f812634aebfe01908caf3c0f584001"} {:id "defn-/complete-animation", :kind "defn-", :line 78, :end-line 87, :hash "fda40b93ecf939167badaff95302099abb243ea8b6cd6d868ca5a8496fd6f471"} {:id "defn-/tick-animation", :kind "defn-", :line 89, :end-line 96, :hash "d36790771a3890de7cfaf18f2210b8f1d8585bdb51ad508c2423011510bcd53c"} {:id "defn-/tick-flash", :kind "defn-", :line 98, :end-line 104, :hash "250b334d14ef979ad1153ae5f37f67bafc1331a2549c6c05b3b70fc6e78d898a"} {:id "defn-/tick-settle", :kind "defn-", :line 106, :end-line 109, :hash "fc966e2f6b5ccbbe9ff169f3796cd0f64d4530520b65fd9ca4a02de864961d2c"} {:id "defn-/begin-next-turn", :kind "defn-", :line 111, :end-line 116, :hash "cf6c5fc3d1f7c035856be779938597a7171437f56412cd48dee0a9afd96d05fa"} {:id "defn-/tick-pass", :kind "defn-", :line 118, :end-line 124, :hash "59d6fa51c1cbc4e73fff2cb7f071af44bf8fac653480244cfd4c34d00535c08e"} {:id "defn/tick", :kind "defn", :line 126, :end-line 131, :hash "6af7ee9b983d3cd11b8b49831d6c6e0e98c4d34a0e911969d56acf41e56531da"} {:id "defn-/deliver-computer-move", :kind "defn-", :line 133, :end-line 137, :hash "2ee9d2a9cdf13328c11d66661cecc98aa90c6e285651e8f726bfd01340aef7ed"} {:id "defn/on-frame", :kind "defn", :line 139, :end-line 154, :hash "8463c334f6b444afc19b19d914cf7211c72460cb81522e684a84ecc2d1be6bca"} {:id "defn/tick-ai", :kind "defn", :line 156, :end-line 161, :hash "a6899d70e3e457b4e40ea0ac564052db5f4cbeac68e14446d53ebf390430f902"} {:id "defn-/flash", :kind "defn-", :line 163, :end-line 164, :hash "2ee83d5065a6cdb558b0fbba934e64f829d1ec13c1727ae8279373f5a9c00eb3"} {:id "defn-/legal-here?", :kind "defn-", :line 166, :end-line 167, :hash "b2b8a58f187191921e2e7da6d43403eafd74223f1ce21627c94841f5673898b4"} {:id "defn-/try-pos", :kind "defn-", :line 169, :end-line 172, :hash "7b6823b5cf97e032a91c3ab43703d568415eeb497319cde2e8e4a5a46ecd16e1"} {:id "defn-/handle-board-click", :kind "defn-", :line 174, :end-line 178, :hash "f188f56e3295b5c247d9e8c2e7875358cc368d6a6ebf4e659986a643944e754c"} {:id "defn-/restart", :kind "defn-", :line 180, :end-line 181, :hash "79cab24212390d452454efa010bc837632cae9cf822dd3033efd059fc4b6fe55"} {:id "defn-/cancel-animation", :kind "defn-", :line 183, :end-line 188, :hash "017d12a3a7401f330bf8170032c6863d443a5cc99c6cfc430edd0834725f8b5a"} {:id "defn-/revert-turn", :kind "defn-", :line 190, :end-line 203, :hash "eaa8d66f5e996144bf8ddcbcecf9a4b625d6d436aa97e8a6cd4044b41631c58a"} {:id "defn/undo-ui", :kind "defn", :line 205, :end-line 208, :hash "527c8a2d70b7372dfbf706d08101ac6999c5db0d02c19e9ceeac34ea8ba89ecb"} {:id "defn/handle-button", :kind "defn", :line 210, :end-line 217, :hash "e35c6ea2b67269c337c5a814930a83cc91045cc2a8992b14187597b9f1867d40"} {:id "defn/on-click", :kind "defn", :line 219, :end-line 222, :hash "6ca3fe2eb25e804eb93c845f2978341685698cf0fd2d96f4314382a8fab11c27"} {:id "defn/on-mouse-move", :kind "defn", :line 224, :end-line 225, :hash "5ec5a4ce8b05758ac050113d7edfc0862fbfbd98ee6842fa97493d672df0af8f"} {:id "defn/on-key", :kind "defn", :line 227, :end-line 234, :hash "57f96622550f4fdacb8b8de90dbed9d832529052660e3c9dbbf6c4125b831549"}]}
;; clj-mutate-manifest-end
