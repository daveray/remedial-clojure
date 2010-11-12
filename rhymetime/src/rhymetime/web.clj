(ns rhymetime.web
  (:gen-class)
  (:use rhymetime.pronounce
        rhymetime.rhyme
        compojure.core
        ring.adapter.jetty
        ring.middleware.keyword-params
        ring.util.response
        ring.util.codec
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers)
  (:require [compojure.route :as route]))

(def dict (ref (parse-dictionary "test/rhymetime/test/test-dict.txt")))
(def rhymer (ref (make-rhymer @dict)))

(defn- render-form
  [word]
  (form-to {:class "form"} [:get "/"] 
          (label :word "Find rhymes for ")
          (text-field :word word)))

(defn- style-phonemes
  "Make the last 'depth' phonemes bold."
  [phonemes depth]
  (let [start (- (count phonemes) depth)
        [leading trailing] (split-at start (map first phonemes))]
    (concat leading (for [ph trailing] [:b ph])))) 

(defn- render-rhyme-results
  [word depth rhymes]
  (let [n (count rhymes)]
    [:div.results
      ; Write header text
      (if (seq rhymes)
          [:span.found "Found " n " rhyme" (if (= n 1) "" "s") " for '" word "'"]
          [:span.unfound "Unknown word '" word "'"])

      ; If there's a result, write the list...
      (when (seq rhymes)
        [:table ; yeah, a table.
          (for [[rhyme i] (map vector rhymes (iterate inc 1))] 
            [:tr 
              [:td.count i]
              [:td [:a {:href (str "?word=" rhyme)} rhyme]]
              [:td.phonemes
                "[ "
                (interpose " - " (style-phonemes (@dict rhyme) depth))
                " ]"]
             ])])]))

(defn- render-page
  [word depth]
  [:html
    [:head [:title "RhymeTime"]
     (include-css "/public/stylesheets/application.css")]
    [:body 
      [:div.container 
        [:div.header
          (render-form word)
          [:a {:href "random"} "Random word"]]
        (when word
          (let [normalized (.toUpperCase word)
                depth      (if depth (Integer/parseInt depth) (dec (count (@dict normalized))))
                rhymes     (sort (@rhymer normalized depth))]
            (render-rhyme-results word depth rhymes)))
       [:div.footer [:em "That's it"]]]]])

(defn index
  [{ { word "word" depth "depth" } :params 
     { last-seen "last-seen" } :cookies }]
  { :status 200
    :body   (html (render-page word depth))})

(defn random
  [request]
  (let [words (keys @dict)
        word  (nth words (rand-int (count words)))]
    (redirect (str "/?word=" (url-encode word)))))

(defroutes all-routes
  (GET "/" request (index request)) 
  (GET "/random" request (random request)) 
  (route/resources "/public")
  (route/not-found "Page not found"))

; This doesn't work
;(def all-routes (wrap-keyword-params all-routes))
;(wrap! all-routes (:keyword-params))

;(use 'rhymetime.web :reload)
;(def server (run {:join? false}))
;(.stop server)

(defn run
  "Run Jetty server. Options default to {:port 8080 :join? true}"
  [options]
  (let [options (merge {:port 8080 :join? true } options)]
    (run-jetty (var all-routes) options)))

(defn load-dictionary
  [file]
  (dosync
        (ref-set dict   (parse-dictionary file))
        (ref-set rhymer (make-rhymer @dict))))

(defn -main [& args]
  (do
    (load-dictionary (first args))
    (run)))

