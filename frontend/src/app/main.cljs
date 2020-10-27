(ns app.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn do-request []
  (go (let [response (<! (http/get "http://localhost:8000"
                                   {:with-credentials? false
                                    :accept "application/json"}))]
        (prn (:body response)))))

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

(defn root-component []
  [movie-watch-question
   {:title "Shigatsu wa Kimi no Uso"
    :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))

(defn main! []
  (println "[main]: reloaded")
  (render-app))

(defn ^:dev/after-load reload! []
  (render-app))
