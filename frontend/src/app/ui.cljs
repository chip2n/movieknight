(ns app.ui
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [app.state :as state]
            [app.api :as api]
            [app.search :as search]
            ["@material-ui/lab/Autocomplete" :default Autocomplete]
            ["@material-ui/core/TextField" :default TextField]
            [cljs-http.client :as http]
            [cljs.core.async :refer [chan pipeline]]))

(defonce event-chan (chan))

(defn movie-watch-question [{:keys [title image-url]}]
  (let [width 300]
    [:div {:style {:width width}}
     [:p "Do you want to watch"]
     [:p title]
     [:img {:src image-url
            :width width}]
   
     [:div {:style {:display :flex
                    :justify-content :space-between}}
      [:button "No"]
      [:div]
      [:button "Ye"]]]))

(defn vote-list []
  [:div {:style {:display :flex :flex-direction :column}}
   (let [search-results (:search-results @state/state)]
     [:> Autocomplete {:free-solo true
                       :get-option-label (fn [x] (get (js->clj x) "label"))
                       :render-input (fn [^js params]
                                       (set! (.-variant params) "outlined")
                                       (set! (.-size params) "small")
                                       (set! (.-label params) "Search")
                                       (r/create-element TextField params))
                       :options search-results}])
   [:table
    {:style {:border-spacing 16}}
    (let [user-votes (:user-votes @state/state)
          users (:users @state/state)
          votes (:votes @state/state)]
      [:tbody
       [:tr
        [:td]
        (for [user users]
          ^{:key (:id user)}
          [:td {:style {:text-align :center}}
           (as-> (:name user) n
             (clojure.string/split n " ")
             (map first n)
             (clojure.string/join n))])]

       (for [vote votes]
         ^{:key (:id vote)}
         [:tr
          [:td
           {:style {:width 200}}
           (:label vote)]
          (for [user users]
            (let [user-vote (get (get user-votes (:id user)) (:id vote))]
              ^{:key (str (:id user) "-" (:id vote))}
              [:td
               {:style {:height 32
                        :width 32
                        :background-color
                        (if user-vote
                          "#00ff00"
                          "#ff0000")}}]))])])]])

(defn root-component []
  [:div {:style {:display :flex}}
   [vote-list]
   [movie-watch-question
    {:title "Shigatsu wa Kimi no Uso"
     :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}]])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))
