(ns backend.core
  (:gen-class)
  (:require [org.httpkit.server :as http-kit]
            [taoensso.sente :as sente]
            [ring.middleware.session]
            [ring.middleware.params]
            [ring.middleware.keyword-params]
            [ring.middleware.anti-forgery]
            [ring.middleware.cors]
            [compojure.core :refer [defroutes GET POST]]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket!
       (get-sch-adapter)
       {:csrf-token-fn nil               ; TODO don't do this in prod!
        :packer :edn})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))

(defroutes app-routes
  (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req)))

(def ring-handler
  (-> app-routes
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      ;; ring.middleware.anti-forgery/wrap-anti-forgery
      ;; (ring.middleware.cors/wrap-cors
      ;;  :access-control-allow-origin [#".*"]
      ;;  :access-control-allow-methods [:get :post])
      ring.middleware.session/wrap-session))

(defonce server (atom nil))



;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]

    ;; TODO use timbre
    ;; (debugf "Unhandled event: %s" event)
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))

;; (defmethod -event-msg-handler :chsk/state
;;   [{:as ev-msg :keys [?data]}]
;;   (let [[old-state-map new-state-map] (have vector? ?data)]
;;     (if (:first-open? new-state-map)
;;       (->output! "Channel socket successfully established!: %s" new-state-map)
;;       (->output! "Channel socket state change: %s"              new-state-map))))

;; (defmethod -event-msg-handler :chsk/recv
;;   [{:as ev-msg :keys [?data]}]
;;   (->output! "Push event from server: %s" ?data))

;; (defmethod -event-msg-handler :chsk/handshake
;;   [{:as ev-msg :keys [?data]}]
;;   (let [[?uid ?csrf-token ?handshake-data] ?data]
;;     (->output! "Handshake: %s" ?data)))

(defmethod -event-msg-handler :app/get-votes
  [{:as ev-msg :keys [?reply-fn]}]
  (if ?reply-fn
    (?reply-fn {:hi "hej"})
    ;; TODO use timbre
    (println "No reply fn")))

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router
    (sente/start-server-chsk-router!
      ch-chsk event-msg-handler)))


(defn start-server! [port]
  (let [[port stop-fn]
        (let [stop-fn (http-kit/run-server ring-handler {:port port})]
          [(:local-port (meta stop-fn)) (fn [] (stop-fn :timeout 100))])

        uri (format "http://localhost:%s/" port)]
    (println "Server running at %s" uri)
    (reset! server stop-fn)))

(defn stop-server! []
  (when-let [stop-fn @server] (stop-fn)))

(defn -main [& args]
  (start-server!))
