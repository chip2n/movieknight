(ns app.socket
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente :as sente :refer (cb-success?)]))

;; (def ?csrf-token
;;   (when-let [el (.getElementById js/document "sente-csrf-token")]
;;     (.getAttribute el "data-csrf-token")))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
       "/chsk"
       nil ;; TODO ?csrf-token
       {:type :auto :port 8020 :packer :edn})]

  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(comment
  (sente/chsk-disconnect! chsk)
  (chsk-send! [:chsk/close])
  (chsk-send! [:app/get-votes2 {:hello "hi"}])
  (chsk-send! [:app/get-votes {:hello "hi"}])
  (chsk-send! [:app/get-votes {:hello "hi"}]
              4000
              (fn [reply] (println "got reply" reply))))
