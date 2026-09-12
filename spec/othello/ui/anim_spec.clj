(ns othello.ui.anim-spec
  (:require [speclj.core :refer :all]
            [othello.ui.anim :as anim]))

(describe "animation policy"
  (it "shows no captures during the place delay"
    (should= 0 (anim/flips-shown {:frame 0 :flips [[3 3]]}))
    (should= 0 (anim/flips-shown {:frame anim/place-frames :flips [[3 3]]})))

  (it "reveals one capture per stagger after the place delay"
    (should= 1 (anim/flips-shown {:frame (+ anim/place-frames anim/flip-stagger)
                                 :flips [[3 3] [3 4]]})))

  (it "finishes after place, staggered flips, and hold"
    (let [one {:flips [[3 3]] :frame 0}
          two-done (+ anim/place-frames
                      (* anim/flip-stagger 2)
                      anim/hold-frames)]
      (should-not (anim/animation-done? one))
      (should (anim/animation-done?
                (assoc one :frame (+ anim/place-frames
                                     anim/flip-stagger
                                     anim/hold-frames))))
      (should-not (anim/animation-done? {:flips [[3 3] [3 4]]
                                        :frame (dec two-done)}))
      (should (anim/animation-done? {:flips [[3 3] [3 4]] :frame two-done})))))
