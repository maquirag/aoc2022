(ns aoc2022.util
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn parse [file]
  (-> file io/resource slurp str/split-lines))
