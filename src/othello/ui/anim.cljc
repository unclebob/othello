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
