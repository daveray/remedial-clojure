(ns rhymetime.test.pronounce
  (:use rhymetime.pronounce
        [lazytest.describe :only (describe it given)]
        [lazytest.expect :only (expect)]))

(describe parse-phoneme
  (it "can parse a phoneme with no stress"
    (= ["K" nil] (parse-phoneme "K")))
  (it "can parse a phoneme with null stress"
    (= ["AH" :n] (parse-phoneme "AH0")))
  (it "can parse a phoneme with primary stress"
    (= ["AH" :p] (parse-phoneme "AH1")))
  (it "can parse a phoneme with null stress"
    (= ["AH" :s] (parse-phoneme "AH2"))))

(describe parse-entry
  (it "can parse an entry with a name and phoneme list"
    (= { :word "CLOSURE" 
         :phonemes [["K" nil] ["L" nil]["OW" :p] ["ZH" nil] ["ER" :n]] } 
       (parse-entry "CLOSURE K L OW1 ZH ER0"))))

(describe parse-dictionary
  (given [line1 "CLOSURE K L OW1 ZH ER0"
          line2 "MACARONI  M AE2 K ER0 OW1 N IY0"
          expected {"CLOSURE"  (:phonemes (parse-entry line1))
                    "MACARONI" (:phonemes (parse-entry line2))}
          input (fn [& s] (java.io.StringReader. (apply str s)))]

  (it "can parse multiple entries"
    (= expected (parse-dictionary (input line1 "\n" line2))))

  (it "can parse a dictionary with comments"
    (= expected (parse-dictionary (input "; comment\n" line1 "\n;;; another\n" line2))))

  (it "can parse a dictionary with empty lines"
    (= expected (parse-dictionary (input line1 "\n\n   \n" line2))))))

