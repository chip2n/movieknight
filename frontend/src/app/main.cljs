(ns app.main
  (:require [app.db :as db]
            [app.api :as api]
            [app.search :as search]
            [app.ui :as ui]
            [app.utils :as utils]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [pipeline]]
            [clojure.spec.alpha :as spec]))

(spec/check-asserts true)

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

(defn main! []
  (println "[main]: reloaded")
  (ui/render-app))

(defn init! []
  (rf/dispatch-sync [:init-db])
  (ui/render-app))

(defn ^:dev/after-load start []
  (main!))
