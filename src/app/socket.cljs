(ns app.socket
  (:require-macros
   [cljs.core.async.macros :refer (go go-loop)])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente :as sente :refer (cb-success?)]))

;; (def ?csrf-token
;;   (when-let [el (.getElementById js/document "sente-csrf-token")]
;;     (.getAttribute el "data-csrf-token")))

(defn create-socket []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk"
         nil ;; TODO ?csrf-token
         {:type :auto :port 8020 :packer :edn})]
    {:chsk chsk
     :ch-chsk ch-recv
     :chsk-send! send-fn
     :chsk-state state}))

(defn send [socket event & [timeout reply-fn]]
  ((:chsk-send! socket) event timeout reply-fn))

(defn disconnect [socket]
  (sente/chsk-disconnect! (:chsk socket)))

(defonce socket (atom nil))

(comment
  (reset! socket (create-socket))

  (send @socket [:app/get-votes2 {:hello "hi"}])
  (send @socket [:app/get-votes {:hello "hi"}])
  (send @socket [:app/get-votes {:hello "hi"}]
        4000
        (fn [reply] (println "got reply" reply)))

  (disconnect @socket))
