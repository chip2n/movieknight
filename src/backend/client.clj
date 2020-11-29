(ns backend.client
  (:require [taoensso.sente :as sente]
            [clojure.core.async :as async]
            [clojure.core.match :refer [match]]))

;; TODO can we share this code with cljs app?

(defn create [host port]
  {:host host
   :port port
   :socket nil})

(defn- create-socket [host port]
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk"
         nil ;; TODO ?csrf-token
         {:type :ws
          :host host
          :port port
          :packer :edn})]
    {:chsk chsk
     :ch-chsk ch-recv
     :chsk-send! send-fn
     :chsk-state state}))

(defn connect [{:keys [host port] :as client}]
  (let [socket (create-socket host port)]
    (async/go-loop []
      (let [msg (async/<! (:ch-chsk socket))]
        (match (:event msg)
               [:chsk/state [_ {:first-open? true}]] (assoc client :socket socket)
               :else (recur))))))

(defn disconnect [{:keys [socket] :as client}]
  (when socket
    (sente/chsk-disconnect! (:chsk socket))
    (assoc client :socket nil)))

(defn send-msg [{:keys [socket]} event & [timeout reply-fn]]
  (if socket
    ((:chsk-send! socket) event timeout reply-fn)
    (throw (ex-info "Client needs to be connected before sending messages" {:type :client-not-connected}))))

(comment
  (def client (atom nil))
  (println @client)

  (reset! client (create "localhost" 8020))
  (swap! client connect)

  (def temp (connect @client))
  (async/take! temp (fn [c] (reset! client c)))
  (println temp)

  (send-msg @client [:app/get-votes2 {:hello "hi"}])
  (send-msg @client [:app/get-votes {:hello "hi"}])
  (send-msg @client [:app/get-votes {:hello "hi"}]
        4000
        (fn [reply] (println "got reply" reply)))

  (swap! client disconnect))
