(ns backend.core
  (:require [com.stuartsierra.component :as component]
            [backend.server :as server]
            [backend.socket :as socket]))

(defn root-system []
  (component/system-map
   :websocket (socket/create)
   :server (component/using
            (server/create 8020)
            [:websocket])))

(defn -main [& args]
  (component/start (root-system)))

(comment
  (def system (root-system))
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop))
