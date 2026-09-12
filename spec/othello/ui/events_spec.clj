(ns othello.ui.events-spec
  (:require [speclj.core :refer :all]
            [othello.board :as board]
            [othello.game :as game]
            [othello.rules :as rules]
            [othello.ui.anim :as anim]
            [othello.ui.events :as events]
            [othello.ui.layout :as layout]
            [othello.spec-helper :refer [click-square click-button drain
                                         drain-animation parse-board play-at
                                         let-computer-play]]))

(defn greedy [board player]
  (last (rules/legal-moves board player)))

(defn ui
  ([] (ui greedy))
  ([ai] (events/fresh-ui board/black ai)))

(describe "events"
  (it "starts a black human on their turn with hints enabled"
    (let [state (ui)]
      (should= :awaiting-human (:phase state))
      (should (:hints? state))
      (should= 0 (:flash-frames state))
      (should= 0 (:settle-frames state))
      (should= 0 (:pass-frames state))
      (should= 0 (:think-frames state))
      (should= board/black (:human (:game state)))))

  (it "defaults to no AI when only a color is supplied"
    (let [state (events/fresh-ui board/black)]
      (should= nil (:ai state))
      (should= :awaiting-human (:phase (events/tick-ai state)))))

  (it "starts a white human with the computer to move"
    (let [state (events/fresh-ui board/white greedy)]
      (should= :awaiting-computer (:phase state))
      (should= board/white (:human (:game state)))))

  (it "ignores board clicks during the computer's turn"
    (let [state (assoc (ui) :phase :awaiting-computer)
          [x y] (layout/square-center 2 3)]
      (should= :awaiting-computer (:phase (events/on-click state x y)))))

  (it "ignores clicks that miss the board and the buttons"
    (let [state (ui)]
      (should= state (events/on-click state 1 1))))

  (it "accepts a legal click even when the mouse reports fractional pixels"
    (let [[x y] (layout/square-center 2 3)
          state (events/on-click (ui) (+ x 0.25) (+ y 0.75))]
      (should= :animating (:phase state))
      (should= 2 (:row (:animation state)))
      (should= 3 (:col (:animation state)))))

  (it "flashes an illegal click and clears the flash"
    (let [state (click-square (ui) 0 0)]
      (should= [0 0] (:flash-pos state))
      (should= anim/flash-frames (:flash-frames state))
      (should= 0 (:flash-frames (events/tick (assoc state :flash-frames 0))))
      (let [ticked (nth (iterate events/tick state) anim/flash-frames)
            mid (nth (iterate events/tick state) (dec anim/flash-frames))]
        (should= 1 (:flash-frames mid))
        (should= [0 0] (:flash-pos mid))
        (should= 0 (:flash-frames ticked))
        (should= nil (:flash-pos ticked)))))

  (it "advances the animation one frame at a time"
    (let [state (click-square (ui) 2 3)
          ticked (events/tick state)]
      (should= :animating (:phase ticked))
      (should= 0 (:flash-frames state))
      (should= 0 (:frame (:animation state)))
      (should= 1 (:frame (:animation ticked)))))

  (it "orders flipped discs by distance from the placed disc"
    (let [board (parse-board
                  "b.......
                   .w......
                   ..w.....
                   ........
                   ........
                   ........
                   ........
                   ........")
          state (assoc-in (ui) [:game :board] board)
          animating (click-square state 3 3)]
      (should= [[2 2] [1 1]] (:flips (:animation animating)))))

  (it "commits the move when the flip animation finishes"
    (let [done (drain-animation (click-square (ui) 2 3))]
      (should= :awaiting-computer (:phase done))
      (should= anim/settle-frames (:settle-frames done))
      (should= 0 (:pass-frames done))
      (should= board/black (board/cell (:board (:game done)) 2 3))
      (should= board/black (board/cell (:board (:game done)) 3 3))))

  (it "settles before the computer is allowed to think"
    (let [state (drain-animation (click-square (ui) 2 3))]
      (should-not (events/awaiting-computer? state))
      (should-not (events/awaiting-computer? (assoc state :settle-frames 1)))
      (should (events/awaiting-computer? (play-at (ui) 2 3)))
      (should= 0 (:settle-frames (nth (iterate events/tick state) anim/settle-frames)))
      (should= 0 (:settle-frames (events/tick (assoc state :settle-frames 0))))))

  (context "computer thinking"
    (it "enters the thinking phase on the first frame"
      (let [ready (play-at (ui) 2 3)
            thinking (events/on-frame ready {:job-done? false :job-result nil})]
        (should= :computer-thinking (:phase thinking))
        (should= 0 (:think-frames thinking))
        (should (events/needs-ai-job? thinking))))

    (it "drops a finished AI job so the next computer turn searches again"
      (let [ready (assoc (play-at (ui) 2 3) :ai-job :old-job)
            thinking (events/on-frame ready {:job-done? true :job-result [2 2]})]
        (should= :computer-thinking (:phase thinking))
        (should= nil (:ai-job thinking))
        (should (events/needs-ai-job? thinking))
        (should= nil (:animation thinking))))

    (it "does not keep the thinking lamp on when the returned move is illegal"
      (let [thinking (assoc (ui)
                       :phase :computer-thinking
                       :think-frames anim/min-think-frames
                       :ai-job :old-job)
            next (events/on-frame thinking {:job-done? true :job-result [0 0]})]
        (should= :computer-thinking (:phase next))
        (should= nil (:ai-job next))
        (should (events/needs-ai-job? next))))

    (it "waits for the minimum think time before placing the reply"
      (let [ready (play-at (ui) 2 3)
            thinking (events/on-frame ready {:job-done? false :job-result nil})
            waited (nth (iterate #(events/on-frame % {:job-done? true
                                                      :job-result [2 2]})
                                 thinking)
                        anim/min-think-frames)]
        (should= :computer-thinking (:phase waited))
        (should= anim/min-think-frames (:think-frames waited))
        (let [done (events/on-frame waited {:job-done? true :job-result [2 2]})]
          (should= :animating (:phase done))
          (should= 2 (:row (:animation done)))
          (should= 2 (:col (:animation done))))))

    (it "does not need a job once one is attached"
      (let [thinking (assoc (ui) :phase :computer-thinking :ai-job :pending)]
        (should-not (events/needs-ai-job? thinking))))

    (it "plays a sync AI move with tick-ai"
      (let [state (play-at (ui) 2 3)
            next (let-computer-play state)]
        (should= board/black (:to-move (:game next)))
        (should= board/white (:player (last (:moves (:game next)))))))

    (it "ignores a missing AI move"
      (should= nil (:animation (events/start-animation (ui) nil)))
      (should= :awaiting-human (:phase (events/start-animation (ui) [0 0])))))

  (context "buttons and keys"
    (it "starts a new game of the same color"
      (let [state (click-button (play-at (ui) 2 3) :new-game)]
        (should= board/black (:human (:game state)))
        (should= :awaiting-human (:phase state))
        (should= (board/initial-board) (:board (:game state)))))

    (it "starts as white from the play-white button"
      (let [state (click-button (ui) :play-white)]
        (should= board/white (:human (:game state)))
        (should= :awaiting-computer (:phase state))))

    (it "starts as black from the play-black button"
      (should= board/black (:human (:game (click-button (ui) :play-black)))))

    (it "toggles hints from the button and the h key"
      (should-not (:hints? (click-button (ui) :hints)))
      (should-not (:hints? (events/on-key (ui) :h))))

    (it "starts a new game from n and the number keys"
      (should= :awaiting-human (:phase (events/on-key (play-at (ui) 2 3) :n)))
      (should= board/black (:human (:game (events/on-key (ui) :1))))
      (should= board/white (:human (:game (events/on-key (ui) :2)))))

    (it "ignores unknown keys and unknown buttons"
      (let [state (ui)]
        (should= state (events/on-key state :x))
        (should= state (events/handle-button state :nope))))

    (it "cancels an in-flight animation on undo"
      (let [animating (click-square (ui) 2 3)
            undone (events/on-key animating :u)]
        (should= :awaiting-human (:phase undone))
        (should= nil (:animation undone))
        (should= 0 (:flash-frames undone))
        (should= (board/initial-board) (:board (:game undone)))))

    (it "advances from a pass notice to the human"
      (let [state (assoc (ui) :phase :pass-notice :pass-frames 0)
            mid (nth (iterate events/tick state) (dec anim/pass-display-frames))]
        (should= :pass-notice (:phase mid))
        (should= (dec anim/pass-display-frames) (:pass-frames mid))
        (should= :awaiting-human (:phase (events/tick mid)))
        (should= 0 (:pass-frames (events/tick mid)))))

    (it "advances from a pass notice to the computer"
      (let [state (assoc (events/fresh-ui board/white greedy)
                    :phase :pass-notice
                    :pass-frames (dec anim/pass-display-frames))]
        (should= :awaiting-computer (:phase (events/tick state)))))

    (it "undoes a completed human-and-computer turn"
      (let [state (-> (ui)
                      (play-at 2 3)
                      let-computer-play
                      (events/on-key :u))]
        (should (game/human? (:game state)))
        (should= (board/initial-board) (:board (:game state)))
        (should= 0 (:think-frames state))
        (should= 0 (:flash-frames state))
        (should= :awaiting-human (:phase state))))))
