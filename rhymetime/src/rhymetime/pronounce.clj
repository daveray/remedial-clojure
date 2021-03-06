(ns rhymetime.pronounce
  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:use [clojure.contrib.str-utils :only (re-split)]))

(defn parse-phoneme
  "Parse a phoneme into a phone/accent pair"
  [s]
  (let [[_ phone stress] (re-matches #"([A-Z]+)([012])?" s)]
    [phone (case stress "0" :n "1" :p "2" :s nil)]))

(defn parse-entry
  "Parse a dictionary entry"
  [s]
  (let [parts (re-split #"\s+" s)]
    { :word     (first parts) 
      :phonemes (map parse-phoneme (rest parts)) }))

(defn- is-dictionary-entry?
  "Returns true if the given line is a dictionary entry"
  [line]
  (and (not (empty? line))  ; (seq line) is more idiomatic!
       (not (.startsWith line ";"))))

(defn parse-dictionary
  [reader]
  (let [lines    (read-lines reader)
        trimmed  (map #(.trim %1) lines)
        filtered (filter is-dictionary-entry? trimmed)
        parsed   (map parse-entry filtered)]
    (reduce #(assoc %1 (:word %2) (:phonemes %2)) {}  parsed)))

; Alternate ->> implementation suggested on the Clojure Group
;(defn parse-dictionary
  ;[reader]
  ;(->> (read-lines reader)
       ;(map #(.trim %1))
       ;(filter is-dictionary-entry?)
       ;(map parse-entry)
       ;(reduce #(assoc %1 (:word %2) (:phonemes %2)) {})))
