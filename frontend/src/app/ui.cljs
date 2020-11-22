(ns app.ui
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [app.api :as api]
            [app.search :as search]
            [app.vote.views :as vote]
            [cljs-http.client :as http]
            [cljs.core.async :as async]))

(defn root-component []
  [:div {:style {:display :flex}}
   [vote/vote-list]
   [vote/movie-vote-box]])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))
