(ns rhymetime.test.rhyme
  (:use rhymetime.rhyme
        rhymetime.pronounce
        lazytest.describe
        [lazytest.expect :only (expect)]))

(describe "with a test dictionary"
  (given [resource (.. (Thread/currentThread) 
                        getContextClassLoader 
                        (getResource "rhymetime/test/test-dict.txt"))
          dict (parse-dictionary resource)
          lisp (dict "LISP")
          asp  (dict "ASP")
          betty (dict "BETTY")]

    (testing make-rhyme-tree
      (it "builds a rhyme tree from a pronouncing dictionary where words are
          indexed by reversed soft rhyme classes. The words at a given node
          are stored under the :words key"
        (let [tree (make-rhyme-tree dict)]
          (and
            (= ["BETTY"] (:words (get-in tree (rhyme-tree-path-for betty))))
            (= ["LISP"] (:words (get-in tree (rhyme-tree-path-for lisp))))
            (= ["ASP"] (:words (get-in tree (rhyme-tree-path-for asp))))))))

    (testing make-rhyme-calculator
      (it "returns a function that calculates rhymes for a word"
        (let [rhymes (make-rhyme-calculator dict)]
          (= #{"BETTY" "READY" "SPAGHETTI" "MACARONI"} (set (rhymes "MACARONI" 1)))
          (= #{"BETTY" "READY" "SPAGHETTI" } (set (rhymes "BETTY" 2)))))
      (it "returns a function that returns an empty seq for unknown words"
        (let [rhymes (make-rhyme-calculator dict)]
          (empty? (rhymes "MACARONIX" 1))))
             )))


(describe phonemes-rhyme?
  (it "returns true if two phonemes are the same"
    (phonemes-rhyme? ["AH" :p] ["AH" :p]))
  (it "returns true if two phonemes are the same with a different stress"
    (phonemes-rhyme? ["AH" :p] ["AH" :s]))
  (it "returns true if two phonemes are in the same soft rhyme class"
    (phonemes-rhyme? ["AA" :p] ["AO" :p]))
  (it "returns true if two phonemes are in the same soft rhyme class"
    (phonemes-rhyme? ["D" nil] ["T" nil]))
  (it "returns false if two phonemes are not in the same soft rhyme class"
    (not (phonemes-rhyme? ["AA" :p] ["AE" :p]))))

(describe rhyme-weight
  (given [resource (.. (Thread/currentThread) 
                        getContextClassLoader 
                        (getResource "rhymetime/test/test-dict.txt"))
          dict (parse-dictionary resource)]
    (it "returns that BETTY and READY rhyme "
      (= 3 (rhyme-weight dict "READY" "BETTY")))
    (it "returns that LISP and ASP rhyme a little"
      (= 2 (rhyme-weight dict "LISP" "ASP")))
    (it "returns that LISP and ASP rhyme a little"
      (= 1 (rhyme-weight dict "MACARONI" "SPAGHETTI")))
    (it "returns that ASMODEUS and ASPARAGUS rhyme"
      (= 2 (rhyme-weight dict "ASMODEUS" "ASPARAGUS")))
    (it "returns zero if words don't rhyme"
      (= 0 (rhyme-weight dict "MACARONI" "LISP")))))

