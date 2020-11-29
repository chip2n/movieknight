(ns backend.end-to-end-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [<! >! go chan put!]]
            [backend.test-utils :refer [test-async]]
            [backend.client :as client]))

(defmacro with-connected-client [client & body]
  `(go
     (let [~client (-> (client/create "localhost" 8020) client/connect <!)]
       (try
         ~@body
         (finally (client/disconnect ~client))))))

(deftest end-to-end-test
  (testing "app/get-initial-state returns correct data"
    (test-async
     (let [ch (chan)]
       (with-connected-client client
         (client/send-msg client [:app/get-initial-state] 1000 (fn [x] (put! ch x)))
         (is (= {:users [{:id "user1" :name "User 1"} {:id "user2" :name "User 1"}]
                 :movies [{:id "movie1" :title "Movie 1" :synopsis "Synopsis" :rating 6.9 :image-url "https://example.com/image.png"}]}
                (<! ch))))))))
