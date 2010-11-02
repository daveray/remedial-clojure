(defproject rhymetime "1.0.0-SNAPSHOT"
  :description "Rhyming dictionary example for remedial Clojure"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.2"]
                 [hiccup "0.3.0"]
                 [ring/ring-jetty-adapter "0.3.1"]]
  :dev-dependencies [[com.stuartsierra/lazytest "1.1.2"]]
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}
  :main rhymetime.web)
