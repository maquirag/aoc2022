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
; clojure.zip
; clojure.walk
; tree-seq


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


; Day 8

(defn raw->grid [lines]
  (vec (map (fn [line] (vec (map #(Character/digit % 10) line))) lines)))

(def transpose
  (memoize (fn [grid] (vec (apply map vector grid)))))

(defn visible? [m n grid]
  (if (or (zero? m) (zero? n)
          (= (inc m) (count grid))
          (= (inc n) (count (first grid)))) true
      (let [row (grid m)
            col ((transpose grid) n)
            left (subvec row 0 n)
            right (subvec row (inc n))
            top (subvec col 0 m)
            bottom (subvec col (inc m))
            tree (row n)]
        (or (every? #(< % tree) left) (every? #(< % tree) right)
            (every? #(< % tree) top) (every? #(< % tree) bottom)))))

(defn tree-score [tree trees]
  (inc (count (take-while #(< % tree) (butlast trees)))))

(defn scenic-score [m n grid]
  (let [row (grid m)
        col ((transpose grid) n)
        tree (row n)
        left (tree-score tree (reverse (subvec row 0 n)))
        right (tree-score tree (subvec row (inc n)))
        top (tree-score tree (reverse (subvec col 0 m)))
        bottom (tree-score tree (subvec col (inc m)))]
    (* left right top bottom)))

(defn day8-task1 [data]
  (let [grid (raw->grid data)
        visible (for [a (range (count grid))
                      b (range (count (first grid)))]
                  (visible? a b grid))]
    (reduce + (map {true 1 false 0} visible))))

(defn day8-task2 [data]
  (let [grid (raw->grid data)
        scenic (for [a (range 1 (dec (count grid)))
                     b (range 1 (dec (count (first grid))))]
                 (scenic-score a b grid))]
    (apply max scenic)))

(comment
  (day8-task1 (parse "dec08sample.txt"))
  (day8-task1 (parse "dec08.txt"))
  (day8-task2 (parse "dec08sample.txt"))
  (day8-task2 (parse "dec08.txt")))

; Day 9 - Rope Bridge

(defn rope-instr [line]
  (let [[dir times] (str/split line #" ")]
    [(first dir) (Integer/valueOf times)]))

(defn move-head [[x y] direction]
  (condp = direction
    \R [(inc x) y]
    \L [(dec x) y]
    \U [x (inc y)]
    \D [x (dec y)]))

(defn move-tail [[hx hy] [tx ty]]
  (let [sx (- hx tx)
        sy (- hy ty)
        dx (Integer/signum sx)
        dy (Integer/signum sy)]
    [(+ tx (if (every? #{-1 0 1} [sx sy]) 0 dx))
     (+ ty (if (every? #{-1 0 1} [sx sy]) 0 dy))]))

(defn rope
  "Advance the small rope with head and tail"
  [[head tail visited] direction]
  (let [newhead (move-head head direction)
        newtail (move-tail newhead tail)]
    [newhead newtail (conj visited newtail)]))

(defn rope-dir
  "Get the next rope state.
   State includes head, tail, and set of visited nodes.
   Finally a vector of direction (RLUD) and the number of steps."
  [state [dir times]]
  (reduce rope state (repeat times dir)))

(defn long-rope
  "Advance the long rope in one direction"
  [[rope-parts visited] direction]
  (let [new-parts
        (loop [head [] tail rope-parts]
          (if (empty? tail) head
              (recur (if (empty? head)
                       [(move-head (first tail) direction)]
                       (conj head (move-tail (last head) (first tail))))
                     (drop 1 tail))))]
    [new-parts (conj visited (last new-parts))]))

(defn long-rope-dir [state [dir times]]
  (reduce long-rope state (repeat times dir)))

(loop [head [] tail [[3 0] [2 0] [1 1]]]
  (if (empty? tail) head
      (recur (if (empty? head) [(move-head (first tail) \R)]
                 (conj head (move-tail (last head) (first tail))))
             (drop 1 tail))))

(defn day9-task1 [data]
  (->> data
       (map rope-instr)
       (reduce rope-dir [[0 0] [0 0] #{[0 0]}])
       last count))

(defn day9-task2 [data]
  (->> data
       (map rope-instr)
       (reduce long-rope-dir [(into [] (repeat 10 [0 0])) #{[0 0]}])
       last count))

(comment
  (day9-task1 (parse "dec09sample.txt"))
  (day9-task1 (parse "dec09.txt"))
  (day9-task2 (parse "dec09sample2.txt"))
  (day9-task2 (parse "dec09.txt")))

; Day 10 - Cathode Ray Tube

(defn eval-sign [line]
  (if-let [num (re-find #"-?\d+" line)]
    [0 (Integer/valueOf num)]
    [0]))

(defn day10-task1 [data]
  (->> data
       (map eval-sign)
       (reduce into [0 1])
       (reductions +)
       (keep-indexed #(when (zero? (rem (- %1 20) 40)) (* %1 %2)))
       (reduce +)))

(defn sprite [x] #{(dec x) x (inc x)})

(defn pixel [idx register] (if ((sprite register) (rem idx 40)) "#" "."))

(defn day10-task2 [data]
  (->> data
       (map eval-sign)
       (reduce into [1])
       (reductions +)
       (map-indexed pixel)
       (partition 40)
       (map #(apply str %))))

(comment
  (apply str (interpose "\n" (map str/join (partition 4 "############")))))

(comment
  (day10-task1 (parse "dec10sample.txt"))
  (day10-task1 (parse "dec10.txt"))
  (day10-task2 (parse "dec10sample.txt"))
  (day10-task2 (parse "dec10.txt")))

