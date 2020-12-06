(ns app.search.events
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :as rf]
            [app.utils :as utils]
            [app.api :as api]
            [cljs.core.async :as async :refer [<!]]))

(rf/reg-event-fx
 :search
 (fn [_ [_ query]]
   {:search query}))

(rf/reg-event-db
 :set-search-results
 (fn [db [_ results]]
   (-> db
       (update-in [:movies] merge (into {} (map (fn [x] [(:movie/id x) x]) results)))
       (assoc :search-results (map (fn [x] {:id (:movie/id x) :label (:movie/title x)}) results)))))

;; TODO debounce
(rf/reg-fx
 :search
 (fn [query]
   (if (<= (count query) 2)
     (do
       (println "Clearing search results")
       (rf/dispatch [:set-search-results []]))
     (go
       (println "Searching for" query)
       (let [result (<! (api/search query))]
         (rf/dispatch [:set-search-results result]))))))
