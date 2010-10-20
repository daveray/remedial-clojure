(ns rhymetime.rhyme)

(def -soft-rhyme-classes 
  [#{"AA" "AO" "AW" "OW"}
   #{"AE" "EH"}
   #{"AH"}
   #{"AY"}
   #{"B" "D" "P" "T"}
   #{"CH" "JH" "ZH" "SH" "Z" "G" "K"}
   #{"DH" "TH"}
   #{"ER" "UH" "UW"}
   #{"EY"}
   #{"F" "V"}
   #{"HH"}
   #{"IH" "IY"}
   #{"L"}
   #{"M" "N"}
   #{"NG"}
   #{"OW"}
   #{"OY"}
   #{"R"}
   #{"S"}
   #{"W"}
   #{"Y"}])

(def soft-rhyme-classes 
  (reduce
    (fn [acc [k v]] (assoc acc k v))
    {} 
    (for [klass -soft-rhyme-classes, phone klass] [phone klass])))

(defn phonemes-rhyme?
  "Returns true if two phonemes rhyme"
  [a b]
  (contains? (soft-rhyme-classes (first a)) (first b)))

(defn rhyme-weight
  "Returns true if two words rhyme using the given dictionary"
  [dict a b]
  (let [aphones (reverse (dict a))
        bphones (reverse (dict b))]
    (count (take-while true? (map phonemes-rhyme? aphones bphones)))))
