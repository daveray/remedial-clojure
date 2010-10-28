(ns histowords.core
  (:use [clojure.contrib.str-utils :only (str-join)])
  (:gen-class))

(defn gather-words 
  "Given a string, return a list of lower-case words with whitespace and
   punctuation removed"
  [s]
  (map #(.toLowerCase %) (filter #(not (.isEmpty %)) (seq (.split #"[\s\W]+" s)))))
(def x {})
(defn count-words 
  "Take a seq of words and return a map from word to word count"
  [words]
  (reduce 
    (fn [m w] (assoc m w (inc (m w 0)))) 
    {} 
    words))

(defn sort-counted-words 
  "Given a sequence of word/count pairs, sort by count"
  [words]
  (sort-by second words))

(defn repeat-str 
  "Repeat a string n times"
  [s n]
  (str-join "" (repeat n s)))

(defn histogram-entry 
  "Make a histogram entry for a word/count pair and maximum word width"
  [[w n] width]
  (let [r (- width (.length w))]
    (str w (repeat-str " " r) " " (repeat-str "#" n))))

(defn histogram 
  "Make a histogram for a seq of word/count pairs"
  [words]
  (let [width (apply max (map #(.length (% 0)) words))]
    (str-join "\n" 
        (map #(histogram-entry % width) words))))

(defn -main [& args]
  (println 
    (histogram 
      (sort-counted-words 
        (count-words 
          (gather-words 
            (slurp (first args))))))))

