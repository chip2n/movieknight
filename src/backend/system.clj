(ns backend.system
  (:require [com.stuartsierra.component :as component]
            [backend.db :as db]
            [backend.server :as server]
            [backend.socket :as socket]))

(defn create
  ([] (create {}))
  ([{:keys [dbname port] :or {dbname "movieknight" port 8020}}]
   (component/system-map
    :database (db/create dbname)
    :websocket (component/using (socket/create) [:database])
    :server (component/using (server/create port) [:websocket :database]))))
