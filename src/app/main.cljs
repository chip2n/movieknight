(ns app.main
  (:require-macros [cljs.core.async :refer [go]])
  (:require [app.db :as db]
            [app.api :as api]
            [app.search.events]
            [app.search.subs]
            [app.vote.events]
            [app.vote.subs]
            [app.ui :as ui]
            [app.utils :as utils]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [<!]]
            [clojure.spec.alpha :as spec]
            [core.client :as client]))

(spec/check-asserts true)

(defn main! []
  (println "[main]: reloaded")
  (ui/render-app))

(defn init! []
  (rf/dispatch-sync [:init-app])
  (ui/render-app))

(defn ^:dev/after-load start []
  (main!))

(rf/reg-event-fx
 :init-app
 (fn [_ _]
   {:connect-backend {}
    :db db/initial-db}))


(rf/reg-fx
 :connect-backend
 (let [client (client/create "localhost" 8020)]
   (fn [{:keys [host port]}]
     (go
       (rf/dispatch [:register-client client])
       (let [client (<! (client/connect client))]
         (rf/dispatch [:register-client client])
         (client/send-msg client [:app/get-initial-state] 5000
                          (fn [reply]
                            (rf/dispatch [:merge-initial-state reply]))))))))

(rf/reg-event-db
 :register-client
 (fn [db client]
   (assoc db :backend-conn client)))

(rf/reg-event-db
 :merge-initial-state
 (fn [db [_ state]]
   (merge db
          {:users (:users state)
           :votes (:votes state)
           :movies (into {} (map (fn [m] [(:movie/id m) m])) (:movies state))})))
