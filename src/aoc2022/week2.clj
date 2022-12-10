(ns aoc2022.week2
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [aoc2022.util :refer [parse]]))

; Day 5

(defn str->move [raw-move]
  (let [[times from to]
        (->> raw-move (re-seq #"\d+") (map #(Integer/valueOf %)))]
    (repeat times [from to])))

(defn str->bucket [raw-stack]
  (->> raw-stack
       (concat " ")
       (partition 4)
       (map #(nth % 2))
       (apply str)))

(defn day5-parse [lines]
  (let [[raw-stacks _ raw-moves] (partition-by str/blank? lines)
        stacks (->> raw-stacks
                    (drop-last)
                    (map str->bucket)
                    (apply mapv vector)
                    (map (fn [c] (filter #(not= \space %) c)))
                    (zipmap (map inc (range)))
                    (into (sorted-map)))
        moves (->> raw-moves
                   (map str->move)
                   (apply conj)
                   (flatten)
                   (partition 2))]
    [stacks moves]))

(defn move-stack [stack [from to]]
  (-> stack
      (update-in [to] #(cons (first (get-in stack [from])) %))
      (update-in [from] #(drop 1 %))))

(defn day5-task1 [data]
  (->>
   (loop [[stacks moves] (day5-parse data)]
     (if (empty? moves) stacks
         (recur [(move-stack stacks (last moves))
                 (butlast moves)])))
   vals
   (map first)
   (apply str)))

(comment
  (day5-task1 (parse "dec05.txt")))

; TODO DAY 5 TASK 2

; Day 6

(defn find-marker [n msg]
  (->> (partition n 1 msg)
       (map-indexed (fn [i s] [(+ n i) (set s)]))
       (filter #(= n (count (second %))))
       first first))

(defn day6-task1 [data]
  (->> (first data) (find-marker 4)))

(defn day6-task2 [data]
  (->> (first data) (find-marker 14)))

(comment
  (day6-task1 (parse "dec06.txt"))
  (day6-task2 (parse "dec06.txt")))

; Day 7

(defn day7-task1 [data]
  (loop [cmds (drop 1 data) cwd ["/"] dir {"/" {}}]
    (if (empty? cmds) dir
        (let [cmd (first cmds)
              [w1 w2 & ws] (str/split cmd #" ")
              newcwd (cond
                   ;(every? #(Character/isDigit %) w1)
                       (= cmd "$ cd ..") (drop-last cwd)
                       (= [w1 w2] ["$" "cd"]) (into [] (concat cwd ws))
                       :else cwd)
              newdir (cond
                       (= w1 "dir")
                       (assoc-in dir cwd
                                 (into (get-in dir cwd)
                                       {w2 {}}))
                       (every? #(Character/isDigit %) w1)
                       (assoc-in dir cwd
                                 (into (get-in dir cwd)
                                       {w2 (Integer/parseInt w1)}))
                       :else dir)]
          (recur (drop 1 cmds) newcwd newdir)))))

; TODO to finish


(comment
  (clojure.pprint/pprint (day7-task1 (parse "dec07sample.txt"))))

; the lines must be traversed keeping cwd in state
; directory structure represented as nested maps?
; key is the name of file or directory
; in case of dir, value is another map
; in case of file, value is the size
; state = ["/" "a"]
; {"\" {"a" {}}}