(ns app.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [chan <! go-loop]]
            [app.api :as api]
            [app.state :as state]
            [app.system :as system]
            [app.utils :as utils]))

(defonce request-chan (chan))

(defn set-search-results [results]
  (state/swap-state!
   (fn [s]
     (update-in s [:movies] merge (into {} (map (fn [x] [(:id x) x]) results)))
     (assoc s :search-results (map (fn [x] {:value (:id x) :label (:title x)}) results))
     )))

(defn handle-search-request [{:keys [query]}]
  (if (<= (count query) 2)
    (do
     (println "Clearing search results")
     (set-search-results []))
    (go
      (println "Searching for" query)
      (let [result (<! (api/search query))]
        (set-search-results result)))))

(defn make-search-component []
  (system/make-component
   :name "search-component"
   :input-ch (utils/debounce request-chan 500)
   :handler handle-search-request))
