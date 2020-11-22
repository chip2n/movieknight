(ns app.vote.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.string :as gstring]
            [app.search.views :as search]))

(defn vote [answer]
  [:td.vote-box
   {:class (case answer
             true :answer-yes
             false :answer-no
             :answer-none)}])

(defn vote-list []
  [:div#vote-list
   [search/search-bar]
   [:table
    {:style {:border-spacing 16}}
    (let [data @(rf/subscribe [:vote-list])]
      [:tbody
       [:tr
        [:td]
        (for [user (:users data)]
          ^{:key (:id user)}
          [:td {:style {:text-align :center}}
           (as-> (:name user) n
             (clojure.string/split n " ")
             (map first n)
             (clojure.string/join n))])]

       (for [movie (:movies data)]
         ^{:key (first movie)}
         [:tr
          [:td
           {:style {:width 200}}
           (first movie)]
          (for [[i user-vote] (map vector (range (- (count movie) 1)) (drop 1 movie))]
              ^{:key (str (first movie) "-" i)}
            [vote user-vote])])])]])

(defn movie-watch-question [{:keys [id title synopsis rating image-url]}]
  (let [width 300]
    [:div {:style {:width width}}
     [:div.movie-question-caption "Do you want to watch"]
     [:div.movie-question-header title]
     [:img.movie-question-img {:src image-url :width width :height 400}]

     [:p [:span.movie-question-header "Rating: "] [:span (gstring/format "%.1f" rating)]]
     [:p (str (subs synopsis 0 120) "…")]
   
     [:div {:style {:display :flex
                    :justify-content :space-between}}
      [:button.movie-question-btn
       {:class [:answer-no]
        :on-click #(rf/dispatch [:vote {:id id :answer false}])}
       [:i.fa.fa-times]]
      [:div]
      [:button.movie-question-btn
       {:class [:answer-yes]
        :on-click #(rf/dispatch [:vote {:id id :answer true}])}
       [:i.fa.fa-check]]]]))

(defn movie-vote-box []
  (let [movies @(rf/subscribe [:vote-prompts])]
    (when-not (empty? movies)
      [movie-watch-question (first movies)])))
