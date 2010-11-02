(ns rhymetime.rhyme)

(def soft-rhyme-classes-seed
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
  ^{ :doc "A map from phoneme to soft rhyme class" }
  (reduce
    (fn [acc [k v]] (assoc acc k v))
    {} 
    (for [klass soft-rhyme-classes-seed, phone klass] [phone klass])))

(defn rhyme-tree-path-for
  "Given a word pronunciation, return its path in a rhyme tree"
  [phonemes]
  (->>
    phonemes
    (map first)
    (map soft-rhyme-classes)
    reverse))

(defn- add-word-to-rhyme-tree
  "Given a rhyme tree and a parsed word, add it to the tree and return
   a new tree"
  [tree [word phonemes]]
  (let [path (rhyme-tree-path-for phonemes)
        current (get-in tree path)]
    (assoc-in tree path (assoc current :words (conj current word)))))
 
(defn make-rhyme-tree
  "Make a rhyme tree from a pronouncing dictionary. This constructs the
   raw tree structure. See make-rhyme-calculator for the actual rhyme
   calculation"
  [dict]
  (reduce add-word-to-rhyme-tree {} dict))

(defn- collect-words-in-sub-tree
  "Returns a lazy seq of all the words in the :words keys of the given
   sub-tree."
  [coll]
  (lazy-seq 
    (when-let [s (seq coll)]
      (let [[k v] (first s) r (rest s)]
        (concat (if (= :words k) 
                  v 
                  (collect-words-in-sub-tree v))
                (collect-words-in-sub-tree r))))))

(defn make-rhyme-calculator
  "Returns a function that can lookup up rhymes for a word in the given dictionary"
  [dict]
  (let [tree (make-rhyme-tree dict)]
    (fn [word depth] 
      (when-let [phonemes (dict word)]
        (let [path     (take depth (rhyme-tree-path-for phonemes))
              sub-tree (get-in tree path)]
          (collect-words-in-sub-tree sub-tree))))))

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
