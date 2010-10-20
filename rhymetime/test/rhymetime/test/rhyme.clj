(ns rhymetime.test.rhyme
  (:use rhymetime.rhyme
        rhymetime.pronounce
        [lazytest.describe :only (describe it given)]
        [lazytest.expect :only (expect)]))

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
  (given [resource (-> (Thread/currentThread) .getContextClassLoader (.getResource "rhymetime/test/test-dict.txt"))
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

