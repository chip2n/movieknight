(ns backend.events
  (:require [taoensso.timbre :as timbre]
            [backend.db :as db]
            [core.movie :as movie]
            [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

(timbre/refer-timbre)

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [db {:keys [id ?data event] :as ev-msg}]
  (-event-msg-handler (assoc ev-msg :db db)) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:keys [event id ?data ring-req ?reply-fn send-fn] :as ev-msg}]
  (let [session (:session ring-req)
        uid     (:uid     session)]

    ;; TODO use timbre
    ;; (debugf "Unhandled event: %s" event)
    (debugf "Unhandled event: %s" event)
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


(s/def ::response (s/keys :req-un [::users ::movies ::votes]))
(s/def ::users (s/coll-of ::user :kind vector?))
(s/def ::user (s/keys :req [:user/id :user/name]))
(s/def ::movies (s/coll-of ::movie :kind vector?))
(s/def ::movie (s/keys :req [:movie/id :movie/title :movie/synopsis :movie/rating :movie/image-url]))
(s/def ::votes (s/coll-of ::vote :kind vector?))
(s/def ::vote (s/keys :req [:vote/user-id :vote/movie-id :vote/answer]))

(defn map-keyword-ns [ns m]
  (into {} (map (fn [[k v]] [(keyword ns (name k)) v])) m))

(s/fdef get-initial-state
  :args (s/cat :db not-empty)
  :ret ::response)

(defn get-initial-state [db]
  (let [movies (db/get-movies db)
        accounts (->> (db/get-accounts db)
                      (map (partial map-keyword-ns "user"))
                      (into []))
        votes (db/get-votes db)]
    (st/select-spec
     ::response
     {:users accounts
      :movies movies
      :votes votes})))

(defmethod -event-msg-handler :app/get-initial-state
  [{:keys [db ?reply-fn] :as ev-msg}]
  (if ?reply-fn
    (?reply-fn (get-initial-state db))
    (debugf "No reply fn")))
