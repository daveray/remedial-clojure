(ns rhymetime.core
  (:use [rhymetime.pronounce :only (parse-dictionary)])
  (:gen-class))

(defn -main
  [& args]
  (time  (println ((parse-dictionary (first args)) (second args)))))

