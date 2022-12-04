(ns aoc2022.core-test
  (:require [clojure.test :refer :all]
            [aoc2022.core :refer :all]))

(deftest d01-sample-test
  (testing "Day 1 Task 1 sample"
    (is (= 24000 (solve-d01-1 (input "dec01sample.txt")))))
  (testing "Day 1 Task 2 sample"
    (is (= 45000 (solve-d01-2 (input "dec01sample.txt"))))))

(deftest d02-sample-test
  (testing "Day 2 Task 1 sample"
    (is (= 15 (solve-d02-1 (input "dec02sample.txt")))))
  (testing "Day 2 Task 2 sample"
    (is (= 12 (solve-d02-2 (input "dec02sample.txt"))))))

(deftest d03-sample-test
  (testing "Day 3 Task 1 sample"
    (is (= 157 (solve-d03-1 (input "dec03sample.txt")))))
  (testing "Day 3 Task 2 sample"
    (is (= 70 (solve-d03-2 (input "dec03sample.txt"))))))

(deftest d04-sample-test
  (testing "Day 4 Task 1 sample"
    (is (= 2 (solve-d04-1 (input "dec04sample.txt")))))
  (testing "Day 4 Task 2 sample"
    (is (= 4 (solve-d04-2 (input "dec04sample.txt"))))))
