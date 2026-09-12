(ns othello.architecture-spec
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [speclj.core :refer :all]))

(defn- clj-source-files []
  (->> (file-seq (io/file "src"))
       (filter #(.isFile %))
       (filter #(re-matches #".*\.clj[cs]?" (.getName %)))))

(defn- read-ns-form [file]
  (with-open [reader (io/reader file)]
    (read {:read-cond :allow :features #{:clj} :eof nil}
          (java.io.PushbackReader. reader))))

(defn- lib-name [entry]
  (cond
    (symbol? entry) entry
    (vector? entry) (first entry)
    :else nil))

(defn- required-libs [ns-form]
  (->> (rest ns-form)
       (filter seq?)
       (filter #(contains? #{:require :use} (first %)))
       (mapcat rest)
       (map lib-name)
       (remove nil?)))

(defn- domain-ns? [ns-name]
  (let [s (str ns-name)]
    (or (= s "othello.board")
        (= s "othello.rules")
        (= s "othello.game")
        (= s "othello.ai")
        (str/starts-with? s "othello.ai."))))

(defn- quil-adapter-ns? [ns-name]
  (contains? #{"othello.ui.draw" "othello.ui.sketch" "othello.ui.web"}
             (str ns-name)))

(defn- ui-logic-ns? [ns-name]
  (let [s (str ns-name)]
    (and (str/starts-with? s "othello.ui")
         (not (quil-adapter-ns? ns-name)))))

(defn- quil-lib? [lib]
  (str/starts-with? (str lib) "quil."))

(defn- ui-lib? [lib]
  (str/starts-with? (str lib) "othello.ui"))

(defn- violations [predicate forbidden]
  (for [file (clj-source-files)
        :let [ns-form (read-ns-form file)]
        :when (and (seq? ns-form) (= 'ns (first ns-form)))
        :let [ns-name (second ns-form)]
        :when (predicate ns-name)
        lib (required-libs ns-form)
        :when (forbidden lib)]
    {:ns ns-name :file (str file) :lib lib}))

(describe "architecture"
  (it "keeps the game domain free of Quil and UI adapters"
    (should= [] (violations domain-ns? #(or (quil-lib? %) (ui-lib? %)))))

  (it "keeps UI logic free of Quil"
    (should= [] (violations ui-logic-ns? quil-lib?)))

  (it "keeps the view-model independent of the event state machine"
    (should= [] (violations #(= % 'othello.ui.view) #(= % 'othello.ui.events)))
    (should= [] (violations #(= % 'othello.ui.events) #(= % 'othello.ui.view))))

  (it "confines Processing to draw and sketch"
    (let [quil-owners
          (for [file (clj-source-files)
                :let [ns-form (read-ns-form file)]
                :when (and (seq? ns-form) (= 'ns (first ns-form)))
                :let [ns-name (second ns-form)]
                lib (required-libs ns-form)
                :when (quil-lib? lib)]
            (str ns-name))]
      (should= ["othello.ui.draw" "othello.ui.sketch" "othello.ui.web"]
               (vec (sort (distinct quil-owners)))))))
