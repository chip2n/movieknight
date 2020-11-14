(ns app.state
  (:require [reagent.core :as r]
            [clojure.spec.alpha :as spec]))

(defonce state
  (r/atom
   {:users [{:id "andreas" :name "Andreas Arvidsson"}
            {:id "henning" :name "Henning Phan"}
            {:id "sebastian" :name "Sebastian Lagerman"}
            {:id "henrik" :name "Henrik Nyström"}
            {:id "joppe" :name "Joppe Widstam"}]
    :user-votes {"andreas" {"movie1" true "movie2" true "movie3" true "movie4" true}
                 "henning" {"movie1" true "movie2" false "movie3" true "movie4" false}
                 "sebastian" {"movie1" true "movie2" true "movie3" true "movie4" false}
                 "henrik" {"movie1" true "movie2" true "movie3" false "movie4" false}
                 "joppe" {"movie1" true "movie2" true "movie3" false "movie4" true}}
    :movies {"movie1" {:id "movie1"
                      :status :loading
                      :title "Vinland Saga"
                      :synopsis "Synopsis"
                      :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}
             "movie2" {:id "movie2"
                      :status :loading
                      :title "Fate/Zero"
                      :synopsis "Synopsis"
                      :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}
             "movie3" {:id "movie3"
                      :status :loading
                      :title "One Piece"
                      :synopsis "Synopsis"
                      :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}
             "movie4" {:id "movie4"
                      :status :loading
                      :title "Naruto Shippuden"
                      :synopsis "Synopsis"
                      :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}}
    :vote-prompts ["movie1" "movie2"]
    :search-results []}))

(defn set-search-results [results]
  (swap! state
         (fn [s]
           (let [new-state (assoc s :search-results results)]
                (spec/assert ::app-state new-state)
                new-state)))
  nil)

(spec/def ::app-state (spec/keys :req-un [::users ::user-votes ::movies ::search-results]))

(spec/def ::users (spec/coll-of ::user))
(spec/def ::user (spec/keys :req-un [:user/id :user/name]))
(spec/def :user/id string?)
(spec/def :user/name string?)

(spec/def ::user-votes (spec/map-of :user/id ::user-vote))
(spec/def ::user-vote (spec/map-of :movie/id boolean?))

(spec/def ::movies (spec/map-of :movie/id ::movie))
(spec/def ::movie (spec/keys :req-un [:movie/id :movie/status :movie/title :movie/synopsis :movie/image-url]))
(spec/def :movie/id string?)
(spec/def :movie/status #{:loading :loaded})
(spec/def :movie/title string?)
(spec/def :movie/synopsis string?)
(spec/def :movie/image-url string?)

(spec/def ::search-results (spec/coll-of :search/result))
(spec/def :search/result (spec/keys :req-un [:search/value :search/label]))
(spec/def :search/value string?)
(spec/def :search/label string?)
