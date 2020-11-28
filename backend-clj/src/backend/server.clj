(ns backend.server
  (:require [org.httpkit.server :as http-kit]
            [taoensso.sente :as sente]
            [ring.middleware.session]
            [ring.middleware.params]
            [ring.middleware.keyword-params]
            [ring.middleware.anti-forgery]
            [ring.middleware.cors]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST]]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

;; (defroutes app-routes
;;   (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
;;   (POST "/chsk" req (ring-ajax-post req)))

(defrecord Websocket [ring-ajax-post ring-ajax-get-or-ws-handshake ch-chsk chsk-send! connected-uids]
  component/Lifecycle

  (start [this]
    (println "Starting websocket")
    (let [{:keys [ch-recv send-fn connected-uids
                  ajax-post-fn ajax-get-or-ws-handshake-fn]}
          (sente/make-channel-socket!
           (get-sch-adapter)
           {:csrf-token-fn nil          ; TODO don't do this in prod!
            :packer :edn})]
      (-> this
          (assoc :ring-ajax-post ajax-post-fn)
          (assoc :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
          (assoc :ch-chsk ch-recv)
          (assoc :chsk-send! send-fn)
          (assoc :connected-uids connected-uids))))

  (stop [this]
    (println "Stopping websocket")
    ;; TODO actually disconnect?
    (-> this
        (assoc :ring-ajax-post nil)
        (assoc :ring-ajax-get-or-ws-handshake nil)
        (assoc :ch-chsk nil)
        (assoc :chsk-send! nil)
        (assoc :connected-uids nil))))

(defrecord Server [port websocket stop-fn]
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
    (println "Stopping database")
    (stop-fn)
    (assoc this :stop-fn nil)))

(defn create-websocket []
  (map->Websocket {}))

(defn create-server [port]
  (map->Server {:port port}))
