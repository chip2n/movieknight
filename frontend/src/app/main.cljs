(ns app.main
  (:require [app.state :as state]
            [app.api :as api]
            [app.search :as search]
            [app.system :as system]
            [app.ui :as ui]
            [app.utils :as utils]
            [cljs.core.async :as async :refer [pipeline]]))

(defonce search-pipeline
  (pipeline 1 search/request-chan (filter (comp #(= :search %) :type)) ui/event-chan))

(defn main! []
  (println "[main]: reloaded")
  (system/start! [(search/make-search-component)])
  (ui/render-app))

(defn ^:dev/before-load stop []
  (system/stop!))

(defn ^:dev/after-load start []
  (main!))
