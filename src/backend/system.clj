(ns backend.system
  (:require [com.stuartsierra.component :as component]
            [backend.db :as db]
            [backend.server :as server]
            [backend.socket :as socket]))

(defn create
  ([] (create {}))
  ([{:keys [dbname port] :or {dbname "movieknight" port 8020}}]
   (component/system-map
    :websocket (socket/create)
    :database (db/create dbname)
    :server (component/using
             (server/create port)
             [:websocket :database]))))
