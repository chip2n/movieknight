(ns backend.socket
  (:require [taoensso.sente :as sente]
            [com.stuartsierra.component :as component]
            [backend.events :as events]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defrecord Websocket []
  component/Lifecycle

  (start [this]
    (println "Starting websocket")
    (let [{:keys [ch-recv send-fn connected-uids
                  ajax-post-fn ajax-get-or-ws-handshake-fn]}
          (sente/make-channel-socket!
           (get-sch-adapter)
           {:csrf-token-fn nil          ; TODO don't do this in prod!
            :packer :edn})]
      (assoc this
             :ring-ajax-post ajax-post-fn
             :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
             :ch-chsk ch-recv
             :chsk-send! send-fn
             :connected-uids connected-uids
             :router (sente/start-server-chsk-router! ch-recv events/event-msg-handler))))

  (stop [this]
    (println "Stopping websocket")
    (when-let [stop-fn (:router this)]
      (stop-fn))
    (assoc this
             :ring-ajax-post nil
             :ring-ajax-get-or-ws-handshake nil
             :ch-chsk nil
             :chsk-send! nil
             :connected-uids nil
             :router nil)))

(defn create []
  (map->Websocket {}))
