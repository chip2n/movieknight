(ns backend.socket
  (:require [taoensso.sente :as sente]
            [com.stuartsierra.component :as component]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

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

(defn create []
  (map->Websocket {}))
