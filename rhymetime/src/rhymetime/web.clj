(ns rhymetime.web
  (:gen-class)
  (:use rhymetime.pronounce
        rhymetime.rhyme
        compojure.core
        ring.adapter.jetty
        ring.middleware.keyword-params
        ring.middleware.params
        ring.util.response
        ring.util.codec
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers)
  (:require [compojure.route :as route]))

(def dict (ref (parse-dictionary "test/rhymetime/test/test-dict.txt")))
(def rhymer (ref (make-rhymer @dict)))

(defn- pick-a-random-word
  []
  (nth (seq @dict) (rand-int (count @dict))))

(defn- render-form
  [word depth]
  (form-to {:class "form"} [:get "/"] 
          (label :word "Find rhymes for ")
          (text-field :word word)
          ;(label :depth " with depth ")
          ;(text-field {:size 4} :depth depth)
          (submit-button "Go")))

(defn- style-phonemes
  "Make the last 'depth' phonemes bold."
  [phonemes depth]
  (let [start (- (count phonemes) depth)
        [leading trailing] (split-at start (map first phonemes))]
    (concat leading (for [ph trailing] [:b ph]))))

(defn- render-result-header
  [word depth n]
  (let [phonemes (@dict word)]
    [:div
      [:span.found 
       "Found " n " rhyme" (if (= n 1) "" "s") " for '" word "' at depth " depth ". "
       "Try depth ... " 
       (for [i (range 1 (count phonemes)) :when (not= i depth)]
          [:a {:href (str "/?word=" word "&depth=" i)} i " "])]]))

(defn- render-rhyme-results
  [word depth rhymes]
  (let [n (count rhymes)]
    [:div.results
      ; Write header text
      (if (seq rhymes)
          (render-result-header word depth n)
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
          (render-form word depth)
          [:a {:href "random"} "Surprise Me"]]
        (when word
          (let [normalized (.toUpperCase word)
                depth      (if depth (Integer/parseInt depth) (dec (count (@dict normalized))))
                rhymes     (sort (@rhymer normalized depth))]
            (render-rhyme-results normalized depth rhymes)))
       [:div.footer [:em "That's it"]]]]])

(defn index
  [{ { word :word depth :depth } :params 
     { last-seen "last-seen" } :cookies }]
  { :status 200
    :body   (html (render-page word depth))})

(defn random
  [request]
  (let [words (keys @dict)
        word  (pick-a-random-word)]
    (redirect (str "/?word=" (url-encode (first word)) "&depth=" (dec (count (second word)))))))

(defroutes all-routes
  (GET "/" request (index request)) 
  (GET "/random" request (random request)) 
  (route/resources "/public")
  (route/not-found "Page not found"))

; The extra :params wrapper is necessary to force :params to be unpacked before
; the keyword param processing happens. This is a quirk of compojure.
(wrap! all-routes :keyword-params :params)

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
    (run {})))

