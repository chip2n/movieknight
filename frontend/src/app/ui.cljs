(ns app.ui
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [app.api :as api]
            [app.vote.views :as vote]
            [cljs-http.client :as http]
            [cljs.core.async :as async]))

(defn vertical-line []
  [:div {:style {:border-left "2px solid #4B4E54"
                 :height "100%"
                 :margin "0px 40px 0px 24px"}}])

(defn root-component []
  [:div#root
   [vote/vote-list]
   [vertical-line]
   [vote/movie-vote-box]])

(defn render-app []
  (dom/render
   [root-component]
   (.getElementById js/document "root")))
