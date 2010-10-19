(ns rhymetime.pronounce
  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:use [clojure.contrib.str-utils :only (re-split)]))

(defn parse-phoneme
  "Parse a phoneme into a phone/accent pair"
  [s]
  (let [[full phone stress] (re-matches #"([A-Z]+)([012])?" s)
        stress-map { nil nil "0" :n "1" :p "2" :s }]
    [phone (stress-map stress)]))

(defn parse-entry
  "Parse a dictionary entry"
  [s]
  (let [parts (re-split #"\s+" s)]
    { :word     (first parts) 
      :phonemes (map parse-phoneme (rest parts)) }))

(defn is-dictionary-entry?
  [line]
  (not (or (.isEmpty line) 
           (.startsWith line ";"))))

(defn parse-dictionary
  [reader]
  (let [trimmed  (map #(.trim %1) (read-lines reader))
        filtered (filter is-dictionary-entry? trimmed)
        parsed   (map parse-entry filtered)]
    (reduce #(assoc %1 (:word %2) (:phonemes %2)) {}  parsed)))

