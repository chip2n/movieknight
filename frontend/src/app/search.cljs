(ns app.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan <! go-loop]]
            [app.api :as api]
            [app.state :as state]))

(defonce request-chan (chan))

(defn handle-search-request [{:keys [query]}]
  (if (<= (count query) 2)
    (do
     (println "Clearing search results")
     (state/set-search-results []))
    (go
      (println "Searching for" query)
      (let [result (<! (api/search query))]
        (state/set-search-results result)))))

(defonce search-handler
  (go-loop []
    (handle-search-request (<! request-chan))
    (recur)))
