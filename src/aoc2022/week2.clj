(ns aoc2022.week2
  (:gen-class)
  (:require [aoc2022.util :refer [parse]])
  (:require [clojure.string :as str])
  (:require [clojure.walk :as walk]))

; Day 5

(defn str->move [raw-move]
  (->> raw-move
       (re-seq #"\d+") (map #(Integer/valueOf %))
       (into [])))

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
                   (partition 3))]
    [stacks moves]))

(defn move-stack [stack [times from to]]
  (loop [st stack x times s from t to]
    (if (zero? x) st
        (recur (-> st
                   (update-in [t]
                              #(cons (first (get-in st [s])) %))
                   (update-in [s] #(drop 1 %)))
               (dec x) s t))))

(defn move-stack-advanced [stack [times from to]]
  (-> stack
      (update-in [to]
                 #(flatten (cons (take times (get-in stack [from])) %)))
      (update-in [from] #(drop times %))))

(defn day5-task1 [data]
  (->>
   (loop [[stacks moves] (day5-parse data)]
     (if (empty? moves) stacks
         (recur [(move-stack stacks (first moves))
                 (drop 1 moves)])))
   vals
   (map first)
   (apply str)))

(defn day5-task2 [data]
  (->>
   (loop [[stacks moves] (day5-parse data)]
     (if (empty? moves) stacks
         (recur [(move-stack-advanced stacks (first moves))
                 (drop 1 moves)])))
   vals
   (map first)
   (apply str)))

(comment
  (day5-task1 (parse "dec05sample.txt"))
  (day5-task1 (parse "dec05.txt"))
  (day5-task2 (parse "dec05sample.txt"))
  (day5-task2 (parse "dec05.txt")))

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

(defn dir-size [[_ v]]
  (if (int? v) v
      (reduce + (map dir-size v))))

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
  (def d (day7-task1 (parse "dec07sample.txt")))
  (clojure.walk/postwalk-demo d)
  (dir-size d))

; the lines must be traversed keeping cwd in state
; directory structure represented as nested maps?
; key is the name of file or directory
; in case of dir, value is another map
; in case of file, value is the size
; state = ["/" "a"]
; {"\" {"a" {}}}

(defn map->edges [m]
  (for [entry m
        [x m] (tree-seq some? val entry)
        ;y (or (keys m) [m])
        ]
    [x]))
(def m
  {:a {:b {:c nil
           :d nil}
       :e nil}})
(map->edges m)
;(map->edges d)

; ideas from slack...
(comment
  (defn assign-size-to-directories [tree]
    (walk/postwalk (fn [node]
                     (if (map? node)
                       {:size (reduce + (concat
                                         (filter number? (vals node))
                                         (keep :size (vals node))))
                        :children node}
                       node))
                   tree))

  {"b.txt" 14848514,
   "c.dat" 8504156,
   "a" {"f" 29116, "g" 2557, "h.lst" 62596, "e" {"i" 584}},
   "d" {"j" 4060174, "d.log" 8033020, "d.ext" 5626152, "k" 7214296}})

