(ns backend.end-to-end-test
  (:require [backend.db :as db]
            [backend.system :as system]
            [backend.test-utils :refer :all]
            [clojure.core.async :refer [<! chan go put!]]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [core.client :as client]))

(def port 8021)

(defmacro with-connected-client [client & body]
  `(go
     (let [~client (-> (client/create "localhost" port) client/connect <!)]
       (try
         ~@body
         (finally (client/disconnect ~client))))))

(defmacro with-system [[db] & body]
  (let [system (gensym "system")
        dbname (gensym "dbname")]
    `(let [~dbname (format "movieknight-test-%s" (rand-str 16))
           ~db (db/create-jdbc "movieknight-test")]
       (try
         ;; we don't need to run migrations etc since the database component already handles this
         (db/execute ~db [(format "CREATE DATABASE \"%s\"" ~dbname)])
         (let [~system (component/start (create-system ~dbname))]
           (try
             (let [~db (:database ~system)]
               ~@body)
             (finally
               (println ~system)
               (component/stop ~system))))
         (finally
           (println "dropping" ~dbname)
           (db/execute ~db [(format "DROP DATABASE \"%s\"" ~dbname)]))))))

(defn create-system [dbname]
  (system/create {:dbname dbname :port port}))

(deftest end-to-end-test
  (testing "app/get-initial-state returns correct data"
    (with-system [db]
      (let [user1 #:user{:id 1 :name "user1"}
            user2 #:user{:id 2 :name "user2"}
            movie1 #:movie{:id 1 :title "test1" :synopsis "synopsis1" :rating 6.8 :image-url "https://example.com/test1.png"}
            movie2 #:movie{:id 2 :title "test2" :synopsis "synopsis2" :rating 6.9 :image-url "https://example.com/test2.png"}
            vote1 #:vote{:user-id 1 :movie-id 2 :answer true}]
        (db/insert-account db {:name (:user/name user1)})
        (db/insert-account db {:name (:user/name user2)})
        (db/insert-movie db {:title (:movie/title movie1) :synopsis (:movie/synopsis movie1) :rating (:movie/rating movie1) :image-url (:movie/image-url movie1)})
        (db/insert-movie db {:title (:movie/title movie2) :synopsis (:movie/synopsis movie2) :rating (:movie/rating movie2) :image-url (:movie/image-url movie2)})
        (db/insert-vote db {:user-id (:vote/user-id vote1) :movie-id (:vote/movie-id vote1) :answer (:vote/answer vote1)})
        (test-async
         (let [ch (chan)]
           (with-connected-client client
             (client/send-msg client [:app/get-initial-state] 1000 (fn [x] (put! ch x)))
             (is (= {:users [user1 user2]
                     :movies [movie1 movie2]
                     :votes [vote1]}
                    (<! ch))))))))))
