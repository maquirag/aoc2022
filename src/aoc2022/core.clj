(ns aoc2022.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn -main []
  (println "Solving Advent of Code 2022"))

(defn input
  "Read a file from resource folder"
  [file]
  (slurp (io/resource file)))

(defn split-by
  "Reverse split arguments for easier threading"
  [regex text]
  (str/split text regex))

(defn d01-add-strings
  "Add numbers in a list of strings"
  [strings]
  (reduce #(+ %1 (Integer/parseInt %2)) 0 strings))

(defn d01-bags
  "Parse the data and summarize each bag"
  [data]
  (let [groups (str/split data #"\n\n")
        elves (map #(str/split % #"\n") groups)]
    (map d01-add-strings elves)))

(defn solve-d01-1
  "Day 1 Task 1"
  [data]
  (->> data
       (d01-bags)
       (apply max)))

(defn solve-d01-2
  "Day 1 Task 2"
  [data]
  (->> data
       (d01-bags)
       (sort)
       (reverse)
       (take 3)
       (reduce +)))

; Day 1 completed

(def d02-move-value {:rock 1 :paper 2 :scissors 3})

(defn d02-match-value [[p1 p2]]
  (cond
    (= p1 p2) 3
    (or (= [p1 p2] [:rock :scissors])
        (= [p1 p2] [:paper :rock])
        (= [p1 p2] [:scissors :paper])) 6
    :else 0))


(defn d02-parse-move [[m1 _ m2]]
  (let [meaning {\A :rock \B :paper \C :scissors
                 \X :rock \Y :paper \Z :scissors}]
    (map meaning [m1 m2])))

(defn d02-parse-expectation [[m1 _ m2]]
  (let [meaning {\A :rock \B :paper \C :scissors
                 \X :lose \Y :draw \Z :win}]
    (map meaning [m1 m2])))

(defn d02-determine-move [pattern]
  (let [path {:rock {:win :paper :draw :rock :lose :scissors}
              :paper {:win :scissors :draw :paper :lose :rock}
              :scissors {:win :rock :draw :scissors :lose :paper}}]
    (get-in path pattern)))

(defn d02-p2-score [[p1 p2]]
  (+ (d02-move-value p2) (d02-match-value [p2 p1])))

(defn solve-d02-1
  "Day 2 Task 1"
  [data]
  (let [moves (map d02-parse-move (str/split data #"\n"))]
    (reduce + (map d02-p2-score moves))))

(defn solve-d02-2
  "Day 2 Task 2"
  [data]
  (->> data
       (split-by #"\n")
       (map d02-parse-expectation)
       (map (fn [[enemy expected]]
              [enemy (d02-determine-move [enemy expected])]))
       (map d02-p2-score)
       (reduce +)))

; Day 2 completed

(defn d03-compartmentalize [bag]
  (split-at (/ (count bag) 2) bag))

(defn d03-find-common [groups]
  (first (apply clojure.set/intersection (map set groups))))

(defn d03-item-value [item]
  (let [offset (if (Character/isLowerCase item) 96 38)]
    (- (int item) offset)))

(defn solve-d03-1
  "Day 3 Task 1"
  [data]
  (->> data
       (split-by #"\n")
       (map d03-compartmentalize)
       (map d03-find-common)
       (map d03-item-value)
       (reduce +)))

(defn solve-d03-2
  "Day 3 Task 2"
  [data]
  (->> data
       (split-by #"\n")
       (partition 3)
       (map d03-find-common)
       (map d03-item-value)
       (reduce +)))

; Day 3 completed

(defn d04-numbers [s]
  (let [pair (split-by #"-" s)
        [start end] (map read-string pair)]
    (set (range start (inc end)))))

(defn d04-ranges [[r1 r2]]
  [(d04-numbers r1) (d04-numbers r2)])

(defn d04-covered? [[r1 r2]]
  (or (every? r1 r2) (every? r2 r1)))

(defn solve-d04-1
  "Day 4 Task 1"
  [data]
  (->> data
       (split-by #"\n")
       (map (partial split-by #","))
       (map d04-ranges)
       (filter d04-covered?)
       count))

(defn solve-d04-2
  "Day 4 Task 2"
  [data]
  (->> data
       (split-by #"\n")
       (map (partial split-by #","))
       (map d04-ranges)
       (filter (fn [[r1 r2]] (some r1 r2)))
       count))

(comment
  (solve-d01-1 (input "dec01.txt"))
  (solve-d01-2 (input "dec01.txt"))
  (solve-d02-1 (input "dec02.txt"))
  (solve-d02-2 (input "dec02.txt"))
  (solve-d03-1 (input "dec03.txt"))
  (solve-d03-2 (input "dec03.txt"))
  (solve-d04-1 (input "dec04.txt"))
  (solve-d04-2 (input "dec04.txt")))
