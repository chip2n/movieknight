(ns backend.events)

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

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
