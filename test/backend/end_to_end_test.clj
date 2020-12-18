(ns backend.end-to-end-test
  (:require [backend.db :as db]
            [backend.system :as system]
            [backend.test-utils :refer :all]
            [clojure.core.async :refer [<! <!! chan go put!]]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [core.client :as client]))

(def port 8021)

(defmacro with-client [client & body]
  `(<!! (go (let [~client (-> (client/create "localhost" port) client/connect <!)]
              (try ~@body
                   (finally (println "Disconnecting client") (client/disconnect ~client)))))))

(defmacro with-system [[db] & body]
  `(let [dbname# (format "movieknight-test-%s" (rand-str 16))
         db# (db/create-jdbc "movieknight-test")]
     (try
       ;; we don't need to run migrations etc since the database component already handles this
       (db/execute db# [(format "CREATE DATABASE \"%s\"" dbname#)])
       (let [sys# (component/start (create-system dbname#))
             ~db (:database sys#)]
         (try ~@body
              (finally (component/stop sys#))))
       (finally
         (println "Dropping" dbname#)
         (db/execute db# [(format "DROP DATABASE \"%s\"" dbname#)])))))

(defn create-system [dbname]
  (system/create {:dbname dbname :port port}))

(defn dummy-movie [i]
  #:movie {:id i
           :title (str "test" i)
           :synopsis (str "synopsis" i)
           :rating 6.8
           :image-url (format "https://example.com/test%s.png" i)})

(defn send-msg-with-reply [client msg]
  (test-async
   (let [ch (chan)]
     (client/send-msg client msg 1000 (fn [x] (put! ch x)))
     ch)))

(deftest end-to-end-test
  (testing "app/get-initial-state returns correct data"
    (let [user1 #:user{:id 1 :name "user1"}
          user2 #:user{:id 2 :name "user2"}
          movie1 (dummy-movie 1)
          movie2 (dummy-movie 2)
          vote1 #:vote{:user-id 1 :movie-id 2 :answer true}]
      (with-system [db]
        (db/insert-account db user1)
        (db/insert-account db user2)
        (db/insert-movie db movie1)
        (db/insert-movie db movie2)
        (db/insert-vote db vote1)

        (with-client client
          (is (= {:users [user1 user2]
                  :movies [movie1 movie2]
                  :votes [vote1]}
                 (send-msg-with-reply client [:app/get-initial-state]))))))))
