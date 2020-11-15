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
 (fn [db [_ {:keys [id answer]}]]
   (update db :vote-prompts #(into [] (remove #{id} %)))))

(rf/reg-event-db
 :suggest-movie
 (fn [db [_ id]]
   (update db :suggested-movies #(conj % id))))

(defn main! []
  (println "[main]: reloaded")
  (rf/dispatch-sync [:init-db])
  (ui/render-app))

(defn ^:dev/after-load start []
  (main!))
