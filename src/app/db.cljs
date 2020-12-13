(ns app.db
  (:require [clojure.spec.alpha :as spec]
            [core.user :as user]
            [core.movie :as movie]
            [re-frame.core :as rf]))

(def initial-db
  {:session {:user-id 1}
   :backend-conn nil
   :users [#:user{:id 1 :name "Andreas Arvidsson"}
           #:user{:id 2 :name "Henning Phan"}
           #:user{:id 3 :name "Sebastian Lagerman"}
           #:user{:id 4 :name "Henrik Nyström"}
           #:user{:id 5 :name "Joppe Widstam"}]
   :user-votes {1 {1 true 3 true}
                2 {1 true 2 false 3 false}
                3 {1 true 2 true 3 true 4 false}
                4 {1 true 2 true 3 false 4 false}
                5 {1 true 2 true 3 false 4 true}}
   :suggested-movies [1 2 3 4]
   :movies {1 #:movie{:id 1
                      :title "Vinland Saga"
                      :synopsis "Synopsis"
                      :rating 7.3
                      :image-url "https://cdn.myanimelist.net/images/anime/1500/103005l.webp"}
            2 #:movie{:id 2
                      :title "Fate/Zero"
                      :synopsis "Synopsis"
                      :rating 7.5
                      :image-url "https://cdn.myanimelist.net/images/anime/2/73249l.webp"}
            3 #:movie{:id 3
                      :title "One Piece"
                      :synopsis "Synopsis"
                      :rating 7.5
                      :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}
            4 #:movie{:id 4
                      :title "Naruto Shippuden"
                      :synopsis "Synopsis"
                      :rating 7.5
                      :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}}
   :search-results []})

(spec/def ::app-state (spec/keys :req-un [::session
                                          ::users
                                          ::user-votes
                                          ::movies
                                          ::suggested-movies
                                          ::search-results]))

(spec/def ::session (spec/keys :req-un [:session/user-id]))
(spec/def :session/user-id :user/id)

(spec/def ::users (spec/coll-of :user/user))

(spec/def ::user-votes (spec/map-of :user/id ::user-vote))
(spec/def ::user-vote (spec/map-of :movie/id boolean?))

(spec/def ::suggested-movies (spec/coll-of :movie/id))

(spec/def ::movies (spec/map-of :movie/id :movie/movie))

(spec/def ::search-results (spec/coll-of :search/result))
(spec/def :search/result (spec/keys :req-un [:movie/id :search/label]))
(spec/def :search/label string?)
