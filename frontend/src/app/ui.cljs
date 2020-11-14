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
            [cljs.core.async :as async]))

(defonce event-chan (async/chan))

(defonce movie-vote-cursor
  (let [src (fn ([k]
                 (let [prompts (get-in @state/state k)
                       movies (:movies @state/state)]
                   (->> prompts
                        (map #(get movies %))
                        (filter (comp not nil?)))))
              ([k v] (swap! state/state assoc-in k v)))]
    (r/cursor src [:vote-prompts])))

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
      [:button {:on-click (fn [] (println "hello"))}
       "Ye"]]]))

(defn movie-vote-box []
  (let [movies @movie-vote-cursor]
    (when-not (empty? movies)
      [movie-watch-question (first movies)])))

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
                       :on-input-change (fn [ev _ reason]
                                          (when (= reason "input")
                                            (->> ev
                                                 .-target
                                                 .-value
                                                 (assoc {:type :search} :query)
                                                 (async/put! event-chan))))
                       :options search-results}])
   [:table
    {:style {:border-spacing 16}}
    (let [user-votes (:user-votes @state/state)
          users (:users @state/state)
          movies (:movies @state/state)]
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

       (for [[_ movie] movies]
         ^{:key (:id movie)}
         [:tr
          [:td
           {:style {:width 200}}
           (:title movie)]
          (for [user users]
            (let [user-vote (get (get user-votes (:id user)) (:id movie))]
              ^{:key (str (:id user) "-" (:id movie))}
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
   [movie-vote-box]])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))
