(ns backend.system
  (:require [com.stuartsierra.component :as component]
            [backend.db :as db]
            [backend.server :as server]
            [backend.socket :as socket]))

(defn create []
  (component/system-map
   :websocket (socket/create)
   :database (db/create "movieknight")
   :server (component/using
            (server/create 8020)
            [:websocket :database])))
