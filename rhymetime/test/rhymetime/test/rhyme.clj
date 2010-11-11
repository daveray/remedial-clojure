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
          betty (dict "BETTY")
          bear (dict "BEAR")]

    (testing make-rhyme-tree
      (it "builds a rhyme tree from a pronouncing dictionary where words are
          indexed by reversed soft rhyme classes. The words at a given node
          are stored under the :words key"
        (let [tree (make-rhyme-tree dict)]
          (and
            (= ["BETTY"]       (:words (get-in tree (rhyme-tree-path-for betty))))
            (= ["LISP"]        (:words (get-in tree (rhyme-tree-path-for lisp))))
            (= ["BEAR" "BARE"] (:words (get-in tree (rhyme-tree-path-for bear))))
            (= ["ASP"]         (:words (get-in tree (rhyme-tree-path-for asp))))))))

    (testing make-rhymer
      (it "returns a function that calculates rhymes for a word"
        (let [rhymer (make-rhymer dict)]
          (= #{"BETTY" "READY" "SPAGHETTI" "MACARONI"} (set (rhymer "MACARONI" 1)))
          (= #{"BETTY" "READY" "SPAGHETTI" } (set (rhymer "BETTY" 2)))))
      (it "returns a function that returns an empty seq for unknown words"
        (let [rhymer (make-rhymer dict)]
          (empty? (rhymer "MACARONIX" 1)))))))


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

