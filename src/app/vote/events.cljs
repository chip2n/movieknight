(ns app.vote.events
  (:require [re-frame.core :as rf]
            [app.utils :as utils]))

(rf/reg-event-db
 :vote
 [(utils/validate-db)]
 (fn [db [_ {:keys [id answer]}]]
   (let [current-user (get-in db [:session :user-id])]
     (-> db
         (assoc-in [:user-votes current-user id] answer)))))

(rf/reg-event-fx
 :suggest-movie
 [(utils/validate-db)]
 (fn [{:keys [db]} [_ id]]
   (let [current-user (get-in db [:session :user-id])]
     {:db (-> db
              (update :suggested-movies #(conj % id))
              (assoc-in [:user-votes current-user id] true))
      :dispatch [:set-search-results []]})))
