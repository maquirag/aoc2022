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

(solve-d01-1 (input "dec01.txt"))
(solve-d01-2 (input "dec01.txt"))
