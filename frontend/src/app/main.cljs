(ns app.main
  (:require [app.state :as state]
            [app.api :as api]
            [app.ui :as ui]
            [app.search :as search]
            [cljs.core.async :as async :refer [pipeline]]))

(defonce search-pipeline
  (pipeline 1 search/request-chan (filter (comp #(= :search %) :type)) ui/event-chan))

(defn main! []
  (println "[main]: reloaded")
  (ui/render-app))

(defn ^:dev/after-load reload! []
  (ui/render-app))
