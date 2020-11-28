(ns backend.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [backend.server :as server]))

(defn root-system []
  (component/system-map
   :websocket (server/create-websocket)
   :server (component/using
            (server/create-server 8020)
            [:websocket])))

(defn -main [& args]
  (component/start (root-system)))

(comment
  (def system (root-system))
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop))
