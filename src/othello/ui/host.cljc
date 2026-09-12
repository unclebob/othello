(ns othello.ui.host
  (:require [othello.board :as board]
            [othello.ui.events :as events]))

(defn initial-state [play-ai]
  (events/fresh-ui board/black play-ai))

(defn update-state [state {:keys [job-done? job-result launch-ai]}]
  (let [s (events/on-frame state {:job-done? job-done? :job-result job-result})]
    (if (events/needs-ai-job? s)
      (launch-ai s)
      s)))

(defn event-xy [event]
  [(int (:x event)) (int (:y event))])

(defn on-press [state event]
  (if (= :right (:button event))
    state
    (let [[x y] (event-xy event)]
      (events/on-click state x y))))

(defn on-move [state event]
  (let [[x y] (event-xy event)]
    (events/on-mouse-move state x y)))

(defn on-key [state event]
  (events/on-key state (:key event)))

;; clj-mutate-manifest-begin
;; {:version 2, :hash-algorithm :sha256-source-v1, :verified? false, :tested-at "2026-09-12T08:46:27.725303-05:00", :module-hash "966c4cad607ab578bff9c58f993dd759a2602f6093e3523773960ec99ef447eb", :provenance {:mutation-rules-version "3", :test-command "clj -M:spec --tag ~no-mutate", :test-roots ["spec"], :test-profile-fingerprint "e484b75f66cdd819ebbd386b124280a03f7445a7a2c6b2346fec4e8f0f72c0d8"}, :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "0d9276f3cb71cd88781f58e12d9713f26e215020a23cd590e8d11d51a3852035"} {:id "defn/initial-state", :kind "defn", :line 5, :end-line 6, :hash "6c9726cfeafdf521250b3eddbea03201b82da67a30f7b2b5eb9440026b92a849"} {:id "defn/update-state", :kind "defn", :line 8, :end-line 12, :hash "fb2f44958bd67f6958f5bf6bf2fd456ecaaf17a90f824a52232fcf4d2c7d4538"} {:id "defn/event-xy", :kind "defn", :line 14, :end-line 15, :hash "d5bc7043eb0ad783ddcda0cdedc90d00174b903f60513e0efa21c4069c0837ed"} {:id "defn/on-press", :kind "defn", :line 17, :end-line 21, :hash "2ee95efc5285e4016b3c5f74ece9014ad7d518b1d95f1d0fc92bb065436bb45e"} {:id "defn/on-move", :kind "defn", :line 23, :end-line 25, :hash "9dc08422bea0928a11d284230154d8737fcc6b2ff77e70c2fa82866ef2f65e97"} {:id "defn/on-key", :kind "defn", :line 27, :end-line 28, :hash "c86ca6d888ac7cc72b8354f3d12bb8eb4603a25f2778b827018a5d11da2240db"}]}
;; clj-mutate-manifest-end
