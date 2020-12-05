(ns backend.end-to-end-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [<! >! go chan put!]]
            [com.stuartsierra.component :as component]
            [backend.system :as system]
            [backend.client :as client]
            [backend.test-utils :refer [test-async]]))

(def port 8021)

(defmacro with-connected-client [client & body]
  `(go
     (let [~client (-> (client/create "localhost" port) client/connect <!)]
       (try
         ~@body
         (finally (client/disconnect ~client))))))

(defmacro with-system [& body]
  (let [system (gensym "system")]
    `(let [~system (create-system)]
       (try
         ~@body
         (finally (component/stop ~system))))))

(defn create-system []
  (component/start
   (system/create {:dbname "movieknight-test" :port port})))

(deftest end-to-end-test
  (testing "app/get-initial-state returns correct data"
    (with-system
      (test-async
       (let [ch (chan)]
         (with-connected-client client
           (client/send-msg client [:app/get-initial-state] 1000 (fn [x] (put! ch x)))
           (is (= {:users [{:id "user1" :name "User 1"} {:id "user2" :name "User 1"}]
                   :movies [{:id "movie1" :title "Movie 1" :synopsis "Synopsis" :rating 6.9 :image-url "https://example.com/image.png"}]}
                  (<! ch)))))))))
