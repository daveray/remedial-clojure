(ns rhymetime.web
  (:gen-class)
  (:use rhymetime.pronounce
        rhymetime.rhyme
        compojure.core
        ring.adapter.jetty
        hiccup.core
        hiccup.form-helpers)
  (:require [compojure.route :as route]))

(def dict (ref (parse-dictionary "test/rhymetime/test/test-dict.txt")))
(def rhymer (ref (make-rhymer @dict)))

(defn- render-form
  [word]
  (form-to {:class "form"} [:get "/"] 
          (label :word "Find rhymes for: ")
          (text-field :word word)))

(defn- render-rhyme-results
  [word depth rhymes]
  (let [n (count rhymes)]
    [:div.results
      ; Write header text
      (if rhymes
          [:em "Found " n " rhyme" (if (= n 1) "" "s") " for '" word "'"] ; at depth " depth]
          [:em "Unknown word '" word "'"])

      ; If there's a result, write the list...
      (when rhymes
        [:ul
          (for [rhyme rhymes] 
            [:li 
              [:a {:href (str "?word=" rhyme)} rhyme]])])]))

(defn- render-page
  [word depth]
  [:html
    [:head [:title "RhymeTime"]]
    [:body 
      [:h1 "Rhyme Finder"]
      (render-form word)
      (when word
        (let [normalized (.toUpperCase word)
              depth (if depth (Integer/parseInt depth) (dec (count (@dict normalized))))
              rhymes (sort (@rhymer normalized depth))]
          (render-rhyme-results word depth rhymes)))]])
 
(defroutes webservice
  (GET "/" 
    {query-params :query-params} 
    (html (render-page (query-params "word") (query-params "depth"))))
  (route/not-found "Page not found"))


(defn -main [& args]
  (do
    (dosync
      (ref-set dict (parse-dictionary (first args)))
      (ref-set rhymer (make-rhymer @dict)))
    (run-jetty webservice {:port 8080} )))


; {:remote-addr 0:0:0:0:0:0:0:1, 
;  :scheme :http, 
;  :query-params {word bar}, 
;  :form-params {}, 
;  :request-method :get, 
;  :query-string word=bar, 
;  :route-params {}, 
;  :content-type nil, :cookies {}, :uri /foo, :server-name localhost, :params {word bar}, :headers {connection keep-alive, keep-alive 300, accept-charset ISO-8859-1,utf-8;q=0.7,*;q=0.7, accept-encoding gzip,deflate, accept-language en-us,en;q=0.5, accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8, user-agent Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko/2009033100 Ubuntu/9.04 (jaunty) Firefox/3.0.8, host localhost:8080}, :content-length nil, :server-port 8080, :character-encoding nil, :body #<Input org.mortbay.jetty.HttpParser$Input@3e1d25>}
