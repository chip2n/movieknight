(ns backend.server
  (:require [org.httpkit.server :as http-kit]
            [ring.middleware.session]
            [ring.middleware.params]
            [ring.middleware.keyword-params]
            [ring.middleware.anti-forgery]
            [ring.middleware.cors]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST]]))

(defrecord Server [port websocket database stop-fn]
  component/Lifecycle

  ;; TODO use legacy-return-value? false
  (start [this]
    (println "Starting server")
    (let [app-routes (routes (GET "/chsk" req ((:ring-ajax-get-or-ws-handshake websocket) req))
                             (POST "/chsk" req ((:ring-ajax-post websocket) req)))
          ring-handler (-> app-routes
                           ring.middleware.keyword-params/wrap-keyword-params
                           ring.middleware.params/wrap-params
                           ;; ring.middleware.anti-forgery/wrap-anti-forgery
                           ;; (ring.middleware.cors/wrap-cors
                           ;;  :access-control-allow-origin [#".*"]
                           ;;  :access-control-allow-methods [:get :post])
                           ring.middleware.session/wrap-session)]
      (let [[port stop-fn]
            (let [stop-fn (http-kit/run-server ring-handler {:port port})]
              [(:local-port (meta stop-fn)) (fn [] (stop-fn :timeout 100))])

            uri (format "http://localhost:%s/" port)]
        (println "Server running at %s" uri)
        (assoc this :stop-fn stop-fn))))

  (stop [this]
    (println "Stopping server")
    (stop-fn)
    (assoc this :stop-fn nil)))

(defn create [port]
  (map->Server {:port port}))
