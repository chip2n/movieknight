(ns backend.db-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [<! >! go chan put!]]
            [com.stuartsierra.component :as component]
            [backend.system :as system]
            [backend.db :as db]
            [core.client :as client]
            [backend.test-utils :refer :all]))

(defmacro with-db [[db] & body]
  (let [dbname (gensym "dbname")]
    `(let [~dbname (format "movieknight-test-%s" (rand-str 16))
           ~db (db/create-jdbc "movieknight-test")]
       (db/execute ~db [(format "CREATE DATABASE \"%s\"" ~dbname)])
       (try
         (let [~db (db/create-jdbc ~dbname)]
           (db/setup ~db)
           ~@body)
         (finally
           (println "dropping" ~dbname)
           (db/execute ~db [(format "DROP DATABASE \"%s\"" ~dbname)]))))))

(deftest get-votes-test
  (testing "empty"
    (with-db [db]
      (is (= [] (db/get-votes db)))))
  (testing "non-empty"
    (with-db [db]
      (db/insert-account db #:user{:name "user1"})
      (db/insert-movie db #:movie{:title "movie1" :synopsis "synopsis1" :image-url "https://example.com/test1.png"})
      (db/insert-vote db #:vote{:user-id 1 :movie-id 1 :answer true})
      (is (= [#:vote{:user-id 1 :movie-id 1 :answer true}]
             (db/get-votes db))))))
