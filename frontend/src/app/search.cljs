(ns app.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [chan <! go-loop]]
            [re-frame.core :as rf]
            [app.api :as api]
            [app.utils :as utils]))

(rf/reg-event-fx
 :search
 (fn [_ [_ query]]
   {:search query}))

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

(rf/reg-event-db
 :set-search-results
 (fn [db [_ results]]
   (-> db
       (update-in [:movies] merge (into {} (map (fn [x] [(:id x) x]) results)))
       (assoc :search-results (map (fn [x] {:value (:id x) :label (:title x)}) results)))))
