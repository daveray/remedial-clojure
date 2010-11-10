(ns rhymetime.web
  (:gen-class)
  (:use rhymetime.pronounce
        rhymetime.rhyme
        compojure.core
        ring.adapter.jetty
        ring.middleware.keyword-params
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
  [phonemes depth]
  (let [start (- (count phonemes) depth)]
    (for [[[ph] i] (map vector phonemes (iterate inc 0))]
      (if (< i start)
        ph
        [:b ph]))))

(defn- render-rhyme-results
  [word depth rhymes]
  (let [n (count rhymes)]
    [:div.results
      ; Write header text
      (if (seq rhymes)
          [:span.found "Found " n " rhyme" (if (= n 1) "" "s") " for '" word "'"] ; at depth " depth]
          [:span.unfound "Unknown word '" word "'"])

      ; If there's a result, write the list...
      (when rhymes
        [:ul
          (for [[rhyme i] (map vector rhymes (iterate inc 1))] 
            [:li 
              [:span.count i]
              " "
              [:a {:href (str "?word=" rhyme)} rhyme]
              " "
              [:span.phonemes 
                "[ "
                (interpose " - " (style-phonemes (@dict rhyme) depth))
                ;(apply str (interpose " - " (map first (@dict rhyme))))
                " ]"
               ]
             ])])]))

(defn- render-page
  [word depth]
  [:html
    [:head [:title "RhymeTime"]
     (include-css "/public/stylesheets/application.css")]
    [:body 
      [:div.container 
        [:div.header
          (render-form word)]
        (when word
          (let [normalized (.toUpperCase word)
                depth (if depth (Integer/parseInt depth) (dec (count (@dict normalized))))
                rhymes (sort (@rhymer normalized depth))]
            (render-rhyme-results word depth rhymes)))]]])
 
(defroutes all-routes
  (GET "/" 
    {{ word "word" depth "depth" } :params :as request}
    (html (render-page word depth)))
  (route/resources "/public")
  (route/not-found "Page not found"))

; This doesn't work
;(wrap! all-routes (wrap-keyword-params))

;(use 'rhymetime.web :reload)
;(def server (run {:join? false}))
;(.stop server)

(defn run
  [options]
  (let [options (merge {:port 8080 :join? true } options)]
    (run-jetty (var all-routes) options)))

(defn -main [& args]
  (do
    (dosync
      (ref-set dict (parse-dictionary (first args)))
      (ref-set rhymer (make-rhymer @dict)))
    (run)))

