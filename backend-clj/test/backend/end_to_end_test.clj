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
         (client/send-msg client
                          [:app/get-initial-state]
                          1000
                          (fn [x] (put! ch x)))
         (is (= :yay (<! ch))))))))
