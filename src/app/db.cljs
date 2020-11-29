(ns app.db
  (:require [clojure.spec.alpha :as spec]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :init-db
 (fn [_ _]
   {:session {:user-id "andreas"}
    :users [{:id "andreas" :name "Andreas Arvidsson"}
            {:id "henning" :name "Henning Phan"}
            {:id "sebastian" :name "Sebastian Lagerman"}
            {:id "henrik" :name "Henrik Nyström"}
            {:id "joppe" :name "Joppe Widstam"}]
    :user-votes {"andreas" {"movie1" true "movie3" true}
                 "henning" {"movie1" true "movie2" false "movie3" false }
                 "sebastian" {"movie1" true "movie2" true "movie3" true "movie4" false}
                 "henrik" {"movie1" true "movie2" true "movie3" false "movie4" false}
                 "joppe" {"movie1" true "movie2" true "movie3" false "movie4" true}}
    :suggested-movies ["movie1" "movie2" "movie3" "movie4"]
    :movies {"movie1" {:id "movie1"
                       :title "Vinland Saga"
                       :synopsis "Young Thorfinn grew up listening to the stories of old sailors that had traveled the ocean and reached the place of legend, Vinland. It's said to be warm and fertile, a place where there would be no need for fighting—not at all like the frozen village in Iceland where he was born, and certainly not like his current life as a mercenary. War is his home now. Though his father once told him, \"You have no enemies, nobody does. There is nobody who it's okay to hurt,\" as he grew, Thorfinn knew that nothing was further from the truth.

The war between England and the Danes grows worse with each passing year. Death has become commonplace, and the viking mercenaries are loving every moment of it. Allying with either side will cause a massive swing in the balance of power, and the vikings are happy to make names for themselves and take any spoils they earn along the way. Among the chaos, Thorfinn must take his revenge and kill Askeladd, the man who murdered his father. The only paradise for the vikings, it seems, is the era of war and death that rages on."
                       :rating 7.3
                       :image-url "https://cdn.myanimelist.net/images/anime/1500/103005l.webp"}
             "movie2" {:id "movie2"
                       :title "Fate/Zero"
                       :synopsis "With the promise of granting any wish, the omnipotent Holy Grail triggered three wars in the past, each too cruel and fierce to leave a victor. In spite of that, the wealthy Einzbern family is confident that the Fourth Holy Grail War will be different; namely, with a vessel of the Holy Grail now in their grasp. Solely for this reason, the much hated \"Magus Killer\" Kiritsugu Emiya is hired by the Einzberns, with marriage to their only daughter Irisviel as binding contract.

Kiritsugu now stands at the center of a cutthroat game of survival, facing off against six other participants, each armed with an ancient familiar, and fueled by unique desires and ideals. Accompanied by his own familiar, Saber, the notorious mercenary soon finds his greatest opponent in Kirei Kotomine, a priest who seeks salvation from the emptiness within himself in pursuit of Kiritsugu.

Based on the light novel written by Gen Urobuchi, Fate/Zero depicts the events of the Fourth Holy Grail War—10 years prior to Fate/stay night. Witness a battle royale in which no one is guaranteed to survive."
                       :rating 7.5
                       :image-url "https://cdn.myanimelist.net/images/anime/2/73249l.webp"}
             "movie3" {:id "movie3"
                       :title "One Piece"
                       :synopsis "Synopsis"
                       :rating 7.5
                       :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}
             "movie4" {:id "movie4"
                       :title "Naruto Shippuden"
                       :synopsis "Synopsis"
                       :rating 7.5
                       :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}}
    :search-results []}))

(spec/def ::app-state (spec/keys :req-un [::session
                                          ::users
                                          ::user-votes
                                          ::movies
                                          ::suggested-movies
                                          ::search-results]))

(spec/def ::session (spec/keys :req-un [:session/user-id]))
(spec/def :session/user-id string?)

(spec/def ::users (spec/coll-of ::user))
(spec/def ::user (spec/keys :req-un [:user/id :user/name]))
(spec/def :user/id string?)
(spec/def :user/name string?)

(spec/def ::user-votes (spec/map-of :user/id ::user-vote))
(spec/def ::user-vote (spec/map-of :movie/id boolean?))

(spec/def ::suggested-movies (spec/coll-of :movie/id))

(spec/def ::movies (spec/map-of :movie/id ::movie))
(spec/def ::movie (spec/keys :req-un [:movie/id
                                      :movie/title
                                      :movie/synopsis
                                      :movie/rating
                                      :movie/image-url]))
(spec/def :movie/id string?)
(spec/def :movie/title string?)
(spec/def :movie/synopsis string?)
(spec/def :movie/rating float?)
(spec/def :movie/image-url string?)

(spec/def ::search-results (spec/coll-of :search/result))
(spec/def :search/result (spec/keys :req-un [:movie/id :search/label]))
(spec/def :search/label string?)
