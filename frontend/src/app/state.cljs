(ns app.state
  (:require [reagent.core :as r]))

(defonce state
  (r/atom
   {:users [{:id "andreas" :name "Andreas Arvidsson"}
            {:id "henning" :name "Henning Phan"}
            {:id "sebastian" :name "Sebastian Lagerman"}
            {:id "henrik" :name "Henrik Nyström"}
            {:id "joppe" :name "Joppe Widstam"}]
    :user-votes {"andreas" {"vote1" true "vote2" true "vote3" true "vote4" true}
                 "henning" {"vote1" true "vote2" false "vote3" true "vote4" false}
                 "sebastian" {"vote1" true "vote2" true "vote3" true "vote4" false}
                 "henrik" {"vote1" true "vote2" true "vote3" false "vote4" false}
                 "joppe" {"vote1" true "vote2" true "vote3" false "vote4" true}}
    :votes [{:id "vote1" :status :loading :label "Vinland Saga"}
            {:id "vote2" :status :loading :label "Fate/Zero"}
            {:id "vote3" :status :loading :label "One Piece"}
            {:id "vote4" :status :loading :label "Naruto Shippuden"}]
    :search-results []}))

(defn set-search-results [results]
  (swap! state assoc :search-results results))
