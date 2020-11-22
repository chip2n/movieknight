(ns app.ui
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [app.api :as api]
            [app.search :as search]
            [goog.string :as gstring]
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

(defn sort-vote-list [rows]
  "Sort the vote list first by # positive answers, then by # unanswered."
  (sort-by
       (juxt
        (comp count (partial filter true?))
        (comp count (partial filter nil?)))
       #(compare %2 %1)
       rows))

(rf/reg-sub
 :vote-list
 (fn [db v]
   (let [votes (:user-votes db)
         movies (:movies db)
         suggested-movies (:suggested-movies db)
         users (:users db)]
     {:users users
      :movies
      (->> movies
           (filter (fn [[id m]] (some (partial = id) suggested-movies)))
           (map (fn [[id m]] (concat [(:title m)]
                                     (map #(get-in votes [(:id %) id]) users))))
           (sort-vote-list))})))

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
  (let [state (r/atom {:input "" :expanded false})
        ref (clojure.core/atom nil)
        click-listener (fn [ev] (swap! state assoc :expanded (.contains @ref (.-target ev))))]
    (r/create-class
     {:display-name "search-bar"

      :component-did-mount
      (fn [this]
        (.addEventListener js/document "click" click-listener))

      :component-will-unmount
      (fn [this]
        (.removeEventListener js/document "click" click-listener))

      :reagent-render
      (fn []
        (let [search-results @(rf/subscribe [:search-results])
              {:keys [input expanded]} @state]
          [:div {:style {:position :relative
                         :display :flex
                         :flex-direction :column}
                 :ref (fn [el] (reset! ref el))}
           [:input {:style {:height 32}
                    :value input
                    :on-key-down (fn [ev] (println "key down"))
                    :on-change (fn [ev]
                                 (let [value (-> ev .-target .-value)]
                                   (swap! state assoc :input value)
                                   (rf/dispatch [:search value])))}]
           (when (and expanded search-results)
             [:ul {:style {:position :absolute
                           :width "100%"
                           :margin 0
                           :top 32
                           :padding 0
                           :list-style :none
                           :box-shadow "0px 8px 8px 0px rgba(0,0,0,0.1)"
                           :z-index 1
                           :background-color "#ffffff"}}
              (for [result search-results]
                ^{:key (str "search-result-" (:value result))}
                [:li.dropdown-content
                 {:on-click (fn [] (rf/dispatch [:suggest-movie (:id result)]))}
                 (:label result)])])]))})))

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
  [:div {:style {:display :flex
                 :flex-direction :column}}
   [search-bar]
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

(defn root-component []
  [:div {:style {:display :flex}}
   [vote-list]
   [movie-vote-box]])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))
