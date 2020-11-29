(ns app.vote.events
  (:require [re-frame.core :as rf]
            [app.utils :as utils]))

(rf/reg-event-db
 :vote
 [(utils/validate-db)]
 (fn [db [_ {:keys [id answer]}]]
   (-> db
       (update :vote-prompts #(into [] (remove #{id} %)))
       (assoc-in [:user-votes "andreas" id] answer))))

(rf/reg-event-fx
 :suggest-movie
 [(utils/validate-db)]
 (fn [{:keys [db]} [_ id]]
   {:db (-> db
            (update :suggested-movies #(conj % id))
            (assoc-in [:user-votes "andreas" id] true))
    :dispatch [:set-search-results []]}))
