(ns histowords.test.core
  (:use [lazytest.describe :only (describe it)])
  (:use histowords.core))

(describe gather-words
  (it "splits words on whitespace"
    (= ["mary" "had" "a" "little" "lamb"] (gather-words "   mary had a\tlittle\n   lamb    ")))
  (it "removes punctuation"
    (= ["mary" "had" "a" "little" "lamb"] (gather-words "., mary, had... a little; lamb!")))
  (it "converts words to lower case"
    (= ["mary" "had" "a" "little" "lamb"] (gather-words "., MaRy, hAd... A liTTle; lAmb!"))))

(describe count-words
  (it "counts words into a map"
    (= {"mary" 2 "why" 3 } (count-words ["why" "mary" "why" "mary" "why"]))))

(describe sort-counted-words
  (it "sorts and returns a list of word/count pairs"
    (= [["a" 1] ["b" 2] ["c" 3]] (sort-counted-words {"b" 2 "c" 3 "a" 1}))))

(describe repeat-str
  (it "returns the empty string if count is zero"
    (= "" (repeat-str "*" 0)))
  (it "repeats the input string n times"
    (= "xxxxx" (repeat-str "x" 5))))

(describe histogram-entry
  (it "can generate a single histogram entry"
    (= "betty   ######" (histogram-entry ["betty" 6] 7))))

(describe histogram
  (it "can generate a histogram from word counts"
    (= "mary ##\nwhy  ###\n" (histogram [["mary" 2] ["why" 3]]))))

