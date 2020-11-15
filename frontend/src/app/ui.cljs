(ns app.ui
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [app.api :as api]
            [app.search :as search]
            [goog.string :as gstring]
            ["@material-ui/lab/Autocomplete" :default Autocomplete]
            ["@material-ui/core/TextField" :default TextField]
            [cljs-http.client :as http]
            [cljs.core.async :as async]))

(rf/reg-sub
 :vote-prompts
 (fn [db v]
   (let [user-id (get-in db [:session :user-id])
         voted-movies (keys (get-in db [:user-votes user-id]))
         movies (:movies db)
         unvoted-movies (clojure.set/difference (into #{} (keys movies))
                                                (into #{} voted-movies))]
     (map (partial get movies) unvoted-movies))))

(rf/reg-sub
 :movie-suggestions
 (fn [db v]
   (let [prompts (:suggested-movies db)
         movies (:movies db)]
     (->> prompts
          (map #(get movies %))
          (filter (comp not nil?))
          (into [])))))

(rf/reg-sub
 :search-results
 (fn [db v] (:search-results db)))

(rf/reg-sub
 :users
 (fn [db v] (:users db)))

(rf/reg-sub
 :user-votes
 (fn [db v] (:user-votes db)))

(defn movie-watch-question [{:keys [id title synopsis rating image-url]}]
  (let [width 300]
    [:div {:style {:width width}}
     [:p "Do you want to watch"]
     [:p title]
     [:img {:src image-url
            :width width
            :height 400
            :style {:object-fit :cover}}]

     [:p (gstring/format "Rating: %.1f" rating)]
     [:p (str (subs synopsis 0 120) "…")]
   
     [:div {:style {:display :flex
                    :justify-content :space-between}}
      [:button {:on-click #(rf/dispatch [:vote {:id id :answer false}])}
       "No"]
      [:div]
      [:button {:on-click #(rf/dispatch [:vote {:id id :answer true}])}
       "Ye"]]]))

(defn movie-vote-box []
  (let [movies @(rf/subscribe [:vote-prompts])]
    (when-not (empty? movies)
      [movie-watch-question (first movies)])))

(defn search-bar []
  (let [search-results @(rf/subscribe [:search-results])]
    [:> Autocomplete {:free-solo true
                      :get-option-label (fn [x] (get (js->clj x) "label"))
                      :render-input (fn [^js params]
                                      (set! (.-variant params) "outlined")
                                      (set! (.-size params) "small")
                                      (set! (.-label params) "Search")
                                      (r/create-element TextField params))
                      :on-change (fn [_ value]
                                   (as-> value v
                                     (js->clj v)
                                     (get v "value")
                                     (rf/dispatch [:suggest-movie v])))
                      :on-input-change (fn [_ value reason]
                                         (when (= reason "input")
                                           (rf/dispatch [:search value])))
                      :options search-results}]))

(defn vote [answer]
  [:td
   {:style {:height 32
            :width 32
            :background-color
            (case answer
              true "#00ff00"
              false "#ff0000"
              "#00000020")}}])

(defn vote-list []
  [:div {:style {:display :flex :flex-direction :column}}
   [search-bar]
   [:table
    {:style {:border-spacing 16}}
    (let [user-votes @(rf/subscribe [:user-votes])
          users @(rf/subscribe [:users])
          movies @(rf/subscribe [:movie-suggestions])]
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

       (for [movie movies]
         ^{:key (:id movie)}
         [:tr
          [:td
           {:style {:width 200}}
           (:title movie)]
          (for [user users]
            (let [user-vote (get (get user-votes (:id user)) (:id movie))]
              ^{:key (str (:id user) "-" (:id movie))}
              [vote user-vote]))])])]])

(defn root-component []
  [:div {:style {:display :flex}}
   [vote-list]
   [movie-vote-box]])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))
