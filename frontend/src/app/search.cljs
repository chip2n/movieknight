(ns app.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan <! go-loop]]
            [app.api :as api]
            [app.state :as state]))

(defonce request-chan (chan))

(defn handle-search-request [request]
  (println "Searching for" (:query request))
  (go
    (let [result (<! (api/search (:query request)))]
      (state/set-search-results result))))

(defonce search-handler
  (go-loop []
    (handle-search-request (<! request-chan))
    (recur)))
