(ns app.vote
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.string :as gstring]
            [app.utils :as utils]
            [app.search :as search]))

;;;; Event handlers

(rf/reg-event-db
 :vote
 [(utils/validate-db)]
 (fn [db [_ {:keys [id answer]}]]
   (-> db
       (update :vote-prompts #(into [] (remove #{id} %)))
       (assoc-in [:user-votes "andreas" id] answer))))

(rf/reg-event-fx
 :suggest-movie
 [(utils/validate-db)]
 (fn [{:keys [db]} [_ id]]
   {:db (-> db
            (update :suggested-movies #(conj % id))
            (assoc-in [:user-votes "andreas" id] true))
    :dispatch [:set-search-results []]}))

;;;; Subscriptions

(rf/reg-sub
 :users
 (fn [db v] (:users db)))

(rf/reg-sub
 :user-votes
 (fn [db v] (:user-votes db)))

(rf/reg-sub
 :vote-prompts
 (fn [db v]
   (let [user-id (get-in db [:session :user-id])
         voted-movies (keys (get-in db [:user-votes user-id]))
         movies (:movies db)
         unvoted-movies (clojure.set/difference (into #{} (keys movies))
                                                (into #{} voted-movies))]
     (map (partial get movies) unvoted-movies))))

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

(rf/reg-sub
 :movie-suggestions
 (fn [db v]
   (let [prompts (:suggested-movies db)
         movies (:movies db)]
     (->> prompts
          (map #(get movies %))
          (filter (comp not nil?))
          (into [])))))

;;;; UI

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
