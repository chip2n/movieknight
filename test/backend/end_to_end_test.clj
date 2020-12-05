(ns backend.end-to-end-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [<! >! go chan put!]]
            [com.stuartsierra.component :as component]
            [backend.system :as system]
            [backend.db :as db]
            [backend.client :as client]
            [backend.test-utils :refer :all]))

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
            movie1 #:movie{:id 1 :title "test1" :synopsis "synopsis1" :image-url "https://example.com/test1.png"}
            movie2 #:movie{:id 2 :title "test2" :synopsis "synopsis2" :image-url "https://example.com/test2.png"}]
        (db/insert-account db {:name (:user/name user1)})
        (db/insert-account db {:name (:user/name user2)})
        (db/insert-movie db {:title (:movie/title movie1) :synopsis (:movie/synopsis movie1) :image-url (:movie/image-url movie1)})
        (db/insert-movie db {:title (:movie/title movie2) :synopsis (:movie/synopsis movie2) :image-url (:movie/image-url movie2)})
        (test-async
         (let [ch (chan)]
           (with-connected-client client
             (client/send-msg client [:app/get-initial-state] 1000 (fn [x] (put! ch x)))
             (is (= {:users [user1 user2]
                     :movies [movie1 movie2]}
                    (<! ch))))))))))
